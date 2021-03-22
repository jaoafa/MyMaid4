package com.jaoafa.mymaid4.lib;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

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
        String Message = _Message.replaceFirst(oldstr, getPlayerColor(player) + "■" + ChatColor.WHITE + oldstr.replace("<", "").replace(">", ":"));
        return Message;
    }


    private static List<String> MessageList() {
        List<String> MessageList = new ArrayList<String>();
        MessageList.add("the New Generation");
        MessageList.add("- Super");
        MessageList.add("Hyper");
        MessageList.add("Ultra");
        MessageList.add("Extreme");
        MessageList.add("Insane");
        MessageList.add("Gigantic");
        MessageList.add("Epic");
        MessageList.add("Amazing");
        MessageList.add("Beautiful");
        MessageList.add("Special");
        MessageList.add("Swag");
        MessageList.add("Lunatic");
        MessageList.add("Exotic");
        MessageList.add("God");
        MessageList.add("Hell");
        MessageList.add("Heaven");
        MessageList.add("Mega");
        MessageList.add("Giga");
        MessageList.add("Tera");
        MessageList.add("Refined");
        MessageList.add("Sharp");
        MessageList.add("Strong");
        MessageList.add("Muscle");
        MessageList.add("Macho");
        MessageList.add("Bomber");
        MessageList.add("Blazing");
        MessageList.add("Frozen");
        MessageList.add("Legendary");
        MessageList.add("Mystical");
        MessageList.add("Tactical");
        MessageList.add("Critical");
        MessageList.add("Overload");
        MessageList.add("Overclock");
        MessageList.add("Fantastic");
        MessageList.add("Criminal");
        MessageList.add("Primordial");
        MessageList.add("Genius");
        MessageList.add("Great");
        MessageList.add("Perfect");
        MessageList.add("Fearless");
        MessageList.add("Ruthless");
        MessageList.add("Bold");
        MessageList.add("Void");
        MessageList.add("Millenium");
        MessageList.add("Exact");
        MessageList.add("Really");
        MessageList.add("Certainty");
        MessageList.add("Infernal");
        MessageList.add("Ender");
        MessageList.add("World");
        MessageList.add("Mad");
        MessageList.add("Crazy");
        MessageList.add("Wrecked");
        MessageList.add("Elegant");
        MessageList.add("Expensive");
        MessageList.add("Rich");
        MessageList.add("Radioactive");
        MessageList.add("Automatic");
        MessageList.add("Honest");
        MessageList.add("Cosmic");
        MessageList.add("Galactic");
        MessageList.add("Dimensional");
        MessageList.add("Sinister");
        MessageList.add("Evil");
        MessageList.add("Abyssal");
        MessageList.add("Hallowed");
        MessageList.add("Holy");
        MessageList.add("Sacred");
        MessageList.add("Omnipotent");

        return MessageList;
    }

    public static String getPlayerSKKJoinMessage(Player player) {
        //try{
            /*String group = PermissionsManager.getPermissionMainGroup(player);
            if(group.equalsIgnoreCase("Limited")){
                return ChatColor.RED + player.getName() + ChatColor.YELLOW + " joined the game.";
            }*/
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
        String result = "";
        if (i < 20) {
            return null;
        } else if (i < 24) {
            result = "VIP";
        } else {
            double vote_double = i / 4;
            vote_double = vote_double - 5;
            int vote = (int) Math.floor(vote_double);
            int o = 0;
            while (vote > 0) {
                if (MessageList().size() <= o) {
                    break;
                }
                if (!result.equalsIgnoreCase("")) {
                    result += " ";
                }
                result += MessageList().get(o);
                vote--;
                o++;
            }
            result += String.format(" VIP (%d)", i);
        }
        return ChatColor.YELLOW + player.getName() + ChatColor.YELLOW + ", " + ChatColor.YELLOW + result + " joined the game.";
        /*}catch(ClassNotFoundException | SQLException e){
            return ChatColor.YELLOW + player.getName() + ChatColor.YELLOW + ", " + ChatColor.YELLOW + player.getName() + " joined the game.";
        }*/
    }

    public static String getPlayerSKKTabListString(Player player) {
        Team team = Bukkit.getServer().getScoreboardManager().getMainScoreboard().getPlayerTeam(player);
        if (team == null) {
            return getPlayerColor(player) + "■" + ChatColor.RESET + player.getName();
        } else {
            return getPlayerColor(player) + "■" + ChatColor.RESET + team.getPrefix() + player.getName();
        }

    }

    public static void setPlayerSKKTabList(Player player) {
        player.setPlayerListName(getPlayerSKKTabListString(player));
    }


}
