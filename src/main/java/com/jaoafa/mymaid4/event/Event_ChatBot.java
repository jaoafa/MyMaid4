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
import com.jaoafa.mymaid4.lib.MeboChatBot;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

public class Event_ChatBot extends MyMaidLibrary implements Listener, EventPremise {
    @Override
    public String description() {
        return "ChatBotに関する処理を行います。";
    }

    @EventHandler
    public void onAsyncPlayerChatEvent(AsyncChatEvent event) {
        Player player = event.getPlayer();
        String content = PlainTextComponentSerializer.plainText().serialize(event.message());

        if (content.length() < 5) {
            return;
        }

        new BukkitRunnable() {
            public void run() {
                MeboChatBot chatBot = Main.getMeboChatBot();
                if (chatBot == null) {
                    return;
                }
                MeboChatBot.MeboResponse response = chatBot.chat(player, content);
                if (response == null) {
                    return;
                }
                if (!response.status()) {
                    return;
                }
                if (response.score() < 85) {
                    return;
                }

                MyMaidLibrary.chatFake(NamedTextColor.GOLD, "jaotan", response.message(), true);
            }
        }.runTaskLaterAsynchronously(Main.getMain(), 10L);
    }
}