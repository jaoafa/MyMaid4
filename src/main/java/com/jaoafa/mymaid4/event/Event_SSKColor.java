package com.jaoafa.mymaid4.event;

import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import com.jaoafa.mymaid4.lib.SKKColorManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class Event_SSKColor extends MyMaidLibrary implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEvent_ChatSKK(PlayerChatEvent event) {
        event.setFormat(
            SKKColorManager.ReplacePlayerSKKChatColor(
                event.getPlayer(),
                "%1",
                event.getFormat()
            )
        );
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEvent_JoinChangeMessage(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String JoinMessage = SKKColorManager.getPlayerSKKJoinMessage(player);
        if (JoinMessage != null) {
            event.setJoinMessage(JoinMessage);
        }

        /*for(Player p: Bukkit.getServer().getOnlinePlayers()) {
            new TabListSKKReloader(p).runTaskLater(plugin, 20L);
        }*/
    }
}
