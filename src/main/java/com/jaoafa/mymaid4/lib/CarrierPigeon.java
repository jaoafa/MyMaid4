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

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * 伝書鳩ちゃん！
 */
public class CarrierPigeon {
    final File file;
    List<String> Messages = new LinkedList<>();
    List<String> Speakers = new LinkedList<>();

    public CarrierPigeon(@NotNull File file) {
        this.file = file;

        loadSettings();
    }

    public static void speakBird(CommandSender sender, String message) {
        speakBird(sender, MyMaidLibrary.replaceComponentURL(Component.text(message, NamedTextColor.GRAY)));
    }

    public static void speakBird(CommandSender sender, Component component) {
        CarrierPigeon carrierPigeon = MyMaidData.getCarrierPigeon();
        String speaker = carrierPigeon.getRandomSpeaker();
        sender.sendMessage(Component.text().append(
            Component.text("[" + MyMaidLibrary.sdfTimeFormat(new Date()) + "]", NamedTextColor.GRAY),
            Component.text("■", getRandomColor()),
            Component.text(speaker, NamedTextColor.WHITE),
            Component.text(" < ", NamedTextColor.WHITE),
            Component.space(),
            component
        ));
    }

    private static NamedTextColor getRandomColor() {
        List<NamedTextColor> colors = Arrays.asList(NamedTextColor.BLACK,
            NamedTextColor.DARK_BLUE,
            NamedTextColor.DARK_GREEN,
            NamedTextColor.DARK_AQUA,
            NamedTextColor.DARK_RED,
            NamedTextColor.DARK_PURPLE,
            NamedTextColor.GOLD,
            NamedTextColor.GRAY,
            NamedTextColor.DARK_GRAY,
            NamedTextColor.BLUE,
            NamedTextColor.GREEN,
            NamedTextColor.AQUA,
            NamedTextColor.RED,
            NamedTextColor.LIGHT_PURPLE,
            NamedTextColor.YELLOW,
            NamedTextColor.WHITE);
        Collections.shuffle(colors);
        return colors.get(0);
    }

    /**
     * スピーカーを追加
     *
     * @param speaker 追加するスピーカー
     */
    public void addSpeaker(String speaker) throws IOException {
        Speakers.add(speaker);
        saveSettings();
    }

    /**
     * スピーカーが存在するか調べる
     *
     * @param index 調べるスピーカーの番号
     *
     * @return 存在したかどうか
     */
    public boolean existsSpeaker(int index) {
        return index >= 0 && index < Speakers.size();
    }

    /**
     * スピーカーが存在するか調べる
     *
     * @param str 調べるスピーカーテキスト
     *
     * @return 存在したかどうか
     */
    public boolean existsSpeaker(String str) {
        return Speakers.contains(str);
    }

    /**
     * スピーカーを取得
     *
     * @param index 取得するスピーカーの番号
     *
     * @return 取得したスピーカー
     */
    public String getSpeaker(int index) {
        return Speakers.get(index);
    }

    /**
     * スピーカーIdを取得
     *
     * @param str 取得するスピーカー
     *
     * @return 取得したスピーカーId
     */
    public int getSpeakerId(String str) {
        return Speakers.indexOf(str);
    }

    /**
     * スピーカーを削除
     *
     * @param index 削除するスピーカーの番号
     *
     * @return 削除できたかどうか
     */
    public void removeSpeaker(int index) throws IOException {
        if (!existsSpeaker(index)) {
            return;
        }
        Speakers.remove(index);
        saveSettings();
    }

    /**
     * メッセージを追加
     *
     * @param message 追加するメッセージ
     */
    public void addMessage(String message) throws IOException {
        Messages.add(message);
        saveSettings();
    }

    /**
     * メッセージが存在するか調べる
     *
     * @param index 調べるメッセージの番号
     *
     * @return 存在したかどうか
     */
    public boolean existsMessage(int index) {
        return index >= 0 && index < Messages.size();
    }

    /**
     * メッセージが存在するか調べる
     *
     * @param str 調べるメッセージテキスト
     *
     * @return 存在したかどうか
     */
    public boolean existsMessage(String str) {
        return Messages.contains(str);
    }

    /**
     * メッセージを取得
     *
     * @param index 取得するメッセージの番号
     *
     * @return 取得したメッセージ
     */
    public String getMessage(int index) {
        return Messages.get(index);
    }

    /**
     * メッセージIdを取得
     *
     * @param str 取得するメッセージ
     *
     * @return 取得したメッセージId
     */
    public int getMessageId(String str) {
        return Messages.indexOf(str);
    }

    /**
     * メッセージを削除
     *
     * @param index 削除するメッセージの番号
     *
     * @return 削除できたかどうか
     *
     * @author mine_book000
     */
    public void removeMessage(int index) throws IOException {
        if (!existsMessage(index)) {
            return;
        }
        Messages.remove(index);
        saveSettings();
    }

    /**
     * メッセージをランダム配信する
     */
    public void randomBroadcast() {
        if (Messages.isEmpty()) {
            return;
        }

        String message = getRandomMessage();
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            if (MyMaidData.getTempMuting().contains(p)) continue;
            String msg = message.replaceAll("%player%", p.getName());
            speakBird(p, msg);
        }
    }

    /**
     * 指定されたメッセージを配信する
     *
     * @param index 配信するメッセージ
     */
    public void broadcast(int index) {
        if (Messages.isEmpty()) {
            return;
        }
        String message = Messages.get(index);

        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            if (MyMaidData.getTempMuting().contains(p)) continue;
            String msg = message.replaceAll("%player%", p.getName());
            speakBird(p, msg);
        }
    }

    /**
     * スピーカーをランダムに取得する
     *
     * @return メッセージ
     */
    public String getRandomSpeaker() {
        if (Speakers.isEmpty()) {
            return "/^v^\\";
        }
        List<String> tempSpeakers = new ArrayList<>(Speakers);
        Collections.shuffle(tempSpeakers);
        return tempSpeakers.get(0);
    }

    /**
     * メッセージをランダムに取得する
     *
     * @return メッセージ
     */
    public String getRandomMessage() {
        if (Messages.isEmpty()) {
            return null;
        }
        List<String> tempMessages = new ArrayList<>(Messages);
        Collections.shuffle(tempMessages);
        return tempMessages.get(0);
    }

    /**
     * スピーカーリストを取得
     *
     * @return スピーカーリスト
     */
    public List<String> getSpeakers() {
        return Speakers;
    }

    /**
     * メッセージリストを取得
     *
     * @return メッセージリスト
     */
    public List<String> getMessages() {
        return Messages;
    }

    /**
     * メッセージとスピーカーの設定をロードします。
     */
    public void loadSettings() {
        Messages = new LinkedList<>();
        Speakers = new LinkedList<>();
        if (!file.exists()) {
            return;
        }
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
        Messages = yml.getStringList("messages");
        Speakers = yml.getStringList("speakers");
    }

    /**
     * メッセージとスピーカーの設定をセーブします。
     */
    public void saveSettings() throws IOException {
        YamlConfiguration yml = new YamlConfiguration();
        yml.set("messages", Messages);
        yml.set("speakers", Speakers);
        yml.save(file);
    }
}
