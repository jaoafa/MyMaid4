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
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Event_AntiDisableInvisibilityWire extends MyMaidLibrary implements Listener, EventPremise {
    @Override
    public String description() {
        return "wireコマンドで出現させたコウモリの透明化エフェクトが切れた際、再度透明化エフェクトを付与します。";
    }

    @EventHandler
    public void EntityDisableInvisibility(EntityPotionEffectEvent event) {
        // エフェクトが発生したMOBのエンティティを取得する
        Entity ent = event.getEntity();

        if (ent.getType() != EntityType.BAT || !(ent.getScoreboardTags().contains("wireUnit")) ||
            event.getCause() != EntityPotionEffectEvent.Cause.valueOf("EXPIRATION") ||
            event.getModifiedType() != PotionEffectType.getByName("INVISIBILITY")) {
            return;
        }
        LivingEntity livent = (LivingEntity) ent;
        livent.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, true, false, true));
    }
}
