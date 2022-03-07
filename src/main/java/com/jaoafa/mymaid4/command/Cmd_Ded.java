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
import com.jaoafa.mymaid4.lib.MyMaidData;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Cmd_Ded extends MyMaidLibrary implements CommandPremise {
    @Override
    public MyMaidCommand.Detail details() {
        return new MyMaidCommand.Detail(
            "ded",
            "最後に死亡した場所にテレポートします。"
        );
    }

    @Override
    public MyMaidCommand.Cmd register(Command.Builder<CommandSender> builder) {
        return new MyMaidCommand.Cmd(
            builder
                .meta(CommandMeta.DESCRIPTION, "最後に死亡した場所にテレポートします。")
                .senderType(Player.class)
                .handler(this::tpLastDed)
                .build()
        );
    }

    void tpLastDed(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        if (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE) {
            SendMessage(player, details(), "Dedコマンドはサバイバル・アドベンチャーモードでは利用できません。");
            SendMessage(player, details(), "クリエイティブモードに切り替えてから実行してください。");
            SendMessage(player, details(), Component.text("=== [!] 警告 ===", NamedTextColor.RED, TextDecoration.BOLD));
            SendMessage(player, details(), Component.text("PvP等での「/ded」コマンドの利用は原則禁止です！", NamedTextColor.RED, TextDecoration.BOLD));
            SendMessage(player, details(), Component.text("多く使用すると迷惑行為として認識される場合もあります！", NamedTextColor.RED, TextDecoration.BOLD));
            return;
        }
        if (!MyMaidData.getLastDed().containsKey(player.getName())) {
            SendMessage(player, details(), "最後に死亡した場所が見つかりませんでした。");
        } else {
            Location location = MyMaidData.getLastDed().get(player.getName());
            player.teleport(location);
            String locationDescription = String.format("( X:%s Y:%s Z:%s )", location.getBlockX(), location.getBlockY(), location.getBlockZ());
            SendMessage(player, details(), "最終死亡場所" + ChatColor.BOLD + locationDescription + ChatColor.RESET + "にテレポートしました。");
            SendMessage(player, details(), Component.text("=== [!] 警告 ===", NamedTextColor.RED, TextDecoration.BOLD));
            SendMessage(player, details(), Component.text("PvP等での「/ded」コマンドの利用は原則禁止です！", NamedTextColor.RED, TextDecoration.BOLD));
            SendMessage(player, details(), Component.text("多く使用すると迷惑行為として認識される場合もあります！", NamedTextColor.RED, TextDecoration.BOLD));
        }
    }
}
