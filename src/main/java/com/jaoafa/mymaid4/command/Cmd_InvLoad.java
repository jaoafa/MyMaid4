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
import cloud.commandframework.bukkit.arguments.selector.SinglePlayerSelector;
import cloud.commandframework.bukkit.parsers.selector.SinglePlayerSelectorArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.meta.CommandMeta;
import com.jaoafa.mymaid4.Main;
import com.jaoafa.mymaid4.lib.CommandPremise;
import com.jaoafa.mymaid4.lib.MyMaidCommand;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.List;
import java.util.Objects;

public class Cmd_InvLoad extends MyMaidLibrary implements CommandPremise {
    @Override
    public MyMaidCommand.Detail details() {
        return new MyMaidCommand.Detail(
            "invload",
            "プレイヤーのインベントリを復元します。"
        );
    }

    @Override
    public MyMaidCommand.Cmd register(Command.Builder<CommandSender> builder) {
        return new MyMaidCommand.Cmd(
            builder
                .meta(CommandMeta.DESCRIPTION, "指定したプレイヤーにおける指定した名前のインベントリを復元します。")
                .argument(SinglePlayerSelectorArgument.of("target"), ArgumentDescription.of("インベントリを保存したプレイヤー"))
                .argument(StringArgument.optional("saveName"), ArgumentDescription.of("保存名"))
                .argument(SinglePlayerSelectorArgument.optional("player"), ArgumentDescription.of("復元先のプレイヤー"))
                .handler(this::loadInventory)
                .build()
        );
    }

    void loadInventory(CommandContext<CommandSender> context) {
        CommandSender sender = context.getSender();
        SinglePlayerSelector targetSelector = context.getOrDefault("target", null);
        String saveName = context.getOrDefault("saveName", "DEFAULT");
        SinglePlayerSelector playerSelector = context.getOrDefault("player", null);

        if (targetSelector == null) {
            SendMessage(context.getSender(), details(), "復元する対象のプレイヤーを指定してください。");
            return;
        }

        Player target = targetSelector.getPlayer();

        if (target == null) {
            SendMessage(sender, details(), "対象のプレイヤーを特定できませんでした。");
            return;
        }

        Player player = target;
        if (playerSelector != null) {
            player = playerSelector.getPlayer();
            if (player == null) {
                SendMessage(sender, details(), "復元先のプレイヤーを特定できませんでした。");
                return;
            }
        }

        File file = new File(Main.getJavaPlugin().getDataFolder(), "saveInventory.yml");
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);

        if (!yml.contains(target.getUniqueId() + "." + saveName + ".contents") || !yml.contains(target.getUniqueId() + "." + saveName + ".armors")) {
            SendMessage(sender, details(), "指定された名前で保存されたインベントリは見つかりませんでした。");
            return;
        }

        @SuppressWarnings("unchecked") // 未チェックのキャスト。YamlConfigurationの仕様によるもの
        ItemStack[] contents = ((List<ItemStack>) Objects.requireNonNull(yml.get(target.getUniqueId() + "." + saveName + ".contents"))).toArray(new ItemStack[0]);
        @SuppressWarnings("unchecked") // 同
        ItemStack[] armors = ((List<ItemStack>) Objects.requireNonNull(yml.get(target.getUniqueId() + "." + saveName + ".armors"))).toArray(new ItemStack[0]);

        player.getInventory().setContents(contents);
        player.getInventory().setArmorContents(armors);

        SendMessage(sender, details(), target.getName() + "のインベントリを「" + saveName + "」という名前で" + (player != target ? player.getName() + "に" : "") + "復元しました。");
    }
}
