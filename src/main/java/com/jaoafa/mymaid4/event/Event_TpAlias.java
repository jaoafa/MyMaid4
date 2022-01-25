/*
 * jaoLicense
 *
 * Copyright (c) 2022 jao Minecraft Server
 *
 * The following license applies to this project: jaoLicense
 *
 * Japanese: https://github.com/jaoafa/jao-Minecraft-Server/blob/master/jaoLICENSE.md
 * English: https://github.com/jaoafa/jao-Minecraft-Server/blob/master/jaoLICENSE-en.md
 */

package com.jaoafa.mymaid4.event;

import com.jaoafa.mymaid4.lib.EventPremise;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import com.jaoafa.mymaid4.lib.TeleportAlias;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class Event_TpAlias extends MyMaidLibrary implements Listener, EventPremise {
    @Override
    public String description() {
        return "テレポートエイリアスを処理します。";
    }

    @EventHandler(ignoreCancelled = true,
                  priority = EventPriority.LOWEST)
    public void onTeleportCommand(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage();
        String[] args = command.split(" ");
        if (args.length == 0) {
            return; // 本来発生しないと思うけど
        }
        if (!args[0].equalsIgnoreCase("/tp") &&
            !args[0].equalsIgnoreCase("/minecraft:tp") &&
            !args[0].equalsIgnoreCase("/teleport") &&
            !args[0].equalsIgnoreCase("/minecraft:teleport")) {
            return; // tpコマンド以外
        }
        if (args.length == 2) {
            String to = args[1];
            String replacement = TeleportAlias.getReplaceAlias(to);
            if (replacement == null) {
                return;
            }
            event.setMessage(args[0] + " " + replacement);
        }
    }
}
