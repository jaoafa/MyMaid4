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
import cloud.commandframework.bukkit.parsers.PlayerArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.meta.CommandMeta;
import com.jaoafa.mymaid4.lib.CommandPremise;
import com.jaoafa.mymaid4.lib.MyMaidCommand;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Cmd_G extends MyMaidLibrary implements CommandPremise {
    @Override
    public MyMaidCommand.Detail details() {
        return new MyMaidCommand.Detail(
            "g",
            Collections.singletonList("gm"),
            "ゲームモードを変更します。"
        );
    }

    @Override
    public MyMaidCommand.Cmd register(Command.Builder<CommandSender> builder) {
        return new MyMaidCommand.Cmd(
            builder
                .meta(CommandMeta.DESCRIPTION, "ゲームモードを切り替えます。")
                .senderType(Player.class)
                .handler(this::autoChangeGamemode)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "指定されたゲームモードに切り替えます。")
                .senderType(Player.class)
                .argument(StringArgument
                    .<CommandSender>newBuilder("gamemode")
                    .withSuggestionsProvider(this::suggestGameMode), ArgumentDescription.of("ゲームモード"))
                .handler(this::changeGamemode)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "指定されたプレイヤーのゲームモードを切り替えます。")
                .argument(StringArgument
                    .<CommandSender>newBuilder("gamemode")
                    .withSuggestionsProvider(this::suggestGameMode))
                .argument(PlayerArgument.of("player"), ArgumentDescription.of("ゲームモード"))
                .handler(this::changePlayerGamemode)
                .build()
        );
    }

    void autoChangeGamemode(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        GameMode beforeGamemode = player.getGameMode();
        // サバイバル/アドベンチャー  → クリエイティブ
        // スペクテイター → クリエイティブ
        // クリエイティブ → スペクテイター

        if (player.getGameMode() == GameMode.CREATIVE) {
            if (!isAMR(player)) {
                SendMessage(player, details(), "あなたはゲームモードをスペクテイターに切り替えることができません。");
                return;
            }

            player.setGameMode(GameMode.SPECTATOR);
            SendMessage(player, details(), Component.text().append(
                Component.text("ゲームモードを切り替えました: ", NamedTextColor.GREEN),
                Component.text(beforeGamemode.name(), NamedTextColor.GREEN)
                    .hoverEvent(HoverEvent.showText(Component.text(MessageFormat.format("{0} にゲームモードを変更します", beforeGamemode.name()))))
                    .clickEvent(ClickEvent.runCommand(String.format("/g %s", beforeGamemode.name()))),
                Component.text(" -> ", NamedTextColor.GREEN),
                Component.text("SPECTATOR", NamedTextColor.GREEN, TextDecoration.BOLD)
            ).build());
        } else {
            player.setGameMode(GameMode.CREATIVE);
            SendMessage(player, details(), Component.text().append(
                Component.text("ゲームモードを切り替えました: ", NamedTextColor.GREEN),
                Component.text(beforeGamemode.name(), NamedTextColor.GREEN)
                    .hoverEvent(HoverEvent.showText(Component.text(MessageFormat.format("{0} にゲームモードを変更します", beforeGamemode.name()))))
                    .clickEvent(ClickEvent.runCommand(String.format("/g %s", beforeGamemode.name()))),
                Component.text(" -> ", NamedTextColor.GREEN),
                Component.text(player.getGameMode().name(), NamedTextColor.GREEN, TextDecoration.BOLD)
            ).build());
        }
    }

    void changeGamemode(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        String gamemodeName = context.getOrDefault("gamemode", "creative");
        GameMode beforeGamemode = player.getGameMode();

        if (gamemodeName == null || getGameModeStartWith(gamemodeName) == null) {
            gamemodeNotFound(player);
            return;
        }
        if (getGameModeStartWith(gamemodeName) == GameMode.SPECTATOR && !isAMR(player)) {
            SendMessage(player, details(), "あなたはゲームモードをスペクテイターに切り替えることができません。");
            return;
        }
        if (getGameModeStartWith(gamemodeName) == null) {
            gamemodeNotFound(player);
            return;
        }
        GameMode gamemode = getGameModeStartWith(gamemodeName);
        if (gamemode == null) {
            SendMessage(player, details(), "指定されたゲームモードは見つかりませんでした。");
            return;
        }

        player.setGameMode(gamemode);
        SendMessage(player, details(), Component.text().append(
            Component.text("ゲームモードを切り替えました: ", NamedTextColor.GREEN),
            Component.text(beforeGamemode.name(), NamedTextColor.GREEN)
                .hoverEvent(HoverEvent.showText(Component.text(MessageFormat.format("{0} にゲームモードを変更します", beforeGamemode.name()))))
                .clickEvent(ClickEvent.runCommand(String.format("/g %s", beforeGamemode.name()))),
            Component.text(" -> ", NamedTextColor.GREEN),
            Component.text(player.getGameMode().name(), NamedTextColor.GREEN, TextDecoration.BOLD)
        ).build());
    }

    void changePlayerGamemode(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        Player target = context.getOrDefault("player", null);
        if (target == null) {
            SendMessage(player, details(), "ゲームモードを変更するプレイヤーを指定してください。");
            return;
        }
        String gamemodeName = context.getOrDefault("gamemode", "creative");
        assert gamemodeName != null;
        GameMode beforeGamemode = target.getGameMode();

        if (!isAMR(player)) {
            SendMessage(player, details(), "あなたは他人のゲームモードを切り替えることができません。");
            return;
        }
        if (getGameModeStartWith(gamemodeName) == null) {
            gamemodeNotFound(player);
        }
        if (getGameModeStartWith(gamemodeName) == GameMode.SPECTATOR && !isAMR(target)) {
            SendMessage(player, details(), MessageFormat.format("{0} のゲームモードをスペクテイターに切り替えることができません。", target.getName()));
            return;
        }

        GameMode gamemode = getGameModeStartWith(gamemodeName);
        if (gamemode == null) {
            SendMessage(player, details(), "指定されたゲームモードは見つかりませんでした。");
            return;
        }

        target.setGameMode(gamemode);
        SendMessage(player, details(), Component.text().append(
            Component.text(MessageFormat.format("{0} のゲームモードを切り替えました: ", target.getName()), NamedTextColor.GREEN),
            Component.text(beforeGamemode.name(), NamedTextColor.GREEN)
                .hoverEvent(HoverEvent.showText(Component.text(MessageFormat.format("{0} にゲームモードを変更します", beforeGamemode.name()))))
                .clickEvent(ClickEvent.runCommand(String.format("/g %s", beforeGamemode.name()))),
            Component.text(" -> ", NamedTextColor.GREEN),
            Component.text(target.getGameMode().name(), NamedTextColor.GREEN, TextDecoration.BOLD)
        ).build());
        SendMessage(player, details(), target.getName() + "のゲームモードを切り替えました。");
        SendMessage(player, details(), beforeGamemode.name() + " -> " + ChatColor.BOLD + target.getGameMode().name());
    }

    List<String> suggestGameMode(final CommandContext<CommandSender> context, final String current) {
        List<String> list = new ArrayList<>();
        list.addAll(Arrays.asList("1", "2", "3", "s", "c", "a", "sp"));
        list.addAll(Arrays.stream(GameMode.values())
            .map(Enum::name).toList());

        return list.stream()
            .filter(s -> s.toLowerCase().startsWith(current.toLowerCase()))
            .collect(Collectors.toList());
    }

    void gamemodeNotFound(Player player) {
        Component component = Component.text().append(
            Component.text("指定されたゲームモードが見つかりませんでした。", NamedTextColor.GREEN),
            Component.newline(),
            Component.text("Survival -> s / 0 / survival", NamedTextColor.GOLD)
                .clickEvent(ClickEvent.runCommand("/g s")),
            Component.newline(),
            Component.text("Creative -> c / 1 / creative", NamedTextColor.WHITE)
                .clickEvent(ClickEvent.runCommand("/g c")),
            Component.newline(),
            Component.text("Advanture -> a / 2 / advanture", NamedTextColor.RED)
                .clickEvent(ClickEvent.runCommand("/g a")),
            Component.newline(),
            Component.text("Spectator -> sp / 3 / spectator", NamedTextColor.YELLOW)
                .clickEvent(ClickEvent.runCommand("/g sp")),
            Component.newline(),
            Component.text("で指定できます。", NamedTextColor.GREEN)
        ).build();
        SendMessage(player, details(), component);
    }

    @Nullable
    GameMode getGameModeStartWith(String str) {
        switch (str) {
            case "0":
                return GameMode.SURVIVAL;
            case "1":
                return GameMode.CREATIVE;
            case "2":
                return GameMode.ADVENTURE;
            case "3":
                return GameMode.SPECTATOR;
        }
        for (GameMode mode : GameMode.values()) {
            if (mode.name().toLowerCase().startsWith(str)) {
                return mode;
            }
        }
        return null;
    }
}
