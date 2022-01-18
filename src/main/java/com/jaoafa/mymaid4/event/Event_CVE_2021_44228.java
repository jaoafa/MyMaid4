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
import com.jaoafa.mymaid4.lib.EventPremise;
import com.jaoafa.mymaid4.tasks.Task_CVE_2021_44228;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class Event_CVE_2021_44228 implements Listener, EventPremise {
    @Override
    public String description() {
        return "ログイン時にクライアントがJavaライブラリ「log4j 2」にあった脆弱性 CVE-2021-44228 に対策されているかを確認します。";
    }

    @EventHandler
    public void OnJoin(PlayerJoinEvent event) {
        if (Main.getMyMaidConfig().isDevelopmentServer()) {
            Main.getMyMaidLogger().warning("開発サーバのため、CVE-2021-44228チェックは動作しません。");
            return;
        }
        new Task_CVE_2021_44228(event.getPlayer()).runTaskAsynchronously(Main.getMain());
    }
}