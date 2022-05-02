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
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.List;

public class Event_AntiPotion extends MyMaidLibrary implements Listener, EventPremise {
    @Override
    public String description() {
        return "ポーション系の規制を行います。";
    }

    /*
    Default: 全ポーションの利用制限
    Verified: 透明化ポーションの利用制限
    */

    /**
     * ポーションチェック
     *
     * @param player プレイヤー
     * @param item   ポーションアイテム
     *
     * @return ポーションが利用不可能かどうか。Trueの場合はキャンセルする
     */
    boolean isDeny(Player player, ItemStack item) {
        if (isD(player)) {
            // 全ポーションの利用制限
            return true;
        }
        if (isV(player)) {
            // 透明化ポーションの利用制限
            PotionMeta meta = (PotionMeta) item.getItemMeta();
            List<PotionEffect> effects = meta.getCustomEffects();
            return meta.getBasePotionData().getType() == PotionType.INVISIBILITY ||
                effects.stream().anyMatch(effect -> effect.getType() == PotionEffectType.INVISIBILITY);
        }
        return false;
    }

    // ポーションを飲む
    @EventHandler
    public void PotionDrink(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item.getType() != Material.POTION
            && item.getType() != Material.SPLASH_POTION
            && item.getType() != Material.LINGERING_POTION) {
            return;
        }

        if (isDeny(player, item)) {
            player.sendMessage(Component.join(JoinConfiguration.noSeparators(),
                Component.text("[Potion]"),
                Component.space(),
                Component.text("ポーションの利用が制限されました。", NamedTextColor.GREEN)));
            event.setCancelled(true);
        }
    }

    // ポーションが割れる
    @EventHandler
    public void PotionSplash(PotionSplashEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getPotion().getShooter();
        if (player == null) {
            return;
        }
        ItemStack item = event.getPotion().getItem();
        if (item.getType() != Material.POTION
            && item.getType() != Material.SPLASH_POTION
            && item.getType() != Material.LINGERING_POTION) {
            return;
        }

        if (isDeny(player, item)) {
            player.sendMessage(Component.join(JoinConfiguration.noSeparators(),
                Component.text("[Potion]"),
                Component.space(),
                Component.text("ポーションの利用が制限されました。", NamedTextColor.GREEN)));
            event.setCancelled(true);
        }
    }
}
