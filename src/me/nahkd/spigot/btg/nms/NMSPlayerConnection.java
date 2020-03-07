package me.nahkd.spigot.btg.nms;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public interface NMSPlayerConnection {
	
	public void play_out_animation(Entity entity, int animationID);
	
	public static NMSPlayerConnection getConnection(Player player) {
		final String name = Bukkit.getServer().getClass().getPackage().getName();
		final String version = name.substring(name.lastIndexOf('.') + 1);
		
		if (version.startsWith("v1_15")) return new me.nahkd.spigot.btg.nms.v1_15_2.MC1_15_2_PlayerConnection(player);
		else return null;
	}
	
}
