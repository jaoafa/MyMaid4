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

package com.jaoafa.mymaid4.discordEvent;

import com.jaoafa.jaosuperachievement2.api.Achievementjao;
import com.jaoafa.jaosuperachievement2.lib.Achievement;
import com.jaoafa.mymaid4.lib.MyMaidData;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class DiscordEvent_ListCmd extends ListenerAdapter {
    @SubscribeEvent
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!event.isFromType(ChannelType.TEXT)) {
            return;
        }
        if (event.isWebhookMessage()) {
            return;
        }
        Guild guild = event.getGuild();
        if (guild.getIdLong() != 597378876556967936L) {
            return;
        }
        MessageChannel channel = event.getChannel();
        if (MyMaidData.getServerChatChannel() == null) {
            return;
        }
        if (channel.getIdLong() != MyMaidData.getServerChatChannel().getIdLong()) {
            return;
        }
        Member member = event.getMember();
        if (member == null) {
            return;
        }
        Message message = event.getMessage();
        String text = message.getContentRaw();

        if (!text.equalsIgnoreCase("/list")) {
            return;
        }

        try {
            Connection conn = MyMaidData.getMainMySQLDBManager().getConnection();
            try (PreparedStatement statement = conn
                .prepareStatement("SELECT * FROM discordlink WHERE disid = ? AND disabled = ?")) {
                statement.setString(1, member.getId());
                statement.setBoolean(2, false);
                try (ResultSet res = statement.executeQuery()) {
                    if (!res.next()) {
                        return;
                    }

                    String uuid = res.getString("uuid");
                    OfflinePlayer offplayer = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
                    Achievementjao.getAchievementAsync(offplayer, Achievement.ISTHEREANYONE); // 誰かいるかな？ 鯖茶から/listコマンドを実行する
                }
            }
        } catch (SQLException e) {
            MyMaidLibrary.reportError(getClass(), e);
        }
    }
}
