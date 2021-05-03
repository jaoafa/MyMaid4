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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class Event_AutoSetHandFacing extends MyMaidLibrary implements Listener, EventPremise {
    @Override
    public String description() {
        return "「//set hand[facing=look]」が実行された際に自動で向いている方向に置き換えます。";
    }

    @EventHandler
    public void onSetHandFacingCommand(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage();
        Player player = event.getPlayer();
        String[] args = command.split(" ");
        if (args.length == 0) {
            return;
        }
        if (!command.equalsIgnoreCase("//set hand[facing=look]")) {
            return; //set hand[facing=look]コマンド以外
        }
        if (isAMRV(player)) {
            event.setMessage(command.replace("look",player.getFacing().name()));
        }
        event.setCancelled(true);
    }
}
