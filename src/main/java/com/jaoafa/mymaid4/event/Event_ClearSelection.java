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
import com.jaoafa.mymaid4.lib.SelClickManager;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class Event_ClearSelection extends MyMaidLibrary implements Listener, EventPremise {
    @Override
    public String description() {
        return "走りながら空気をクリックして/selを実行します。";
    }

    @EventHandler
    public void onAirClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction() != Action.LEFT_CLICK_AIR) {
            return;
        }
        if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
            return;
        }
        if (!isAMRV(player)) {
            return;
        }
        if (!player.isSprinting()) {
            return;
        }
        if (!SelClickManager.isEnable(player)) {
            return;
        }
        player.performCommand("/sel");
        player.getWorld().playSound(
            player.getLocation(),
            Sound.BLOCK_WOODEN_BUTTON_CLICK_ON,
            3,
            1
        );
    }
}