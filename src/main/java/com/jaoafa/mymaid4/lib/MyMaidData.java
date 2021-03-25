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

package com.jaoafa.mymaid4.lib;

import net.dv8tion.jda.api.entities.TextChannel;
import org.bukkit.Location;

import javax.annotation.Nullable;
import java.util.*;

/**
 * 複数のクラスを跨いで使用されるリストなどの変数をまとめるクラス
 */
public class MyMaidData {
    private static TextChannel reportChannel = null;
    private static TextChannel serverChatChannel = null;
    private static TextChannel jaotanChannel = null;
    private static TextChannel generalChannel = null;
    private static final Map<String, Location> lastDed = new HashMap<>();
    private static MySQLDBManager mainMySQLDBManager = null;
    private static MySQLDBManager zkrhatMySQLDBManager = null;
    private static final Set<UUID> hid = new HashSet<>();

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

    public static Map<String, Location> getLastDed() {
        return lastDed;
    }

    public static void setLastDed(String name, Location loc) {
        lastDed.put(name, loc);
    }

    public static boolean isMainDBActive(){
        return mainMySQLDBManager != null;
    }

    public static MySQLDBManager getMainMySQLDBManager() {
        return mainMySQLDBManager;
    }

    public static void setMainMySQLDBManager(MySQLDBManager mainMySQLDBManager) {
        MyMaidData.mainMySQLDBManager = mainMySQLDBManager;
    }

    public static boolean isZKRHatDBActive(){
        return zkrhatMySQLDBManager != null;
    }

    public static MySQLDBManager getZKRHatMySQLDBManager() {
        return zkrhatMySQLDBManager;
    }

    public static void setZKRHatMySQLDBManager(MySQLDBManager zkrhatMySQLDBManager) {
        MyMaidData.zkrhatMySQLDBManager = zkrhatMySQLDBManager;
    }

    public static boolean isHid(UUID uuid) {
        return hid.contains(uuid);
    }

    public static void addHid(UUID uuid) {
        hid.add(uuid);
    }

    public static void removeHid(UUID uuid) {
        hid.remove(uuid);
    }
}
