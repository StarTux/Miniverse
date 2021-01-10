package com.cavetale.miniverse;

import lombok.RequiredArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public final class SwitchCommand implements CommandExecutor {
    final MiniversePlugin plugin;

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (args.length != 0) return false;
        if (!(sender instanceof Player)) {
            sender.sendMessage("[Miniverse] Player expected!");
            return true;
        }
        plugin.switchPlayer((Player) sender);
        return true;
    }
}
