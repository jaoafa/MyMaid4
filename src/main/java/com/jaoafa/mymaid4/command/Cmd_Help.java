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
import com.jaoafa.mymaid4.Main;
import com.jaoafa.mymaid4.lib.CommandPremise;
import com.jaoafa.mymaid4.lib.MyMaidCommand;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Cmd_Help extends MyMaidLibrary implements CommandPremise {
    File file = null;

    @Override
    public MyMaidCommand.Detail details() {
        return new MyMaidCommand.Detail(
            "help",
            Collections.singletonList("?"),
            "ヘルプブックを取得・登録します。"
        );
    }

    @Override
    public MyMaidCommand.Cmd register(Command.Builder<CommandSender> builder) {
        return new MyMaidCommand.Cmd(
            builder
                .meta(CommandMeta.DESCRIPTION, "ヘルプブックを取得します。")
                .senderType(Player.class)
                .argument(StringArgument
                    .<CommandSender>newBuilder("params")
                    .greedy()
                    .asOptional()
                    .build(), ArgumentDescription.of("ヘルプパラメーター"))
                .handler(this::giveHelpBook)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "ヘルプブックを登録します。")
                .literal("register")
                .handler(this::registerHelpBook)
                .build()
        );
    }

    void giveHelpBook(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();

        if (file == null) {
            file = new File(Main.getJavaPlugin().getDataFolder(), "helpBook.yml");
        }

        if (isAMR(player)) {
            String params = context.getOrDefault("params", "");
            assert params != null;
            SendMessage(player, details(), Component.text().append(
                Component.text("「"),
                Component.text(String.format("/bukkit:help %s", params).trim(), NamedTextColor.AQUA, TextDecoration.UNDERLINED)
                    .clickEvent(ClickEvent.runCommand(String.format("/bukkit:help %s", params).trim())),
                Component.text("」を実行することで純正のヘルプコマンドを表示できます。")
            ).build());
        }
        ItemStack is = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) is.getItemMeta();
        if (!file.exists()) {
            SendMessage(player, details(), "ヘルプブックが登録されていません。運営によって登録されるのをお待ちください…。");
            return;
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        String strTitle = yaml.getString("title");
        Component title = strTitle != null ? GsonComponentSerializer.gson().deserialize(strTitle) : Component.text().build();
        List<Component> pages = yaml.getStringList("pages").stream()
            .map(page -> GsonComponentSerializer.gson().deserialize(page))
            .collect(Collectors.toList());
        Book book = meta.title(title).pages(pages);

        player.openBook(book);
    }

    void registerHelpBook(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();

        if (file == null) {
            file = new File(Main.getJavaPlugin().getDataFolder(), "helpBook.yml");
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() != Material.WRITTEN_BOOK) {
            SendMessage(player, details(), "このコマンドを使用するには、登録する本を手に持ってください。");
            return;
        }
        BookMeta meta = (BookMeta) item.getItemMeta();
        Component componentTitle = meta.title();
        String title = componentTitle != null ? GsonComponentSerializer.gson().serialize(componentTitle) : null;
        List<String> pages = meta.pages().stream()
            .map(page -> GsonComponentSerializer.gson().serialize(page))
            .collect(Collectors.toList());

        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("title", title);
        yaml.set("pages", pages);
        try {
            yaml.save(file);
            SendMessage(player, details(), "ヘルプブックを登録しました。");
        } catch (IOException e) {
            SendMessage(player, details(), String.format("ヘルプブックが登録できませんでした: %s", e.getMessage()));
            MyMaidLibrary.reportError(getClass(), e);
        }
    }
}
