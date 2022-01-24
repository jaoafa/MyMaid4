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
import com.jaoafa.mymaid4.lib.EBan;
import com.jaoafa.mymaid4.lib.EventPremise;
import com.jaoafa.mymaid4.lib.MyMaidData;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import net.dv8tion.jda.api.EmbedBuilder;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class Event_PacketLimiterKick extends MyMaidLibrary implements Listener, EventPremise {
    static final Map<UUID, Integer> limited = new HashMap<>();

    @Override
    public String description() {
        return "PacketLimiterによるキック時に通知を行います。";
    }

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        String reason = PlainTextComponentSerializer.plainText().serialize(event.reason());
        if (!reason.equalsIgnoreCase("You are sending too many packets!") &&
            !reason.equalsIgnoreCase("You are sending too many packets, :(")) {
            return;
        }

        Player player = event.getPlayer();
        Location loc = player.getLocation();
        String location = loc.getWorld().getName() + " " + loc.getBlockX() + " " + loc.getBlockY() + " "
            + loc.getBlockZ();

        EBan eban = EBan.getInstance(player);
        if (eban.isStatus()) {
            return;
        }

        if (limited.containsKey(player.getUniqueId()) && limited.get(player.getUniqueId()) >= 5) {
            // サーバ起動中に5回キック
            eban.addBan("jaotan", "PacketLimiterによって規定回数以上キックされたため。");
            return;
        }
        limited.put(player.getUniqueId(), limited.getOrDefault(player.getUniqueId(), 0) + 1);

        EmbedBuilder embed = new EmbedBuilder()
            .setTitle("警告！！")
            .appendDescription("プレイヤーがパケットを送信しすぎてKickされました。ハッククライアントの可能性があります。")
            .setAuthor(event.getPlayer().getName(),
                "https://users.jaoafa.com/" + event.getPlayer().getUniqueId(),
                "https://crafatar.com/avatars/" + event.getPlayer().getUniqueId())
            .setColor(Color.ORANGE)
            .addField("プレイヤー", "`" + event.getPlayer().getName() + "`", true)
            .addField("理由", "`" + reason + "`", false)
            .addField("座標", location, false);

        Random rand = new Random();
        boolean x_isMinus = rand.nextBoolean();
        int x = rand.nextInt(310) + 152; // 152 - 462
        x = x_isMinus ? -x : x;

        boolean z_isMinus = rand.nextBoolean();
        int z = rand.nextInt(310) + 152; // 152 - 462
        z = z_isMinus ? -z : z;

        Location teleportLoc = new Location(Bukkit.getWorld("Jao_Afa"), x, 70, z);
        event.getPlayer().teleport(teleportLoc);
        Main.getMyMaidLogger().info("[PacketLimiter_AutoTP] teleport to Jao_Afa " + x + " 70 " + z);

        if (MyMaidData.getJaotanChannel() == null) {
            return;
        }
        MyMaidData.getJaotanChannel().sendMessageEmbeds(embed.build()).queue();
    }
}
