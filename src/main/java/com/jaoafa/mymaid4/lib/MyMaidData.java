package com.jaoafa.mymaid4.lib;

import net.dv8tion.jda.api.entities.TextChannel;
import org.bukkit.Location;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * 複数のクラスを跨いで使用されるリストなどの変数をまとめるクラス
 */
public class MyMaidData {
    private static TextChannel reportChannel = null;
    private static TextChannel serverChatChannel = null;
    private static TextChannel jaotanChannel = null;
    private static TextChannel generalChannel = null;
    private static Map<String, Location> lastded = null;

    @Nullable
    public static TextChannel getReportChannel() {
        return reportChannel;
    }

    public static void setReportChannel(TextChannel _reportChannel) {
        reportChannel = _reportChannel;
    }

    @Nullable
    public static TextChannel getServerChatChannel() {
        return serverChatChannel;
    }

    public static void setServerChatChannel(TextChannel _serverChatChannel) {
        serverChatChannel = _serverChatChannel;
    }

    @Nullable
    public static TextChannel getJaotanChannel() {
        return jaotanChannel;
    }

    public static void setJaotanChannel(TextChannel _jaotanChannel) {
        jaotanChannel = _jaotanChannel;
    }

    @Nullable
    public static TextChannel getGeneralChannel() {
        return generalChannel;
    }

    public static void setGeneralChannel(TextChannel generalChannel) {
        MyMaidData.generalChannel = generalChannel;
    }

    @Nullable
    public static Map<String, Location> getLastded() {
        return lastded;
    }

    public static void setLastded(String name, Location loc) {
        lastded.put(name,loc);
    }
}
