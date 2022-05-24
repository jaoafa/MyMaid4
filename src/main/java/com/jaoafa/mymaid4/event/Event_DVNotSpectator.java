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
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;

public class Event_DVNotSpectator extends MyMaidLibrary implements Listener, EventPremise {
    @Override
    public String description() {
        return "DefaultとVerified権限グループのプレイヤーによるスペクテイター変更を禁止します。";
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChangeGameMode(PlayerGameModeChangeEvent event) {
        Player player = event.getPlayer();
        if (event.getNewGameMode() != GameMode.SPECTATOR) {
            return;
        }
        if (isAMR(player)) {
            return;
        }
        player.sendMessage(Component.join(JoinConfiguration.noSeparators(),
            Component.text("[GAMEMODE] "),
            Component.text("あなたはゲームモードをスペクテイターに切り替えることができません。", NamedTextColor.GREEN)));
        event.setCancelled(true);
    }
}