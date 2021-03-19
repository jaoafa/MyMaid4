package com.jaoafa.mymaid4.discordEvent;

import com.jaoafa.mymaid4.Main;
import com.jaoafa.mymaid4.lib.MyMaidData;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;

public class DiscordEvent_Ready {
    @SubscribeEvent
    public void onReadyEvent(ReadyEvent event) {
        System.out.println("Ready: " + event.getJDA().getSelfUser().getName());

        MyMaidData.setReportChannel(event.getJDA().getTextChannelById(Main.getMyMaidConfig().getReportChannelId()));
        MyMaidData.setJaotanChannel(event.getJDA().getTextChannelById(Main.getMyMaidConfig().getJaotanChannelId()));
        MyMaidData.setGeneralChannel(event.getJDA().getTextChannelById(Main.getMyMaidConfig().getGeneralChannelId()));
        MyMaidData.setServerChatChannel(event.getJDA().getTextChannelById(Main.getMyMaidConfig().getServerChatChannelId()));
    }
}
