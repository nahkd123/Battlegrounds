package me.nahkd.spigot.btg.events.handlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.nahkd.spigot.btg.Battlegrounds;
import me.nahkd.spigot.btg.pub.ArenaTempData;
import me.nahkd.spigot.btg.pub.Weapon;
import me.nahkd.spigot.btg.pub.WeaponType;

public class PlayerEventsHandler implements Listener {
	
	Battlegrounds plugin;
	Set<UUID> playerAfterJump;
	public static Random random;
	
	public PlayerEventsHandler(Battlegrounds plugin) {
		this.plugin = plugin;
		this.playerAfterJump = new HashSet<UUID>();
		random = new Random();
	}
	
	@EventHandler
	public void damage(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Player) {
			Player p = (Player) event.getDamager();
			ItemStack holdingItem = p.getInventory().getItemInMainHand();
			if (Weapon.isWeapon(holdingItem)) {
				final String id = Weapon.getID(holdingItem);
				if (plugin.weapons.get(id).type == WeaponType.Melee) {
					event.setDamage(plugin.weapons.get(id).damage);
				}
			}
		}
	}
	
	@EventHandler
	public void getDamage(EntityDamageEvent event) {
		if (plugin.arenaTemp != null &&
				(plugin.arenaTemp.currentStatus == ArenaTempData.STATUS_STARTING || plugin.arenaTemp.currentStatus == ArenaTempData.STATUS_STARTGAME || plugin.arenaTemp.currentStatus == ArenaTempData.STATUS_END)
				&& event.getEntity() instanceof Player) {
			event.setCancelled(true);
		} else {
			if (event.getEntity() instanceof Player) {
				Player player = (Player) event.getEntity();
				if (player.getHealth() <= event.getDamage()) {
					event.setCancelled(true);
					for (ItemStack item : player.getInventory().getContents()) if (item != null && item.getType() != Material.AIR) player.getWorld().dropItem(player.getLocation(), item);
					player.setGameMode(GameMode.SPECTATOR);
					player.sendMessage("§3>> §cYou died. Better luck next time!");
					plugin.arenaTemp.alives.remove(player);
					if (plugin.arenaTemp.currentStatus != ArenaTempData.STATUS_END) {
						if (plugin.arenaTemp.alives.size() == 1) {
							Bukkit.broadcastMessage("§3");
							Bukkit.broadcastMessage("§3   §b" + plugin.arenaTemp.alives.iterator().next().getName() + " §3is the winner!!!");
							Bukkit.broadcastMessage("§3   §7Ranking: Ranking coming soon!");
							Bukkit.broadcastMessage("§3");
							plugin.arenaTemp.currentStatus = ArenaTempData.STATUS_END;
							scheduleRestart();
						} else if (plugin.arenaTemp.alives.size() <= 0) {
							Bukkit.broadcastMessage("§3");
							Bukkit.broadcastMessage("§3   §bOOF §3No ones win :<");
							Bukkit.broadcastMessage("§3   §7Ranking: Ranking coming soon!");
							Bukkit.broadcastMessage("§3");
							plugin.arenaTemp.currentStatus = ArenaTempData.STATUS_END;
							scheduleRestart();
						}
					}
				}
			}
		}
	}
	
	public void scheduleRestart() {
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			@Override
			public void run() {
				if (Bukkit.getOnlinePlayers().size() >= 2) {
					plugin.arenaTemp.currentStatus = ArenaTempData.STATUS_STARTING;
					plugin.arenaTemp.timer = ArenaTempData.TIMER_STARTGAMEWAIT;
					Bukkit.broadcastMessage("§3>> §bGame will be starts in " + ArenaTempData.TIMER_STARTGAMEWAIT + " seconds!");
				} else plugin.arenaTemp.currentStatus = ArenaTempData.STATUS_STARTGAME;
				
				plugin.arenaTemp.timer = ArenaTempData.TIMER_STARTGAMEWAIT;
				plugin.arenaTemp.borderCenter = plugin.arena.center.clone();
				plugin.arenaTemp.openedCrates = new HashSet<Location>();
				plugin.arenaTemp.alives = new HashSet<Player>();
				for (Entity e : plugin.arena.evolvedWorld.getEntities()) if (e instanceof Item) e.remove();
				
				plugin.arenaTemp.emptyCrates();
				for (Player p : Bukkit.getOnlinePlayers()) {
					p.setHealth(20.0);
					p.setFoodLevel(20);
					p.setGameMode(GameMode.ADVENTURE);
					p.getInventory().clear();
					p.teleport(plugin.arena.waiting);
				}
			}
		}, 20 * 10);
	}
	
	@EventHandler
	public void interact(PlayerInteractEvent event) {
//		if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
//		} else if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
//			NMSPlayerConnection connection = NMSPlayerConnection.getConnection(event.getPlayer());
//			connection.play_out_animation(event.getPlayer(), 3);
//		}
		if (true) { // pls don't kill me
			if (Weapon.isWeapon(event.getItem())) {
				final String id = Weapon.getID(event.getItem());
				if (plugin.weapons.get(id).type == WeaponType.Projectie) {
					if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
						ItemStack item = event.getItem();
						if (plugin.aimingState.getOrDefault(event.getPlayer().getUniqueId(), false)) {
							// Remove aim
							plugin.aimingState.put(event.getPlayer().getUniqueId(), false);
							Weapon weapon = plugin.weapons.get(Weapon.getID(item));
							ItemMeta meta = item.getItemMeta();
							meta.setCustomModelData(weapon.findSkinByModelID(meta.getCustomModelData()).skinModel);
							item.setItemMeta(meta);
							event.getPlayer().getInventory().setItemInMainHand(new ItemStack(item));
							event.getPlayer().setWalkSpeed(0.2F);
						} else {
							// Aim
							plugin.aimingState.put(event.getPlayer().getUniqueId(), true);
							Weapon weapon = plugin.weapons.get(Weapon.getID(item));
							ItemMeta meta = item.getItemMeta();
							meta.setCustomModelData(weapon.findSkinByModelID(meta.getCustomModelData()).skinAim);
							item.setItemMeta(meta);
							event.getPlayer().getInventory().setItemInMainHand(new ItemStack(item));
							event.getPlayer().setWalkSpeed(0.12F);
						}
						event.setCancelled(true);
						return;
					}
					// Gun: Has bullets
					final String itemName = event.getItem().getItemMeta().getDisplayName();
					int bullets = Weapon.getBullets(itemName);
					Weapon weapon = plugin.weapons.get(id);
					if (!plugin.refireTime.containsKey(event.getPlayer().getUniqueId())) {
						plugin.refireTime.put(event.getPlayer().getUniqueId(), new HashMap<String, Integer>());
					}
					HashMap<String, Integer> refire = plugin.refireTime.get(event.getPlayer().getUniqueId());
					if (refire.getOrDefault(weapon.id, 0) <= 0) {
						if (bullets > 0) {
							plugin.fire(bullets, weapon, event.getPlayer(), event.getItem());
							if (weapon.auto) plugin.autoHold.put(event.getPlayer().getUniqueId(), true);
							refire.put(weapon.id, weapon.refireDuration);
						} else {
							// Reload
							if (reload(event.getPlayer(), weapon)) event.getPlayer().getInventory().setItemInMainHand(Weapon.setBullets(event.getItem(), plugin.weapons.get(weapon.magazine).bullets, plugin));
						}
						event.setCancelled(true);
					}
				}
			} else if (plugin.arenaTemp != null && event.getClickedBlock() != null && plugin.arena.supplyCrates.containsKey(event.getClickedBlock().getLocation()) && !plugin.arenaTemp.openedCrates.contains(event.getClickedBlock().getLocation())) {
				// Random item
				ArrayList<String> keys = new ArrayList<String>(plugin.weapons.keySet());
				int randomIndex = (int) Math.round(Math.random() * (keys.size() - 1));
				
				Weapon wp = plugin.weapons.get(keys.get(randomIndex));
				if (wp.type == WeaponType.Magazine) {
					ItemStack eeeeeee = new ItemStack(wp.createItem(plugin.getSelectedSkin(event.getPlayer(), wp.id), plugin, false));
					eeeeeee.setAmount((int) Math.round(Math.random() * 12));
					event.getPlayer().getInventory().addItem(eeeeeee);
				}
				event.getPlayer().getInventory().addItem(wp.createItem(plugin.getSelectedSkin(event.getPlayer(), wp.id), plugin, false));
				
				plugin.arenaTemp.openedCrates.add(event.getClickedBlock().getLocation());
				event.getClickedBlock().getWorld().spawnParticle(Particle.CLOUD, event.getClickedBlock().getLocation(), 0);
				event.getClickedBlock().setType(Material.AIR);
			}
		}
	}
	
	@EventHandler
	public void handChange(PlayerItemHeldEvent event) {
		if (plugin.autoHold.getOrDefault(event.getPlayer().getUniqueId(), false)) {
			event.setCancelled(true);
			return;
		}
		if (plugin.aimingState.getOrDefault(event.getPlayer().getUniqueId(), false)) {
			plugin.aimingState.put(event.getPlayer().getUniqueId(), false);
			ItemStack item = event.getPlayer().getInventory().getItem(event.getPreviousSlot());
			Weapon weapon = plugin.weapons.get(Weapon.getID(item));
			ItemMeta meta = item.getItemMeta();
			meta.setCustomModelData(weapon.findSkinByModelID(meta.getCustomModelData()).skinModel);
			item.setItemMeta(meta);
			event.getPlayer().getInventory().setItem(event.getPreviousSlot(), new ItemStack(item));
			event.getPlayer().setWalkSpeed(0.2F);
		}
	}
	
	@EventHandler
	public void hunger(FoodLevelChangeEvent event) {
		event.setCancelled(true);
	}
	
	public boolean reload(Player player, Weapon weapon) {
		ItemStack[] is = player.getInventory().getContents();
		for (int index = 0; index < is.length; index++) {
			ItemStack i = is[index];
			if (Weapon.isWeapon(i) && Weapon.getID(i).equalsIgnoreCase(weapon.magazine)) {
				// Reload here
				ItemStack newIs = new ItemStack(i);
				newIs.setAmount(newIs.getAmount() - 1);
				is[index] = newIs.getAmount() == 0? null : newIs;
				player.getInventory().setContents(is);
				return true;
			}
		}
		return false;
	}
	
	@EventHandler
	public void bunnyHopping(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		if (player.getLocation().getBlock().getType() != Material.LADDER) {
			if (!player.isOnGround() && player.getVelocity().getY() >= 0.418) {
				playerAfterJump.add(player.getUniqueId());
			} else if (playerAfterJump.contains(player.getUniqueId()) && player.getVelocity().getY() <= -0.15233518685055708) {
				if (player.isSneaking()) {
					playerAfterJump.remove(player.getUniqueId());
					double oldY = player.getVelocity().getY();
					player.setVelocity(player.getVelocity().multiply(3.75).setY(oldY));
				}
			} else if (player.isOnGround()) {
				playerAfterJump.remove(player.getUniqueId());
			}
		}
	}
	
	@EventHandler
	public void login(PlayerLoginEvent event) {
		if (plugin.arenaTemp != null) {
			if (plugin.arenaTemp.currentStatus == ArenaTempData.STATUS_STARTING || plugin.arenaTemp.currentStatus == ArenaTempData.STATUS_STARTGAME) event.allow();
			else event.disallow(Result.KICK_FULL, "This area is currently playing");
		} else {
			if (event.getPlayer().hasPermission("battlegrounds.admin")) event.allow();
			else event.disallow(Result.KICK_WHITELIST, "This arena is not ready to play yet!");
		}
	}
	
	@EventHandler
	public void join(PlayerJoinEvent event) {
		if (plugin.arenaTemp != null) {
			plugin.initPlayer(event.getPlayer());
			event.getPlayer().setFoodLevel(20);
			event.getPlayer().setHealth(20);
			event.getPlayer().setGameMode(GameMode.ADVENTURE);
			event.getPlayer().getInventory().clear();
			event.getPlayer().teleport(plugin.arena.waiting);
			event.setJoinMessage("§3>> §b" + event.getPlayer().getName() + " §7joined! §8(" + Bukkit.getOnlinePlayers().size() + "/" + Bukkit.getMaxPlayers() + ")");
			if (Bukkit.getOnlinePlayers().size() >= 2) {
				plugin.arenaTemp.currentStatus = ArenaTempData.STATUS_STARTING;
				plugin.arenaTemp.timer = ArenaTempData.TIMER_STARTGAMEWAIT;
				Bukkit.broadcastMessage("§3>> §bGame will be starts in " + ArenaTempData.TIMER_STARTGAMEWAIT + " seconds!");
			}
		} else {
			event.getPlayer().sendMessage("§7>> §3/bgadmin wizard §7to setup arena!");
		}
	}
	
	@EventHandler
	public void leave(PlayerQuitEvent event) {
		event.setQuitMessage("§3>> §b" + event.getPlayer().getName() + " §7leaved! §8(" + (Bukkit.getOnlinePlayers().size() - 1) + "/" + Bukkit.getMaxPlayers() + ")");
		if (Bukkit.getOnlinePlayers().size() < 2 && plugin.arenaTemp.currentStatus == ArenaTempData.STATUS_STARTING) {
			plugin.arenaTemp.currentStatus = ArenaTempData.STATUS_STARTGAME;
			Bukkit.broadcastMessage("§3>> §bNot enough players! Canceled timer.");
		}
	}
	
}
