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

package com.jaoafa.mymaid4.command;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.arguments.standard.StringArrayArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.meta.CommandMeta;
import com.jaoafa.mymaid4.lib.CommandPremise;
import com.jaoafa.mymaid4.lib.MyMaidCommand;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Cmd_ItemEdit extends MyMaidLibrary implements CommandPremise {
    @Override
    public MyMaidCommand.Detail details() {
        return new MyMaidCommand.Detail(
            "itemedit",
            "持っているアイテムの表示名や説明文を変更します。"
        );
    }

    @Override
    public MyMaidCommand.Cmd register(Command.Builder<CommandSender> builder) {
        return new MyMaidCommand.Cmd(
            builder
                .meta(CommandMeta.DESCRIPTION, "手に持っているアイテムの表示名を変更します。")
                .literal("name")
                .argument(StringArgument.greedy("name"))
                .senderType(Player.class)
                .handler(this::changeDisplayName)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "手に持っているアイテムの説明文を変更します。")
                .literal("lore")
                .argument(StringArrayArgument.of("lore", (c, l) -> new ArrayList<>()))
                .senderType(Player.class)
                .handler(this::changeLore)
                .build()
        );
    }

    void changeDisplayName(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        String name = context.get("name");

        PlayerInventory inv = player.getInventory();
        ItemStack hand = inv.getItemInMainHand();
        ItemMeta meta = hand.getItemMeta();
        meta.displayName(Component.text(name));
        hand.setItemMeta(meta);
        inv.setItemInMainHand(hand);

        SendMessage(player, details(), "アイテムの表示名を「%s」に変更しました。".formatted(name));
    }

    void changeLore(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        String[] rawLore = context.get("lore");
        List<Component> lore = Arrays.stream(rawLore)
            .map(Component::text)
            .map(Component::asComponent)
            .toList();

        PlayerInventory inv = player.getInventory();
        ItemStack hand = inv.getItemInMainHand();
        ItemMeta meta = hand.getItemMeta();
        meta.lore(lore);
        hand.setItemMeta(meta);
        inv.setItemInMainHand(hand);

        SendMessage(player, details(), "アイテムの説明文「%s」に変更しました。".formatted(
            String.join("」「", rawLore)
        ));
    }
}
