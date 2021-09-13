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

package com.jaoafa.mymaid4.command;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.Command;
import cloud.commandframework.bukkit.parsers.PlayerArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.meta.CommandMeta;
import com.jaoafa.mymaid4.lib.CommandPremise;
import com.jaoafa.mymaid4.lib.MyMaidCommand;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;

public class Cmd_InvShow extends MyMaidLibrary implements CommandPremise {
    @Override
    public MyMaidCommand.Detail details() {
        return new MyMaidCommand.Detail(
            "invshow",
            "プレイヤーのインベントリを閲覧します。"
        );
    }

    @Override
    public MyMaidCommand.Cmd register(Command.Builder<CommandSender> builder) {
        return new MyMaidCommand.Cmd(
            builder
                .senderType(Player.class)
                .meta(CommandMeta.DESCRIPTION, "指定したプレイヤーのインベントリを閲覧します。")
                .argument(PlayerArgument.of("target"), ArgumentDescription.of("対象のプレイヤー"))
                .handler(this::showInventory)
                .build()
        );
    }

    void showInventory(CommandContext<CommandSender> context) {
        CommandSender sender = context.getSender();
        Player player = (Player) sender;
        Player target = context.get("target");
        if (!isAMR(player)) {
            SendMessage(sender, details(), "あなたの権限ではこのコマンドを使用できません。");
            return;
        }

        PlayerInventory inv = target.getInventory();
        Inventory inventory = Bukkit.getServer().createInventory(player, 6 * 9, Component.text(target.getName() + "のインベントリ"));
        for (int i = 0; i < inv.getArmorContents().length; i++) {
            inventory.setItem(i, inv.getArmorContents()[i]);
        }
        inventory.setItem(8, inv.getItemInOffHand());
        for (int i = 0; i < inv.getStorageContents().length; i++) {
            inventory.setItem(i + 9, inv.getStorageContents()[i]);
        }
        player.openInventory(inventory);
    }
}
