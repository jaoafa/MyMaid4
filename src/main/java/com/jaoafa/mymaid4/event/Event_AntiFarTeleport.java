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
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

public class Event_AntiFarTeleport extends MyMaidLibrary implements Listener, EventPremise {
    @Override
    public String description() {
        return "極端に遠い場所へのテレポートを禁止します。";
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerTeleportEvent(PlayerTeleportEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        Player player = event.getPlayer();
        if (!from.getWorld().getName().equals(to.getWorld().getName())) {
            return;
        }
        if (Math.abs(to.getY()) <= 8192) { // プラスマイナス8192未満の場合何もしない
            return;
        }
        player.sendMessage("[TeleportCheck] " + ChatColor.GREEN + "Y座標が極端な場所へはテレポートできません。");
        event.setCancelled(true);
    }
}
