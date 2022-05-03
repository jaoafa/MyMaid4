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

package com.jaoafa.mymaid4.event;

import com.jaoafa.jaosuperachievement2.api.Achievementjao;
import com.jaoafa.jaosuperachievement2.lib.Achievement;
import com.jaoafa.mymaid4.Main;
import com.jaoafa.mymaid4.lib.*;
import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;
import net.dv8tion.jda.api.entities.TextChannel;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class Event_Vote extends MyMaidLibrary implements Listener, EventPremise {
    @Override
    public String description() {
        return "各サーバリストサイトからの投票通知を受け取り、処理します。";
    }

    @EventHandler
    public void onVotifierEvent(VotifierEvent event) {
        Vote vote = event.getVote();
        String name = vote.getUsername();
        String service = vote.getServiceName();
        Main.getMyMaidLogger().info("onVotifierEvent[MyMaid4]: " + vote.getUsername() + " " + vote.getAddress() + " "
            + vote.getServiceName() + " " + vote.getTimeStamp());
        new BukkitRunnable() {
            public void run() {
                if (service.equalsIgnoreCase("minecraft.jp")) {
                    VoteReceive(name);
                } else if (service.equalsIgnoreCase("monocraft.net")) {
                    VoteReceiveMonocraftNet(name);
                }
            }
        }.runTaskAsynchronously(Main.getJavaPlugin());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        new BukkitRunnable() {
            public void run() {
                boolean notVoted = false;
                PlayerVoteDataMCJP mcjp = new PlayerVoteDataMCJP(player);
                if (!mcjp.isVoted()) {
                    player.sendMessage(Component.join(JoinConfiguration.noSeparators(),
                        Component.text("[Vote]"),
                        Component.space(),
                        Component.text("まだ minecraft.jp で投票していないようです。", NamedTextColor.GREEN),
                        Component.text("こちら", NamedTextColor.GREEN, TextDecoration.UNDERLINED)
                            .hoverEvent(HoverEvent.showText(Component.text("「jaoafa.com/vote」をブラウザで開きます。", NamedTextColor.AQUA)))
                            .clickEvent(ClickEvent.openUrl("https://jaoafa.com/vote")),
                        Component.text("から投票をお願いします！")));
                    notVoted = true;
                }

                PlayerVoteDataMono mono = new PlayerVoteDataMono(player);
                if (!mono.isVoted()) {
                    player.sendMessage(Component.join(JoinConfiguration.noSeparators(),
                        Component.text("[Vote]"),
                        Component.space(),
                        Component.text("まだ monocraft.net で投票していないようです。", NamedTextColor.GREEN),
                        Component.text("こちら", NamedTextColor.GREEN, TextDecoration.UNDERLINED)
                            .hoverEvent(HoverEvent.showText(Component.text("「jaoafa.com/monovote」をブラウザで開きます。", NamedTextColor.AQUA)))
                            .clickEvent(ClickEvent.openUrl("https://jaoafa.com/monovote")),
                        Component.text("から投票をお願いします！")));
                    notVoted = true;
                }

                if (notVoted) {
                    player.sendMessage(Component.join(JoinConfiguration.noSeparators(),
                        Component.text("[Vote]"),
                        Component.space(),
                        Component.text("投票についての解説は", NamedTextColor.GREEN),
                        Component.text("こちら", NamedTextColor.GREEN, TextDecoration.UNDERLINED)
                            .hoverEvent(HoverEvent.showText(Component.text("「jaoafa.com/blog/how-to-vote」をブラウザで開きます。", NamedTextColor.AQUA)))
                            .clickEvent(ClickEvent.openUrl("https://jaoafa.com/blog/how-to-vote")),
                        Component.text("からご覧ください。")));
                }
            }
        }.runTaskAsynchronously(Main.getJavaPlugin());
    }

    void VoteReceiveMonocraftNet(String name) {
        MySQLDBManager MySQLDBManager = MyMaidData.getMainMySQLDBManager();
        if (MySQLDBManager == null) {
            missedNotifyMonocraftNet(name, "MySQLDBManager == null");
            return;
        }
        // nameからuuidを取得する
        UUID uuid = getUUID(MySQLDBManager, name);
        if (uuid == null) {
            missedNotifyMonocraftNet(name, "UUID取得失敗");
            return;
        }

        OfflinePlayer offplayer = Bukkit.getOfflinePlayer(uuid);

        if (offplayer.getName() == null) {
            missedNotifyMonocraftNet(name, "OfflinePlayer取得失敗");
            return;
        }

        if (!offplayer.getName().equals(name)) {
            name += "(" + offplayer.getName() + ")";
        }

        int oldVote;
        int newVote;
        boolean isTodayFirst;
        try {
            PlayerVoteDataMono pvd = new PlayerVoteDataMono(offplayer);
            oldVote = pvd.getVoteCount();

            isTodayFirst = PlayerVoteDataMono.isTodayFirstVote();

            pvd.add();

            newVote = pvd.getVoteCount();
        } catch (SQLException | NullPointerException e) {
            missedNotifyMonocraftNet(name, e.getClass().getName() + " -> " + e.getMessage() + " (投票数追加失敗)");
            MyMaidLibrary.reportError(getClass(), e);
            return;
        }

        successNotifyMonocraftNet(name, oldVote, newVote);
        checkjSA(offplayer, isTodayFirst, newVote);
    }

    public static void successNotifyMinecraftJP(String name, int oldVote, int newVote, boolean isAutoFill) {
        String autoFillMessage = isAutoFill ? " [自動補填]" : "";

        Bukkit.getServer().sendMessage(Component.text().append(
            Component.text("[MyMaid]"),
            Component.space(),
            Component.text("プレイヤー「" + name + "」がminecraft.jpで投票をしました！(現在の投票数:" + newVote + "回)", NamedTextColor.GREEN),
            Component.text(autoFillMessage, NamedTextColor.GREEN)
        ));
        Bukkit.getServer().sendMessage(Component.text().append(
            Component.text("[MyMaid]"),
            Component.space(),
            Component.text("投票をよろしくお願いします！", NamedTextColor.GREEN),
            Component.space(),
            Component.text("https://jaoafa.com/vote", NamedTextColor.GREEN, TextDecoration.UNDERLINED)
                .hoverEvent(HoverEvent.showText(Component.text("クリックすると「https://jaoafa.com/vote」をブラウザで開きます。")))
                .clickEvent(ClickEvent.openUrl("https://jaoafa.com/vote"))
        ));

        if (Main.getMyMaidConfig().getJDA() != null) {
            if (MyMaidData.getServerChatChannel() != null) {
                MyMaidData.getServerChatChannel()
                    .sendMessage("プレイヤー「" + DiscordEscape(name) + "」がminecraft.jpで投票をしました！(現在の投票数:" + newVote + "回)" + autoFillMessage)
                    .queue();
                MyMaidData.getServerChatChannel().sendMessage("投票をよろしくお願いします！ https://jaoafa.com/vote").queue();
            }

            TextChannel vote_channel = Main.getMyMaidConfig().getJDA().getTextChannelById(499922840871632896L);
            if (vote_channel != null) {
                vote_channel
                    .sendMessage(":o: `" + name + "`の投票特典付与処理に成功しました(minecraft.jp): " + oldVote + "回 -> " + newVote + "回" + autoFillMessage)
                    .queue();
            }
        }
    }

    public static void successNotifyMonocraftNet(String name, int oldVote, int newVote, boolean isAutoFill) {
        String autoFillMessage = isAutoFill ? " [自動補填]" : "";

        Bukkit.getServer().sendMessage(Component.text().append(
            Component.text("[MyMaid]"),
            Component.space(),
            Component.text("プレイヤー「" + name + "」がmonocraft.netで投票をしました！(現在の投票数:" + newVote + "回)", NamedTextColor.GREEN),
            Component.text(autoFillMessage, NamedTextColor.GREEN)
        ));
        Bukkit.getServer().sendMessage(Component.text().append(
            Component.text("[MyMaid]"),
            Component.space(),
            Component.text("投票をよろしくお願いします！", NamedTextColor.GREEN),
            Component.space(),
            Component.text("https://jaoafa.com/monovote", NamedTextColor.GREEN, TextDecoration.UNDERLINED)
                .hoverEvent(HoverEvent.showText(Component.text("クリックすると「https://jaoafa.com/monovote」をブラウザで開きます。")))
                .clickEvent(ClickEvent.openUrl("https://jaoafa.com/monovote"))
        ));

        if (Main.getMyMaidConfig().getJDA() != null) {
            if (MyMaidData.getServerChatChannel() != null) {
                MyMaidData.getServerChatChannel()
                    .sendMessage("プレイヤー「" + DiscordEscape(name) + "」がmonocraft.netで投票をしました！(現在の投票数:" + newVote + "回)" + autoFillMessage)
                    .queue();
                MyMaidData.getServerChatChannel().sendMessage("投票をよろしくお願いします！ https://jaoafa.com/monovote").queue();
            }

            TextChannel vote_channel = Main.getMyMaidConfig().getJDA().getTextChannelById(499922840871632896L);
            if (vote_channel != null) {
                vote_channel
                    .sendMessage(":o: `" + name + "`の投票特典付与処理に成功しました(monocraft.net): " + oldVote + "回 -> " + newVote + "回" + autoFillMessage)
                    .queue();
            }
        }
    }

    void VoteReceive(String name) {
        MySQLDBManager MySQLDBManager = MyMaidData.getMainMySQLDBManager();
        if (MySQLDBManager == null) {
            missedNotifyMinecraftJP(name, "MySQLDBManager == null");
            return;
        }
        // nameからuuidを取得する
        UUID uuid = getUUID(MySQLDBManager, name);
        if (uuid == null) {
            missedNotifyMinecraftJP(name, "UUID取得失敗");
            return;
        }

        OfflinePlayer offplayer = Bukkit.getOfflinePlayer(uuid);

        if (offplayer.getName() == null) {
            missedNotifyMinecraftJP(name, "OfflinePlayer取得失敗");
            return;
        }

        if (!offplayer.getName().equals(name)) {
            name += "(" + offplayer.getName() + ")";
        }

        int oldVote;
        int newVote;
        boolean isTodayFirst;
        try {
            PlayerVoteDataMCJP pvd = new PlayerVoteDataMCJP(offplayer);
            oldVote = pvd.getVoteCount();

            isTodayFirst = PlayerVoteDataMCJP.isTodayFirstVote();

            pvd.add();

            newVote = pvd.getVoteCount();
        } catch (SQLException | NullPointerException e) {
            missedNotifyMinecraftJP(name, e.getClass().getName() + " -> " + e.getMessage() + " (投票数追加失敗)");
            MyMaidLibrary.reportError(getClass(), e);
            return;
        }

        successNotifyMinecraftJP(name, oldVote, newVote);
        checkjSA(offplayer, isTodayFirst, newVote);
    }

    public static void checkjSA(OfflinePlayer offplayer, boolean isTodayFirst, int newVote) {
        if (isTodayFirst) {
            Achievementjao.getAchievementAsync(offplayer, Achievement.EARLYSHAREHOLDER); // 筆頭株主 - 誰よりも早くjao鯖に投票
        }
        Achievementjao.getAchievementAsync(offplayer, Achievement.EXPECTEDMEMBER); // 期待の新人 - 初めての投票
        if (newVote >= 10) {
            Achievementjao.getAchievementAsync(offplayer, Achievement.STABLESHAREHOLDER); // 安定株主 - 10回投票
        }
        if (newVote >= 20) {
            Achievementjao.getAchievementAsync(offplayer, Achievement.VIPPER); // VIPPERな俺 - 20回投票
        }
        if (newVote >= 100) {
            Achievementjao.getAchievementAsync(offplayer, Achievement.MAJORSHAREHOLDER); // 大株主 - 100回投票
        }
        if (newVote >= 1000) {
            Achievementjao.getAchievementAsync(offplayer, Achievement.LEGENDARYSHAREHOLDER); // 伝説の株主 - 1000回投票
        }
    }

    void successNotifyMinecraftJP(String name, int oldVote, int newVote) {
        successNotifyMinecraftJP(name, oldVote, newVote, false);
    }

    void successNotifyMonocraftNet(String name, int oldVote, int newVote) {
        successNotifyMonocraftNet(name, oldVote, newVote, false);
    }

    void missedNotify(String name, String reason) {
        if (Main.getMyMaidConfig().getJDA() == null) {
            return;
        }
        TextChannel vote_channel = Main.getMyMaidConfig().getJDA().getTextChannelById(499922840871632896L);
        if (vote_channel == null) {
            return;
        }

        vote_channel
            .sendMessage(":x: <@221991565567066112> `" + name + "`の投票特典付与処理に失敗しました: `" + reason + "`")
            .queue();
    }

    void missedNotifyMinecraftJP(String name, String reason) {
        if (Main.getMyMaidConfig().getJDA() == null) {
            return;
        }
        TextChannel vote_channel = Main.getMyMaidConfig().getJDA().getTextChannelById(499922840871632896L);
        if (vote_channel == null) {
            return;
        }

        vote_channel
            .sendMessage(":x: <@221991565567066112> `" + name + "`の投票特典付与処理に失敗しました(minecraft.jp): `" + reason + "`")
            .queue();
    }

    void missedNotifyMonocraftNet(String name, String reason) {
        if (Main.getMyMaidConfig().getJDA() == null) {
            return;
        }
        TextChannel vote_channel = Main.getMyMaidConfig().getJDA().getTextChannelById(499922840871632896L);
        if (vote_channel == null) {
            return;
        }

        vote_channel
            .sendMessage(
                ":x: <@221991565567066112> `" + name + "`の投票特典付与処理に失敗しました(monocraft.net): `" + reason + "`")
            .queue();
    }

    UUID getUUID(MySQLDBManager MySQLDBManager, String name) {
        UUID uuid = null;
        try {
            Connection conn = MySQLDBManager.getConnection();
            PreparedStatement statement = conn
                .prepareStatement("SELECT * FROM login WHERE player = ? ORDER BY id DESC");
            statement.setString(1, name);

            ResultSet res = statement.executeQuery();
            if (res.next()) {
                uuid = UUID.fromString(res.getString("uuid"));
            }
            return uuid;
        } catch (SQLException e) {
            missedNotify(name, e.getClass().getName() + " -> " + e.getMessage());
            MyMaidLibrary.reportError(getClass(), e);
            return null;
        }
    }
}