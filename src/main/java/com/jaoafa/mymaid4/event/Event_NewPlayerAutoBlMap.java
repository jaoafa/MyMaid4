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
import net.dv8tion.jda.api.entities.TextChannel;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Event_NewPlayerAutoBlMap extends MyMaidLibrary implements Listener, EventPremise {
    @Override
    public String description() {
        return "新規プレイヤーがログアウトした際にブロック編集情報を通知します。";
    }

    final Set<UUID> firstLoginer = new HashSet<>();

    @EventHandler
    public void OnEvent_FirstLogin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.hasPlayedBefore()) {
            firstLoginer.remove(player.getUniqueId());
            return;
        }
        System.out.println("NewPlayerAutoBlMap: 初ログインユーザーがログイン");
        firstLoginer.add(player.getUniqueId());
    }

    @EventHandler
    public void OnEvent_Quit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (!firstLoginer.contains(player.getUniqueId())) {
            return;
        }
        System.out.println("NewPlayerAutoBlMap: 初ログインユーザーがログアウト");
        new BukkitRunnable() {
            public void run() {
                firstLoginer.remove(player.getUniqueId());
                String url = "https://api.jaoafa.com/cities/getblockimg?uuid=" + player.getUniqueId();

                TextChannel channel = MyMaidData.getJaotanChannel();
                if (channel == null) {
                    return;
                }
                try {
                    OkHttpClient client = new OkHttpClient().newBuilder()
                        .connectTimeout(60, TimeUnit.SECONDS)
                        .readTimeout(60, TimeUnit.SECONDS)
                        .build();
                    Request request = new Request.Builder().url(url).build();

                    Response response = client.newCall(request).execute();
                    if (response.code() != 200 && response.code() != 302) {
                        System.out.printf("NewPlayerAutoBlMap: APIサーバへの接続に失敗: %d %s\nhttps://jaoafa.com/cp/?uuid=%s%n", response.code(), Objects.requireNonNull(response.body()).string(), player.getUniqueId());
                        response.close();
                        return;
                    }
                    if (response.body() == null) {
                        System.out.printf("NewPlayerAutoBlMap: APIサーバへの接続に失敗: response.body() is null.\nhttps://jaoafa.com/cp/?uuid=%s%n", player.getUniqueId());
                        response.close();
                        return;
                    }
                    System.out.println("NewPlayerAutoBlMap: ブロック編集マップ取得完了");

                    channel.sendFile(response.body().byteStream(), player.getUniqueId() + ".png")
                        .append(String.format("新規プレイヤー「%s」のブロック編集マップ\nhttps://jaoafa.com/cp/?uuid=%s", player.getName(), player.getUniqueId())).queue(msg -> {
                        System.out.println("NewPlayerAutoBlMap: メッセージ送信完了 (" + msg.getJumpUrl() + ")");
                        response.close();
                    }, failure -> {
                        System.out.println("NewPlayerAutoBlMap: メッセージ送信失敗 (" + failure.getMessage() + ")");
                        failure.printStackTrace();
                        response.close();
                    });
                } catch (IOException ex) {
                    System.out.println("NewPlayerAutoBlMap: APIサーバへの接続に失敗: " + ex.getMessage());
                }
            }
        }.runTaskAsynchronously(Main.getJavaPlugin());
    }
}
