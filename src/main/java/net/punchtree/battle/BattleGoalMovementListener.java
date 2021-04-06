package net.punchtree.battle;

import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import net.punchtree.battle.arena.BattleGoal;

public class BattleGoalMovementListener implements Listener {

	private final BattleGame game;
	private final Collection<BattleGoal> allGoals;
	
	public BattleGoalMovementListener(BattleGame game, Collection<BattleGoal> allGoals) {
		this.game = game;
		this.allGoals = allGoals;
		
		Bukkit.getPluginManager().registerEvents(this, Battle.getPlugin());
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		if (!game.hasPlayer(event.getPlayer())) return;
		BattlePlayer bp = game.getPlayer(event.getPlayer().getUniqueId());
		for (BattleGoal goal : allGoals) {
			if(goal.isOnGoal(event.getTo()) && !goal.isOnGoal(event.getFrom())){
//				Bukkit.getServer().getPluginManager().callEvent(new PlayerOnGoalEvent(this, Jailbreak.getPlayer(e.getPlayer())));
				goal.playerOnGoal(bp);
			} else if (goal.isOnGoal(event.getFrom()) && !goal.isOnGoal(event.getTo())){
//				Bukkit.getServer().getPluginManager().callEvent(new PlayerOffGoalEvent(this, Jailbreak.getPlayer(e.getPlayer())));
				goal.playerOffGoal(bp);
			}
		}
	}
	
	public void onDieOrLeave(BattlePlayer dead, Location deathOrLeaveLocation) {
		for (BattleGoal goal : allGoals) {
			if (goal.isOnGoal(deathOrLeaveLocation)) {
				goal.playerOffGoal(dead);
			}
		}
	}
	
}
