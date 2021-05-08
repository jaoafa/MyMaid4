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
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class Event_Set43 extends MyMaidLibrary implements Listener, EventPremise {
    @Override
    public String description() {
        return "set 43コマンドを従来の動作に戻します。";
    }

    @EventHandler
    public void onTeleportCommandFromPlayer(PlayerCommandPreprocessEvent event) {
        Player sender = event.getPlayer();
        String command = event.getMessage();
        if (!command.startsWith("//set 43")) {
            return; // set 43以外
        }
        event.setCancelled(true);
        Bukkit.dispatchCommand(sender, "/set smooth_stone_slab[type=double]");
    }
}
