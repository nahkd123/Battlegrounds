package me.nahkd.spigot.btg.pub;

import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

public class Arena {
	
	public World evolvedWorld;
	public Location center;
	public Location waiting;
	public double size;
	public double sizeMin;
	public HashMap<Location, Material> supplyCrates;

	// In seconds rather than ticks
	public int timer_borderShrinking;
	public int timer_borderWaiting;
	public double timer_borderShrinks;
	
	public Arena() {
		supplyCrates = new HashMap<Location, Material>();
	}
	
	public Arena loadConfig(ConfigurationSection config) {
		evolvedWorld = Bukkit.getWorld(config.getString("World", "world"));
		final String[] locs_center = config.getString("Center", "0;0;0").split(";");
		center = new Location(evolvedWorld, Integer.parseInt(locs_center[0]), Integer.parseInt(locs_center[1]), Integer.parseInt(locs_center[2]));
		final String[] locs_waiting = config.getString("Waiting Room", "0;0;0").split(";");
		waiting = new Location(evolvedWorld, Integer.parseInt(locs_waiting[0]), Integer.parseInt(locs_waiting[1]), Integer.parseInt(locs_waiting[2]));
		
		size = config.getDouble("Border.Size", 150.0);
		sizeMin = config.getDouble("Border.Minimum Size", 50.0);
		timer_borderShrinks = config.getDouble("Border.Shrink Per Period", 10.0D);
		timer_borderShrinking = config.getInt("Border.Shrinking Period.Shrinking", 30);
		timer_borderWaiting = config.getInt("Border.Shrinking Period.Wait", 20);
		
		for (String loc : config.getConfigurationSection("Crates").getKeys(false)) {
			final String[] locs = loc.split(";");
			// supplyCrates.add(new Location(evolvedWorld, Integer.parseInt(locs[0]), Integer.parseInt(locs[1]), Integer.parseInt(locs[2])));
			supplyCrates.put(new Location(evolvedWorld, Integer.parseInt(locs[0]), Integer.parseInt(locs[1]), Integer.parseInt(locs[2])), Material.valueOf(config.getString("Crates." + loc, "STONE")));
		}
		return this;
	}
	
	public Arena saveConfig(ConfigurationSection config) {
		config.set("World", evolvedWorld.getName());
		config.set("Center", center.getBlockX() + ";" + center.getBlockY() + ";" + center.getBlockZ());
		config.set("Waiting Room", waiting.getBlockX() + ";" + waiting.getBlockY() + ";" + waiting.getBlockZ());
		config.set("Border.Size", size);
		config.set("Border.Minimum Size", sizeMin);
		config.set("Border.Shrink Per Period", timer_borderShrinks);
		config.set("Border.Shrinking Period.Shrinking", timer_borderShrinking);
		config.set("Border.Shrinking Period.Wait", timer_borderWaiting);
		
		for (Entry<Location, Material> entry : supplyCrates.entrySet()) {
			config.set("Crates." + entry.getKey().getBlockX() + ";" + entry.getKey().getBlockY() + ";" + entry.getKey().getBlockZ(), entry.getValue().toString());
		}
		return this;
	}
	
}
