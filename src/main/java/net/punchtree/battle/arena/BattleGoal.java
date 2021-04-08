package net.punchtree.battle.arena;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import net.punchtree.battle.BattleGame;
import net.punchtree.battle.BattlePlayer;
import net.punchtree.battle.BattleTeam;

public class BattleGoal {
	
	private final BattleTeam capturingTeam;
	private final Location center;
	private final double radius;
	
	private final Set<BattlePlayer> attackersOnGoal = new LinkedHashSet<>();
	private final Set<BattlePlayer> defendersOnGoal = new LinkedHashSet<>();
	
	private final BattleGame game;
	
//	private final GoalAnimation goalAnimation;
	
	private static final float PROGRESS_PER_TICK = .01f;
	private float progress = 0;
	
	public BattleGoal(Location center, double radius, BattleTeam capturingTeam, BattleGame game) {
		this.center = center;
		this.radius = radius;
		this.capturingTeam = capturingTeam;
		this.game = game;
		// TODO add color
//		this.goalAnimation = new GoalAnimation();
	}
	
	public boolean isOnGoal(Location location) {
		return location.getWorld() == center.getWorld() && location.distance(center) <= radius;
	}
	
	public void playerOnGoal(BattlePlayer bp) {
		boolean wasCapturing = isCapturing();
		if (bp.getTeam().equals(capturingTeam)) {
			attackersOnGoal.add(bp);
		} else {
			defendersOnGoal.add(bp);
		}
		if (!wasCapturing && isCapturing()) {
			game.onStartCapturing(this, attackersOnGoal.iterator().next());
		} else if (wasCapturing && !isCapturing()) {
			game.onStopCapturing(this);
		}
	}
	
	public void playerOffGoal(BattlePlayer bp) {
		boolean wasCapturing = isCapturing();
		if (bp.getTeam().equals(capturingTeam)) {
			attackersOnGoal.remove(bp);
		} else {
			defendersOnGoal.remove(bp);
		}
		if (!wasCapturing && isCapturing()) {
			game.onStartCapturing(this, attackersOnGoal.iterator().next());
		} else if (wasCapturing && !isCapturing()) {
			game.onStopCapturing(this);
		}
	}
	
	public boolean isCapturing() {
		return game.canCapture(capturingTeam) 
				&& attackersOnGoal.size() > 0 
				&& defendersOnGoal.isEmpty()
				&& !isCaptured();
	}
	
	public void tickProgress() {
		// Lets assume twice a second -> 50 seconds to capture
		if (isCapturing()) {
			float progressRate = attackersOnGoal.size() *  PROGRESS_PER_TICK;
			setProgress(progress + progressRate);
		}
	}
	
	public void setProgress(float newProgress) {
		float oldProgress = progress;
		progress = Math.min(1f, Math.max(0f, newProgress));
		animateProgress(oldProgress, progress);
		if (oldProgress != 1f && progress == 1f) {
			game.onCaptureGoal(this);
		}
	}
	
	private void animateProgress(float oldProgress, float newProgress) {
		Bukkit.broadcastMessage(capturingTeam.getChatColor() + "" + oldProgress + " -> " + newProgress);
	}

	public BattleTeam getTeam() {
		return capturingTeam;
	}

	public void reset() {
		attackersOnGoal.clear();
		defendersOnGoal.clear();
		progress = 0f;
	}

	public boolean isCaptured() {
		return progress == 1f;
	}
	
}
