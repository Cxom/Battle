package net.punchtree.battle;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.punchtree.minigames.region.Area;
import net.punchtree.minigames.utility.color.ColoredPlayer;
import net.punchtree.util.color.PunchTreeColor;

public class BattlePlayer implements ColoredPlayer {

private final UUID uuid;
	
	private BattleTeam team;
	
	public BattlePlayer(Player player, BattleTeam team){
		this.uuid = player.getUniqueId();
		this.team = team;
	}
	
	public UUID getUniqueId(){
		return uuid;
	}
	
	public Player getPlayer(){
		return Bukkit.getPlayer(uuid);
	}
	
	public BattleTeam getTeam() {
		return team;
	}
	
	public boolean isFree(Area allJails){
		return !allJails.contains(getPlayer().getLocation());
	}
	
//	public void setFree(boolean free){
//		this.free = free;
//	}

	@Override
	public PunchTreeColor getColor() {
		return team.getColor();
	}
	
}
