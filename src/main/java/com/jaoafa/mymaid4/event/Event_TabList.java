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

import com.jaoafa.mymaid4.Main;
import com.jaoafa.mymaid4.lib.EventPremise;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import com.jaoafa.mymaid4.tasks.Task_TabList;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class Event_TabList extends MyMaidLibrary implements Listener, EventPremise {
    @Override
    public String description() {
        return "Tabで表示されるヘッダー・フッターに関する処理を行います。";
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        new Task_TabList().runTaskAsynchronously(Main.getJavaPlugin());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        new Task_TabList().runTaskAsynchronously(Main.getJavaPlugin());
    }
}
