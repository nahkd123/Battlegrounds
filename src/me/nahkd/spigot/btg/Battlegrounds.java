package me.nahkd.spigot.btg;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import me.nahkd.spigot.btg.commands.AdminCommand;
import me.nahkd.spigot.btg.events.handlers.EditorsEventsHandler;
import me.nahkd.spigot.btg.events.handlers.PlayerEventsHandler;
import me.nahkd.spigot.btg.net.LobbyConnection;
import me.nahkd.spigot.btg.pub.Arena;
import me.nahkd.spigot.btg.pub.ArenaTempData;
import me.nahkd.spigot.btg.pub.BulletData;
import me.nahkd.spigot.btg.pub.Weapon;

public class Battlegrounds extends JavaPlugin {
	
	/*public static final void copyResource(ClassLoader loader, String resource, File output) {
		try {
			InputStream input = loader.getResourceAsStream(resource);
			OutputStream out = new FileOutputStream(output);
			int avaliableLeft;
			byte[] str = new byte[avaliableLeft = input.available()];
			while (avaliableLeft > 0) avaliableLeft -= input.read(str);
			out.write(str);
			out.close();
			input.close();
		} catch (IOException e) {
			System.err.println("Unable to copy resource: IOException");
			e.printStackTrace();
			System.err.println("Please check if the folder have write access.");
		}
	}*/ // 1.15.2 has something called "saveResource"
	
	public File configFile;
	public File weaponsFile;
	public File playerData;
	public File arenaFile;
	public YamlConfiguration config;
	public YamlConfiguration weaponsConfig;
	public HashMap<String, Weapon> weapons;
	public HashMap<UUID, HashMap<String, String>> selectedSkins;
	public HashMap<UUID, HashMap<String, Integer>> refireTime;
	public HashMap<UUID, Boolean> aimingState;
	public HashMap<UUID, Boolean> autoHold;
	
	ArrayList<BulletData> bullets;
	public Arena arena;
	public ArenaTempData arenaTemp;
	
	// Editing
	public HashSet<UUID> editors;
	
	// Lobby
	LobbyConnection lobby;
	
	@Override
	public void onEnable() {
		selectedSkins = new HashMap<UUID, HashMap<String,String>>();
		aimingState = new HashMap<UUID, Boolean>();
		autoHold = new HashMap<UUID, Boolean>();
		bullets = new ArrayList<BulletData>();
		if (getServer().getPluginManager().getPlugin("ErotoLib") != null) {
			try {
				getServer().getConsoleSender().sendMessage("§fErotoLib found, creating connection to lobby server...");
				lobby = new LobbyConnection(36969, this);
				lobby.connection.sendString("connected\u0000clientnamehere");
			} catch (UnknownHostException e) {
				e.printStackTrace();
				System.out.println("It's kinda weird, because localhost isn't real. Please check your /etc/hosts.");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if (!getDataFolder().exists()) getDataFolder().mkdir();
		playerData = new File(getDataFolder(), "playersdata");
		if (!playerData.exists()) playerData.mkdir();
		
		configFile = new File(getDataFolder(), "config.yml");
		if (!configFile.exists()) saveResource("config.yml", false);
		config = YamlConfiguration.loadConfiguration(configFile);
		
		arenaFile = new File(getDataFolder(), "arena.yml");
		arena = new Arena();
		if (arenaFile.exists()) {
			arena.loadConfig(YamlConfiguration.loadConfiguration(arenaFile));
			arenaTemp = new ArenaTempData(arena);
			arenaTemp.emptyCrates();
		} else {
			getServer().getConsoleSender().sendMessage("§3Battlegrounds §7>> §cArena file not found. Please join the server and type /bgadmin wizard to start editing arena!");
		}
		editors = new HashSet<UUID>();
		
		weaponsFile = new File(getDataFolder(), "weapons.yml");
		if (!weaponsFile.exists()) saveResource("weapons.yml", false);
		weaponsConfig = YamlConfiguration.loadConfiguration(weaponsFile);
		weapons = new HashMap<String, Weapon>();
		for (String name : weaponsConfig.getKeys(false)) if (!name.startsWith("_")) {
			weapons.put(name, new Weapon(weaponsConfig.getConfigurationSection(name), name));
		}
		getServer().getConsoleSender().sendMessage("§3Battlegrounds §7>> §aLoaded " + weapons.size() + " weapons!");
		
		if (getServer().getPluginManager().getPlugin("ErotoLib") != null) getServer().getConsoleSender().sendMessage("§3Battlegrounds §7>> §aIt's seem like ErotoLib is enabled...");
		getServer().getConsoleSender().sendMessage("§3Battlegrounds §7>> §aPlugin enabled!");
		
		{
			getServer().getPluginManager().registerEvents(new PlayerEventsHandler(this), this);
			getServer().getPluginManager().registerEvents(new EditorsEventsHandler(this), this);
		}
		{
			AdminCommand adminCommand = new AdminCommand(this);
			getCommand("bgadmin").setExecutor(adminCommand);
			getCommand("bgadmin").setTabCompleter(adminCommand);
		}
		
		// Scheduled repeating tasks
		Battlegrounds plugin = this;
		// Bullets
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			@Override
			public void run() {
				Iterator<BulletData> bi = bullets.iterator();
				while (bi.hasNext()) {
					BulletData b = bi.next();
					b.tick();
					if (b.livingtime >= BulletData.MAX_LENGTH) bi.remove();
				}
			}
		}, 1, 1);
		
		// Timer
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			@Override
			public void run() {
				if (arenaTemp == null) return;
				if (arenaTemp.timer > 0) arenaTemp.timer--;
				if (arenaTemp.currentStatus == ArenaTempData.STATUS_STARTING) {
					if (arenaTemp.timer <= 5 && arenaTemp.timer >= 1) Bukkit.broadcastMessage("§3>> §bGame will be starts in " + arenaTemp.timer + " seconds!");
					else if (arenaTemp.timer == 0) {
						Random rand = new Random();
						final Weapon defaultWeapon = weapons.get(weaponsConfig.getString("_default", "example"));
						for (Player p : getServer().getOnlinePlayers()) {
							p.setFoodLevel(20);
							p.setHealth(20);
							final int tpX = (int) Math.round(rand.nextDouble() * arena.size);
							final int tpZ = (int) Math.round(rand.nextDouble() * arena.size);
							final double half = arena.size / 2;
							Location tpLoc = new Location(arena.evolvedWorld, arena.center.getX() + tpX - half, 255, arena.center.getZ() + tpZ - half);
							while (tpLoc.getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR) tpLoc.subtract(0, 1, 0);
							p.teleport(tpLoc);
							
							p.getInventory().clear();
							p.setGameMode(GameMode.ADVENTURE);
							p.setWalkSpeed(0.2F);
							aimingState.put(p.getUniqueId(), false);
							
							// Each players will get default melee weapon
							p.getInventory().addItem(defaultWeapon.createItem(getSelectedSkin(p, defaultWeapon.id), plugin, false));
							
							arenaTemp.alives.add(p);
						}
						Bukkit.broadcastMessage("§3>> §bGame started!");
						arenaTemp.currentStatus = ArenaTempData.STATUS_BORDERWAIT;
						arenaTemp.timer = arena.timer_borderWaiting;
						arenaTemp.resetCrates();
					}
				}
			}
		}, 0, 20);
		
		// Refiring time & auto hold
		refireTime = new HashMap<UUID, HashMap<String,Integer>>();
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			@Override
			public void run() {
				if (refireTime.size() > 0) for (UUID uuid : refireTime.keySet()) {
					HashMap<String, Integer> refire = refireTime.get(uuid);
					if (refire.size() > 0) for (String a : refire.keySet()) if (refire.getOrDefault(a, 0) > 0) {
						refire.put(a, refire.get(a) - 1);
						if (refire.get(a) <= 0 && autoHold.getOrDefault(uuid, false)) autoHold.put(uuid, false);
					}
				}
				if (getServer().getOnlinePlayers().size() > 0) for (Player p : getServer().getOnlinePlayers()) {
					if (autoHold.getOrDefault(p.getUniqueId(), false)) {
						// Fire next bullet
						ItemStack item = p.getInventory().getItemInMainHand();
						if (item == null || item.getType() == Material.AIR) {
							// Item is null???
							autoHold.put(p.getUniqueId(), false);
						} else {
							Weapon weapon = weapons.get(Weapon.getID(item));
							int bullets = Weapon.getBullets(item.getItemMeta().getDisplayName());
							if (bullets > 0) fire(bullets, weapon, p, item);
							else {
								autoHold.put(p.getUniqueId(), false);
							}
						}
					}
				}
			}
		}, 0, 1);
		
		for (Player p : getServer().getOnlinePlayers()) initPlayer(p);
		if (Bukkit.getOnlinePlayers().size() >= 2 && arenaTemp != null) {
			arenaTemp.currentStatus = ArenaTempData.STATUS_STARTING;
			arenaTemp.timer = ArenaTempData.TIMER_STARTGAMEWAIT;
			Bukkit.broadcastMessage("§3>> §bGame will be starts in " + ArenaTempData.TIMER_STARTGAMEWAIT + " seconds!");
		}
	}
	
	public void fire(int bullets, Weapon weapon, Player player, ItemStack item) {
		if (bullets > 0) {
			bullets--;
			ItemMeta meta = item.getItemMeta();
			boolean broke = false;
			if (meta instanceof Damageable) {
				Damageable dmg = (Damageable) meta;
				dmg.setDamage(dmg.getDamage() + 1);
				if (dmg.getDamage() >= item.getType().getMaxDurability()) broke = true;
			}
			item.setItemMeta(meta);
			if (!broke) player.getInventory().setItemInMainHand(Weapon.setBullets(item, bullets, this));
			else player.getInventory().setItemInMainHand(null);
			
//			Entity target = PlayerUtils.getTarget(player, player.getWorld().getEntities());
//			if (target != null && target instanceof LivingEntity) ((LivingEntity) target).damage(weapon.damage, player);
			for (int i = 0; i < weapon.firingAmount; i++) {
				Vector dir = player.getEyeLocation().getDirection().clone();
				final double varr = (1.0 - weapon.accuracy)
						* (player.isSprinting()? 1.2D : player.isSneaking()? 0.35D : 1.0D)
						* (this.aimingState.getOrDefault(player.getUniqueId(), false)? 0.7 : 1.0);
				final double halfVar = varr / 2D;
				dir.add(new Vector(PlayerEventsHandler.random.nextDouble() * varr - halfVar, PlayerEventsHandler.random.nextDouble() * varr - halfVar, PlayerEventsHandler.random.nextDouble() * varr - halfVar)).normalize();
				createBullet(
						player.getEyeLocation(),
						dir,
						weapon.damage, player);
			}
			
			// After shoot
			double recoil = weapon.recoil * (player.isSneaking()? 0.75 : 1.0);
			player.setVelocity(player.getVelocity().add(player.getEyeLocation().getDirection().multiply(-recoil)));
		}
	}
	
	@Override
	public void onDisable() {
		if (lobby != null) lobby.shutdown();
	}
	
	public void createBullet(Location origin, Vector direction, double damage, Player owner) {
		bullets.add(new BulletData(origin, direction, damage, owner));
	}
	
	// TODO Link to database or something else (plugin messaging)
	public String getSelectedSkin(Player player, String id) {
		return selectedSkins.get(player.getUniqueId()).getOrDefault(id, "Default");
	}
	public void initPlayer(Player player) {
		if (!selectedSkins.containsKey(player.getUniqueId())) {
			HashMap<String, String> weaponSkins = new HashMap<String, String>();
			selectedSkins.put(player.getUniqueId(), weaponSkins);
			// if (getServer().getPluginManager().isPluginEnabled("ErotoLib")) {
			if (lobby != null) {
				// Load skin from lobby server
				for (String weaponId : weapons.keySet()) lobby.skinRequest(weaponId, player.getUniqueId());
			}
		}
	}
}
