package me.nahkd.spigot.btg.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class PluginInformationCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		sender.sendMessage("§7Battlegrounds §3by nahkd123");
		sender.sendMessage("§e1.0.1 §7- §61.15.2");
		sender.sendMessage("§bLicensed under GPL v3.0 §7(including source code)");
		return true;
	}

}
