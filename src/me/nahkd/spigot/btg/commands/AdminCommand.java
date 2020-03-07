package me.nahkd.spigot.btg.commands;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.nahkd.spigot.btg.Battlegrounds;
import me.nahkd.spigot.btg.pub.ArenaTempData;
import me.nahkd.spigot.btg.pub.TabCompleteUtils;

public class AdminCommand implements CommandExecutor, TabCompleter {

	public Battlegrounds plugin;
	
	public AdminCommand(Battlegrounds plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 0) {
			sender.sendMessage("§7Usage: §b/bgadmin §3wizard/edit | cheat §e<ID §3[Skin=Default] | list | force[start]");
		} else {
			if (args[0].equalsIgnoreCase("list")) {
				sender.sendMessage("§7List of all weapons:");
				for (String id : plugin.weapons.keySet()) sender.sendMessage("§7 - §e" + id + " §3" + plugin.weapons.get(id).skins.size() + " skins");
			} else if (args[0].equalsIgnoreCase("cheat")) {
				if (sender instanceof Player) {
					String weaponID = args[1];
					String skin = (args.length >= 3)? args[2] : "Default";
					
					ItemStack item = plugin.weapons.get(weaponID).createItem(skin, plugin, false);
					((Player) sender).getInventory().addItem(item);
				} else sender.sendMessage("§cYou must be player to use this command");
			} else if (args[0].equalsIgnoreCase("wizard") || args[0].equalsIgnoreCase("edit")) {
				if (sender instanceof Player) {
					if (!plugin.editors.contains(((Player) sender).getUniqueId())) {
						plugin.editors.add(((Player) sender).getUniqueId());
						sender.sendMessage("§7>> §oUse /bgadmin wizard to exit");
						sender.sendMessage("§7>> §oChat §3?help §7§ofor editor commands list");
						sender.sendMessage("§7>> §oMake sure to use §3?save §7§oto save arena");
					} else {
						plugin.editors.remove(((Player) sender).getUniqueId());
						sender.sendMessage("§7>> §aExited editor");
					}
				} else sender.sendMessage("§cYou must be a player to use this");
			} else if (args[0].equalsIgnoreCase("force") || args[0].equalsIgnoreCase("forcestart")) {
				plugin.arenaTemp.currentStatus = ArenaTempData.STATUS_STARTING;
				plugin.arenaTemp.timer = 10;
				Bukkit.broadcastMessage("§3>> §bGame will be starts in 10 seconds!");
			}
		}
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (args.length == 1) return TabCompleteUtils.search(args[0], "wizard", "edit", "cheat", "list", "force", "forcestart");
		else if (args[0].equalsIgnoreCase("cheat")) {
			if (args.length == 2) return TabCompleteUtils.search(args[1], plugin.weapons.keySet());
			else if (args.length == 3) return TabCompleteUtils.search(args[2], plugin.weapons.get(args[1]).skins.keySet());
			else return Arrays.asList();
		}
		else return Arrays.asList();
	}

}
