package com.jaoafa.mymaid4.lib;

import cloud.commandframework.context.CommandContext;
import com.jaoafa.mymaid4.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * MyMaid全体で利用されるスタティックメソッドをまとめたライブラリクラス
 */
public class MyMaidLibrary {
    /**
     * CommandSenderに対してメッセージを送信します。
     *
     * @param sender  CommandSender
     * @param detail  MyMaidCommand.Detail
     * @param message メッセージ
     */
    public static void SendMessage(CommandSender sender, MyMaidCommand.Detail detail, String message) {
        sender.sendMessage(Component.text().append(
            Component.text("[" + detail.getName().toUpperCase() + "]"),
            Component.space(),
            Component.text(message).style(Style.style(NamedTextColor.GREEN))
        ).build());
    }

    /**
     * CommandSenderに対してメッセージを送信します。
     *
     * @param sender    CommandSender
     * @param detail    MyMaidCommand.Detail
     * @param component メッセージComponent
     */
    public static void SendMessage(CommandSender sender, MyMaidCommand.Detail detail, Component component) {
        sender.sendMessage(Component.text().append(
            Component.text("[" + detail.getName().toUpperCase() + "]"),
            Component.space(),
            component.replaceText(builder -> builder.match("\n").replacement("\n" + "[" + detail.getName().toUpperCase() + "] "))
        ).build());
    }

    /**
     * エラーをDiscordのreportチャンネルへ報告します。
     *
     * @param e Throwable
     */
    public static void reportError(Class<?> clazz, Throwable e) {
        e.printStackTrace();

        TextChannel reportChannel = MyMaidData.getReportChannel();
        if (reportChannel == null) {
            return;
        }
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        pw.flush();
        String details = sw.toString();
        InputStream is = new ByteArrayInputStream(details.getBytes(StandardCharsets.UTF_8));

        MessageEmbed embed = new EmbedBuilder()
            .setTitle("MyMaid4 Error Reporter")
            .addField("Summary", String.format("%s (%s)", e.getMessage(), e.getClass().getName()), false)
            .addField("Details", details.substring(0, 1000), false)
            .addField("Class", clazz.getName(), false)
            .setColor(Color.RED)
            .setFooter(String.format("MyMaid4 %s", Main.getJavaPlugin().getDescription().getVersion()))
            .build();
        reportChannel.sendMessage(embed).queue();
        reportChannel.sendFile(is, "stacktrace.txt").queue();
    }

    /**
     * Dateをyyyy/MM/dd HH:mm:ss形式でフォーマットします。
     *
     * @param date フォーマットするDate
     * @return フォーマットされた結果文字列
     */
    public static String sdfFormat(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        return sdf.format(date);
    }


    /**
     * DateをHH:mm:ss形式でフォーマットします。
     *
     * @param date フォーマットするDate
     * @return フォーマットされた結果文字列
     */
    private static String sdfTimeFormat(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(date);
    }

    /**
     * 指定された期間内かどうか<br></>
     * http://www.yukun.info/blog/2009/02/java-jsp-gregoriancalendar-period.html
     *
     * @param start 期間の開始
     * @param end   期間の終了
     * @return 期間内ならtrue、期間外ならfalse
     */
    public static boolean isPeriod(Date start, Date end) {
        Date now = new Date();
        if (now.after(start)) return now.before(end);

        return false;
    }

    /**
     * 指定されたプレイヤーのメイン権限グループを取得します。
     *
     * @param player プレイヤー名
     * @return メイン権限グループ名
     */
    public static String getPermissionMainGroup(OfflinePlayer player) {
        LuckPerms LPApi = LuckPermsProvider.get();
        User LP_Player = LPApi.getUserManager().getUser(player.getUniqueId());
        if (LP_Player == null) {
            return null;
        }
        String groupName = LP_Player.getPrimaryGroup();
        Group group = LPApi.getGroupManager().getGroup(groupName);
        if (group == null) return null;
        return group.getFriendlyName();
    }

    /**
     * Admin・Moderatorにメッセージを送信します。
     *
     * @param str 送信するメッセージ文字列
     */
    public static void sendAM(String str) {
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            String group = getPermissionMainGroup(p);
            if (!isAM(p)) continue;
            p.sendMessage(str);
        }
    }

    /**
     * Admin・Moderator・Regularにメッセージを送信します。
     *
     * @param str 送信するメッセージ文字列
     */
    public static void sendAMR(String str) {
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            String group = getPermissionMainGroup(p);
            if (!isAMR(p)) continue;
            p.sendMessage(str);
        }
    }

    /**
     * Admin・Moderator・Regular・Verifiedにメッセージを送信します。
     *
     * @param str 送信するメッセージ文字列
     */
    public static void sendAMRV(String str) {
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            if (!isAMRV(p)) continue;
            p.sendMessage(str);
        }
    }

    /**
     * プレイヤーがAdminであるかを判定します。
     *
     * @param player 判定するプレイヤー
     */
    protected static boolean isA(Player player) {
        String group = getPermissionMainGroup(player);
        if (group == null) return false;
        return group.equalsIgnoreCase("Admin");
    }

    /**
     * プレイヤーがAdmin・Moderatorのいずれかであるかを判定します。
     *
     * @param player 判定するプレイヤー
     */
    public static boolean isAM(Player player) {
        String group = getPermissionMainGroup(player);
        if (group == null) return false;
        return isA(player) || group.equalsIgnoreCase("Moderator");
    }

    /**
     * プレイヤーがAdmin・Moderator・Regularのいずれかであるかを判定します。
     *
     * @param player 判定するプレイヤー
     */
    public static boolean isAMR(Player player) {
        String group = getPermissionMainGroup(player);
        if (group == null) return false;
        return isAM(player) || group.equalsIgnoreCase("Regular");
    }

    /**
     * プレイヤーがAdmin・Moderator・Verifiedのいずれかであるかを判定します。
     *
     * @param player 判定するプレイヤー
     */
    protected static boolean isAMRV(Player player) {
        String group = getPermissionMainGroup(player);
        if (group == null) return false;
        return isAMR(player) || group.equalsIgnoreCase("Verified");
    }

    /**
     * 文字列が数値であるかを判定します。
     *
     * @param s 判定する文字列
     * @return 判定結果
     */
    protected static boolean isInt(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 文字列がUUIDとして正しいか判定します。
     *
     * @param s 判定する文字列
     * @return 判定結果
     */
    protected static boolean isUUID(String s) {
        return s.split("-").length == 5;
    }

    /**
     * 文字列をDiscord用にエスケープします。
     *
     * @param text エスケープする文字列
     * @return エスケープされた文字列
     */
    protected static String DiscordEscape(String text) {
        return text == null ? "" : text.replace("_", "\\_").replace("*", "\\*").replace("~", "\\~");
    }

    /**
     * 4バイトの文字列を含むかどうかを調べます
     *
     * @param str 文字列
     * @return 含むならtrue
     */
    protected static boolean check4bytechars(String str) {
        Pattern pattern = Pattern.compile(".*([^\\u0000-\\uFFFF]).*");
        Matcher m = pattern.matcher(str);
        return m.matches();
    }

    /**
     * 4バイトの文字列を含むかどうかを調べ、含んでいればその文字列を返します。
     *
     * @param str 文字列
     * @return 含むならその文字列、そうでなければnull
     */
    protected static String check4bytechars_MatchText(String str) {
        Pattern pattern = Pattern.compile(".*([^\\u0000-\\uFFFF]).*");
        Matcher m = pattern.matcher(str);
        if (m.matches()) return m.group(1);

        return null;
    }

    /**
     * 4バイトの文字列を含むかどうかを調べ、含んでいればその文字列を消したものを返します。
     *
     * @param str 文字列
     * @return 含む場合消した文字列、そうでない場合入力された文字列
     */
    protected static String check4bytechars_DeleteMatchText(String str) {
        Pattern pattern = Pattern.compile("([^\\u0000-\\uFFFF]+)");
        return pattern.matcher(str).replaceAll("");
    }

    /**
     * フェイクのチャットを送信します。
     *
     * @param color 四角色
     * @param name  プレイヤー名
     * @param text  テキスト
     */
    public static void chatFake(ChatColor color, String name, String text) {
        // TODO Componentに修正する
        Bukkit.broadcastMessage(ChatColor.GRAY + "[" + sdfTimeFormat(new Date()) + "]" + color + "■" + ChatColor.WHITE + name + ": " + text);
        if (MyMaidData.getServerChatChannel() != null)
            MyMaidData.getServerChatChannel()
                .sendMessage("**" + DiscordEscape(name) + "**: " + DiscordEscape(ChatColor.stripColor(text)))
                .queue();
    }

    /**
     * 指定した地点の地面の高さを返す
     *
     * @param loc 地面を探したい場所の座標
     * @return 地面の高さ（Y座標）
     * <p>
     * http://www.jias.jp/blog/?57
     */
    protected static int getGroundPos(Location loc) {
        // 最も高い位置にある非空気ブロックを取得
        loc = loc.getWorld().getHighestBlockAt(loc).getLocation();

        // 最後に見つかった地上の高さ
        int ground = loc.getBlockY();

        // 下に向かって探索
        for (int y = loc.getBlockY(); y != 0; y--) {
            // 座標をセット
            loc.setY(y);

            // そこは太陽光が一定以上届く場所で、非固体ブロックで、ひとつ上も非固体ブロックか
            // 地上の高さとして記憶しておく
            if (loc.getBlock().getLightFromSky() >= 8
                && !loc.getBlock().getType().isSolid()
                && !loc.clone().add(0, 1, 0).getBlock().getType().isSolid()) ground = y;
        }

        // 地上の高さを返す
        return ground;
    }

    /**
     * 指定されたLocationに一番近いプレイヤーを取得します。
     *
     * @param loc Location
     * @return 一番近いプレイヤー
     */
    public Player getNearestPlayer(Location loc) {
        double closest = Double.MAX_VALUE;
        Player closestp = null;
        for (Player i : loc.getWorld().getPlayers()) {
            double dist = i.getLocation().distance(loc);
            if (closest == Double.MAX_VALUE || dist < closest) {
                closest = dist;
                closestp = i;
            }
        }
        return closestp;
    }

    /**
     * オフラインプレイヤーのサジェスト
     *
     * @param context CommandContext
     * @param current current String
     * @return 該当するプレイヤー
     */
    public List<String> suggestOfflinePlayers(final CommandContext<CommandSender> context, final String current) {
        return Arrays.stream(Bukkit.getServer().getOfflinePlayers())
            .map(OfflinePlayer::getName)
            .filter(Objects::nonNull)
            .filter(s -> s.toLowerCase().startsWith(current.toLowerCase()))
            .collect(Collectors.toList());
    }
}
