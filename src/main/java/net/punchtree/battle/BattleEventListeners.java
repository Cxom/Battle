package net.punchtree.battle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class BattleEventListeners implements Listener {

	private final BattleGame game;
	
	// Working variables
	EntityDamageEvent ede;
	EntityDamageByEntityEvent edbee;
	UUID entityId;
	Player killed;
	Player killer;
	
	BattleEventListeners(BattleGame game) {
		this.game = game;
		
		Bukkit.getPluginManager().registerEvents(this, Battle.getPlugin());
	}
	
	// Kill and Death events
	
	@EventHandler
	public void onPlayerDeath(EntityDamageEvent ede) {
		
		this.ede = ede;
		UUID entityId = ede.getEntity().getUniqueId();
		
		if (!isPlayerInTheGame(ede.getEntity())) return;
		killed = (Player) ede.getEntity();
		
		if (!damageIsLethal()) return;
		killer = null;
		edbee = null;
		
		if (ede instanceof EntityDamageByEntityEvent) {
			edbee = (EntityDamageByEntityEvent) ede;
			Entity killingEntity = edbee.getDamager();
			
			// Determine killer
			if (isPlayerInTheGame(killingEntity)) {
				killer = (Player) killingEntity;
			} else if (isShotByPlayerInTheGame(killingEntity)) {
				killer = (Player) ((Projectile) killingEntity).getShooter();
				killingEntity.remove();
			}
		}
		
		if (killer != null) {
			game.handleKill(killer, killed, edbee);
		} else {
			game.handleNonPvpDeath(killed, ede);
		}
		
	}
	
	private boolean isPlayerInTheGame(Entity entity) {
		return game.hasPlayer(entity.getUniqueId());
	}
	
	private boolean damageIsLethal() {
		return ede.getFinalDamage() >= killed.getHealth();
	}
	
	private boolean isShotByPlayerInTheGame(Entity killingEntity) {
		if ( ! (killingEntity instanceof Projectile )) return false;
		Projectile projectile = (Projectile) killingEntity;
		
		if ( ! (projectile.getShooter() instanceof Player)) return false;
		
		Player shooter = (Player) projectile.getShooter();
		return isPlayerInTheGame(shooter);
	}
	
	
	// Quit & Leave Events
	
	@EventHandler
	public void onPlayerLeaveServer(PlayerQuitEvent e){
		if ( ! game.removePlayerFromGame(e.getPlayer())) {
			 game.removePlayerFromLobby(e.getPlayer());
		}
	}
	
	//This was a preprocess event so that the game the player is in can be determined,
	//but this could probably be done properly
	@EventHandler
	public void onBattleLeaveCommand(PlayerCommandPreprocessEvent e) {
		if (! e.getMessage().toLowerCase().startsWith("/leave")) return;
		if (game.removePlayerFromGame(e.getPlayer()) || game.removePlayerFromLobby(e.getPlayer())){
			e.setCancelled(true); //Prevents normal command execution
		}
	}

	// Cancelled Events
		
	@EventHandler
	public void onEnvironmentDamage(EntityDamageEvent e) {
		if (entityIsInGame(e.getEntity()) && damageCauseIsProtected(e.getCause())){
			e.setCancelled(true);
		}
	}
	
	static boolean damageCauseIsProtected(DamageCause cause) {
		//Fall Damage is off so movement system is non lethal
		//Entity Explosion is off to prevent firework damage from kills
		return cause == DamageCause.FALL || cause == DamageCause.ENTITY_EXPLOSION;
	}

	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent e) {
		if (entityIsInGame(e.getEntity())){
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerRegainHealth(EntityRegainHealthEvent e) {
		if (entityIsInGame(e.getEntity())){
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerOpenInventoryEvent(InventoryOpenEvent e) {
		if (e.getInventory().getType() != InventoryType.PLAYER && game.hasPlayer(e.getPlayer().getUniqueId())) {			
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerBlockBreak(BlockBreakEvent e) {
		if (game.hasPlayer(e.getPlayer())) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerBlockPlace(BlockPlaceEvent e) {
		if (game.hasPlayer(e.getPlayer())) {
			e.setCancelled(true);
		}
	}
	
	private boolean entityIsInGame(Entity entity) {
		return entity instanceof Player && game.hasPlayer(entity.getUniqueId());
	}
	
	// TODO config value in PunchTree-Minigames??
	private static final List<String> cmds = new ArrayList<String>(Arrays.asList(new String[] {
			"/m", "/msg", "/message", "/t", "/tell", "/w", "/whisper", "/r",
			"/reply", "/ac", "/helpop", "/leave"}));

	@EventHandler
	public void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent e) {
		Player player = e.getPlayer();
		String command = e.getMessage().toLowerCase() + " ";
		if ((game.hasPlayer(player) || game.getLobby().hasPlayer(player))
		 && ! player.isOp()
		 && ! cmds.contains(command.split(" ")[0])) {
			
			e.setCancelled(true);
			player.sendMessage(Battle.BATTLE_CHAT_PREFIX + ChatColor.RED + "You do not have permission to use non-messaging commands in Melee. If you wish to leave the match, type /melee leave.");
			
		}
	}
	
}
