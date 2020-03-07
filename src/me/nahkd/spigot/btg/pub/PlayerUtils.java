package me.nahkd.spigot.btg.pub;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class PlayerUtils {
	
	public static boolean isLookingAtEntity(Player player, LivingEntity target, double addY) {
		Location eye = player.getEyeLocation();
		Vector toEntity = target.getEyeLocation().toVector().subtract(eye.toVector());
		final double dot = toEntity.normalize().dot(eye.getDirection().add(new Vector(0, addY, 0).normalize()));
		
		return dot > 0.99D;
	}
	
	public static <T extends Entity> T getTarget(final Entity entity, final Iterable<T> entities) {
		if (entity == null) return null;
		T target = null;
		final double threshold = 1;
		for (final T other : entities) {
			final Vector n = other.getLocation().toVector().subtract(entity.getLocation().toVector());
			if (entity.getLocation().getDirection().normalize().crossProduct(n).lengthSquared() < threshold && n.normalize().dot(entity.getLocation().getDirection().normalize()) >= 0) {
				if (target == null || target.getLocation().distanceSquared(entity.getLocation()) > other.getLocation().distanceSquared(entity.getLocation())) target = other;
			}
		}
		return target;
	}
	
}
