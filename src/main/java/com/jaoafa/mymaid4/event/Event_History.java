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
import com.jaoafa.mymaid4.lib.Historyjao;
import com.jaoafa.mymaid4.lib.MyMaidData;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.commons.lang.time.DateUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Event_History extends MyMaidLibrary implements Listener, EventPremise {
    @Override
    public String description() {
        return "historyコマンド(jaoHistory)に関する処理を行います。";
    }

    @EventHandler
    public void OnEvent_JoinHistory(PlayerJoinEvent event) {
        if (!MyMaidData.isMainDBActive()) {
            return;
        }

        Player player = event.getPlayer();
        Historyjao histjao = Historyjao.getHistoryjao(player);

        if (!histjao.isFound()) {
            return;
        }

        if (histjao.getDataList().isEmpty()) {
            return;
        }

        List<String> data = new ArrayList<>();
        for (Historyjao.Data hist : histjao.getDataList()) {
            if (!hist.notify) continue;
            data.add(MessageFormat.format("[{0}] {1} - {2}", hist.id, hist.message, sdfFormat(hist.getCreatedAt())));
        }

        if (data.isEmpty()) {
            return;
        }

        Date whenNotified = histjao.getWhenNotified();
        if (whenNotified != null && DateUtils.isSameDay(new Date(), whenNotified)) {
            return;
        }

        TextChannel jaotan = MyMaidData.getJaotanChannel();
        if (jaotan == null) return;
        jaotan.sendMessage(MessageFormat.format("**-----: Historyjao DATA / `{0}` :-----**\n```{1}```",
            player.getName(),
            String.join("\n", data))).queue();
        histjao.setNotified();
    }
}