package com.jaoafa.mymaid4.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.meta.CommandMeta;
import com.jaoafa.mymaid4.lib.CommandPremise;
import com.jaoafa.mymaid4.lib.MyMaidCommand;
import com.jaoafa.mymaid4.lib.MyMaidData;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Cmd_Link extends MyMaidLibrary implements CommandPremise {
    @Override
    public MyMaidCommand.Detail details() {
        return new MyMaidCommand.Detail(
            "link",
            "linkコマンドはjMS Gamers Clubで実行するのだ。"
        );
    }

    @Override
    public MyMaidCommand.Cmd register(Command.Builder<CommandSender> builder) {
        return new MyMaidCommand.Cmd(
            builder
                .meta(CommandMeta.DESCRIPTION, "linkコマンドはjMS Gamers Clubで実行するのだ。")
                .senderType(Player.class)
                .handler(this::tellLinkHelp)
                .build()
        );
    }

    void tellLinkHelp(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        SendMessage(player, details(), "linkコマンドはMinecraftサーバ内ではなくjMS Gamers Clubの" + ChatColor.BOLD + "Discordサーバ内" + ChatColor.RESET + "で実行してね！");
        SendMessage(player, details(), "そこでコマンドが発行されるから、そのコマンドをMinecraftサーバ内で打ち込んでね！");
    }
}
