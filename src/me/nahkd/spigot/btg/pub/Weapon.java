package me.nahkd.spigot.btg.pub;

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.nahkd.spigot.btg.Battlegrounds;

public class Weapon {
	
	public final static UUID globalUUID = UUID.randomUUID();
	public final String id;
	
	public WeaponType type;
	public HashMap<String, WeaponSkin> skins;
	public double damage;
	public double movementSpeed;
	
	// Gun
	public double recoil;
	public String magazine;
	public int refireDuration;
	public int firingAmount;
	public double accuracy;
	public boolean aimable;
	public boolean auto;
	
	// Magazine
	public int bullets;
	
	public Weapon(ConfigurationSection config, String id) {
		this.id = id;
		type = WeaponType.valueOf(config.getString("Type", "Melee"));
		damage = config.getDouble("Damage", 1.0D);
		movementSpeed = config.getDouble("Movement Speed", 1.0D);
		skins = new HashMap<String, WeaponSkin>();
		aimable = config.getBoolean("Aimable", false);
		for (String skinName : config.getConfigurationSection("Skins").getKeys(false)) {
			skins.put(skinName, new WeaponSkin(
					config.getString("Skins." + skinName + ".Name", "§7Name doesn't exists"),
					config.getInt("Skins." + skinName + ".Model", 0),
					config.getInt("Skins." + skinName + ".Aim", 0),
					Material.valueOf(config.getString("Skins." + skinName + ".Material", "STONE"))));
		}
		
		if (type == WeaponType.Projectie) {
			recoil = config.getDouble("Recoil", 0.2D);
			magazine = config.getString("Magazine Type", "unknown");

			refireDuration = config.getInt("Firing Speed", 0);
			firingAmount = config.getInt("Firing Amount", 1);
			accuracy = config.getDouble("Accuracy", 1.0D);
			auto = config.getBoolean("Auto", false);
		} else if (type == WeaponType.Magazine) {
			bullets = config.getInt("Bullets", 12);
		}
	}
	
	public WeaponSkin findSkinByModelID(int id) {
		for (WeaponSkin skin : skins.values()) if (skin.skinModel == id || skin.skinAim == id) return skin;
		return null;
	}
	
	public ItemStack createItem(String skin, Battlegrounds plugin, boolean aiming) {
		WeaponSkin sss = skins.get(skin);
		ItemStack out = new ItemStack(sss.displayMaterial);
		ItemMeta meta = out.getItemMeta();
		meta.setLocalizedName(sss.displayName);
		if (type == WeaponType.Projectie) meta.setDisplayName(sss.displayName + " §8[§7" + plugin.weapons.get(magazine).bullets + "§8/" + plugin.weapons.get(magazine).bullets + "]");	
		else meta.setDisplayName(sss.displayName);
		
		meta.setLore(Arrays.asList(
				"§8" + type.toString() + " Weapon",
				"§7Damage: §c" + damage,
				"§7Movement Speed: §b-" + movementSpeed,
				(type == WeaponType.Projectie)? "§8Magazine: " + magazine : "",
				"§7§7§7§8" + id
				));
		if (sss.skinModel != 0) meta.setCustomModelData(aiming? sss.skinAim : sss.skinModel);
		meta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, new AttributeModifier(globalUUID, "bgweapon", 13376969.0, Operation.ADD_NUMBER));
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		out.setItemMeta(meta);
		return out;
	}
	
	public static boolean isWeapon(ItemStack item) {
		return item != null
				&& item.hasItemMeta()
				&& item.getItemMeta().hasLore()
				&& item.getItemMeta().getLore().size() >= 5
				&& item.getItemMeta().getLore().get(4).startsWith("§7§7§7§8");
	}
	public static String getID(ItemStack item) {
		return item.getItemMeta().getLore().get(4).substring(8);
	}
	public static int getBullets(String displayName) {
		return Integer.parseInt(displayName.split(" §8\\[§7")[1].split("§8/")[0]);
	}
	public static ItemStack setBullets(ItemStack item, int bullets, Battlegrounds plugin) {
		ItemMeta meta = item.getItemMeta();
//		final String displayName = meta.getDisplayName();
		final String id = meta.getLore().get(3).substring(12);
		meta.setDisplayName(meta.getLocalizedName() + " §8[§7" + bullets + "§8/" + plugin.weapons.get(id).bullets + "]");
		item.setItemMeta(meta);
		return item;
	}
	
}
