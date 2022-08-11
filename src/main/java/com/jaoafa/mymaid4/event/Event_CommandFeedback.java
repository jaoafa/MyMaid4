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
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class Event_CommandFeedback extends MyMaidLibrary implements Listener, EventPremise {
    @Override
    public String description() {
        return "実行したコマンドを実行者に対して表示します。";
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player executor = event.getPlayer();
        String command = event.getMessage();

        executor.sendMessage(Component.join(JoinConfiguration.noSeparators(),
                Component.text("["),
                Component.text("Cmd", Style.style(TextDecoration.UNDERLINED))
                    .hoverEvent(HoverEvent.showText(Component.text("クリックすると実行したコマンドをコピーします。")))
                    .clickEvent(ClickEvent.copyToClipboard(command)),
                Component.text("] " + command))
            .colorIfAbsent(NamedTextColor.DARK_GRAY));
    }
}
