package net.punchtree.battle.arena;

import java.util.List;
import java.util.Optional;

import org.bukkit.Location;

import net.punchtree.minigames.arena.Arena;
import net.punchtree.util.color.PunchTreeColor;

public class BattleArena extends Arena {

	public final List<BattleArenaTeamBase> teamBases;
	public final Optional<GoalSpecification> centerpoint;
	
	public BattleArena(String name, Location pregameLobby, int playersNeededToStart, List<BattleArenaTeamBase> teamBases, Optional<GoalSpecification> centerpoint) {
		super(name, pregameLobby, playersNeededToStart);
		this.teamBases = teamBases;
		this.centerpoint = centerpoint;
	}
	
	public static class GoalSpecification {
		public final Location center;
		public final double radius;
		
		public GoalSpecification(Location center, double radius) {
			this.center = center;
			this.radius = radius;
		}
	}
	
	public static class BattleGoalSpecification extends GoalSpecification {
		public BattleGoalSpecification(Location center, double radius) {
			super(center, radius);
		}
	}
	
	public static class BattleArenaTeamBase {
		public final String name;
		public final PunchTreeColor color;
		public final List<Location> spawns;
		public final List<BattleGoalSpecification> goals;
		
		public BattleArenaTeamBase(String name, PunchTreeColor color, List<Location> spawns, List<BattleGoalSpecification> goals) {
			this.name = name;
			this.spawns = spawns;
			this.goals = goals;
			this.color = color;
		}
	}

}
