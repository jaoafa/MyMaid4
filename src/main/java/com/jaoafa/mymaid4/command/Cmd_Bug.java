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
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.meta.CommandMeta;
import com.jaoafa.mymaid4.Main;
import com.jaoafa.mymaid4.lib.CommandPremise;
import com.jaoafa.mymaid4.lib.MyMaidCommand;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import com.jaoafa.mymaid4.lib.NMSManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
import okhttp3.*;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Cmd_Bug extends MyMaidLibrary implements CommandPremise {
    static long sendTime = -1L;

    @Override
    public MyMaidCommand.Detail details() {
        return new MyMaidCommand.Detail(
            "bug",
            "本によるIssueの作成を行います。"
        );
    }

    @Override
    public MyMaidCommand.Cmd register(Command.Builder<CommandSender> builder) {
        return new MyMaidCommand.Cmd(
            builder
                .meta(CommandMeta.DESCRIPTION, "本によるIssueの作成を行います。")
                .senderType(Player.class)
                .handler(this::giveIssueBook)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "本によりIssue作成処理を行います。")
                .senderType(Player.class)
                .literal("true")
                .handler(this::createIssueBook)
                .hidden()
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "記入済みの本を記入可能な本に戻します。")
                .senderType(Player.class)
                .literal("false")
                .handler(this::reWritableBook)
                .hidden()
                .build()
        );
    }

    void giveIssueBook(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        PlayerInventory inv = player.getInventory();

        if (Main.getMyMaidConfig().getGitHubAccessToken() == null) {
            SendMessage(player, details(), "不具合報告に必要な設定情報が見つからなかったため、不具合報告ができません。");
            return;
        }

        ItemStack is = new ItemStack(Material.WRITABLE_BOOK);
        BookMeta book = (BookMeta) is.getItemMeta();
        book = book.title(Component.text("MyMaid4 Issue Book", NamedTextColor.GOLD));
        book.addPages(
            Component.text().append(
                Component.text("*- MyMaid4 Issue Book --*", NamedTextColor.GOLD, TextDecoration.BOLD),
                Component.newline(),
                Component.text("不具合を見つけた際は、"),
                Component.newline(),
                Component.text("この本の次のページから記入して下さい。"),
                Component.newline(),
                Component.newline(),
                Component.text("この本を署名することで、jaoafa/MyMaid4にIssueが作成されます", NamedTextColor.RED, TextDecoration.BOLD),
                Component.newline(),
                Component.text("自治体申請や運営への対応が必要な用件は公式Discordサーバの#supportにてお願いします。")
            ).build(),
            Component.text().append(
                Component.text("## 不具合の説明", NamedTextColor.GOLD, TextDecoration.BOLD),
                Component.newline(),
                Component.newline(),
                Component.text("> 不具合の内容を明確・簡潔に説明してください", NamedTextColor.GRAY),
                Component.newline(),
                Component.newline()
            ).build(),
            Component.text().append(
                Component.text("## 再現手順", NamedTextColor.GOLD, TextDecoration.BOLD),
                Component.newline(),
                Component.newline(),
                Component.text("> バグが発生する手順を示してください", NamedTextColor.GRAY),
                Component.newline(),
                Component.newline()
            ).build(),
            Component.text().append(
                Component.text("## スクリーンショット", NamedTextColor.GOLD, TextDecoration.BOLD),
                Component.newline(),
                Component.newline(),
                Component.text("> エラーメッセージなどのスクリーンショットがある場合はURLを添付して下さい", NamedTextColor.GRAY),
                Component.newline(),
                Component.newline()
            ).build(),
            Component.text().append(
                Component.text("## 追加情報", NamedTextColor.GOLD, TextDecoration.BOLD),
                Component.newline(),
                Component.newline(),
                Component.text("> 何か追加情報がある場合は記載してください", NamedTextColor.GRAY),
                Component.newline(),
                Component.newline()
            ).build()
        );
        is.setItemMeta(book);
        is = NMSManager.setNBTString(is, "MyMaidBugBook", String.valueOf(System.currentTimeMillis()));

        ItemStack main = inv.getItemInMainHand();
        inv.setItemInMainHand(is);
        SendMessage(player, details(), "不具合報告用の本をあなたのメインハンドのアイテムと置きかえました。");
        SendMessage(player, details(), "本を編集し、署名することで不具合を報告できます。");

        if (main.getType() != Material.AIR) {
            if (player.getInventory().firstEmpty() == -1) {
                player.getLocation().getWorld().dropItem(player.getLocation(), main);
                SendMessage(player, details(), "インベントリがいっぱいだったため、既に持っていたアイテムはあなたの足元にドロップしました。");
            } else {
                inv.addItem(main);
            }
        }
    }

    void createIssueBook(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        PlayerInventory inv = player.getInventory();
        ItemStack is = inv.getItemInMainHand();
        if (is.getType() != Material.WRITTEN_BOOK) {
            SendMessage(player, details(), "記入済みの本を手に持ってください。");
            return;
        }

        BookMeta meta = (BookMeta) is.getItemMeta();

        Component rawTitle = meta.title();
        if (rawTitle == null || rawTitle.equals(Component.text("MyMaid4 Issue Book", NamedTextColor.GOLD))) {
            SendMessage(player, details(), "持っている本は対象の本ではないようです。");
            return;
        }

        if (sendTime >= System.currentTimeMillis() - 5L * 60L * 1000L) {
            SendMessage(player, details(), "誰かが報告されてから5分経っていません。繰り返し報告する場合は5分空けてください。");
            return;
        }

        String title = meta.getTitle();
        String author = meta.getAuthor();
        List<Component> pages = meta.pages();
        String body = pages.stream()
            .skip(1)
            .map(o -> ChatColor.stripColor(PlainComponentSerializer.plain().serialize(o)))
            .collect(Collectors.joining("\n")) +
            MessageFormat.format("\n" +
                    "## システム追加情報\n" +
                    "\n" +
                    "- 作成者: [{0} (`{1}`)]({2})\n" +
                    "- 執筆者: {3}",
                player.getName(),
                player.getUniqueId().toString(),
                String.format("https://users.jaoafa.com/%s", player.getUniqueId()),
                author);

        new BukkitRunnable() {
            public void run() {
                String accessToken = Main.getMyMaidConfig().getGitHubAccessToken();
                if (accessToken == null) {
                    SendMessage(player, details(), "不具合報告に必要な設定情報が見つからなかったため、不具合報告ができませんでした。");
                    return;
                }

                String repo = "jaoafa/MyMaid4";
                String url = String.format("https://api.github.com/repos/%s/issues", repo);
                JSONObject json = new JSONObject()
                    .put("title", title)
                    .put("body", body)
                    .put("labels", new JSONArray()
                        .put("\uD83D\uDC1Bbug"));

                try {
                    OkHttpClient client = new OkHttpClient();
                    RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=UTF-8"), json.toString());
                    Request request = new Request.Builder()
                        .url(url)
                        .header("Authorization", String.format("token %s", accessToken))
                        .post(requestBody)
                        .build();
                    JSONObject obj;
                    try (Response response = client.newCall(request).execute()) {
                        if (response.code() != 201) {
                            SendMessage(player, details(), String.format("不具合報告に失敗しました: %d", response.code()));
                            return;
                        }
                        obj = new JSONObject(Objects.requireNonNull(response.body()).string());
                    }

                    int issueNum = obj.getInt("number");
                    SendMessage(player, details(), Component.text().append(
                        Component.text("不具合報告に成功しました。", NamedTextColor.GREEN),
                        Component.text("こちら", NamedTextColor.AQUA, TextDecoration.UNDERLINED)
                            .hoverEvent(HoverEvent.showText(Component.text()))
                            .clickEvent(ClickEvent.openUrl(MessageFormat.format("https://github.com/{0}/issues/{1}", repo, issueNum))),
                        Component.text("から確認できます。", NamedTextColor.GREEN)
                    ).build());
                    SendMessage(player, details(), "不具合の改善報告等は致しかねますので、上記リンク先を定期的にご確認ください。ご報告いただきありがとうございました！");
                    inv.setItemInMainHand(null);
                    sendTime = System.currentTimeMillis();
                } catch (IOException e) {
                    SendMessage(player, details(), String.format("不具合報告に失敗しました: %s", e.getMessage()));
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(Main.getJavaPlugin());
    }

    void reWritableBook(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        PlayerInventory inv = player.getInventory();
        ItemStack is = inv.getItemInMainHand();
        if (is.getType() != Material.WRITTEN_BOOK) {
            SendMessage(player, details(), "記入済みの本を手に持ってください。");
            return;
        }
        BookMeta meta = (BookMeta) is.getItemMeta();
        is.setType(Material.WRITABLE_BOOK);
        is.setItemMeta(meta);
        inv.setItemInMainHand(is);
        SendMessage(player, details(), "記入済みの本を記入可能な本に戻しました。");
    }
}
