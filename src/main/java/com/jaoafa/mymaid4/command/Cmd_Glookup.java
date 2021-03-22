package com.jaoafa.mymaid4.command;

import cloud.commandframework.Command;
import cloud.commandframework.bukkit.parsers.PlayerArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.meta.CommandMeta;
import com.jaoafa.mymaid4.lib.CommandPremise;
import com.jaoafa.mymaid4.lib.MyMaidCommand;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Cmd_Glookup extends MyMaidLibrary implements CommandPremise {
    @Override
    public MyMaidCommand.Detail details() {
        return new MyMaidCommand.Detail(
            "glookup",
            "他人のゲームモードを確認します。"
        );
    }

    @Override
    public MyMaidCommand.Cmd register(Command.Builder<CommandSender> builder) {
        return new MyMaidCommand.Cmd(
            builder
                .meta(CommandMeta.DESCRIPTION, "他人のゲームモードを確認します。")
                .argument(PlayerArgument.of("player"))
                .handler(this::playerGamemodeLookup)
                .build()
        );
    }

    void playerGamemodeLookup(CommandContext<CommandSender> context) {
        Player player = context.getOrDefault("player", null);
        if (player == null) {
            SendMessage(context.getSender(), details(), "プレイヤーは指定されていないか存在しません。");
            return;
        }

        SendMessage(context.getSender(), details(), player.getName() + "のゲームモードは" + player.getGameMode().name() + "です。");
    }
}
