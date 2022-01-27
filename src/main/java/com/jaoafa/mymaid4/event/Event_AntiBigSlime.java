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

import com.jaoafa.mymaid4.lib.EventPremise;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class Event_AntiBigSlime extends MyMaidLibrary implements Listener, EventPremise {
    @Override
    public String description() {
        return "極端に大きいスライム・マグマキューブを召喚できないようにします。";
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!(event.getEntity() instanceof Slime slime)) {
            return;
        }
        if (slime.getSize() <= 30) {
            return;
        }
        event.setCancelled(true);
        Location location = event.getLocation();
        Player nearPlayer = getNearestPlayer(location);
        event.setCancelled(true);
        if (nearPlayer == null) {
            return;
        }
        nearPlayer.sendMessage(Component.text().append(
            Component.text("[AntiBigSlime]"),
            Component.space(),
            Component.text("負荷対策の為に極端に大きいスライム・マグマキューブの召喚を禁止しています。ご協力をお願いします。", NamedTextColor.GREEN)
        ));
        sendAM(Component.text().append(
            Component.text("["),
            Component.text("AntiBigSlime", NamedTextColor.RED),
            Component.text("]"),
            Component.space(),
            Component.text(nearPlayer.getName() + "の近くで極端に大きいスライム・マグマキューブが発生しましたが、発生を規制されました。", NamedTextColor.GREEN)
        ).build());
    }
}
