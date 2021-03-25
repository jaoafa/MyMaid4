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
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.bukkit.parsers.PlayerArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.meta.CommandMeta;
import com.jaoafa.mymaid4.lib.CommandPremise;
import com.jaoafa.mymaid4.lib.MyMaidCommand;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Cmd_G extends MyMaidLibrary implements CommandPremise {
    @Override
    public MyMaidCommand.Detail details() {
        return new MyMaidCommand.Detail(
            "g",
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
                    .withSuggestionsProvider(this::suggestGameMode))
                .handler(this::changeGamemode)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "指定されたプレイヤーのゲームモードを切り替えます。")
                .argument(StringArgument
                    .<CommandSender>newBuilder("gamemode")
                    .withSuggestionsProvider(this::suggestGameMode))
                .argument(PlayerArgument.of("player"))
                .handler(this::changePlayerGamemode)
                .build()
        );
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

    void autoChangeGamemode(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        String beforeGamemode = player.getGameMode().name();
        if (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE || player.getGameMode() == GameMode.SPECTATOR) {
            player.setGameMode(GameMode.CREATIVE);
            SendMessage(player, details(), "ゲームモードを切り替えました。");
            SendMessage(player, details(), beforeGamemode + " -> " + ChatColor.BOLD + "CREATIVE");
            return;
        }
        if (player.getGameMode() == GameMode.CREATIVE) {
            if (isAMR(player)) {
                player.setGameMode(GameMode.SPECTATOR);
                SendMessage(player, details(), "ゲームモードを切り替えました。");
                SendMessage(player, details(), beforeGamemode + " -> " + ChatColor.BOLD + "SPECTATOR");
            } else {
                SendMessage(player, details(), "あなたはゲームモードをスペクテイターに切り替えることができません。");
            }
        } else {
            gamemodeNotFound(player);
        }

    }

    void changeGamemode(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        String gamemodeName = context.getOrDefault("gamemode", "creative");
        String beforeGamemode = player.getGameMode().name();

        if (gamemodeName == null && getGameModeStartWith(gamemodeName) == null) {
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
        player.setGameMode(getGameModeStartWith(gamemodeName));
        SendMessage(player, details(), "ゲームモードを切り替えました。");
        SendMessage(player, details(), beforeGamemode + " -> " + ChatColor.BOLD + player.getGameMode().name());
    }

    void changePlayerGamemode(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        Player gamemodeChangePlayer = context.getOrDefault("player", null);
        String gamemodeName = context.getOrDefault("gamemode", "creative");
        String changePlayerName = gamemodeChangePlayer.getName();
        String beforeGamemode = gamemodeChangePlayer.getGameMode().name();

        if (!isAMR(player)) {
            SendMessage(player, details(), "あなたは他人のゲームモードを切り替えることができません。");
            return;
        }
        if (getGameModeStartWith(gamemodeName) == null) {
            gamemodeNotFound(player);
        }
        if (getGameModeStartWith(gamemodeName) == GameMode.SPECTATOR && !isAMR(gamemodeChangePlayer)) {
            SendMessage(player, details(), gamemodeChangePlayer.getName() + "のゲームモードをスペクテイターに切り替えることができません。");
            return;
        }

        gamemodeChangePlayer.setGameMode(getGameModeStartWith(gamemodeName));
        SendMessage(player, details(), changePlayerName + "のゲームモードを切り替えました。");
        SendMessage(player, details(), beforeGamemode + " -> " + ChatColor.BOLD + gamemodeChangePlayer.getGameMode().name());
    }

    List<String> suggestGameMode(final CommandContext<CommandSender> context, final String current) {
        List<String> list = new ArrayList<>();
        list.addAll(Arrays.asList("1", "2", "3", "s", "c", "a", "sp"));
        list.addAll(Arrays.stream(GameMode.values())
            .map(Enum::name)
            .collect(Collectors.toList()));

        return list.stream()
            .filter(s -> s.toLowerCase().startsWith(current.toLowerCase()))
            .collect(Collectors.toList());
    }
}
