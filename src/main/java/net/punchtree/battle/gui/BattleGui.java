package net.punchtree.battle.gui;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scoreboard.Scoreboard;

import net.punchtree.battle.Battle;
import net.punchtree.battle.BattleGame;
import net.punchtree.battle.BattlePlayer;
import net.punchtree.battle.BattleTeam;
import net.punchtree.minigames.game.pvp.AttackMethod;
import net.punchtree.minigames.gui.Killfeed;

public class BattleGui {

	private final BattleGame game;
	
	private final BattleBossBar bossbar;
	private final Scoreboard scoreboard;
	private final Killfeed killfeed;
//	private final BattleTabList tablist;
	
	private final Set<Player> players = new HashSet<>();
	
	public BattleGui(BattleGame game) {
		this.game = game;
		
		this.bossbar = new BattleBossBar();
//		this.bossbar.setStyle(BarStyle.SEGMENTED_10);
		this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
		this.killfeed = new Killfeed(scoreboard, Battle.BATTLE_CHAT_PREFIX);
	}

	public void addPlayer(BattlePlayer bp) {
		Player player = bp.getPlayer();
		
		players.add(player);
		player.setScoreboard(scoreboard);
		bossbar.addPlayer(player);
	}
	
	public void removePlayer(Player player) {
		player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
		players.remove(player);
		bossbar.removePlayer(player);
	}

	public void removeAll() {
		players.forEach(player -> player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard()));
		players.clear();
		bossbar.removeAll();
	}
	
	public void playStart() {
		// TODO Auto-generated method stub
		Bukkit.broadcastMessage("gui::playStart");
	}
	
	public void sendKill(BattlePlayer killer, BattlePlayer killed, AttackMethod attackMethod) {
		killfeed.sendKill(killer, killed, attackMethod);
	}

	/**
	 * When a player is added to the game (not a spectator)
	 * @param bp - The BattlePlayer
	 */
	public void initialSpawnIn(BattlePlayer bp) {
		// TODO Auto-generated method stub
		Bukkit.broadcastMessage("gui::initialSpawnIn (" + bp.getPlayer().getName() + ")");
	}

	public void playKill(BattlePlayer killer, BattlePlayer killed, EntityDamageByEntityEvent edbee, Location killLocation) {
		//Send killfeed message
		AttackMethod attackMethod = AttackMethod.getAttackMethod(edbee.getDamager());
		killfeed.sendKill(killer, killed, attackMethod);
	}
	
	public void playDeath(BattlePlayer killed, EntityDamageEvent ede, Location deathLocation) {
		// Currently, non-kill death doesn't happen, so nothing we want to do
	}

	public void playStop() {
		Bukkit.broadcastMessage("gui::playStop");
	}

	public void reset() {
		// TODO Auto-generated method stub
		players.forEach(player -> player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard()));
		players.clear();
		bossbar.removeAll();
//		tablist.removeAll();
		bossbar.reset();
	}

	public void playTooManyLeft() {
		Bukkit.broadcastMessage("Too many left");
	}

	public void playPostgame(BattleTeam winner, int postgameDurationSeconds) {
		Bukkit.broadcastMessage("gui::playPostgame ("  + winner.getName() + ")");
	}

	public void playWaveChange(BattleTeam team) {
		Bukkit.broadcastMessage("gui::playWaveChange (" + team.getName() + ")");
		bossbar.setWave(team);
	}

	public void playWaveTick(double waveTimerSeconds) {
		bossbar.setSeconds(waveTimerSeconds);
	}
	
}
