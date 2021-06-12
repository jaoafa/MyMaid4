/*
 * jaoLicense
 *
 * Copyright (c) 2021 jao Minecraft Server
 *
 * The following license applies to this project: jaoLicense
 *
 * Japanese: https://github.com/jaoafa/jao-Minecraft-Server/blob/master/jaoLICENSE.md
 * English: https://github.com/jaoafa/jao-Minecraft-Server/blob/master/jaoLICENSE-en.md
 */

package com.jaoafa.mymaid4.event;

import com.jaoafa.mymaid4.lib.EventPremise;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Event_ManageCPPerms extends MyMaidLibrary implements Listener, EventPremise {
    static final Map<String, String> cpSubCommands = new HashMap<>();

    static {
        // SubArgument : PermissionNode
        cpSubCommands.put("rollback", "coreprotect.rollback");
        cpSubCommands.put("rb", "coreprotect.rollback");
        cpSubCommands.put("ro", "coreprotect.rollback");

        cpSubCommands.put("restore", "coreprotect.restore");
        cpSubCommands.put("rs", "coreprotect.restore");
        cpSubCommands.put("re", "coreprotect.restore");
        cpSubCommands.put("undo", "coreprotect.restore");

        cpSubCommands.put("i", "coreprotect.inspect");
        cpSubCommands.put("inspect", "coreprotect.inspect");

        cpSubCommands.put("help", "coreprotect.help");

        cpSubCommands.put("purge", "coreprotect.purge");

        cpSubCommands.put("l", "coreprotect.lookup");
        cpSubCommands.put("lookup", "coreprotect.lookup");

        cpSubCommands.put("near", "coreprotect.lookup.near");

        cpSubCommands.put("tp", "coreprotect.teleport");
        cpSubCommands.put("teleport", "coreprotect.teleport");

        cpSubCommands.put("reload", "coreprotect.reload");

        cpSubCommands.put("status", "coreprotect.status");
        cpSubCommands.put("stats", "coreprotect.status");
        cpSubCommands.put("version", "coreprotect.status");
    }

    @Override
    public String description() {
        return "CoreProtectのパーミッションノードを管理します。";
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage();
        Player player = event.getPlayer();
        if (command.length() == 0) {
            return;
        }
        String[] args = command.split(" ");
        if (!args[0].equalsIgnoreCase("/coreprotect") &&
            !args[0].equalsIgnoreCase("/core") &&
            !args[0].equalsIgnoreCase("/co")) {
            return;
        }
        if (args.length == 1) {
            return;
        }

        Optional<Map.Entry<String, String>> func = cpSubCommands.entrySet().stream()
            .filter(cmd -> cmd.getKey().equalsIgnoreCase(args[1]))
            .findFirst();

        if (func.isEmpty()) {
            return;
        }
        if (player.hasPermission(func.get().getValue())) {
            return;
        }
        player.sendMessage(Component.text().append(
            Component.text("[CoreProtect] "),
            Component.text("あなたにはこのコマンドを実行する権限がありません。", NamedTextColor.GREEN)
        ));
        event.setCancelled(true);
    }
}
