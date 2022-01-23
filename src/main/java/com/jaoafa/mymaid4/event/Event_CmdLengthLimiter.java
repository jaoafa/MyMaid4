/*
 * jaoLicense
 *
 * Copyright (c) 2022 jao Minecraft Server
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

public class Event_CmdLengthLimiter extends MyMaidLibrary implements Listener, EventPremise {
    @EventHandler
    public static void onCommand(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage();
        Player player = event.getPlayer();

        if (MyMaidLibrary.isAMRV(player) || command.length() < 100) return;

        event.setCancelled(true);
        Component component = Component.text().append(
            Component.text("[CmdLengthLimiter]"),
            Component.space(),
            Component.text("あなたは100文字以上のコマンドを実行することが出来ません！", NamedTextColor.GREEN)
        ).build();
        player.sendMessage(component);
    }

    @Override
    public String description() {
        return "Default権限グループのプレイヤーによる100文字を超えるコマンドの実行を制限します。";
    }
}
