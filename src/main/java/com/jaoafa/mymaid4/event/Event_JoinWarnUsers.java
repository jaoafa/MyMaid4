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

import com.jaoafa.mymaid4.Main;
import com.jaoafa.mymaid4.lib.EventPremise;
import com.jaoafa.mymaid4.lib.MyMaidData;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.JSONObject;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.UUID;

public class Event_JoinWarnUsers extends MyMaidLibrary implements Listener, EventPremise {
    @Override
    public String description() {
        return "ログイン時、注意する必要のあるプレイヤーが参加したときに警告します。";
    }

    @EventHandler(priority = EventPriority.MONITOR,
                  ignoreCancelled = true)
    public void OnJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        new BukkitRunnable() {
            public void run() {
                File file = new File(Main.getJavaPlugin().getDataFolder(), "warning_users.json");
                if (!file.exists()) {
                    return;
                }
                try {
                    JSONObject object = new JSONObject(Files.readString(file.toPath()));
                    if (!object.has(uuid.toString())) {
                        return;
                    }
                    TextChannel channel = MyMaidData.getJaotanChannel();
                    if (channel == null) {
                        return;
                    }
                    EmbedBuilder builder = new EmbedBuilder()
                        .setColor(Color.RED)
                        .setTitle(player.getName() + " は警告対象者です")
                        .setAuthor(player.getName(), "https://users.jaoafa.com/" + uuid, "https://crafatar.com/avatars/" + uuid)
                        .setDescription("理由: " + object.getString(uuid.toString()))
                        .setTimestamp(Instant.now())
                        .setFooter("MyMaid4 WarnUsers");
                    channel.sendMessageEmbeds(builder.build()).queue();
                } catch (IOException e) {
                    MyMaidLibrary.reportError(getClass(), e);
                }
            }
        }.runTaskAsynchronously(Main.getJavaPlugin());
    }
}
