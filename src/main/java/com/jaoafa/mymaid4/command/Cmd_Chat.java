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
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.meta.CommandMeta;
import com.jaoafa.mymaid4.lib.CommandPremise;
import com.jaoafa.mymaid4.lib.MyMaidCommand;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Cmd_Chat extends MyMaidLibrary implements CommandPremise {

    @Override
    public MyMaidCommand.Detail details() {
        return new MyMaidCommand.Detail(
            "chat",
            "偽のプレイヤーに喋らせます。"
        );
    }

    @Override
    public MyMaidCommand.Cmd register(Command.Builder<CommandSender> builder) {

        return new MyMaidCommand.Cmd(
            builder
                .meta(CommandMeta.DESCRIPTION, "偽のプレイヤーに喋らせます。")
                .argument(StringArgument.single("player"), ArgumentDescription.of("喋らせるプレイヤー名"))
                .argument(StringArgument.greedy("message"), ArgumentDescription.of("喋らせるメッセージ"))
                .handler(this::chatFake)
                .build()
        );
    }


    void chatFake(CommandContext<CommandSender> context) {
        String player = context.get("player");
        List<String> messages = Arrays.asList(context.<String>get("message").split(" "));

        // 定義されている場合四角色を取得
        Optional<String> strColor = messages.stream()
            .filter(s -> s.startsWith("color:"))
            .map(s -> s.substring(s.indexOf(":") + 1))
            .findFirst();
        NamedTextColor color = strColor.isPresent() ?
            getNamedTextColor(strColor.get()) :
            NamedTextColor.GRAY;

        // 四角色指定以外のテキストを取得
        String message = messages.stream()
            .filter(s -> !s.startsWith("color:"))
            .collect(Collectors.joining(" "));

        // プレイヤーがjaotanだった場合ゴールドを強制する
        if (player.equals("jaotan")) {
            color = NamedTextColor.GOLD;
        }

        chatFake(color, player, message);
    }
}
