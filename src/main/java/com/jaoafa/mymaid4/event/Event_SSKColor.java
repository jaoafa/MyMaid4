package com.jaoafa.mymaid4.event;

import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import com.jaoafa.mymaid4.lib.SKKColorManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
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
            SKKColorManager.replacePlayerSKKChatColor(
                event.getPlayer(),
                "%1",
                event.getFormat()
            )
        );
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEvent_JoinChangeMessage(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Component JoinMessage = SKKColorManager.getPlayerSKKJoinMessage(player);
        if (JoinMessage != null) {
            event.joinMessage(SKKColorManager.getPlayerSKKJoinMessage(player));
        }

        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            SKKColorManager.setPlayerSKKTabList(player);
        }
    }
}
