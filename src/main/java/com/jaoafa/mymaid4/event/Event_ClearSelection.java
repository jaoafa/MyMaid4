package com.jaoafa.mymaid4.event;

import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class Event_ClearSelection extends MyMaidLibrary implements Listener {
    @EventHandler
    public void onAirClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction() != Action.LEFT_CLICK_AIR) {
            return;
        }
        if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
            return;
        }
        if (!isAMRV(player)) {
            return;
        }
        if (!player.isSprinting()) {
            return;
        }
        player.performCommand("/sel");
        player.sendMessage("[SEL] " + ChatColor.GREEN + "Selection Cleared!");
    }
}
