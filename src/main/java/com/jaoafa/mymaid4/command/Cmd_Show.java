package com.jaoafa.mymaid4.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.meta.CommandMeta;
import com.jaoafa.mymaid4.lib.CommandPremise;
import com.jaoafa.mymaid4.lib.MyMaidCommand;
import com.jaoafa.mymaid4.lib.MyMaidConfig;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Cmd_Show extends MyMaidLibrary implements CommandPremise {
    @Override
    public MyMaidCommand.Detail details() {
        return new MyMaidCommand.Detail(
            "show",
            "Hide状態を解除します。"
        );
    }

    @Override
    public MyMaidCommand.Cmd register(Command.Builder<CommandSender> builder) {
        return new MyMaidCommand.Cmd(
            builder
                .meta(CommandMeta.DESCRIPTION, "Hide状態を解除します。")
                .senderType(Player.class)
                .handler(this::addHid)
                .build()
        );
    }

    void addHid(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        if (!isAMR(player)) {
            SendMessage(player, details(), "あなたの権限ではこのコマンドを実行することができません！");
            return;
        }

        player.showPlayer(player);
        if (MyMaidConfig.isHid(player.getUniqueId())) {
            MyMaidConfig.removeHid(player.getUniqueId());
        }
        SendMessage(player, details(), "あなたは他のプレイヤーから見えなくなりました。見えるようにするには/showを実行しましょう。");
        SendMessage(player, details(), "なお、プレイヤーリストからも見えなくなりますのでお気をつけて。");
    }
}
