package net.punchtree.battle;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;

import net.punchtree.battle.arena.BattleArena.BattleArenaTeamBase;
import net.punchtree.battle.arena.BattleArena.BattleGoalSpecification;
import net.punchtree.minigames.utility.collections.CirculatingList;
import net.punchtree.minigames.utility.color.MinigameColor;

public class BattleTeam {

	private final String name;
	private final MinigameColor color;
	
	private final BattleArenaTeamBase arenaTeamBase;
	
	private final CirculatingList<Location> circulatingSpawns;
	
	private Map<UUID, BattlePlayer> bplayers = new HashMap<>();
	
	public BattleTeam(BattleArenaTeamBase arenaTeamBase) {
		this.name = arenaTeamBase.name;
		this.color = arenaTeamBase.color;
		
		this.arenaTeamBase = arenaTeamBase;
		
		this.circulatingSpawns = new CirculatingList<>(arenaTeamBase.spawns, false);
	}
	
	public void addPlayer(BattlePlayer bp) {
		bplayers.put(bp.getUniqueId(), bp);
	}
	
	// TODO common jeez better player management idk what this team thing is
	BattlePlayer getPlayer(UUID uniqueId) {
		return bplayers.get(uniqueId);
	}
	
	Collection<BattlePlayer> getPlayers() {
		return bplayers.values();
	}
	
	public void removePlayer(BattlePlayer bp) {
		bplayers.remove(bp.getUniqueId());
	}
	
	public String getName(){
		return name;
	}
	
	public MinigameColor getColor(){
		return color;
	}
	
	public ChatColor getChatColor(){
		return color.getChatColor();
	}
	
	public List<Location> getSpawns(){
		return arenaTeamBase.spawns;
	}
	
	public List<BattleGoalSpecification> getGoalSpecifications() {
		return arenaTeamBase.goals;
	}
	
	public Location getNextSpawn() {
		return circulatingSpawns.next();
	}

//	public Goal getGoal() {
//		return goal;
//	}

//	public Area getJails() {
//		return jails;
//	}
	
	public int getSize(){
		return bplayers.size();
	}
	
//	public int getAlive(Area allJails) {
//		int alive = 0;
//		for (JailbreakPlayer jp : jplayers) {
//			if (jp.isFree(allJails)) {
//				++alive;
//			}
//		}
//		return alive;
//	}
	
//	public void setAlive(int alive){
//		this.alive = alive;
//	}
//	
//	public void incrementAlive(){
//		alive++;
//	}
	
//	public void decrementAlive(){
//		alive--;
//	}
//	
//	@Override
//	public boolean equals(Object o){
//		if (! (o instanceof BattleTeam)) return false;
//		BattleTeam team = (BattleTeam) o;
//		return team.getName().equals(name) && team.getGoal().equals(goal);
//		// Comparing goals is arbitrary, it just prevents errors from comparing teams from different maps
//	}
	
//	public void reset() {
//		bplayers.clear();
//		
//		// Clear team base details (or in arena method???)
//	}
	
}
