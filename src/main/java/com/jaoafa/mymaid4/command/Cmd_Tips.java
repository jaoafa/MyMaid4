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
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.meta.CommandMeta;
import com.jaoafa.mymaid4.lib.*;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Cmd_Tips extends MyMaidLibrary implements CommandPremise {
    @Override
    public MyMaidCommand.Detail details() {
        return new MyMaidCommand.Detail(
            "tips",
            "Tipsの定型文を投稿・管理します。"
        );
    }

    @Override
    public MyMaidCommand.Cmd register(Command.Builder<CommandSender> builder) {
        return new MyMaidCommand.Cmd(
            builder
                .meta(CommandMeta.DESCRIPTION, "Tipsを追加します。")
                .literal("add", "a", "register", "reg")
                .argument(StringArgument.of("name"), ArgumentDescription.of("Tipsの名前"))
                .argument(StringArgument.greedy("text"), ArgumentDescription.of("Tipsの文章"))
                .handler(this::addTip)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "Tipsを削除します。")
                .literal("remove", "rem", "del", "r", "d", "unregister", "unreg")
                .argument(StringArgument.of("name"), ArgumentDescription.of("Tipsの名前"))
                .handler(this::removeTip)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "Tipsを表示します。")
                .literal("list", "l", "view", "v")
                .handler(this::listTip)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "Tipsを送信・発言します。")
                .literal("broadcast", "b", "send", "s", "say")
                .argument(StringArgument.<CommandSender>newBuilder("name").withSuggestionsProvider(this::suggestTipNames), ArgumentDescription.of("Tipsの名前"))
                .handler(this::broadcastTip)
                .build()
        );
    }

    void addTip(CommandContext<CommandSender> context) {
        CommandSender sender = context.getSender();
        String name = context.get("name");
        String text = context.get("text");

        if (Tips.isExist(name)) {
            SendMessage(sender, details(), "「%s」は既に存在しています。".formatted(name));
            return;
        }

        Tips.addTip(name, text);
        SendMessage(sender, details(), "「%s」を追加しました。".formatted(name));
    }

    void removeTip(CommandContext<CommandSender> context) {
        CommandSender sender = context.getSender();
        String name = context.get("name");

        if (!Tips.isExist(name)) {
            SendMessage(sender, details(), "「%s」は存在しません。".formatted(name));
            return;
        }

        Tips.removeTip(name);
        SendMessage(sender, details(), "「%s」を削除しました。".formatted(name));
    }

    void listTip(CommandContext<CommandSender> context) {
        CommandSender sender = context.getSender();

        SendMessage(sender, details(), "Tips 一覧");
        for (Map.Entry<String, String> entry : Tips.getTips().entrySet()) {
            SendMessage(sender, details(), "- %s: %s".formatted(entry.getKey(), entry.getValue()));
        }
    }

    void broadcastTip(CommandContext<CommandSender> context) {
        CommandSender sender = context.getSender();
        String name = context.get("name");

        TextColor color = NamedTextColor.GRAY;
        if (sender instanceof Player) {
            SKKColorManager.getPlayerColor((Player) sender);
        }

        if (!Tips.isExist(name)) {
            SendMessage(sender, details(), "「%s」は存在しません。".formatted(name));
            return;
        }

        String text = Tips.getTip(name);
        chatFake(color, sender.getName(), text);
    }

    List<String> suggestTipNames(final CommandContext<CommandSender> context, final String current) {
        return Tips.getTips().keySet().stream()
            .filter(name -> name.startsWith(current))
            .collect(Collectors.toList());
    }
}
