package com.jaoafa.mymaid4.lib;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SKKColorManager {
    private static ChatColor getPlayerColor(Player player) {
        List<ChatColor> ColorList = new ArrayList<>();
        ColorList.add(ChatColor.WHITE);
        ColorList.add(ChatColor.DARK_BLUE);
        ColorList.add(ChatColor.BLUE);
        ColorList.add(ChatColor.AQUA);
        ColorList.add(ChatColor.DARK_AQUA);
        ColorList.add(ChatColor.DARK_GREEN);
        ColorList.add(ChatColor.GREEN);
        ColorList.add(ChatColor.YELLOW);
        ColorList.add(ChatColor.GOLD);
        ColorList.add(ChatColor.RED);
        ColorList.add(ChatColor.DARK_RED);
        ColorList.add(ChatColor.DARK_PURPLE);
        ColorList.add(ChatColor.LIGHT_PURPLE);

        if (MyMaidData.isMainDBActive()) {
            int i = 0;
            try {
                Connection connection = MyMaidData.getMainMySQLDBManager().getConnection();
                ResultSet resultSet = connection.prepareStatement("SELECT * FROM vote WHERE `uuid`='" + player.getUniqueId() + "'").executeQuery();
                while (resultSet.next()) {
                    i = resultSet.getInt("count");
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            if (i >= 0 && i <= 5) {
                return ChatColor.WHITE;
            } else if (i >= 6 && i <= 19) {
                return ChatColor.DARK_BLUE;
            } else if (i >= 20 && i <= 33) {
                return ChatColor.BLUE;
            } else if (i >= 34 && i <= 47) {
                return ChatColor.AQUA;
            } else if (i >= 48 && i <= 61) {
                return ChatColor.DARK_AQUA;
            } else if (i >= 62 && i <= 76) {
                return ChatColor.DARK_GREEN;
            } else if (i >= 77 && i <= 89) {
                return ChatColor.GREEN;
            } else if (i >= 90 && i <= 103) {
                return ChatColor.YELLOW;
            } else if (i >= 104 && i <= 117) {
                return ChatColor.GOLD;
            } else if (i >= 118 && i <= 131) {
                return ChatColor.RED;
            } else if (i >= 132 && i <= 145) {
                return ChatColor.DARK_RED;
            } else if (i >= 146 && i <= 159) {
                return ChatColor.DARK_PURPLE;
            } else if (i >= 160) {
                return ChatColor.LIGHT_PURPLE;
            }
        }
        return null;
    }

    public static String ReplacePlayerSKKChatColor(Player player, String oldstr, String _Message) {
        String Message = _Message.replaceFirst(oldstr, getPlayerColor(player) + "■" + ChatColor.WHITE + oldstr);
        return Message;
    }

}
