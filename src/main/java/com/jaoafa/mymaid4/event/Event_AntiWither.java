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

import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class Event_AntiWither extends MyMaidLibrary implements Listener {
    @EventHandler
    public void CreatureSpawn(CreatureSpawnEvent event) {
        // 召喚されたMOBのエンティティを取得する
        LivingEntity ent = event.getEntity();
        // ウィザーの召喚操作によるものか？
        if (ent.getType() != EntityType.WITHER ||
            event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.BUILD_WITHER) {
            return;
        }
        Location location = event.getLocation();
        double min = 1.79769313486231570E+308;
        Player min_player = null;
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            org.bukkit.Location location_p = player.getLocation();
            if (location.getWorld().getName().equals(location_p.getWorld().getName())) {
                double distance = location.distance(location_p);
                if (distance < min) {
                    min = distance;
                    min_player = player;
                }
            }
        }
        if (min_player == null) {
            event.setCancelled(true);
            return;
        }
        event.setCancelled(true);
        min_player.sendMessage("[AntiWither] " + ChatColor.GREEN + "負荷対策の為にウィザーの召喚を禁止しています。ご協力をお願いします。");
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            if (isAM(p)) {
                p.sendMessage("[" + ChatColor.RED + "NoWither" + ChatColor.WHITE + "] " + ChatColor.GREEN + min_player.getName() + "の近くでウィザーが発生しましたが、発生を規制されました。");
            }
        }
    }
}
