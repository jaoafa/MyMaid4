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

public class Event_AntiKillWire extends MyMaidLibrary implements Listener, EventPremise {
    @Override
    public String description() {
        return "wireコマンドで出現させたコウモリのキルを無効化します。";
    }

    @EventHandler
    public void EntityDamageByEntity(EntityDeathEvent event) {
        // キルされたMOBのエンティティを取得する
        Entity ent = event.getEntity();

        if (ent.getType() != EntityType.BAT) return; // コウモリではない
        if (event.getEntity().getKiller() == null) return; // エンティティの殺害者がいない = プレイヤーやエンティティがキルしたわけではない = コマンド等でキルされた場合
        if (!ent.getScoreboardTags().contains("CmdWire1") && !ent.getScoreboardTags().contains("CmdWire2"))
            return; // CmdWire1とCmdWire2、どちらのタグもついてない

        event.setCancelled(true);
        if (event.getEntity().getKiller() != null) {
            event.getEntity().getKiller().sendMessage("[AntiKillWire] " + ChatColor.GREEN + "これは特別天然記念物のワイヤーコウモリです。キルする場合はwireコマンドのdelまたはdelweを使用してください。");
        }
    }
}
