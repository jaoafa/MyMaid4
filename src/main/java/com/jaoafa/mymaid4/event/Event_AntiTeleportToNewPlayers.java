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

import com.jaoafa.mymaid4.customEvents.TeleportCommandEvent;
import com.jaoafa.mymaid4.lib.EventPremise;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import org.bukkit.GameMode;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class Event_AntiTeleportToNewPlayers extends MyMaidLibrary implements Listener, EventPremise {
    @Override
    public String description() {
        return "新規プレイヤーへのテレポートを規制します。";
    }

    @EventHandler
    public void onTeleportCommand(TeleportCommandEvent event) {
        if (!isAMR(event.getFromPlayer())) {
            return;
        }
        Player toPlayer = event.getToPlayer();
        if (toPlayer == null) {
            return;
        }
        if (event.getFromPlayer().getGameMode() == GameMode.SPECTATOR) {
            return;
        }
        if (toPlayer.getStatistic(Statistic.PLAY_ONE_MINUTE) > 72000) {
            // 既に1時間過
            return;
        }
        event.getSender().sendMessage(Component.text().append(
            Component.text("[TELEPORT]"),
            Component.space(),
            Component.text("新規プレイヤーへのテレポートは制限されています。スペクテイターで実行してください。").style(Style.style(NamedTextColor.GREEN))
        ));
        event.setCancelled(true);
    }
}