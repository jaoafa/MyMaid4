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

package com.jaoafa.mymaid4.lib;

import cloud.commandframework.context.CommandContext;
import com.jaoafa.mymaid4.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import org.bukkit.*;
import org.bukkit.block.data.type.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
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
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * MyMaid全体で利用されるスタティックメソッドをまとめたライブラリクラス
 */
public class MyMaidLibrary {

    // https://github.com/ErdbeerbaerLP/DiscordIntegration-Core/blob/564b32d29605322f927853ee62a6af938a0af7d3/src/main/java/de/erdbeerbaerlp/dcintegration/common/util/MessageUtils.java#L31-L35
    static final Pattern URL_PATTERN = Pattern.compile(
        "([a-z0-9]{2,}://(?:(?:[0-9]{1,3}\\.){3}[0-9]{1,3}|[-\\w_]+\\.[a-z]{2,}?)(?::[0-9]{1,5})?.*?(?=[!\"\u00A7 \n]|$))",
        Pattern.CASE_INSENSITIVE);

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
     * エラーをDiscordのreportチャンネルへ報告します。
     *
     * @param e Throwable
     */
    public static void reportError(Class<?> clazz, Throwable e) {
        Main.getMyMaidLogger().log(Level.WARNING, e.getMessage(), e);

        if (Main.getMyMaidConfig().isDevelopmentServer()) {
            return;
        }

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
        reportChannel.sendMessageEmbeds(embed).queue();
        reportChannel.sendFile(is, "stacktrace.txt").queue();

        if (Main.getRollbar() != null && !Main.getMyMaidConfig().isDevelopmentServer()) {
            Main.getRollbar().critical(e, "Class: " + clazz.getName());
        }
    }

    /**
     * Dateをyyyy/MM/dd HH:mm:ss形式でフォーマットします。
     *
     * @param date フォーマットするDate
     *
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
     *
     * @return フォーマットされた結果文字列
     */
    protected static String sdfTimeFormat(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(date);
    }

    /**
     * 指定された期間内かどうか<br></>
     * http://www.yukun.info/blog/2009/02/java-jsp-gregoriancalendar-period.html
     *
     * @param start 期間の開始
     * @param end   期間の終了
     *
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
     *
     * @return メイン権限グループ名
     */
    public static String getPermissionMainGroup(OfflinePlayer player) {
        if (!Bukkit.getPluginManager().isPluginEnabled("LuckPerms")) {
            return null;
        }
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
     * @param component 送信するメッセージコンポーネント
     */
    public static void sendAM(Component component) {
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            if (!isAM(p)) continue;
            if (MyMaidData.getTempMuting().contains(p)) continue;
            p.sendMessage(component);
        }
    }

    /**
     * Admin・Moderator・Regularにメッセージを送信します。
     *
     * @param component 送信するメッセージコンポーネント
     */
    public static void sendAMR(Component component) {
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            if (!isAMR(p)) continue;
            if (MyMaidData.getTempMuting().contains(p)) continue;
            p.sendMessage(component);
        }
    }

    /**
     * Admin・Moderator・Regular・Verifiedにメッセージを送信します。
     *
     * @param component 送信するメッセージコンポーネント
     */
    public static void sendAMRV(Component component) {
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            if (!isAMRV(p)) continue;
            if (MyMaidData.getTempMuting().contains(p)) continue;
            p.sendMessage(component);
        }
    }

    /**
     * Verified・Defaultにメッセージを送信します。
     *
     * @param component 送信するメッセージコンポーネント
     */
    public static void sendVD(Component component) {
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            if (!isD(p) && !isV(p)) continue;
            if (MyMaidData.getTempMuting().contains(p)) continue;
            p.sendMessage(component);
        }
    }

    /**
     * プレイヤーがAdminであるかを判定します。
     *
     * @param player 判定するプレイヤー
     */
    protected static boolean isA(OfflinePlayer player) {
        String group = getPermissionMainGroup(player);
        if (group == null) return false;
        return group.equalsIgnoreCase("Admin");
    }

    /**
     * プレイヤーがAdmin・Moderatorのいずれかであるかを判定します。
     *
     * @param player 判定するプレイヤー
     */
    public static boolean isAM(OfflinePlayer player) {
        String group = getPermissionMainGroup(player);
        if (group == null) return false;
        return isA(player) || group.equalsIgnoreCase("Moderator");
    }

    /**
     * プレイヤーがAdmin・Moderator・Regularのいずれかであるかを判定します。
     *
     * @param player 判定するプレイヤー
     */
    public static boolean isAMR(OfflinePlayer player) {
        String group = getPermissionMainGroup(player);
        if (group == null) return false;
        return isAM(player) || group.equalsIgnoreCase("Regular");
    }

    /**
     * プレイヤーがAdmin・Moderator・Verifiedのいずれかであるかを判定します。
     *
     * @param player 判定するプレイヤー
     */
    protected static boolean isAMRV(OfflinePlayer player) {
        String group = getPermissionMainGroup(player);
        if (group == null) return false;
        return isAMR(player) || isV(player);
    }

    /**
     * プレイヤーがRegularであるかを判定します。
     *
     * @param player 判定するプレイヤー
     */
    protected static boolean isR(OfflinePlayer player) {
        String group = getPermissionMainGroup(player);
        if (group == null) return false;
        return group.equalsIgnoreCase("Regular");
    }

    /**
     * プレイヤーがVerifiedであるかを判定します。
     *
     * @param player 判定するプレイヤー
     */
    protected static boolean isV(OfflinePlayer player) {
        String group = getPermissionMainGroup(player);
        if (group == null) return false;
        return group.equalsIgnoreCase("Verified");
    }

    /**
     * プレイヤーがDefaultであるかを判定します。
     *
     * @param player 判定するプレイヤー
     */
    protected static boolean isD(OfflinePlayer player) {
        String group = getPermissionMainGroup(player);
        if (group == null) return false;
        return group.equalsIgnoreCase("Default");
    }

    /**
     * 文字列が数値であるかを判定します。
     *
     * @param s 判定する文字列
     *
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
     *
     * @return 判定結果
     */
    protected static boolean isUUID(String s) {
        return s.split("-").length == 5;
    }

    /**
     * 文字列をDiscord用にエスケープします。
     *
     * @param text エスケープする文字列
     *
     * @return エスケープされた文字列
     */
    protected static String DiscordEscape(String text) {
        return text == null ? "" : text.replace("_", "\\_").replace("*", "\\*").replace("~", "\\~");
    }

    /**
     * 4バイトの文字列を含むかどうかを調べます
     *
     * @param str 文字列
     *
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
     *
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
     *
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
    public static void chatFake(TextColor color, String name, String text) {
        chatFake(color, name, text, true);
    }

    /**
     * フェイクのチャットを送信します。
     *
     * @param color         四角色
     * @param name          プレイヤー名
     * @param text          テキスト
     * @param sendToDiscord Discordにも送信するか
     */
    public static void chatFake(TextColor color, String name, String text, boolean sendToDiscord) {
        chatFake(color, name, Component.text(text), sendToDiscord);
    }

    /**
     * フェイクのチャットを送信します。
     *
     * @param color         四角色
     * @param name          プレイヤー名
     * @param component     テキスト
     * @param sendToDiscord Discordにも送信するか
     */
    public static void chatFake(TextColor color, String name, Component component, boolean sendToDiscord) {
        Bukkit.getServer().sendMessage(Component.text().append(
            Component.text("[" + sdfTimeFormat(new Date()) + "]", NamedTextColor.GRAY),
            Component.text("■", color),
            Component.text(name, NamedTextColor.WHITE),
            Component.text(":"),
            Component.space(),
            component
        ));
        String text = PlainTextComponentSerializer.plainText().serialize(component);
        if (sendToDiscord && MyMaidData.getServerChatChannel() != null)
            MyMaidData.getServerChatChannel()
                .sendMessage("**" + DiscordEscape(name) + "**: " + DiscordEscape(ChatColor.stripColor(text)))
                .queue();
    }

    /**
     * フェイクのチャットを取得します。
     *
     * @param color 四角色
     * @param name  プレイヤー名
     * @param text  テキスト
     *
     * @return TextComponent
     */
    public static TextComponent getChatFake(NamedTextColor color, String name, String text) {
        return Component.text().append(
            Component.text("[" + sdfTimeFormat(new Date()) + "]", NamedTextColor.GRAY),
            Component.text("■", color),
            Component.text(name, NamedTextColor.WHITE),
            Component.text(":"),
            Component.space(),
            Component.text(text)
        ).build();
    }

    /**
     * 指定した地点の地面の高さを返す
     *
     * @param loc 地面を探したい場所の座標
     *
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
     * ワールド名のサジェスト
     *
     * @param context CommandContext
     * @param current current String
     *
     * @return 該当するワールド名
     */
    public static List<String> suggestWorldNames(final CommandContext<CommandSender> context, final String current) {
        return Bukkit.getServer().getWorlds().stream()
            .map(World::getName)
            .filter(s -> s.toLowerCase().startsWith(current.toLowerCase()))
            .collect(Collectors.toList());
    }

    /**
     * スパムかどうかチェックを行い、状態に応じてJailします
     *
     * @param player [player's name]
     */
    public static void checkSpam(Player player) {
        if (MyMaidData.getSpamCount(player.getUniqueId()) == null || MyMaidData.getSpamTime(player.getUniqueId()) == null) {
            MyMaidData.setSpamCount(player.getUniqueId(), 1);
            MyMaidData.setSpamTime(player.getUniqueId(), System.currentTimeMillis());
            return;
        }
        int count = MyMaidData.getSpamCount(player.getUniqueId());
        long time = MyMaidData.getSpamTime(player.getUniqueId());

        if (System.currentTimeMillis() - time > 180000) {
            //3分
            MyMaidData.setSpamCount(player.getUniqueId(), 1);
            MyMaidData.setSpamTime(player.getUniqueId(), System.currentTimeMillis());
            return;
        }

        if (count == 2) {
            Jail jail = Jail.getInstance(player);
            if (jail.isStatus()) {
                return;
            }
            jail.addBan("jaotan", "迷惑コマンドを過去3分間に3回以上実行したため");
            return;
        } else if (count == 1) {
            player.sendMessage(String.format("[AntiProblemCommand] %s短時間に複数回にわたる迷惑コマンドが実行された場合、処罰対象となる場合があります。ご注意ください。", ChatColor.GREEN));

        } else {
            player.sendMessage(String.format("[AntiProblemCommand] %sあなたが実行したコマンドは迷惑コマンドとされています。複数回実行すると、迷惑行為として処罰対象となる場合がございます。", ChatColor.GREEN));
        }
        MyMaidData.setSpamCount(player.getUniqueId(), count + 1);
        MyMaidData.setSpamTime(player.getUniqueId(), System.currentTimeMillis());
    }

    public static void debug(String message) {
        if (!Main.getMyMaidConfig().isDevelopmentServer()) {
            return;
        }
        Main.getMyMaidLogger().info("DEBUG -> %s".formatted(message));
    }

    public static NamedTextColor getNamedTextColor(String color) {
        return switch (color.toUpperCase()) {
            case "BLACK" -> NamedTextColor.BLACK;
            case "DARK_BLUE" -> NamedTextColor.DARK_BLUE;
            case "DARK_GREEN" -> NamedTextColor.DARK_GREEN;
            case "DARK_AQUA" -> NamedTextColor.DARK_AQUA;
            case "DARK_RED" -> NamedTextColor.DARK_RED;
            case "DARK_PURPLE" -> NamedTextColor.DARK_PURPLE;
            case "GOLD" -> NamedTextColor.GOLD;
            case "GRAY" -> NamedTextColor.GRAY;
            case "DARK_GRAY" -> NamedTextColor.DARK_GRAY;
            case "BLUE" -> NamedTextColor.BLUE;
            case "GREEN" -> NamedTextColor.GREEN;
            case "AQUA" -> NamedTextColor.AQUA;
            case "RED" -> NamedTextColor.RED;
            case "LIGHT_PURPLE" -> NamedTextColor.LIGHT_PURPLE;
            case "YELLOW" -> NamedTextColor.YELLOW;
            case "WHITE" -> NamedTextColor.WHITE;
            default -> null;
        };
    }

    @Nullable
    public static NamedTextColor getNamedTextColor(ChatColor color) {
        return getNamedTextColor(color.name());
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
            component
                .replaceText(builder ->
                    builder
                        .match("\n")
                        .replacement("\n" + "[" + detail.getName().toUpperCase() + "] ")
                )
                .colorIfAbsent(NamedTextColor.GREEN)
        ).build());
    }

    public static Component replaceComponentURL(Component component) {
        return component.replaceText(TextReplacementConfig.builder()
            .match(URL_PATTERN)
            .replacement(url -> url
                .decorate(TextDecoration.UNDERLINED)
                .hoverEvent(HoverEvent.showText(Component.text("クリックすると「" + url.content() + "」にアクセスします。")))
                .clickEvent(ClickEvent.openUrl(url.content()))).build());
    }

    public static boolean isSign(Material material) {
        return Arrays.stream(Material.values())
            .filter(m -> m.data == Sign.class || m.data == WallSign.class)
            .anyMatch(m -> m == material);
    }

    /**
     * Locationオブジェクトを「ワールド X Y Z」の文字列形式で返します。
     *
     * @param loc Locationオブジェクト
     *
     * @return 「ワールド X Y Z」の文字列形式
     */
    public static String formatLocation(Location loc) {
        return loc.getWorld().getName() + " " + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ();
    }

    /**
     * プレイヤーを南の楽園にテレポートさせます。
     *
     * @param player テレポートするプレイヤー
     *
     * @return テレポートに成功したかどうか
     *
     * @throws IllegalStateException Jao_Afaワールドが存在しなかった場合
     */
    public static boolean teleportToParadise(Player player) {
        if (Bukkit.getWorld("Jao_Afa") == null)
            throw new IllegalStateException("World:Jao_Afa Not Found!");

        return player.teleport(
            MyMaidData.paradiseLocation,
            PlayerTeleportEvent.TeleportCause.PLUGIN
        );
    }

    /**
     * 指定されたLocationに一番近いプレイヤーを取得します。
     *
     * @param loc Location
     *
     * @return 一番近いプレイヤー
     */
    @Nullable
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
     * オンラインプレイヤーのサジェスト (これではなくPlayerArgumentを使うことをお勧め)
     *
     * @param context CommandContext
     * @param current current String
     *
     * @return 該当するプレイヤー
     */
    public List<String> suggestOnlinePlayers(final CommandContext<CommandSender> context, final String current) {
        return Bukkit.getServer().getOnlinePlayers().stream()
            .map(Player::getName)
            .filter(s -> s.toLowerCase().startsWith(current.toLowerCase()))
            .collect(Collectors.toList());
    }

    /**
     * オフラインプレイヤーのサジェスト
     *
     * @param context CommandContext
     * @param current current String
     *
     * @return 該当するプレイヤー
     */
    public List<String> suggestOfflinePlayers(final CommandContext<CommandSender> context, final String current) {
        return Arrays.stream(Bukkit.getServer().getOfflinePlayers())
            .map(OfflinePlayer::getName)
            .filter(Objects::nonNull)
            .filter(s -> s.toLowerCase().startsWith(current.toLowerCase()))
            .collect(Collectors.toList());
    }

    /**
     * プラグインが有効であるかどうかを取得します。
     *
     * @return プラグインが有効であるか
     */
    protected boolean isDisabledPlugin(String pluginName) {
        Plugin plugin = Main.getJavaPlugin().getServer().getPluginManager().getPlugin(pluginName);
        return plugin == null || !plugin.isEnabled();
    }

    protected boolean getLookingAt(Player player, Player target) {
        Location eye = player.getEyeLocation();
        Vector toEntity = target.getEyeLocation().toVector().subtract(eye.toVector());
        double dot = toEntity.normalize().dot(eye.getDirection());

        return dot > 0.99D;
    }

    protected boolean isEntityLooking(Player player, Entity target) {
        Location eye = player.getEyeLocation();
        Location location;
        if (target instanceof LivingEntity) {
            location = ((LivingEntity) target).getEyeLocation();
        } else {
            location = target.getLocation();
        }
        Vector toEntity = location.toVector().subtract(eye.toVector());
        double dot = toEntity.normalize().dot(eye.getDirection());

        return dot > 0.99D;
    }

    /**
     * jaoiumと判定されるアイテムかどうか
     *
     * @param list PotionEffectのList
     *
     * @return jaoiumかどうか
     */
    public boolean isjaoium(List<PotionEffect> list) {
        boolean jaoium = false;
        for (PotionEffect po : list) {
            if (po.getType().equals(PotionEffectType.HEAL)) {
                if (Arrays.asList(
                    29,
                    61,
                    93,
                    125
                ).contains(po.getAmplifier())) {
                    // アウト
                    jaoium = true;
                }
            }
        }
        return jaoium;
    }

    public void wrapGetAchievement(Player player, String achievement) {

    }
}
