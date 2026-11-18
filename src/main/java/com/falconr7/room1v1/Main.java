package com.falconr7.room1v1;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class Main extends JavaPlugin implements Listener, CommandExecutor {

    private Location door1;
    private Location door2;
    private Location center;
    private Location exitLoc;
    private Location spawn1;
    private Location spawn2;
    private boolean active = false;
    private final List<Player> fighters = new ArrayList<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadLocations();

        getServer().getPluginManager().registerEvents(this, this);
        getCommand("minehavvyroom").setExecutor(this);
        getCommand("minehavvyroom2").setExecutor(this);
        getLogger().info("MinehavvyRoom eklentisi aktif! - FALCONR7");

        Bukkit.getScheduler().runTaskTimer(this, () -> {
            if (center == null || door1 == null || door2 == null) return;

            if (!active) {
                List<Player> inside = new ArrayList<>();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getWorld().equals(center.getWorld()) && p.getLocation().distance(center) <= 6.0) {
                        if (p.getGameMode() == GameMode.SURVIVAL) {
                            inside.add(p);
                        }
                    }
                }

                if (inside.size() == 2) {
                    active = true;
                    fighters.addAll(inside);

                    // Oyuncuları spawn1 ve spawn2 noktalarına ışınla
                    if (spawn1 != null) fighters.get(0).teleport(spawn1);
                    if (spawn2 != null) fighters.get(1).teleport(spawn2);

                    // Kapıyı NORMAL CAM ile kapat
                    setBlocks(Material.GLASS);

                    for (Player p : fighters) {
                        p.sendMessage(ChatColor.RED + "1v1 BAŞLADI! Rakibini yenene kadar kapı açılmayacak.");
                    }
                } else if (inside.size() > 2) {
                    for (int i = 2; i < inside.size(); i++) {
                        if (exitLoc != null) {
                            inside.get(i).teleport(exitLoc);
                        }
                        inside.get(i).sendMessage(ChatColor.RED + "Bu oda en fazla 2 kişiliktir!");
                    }
                }
            }
        }, 0L, 20L);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player p = (Player) sender;

        if (!p.isOp()) {
            p.sendMessage(ChatColor.RED + "Bu komutu kullanma yetkin yok!");
            return true;
        }

        if (command.getName().equalsIgnoreCase("minehavvyroom2")) {
            spawn2 = p.getLocation();
            saveLocations();
            p.sendMessage(ChatColor.GREEN + "2. Oyuncunun başlangıç noktası (spawn2) ayarlandı!");
            return true;
        }

        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("kapi1")) {
                Block target = p.getTargetBlockExact(5);
                if (target == null) {
                    p.sendMessage(ChatColor.RED + "Bir bloğa bakarak komutu gir!");
                    return true;
                }
                door1 = target.getLocation();
                saveLocations();
                p.sendMessage(ChatColor.GREEN + "Kapının 1. köşesi ayarlandı!");
            } else if (args[0].equalsIgnoreCase("kapi2")) {
                Block target = p.getTargetBlockExact(5);
                if (target == null) {
                    p.sendMessage(ChatColor.RED + "Bir bloğa bakarak komutu gir!");
                    return true;
                }
                door2 = target.getLocation();
                saveLocations();
                p.sendMessage(ChatColor.GREEN + "Kapının 2. köşesi ayarlandı!");
            } else if (args[0].equalsIgnoreCase("iceri")) {
                center = p.getLocation();
                saveLocations();
                p.sendMessage(ChatColor.GREEN + "Oda içi kontrol noktası ayarlandı!");
            } else if (args[0].equalsIgnoreCase("disari")) {
                exitLoc = p.getLocation();
                saveLocations();
                p.sendMessage(ChatColor.GREEN + "Oda dışı atılma noktası ayarlandı!");
            } else if (args[0].equalsIgnoreCase("spawn1")) {
                spawn1 = p.getLocation();
                saveLocations();
                p.sendMessage(ChatColor.GREEN + "1. Oyuncunun başlangıç noktası (spawn1) ayarlandı!");
            } else if (args[0].equalsIgnoreCase("spawn2")) {
                spawn2 = p.getLocation();
                saveLocations();
                p.sendMessage(ChatColor.GREEN + "2. Oyuncunun başlangıç noktası (spawn2) ayarlandı!");
            } else if (args[0].equalsIgnoreCase("sifirla")) {
                resetRoom();
                p.sendMessage(ChatColor.GREEN + "Oda sıfırlandı ve cam kapı açıldı!");
            }
        } else {
            p.sendMessage(ChatColor.YELLOW + "/minehavvy <kapi1 | kapi2 | iceri | disari | spawn1 | spawn2 | sifirla>");
            p.sendMessage(ChatColor.YELLOW + "Kısayol: /minehavvyroom2");
        }
        return true;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        if (active && fighters.contains(e.getEntity())) {
            resetRoom();
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendMessage(ChatColor.GOLD + "1v1 düellosu sona erdi, oda tekrar açıldı!");
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        if (active && fighters.contains(e.getPlayer())) {
            resetRoom();
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendMessage(ChatColor.YELLOW + "Oyuncu oyundan çıktığı için 1v1 iptal edildi ve kapı açıldı!");
            }
        }
    }

    private void setBlocks(Material mat) {
        if (door1 == null || door2 == null) return;
        int minX = Math.min(door1.getBlockX(), door2.getBlockX());
        int maxX = Math.max(door1.getBlockX(), door2.getBlockX());
        int minY = Math.min(door1.getBlockY(), door2.getBlockY());
        int maxY = Math.max(door1.getBlockY(), door2.getBlockY());
        int minZ = Math.min(door1.getBlockZ(), door2.getBlockZ());
        int maxZ = Math.max(door1.getBlockZ(), door2.getBlockZ());

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    door1.getWorld().getBlockAt(x, y, z).setType(mat);
                }
            }
        }
    }

    private void resetRoom() {
        active = false;
        setBlocks(Material.AIR);
        fighters.clear();
    }

    private void saveLocations() {
        getConfig().set("door1", door1);
        getConfig().set("door2", door2);
        getConfig().set("center", center);
        getConfig().set("exitLoc", exitLoc);
        getConfig().set("spawn1", spawn1);
        getConfig().set("spawn2", spawn2);
        saveConfig();
    }

    private void loadLocations() {
        door1 = getConfig().getLocation("door1");
        door2 = getConfig().getLocation("door2");
        center = getConfig().getLocation("center");
        exitLoc = getConfig().getLocation("exitLoc");
        spawn1 = getConfig().getLocation("spawn1");
        spawn2 = getConfig().getLocation("spawn2");
    }
}
