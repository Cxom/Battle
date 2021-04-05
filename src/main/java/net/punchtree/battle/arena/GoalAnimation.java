package net.punchtree.battle.arena;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import net.md_5.bungee.api.ChatColor;
import net.punchtree.battle.Battle;

public class GoalAnimation { /*extends BukkitRunnable implements Listener {

//	private final Location LOCATION = new Location(Bukkit.getWorld("GTA_City"), 2309, 71, 745);
	
	private final Location center;
	private final double radius;
	
	private boolean ringEnabled = true;
	private boolean cylEnabled = true;
	private boolean cinEnabled = true;
	private boolean riseEnabled = false;
	
	private boolean onGoal = false;
	private int progress = 0;
	private final int PROGRESS_MAX = 100;
	
	private final float linearUnitProgress() {
		return (float) progress / PROGRESS_MAX;
	}
	
//	private static GoalAnimation ga;
//	public static void register() {
//		ga = new GoalAnimation();
//		Bukkit.getPluginManager().registerEvents(ga, Battle.getPlugin());
//	}
	
	private static BukkitTask gaTask;
	public static void start() {
		gaTask = ga.runTaskTimerAsynchronously(Battle.getPlugin(), 0, 1);
	}
	
	public static void stop() {
		if (gaTask == null) return;
		gaTask.cancel();
		gaTask = null;
	}
	
	private int TASK_COUNTER = 0;
	@Override
	public void run() {
		if (TASK_COUNTER % TCYL_RATE == 0) drawTestCylinder();
		if (TASK_COUNTER % PASSIVE_SOUND_RATE == 0) playPassiveSound();
		if (TASK_COUNTER % PIN_RATE == 0) drawCentralProgressIndicator();
		drawRing();
		TASK_COUNTER++;
		if (onGoal) {
			progress = Math.min(progress + 1, PROGRESS_MAX);
		} else {
			progress = Math.max(progress - 1, 0);
		}
		
	}
	
//	@EventHandler
//	public void onPlayerChat(AsyncPlayerChatEvent event) {
//		if ("ga start".equalsIgnoreCase(event.getMessage())) {
//			if (gaTask == null) {
//				Bukkit.broadcastMessage(ChatColor.BLUE + "Started animation");
//				start();
//			} else {
//				Bukkit.broadcastMessage(ChatColor.RED + "Animation already running");
//			}
//		}
//		if ("ga stop".equalsIgnoreCase(event.getMessage())) {
//			if (gaTask != null) {
//				Bukkit.broadcastMessage(ChatColor.BLUE + "Stopped animation");
//				stop();
//			} else {
//				Bukkit.broadcastMessage(ChatColor.RED + "No animation running");
//			}
//		}
//		if ("ga ring".equalsIgnoreCase(event.getMessage())) {
//			Bukkit.broadcastMessage(ChatColor.AQUA + "Toggling ring: " + (ringEnabled ^ true));
//			ringEnabled ^= true;
//		}
//		if ("ga cyl".equalsIgnoreCase(event.getMessage())) {
//			Bukkit.broadcastMessage(ChatColor.AQUA + "Toggling cyl out: " + (cylEnabled ^ true));
//			cylEnabled ^= true;
//		}
//		if ("ga cin".equalsIgnoreCase(event.getMessage())) {
//			Bukkit.broadcastMessage(ChatColor.AQUA + "Toggling cyl inn: " + (cinEnabled ^ true));
//			cinEnabled ^= true;
//		}
//		if ("ga rise".equalsIgnoreCase(event.getMessage())) {
//			Bukkit.broadcastMessage(ChatColor.AQUA + "Toggling rise");
//			riseEnabled ^= true;
//		}
//	}
	
	
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		if (isOnGoal(e.getTo()) && !isOnGoal(e.getFrom())) {
			onGoal = true;
			e.getTo().getWorld().playSound(e.getTo(), Sound.BLOCK_BEEHIVE_ENTER, 1, 1);
		} else if (isOnGoal(e.getFrom()) && !isOnGoal(e.getTo())) {
			e.getFrom().getWorld().playSound(e.getFrom(), Sound.BLOCK_BEEHIVE_EXIT, 1, 1);
			onGoal = false;
		}
	}
	
//	private boolean isOnGoal(Location location) {
//		return location.getWorld() == LOCATION.getWorld() && location.distanceSquared(LOCATION) <= RING_RADIUS_SQ;
//	}
	
	private final Color PRIMARY_COL = Color.BLUE;
	private final Color SECONDARY_COL = Color.AQUA;
	
	// To be called every second
	private final int RING_RATE = 1;
	private final float RING_RADIUS = 2.5f;
	private final int RING_RADIUS_STEPS = (int) (2 * RING_RADIUS * 14);
	private final double RING_STEP = (2 * Math.PI) / RING_RADIUS_STEPS;
	private final int RING_PARTICLES_PER_TICK = 1;
	private double RING_d = 0;
	private final float RING_RADIUS_SQ = RING_RADIUS * RING_RADIUS;
	private void drawRingStep() {
		Location particleSpawnLocation = LOCATION.clone();
		Location particleSpawnLocation2 = LOCATION.clone();
		double xAdd = Math.sin(RING_d) * RING_RADIUS;
		double zAdd = Math.cos(RING_d) * RING_RADIUS;
		particleSpawnLocation.add(xAdd, 0, zAdd);
		particleSpawnLocation2.add(-xAdd, 0, -zAdd);
		spawnRedstoneParticle(particleSpawnLocation, 1.1f, PRIMARY_COL);
		spawnRedstoneParticle(particleSpawnLocation, 1.1f, PRIMARY_COL);
		spawnRedstoneParticle(particleSpawnLocation2, 1.1f, PRIMARY_COL);
		spawnRedstoneParticle(particleSpawnLocation2, 1.1f, PRIMARY_COL);
		RING_d += RING_STEP;
		
		float randX = (float) (Math.random() * RING_RADIUS * 2) - RING_RADIUS;
		float randZ = (float) (Math.random() * RING_RADIUS * 2) - RING_RADIUS;
//		if (randX*randX + randZ*randZ < RING_RADIUS_SQ) {
//			particleSpawnLocation = LOCATION.clone();
//			xAdd = randX;
//			zAdd = randZ;
//			particleSpawnLocation.add(xAdd, 0.1, zAdd);
//			spawnParticle(particleSpawnLocation, .5f, SECONDARY_COL.mixColors(Color.WHITE));
//		}
		randX = (float) (Math.random() * RING_RADIUS * 2) - RING_RADIUS;
		randZ = (float) (Math.random() * RING_RADIUS * 2) - RING_RADIUS;
		if (randX*randX + randZ*randZ < RING_RADIUS_SQ) {
			particleSpawnLocation = LOCATION.clone();
			xAdd = randX;
			zAdd = randZ;
			particleSpawnLocation.add(xAdd, 0, zAdd);
			spawnRedstoneParticle(particleSpawnLocation, .7f, SECONDARY_COL.mixColors(PRIMARY_COL));
		}
	}
	private void drawRing() {
		if (!ringEnabled) {
			return;
		}
		for(int i = 0; i < RING_PARTICLES_PER_TICK; ++i) {
			drawRingStep();
		}
	}
	
	private final int TCYL_RATE = 1;
	private final float TCYL_RADIUS = .4f;
	private final int TCYL_RADIUS_STEPS = 9;
	private final float TCYL_STEP = (float) (2 * Math.PI) / TCYL_RADIUS_STEPS;
	private final float TCYL_HEIGHT = 2.5f;
	private final float TCYL_HEIGHT_STEP = 0.3f;
	private float[] tcyl_bars = new float[TCYL_RADIUS_STEPS];
	private float[] tcyl_bars_vel = new float[TCYL_RADIUS_STEPS];
	private float[] tcyl_bars_acc = new float[TCYL_RADIUS_STEPS];
	{
		for(int f = 0; f < TCYL_RADIUS_STEPS; ++f) {
			tcyl_bars[f] = TCYL_STEP * f;
			tcyl_bars_vel[f] = 0;
			tcyl_bars_acc[f] = 0;
		}
	}
	private final float TCYL_RADIAN_RAND_RATE = 0.0008f;
	final float TCIN_RADIUS = .2f;
	final float TCIN_MIN_HEIGHT = 3.8f;
	final float TCIN_MAX_HEIGHT = 7f;
	private void drawTestCylinder() {
		if (!cylEnabled) {
			return;
		}
		for(int i = 0; i < TCYL_RADIUS_STEPS; ++i) {
			tcyl_bars_acc[i] = (float) Math.max(-.005, Math.min(.005, (tcyl_bars_acc[i] + (Math.random() * 2 - 1) * TCYL_RADIAN_RAND_RATE)));
			tcyl_bars_vel[i] = Math.max(-.05f, Math.min(.05f, tcyl_bars_vel[i] + tcyl_bars_acc[i]));
			tcyl_bars[i] += tcyl_bars_vel[i];
			for(float h = 0; h <= TCYL_HEIGHT - Math.sin(tcyl_bars[0]*2 + TCYL_STEP*i); h+=TCYL_HEIGHT_STEP) {
				Location particleSpawnLocation = LOCATION.clone();
				double xAdd = Math.sin(tcyl_bars[0] + TCYL_STEP*i) * TCYL_RADIUS;
				double zAdd = Math.cos(tcyl_bars[0] + TCYL_STEP*i) * TCYL_RADIUS;
				particleSpawnLocation.add(xAdd, h, zAdd);
				spawnRedstoneParticle(particleSpawnLocation, .3f, PRIMARY_COL);
			}
		}
		final int TCYL_TOP_RADIUS_STEPS = 20;
		final float TCYL_TOP_STEP = (float) (2 * Math.PI) / TCYL_TOP_RADIUS_STEPS;
		for (float i = 0; i < 2 * Math.PI; i+=TCYL_TOP_STEP) {
			Location particleSpawnLocationTop = LOCATION.clone();
			Location particleSpawnLocationBottom = LOCATION.clone();
			double xAdd = Math.sin(i) * TCYL_RADIUS;
			double zAdd = Math.cos(i) * TCYL_RADIUS;
			particleSpawnLocationTop.add(xAdd, TCYL_HEIGHT - Math.sin(i + tcyl_bars[0]), zAdd);
			particleSpawnLocationBottom.add(xAdd, 0, zAdd);
			spawnRedstoneParticle(particleSpawnLocationTop, .5f, PRIMARY_COL.mixColors(PRIMARY_COL, SECONDARY_COL));
			spawnRedstoneParticle(particleSpawnLocationBottom, .5f, PRIMARY_COL.mixColors(PRIMARY_COL, SECONDARY_COL));
		}
		if (!cinEnabled) {
			return;
		}
		float TCIN_HEIGHT = TCIN_MIN_HEIGHT + (TCIN_MAX_HEIGHT - TCIN_MIN_HEIGHT) * linearUnitProgress();
		final int TCIN_HEIGHT_STEPS = (int) (TCIN_HEIGHT * 4.4);
		final float TCIN_HEIGHT_STEP = TCIN_HEIGHT / TCIN_HEIGHT_STEPS;
		float FRAC_PI = (float) (Math.PI * .25);
		float randCounter = 0;
		for(float h = 0; h < TCIN_HEIGHT; h+=TCIN_HEIGHT_STEP) {
			Location particleSpawnLocation = LOCATION.clone();
			double xAdd = Math.sin(randCounter + RING_d) * TCIN_RADIUS;
			double zAdd = Math.cos(randCounter + RING_d) * TCIN_RADIUS;
			particleSpawnLocation.add(xAdd, h, zAdd);
			spawnRedstoneParticle(particleSpawnLocation, .5f, SECONDARY_COL);
			randCounter += FRAC_PI + Math.random() * FRAC_PI * 1.f;
		}
		final int TCIN_TOP_RADIUS_STEPS = 10;
		final float TCIN_TOP_STEP = (float) (2 * Math.PI) / TCIN_TOP_RADIUS_STEPS;
		for (float i = 0; i < 2 * Math.PI; i+=TCIN_TOP_STEP) {
			Location particleSpawnLocationTop = LOCATION.clone();
			double xAdd = Math.sin(i) * TCIN_RADIUS;
			double zAdd = Math.cos(i) * TCIN_RADIUS;
			particleSpawnLocationTop.add(xAdd, TCIN_HEIGHT, zAdd);
			spawnRedstoneParticle(particleSpawnLocationTop, .5f, PRIMARY_COL.mixColors(SECONDARY_COL));
		}
	}
	
	final float PIN_HEIGHT = TCIN_MAX_HEIGHT;
	final float PIN_STEP = 0.1f;
	final int PIN_RATE = 1;
	private void drawCentralProgressIndicator() {
		for(double d = 0; d <= PIN_HEIGHT * linearUnitProgress(); d+=PIN_STEP) {
			spawnRedstoneParticle(LOCATION.clone().add(0, d, 0), 1.5f, Color.WHITE);
		}
	}
	
	private final int PASSIVE_SOUND_RATE = 40;
	private void playPassiveSound() {
		LOCATION.getWorld().playSound(LOCATION, Sound.BLOCK_BEACON_AMBIENT, .5f, 1);
	}
	
	private void spawnRedstoneParticle(Location particleSpawnLocation, float particleSize, Color color) {
		DustOptions dustOptions = new DustOptions(color, particleSize);
		World world = particleSpawnLocation.getWorld();
		world.spawnParticle(
				Particle.REDSTONE, 
				particleSpawnLocation, 
				1, 
				dustOptions);
	}
	
	private void spawnParticle(Location particleSpawnLocation, float particleSize, Particle particle) {
		World world = particleSpawnLocation.getWorld();
		world.spawnParticle(
				particle, 
				particleSpawnLocation, 
				1
				);
	}
	*/
}
