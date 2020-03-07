package me.nahkd.spigot.btg.pub;

public enum WeaponType {
	
	Melee("melee"),
	Projectie("proj"),
	Magazine("mag");
	
	public final String nameHeader;
	private WeaponType(String nameHeader) {
		this.nameHeader = nameHeader;
	}
	
}
