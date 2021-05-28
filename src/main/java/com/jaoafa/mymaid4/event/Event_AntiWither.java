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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class Event_AntiWither extends MyMaidLibrary implements Listener, EventPremise {
    @Override
    public String description() {
        return "ウィザーの出現制限を行います。";
    }

    @EventHandler
    public void CreatureSpawn(CreatureSpawnEvent event) {
        // 召喚されたMOBのエンティティを取得する
        LivingEntity ent = event.getEntity();
        // ウィザーの召喚操作によるものか？
        if (ent.getType() != EntityType.WITHER ||
            event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.BUILD_WITHER) {
            return;
        }
        Location location = event.getLocation();
        Player nearPlayer = getNearestPlayer(location);
        event.setCancelled(true);
        if (nearPlayer == null) {
            return;
        }
        nearPlayer.sendMessage(Component.text().append(
            Component.text("[AntiWither]"),
            Component.space(),
            Component.text("負荷対策の為にウィザーの召喚を禁止しています。ご協力をお願いします。", NamedTextColor.GREEN)
        ));
        sendAM(Component.text().append(
            Component.text("["),
            Component.text("AntiWither", NamedTextColor.RED),
            Component.text("]"),
            Component.space(),
            Component.text(nearPlayer.getName() + "の近くでウィザーが発生しましたが、発生を規制されました。", NamedTextColor.GREEN)
        ).build());
    }
}
