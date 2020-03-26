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
import org.bukkit.persistence.PersistentDataType;

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
		meta.setDisplayName(sss.displayName);
		
		meta.setLore(Arrays.asList(
				"§8" + type.toString() + " Weapon"
				));
		if (sss.skinModel != 0) meta.setCustomModelData(aiming? sss.skinAim : sss.skinModel);
		meta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, new AttributeModifier(globalUUID, "bgweapon", 13376969.0, Operation.MULTIPLY_SCALAR_1));
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		
		meta.getPersistentDataContainer().set(Battlegrounds.WEAPON_ID, PersistentDataType.STRING, id);
		meta.getPersistentDataContainer().set(Battlegrounds.WEAPON_DMG_OR_HLT, PersistentDataType.DOUBLE, damage);
		meta.getPersistentDataContainer().set(Battlegrounds.WEAPON_MOVEMENT_SPEED, PersistentDataType.DOUBLE, movementSpeed);
		if (type == WeaponType.Projectie || type == WeaponType.Magazine) {
			meta.getPersistentDataContainer().set(Battlegrounds.WEAPON_BULLETS, PersistentDataType.INTEGER, type == WeaponType.Projectie? plugin.weapons.get(magazine).bullets : bullets);
		}
		
		out.setItemMeta(meta);
		return out;
	}
	
	public static boolean isWeapon(ItemStack item) {
		return item != null
				&& item.hasItemMeta()
				&& item.getItemMeta().getPersistentDataContainer().has(Battlegrounds.WEAPON_ID, PersistentDataType.STRING);
	}
	public static Weapon getWeaponObject(ItemStack item, Battlegrounds plugin) {
		return plugin.weapons.get(item.getItemMeta().getPersistentDataContainer().get(Battlegrounds.WEAPON_ID, PersistentDataType.STRING));
	}
	public static String getID(ItemStack item) {
		// return item.getItemMeta().getLore().get(4).substring(8);
		return item.getItemMeta().getPersistentDataContainer().get(Battlegrounds.WEAPON_ID, PersistentDataType.STRING);
	}
	public static int getBullets(ItemStack item) {
		return item.getItemMeta().getPersistentDataContainer().get(Battlegrounds.WEAPON_BULLETS, PersistentDataType.INTEGER);
	}
	public static ItemStack setBullets(ItemStack item, int bullets, Battlegrounds plugin) {
		ItemMeta meta = item.getItemMeta();
//		final String displayName = meta.getDisplayName();
		// final String id = meta.getLore().get(3).substring(12);
		// final String id = meta.getPersistentDataContainer().get(Battlegrounds.WEAPON_ID, PersistentDataType.STRING);
		meta.getPersistentDataContainer().set(Battlegrounds.WEAPON_BULLETS, PersistentDataType.INTEGER, bullets);
		// meta.setDisplayName(meta.getLocalizedName() + " §8[§7" + bullets + "§8/" + plugin.weapons.get(id).bullets + "]");
		item.setItemMeta(meta);
		return item;
	}
	
}
