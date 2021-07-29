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
import com.jaoafa.mymaid4.lib.MyMaidData;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class Event_CommandSendR extends MyMaidLibrary implements Listener, EventPremise {
    @Override
    public String description() {
        return "実行されたコマンドをRegularに通知します。";
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage();
        String group = getPermissionMainGroup(player);
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            //AMRかつAMではない = R
            //かつ実行者本人ではない
            if (p.getName().equals("yuuaHP")) {
                p.sendMessage(
                    Component.text()
                        .color(NamedTextColor.DARK_GRAY)
                        .append(
                            // [Group/PlayerName] command test test (取り消し済み)
                            Component.text(
                                String.format("[%s/", group),
                                NamedTextColor.GRAY
                            ),
                            Component.text(
                                player.getName(),
                                Style.style()
                                    .color(NamedTextColor.DARK_AQUA)
                                    .decorate(TextDecoration.UNDERLINED)
                                    .clickEvent(ClickEvent.runCommand("/g sp"))
                                    .clickEvent(ClickEvent.runCommand("/tp " + player.getName()))
                                    .hoverEvent(HoverEvent.showText(
                                        Component.text(String.format("スぺテクターで%sにテレポート", player.getName()))
                                    ))
                                    .build()
                            ),
                            Component.text(
                                "] ",
                                NamedTextColor.GRAY
                            ),
                            Component.text(command, NamedTextColor.YELLOW),
                            Component.text((event.isCancelled() ? " (取り消し済み)" : ""), NamedTextColor.RED)
                        )
                );

            }
            if (isAMR(p) && (!isAM(p)) && (!player.getName().equals(p.getName()))) {
                if (MyMaidData.getTempMuting().contains(player)) return;

                p.sendMessage(
                    Component.text()
                        .color(NamedTextColor.DARK_GRAY)
                        .append(
                            // [Group/PlayerName] command test test (取り消し済み)
                            Component.text(
                                String.format("[%s/", group),
                                NamedTextColor.GRAY
                            ),
                            Component.text(
                                player.getName(),
                                Style.style()
                                    .color(NamedTextColor.DARK_AQUA)
                                    .decorate(TextDecoration.UNDERLINED)
                                    .clickEvent(ClickEvent.runCommand("/g sp"))
                                    .clickEvent(ClickEvent.runCommand("/tp " + player.getName()))
                                    .hoverEvent(HoverEvent.showText(
                                        Component.text(String.format("スぺテクターで%sにテレポート", player.getName()))
                                    ))
                                    .build()
                            ),
                            Component.text(
                                "] ",
                                NamedTextColor.GRAY
                            ),
                            Component.text(command, NamedTextColor.YELLOW),
                            Component.text((event.isCancelled() ? " (取り消し済み)" : ""), NamedTextColor.RED)
                        )
                );
            }
        }
    }
}