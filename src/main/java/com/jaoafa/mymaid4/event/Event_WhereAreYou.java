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

import com.jaoafa.mymaid4.Main;
import com.jaoafa.mymaid4.lib.EventPremise;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class Event_WhereAreYou extends MyMaidLibrary implements Listener, EventPremise {
    List<List<String>> whereAreYou = List.of(
        List.of(
            "みんな",
            "どこ"
        ),
        List.of(
            "mina",
            "doko"
        ),
        List.of(
            "minna",
            "doko"
        ),
        List.of(
            "minnna",
            "doko"
        )
    );

    @Override
    public String description() {
        return "「みんなどこ」に自動返信します。";
    }

    @EventHandler
    public void onChatWhereAreYou(AsyncChatEvent event) {
        Player player = event.getPlayer();
        Component component = event.message();
        String message = PlainTextComponentSerializer.plainText().serialize(component);

        // 各項目のいずれか、そのうちのすべてに一致する場合
        if (whereAreYou.stream().noneMatch(list -> list.stream().allMatch(message::contains))) {
            return;
        }
        new BukkitRunnable() {
            public void run() {
                chatFake(NamedTextColor.YELLOW, "jaotan", Component.join(JoinConfiguration.noSeparators(),
                    Component.text(player.getName() + "さん、ほかの人の場所を知るには、Dynmapというマップを使うのがおすすめです！"),
                    Component.text("https://map.jaoafa.com/", NamedTextColor.AQUA, TextDecoration.UNDERLINED)
                        .hoverEvent(HoverEvent.showText(Component.text("クリックすると「map.jaoafa.com」をブラウザで開きます。")))
                        .clickEvent(ClickEvent.openUrl("https://map.jaoafa.com/"))), true);
            }
        }.runTaskLater(Main.getJavaPlugin(), 20L);
    }
}
