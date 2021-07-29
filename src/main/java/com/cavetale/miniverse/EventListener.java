package com.cavetale.miniverse;

import com.cavetale.core.event.block.PlayerCanBuildEvent;
import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import lombok.RequiredArgsConstructor;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

@RequiredArgsConstructor
public final class EventListener implements Listener {
    final MiniversePlugin plugin;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockFromTo(BlockFromToEvent event) {
        Miniverse miniverse = plugin.mapWorldOf(event.getBlock().getWorld());
        if (miniverse == null) return;
        plugin.getLogger().info(event.getEventName() + " " + toString(event.getBlock()));
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockFade(BlockFadeEvent event) {
        Miniverse miniverse = plugin.mapWorldOf(event.getBlock().getWorld());
        if (miniverse == null) return;
        plugin.getLogger().info(event.getEventName() + " " + toString(event.getBlock()));
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockGrow(BlockGrowEvent event) {
        Miniverse miniverse = plugin.mapWorldOf(event.getBlock().getWorld());
        if (miniverse == null) return;
        plugin.getLogger().info(event.getEventName() + " " + toString(event.getBlock()));
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockForm(BlockFormEvent event) {
        Miniverse miniverse = plugin.mapWorldOf(event.getBlock().getWorld());
        if (miniverse == null) return;
        plugin.getLogger().info(event.getEventName() + " " + toString(event.getBlock()));
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockSpread(BlockSpreadEvent event) {
        Miniverse miniverse = plugin.mapWorldOf(event.getBlock().getWorld());
        if (miniverse == null) return;
        plugin.getLogger().info(event.getEventName() + " " + toString(event.getBlock()));
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockDestroy(BlockDestroyEvent event) {
        Miniverse miniverse = plugin.mapWorldOf(event.getBlock().getWorld());
        if (miniverse == null) return;
        plugin.getLogger().info(event.getEventName() + " " + toString(event.getBlock()));
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        Miniverse miniverse = plugin.mapWorldOf(event.getBlock().getWorld());
        if (miniverse == null) return;
        plugin.getLogger().info(event.getEventName() + " " + toString(event.getBlock()));
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.hasBlock()) return;
        Miniverse miniverse = plugin.mapWorldOf(event.getClickedBlock().getWorld());
        if (miniverse == null) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Miniverse miniverse = plugin.mapWorldOf(event.getBlock().getWorld());
        if (miniverse == null) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        Miniverse miniverse = plugin.mapWorldOf(event.getBlock().getWorld());
        if (miniverse == null) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerBucketFill(PlayerBucketFillEvent event) {
        Miniverse miniverse = plugin.mapWorldOf(event.getBlock().getWorld());
        if (miniverse == null) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        Miniverse miniverse = plugin.mapWorldOf(event.getBlock().getWorld());
        if (miniverse == null) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        Miniverse miniverse = plugin.mapWorldOf(event.getEntity().getWorld());
        if (miniverse == null) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerCanBuild(PlayerCanBuildEvent event) {
        Miniverse miniverse = plugin.mapWorldOf(event.getBlock().getWorld());
        if (miniverse == null) return;
        event.setCancelled(true);
    }

    String toString(Block block) {
        return "" + block.getX() + "," + block.getY() + "," + block.getZ();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        if (!event.isSneaking()) return;
        Player player = event.getPlayer();
        if (!player.isOnGround()) return;
        World world = player.getWorld();
        Miniverse miniverse = plugin.miniverseOf(world);
        if (miniverse == null || !miniverse.isMapWorld(world)) return;
        plugin.switchPlayer(player);
    }
}
