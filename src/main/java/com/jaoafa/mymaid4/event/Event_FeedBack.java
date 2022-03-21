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
import com.jaoafa.mymaid4.lib.NMSManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.inventory.ItemStack;

public class Event_FeedBack extends MyMaidLibrary implements Listener, EventPremise {
    @Override
    public String description() {
        return "feedbackコマンドに関する処理を行います。";
    }

    @EventHandler(ignoreCancelled = true)
    public void onEditedBook(PlayerEditBookEvent event) {
        Player player = event.getPlayer();
        if (player.getInventory().getItemInMainHand().getType() != Material.WRITABLE_BOOK) {
            return;
        }
        ItemStack is = player.getInventory().getItemInMainHand();
        if (!NMSManager.hasNBT(is, "FeedBackBook")) {
            return;
        }

        player.sendMessage(Component.text().append(
            Component.text("[FEEDBACK] "),
            Component.text("報告してよろしいですか？", NamedTextColor.GREEN),
            Component.text("記入した内容で報告する場合は", NamedTextColor.GREEN),
            Component.text("ここ", NamedTextColor.AQUA, TextDecoration.UNDERLINED)
                .hoverEvent(HoverEvent.showText(Component.text("クリックすると手に持っている本の内容でフィードバックを作成します")))
                .clickEvent(ClickEvent.runCommand("/bug true")),
            Component.text("を、記入可能な本に戻す場合は", NamedTextColor.GREEN),
            Component.text("ここ", NamedTextColor.AQUA, TextDecoration.UNDERLINED)
                .hoverEvent(HoverEvent.showText(Component.text("クリックすると記入済みの本を記入可能な本に戻します")))
                .clickEvent(ClickEvent.runCommand("/bug false")),
            Component.text("をクリックして下さい。", NamedTextColor.GREEN)
        ));
    }
}
