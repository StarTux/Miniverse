package com.cavetale.miniverse;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.plugin.java.JavaPlugin;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextDecoration.*;

public final class MiniversePlugin extends JavaPlugin {
    List<Miniverse> miniverses = new ArrayList<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        EventListener eventListener = new EventListener(this);
        Bukkit.getPluginManager().registerEvents(eventListener, this);
        Bukkit.getScheduler().runTaskTimer(this, this::onTick, 1, 1);
        getCommand("miniverse").setExecutor(new MiniverseCommand(this));
        getCommand("switch").setExecutor(new SwitchCommand(this));
        loadMiniverseConfig();
    }

    @Override
    public void onDisable() {
    }

    void loadMiniverseConfig() {
        reloadConfig();
        miniverses.clear();
        ConfigurationSection worldsSection = getConfig().getConfigurationSection("worlds");
        for (String key : worldsSection.getKeys(false)) {
            ConfigurationSection section = worldsSection.getConfigurationSection(key);
            Miniverse miniverse = new Miniverse(this);
            miniverse.mapWorldName = section.getString("map", "");
            miniverse.sourceWorldName = section.getString("source", "");
            miniverse.scaleX = section.getInt("scale", 16);
            miniverse.scaleZ = miniverse.scaleX;
            miniverse.scaleY = section.getInt("scaleY", 4);
            miniverse.offsetY = section.getInt("offsetY", 64);
            miniverses.add(miniverse);
            getLogger().info("Miniverse loaded: " + miniverse.info());
        }
        getLogger().info("" + miniverses.size() + " miniverses loaded");
    }

    void onTick() {
        for (Miniverse miniverse : miniverses) {
            miniverse.tick();
        }
    }

    Miniverse sourceWorldOf(World world) {
        String name = world.getName();
        for (Miniverse miniverse : miniverses) {
            if (name.equals(miniverse.sourceWorldName)) return miniverse;
        }
        return null;
    }

    Miniverse mapWorldOf(World world) {
        String name = world.getName();
        for (Miniverse miniverse : miniverses) {
            if (name.equals(miniverse.mapWorldName)) return miniverse;
        }
        return null;
    }

    Miniverse miniverseOf(World world) {
        String name = world.getName();
        for (Miniverse miniverse : miniverses) {
            if (name.equals(miniverse.sourceWorldName)) return miniverse;
            if (name.equals(miniverse.mapWorldName)) return miniverse;
        }
        return null;
    }

    public boolean switchPlayer(Player player) {
        Miniverse miniverse = miniverseOf(player.getWorld());
        if (miniverse == null) {
            player.sendMessage("You're not in a mapped world");
            return false;
        }
        final Location loc = miniverse.translateLocation(player.getLocation());
        if (loc == null) {
            player.sendMessage("Can't find miniverse! :(");
            return false;
        }
        loc.getWorld().getChunkAtAsync(loc, (Consumer<Chunk>) unusedChunk -> {
                while (loc.getY() > 0 && loc.getBlock().isEmpty()) loc.add(0, -1, 0);
                while (loc.getY() < 255 && !loc.getBlock().isEmpty()) loc.add(0, 1, 0);
                if (!player.teleport(loc, TeleportCause.PLUGIN)) {
                    player.sendMessage("Can't teleport! :(");
                }
            });
        if (miniverse.isMapWorld(player.getWorld())) {
            player.sendMessage(text("Switching to Miniverse", AQUA, ITALIC));
        } else {
            player.sendMessage(text("Switching to Original", GREEN, BOLD));
        }
        return true;
    }
}
