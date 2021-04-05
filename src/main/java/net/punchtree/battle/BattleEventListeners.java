package net.punchtree.battle;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class BattleEventListeners implements Listener {

	private final BattleGame game;
	
	// Working variables
	EntityDamageEvent ede;
	EntityDamageByEntityEvent edbee;
	UUID entityId;
	Player killed;
	Player killer;
	
	BattleEventListeners(BattleGame game) {
		this.game = game;
		
		Bukkit.getPluginManager().registerEvents(this, Battle.getPlugin());
	}
	
	// Kill and Death events
	
	@EventHandler
	public void onPlayerDeath(EntityDamageEvent ede) {
		
		this.ede = ede;
		UUID entityId = ede.getEntity().getUniqueId();
		
		if (!isPlayerInTheGame(ede.getEntity())) return;
		killed = (Player) ede.getEntity();
		
		if (!damageIsLethal()) return;
		killer = null;
		edbee = null;
		
		if (ede instanceof EntityDamageByEntityEvent) {
			edbee = (EntityDamageByEntityEvent) ede;
			Entity killingEntity = edbee.getDamager();
			
			// Determine killer
			if (isPlayerInTheGame(killingEntity)) {
				killer = (Player) killingEntity;
			} else if (isShotByPlayerInTheGame(killingEntity)) {
				killer = (Player) ((Projectile) killingEntity).getShooter();
				killingEntity.remove();
			}
		}
		
		if (killer != null) {
			game.handleKill(killer, killed, edbee);
		} else {
			game.handleNonPvpDeath(killed, ede);
		}
		
	}
	
	private boolean isPlayerInTheGame(Entity entity) {
		return game.hasPlayer(entity.getUniqueId());
	}
	
	private boolean damageIsLethal() {
		return ede.getFinalDamage() >= killed.getHealth();
	}
	
	private boolean isShotByPlayerInTheGame(Entity killingEntity) {
		if ( ! (killingEntity instanceof Projectile )) return false;
		Projectile projectile = (Projectile) killingEntity;
		
		if ( ! (projectile.getShooter() instanceof Player)) return false;
		
		Player shooter = (Player) projectile.getShooter();
		return isPlayerInTheGame(shooter);
	}
	
}
