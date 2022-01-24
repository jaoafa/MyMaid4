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

import com.jaoafa.mymaid4.Main;
import com.jaoafa.mymaid4.lib.MyMaidData;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.hooks.SubscribeEvent;

public class DiscordEvent_Ready extends ListenerAdapter {
    @SubscribeEvent
    public void onReady(ReadyEvent event) {
        Main.getMyMaidLogger().info("Ready: " + event.getJDA().getSelfUser().getName());

        MyMaidData.setReportChannel(event.getJDA().getTextChannelById(Main.getMyMaidConfig().getReportChannelId()));
        MyMaidData.setJaotanChannel(event.getJDA().getTextChannelById(Main.getMyMaidConfig().getJaotanChannelId()));
        MyMaidData.setGeneralChannel(event.getJDA().getTextChannelById(Main.getMyMaidConfig().getGeneralChannelId()));
        MyMaidData.setServerChatChannel(event.getJDA().getTextChannelById(Main.getMyMaidConfig().getServerChatChannelId()));
    }
}
