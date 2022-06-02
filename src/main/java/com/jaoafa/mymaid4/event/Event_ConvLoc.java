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

import com.jaoafa.mymaid4.lib.ConvLoc;
import com.jaoafa.mymaid4.lib.EventPremise;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.Objects;

public class Event_ConvLoc extends MyMaidLibrary implements Listener, EventPremise {
    @Override
    public String description() {
        return "convlocコマンドに関する処理を行います。";
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();
        Block block = event.getClickedBlock();

        if (action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (player.getInventory().getItemInMainHand().getType() != Material.STICK) {
            return;
        }

        ItemMeta meta = player.getInventory().getItemInMainHand().getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return;
        }

        if (!player.hasPermission("mymaid.convloc")) {
            return;
        }

        String displayName = PlainTextComponentSerializer.plainText().serialize(Objects.requireNonNull(meta.displayName()));
        if (!displayName.equals("ConvLocStick : RELATIVE") && !displayName.equals("ConvLocStick : ABSOLUTE")) {
            return;
        }
        boolean isRelative = displayName.equals("ConvLocStick : RELATIVE");

        if (block == null) {
            return;
        }
        if (block.getType() != Material.COMMAND_BLOCK && block.getType() != Material.CHAIN_COMMAND_BLOCK && block.getType() != Material.REPEATING_COMMAND_BLOCK) {
            return;
        }

        new ConvLoc().replace(player, Collections.singletonList(block), isRelative);
        event.setCancelled(true);
    }
}
