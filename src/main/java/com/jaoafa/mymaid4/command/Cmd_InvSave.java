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
import java.io.IOException;

public class Cmd_InvSave extends MyMaidLibrary implements CommandPremise {
    @Override
    public MyMaidCommand.Detail details() {
        return new MyMaidCommand.Detail(
            "invsave",
            "プレイヤーのインベントリを保存します。"
        );
    }

    @Override
    public MyMaidCommand.Cmd register(Command.Builder<CommandSender> builder) {
        return new MyMaidCommand.Cmd(
            builder
                .meta(CommandMeta.DESCRIPTION, "指定したプレイヤーのインベントリを指定した名前で保存します。")
                .argument(SinglePlayerSelectorArgument.of("target"), ArgumentDescription.of("インベントリを保存するプレイヤー"))
                .argument(StringArgument.optional("saveName"), ArgumentDescription.of("保存名"))
                .handler(this::saveInventory)
                .build()
        );
    }

    void saveInventory(CommandContext<CommandSender> context) {
        CommandSender sender = context.getSender();
        SinglePlayerSelector targetSelector = context.getOrDefault("target", null);
        String saveName = context.getOrDefault("saveName", "DEFAULT");

        if (targetSelector == null) {
            SendMessage(sender, details(), "保存する対象のプレイヤーを指定してください。");
            return;
        }

        Player target = targetSelector.getPlayer();

        if (target == null) {
            SendMessage(sender, details(), "対象のプレイヤーを特定できませんでした。");
            return;
        }

        ItemStack[] contents = target.getInventory().getContents();
        ItemStack[] armors = target.getInventory().getArmorContents();

        File file = new File(Main.getJavaPlugin().getDataFolder(), "saveInventory.yml");
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
        yml.set(target.getUniqueId() + "." + saveName + ".contents", contents);
        yml.set(target.getUniqueId() + "." + saveName + ".armors", armors);
        try {
            yml.save(file);
            SendMessage(sender, details(), target.getName() + "のインベントリを「" + saveName + "」という名前で保存しました。");
        } catch (IOException e) {
            reportError(getClass(), e);
            SendMessage(sender, details(), target.getName() + "のインベントリを「" + saveName + "」という名前で保存しようとしましたが失敗しました。");
            MyMaidLibrary.reportError(getClass(), e);
        }
    }
}
