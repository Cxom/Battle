package net.punchtree.battle.listeners;

import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

public class ProjectileHitListener<TPlayer> implements Listener {

	// TODO what is this
	
	private final Plugin plugin;
	private Function<UUID, TPlayer> playerLookup;
	private BiConsumer<TPlayer, ProjectileHitEvent> onArrowHit;
	
	public ProjectileHitListener(Plugin plugin, Function<UUID, TPlayer> playerLookup, BiConsumer<TPlayer, ProjectileHitEvent> onArrowHit) {
		this.plugin = plugin;
		this.playerLookup = playerLookup;
		this.onArrowHit = onArrowHit;
	}
	
	@EventHandler
	public void onShootBow(EntityShootBowEvent esbe) {
		TPlayer shooter = playerLookup.apply(esbe.getEntity().getUniqueId());
		if (shooter == null) return;
		
		esbe.getProjectile().setMetadata("Shooter", new FixedMetadataValue(plugin, shooter));
	}
	
	@EventHandler
	public void onProjectileHit(ProjectileHitEvent phe) {
		// TODO different projectile types...
		if (phe.getEntityType() != EntityType.ARROW) return;
		
		Arrow arrow = (Arrow) phe.getEntity();
		if ( ! arrow.hasMetadata("Shooter")) {
			arrow.remove();
			return;
		}
		arrow.remove();
		
		@SuppressWarnings("unchecked")
		TPlayer shooter = (TPlayer) arrow.getMetadata("Shooter").get(0).value();
		// There's no catch for interfering arrows from other games but that's not a problem that needs to be solved
		onArrowHit.accept(shooter, phe);
	}
	
}
