package me.nahkd.spigot.btg.pub;

import java.util.HashSet;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;

public class ArenaTempData {

	public static final int STATUS_STARTGAME = 0;
	public static final int STATUS_STARTING = 1;
	public static final int STATUS_BORDERWAIT = 2;
	public static final int STATUS_BORDERSHRINK = 3;
	public static final int STATUS_END = 4;
	
	public static final int TIMER_STARTGAMEWAIT = 30; // 30s
	
	public final Arena arena;
	
	public int currentStatus;
	public int timer;
	public Location borderCenter;
	
	public HashSet<String> openedCrates;
	public HashSet<Player> alives;
	
	public ArenaTempData(Arena arena) {
		this.arena = arena;
		currentStatus = STATUS_STARTGAME;
		timer = 0;
		borderCenter = arena.center.clone();
		
		openedCrates = new HashSet<String>();
		alives = new HashSet<Player>();
	}
	
	public void resetCrates() {
		for (Location l : arena.supplyCrates.keySet()) {
			// l.getBlock().setType(arena.supplyCrates.get(l));
			l.getBlock().setType(Material.CHEST);
		}
		openedCrates = new HashSet<String>();
	}
	
	public void emptyCrates() {
		for (Location l : arena.supplyCrates.keySet()) {
			if (l.getBlock().getType() == Material.CHEST) {
				Chest chest = (Chest) l.getBlock().getState();
				chest.update(true);
				chest.getBlockInventory().clear();
			}
			l.getBlock().setType(Material.AIR);
		}
	}
	
}
