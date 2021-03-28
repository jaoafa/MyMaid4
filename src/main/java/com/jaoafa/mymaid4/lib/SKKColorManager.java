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

package com.jaoafa.mymaid4.lib;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SKKColorManager {
    static List<ChatColor> ChatColorList = Arrays.asList(
        ChatColor.GRAY,
        ChatColor.WHITE,
        ChatColor.DARK_BLUE,
        ChatColor.BLUE,
        ChatColor.AQUA,
        ChatColor.DARK_AQUA,
        ChatColor.DARK_GREEN,
        ChatColor.GREEN,
        ChatColor.YELLOW,
        ChatColor.GOLD,
        ChatColor.RED,
        ChatColor.DARK_RED,
        ChatColor.DARK_PURPLE,
        ChatColor.LIGHT_PURPLE);
    static List<NamedTextColor> TextColorList = Arrays.asList(
        NamedTextColor.GRAY,
        NamedTextColor.WHITE,
        NamedTextColor.DARK_BLUE,
        NamedTextColor.BLUE,
        NamedTextColor.AQUA,
        NamedTextColor.DARK_AQUA,
        NamedTextColor.DARK_GREEN,
        NamedTextColor.GREEN,
        NamedTextColor.YELLOW,
        NamedTextColor.GOLD,
        NamedTextColor.RED,
        NamedTextColor.DARK_RED,
        NamedTextColor.DARK_PURPLE,
        NamedTextColor.LIGHT_PURPLE);
    static List<String> JoinMessages = Arrays.asList(
        "the New Generation", "- Super", "Hyper", "Ultra", "Extreme", "Insane", "Gigantic", "Epic", "Amazing", "Beautiful",
        "Special", "Swag", "Lunatic", "Exotic", "God", "Hell", "Heaven", "Mega", "Giga", "Tera", "Refined", "Sharp",
        "Strong", "Muscle", "Macho", "Bomber", "Blazing", "Frozen", "Legendary", "Mystical", "Tactical", "Critical",
        "Overload", "Overclock", "Fantastic", "Criminal", "Primordial", "Genius", "Great", "Perfect", "Fearless",
        "Ruthless", "Bold", "Void", "Millenium", "Exact", "Really", "Certainty", "Infernal", "Ender", "World", "Mad",
        "Crazy", "Wrecked", "Elegant", "Expensive", "Rich", "Radioactive", "Automatic", "Honest", "Cosmic", "Galactic",
        "Dimensional", "Sinister", "Evil", "Abyssal", "Hallowed", "Holy", "Sacred", "Omnipotent"
    );

    private static int getVoteCount(Player player) {
        if (!MyMaidData.isMainDBActive()) {
            return 0;
        }
        try {
            Connection connection = MyMaidData.getMainMySQLDBManager().getConnection();
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM vote WHERE uuid = ?");
            stmt.setString(1, player.getUniqueId().toString());
            ResultSet res = stmt.executeQuery();
            if (!res.next()) {
                return 0;
            }
            return res.getInt("count");
        } catch (SQLException e) {
            MyMaidLibrary.reportError(SKKColorManager.class, e);
            return 0;
        }
    }

    /**
     * プレイヤーの四角色を取得する
     *
     * @param player プレイヤー
     *
     * @return 四角色
     */
    public static ChatColor getPlayerChatColor(Player player) {
        int count = getVoteCount(player);
        return ChatColorList.get(calculateRank(count));
    }

    public static TextColor getPlayerTextColor(Player player) {
        int count = getVoteCount(player);
        return TextColorList.get(calculateRank(count));
    }

    /**
     * ランク数値を取得する
     *
     * @param vote_count 投票数
     *
     * @return ランク数値 (0 <= n <= 13)
     */
    static int calculateRank(int vote_count) {
        if (vote_count == 0)
            return 0;
        if (vote_count <= 5)
            return 1;
        if (vote_count >= 160)
            return 13;
        return (vote_count - 5) / 14 + 1;
    }

    /**
     * プレイヤー名の前に四角色を追加する
     *
     * @param player  プレイヤー
     * @param str     置き換えるテキストパラメーター
     * @param message フォーマットテキスト
     * @return 追加した後のテキスト
     */
    public static String replacePlayerSKKChatColor(Player player, String str, String message) {
        //player.sendMessage("str:"+str);
        //player.sendMessage("msg:"+message);
        return message.replaceFirst(str, getPlayerTextColor(player) + "■" + ChatColor.WHITE + str);
        //return message.replaceFirst(str, String.format("%s■%s%s", getPlayerColor(player), ChatColor.WHITE, str));
    }

    private static String getJoinMessage(int count) {
        if (count < 20) {
            return null;
        } else if (count < 24) {
            return "VIP";
        } else {
            int _count = count;
            _count /= 4;
            _count -= 5;
            _count = (int) Math.floor(_count);

            return "the New Generation " + JoinMessages.stream().limit(_count).collect(Collectors.joining(" ")) + " VIP";
        }
    }

    public static Component getPlayerSKKJoinMessage(Player player) {
        int count = getVoteCount(player);
        if (count < 20) {
            return Component.text().append(
                Component.text(player.getName()),
                Component.space(),
                Component.text("joined the game.")
            ).color(NamedTextColor.GREEN).build();
        }
        String rankText = getJoinMessage(count);
        return Component.text(
            String.format("%s, %s (%d) joined the game.",
                player.getName(),
                rankText,
                count),
            NamedTextColor.YELLOW);
    }

    public static Component getPlayerSKKTabListComponent(Player player) {
        Team team = Bukkit.getServer().getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName());

        return team == null ?
            Component.text().append(
                Component.text("■").color(getPlayerTextColor(player)),
                Component.text(player.getName())
            ).build() :
            Component.text().append(
                Component.text("■").color(getPlayerTextColor(player)),
                Component.text(player.getName(), team.color())
            ).build();
    }

    public static void setPlayerSKKTabList(Player player) {
        player.playerListName(getPlayerSKKTabListComponent(player));
    }
}
