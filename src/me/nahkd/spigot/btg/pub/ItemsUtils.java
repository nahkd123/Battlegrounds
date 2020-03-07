package me.nahkd.spigot.btg.pub;

import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemsUtils {
	
	public static final ItemStack create(Material type, String name, String... lore) {
		ItemStack item = new ItemStack(type);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(name);
		if (lore.length > 0) meta.setLore(Arrays.asList(lore));
		item.setItemMeta(meta);
		return item;
	}
	
}
