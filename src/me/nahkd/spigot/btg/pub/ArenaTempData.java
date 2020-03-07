package me.nahkd.spigot.btg.pub;

import java.util.HashSet;

import org.bukkit.Location;
import org.bukkit.Material;
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
	
	public HashSet<Location> openedCrates;
	public HashSet<Player> alives;
	
	public ArenaTempData(Arena arena) {
		this.arena = arena;
		currentStatus = STATUS_STARTGAME;
		timer = 0;
		borderCenter = arena.center.clone();
		
		openedCrates = new HashSet<Location>();
		alives = new HashSet<Player>();
	}
	
	public void resetCrates() {
		for (Location l : arena.supplyCrates.keySet()) {
			l.getBlock().setType(arena.supplyCrates.get(l));
		}
		openedCrates = new HashSet<Location>();
	}
	
	public void emptyCrates() {
		for (Location l : arena.supplyCrates.keySet()) {
			l.getBlock().setType(Material.AIR);
		}
	}
	
}
