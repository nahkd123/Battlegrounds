package me.nahkd.spigot.btg.events.handlers;

import java.io.IOException;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import me.nahkd.spigot.btg.Battlegrounds;
import me.nahkd.spigot.btg.pub.ItemsUtils;

public class EditorsEventsHandler implements Listener {
	
	public static final ItemStack CRATE_WAND = ItemsUtils.create(Material.ARROW, "§eCrate Wand", "§7§oOwO What's this?");
	Battlegrounds plugin;
	
	public EditorsEventsHandler(Battlegrounds plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void interact(PlayerInteractEvent event) {
		if (
				CRATE_WAND.isSimilar(event.getItem())
				&& plugin.editors.contains(event.getPlayer().getUniqueId())
				&& (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK)
			) {
			event.setCancelled(true);
			if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
				if (!plugin.arena.supplyCrates.containsKey(event.getClickedBlock().getLocation())) {
					plugin.arena.supplyCrates.put(event.getClickedBlock().getLocation(), event.getClickedBlock().getType());
					event.getPlayer().sendMessage("§7>> Added block");
					if (plugin.arena.evolvedWorld == null) plugin.arena.evolvedWorld = event.getPlayer().getWorld();
				} else event.getPlayer().sendMessage("§7>> §cThis block already added to the configuration file");
			} else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
				if (plugin.arena.supplyCrates.containsKey(event.getClickedBlock().getLocation())) {
					plugin.arena.supplyCrates.remove(event.getClickedBlock().getLocation());
					event.getPlayer().sendMessage("§7>> Removed block");
					if (plugin.arena.evolvedWorld == null) plugin.arena.evolvedWorld = event.getPlayer().getWorld();
				} else event.getPlayer().sendMessage("§7>> §cThis block isn't exists in configuration file...");
			}
		}
	}
	
	@EventHandler
	public void chatEvent(AsyncPlayerChatEvent event) {
		if (plugin.editors.contains(event.getPlayer().getUniqueId()) && event.getMessage().startsWith("?")) {
			event.setCancelled(true);
			final String command = event.getMessage().substring(1);
			if (command.equalsIgnoreCase("help")) {
				event.getPlayer().sendMessage("§7>> §bList of editor commands:");
				event.getPlayer().sendMessage("§7>> §b?help §7Show this list");
				event.getPlayer().sendMessage("§7>> §b?save §7Save current arena");
				event.getPlayer().sendMessage("§7>> §b?cratewand §7Get a crate wand (for adding a crate)");
				event.getPlayer().sendMessage("§7>> §b?centerhere §7Set current location as center");
				event.getPlayer().sendMessage("§7>> §b?waitingroom §7Set current location as waiting room");
				event.getPlayer().sendMessage("§7>> §b?bordermax §3<Size>");
				event.getPlayer().sendMessage("§7>> §b?bordermin §3<Size> §7Set border size");
			} else if (command.equalsIgnoreCase("cratewand")) {
				event.getPlayer().getInventory().addItem(CRATE_WAND);
				event.getPlayer().sendMessage("§7>> Left-click: Add | Right-click: Remove");
			} else if (command.equalsIgnoreCase("centerhere")) {
				plugin.arena.center = event.getPlayer().getLocation();
				if (plugin.arena.evolvedWorld == null) plugin.arena.evolvedWorld = event.getPlayer().getWorld();
				event.getPlayer().sendMessage("§7>> §7Changed location for map center");
			} else if (command.equalsIgnoreCase("waitingroom")) {
				plugin.arena.waiting = event.getPlayer().getLocation();
				if (plugin.arena.evolvedWorld == null) plugin.arena.evolvedWorld = event.getPlayer().getWorld();
				event.getPlayer().sendMessage("§7>> §7Changed location for waiting room");
			} else if (command.startsWith("bordermax ")) {
				plugin.arena.size = Double.parseDouble(command.substring(10));
				event.getPlayer().sendMessage("§7>> §7Resized to " + plugin.arena.size);
			} else if (command.startsWith("bordermin ")) {
				plugin.arena.sizeMin = Double.parseDouble(command.substring(10));
				event.getPlayer().sendMessage("§7>> §7Resized to " + plugin.arena.sizeMin);
			} else if (command.startsWith("save")) {
				try {
					YamlConfiguration cfg = new YamlConfiguration();
					plugin.arena.saveConfig(cfg);
					cfg.save(plugin.arenaFile);
					event.getPlayer().sendMessage("§7>> §aSaved configuration file!");
					event.getPlayer().sendMessage("§7>> §oMake sure to restart your server for full effect!");
				} catch (IOException e) {
					e.printStackTrace();
					event.getPlayer().sendMessage("§7>> §cUnable to save file: IOException (Something wrong with the write permission??)");
				}
			} else event.getPlayer().sendMessage("§7>> §cUnknown command");
		}
	}
	
}
