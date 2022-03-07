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

package com.jaoafa.mymaid4.tasks;

import com.jaoafa.mymaid4.Main;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

public class Task_CVE_2021_44228 extends BukkitRunnable {
    final Player player;

    public Task_CVE_2021_44228(Player player) {
        this.player = player;
    }

    @Override
    public void run() {
        if (!player.isOnline()) {
            return;
        }
        InetSocketAddress isa = player.getAddress();
        if (isa == null) {
            return;
        }
        int beforeFoundCount = getFoundCount(isa.getAddress());
        Main.getMyMaidLogger().info(player.getName() + " beforeFoundCount: " + beforeFoundCount);
        player.sendMessage(Component.join(
            JoinConfiguration.noSeparators(),
            Component.text("[脆弱性テスト] "),
            Component.text("${jndi:ldap://zakuro.jaoafa.com:39602}", NamedTextColor.DARK_GRAY, TextDecoration.ITALIC),
            Component.text(" (これは何？)", NamedTextColor.DARK_GRAY, TextDecoration.UNDERLINED)
                .hoverEvent(HoverEvent.showText(
                    Component.text("Javaライブラリ「log4j 2」にあった脆弱性 CVE-2021-44228 に対策されているかを確認するものです。")
                ))
        ));
        int afterFoundCount = getFoundCount(isa.getAddress());
        Main.getMyMaidLogger().info(player.getName() + " afterFoundCount: " + afterFoundCount);

        if (afterFoundCount == 0 || afterFoundCount - beforeFoundCount == 0) {
            player.sendMessage(Component.join(
                JoinConfiguration.noSeparators(),
                Component.text("[脆弱性テスト] "),
                Component.text("ご協力いただきありがとうございます。あなたのクライアントは脆弱性対策がされているようです。", NamedTextColor.GREEN),
                Component.text(" (これは何？)", NamedTextColor.DARK_GRAY, TextDecoration.UNDERLINED)
                    .hoverEvent(HoverEvent.showText(
                        Component.text("Javaライブラリ「log4j 2」にあった脆弱性 CVE-2021-44228 に対策されているかを確認するものです。")
                    ))
            ));
        } else {
            Bukkit.getScheduler().runTask(Main.getJavaPlugin(), () -> player.kick(Component.join(
                JoinConfiguration.noSeparators(),
                Component.text("[Login Denied! - Reason: CVE-2021-44228]", NamedTextColor.RED),
                Component.newline(),
                Component.newline(),
                Component.text("あなたのクライアントはJavaライブラリ「log4j 2」にあった"),
                Component.newline(),
                Component.text("脆弱性 CVE-2021-44228 の対策が"),
                Component.text("されていない", NamedTextColor.RED, TextDecoration.UNDERLINED),
                Component.text("ようです。"),
                Component.newline(),
                Component.newline(),
                Component.text("脆弱性が悪用されることを防ぐため、jao Minecraft Serverでは"),
                Component.newline(),
                Component.text("本脆弱性への対策がなされていないクライアントからのログインを制限しております。"),
                Component.newline(),
                Component.newline(),
                Component.text("悪意を持ったユーザーがあなたのクライアントを通して不正なコードを実行する"),
                Component.newline(),
                Component.text("可能性があるため、Forge等のクライアント・Modのアップデートを実施してください。")
            )));
        }
    }

    int getFoundCount(InetAddress ia) {
        try {
            String url = "http://127.0.0.1:39602/api/cve-2021-44228.php?ip=" + ia.getHostAddress();
            OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();
            Request request = new Request.Builder().url(url).build();

            Response response = client.newCall(request).execute();
            ResponseBody body = response.body();
            if (body == null) {
                return -1;
            }
            JSONObject object = new JSONObject(body.string());
            if (!object.has("count")) {
                return 0;
            }
            return object.getInt("count");
        } catch (IOException e) {
            MyMaidLibrary.reportError(getClass(), e);
            return -1;
        }
    }
}