package me.nahkd.spigot.btg.nms.v1_15_2;

import org.bukkit.craftbukkit.v1_15_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import me.nahkd.spigot.btg.nms.NMSPlayerConnection;
import net.minecraft.server.v1_15_R1.PacketPlayOutAnimation;

public class MC1_15_2_PlayerConnection implements NMSPlayerConnection {

	CraftPlayer player;
	
	public MC1_15_2_PlayerConnection(Player player) {
		this.player = (CraftPlayer) player;
	}
	
	@Override
	public void play_out_animation(Entity entity, int animationID) {
		CraftEntity e = (CraftEntity) entity;
		player.getHandle().playerConnection.sendPacket(new PacketPlayOutAnimation(e.getHandle(), animationID));
	}

}
