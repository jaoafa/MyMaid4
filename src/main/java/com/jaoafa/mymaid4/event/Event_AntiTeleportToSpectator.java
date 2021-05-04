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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class Event_AntiTeleportToSpectator extends MyMaidLibrary implements Listener, EventPremise {
    @Override
    public String description() {
        return "スペクテイタープレイヤーへのテレポートを禁止します。";
    }

    @EventHandler
    public void onTeleportCommand(TeleportCommandEvent event) {
        Player toPlayer = event.getToPlayer();
        if (toPlayer == null) {
            return;
        }
        if (toPlayer.getGameMode() != GameMode.SPECTATOR) {
            // 相手がスペクテイター以外の場合
            return;
        }
        if (event.getFromPlayer().getGameMode() == GameMode.SPECTATOR) {
            // 相手がスペクテイターかつ、自分もスペクテイターの場合
            return;
        }
        // -> 相手がスペクテイターで、自分がスペクテイター以外の場合
        if (!(event.getSender() instanceof Player) || isAMRV((Player) event.getSender())) {
            event.getSender().sendMessage(Component.text().append(
                Component.text("[TELEPORT]"),
                Component.space(),
                Component.text("スペクテイタープレイヤーへのテレポートは禁止されています。スペクテイターで実行してください。").style(Style.style(NamedTextColor.GREEN))
            ));
        } else {
            event.getSender().sendMessage(Component.text().append(
                Component.text("[TELEPORT]"),
                Component.space(),
                Component.text("何らかの理由によりテレポートできませんでした。").style(Style.style(NamedTextColor.GREEN))
            ));
        }
        event.setCancelled(true);
    }
}