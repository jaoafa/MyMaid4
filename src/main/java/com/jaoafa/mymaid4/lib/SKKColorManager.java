package com.jaoafa.mymaid4.lib;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
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
                PreparedStatement stmt = connection.prepareStatement("SELECT * FROM vote WHERE uuid = ?");
                stmt.setString(1, player.getUniqueId().toString());
                ResultSet resultSet = stmt.executeQuery();
                if (!resultSet.next()) {
                    return null;
                }
                i = resultSet.getInt("count");
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

    public static String replacePlayerSKKChatColor(Player player, String oldstr, String _Message) {
        String Message = _Message.replaceFirst(oldstr, String.format("%s■%s%s", getPlayerColor(player), ChatColor.WHITE, oldstr));
        return Message;
    }


    private static List<String> MessageList() {
        List<String> MessageList = Arrays.asList(
            "the New Generation"
            , "- Super"
            , "Hyper"
            , "Ultra"
            , "Extreme"
            , "Insane"
            , "Gigantic"
            , "Epic"
            , "Amazing"
            , "Beautiful"
            , "Special"
            , "Swag"
            , "Lunatic"
            , "Exotic"
            , "God"
            , "Hell"
            , "Heaven"
            , "Mega"
            , "Giga"
            , "Tera"
            , "Refined"
            , "Sharp"
            , "Strong"
            , "Muscle"
            , "Macho"
            , "Bomber"
            , "Blazing"
            , "Frozen"
            , "Legendary"
            , "Mystical"
            , "Tactical"
            , "Critical"
            , "Overload"
            , "Overclock"
            , "Fantastic"
            , "Criminal"
            , "Primordial"
            , "Genius"
            , "Great"
            , "Perfect"
            , "Fearless"
            , "Ruthless"
            , "Bold"
            , "Void"
            , "Millenium"
            , "Exact"
            , "Really"
            , "Certainty"
            , "Infernal"
            , "Ender"
            , "World"
            , "Mad"
            , "Crazy"
            , "Wrecked"
            , "Elegant"
            , "Expensive"
            , "Rich"
            , "Radioactive"
            , "Automatic"
            , "Honest"
            , "Cosmic"
            , "Galactic"
            , "Dimensional"
            , "Sinister"
            , "Evil"
            , "Abyssal"
            , "Hallowed"
            , "Holy"
            , "Sacred"
            , "Omnipotent"
        );


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
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM vote WHERE uuid = ?");
            stmt.setString(1, player.getUniqueId().toString());
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                i = resultSet.getInt("count");
            }
        } catch (SQLException throwables) {
            //MyMaidLibrary.reportError(throwables);
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
        return String.format("%s%s%s, %s%s joined the game.", ChatColor.YELLOW, player.getName(), ChatColor.YELLOW, ChatColor.YELLOW, result);
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
