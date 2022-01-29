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
import com.jaoafa.mymaid4.Main;
import com.jaoafa.mymaid4.lib.CommandPremise;
import com.jaoafa.mymaid4.lib.MyMaidCommand;
import com.jaoafa.mymaid4.lib.MyMaidData;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Cmd_jaoBox extends MyMaidLibrary implements CommandPremise {
    public static Component viewerTitleComponent = Component.text("jaoBox", NamedTextColor.YELLOW);
    public static Component registerTitleComponent = Component.join(
        JoinConfiguration.noSeparators(),
        Component.text("jaoBox"),
        Component.space(),
        Component.text("(登録モード)", NamedTextColor.RED, TextDecoration.UNDERLINED)
    );
    public static File file;

    @Override
    public MyMaidCommand.Detail details() {
        return new MyMaidCommand.Detail(
            "jaobox",
            "jao Minecraft Serverでの便利アイテムを集めたjaoBoxを開きます。"
        );
    }

    @Override
    public MyMaidCommand.Cmd register(Command.Builder<CommandSender> builder) {
        return new MyMaidCommand.Cmd(
            builder
                .senderType(Player.class)
                .meta(CommandMeta.DESCRIPTION, "jaoBoxを開きます。")
                .handler(this::jaoBox)
                .build(),
            builder
                .senderType(Player.class)
                .meta(CommandMeta.DESCRIPTION, "jaoBoxを設定します。")
                .literal("register")
                .handler(this::jaoBoxRegister)
                .build()
        );
    }

    void jaoBox(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();

        openBox(player, viewerTitleComponent);
    }

    void jaoBoxRegister(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();

        if (!isAMR(player)) {
            SendMessage(player, details(), "あなたにはjaoBoxを登録モードで開く権限がありません。");
            return;
        }

        openBox(player, registerTitleComponent);
        SendMessage(player, details(), "jaoBoxを登録モードで開きました。Escキーで閉じると保存します。");
    }

    public static void openBox(Player player, Component title) {
        if (file == null) {
            file = new File(Main.getJavaPlugin().getDataFolder(), "jaoBox.yml");
        }

        Inventory inventory = Bukkit.getServer().createInventory(
            player,
            6 * 9,
            title);
        if (file.exists()) {
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
            @SuppressWarnings("unchecked") // 未チェックのキャスト。YamlConfigurationの仕様によるもの
            ItemStack[] items = ((List<ItemStack>) Objects.requireNonNull(yaml.get("items"))).toArray(new ItemStack[0]);
            inventory.setContents(items);

            if (title == registerTitleComponent) {
                MyMaidData.setBoxPrevious(player.getUniqueId(), Arrays.stream(items).toList());
            }
        }

        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0F, 1.0F);
        player.openInventory(inventory);
    }
}
