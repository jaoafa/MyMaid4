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

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.Command;
import cloud.commandframework.bukkit.parsers.PlayerArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.meta.CommandMeta;
import com.jaoafa.mymaid4.lib.CommandPremise;
import com.jaoafa.mymaid4.lib.MyMaidCommand;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class Cmd_Light extends MyMaidLibrary implements CommandPremise {
    @Override
    public MyMaidCommand.Detail details() {
        return new MyMaidCommand.Detail(
            "light",
            "ライトブロックを入手します。"
        );
    }

    @Override
    public MyMaidCommand.Cmd register(Command.Builder<CommandSender> builder) {
        return new MyMaidCommand.Cmd(
            builder
                .meta(CommandMeta.DESCRIPTION, "ライトブロックをコマンド実行者のメインハンドのアイテムと置き換えます。")
                .senderType(Player.class)
                .handler(this::giveLight)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "ライトブロックを指定したプレイヤーのメインハンドのアイテムと置き換えます。")
                .argument(PlayerArgument.of("player"), ArgumentDescription.of("ライトブロックを付与するプレイヤー名"))
                .handler(this::giveLightToPlayer)
                .build()
        );
    }

    void giveLight(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        ItemStack is = new ItemStack(Material.LIGHT);

        PlayerInventory inv = player.getInventory();
        ItemStack main = inv.getItemInMainHand();

        inv.setItemInMainHand(is);
        SendMessage(player, details(), "ライトブロックをメインハンドのアイテムと置きかえました。");

        if (main.getType() != Material.AIR) {
            if (player.getInventory().firstEmpty() == -1) {
                player.getLocation().getWorld().dropItem(player.getLocation(), main);
                SendMessage(player, details(), "インベントリがいっぱいだったため、既に持っていたアイテムはあなたの足元にドロップしました。");
            } else {
                inv.addItem(main);
            }
        }
    }

    void giveLightToPlayer(CommandContext<CommandSender> context) {
        Player player = context.getOrDefault("player", null);
        if (player == null) {
            SendMessage(context.getSender(), details(), "プレイヤーは指定されていないか存在しません。");
            return;
        }

        ItemStack is = new ItemStack(Material.LIGHT);

        PlayerInventory inv = player.getInventory();
        ItemStack main = inv.getItemInMainHand();

        inv.setItemInMainHand(is);
        SendMessage(context.getSender(), details(), "ライトブロックをプレイヤー「" + player.getName() + "」のメインハンドのアイテムと置きかえました。");

        if (main.getType() != Material.AIR) {
            if (player.getInventory().firstEmpty() == -1) {
                player.getLocation().getWorld().dropItem(player.getLocation(), main);
                SendMessage(player, details(), "インベントリがいっぱいだったため、既に持っていたアイテムはあなたの足元にドロップしました。");
            } else {
                inv.addItem(main);
            }
        }
    }
}
