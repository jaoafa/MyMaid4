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
import org.bukkit.Material;
import org.bukkit.block.data.type.RespawnAnchor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class Event_DisableRespawnAnchor extends MyMaidLibrary implements Listener, EventPremise {
    @Override
    public String description() {
        return "リスポーンアンカーの爆発を無効化します。";
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (event.getClickedBlock() == null) {
            return;
        }
        if (event.getClickedBlock().getType() != Material.RESPAWN_ANCHOR) {
            return;
        }
        RespawnAnchor respawnAnchor = (RespawnAnchor) event.getClickedBlock().getBlockData();
        if (event.getItem() != null && event.getItem().getType() == Material.GLOWSTONE &&
            respawnAnchor.getCharges() != respawnAnchor.getMaximumCharges()) {
            return;
        }
        event.setCancelled(true);
    }
}