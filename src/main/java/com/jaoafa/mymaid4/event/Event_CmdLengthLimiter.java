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

import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class Event_CmdLengthLimiter extends MyMaidLibrary implements Listener {
        @EventHandler
        public void onCommand(PlayerCommandPreprocessEvent event) {
            String command = event.getMessage();
            Player player = event.getPlayer();

            if (isAMRV(player)||command.length() < 100) {
                return;
            }

            event.setCancelled(true);
            player.sendMessage("[CmdLengthLimiter] あなたは100文字以上のコマンドを実行することが出来ません！");
        }
}
