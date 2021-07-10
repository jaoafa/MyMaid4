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

import com.jaoafa.mymaid4.lib.EventPremise;
import com.jaoafa.mymaid4.lib.MyMaidData;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class Event_TempMute extends MyMaidLibrary implements Listener, EventPremise {
    @Override
    public String description() {
        return "tempmuteコマンドに関する処理を行います。";
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        if (MyMaidData.getTempMuting().size() == 0) return;

        event.viewers().removeAll(MyMaidData.getTempMuting());
    }
}
