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
import com.jaoafa.mymaid4.lib.MyMaidData;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;

public class Cmd_DiscordLink extends MyMaidLibrary implements CommandPremise {
    @Override
    public MyMaidCommand.Detail details() {
        return new MyMaidCommand.Detail(
            "discordlink",
            "DiscordアカウントとMinecraftアカウントを紐づけます。"
        );
    }


    @Override
    public MyMaidCommand.Cmd register(Command.Builder<CommandSender> builder) {
        return new MyMaidCommand.Cmd(
            builder
                .meta(CommandMeta.DESCRIPTION, "DiscordアカウントとMinecraftアカウントを紐づけます。")
                .senderType(Player.class)
                .argument(StringArgument.of("authKey"), ArgumentDescription.of("認証コード"))
                .handler(this::authDiscord)
                .build()
        );
    }

    void authDiscord(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        String authKey = context.get("authKey");

        // AuthKeyは「半角英数字」で構成されているか？
        if (!authKey.matches("^[0-9a-zA-Z]+$")) {
            SendMessage(player, details(), "AuthKeyは英数字のみ受け付けています。");
            return;
        }

        int id;
        String name, disid, discriminator;
        Connection conn;

        try {
            conn = MyMaidData.getMainMySQLDBManager().getConnection();

            // 指定されたAuthKeyは存在するか？
            try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM discordlink_waiting WHERE authkey = ?")) {
                stmt.setString(1, authKey);
                try (ResultSet res = stmt.executeQuery()) {

                    if (!res.next()) {
                        SendMessage(player, details(), "指定されたAuthIDは見つかりませんでした。");
                        return;
                    }

                    id = res.getInt("id");
                    name = res.getString("name");
                    disid = res.getString("disid");
                    discriminator = res.getString("discriminator");
                }
            }
        } catch (SQLException e) {
            reportError(getClass(), e);
            SendMessage(player, details(), "操作に失敗しました。");
            SendMessage(player, details(), "再度実行しなおすと動作するかもしれません。");
            return;
        }

        try {
            // すでにリンク要求されたMinecraftアカウントと紐づいているか？
            try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM discordlink WHERE uuid = ? AND disid = ? AND disabled = ?")) {
                stmt.setString(1, player.getUniqueId().toString());
                stmt.setString(2, disid);
                stmt.setInt(3, 0);
                try (ResultSet res = stmt.executeQuery()) {
                    if (res.next()) {
                        SendMessage(player, details(), "すでにあなたのMinecraftアカウントと接続されています。");
                        return;
                    }
                }
            }
        } catch (SQLException e) {
            reportError(getClass(), e);
            SendMessage(player, details(), "操作に失敗しました。");
            SendMessage(player, details(), "再度実行しなおすと動作するかもしれません。");
            return;
        }

        try {
            // リンク要求されたMinecraftアカウントが別のDiscordアカウントと紐づいていないか？
            try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM discordlink WHERE uuid = ? AND disabled = ?")) {
                stmt.setString(1, player.getUniqueId().toString());
                stmt.setInt(2, 0);
                try (ResultSet res = stmt.executeQuery()) {
                    if (res.next()) {
                        SendMessage(player, details(), "すでにあなたのMinecraftアカウントは別のDiscordアカウントに接続されています。");
                        return;
                    }
                }
            }
        } catch (SQLException e) {
            reportError(getClass(), e);
            SendMessage(player, details(), "操作に失敗しました。");
            SendMessage(player, details(), "再度実行しなおすと動作するかもしれません。");
            return;
        }

        try {
            // Discordアカウントが別のMinecraftアカウントと紐づいていないか？
            try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM discordlink WHERE disid = ? AND disabled = ?")) {
                stmt.setString(1, disid);
                stmt.setInt(2, 0);
                try (ResultSet res = stmt.executeQuery()) {
                    if (res.next()) {
                        SendMessage(player, details(), "アカウントリンク要求をしたDiscordアカウントは既に他のMinecraftアカウントと接続されています。");
                        return;
                    }
                }
            }
        } catch (SQLException e) {
            reportError(getClass(), e);
            SendMessage(player, details(), "操作に失敗しました。");
            SendMessage(player, details(), "再度実行しなおすと動作するかもしれません。");
            return;
        }

        // DiscordアカウントがDiscordチャンネルから退出していないかどうか
        JDA jda = Main.getMyMaidConfig().getJDA();
        if (jda == null) {
            SendMessage(player, details(), "Discordへの接続に失敗しました。");
            SendMessage(player, details(), "再度実行しなおすと動作するかもしれません。");
            return;
        }
        Guild guild = jda.getGuildById(597378876556967936L);
        if (guild == null) {
            SendMessage(player, details(), "Discordサーバの取得に失敗しました。");
            SendMessage(player, details(), "再度実行しなおすと動作するかもしれません。");
            return;
        }
        Member member = guild.retrieveMemberById(disid).complete();
        if (member == null) {
            SendMessage(player, details(), "アカウントリンク要求をしたDiscordアカウントは既に当サーバのDiscordチャンネルから退出しています。");
            return;
        }

        String old_perm = null;
        try {
            try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM discordlink WHERE disid = ? AND disabled = ? AND dead_at > (NOW() - INTERVAL 7 DAY) ORDER BY id LIMIT 1")) {
                stmt.setString(1, disid);
                stmt.setBoolean(2, true);
                try (ResultSet res = stmt.executeQuery()) {
                    if (res.next()) {
                        old_perm = res.getString("dead_perm");
                    }
                }
            }
        } catch (SQLException e) {
            reportError(getClass(), e);
            SendMessage(player, details(), "操作に失敗しました。");
            SendMessage(player, details(), "詳しくはサーバコンソールをご確認ください");
            SendMessage(player, details(), "再度実行しなおすと動作するかもしれません。");
            return;
        }

        if (old_perm != null) {
            SendMessage(player, details(), String.format("自動切断から1週間以内に再連携されたため、元の権限(%s)に復元します。", old_perm.toLowerCase(Locale.ROOT)));
        }

        try {
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM discordlink_waiting WHERE id = ?")) {
                stmt.setInt(1, id);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            reportError(getClass(), e);
            SendMessage(player, details(), "操作に失敗しました。");
            SendMessage(player, details(), "詳しくはサーバコンソールをご確認ください");
            SendMessage(player, details(), "再度実行しなおすと動作するかもしれません。");
            return;
        }


        String group = getPermissionMainGroup(player);
        try {
            try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO discordlink (player, uuid, name, disid, discriminator, pex) VALUES (?, ?, ?, ?, ?, ?);")) {
                stmt.setString(1, player.getName());
                stmt.setString(2, player.getUniqueId().toString());
                stmt.setString(3, name);
                stmt.setString(4, disid);
                stmt.setString(5, discriminator);
                stmt.setString(6, group);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            reportError(getClass(), e);
            SendMessage(player, details(), "操作に失敗しました。");
            SendMessage(player, details(), "再度実行しなおすと動作するかもしれません。");
            return;
        }

        SendMessage(player, details(), "アカウントのリンクが完了しました。");
        TextChannel general = MyMaidData.getGeneralChannel();
        if (general == null) {
            Main.getJavaPlugin().getLogger().warning("general が null です。");
            return;
        }
        if (old_perm != null) {
            general.sendMessage(
                    ":loudspeaker:<@" + disid + ">さんのMinecraftアカウント連携を完了しました！自動切断から1週間以内の再連携のため、元の権限(" + old_perm.toLowerCase(Locale.ROOT) + ")に自動復元されます。 MinecraftID: `" + player.getName() + "`")
                .queue();

            if (old_perm.equals("COMMUNITYREGULAR")) {
                Main.getMyMaidLogger().info("CommunityRegularへの復元のため、鯖内権限をVerifiedに、Discord内ロールをCommunityRegularに変更します。");
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getUniqueId() + " parent set verified");
                Role roleCommunityRegular = guild.getRoleById(RoleId.COMMUNITYREGULAR.getId());
                if (roleCommunityRegular != null) {
                    guild.removeRoleFromMember(member, roleCommunityRegular).queue();
                }
            } else {
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getUniqueId() + " parent set " + old_perm.toLowerCase(Locale.ROOT));
                try {
                    RoleId roleIdEnum = RoleId.valueOf(old_perm);
                    Role role = guild.getRoleById(roleIdEnum.getId());
                    if (role != null) {
                        guild.removeRoleFromMember(member, role).queue();
                    }
                } catch (IllegalArgumentException e) {
                    Main.getMyMaidLogger().warning("権限名が不正です。");
                    MyMaidLibrary.reportError(getClass(), e);
                }
            }
        } else {
            general.sendMessage(
                    ":loudspeaker:<@" + disid + ">さんのMinecraftアカウント連携を完了しました！ MinecraftID: `" + player.getName() + "`")
                .queue();
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getUniqueId() + " parent set verified");
        }

        Role minecraftConnected = guild.getRoleById(604011598952136853L);
        Role verified = guild.getRoleById(597405176969560064L);
        if (minecraftConnected == null || verified == null) {
            Main.getJavaPlugin().getLogger().warning("MinecraftConnected または Verified が null です。");
            return;
        }
        guild.addRoleToMember(member, minecraftConnected).queue(); // MinecraftConnected
        guild.addRoleToMember(member, verified).queue(); // Verified
    }

    enum RoleId {
        REGULAR(597405176189419554L),
        COMMUNITYREGULAR(888150763421970492L),
        VERIFIED(597405176969560064L);

        private final long id;

        RoleId(long id) {
            this.id = id;
        }

        public long getId() {
            return id;
        }
    }
}
