package me.nahkd.spigot.btg.pub;

public enum WeaponType {
	
	Melee("melee"),
	Projectie("proj"),
	Magazine("mag"),
	Consumable("con"),
	Throwable("throw");
	
	public final String nameHeader;
	private WeaponType(String nameHeader) {
		this.nameHeader = nameHeader;
	}
	
}
