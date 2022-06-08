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

public class Cmd_Boots extends MyMaidLibrary implements CommandPremise {
    @Override
    public MyMaidCommand.Detail details() {
        return new MyMaidCommand.Detail(
            "boots",
            "手に持っているアイテムを足に付けます。"
        );
    }

    @Override
    public MyMaidCommand.Cmd register(Command.Builder<CommandSender> builder) {
        return new MyMaidCommand.Cmd(
            builder
                .meta(CommandMeta.DESCRIPTION, "手に持っているアイテムを足に付けます。")
                .handler(this::setItemBoots)
                .build()
        );
    }

    void setItemBoots(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        PlayerInventory inv = player.getInventory();
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand.getType() == Material.AIR) {
            SendMessage(player, details(), "手にブロックを持ってください。");
            return;
        }
        ItemStack boots = inv.getBoots();
        if (boots != null) {
            if (boots.getType() != Material.AIR) {
                inv.removeItem(boots);
            }
        }
        inv.setBoots(hand);
        player.getInventory().setItemInMainHand(boots);
        SendMessage(player, details(), "持っていたブロックを足に付けました。");
    }
}
