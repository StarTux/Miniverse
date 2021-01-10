package com.cavetale.miniverse;

import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public final class MiniverseCommand implements CommandExecutor {
    final MiniversePlugin plugin;

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (args.length == 0) return false;
        return onCommand(sender, args[0], Arrays.copyOfRange(args, 1, args.length));
    }

    boolean onCommand(CommandSender sender, String cmd, String[] args) {
        Player player = sender instanceof Player ? (Player) sender : null;
        switch (cmd) {
        case "switch": {
            if (args.length != 0) return false;
            if (player == null) return false;
            plugin.switchPlayer(player);
            return true;
        }
        case "info": {
            if (args.length != 0) return false;
            if (player == null) return false;
            if (!player.hasPermission("miniverse.admin")) return false;
            Miniverse miniverse = plugin.miniverseOf(player.getWorld());
            if (miniverse == null) {
                sender.sendMessage("Not a miniverse world!");
                return true;
            }
            sender.sendMessage("Miniverse: " + miniverse.info());
            return true;
        }
        case "init": {
            if (args.length != 0) return false;
            if (player == null) return false;
            if (!player.hasPermission("miniverse.admin")) return false;
            Miniverse miniverse = plugin.miniverseOf(player.getWorld());
            if (miniverse == null) {
                sender.sendMessage("Not a miniverse world!");
                return true;
            }
            if (!miniverse.initialize()) {
                sender.sendMessage("Could not initialize successfully: " + miniverse.info());
            } else {
                sender.sendMessage("Initialized miniverse: " + miniverse.info());
            }
            return true;
        }
        case "load": {
            if (args.length != 0) return false;
            if (player == null) return false;
            if (!player.hasPermission("miniverse.admin")) return false;
            World world = player.getWorld();
            Miniverse miniverse = plugin.sourceWorldOf(world);
            if (miniverse == null) {
                player.sendMessage("You're not in a mapped world");
                return true;
            }
            int viewDist = world.getViewDistance();
            Chunk center = player.getLocation().getChunk();
            int cx = center.getX();
            int cz = center.getZ();
            int chunkCount = 0;
            int count = 0;
            for (int z = -viewDist; z <= viewDist; z += 1) {
                for (int x = -viewDist; x <= viewDist; x += 1) {
                    int chunkX = cx + x;
                    int chunkZ = cz + z;
                    if (!world.isChunkLoaded(chunkX, chunkZ)) continue;
                    int sum = miniverse.inputSourceChunk(world.getChunkAt(chunkX, chunkZ));
                    if (sum > 0) {
                        count += sum;
                        chunkCount += 1;
                    }
                }
            }
            player.sendMessage("" + chunkCount + " chunks, " + count + " pillars queued, viewDistance=" + viewDist);
            return true;
        }
        case "pillar": {
            if (args.length != 0) return false;
            if (player == null) return false;
            if (!player.hasPermission("miniverse.admin")) return false;
            World world = player.getWorld();
            Miniverse miniverse = plugin.mapWorldOf(world);
            if (miniverse == null) {
                player.sendMessage("You're not in a map world");
                return true;
            }
            Location loc = player.getLocation();
            miniverse.processMapPillar(loc.getBlockX(), loc.getBlockZ());
            player.sendMessage("Calculating pillar");
            return true;
        }
        case "fill": {
            if (args.length != 0) return false;
            if (player == null) return false;
            if (!player.hasPermission("miniverse.admin")) return false;
            Miniverse miniverse = plugin.miniverseOf(player.getWorld());
            if (miniverse == null) {
                sender.sendMessage("Not a miniverse world!");
                return true;
            }
            miniverse.startFilling();
            player.sendMessage("Started filling: " + miniverse.info());
            return true;
        }
        case "stop": {
            if (args.length != 0) return false;
            if (player == null) return false;
            if (!player.hasPermission("miniverse.admin")) return false;
            Miniverse miniverse = plugin.miniverseOf(player.getWorld());
            if (miniverse == null) {
                sender.sendMessage("Not a miniverse world!");
                return true;
            }
            miniverse.stopFilling();
            player.sendMessage("Stopped filling: " + miniverse.info());
            return true;
        }
        default: return false;
        }
    }
}
