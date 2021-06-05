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
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class Event_AntiEndPortal extends MyMaidLibrary implements Listener, EventPremise {
    @Override
    public String description() {
        return "エンドポータルの作成を制限します。";
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (event.getClickedBlock() == null) {
            return;
        }
        if (event.getClickedBlock().getType() != Material.END_PORTAL_FRAME) {
            return;
        }
        if (event.getMaterial() != Material.ENDER_EYE) {
            return;
        }
        event.getPlayer().sendMessage(Component.text().append(
            Component.text("["),
            Component.text("AntiEndPortal", NamedTextColor.RED),
            Component.text("]"),
            Component.space(),
            Component.text("エンドラの息がくさすぎて死者が出るので、このサーバにはエンドはありません。ジ・エンドってね。", NamedTextColor.GREEN)
        ));
        event.setCancelled(true);
    }
}
