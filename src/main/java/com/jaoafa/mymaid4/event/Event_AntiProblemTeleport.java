package com.jaoafa.mymaid4.event;

import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

public class Event_AntiProblemTeleport extends MyMaidLibrary implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerTeleportEvent(PlayerTeleportEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        Player player = event.getPlayer();
        if (!from.getWorld().getName().equals(to.getWorld().getName())){
            return;
        }
        if ((to.getY()>8192)||(to.getY()<-8192)){
            player.sendMessage("[TeleportCheck] " + ChatColor.GREEN + "Y座標が極端な場所へはテレポートできません");
            event.setCancelled(true);
        }
    }
}