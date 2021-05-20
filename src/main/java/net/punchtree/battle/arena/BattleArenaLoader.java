package net.punchtree.battle.arena;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import net.punchtree.battle.arena.BattleArena.BattleArenaTeamBase;
import net.punchtree.battle.arena.BattleArena.BattleGoalSpecification;
import net.punchtree.battle.arena.BattleArena.GoalSpecification;
import net.punchtree.minigames.arena.creation.ArenaLoader;
import net.punchtree.util.color.PunchTreeColor;

public class BattleArenaLoader extends ArenaLoader {

	public static BattleArena load(FileConfiguration arenacfg) {
		String name = arenacfg.getString("name");
		
		List<BattleArenaTeamBase> teamBases = getList(arenacfg.getConfigurationSection("teambases"), BattleArenaLoader::getTeamBase);
		Optional<GoalSpecification> centerpoint = getCenterpoint(arenacfg);
		
		// Remove any failed (null) teambases from loading
		teamBases.removeAll(Collections.singleton(null));
		if (teamBases.size() < 2) return null;
		
		Location pregameLobby;
		if (arenacfg.isConfigurationSection("lobby")) {
			pregameLobby = getLocation(arenacfg.getConfigurationSection("lobby"));
		} else if (centerpoint.isPresent()){
			pregameLobby = centerpoint.get().center;
		} else {
			pregameLobby = teamBases.get(0).spawns.get(0);
		}
		
		int playersToStart = arenacfg.getInt("playersToStart", teamBases.size());
		
		return new BattleArena(name, pregameLobby, playersToStart, teamBases, centerpoint);
	}
	
	public static BattleArenaTeamBase getTeamBase(ConfigurationSection section) {
		String name = section.getString("name");
		PunchTreeColor color = getColor(section.getConfigurationSection("color"));
		if (name == null && color == null) return null;
		if (name == null) {
			name = StringUtils.capitalize(color.getChatColor().name());
		} else if (color == null) {
			color = PunchTreeColor.valueOf(name);
		}
		
		World world = getRootWorld(section);
		if (world == null) return null;
		
		List<Location> spawns = getList(section.getConfigurationSection("spawns"),
				(ConfigurationSection spawn) -> getLocation(spawn, world));
		
		List<BattleGoalSpecification> goals = getList(section.getConfigurationSection("goals"), BattleArenaLoader::getBattleGoal);
	
		return new BattleArenaTeamBase(name, color, spawns, goals);
	}
	
	public static BattleGoalSpecification getBattleGoal(ConfigurationSection section) {
		Location center = getLocation(section.getConfigurationSection("center"));
		
		double radius = section.getDouble("radius", 2);
		return new BattleGoalSpecification(center, radius);
	}
	
	public static GoalSpecification getGoal(ConfigurationSection section) {
		Location center = getLocation(section.getConfigurationSection("center"));
		
		double radius = section.getDouble("radius", 2);
		return new GoalSpecification(center, radius);
	}
	
	private static Optional<GoalSpecification> getCenterpoint(ConfigurationSection arenacfg) {
		if (arenacfg.isConfigurationSection("centerpoint")) {
			return Optional.of(getGoal(arenacfg.getConfigurationSection("centerpoint")));
		}
		return Optional.empty();
	}
	
	
	
}
