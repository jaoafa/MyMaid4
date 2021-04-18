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

import com.jaoafa.mymaid4.lib.EventPremise;
import com.jaoafa.mymaid4.lib.MyMaidData;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSprintEvent;

public class Event_FlySpeed extends MyMaidLibrary implements Listener, EventPremise {
    @Override
    public String description() {
        return "flyspeedコマンドに関する処理を行います。";
    }

    @EventHandler
    public void onSprint(PlayerToggleSprintEvent event) {
        Player player = event.getPlayer();
        if (event.isSprinting()) {
            // ダッシュ
            if (!MyMaidData.isSetFlySpeed(player.getUniqueId())) {
                return;
            }
            player.setFlySpeed(MyMaidData.getFlySpeed(player.getUniqueId()));
        } else {
            player.setFlySpeed(0.1f);
        }
    }
}
