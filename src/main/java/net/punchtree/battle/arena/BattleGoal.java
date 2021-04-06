package net.punchtree.battle.arena;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import net.punchtree.battle.BattlePlayer;
import net.punchtree.battle.BattleTeam;

public class BattleGoal {
	
	private final BattleTeam team;
	private final Location center;
	private final double radius;
	
	private final Set<BattlePlayer> teammatesOnGoal = new HashSet<>();
	private final Set<BattlePlayer> enemiesOnGoal = new HashSet<>();
	
	private final Consumer<BattleGoal> onCapture;
	
//	private final GoalAnimation goalAnimation;
	
	private static final float PROGRESS_PER_TICK = .01f;
	private float progress = 0;
	
	public BattleGoal(Location center, double radius, BattleTeam team, Consumer<BattleGoal> onCapture) {
		this.center = center;
		this.radius = radius;
		this.team = team;
		this.onCapture = onCapture;
		// TODO add color
//		this.goalAnimation = new GoalAnimation();
	}
	
	public boolean isOnGoal(Location location) {
		return location.getWorld() == center.getWorld() && location.distance(center) <= radius;
	}
	
	public void playerOnGoal(BattlePlayer bp) {
		if (bp.getTeam().equals(team)) {
			teammatesOnGoal.add(bp);
		} else {
			enemiesOnGoal.add(bp);
		}
	}
	
	public void playerOffGoal(BattlePlayer bp) {
		if (bp.getTeam().equals(team)) {
			teammatesOnGoal.remove(bp);
		} else {
			enemiesOnGoal.remove(bp);
		}
	}
	
	public void tickProgress() {
		// Lets assume twice a second -> 50 seconds to capture
		if (!teammatesOnGoal.isEmpty() && enemiesOnGoal.isEmpty()) {
			float progressRate = teammatesOnGoal.size() *  PROGRESS_PER_TICK;
			setProgress(progress + progressRate);
		}
	}
	
	public void setProgress(float newProgress) {
		float oldProgress = progress;
		progress = Math.min(1f, Math.max(0f, newProgress));
		animateProgress(oldProgress, progress);
		if (oldProgress != 1f && progress == 1f) {
			onCapture.accept(this);
		}
	}
	
	private void animateProgress(float oldProgress, float newProgress) {
		Bukkit.broadcastMessage(team.getChatColor() + "" + oldProgress + " -> " + newProgress);
	}

	public BattleTeam getTeam() {
		return team;
	}

	public void reset() {
		teammatesOnGoal.clear();
		enemiesOnGoal.clear();
		progress = 0f;
	}
	
}
