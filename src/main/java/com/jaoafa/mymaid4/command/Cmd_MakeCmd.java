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

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.meta.CommandMeta;
import com.jaoafa.mymaid4.lib.CommandPremise;
import com.jaoafa.mymaid4.lib.MyMaidCommand;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import com.jaoafa.mymaid4.lib.NMSManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Cmd_MakeCmd extends MyMaidLibrary implements CommandPremise {
    @Override
    public MyMaidCommand.Detail details() {
        return new MyMaidCommand.Detail(
            "makecmd",
            Collections.singletonList("givecmd"),
            "giveコマンドを生成します。"
        );
    }

    @Override
    public MyMaidCommand.Cmd register(Command.Builder<CommandSender> builder) {
        return new MyMaidCommand.Cmd(
            builder
                .meta(CommandMeta.DESCRIPTION, "giveコマンドを生成します。")
                .senderType(Player.class)
                .handler(this::givecmd)
                .build()
        );
    }

    void givecmd(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        PlayerInventory inv = player.getInventory();
        ItemStack main = inv.getItemInMainHand();

        if (main.getType() == Material.AIR) {
            SendMessage(player, details(), "手にアイテムを持ってください。");
            return;
        }

        // /give <player>[<selectors>] <item>[<NBT>] [<count>]

        String nbt = NMSManager.getNBT(main);
        if (nbt == null) {
            SendMessage(player, details(), "NBTデータの取得に失敗しました。");
            return;
        }

        List<String> command = new ArrayList<>();
        command.add("/give");
        command.add("@p");
        command.add(main.getType().name().toLowerCase() + (!nbt.equals("{}") ? nbt : ""));
        command.add(String.valueOf(main.getAmount()));

        SendMessage(player, details(), Component.text().append(
            Component.text("ここ", NamedTextColor.AQUA, TextDecoration.UNDERLINED)
                .clickEvent(ClickEvent.copyToClipboard(String.join(" ", command))),
            Component.text("をクリックすると手に持っているアイテムのgiveコマンドをコピーできます。", NamedTextColor.GREEN)
        ).build());
    }
}
