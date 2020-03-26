package me.nahkd.spigot.btg.pub;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class BulletData {

	public static final double SPEED = 1D;
	public static final double DAMAGE_R = 0.3D;
	public static final double DAMAGE_HEADSHOT_MUL = 1.4D; // +40% damage
	public static final int LENGTH_PER_TICK = 25;
	public static final int MAX_LENGTH = 250;
	
	public Location origin;
	Vector direction;
	double damage;
	public int livingtime;
	Player owner;
	
	// Caching
	public int i;
	
	public BulletData(Location origin, Vector direction, double damage, Player owner) {
		this.origin = origin;
		this.direction = direction;
		this.damage = damage;
		this.livingtime = 0;
		this.owner = owner;
	}
	
	public void tick() {
		for (i = 0; i < LENGTH_PER_TICK; i++) {
			this.origin = origin.add(direction.clone().multiply(SPEED));
			origin.getWorld().spawnParticle(Particle.CRIT_MAGIC, origin, 0);
			if (origin.getBlock().getType() != Material.AIR) {
				livingtime = MAX_LENGTH;
				return;
			} else {
				for (Entity e : origin.getWorld().getNearbyEntities(origin, DAMAGE_R, DAMAGE_R, DAMAGE_R)) {
					if (e instanceof LivingEntity && e != owner) {
						LivingEntity le = (LivingEntity) e;
						// Headshot thing
						if (origin.distanceSquared(le.getEyeLocation()) < origin.distanceSquared(le.getLocation())) damage = damage * DAMAGE_HEADSHOT_MUL;
						le.damage(damage, owner);
						livingtime = MAX_LENGTH;
						return;
					}
				}
			}
		}
		livingtime += LENGTH_PER_TICK;
	}
	
}
