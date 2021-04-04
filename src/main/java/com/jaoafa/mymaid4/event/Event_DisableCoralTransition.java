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
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFadeEvent;

import java.util.Arrays;
import java.util.List;

public class Event_DisableCoralTransition extends MyMaidLibrary implements Listener, EventPremise {
    @Override
    public String description() {
        return "サンゴの変化を無効化します。";
    }

    @EventHandler
    public void onBlockFadeEvent(BlockFadeEvent event) {
        Block block = event.getBlock();
        if (!isCorals(block.getType())) {
            return;
        }
        event.setCancelled(true);
    }

    boolean isCorals(Material material) {
        List<Material> corals = Arrays.asList(
            Material.DEAD_TUBE_CORAL_BLOCK,
            Material.DEAD_BRAIN_CORAL_BLOCK,
            Material.DEAD_BUBBLE_CORAL_BLOCK,
            Material.DEAD_FIRE_CORAL_BLOCK,
            Material.DEAD_HORN_CORAL_BLOCK,
            Material.TUBE_CORAL_BLOCK,
            Material.BRAIN_CORAL_BLOCK,
            Material.BUBBLE_CORAL_BLOCK,
            Material.FIRE_CORAL_BLOCK,
            Material.HORN_CORAL_BLOCK,
            Material.TUBE_CORAL,
            Material.BRAIN_CORAL,
            Material.BUBBLE_CORAL,
            Material.FIRE_CORAL,
            Material.HORN_CORAL,
            Material.DEAD_BRAIN_CORAL,
            Material.DEAD_BUBBLE_CORAL,
            Material.DEAD_FIRE_CORAL,
            Material.DEAD_HORN_CORAL,
            Material.DEAD_TUBE_CORAL,
            Material.TUBE_CORAL_FAN,
            Material.BRAIN_CORAL_FAN,
            Material.BUBBLE_CORAL_FAN,
            Material.FIRE_CORAL_FAN,
            Material.HORN_CORAL_FAN,
            Material.DEAD_TUBE_CORAL_FAN,
            Material.DEAD_BRAIN_CORAL_FAN,
            Material.DEAD_BUBBLE_CORAL_FAN,
            Material.DEAD_FIRE_CORAL_FAN,
            Material.DEAD_HORN_CORAL_FAN);

        return corals.contains(material);
    }
}