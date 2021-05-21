package net.punchtree.battle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.trinoxtion.movement.MovementPlusPlus;
import com.trinoxtion.movement.MovementSystem;

import net.punchtree.battle.arena.BattleArena;
import net.punchtree.battle.arena.BattleGoal;
import net.punchtree.battle.gui.BattleGui;
import net.punchtree.minigames.arena.Arena;
import net.punchtree.minigames.game.GameState;
import net.punchtree.minigames.game.PvpGame;
import net.punchtree.minigames.lobby.Lobby;
import net.punchtree.minigames.utility.collections.CirculatingList;
import net.punchtree.minigames.utility.player.InventoryUtils;
import net.punchtree.minigames.utility.player.PlayerProfile;
import net.punchtree.minigames.utility.player.PlayerUtils;

public class BattleGame implements PvpGame {

	// class constants
	private static final int POSTGAME_DURATION_SECONDS = 10;
	private static final int WAVE_LENGTH_SECONDS = 15;
	private static final int GOAL_TICK_RATE_TICKS = 10;
	
	// PErsistent properties
	private final Lobby lobby;
	private final BattleGui gui;
	private final BattleArena arena;
	private final MovementSystem movement = MovementPlusPlus.CXOMS_MOVEMENT;
	// Two teams. More than two => KISS
	CirculatingList<BattleTeam> teams;
	private BattleTeam team1;
	private BattleTeam team2;
	private Set<BattleGoal> allGoals = new HashSet<>();
	
	// Listeners
	private final BattleEventListeners eventListeners;
	private final BattleGoalMovementListener goalMovementListener;
	
	// State fields
	private GameState gamestate = GameState.WAITING;
	
	private BattleTeam whoseWave = null;
	private double waveTimer = 0;
	private BukkitTask waveTask;
	
	private BukkitTask goalTicker;
	
	public BattleGame(BattleArena arena) {
		this.arena = arena;
		this.gui = new BattleGui(this);
		this.lobby = new Lobby(this, this::startGame, Battle.BATTLE_CHAT_PREFIX);
		
		teams = new CirculatingList<>(arena.teamBases.stream().map(BattleTeam::new).collect(Collectors.toList()), false);

		// Two teams. More than two => KISS
		team1 = teams.next();
		team2 = teams.next();

		initializeGoals();
		
		eventListeners = new BattleEventListeners(this);
		goalMovementListener = new BattleGoalMovementListener(this, allGoals);
	}
	
	private void initializeGoals() {
		team2.goalsToCaptureToWin.addAll(team1.getGoalSpecifications().stream().map(spec -> {
				return new BattleGoal(spec.center, spec.radius, team1.getColor(), team2, this);
			}).collect(Collectors.toSet()));
		team1.goalsToCaptureToWin.addAll(team2.getGoalSpecifications().stream().map(spec -> {
			return new BattleGoal(spec.center, spec.radius, team2.getColor(), team1, this);
		}).collect(Collectors.toSet()));
		allGoals.addAll(team1.goalsToCaptureToWin);
		allGoals.addAll(team2.goalsToCaptureToWin);
	}
	
	private void startGame(Set<Player> players) {
		
		List<Player> playersList = new ArrayList<>(players);
		Collections.shuffle(playersList);
		
		for ( Player player : playersList ) {
			
			BattleTeam team = teams.next();
			BattlePlayer bp = new BattlePlayer(player, team);
			team.addPlayer(bp);
			
			player.getInventory().clear();
			spawnPlayer(bp);
			InventoryUtils.equipPlayer(player, team.getColor());
			giveConcrete(player, team);
			
			movement.addPlayer(player);
			player.setInvulnerable(false);
			
			gui.addPlayer(bp);
			gui.initialSpawnIn(bp);
			
		}
		
		this.setGameState(GameState.RUNNING);
		
		gui.playStart();
		
		allGoals.forEach(BattleGoal::startAnimation);
		
		startWaveTask();
		startGoalTicker();
	}
	
	private void startWaveTask() {
		// TODO make sure this is consistently cancelled
		waveTask = new BukkitRunnable() {
			@Override
			public void run() {
				if ( whoseWave == null ) return;
				
				if (!isTeamCapturing(whoseWave)) {					
					waveTimer -= .05;
					if ( waveTimer <= 0 ) {
						waveTimer = WAVE_LENGTH_SECONDS;
						setWhoseWave(getNextWaveTeam());
					}
				}
				gui.playWaveTick(whoseWave, waveTimer);
			}
			
			private BattleTeam getNextWaveTeam() {
				// Two teams. More than two => KISS
				// This won't be called as long as whoseWave is null
				return whoseWave == team1 ? team2 : team1;
			}
		}.runTaskTimer(Battle.getPlugin(), 0, 1);
	}
	
	private void setWhoseWave(BattleTeam team) {
		whoseWave = team;
		gui.playWaveChange(team);
	}

	private void setGameState(GameState gamestate) {
		this.gamestate = gamestate;
	}
	
	private void startGoalTicker() {
		goalTicker = new BukkitRunnable() {
			@Override
			public void run() {
				allGoals.forEach(BattleGoal::tickProgress);
			}
		}.runTaskTimer(Battle.getPlugin(), 0, GOAL_TICK_RATE_TICKS);
	}
	
	private boolean isTeamCapturing(BattleTeam team) {
		for (BattleGoal goal : team.goalsToCaptureToWin) {
			if (goal.isCapturing()) return true;
		}
		return false;
	}
	
	// TODO move and debrittle
	private static ItemStack RED_TEAM = new ItemStack(Material.RED_CONCRETE);
	private static ItemStack BLUE_TEAM = new ItemStack(Material.BLUE_CONCRETE);
	static {
		ItemMeta rtmeta = RED_TEAM.getItemMeta();
		rtmeta.setDisplayName(ChatColor.DARK_RED + "Red Team");
		RED_TEAM.setItemMeta(rtmeta);
		
		ItemMeta btmeta = BLUE_TEAM.getItemMeta();
		btmeta.setDisplayName(ChatColor.DARK_BLUE + "Blue Team");
		BLUE_TEAM.setItemMeta(btmeta);
	}
	private void giveConcrete(Player player, BattleTeam team) {
		if ( ! (team.getName().equalsIgnoreCase("Red") || team.getName().equalsIgnoreCase("Blue"))) { return; }
		if (team.getName().equalsIgnoreCase("Red")) {
			player.getInventory().setItem(2, RED_TEAM);
			player.getInventory().setItem(3, RED_TEAM);
			player.getInventory().setItem(4, RED_TEAM);
			player.getInventory().setItem(5, RED_TEAM);
			player.getInventory().setItem(6, RED_TEAM);
			player.getInventory().setItem(7, RED_TEAM);
			player.getInventory().setItem(8, RED_TEAM);
		} else {
			player.getInventory().setItem(2, BLUE_TEAM);
			player.getInventory().setItem(3, BLUE_TEAM);
			player.getInventory().setItem(4, BLUE_TEAM);
			player.getInventory().setItem(5, BLUE_TEAM);
			player.getInventory().setItem(6, BLUE_TEAM);
			player.getInventory().setItem(7, BLUE_TEAM);
			player.getInventory().setItem(8, BLUE_TEAM);
		}
	}
	
	public void spawnPlayer(BattlePlayer bp) {
		Player player = bp.getPlayer();
		PlayerUtils.perfectStats(player);
		player.teleport(bp.getTeam().getNextSpawn());
	}
	
	/**
	 * Used for *force* stopping a game (not regular game ending by a win)
	 */
	public void interruptAndShutdown() {
		gui.playStop();
		
		resetGame();
		lobby.removeAndRestoreAll();
		
		setGameState(GameState.STOPPED);
	}
	
	private void resetGame() {
		gui.reset();
		
		// Reset players
		for (BattlePlayer bp : team1.getPlayers()) {
			Player player = bp.getPlayer();
			movement.removePlayer(player);
			PlayerProfile.restore(player);
		}
		for (BattlePlayer bp : team2.getPlayers()) {
			Player player = bp.getPlayer();
			movement.removePlayer(player);
			PlayerProfile.restore(player);
		}
		team1.getPlayers().clear();
		team2.getPlayers().clear();
		
		resetWave();
		resetGoals();
		
		this.setGameState(GameState.WAITING);
		
	}
	
	private void resetWave() {
		if (this.waveTask != null) {			
			this.waveTask.cancel();
			this.waveTask = null;
		}
		this.whoseWave = null;
		this.waveTimer = 0;
	}
	
	private void resetGoals() {
		if (this.goalTicker != null) {			
			this.goalTicker.cancel();
			this.goalTicker = null;
		}
		for (BattleGoal goal : allGoals) {
			goal.reset();
		}
	}
	
	public boolean removePlayerFromGame(Player player) {
		if (!hasPlayer(player.getUniqueId())) { return false; }
		
		// TODO Removing logic
		BattlePlayer bplayer = getPlayer(player.getUniqueId());
		goalMovementListener.onDieOrLeave(bplayer, player.getLocation());
		
		// Restore stats and location
		movement.removePlayer(player);
		bplayer.getTeam().removePlayer(bplayer);
		gui.removePlayer(player);

		PlayerProfile.restore(player);

		// End the game if it gets down to one (team) left.
		if (bplayer.getTeam().getSize() == 0) {
			gui.playTooManyLeft();
			resetGame();
		}
		return true;
	}
	
	public boolean removePlayerFromLobby(Player player) {
		if (!lobby.hasPlayer(player)) { return false; }
		
		lobby.removeAndRestorePlayer(player);
		return true;
	}
	
	@Override
	public String getName() {
		return "Battle";
	}

	public Lobby getLobby() {
		return lobby;
	}

	@Override
	public Arena getArena() {
		return arena;
	}

	@Override
	public GameState getGameState() {
		return gamestate;
	}
	
	public boolean hasPlayer(Player player) {
		return hasPlayer(player.getUniqueId());
	}
	
	public boolean hasPlayer(UUID uuid) {
		return getPlayer(uuid) != null;
	}
	
	public BattlePlayer getPlayer(UUID uniqueId) {
		BattlePlayer bp = team1.getPlayer(uniqueId);
		return bp != null ? bp : team2.getPlayer(uniqueId);
	}
	
	public boolean canCapture(BattleTeam team) {
		return whoseWave == null || whoseWave == team;
	}
	
	public void onStartCapturing(BattleGoal goal, BattlePlayer capturer) {
		BattleTeam capturingTeam = goal.getTeam();
		Bukkit.broadcastMessage(capturingTeam.getChatColor() + capturingTeam.getName() + " goal capturing started!");
		if (whoseWave == null) {
			setWhoseWave(capturingTeam);
		}
		if (whoseWave == capturingTeam) {
			waveTimer = WAVE_LENGTH_SECONDS;
		}
	}
	
	public void onStopCapturing(BattleGoal goal) {
		BattleTeam capturingTeam = goal.getTeam();
//		Bukkit.broadcastMessage(capturingTeam.getChatColor() + capturingTeam.getName() + " goal capturing stopped!");
	
	}
	
	public void onCaptureGoal(BattleGoal capturedGoal) {
		BattleTeam capturingTeam = capturedGoal.getTeam();
//		Bukkit.broadcastMessage(capturingTeam.getChatColor() + capturingTeam.getName() + " goal captured!");
		capturedGoal.getLocation().getWorld().playSound(capturedGoal.getLocation(), Sound.BLOCK_END_PORTAL_SPAWN, 2, 1);
		if (teamHasWon(capturingTeam)) {
			// Maybe name as end game?
			runPostgameWithWinner(capturingTeam);
		}
	}
	
	private boolean teamHasWon(BattleTeam team) {
		return team.goalsToCaptureToWin.stream().allMatch(BattleGoal::isCaptured);
	}
	
	// -------------------------- //
	// ---- EVENT RESPONDERS ---- //
	// -------------------------- //
	
	public void handleKill(Player killer, Player killed, EntityDamageByEntityEvent edbee) {
		
		if (getGameState() != GameState.RUNNING) return;
		
		BattlePlayer bpKiller = getPlayer(killer.getUniqueId());
		BattlePlayer bpKilled = getPlayer(killed.getUniqueId());
		Location killLocation = killed.getLocation();
		
		// if suicide, not a kill
		if (bpKiller.equals(bpKilled)) {
			handleNonPvpDeath(killed, edbee);
			return;
		}
		
		// cancel damage
		edbee.setCancelled(true);
		
		goalMovementListener.onDieOrLeave(bpKilled, killLocation);
		this.spawnPlayer(bpKilled);
		
		gui.playKill(bpKiller, bpKilled, edbee, killLocation);
	}

	public void handleNonPvpDeath(Player killed, EntityDamageEvent e) {
		// TODO Auto-generated method stub
		BattlePlayer bpKilled = getPlayer(killed.getUniqueId());
		Location deathLocation = killed.getLocation();
		
		e.setCancelled(true);
		//Let player take non-fatal damage that isn't caused by a fall or firework explosion
		if (e.getCause() != DamageCause.FALL && e.getCause() != DamageCause.ENTITY_EXPLOSION){
			((Player) e.getEntity()).setHealth(1);
		}

		gui.playDeath(bpKilled, e, deathLocation);
	}
	
	private void runPostgameWithWinner(BattleTeam winner) {
		gui.playPostgame(winner, BattleGame.POSTGAME_DURATION_SECONDS);
		
		// Cancel the goal ticker here to prevent capturing in the postgame
		if (this.goalTicker != null) {
			this.goalTicker.cancel();
			this.goalTicker = null;
		}
		
		setGameState(GameState.ENDING);
		
		// TODO Seriously wth is this player tracking
		team1.getPlayers().forEach(mp -> {
			mp.getPlayer().setGameMode(GameMode.ADVENTURE);
			mp.getPlayer().setAllowFlight(true);
			mp.getPlayer().setInvulnerable(true);
		});
		team2.getPlayers().forEach(mp -> {
			mp.getPlayer().setGameMode(GameMode.ADVENTURE);
			mp.getPlayer().setAllowFlight(true);
			mp.getPlayer().setInvulnerable(true);
		});
		
		resetAfterPostgame();
	}
	
	private void resetAfterPostgame() {
		new BukkitRunnable() {
			@Override
			public void run() {
				BattleGame.this.resetGame();
			}
		}.runTaskLater(Battle.getPlugin(), BattleGame.POSTGAME_DURATION_SECONDS * 20);
	}
	
}
