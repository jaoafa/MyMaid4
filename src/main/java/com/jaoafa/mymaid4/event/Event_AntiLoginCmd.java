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

import com.jaoafa.mymaid4.lib.EBan;
import com.jaoafa.mymaid4.lib.MyMaidData;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class Event_AntiLoginCmd extends MyMaidLibrary implements Listener {
    @EventHandler
    public void onOPCommand(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage();
        Player player = event.getPlayer();
        String[] args = command.split(" ", 0);
        if (args.length == 0 || !args[0].equalsIgnoreCase("/login")) return;

        EBan eban = EBan.getInstance(player);
        if (eban.isStatus()) {
            event.setCancelled(true);
            return;
        }

        eban.addBan("jaotan", String.format("コマンド「%s」を実行したことにより、サーバルールへの違反の可能性を検知したため", command));
        player.kick(Component.text("Disconnected."));
        if (MyMaidData.getJaotanChannel() != null)
            MyMaidData.getJaotanChannel().sendMessage(String.format("プレイヤー「%s」がコマンド「%s」を実行したため、キックしました。", player.getName(), command)).queue();
        else
            System.out.println("MyMaidData.getJaotanChannel is null");
        
        event.setCancelled(true);
    }
}
