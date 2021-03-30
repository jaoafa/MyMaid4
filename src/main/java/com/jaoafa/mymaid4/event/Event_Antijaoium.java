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

import com.jaoafa.jaosuperachievement2.api.Achievementjao;
import com.jaoafa.jaosuperachievement2.lib.Achievement;
import com.jaoafa.mymaid4.lib.EBan;
import com.jaoafa.mymaid4.lib.Jail;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class Event_Antijaoium extends MyMaidLibrary implements Listener {
    List<Integer> heal = Arrays.asList(
        -3,
        29,
        125,
        253
    );
    List<Integer> health_boost = Collections.singletonList(
        -7
    );

    /**
     * jaoiumと判定されるアイテムかどうか
     *
     * @param list PotionEffectのList
     * @return jaoiumかどうか
     * @author mine_book000
     */
    private boolean isjaoium(List<PotionEffect> list) {
        boolean jaoium = false;
        for (PotionEffect po : list) {
            if (po.getType().equals(PotionEffectType.HEAL)) {
                if (heal.contains(po.getAmplifier())) {
                    // アウト
                    jaoium = true;
                }
            }
            if (po.getType().equals(PotionEffectType.HEALTH_BOOST)) {
                if (health_boost.contains(po.getAmplifier())) {
                    // アウト
                    jaoium = true;
                }
            }
        }
        return jaoium;
    }

    /**
     * 悪意のあるアイテムかどうか
     *
     * @param potion PotionMeta
     * @return 悪意のあるアイテムかどうか
     */
    private String isMalicious(PotionMeta potion) {
        Component component = potion.displayName();
        if (component == null) {
            return null;
        }
        String displayName = PlainComponentSerializer.plain().serialize(component);
        if (displayName.contains("§4§lDEATH")) {
            // Wurst?
            return "Wurst";
        }
        return null;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void OnPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity();
        Item item = event.getItem();
        ItemStack hand = item.getItemStack();
        if (hand.getType() != Material.SPLASH_POTION && hand.getType() != Material.LINGERING_POTION) {
            return;
        }
        PotionMeta potion = (PotionMeta) hand.getItemMeta();
        if (!isjaoium(potion.getCustomEffects())) {
            return;
        }
        player.sendMessage("[jaoium_Checker] " + ChatColor.GREEN
            + "あなたはjaoiumを拾いました。何か行動をする前に/clearをしないと、自動的に投獄されてしまうかもしれません！");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void InvClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        Inventory inv = event.getInventory();
        ItemStack[] is = inv.getContents();
        Inventory click_inv = event.getClickedInventory();
        ItemStack[] click_is = click_inv != null ? click_inv.getContents() : new ItemStack[]{};

        boolean isMatched = false;
        String malicious = null;

        Optional<ItemStack> matched = Arrays.stream(is)
            .filter(Objects::nonNull)
            .filter(i -> i.getType() == Material.SPLASH_POTION || i.getType() == Material.LINGERING_POTION)
            .filter(i -> isjaoium(((PotionMeta) i.getItemMeta()).getCustomEffects()))
            .findFirst();
        if(matched.isPresent()){
           // jaoium有
            setjaoiumItemData(player, matched.get());
            inv.clear();
            isMatched = true;
            malicious = isMalicious((PotionMeta) matched.get().getItemMeta());
        }

        Optional<ItemStack> click_matched = Arrays.stream(click_is)
            .filter(Objects::nonNull)
            .filter(i -> i.getType() == Material.SPLASH_POTION || i.getType() == Material.LINGERING_POTION)
            .filter(i -> isjaoium(((PotionMeta) i.getItemMeta()).getCustomEffects()))
            .findFirst();
        if(click_matched.isPresent()){
            // jaoium有
            setjaoiumItemData(player, click_matched.get());
            click_inv.clear();
            isMatched = true;
            malicious = isMalicious((PotionMeta) click_matched.get().getItemMeta());
        }

        if(!isMatched){
            return;
        }

        Jail jail = new Jail(player);
        if (jail.isBanned()) {
            return;
        }
        EBan eban = new EBan(player);
        if (eban.isBanned()) {
            return;
        }

        Achievementjao.getAchievementAsync(player, Achievement.DRUGADDICTION);
        player.getInventory().clear();
        if(malicious != null){
            eban.addBan("jaotan", String.format("禁止クライアントMod「%s」使用の疑い。方針「クライアントModの導入・利用に関する規則」の「禁止事項」への違反", malicious));
        }else{
            jail.addBan("jaotan", "jaoium所持");
        }
    }

    private void setjaoiumItemData(Player player, ItemStack is) {


    }
}
