package com.jaoafa.mymaid4.event;

import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import com.jaoafa.mymaid4.lib.SKKColorManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;

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
}
