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
import com.jaoafa.mymaid4.lib.MyMaidData;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class Event_Ded extends MyMaidLibrary implements Listener, EventPremise {
    @Override
    public String description() {
        return "dedコマンドに関する処理を行います。";
    }

    @EventHandler
    public void onPlayerDeathEvent(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Location loc = player.getLocation();
        MyMaidData.setLastDed(player.getName(), loc);
        player.sendMessage(Component.join(JoinConfiguration.noSeparators(),
            Component.text("[DED]"),
            Component.space(),
            Component.text("死亡した場所に戻るには「/ded」コマンドが使用できます。", NamedTextColor.GREEN)));
        player.sendMessage(Component.join(JoinConfiguration.noSeparators(),
            Component.text("[DED]"),
            Component.space(),
            Component.text("=== [!] 警告 ===", NamedTextColor.RED, TextDecoration.BOLD)));
        player.sendMessage(Component.join(JoinConfiguration.noSeparators(),
            Component.text("[DED]"),
            Component.space(),
            Component.text("PvP等での「/ded」コマンドの利用は原則禁止です！", NamedTextColor.RED, TextDecoration.BOLD)));
        player.sendMessage(Component.join(JoinConfiguration.noSeparators(),
            Component.text("[DED]"),
            Component.space(),
            Component.text("多く使用すると迷惑行為として認識される場合もあります！", NamedTextColor.RED, TextDecoration.BOLD)));
    }
}
