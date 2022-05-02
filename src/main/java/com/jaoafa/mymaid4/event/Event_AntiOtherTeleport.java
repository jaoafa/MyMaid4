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

import com.jaoafa.mymaid4.customEvents.TeleportCommandEvent;
import com.jaoafa.mymaid4.lib.EventPremise;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class Event_AntiOtherTeleport extends MyMaidLibrary implements Listener, EventPremise {
    @Override
    public String description() {
        return "Verified以下のプレイヤーが他人をテレポートすることを制限します。";
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTeleportCommand(TeleportCommandEvent event) {
        CommandSender sender = event.getSender();
        Player from = event.getFromPlayer();

        if (!(sender instanceof Player player)) {
            return; // プレイヤー以外による実行の場合は何もしない
        }
        if (isAMR(player)) {
            return; // AMRなら何もしない
        }

        if (from.getUniqueId().equals(player.getUniqueId())) {
            return; // 自分自身のテレポートは許可
        }

        player.sendMessage("[TeleportCheck] " + ChatColor.GREEN + "Verified以下のプレイヤーが他人をテレポートすることはできません。");
        event.setCancelled(true);
    }
}
