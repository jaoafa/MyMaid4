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
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class Event_AntiKillWireUnit extends MyMaidLibrary implements Listener, EventPremise {
    @Override
    public String description() {
        return "wireコマンドで出現させたコウモリのキルを無効化します。";
    }

    @EventHandler
    public void EntityDamageByEntity(EntityDeathEvent event) {
        // キルされたMOBのエンティティを取得する
        Entity ent = event.getEntity();

        if (ent.getType() != EntityType.BAT || !(ent.getScoreboardTags().contains("wireUnit")) || event.getEntity().getKiller() == null) {
            return;
        }
        event.setCancelled(true);
        if (event.getEntity().getKiller() != null) {
            event.getEntity().getKiller().sendMessage("[AntiKillWireUnit] " + ChatColor.GREEN + "これは特別天然記念物のワイヤーユニットコウモリです。特殊な操作をしなければ殺すことはできません。ご協力をお願いします。" +
                "ちなみに特別な操作っていうのは、今後実装する予定の/wire delweみたいなコマンドのことを言いたいんだけど、今その機能ないから適当にkillコマンドでkillしてね。delwe実装したらkillコマンドもそのうちできなくなります！ｗ");
        }
    }
}
