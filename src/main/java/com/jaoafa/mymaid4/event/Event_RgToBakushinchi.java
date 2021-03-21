package com.jaoafa.mymaid4.event;

import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class Event_RgToBakushinchi extends MyMaidLibrary implements Listener {
    @EventHandler
    public void onAirClick(PlayerCommandPreprocessEvent event) {
        if (event.getMessage().startsWith("/rg claim")) {
            event.getPlayer().sendMessage("[Bakushinchi] " + ChatColor.RED + "" + ChatColor.BOLD + "「/bakushinch claim」を使用してください！");
        }
    }
}
