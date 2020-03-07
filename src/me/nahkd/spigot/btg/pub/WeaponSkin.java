package me.nahkd.spigot.btg.pub;

import org.bukkit.Material;

public class WeaponSkin {
	
	public final String displayName;
	public final int skinModel;
	public final int skinAim;
	public final Material displayMaterial;
	
	public WeaponSkin(String displayName, int skinModel, int skinAim, Material displayMaterial) {
		this.displayName = displayName;
		this.skinModel = skinModel;
		this.skinAim = skinAim;
		this.displayMaterial = displayMaterial;
	}
	
}
