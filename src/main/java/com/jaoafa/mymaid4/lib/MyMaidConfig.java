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

import com.jaoafa.mymaid4.Main;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * config.ymlで定義されるコンフィグのクラス
 */
public class MyMaidConfig {
    private JDA jda;
    private boolean isDevelopmentServer = false;
    private Long generalChannelId = null;
    private Long jaotanChannelId = null;
    private Long reportChannelId = null;
    private Long serverChatChannelId = null;
    private String githubAccessToken = null;
    private String rollbarAccessToken = null;

    public void init() {
        JavaPlugin plugin = Main.getJavaPlugin();
        if (new File(plugin.getDataFolder(), "this-server-is-development").exists()) {
            plugin.getLogger().warning("このサーバは開発サーバです。一部の機能が無効・変更される可能性があります。");
            isDevelopmentServer = true;
        }

        if (!new File(plugin.getDataFolder(), "config.yml").exists()) {
            plugin.getLogger().warning("コンフィグファイルが見つかりませんでした。一部の機能は無効化されます。");
            return;
        }

        FileConfiguration config = plugin.getConfig();

        ConfigurationSection discord = config.getConfigurationSection("discord");
        if (config.contains("discord") && discord != null) {
            if (discord.contains("general_id")) {
                generalChannelId = discord.getLong("general_id");
            } else plugin.getLogger().warning(notFoundConfigKey("discord.general_id"));

            if (discord.contains("jaotan_id")) {
                jaotanChannelId = discord.getLong("jaotan_id");
            } else plugin.getLogger().warning(notFoundConfigKey("discord.jaotan_id"));

            if (discord.contains("report_id")) {
                reportChannelId = discord.getLong("report_id");
            } else plugin.getLogger().warning(notFoundConfigKey("discord.report_id"));

            if (discord.contains("serverchat_id")) {
                serverChatChannelId = discord.getLong("serverchat_id");
            } else plugin.getLogger().warning(notFoundConfigKey("discord.report_id"));

            if (discord.contains("token")) {
                try {
                    JDABuilder jdabuilder = JDABuilder.createDefault(discord.getString("token"))
                        .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_PRESENCES, GatewayIntent.GUILD_MESSAGES)
                        .setAutoReconnect(true)
                        .setBulkDeleteSplittingEnabled(false)
                        .setContextEnabled(false)
                        .setRawEventsEnabled(false);

                    Main.registerDiscordEvent(jdabuilder);

                    jda = jdabuilder.build().awaitReady();
                } catch (Exception e) {
                    plugin.getLogger().warning("Discordへの接続に失敗しました。(" + e.getMessage() + ")");
                    plugin.getLogger().warning("プラグインを無効化します。");
                    plugin.getServer().getPluginManager().disablePlugin(plugin);
                }
            }
        } else {
            plugin.getLogger().warning(notFoundConfigKey("discord"));
        }

        ConfigurationSection main_database = config.getConfigurationSection("main_database");
        if (config.contains("main_database") && main_database != null) {
            String hostname = main_database.getString("hostname");
            int port = main_database.getInt("port");
            String username = main_database.getString("username");
            String password = main_database.getString("password");
            String dbname = main_database.getString("database");

            try {
                MyMaidData.setMainMySQLDBManager(new MySQLDBManager(hostname, port, username, password, dbname));
            } catch (ClassNotFoundException e) {
                plugin.getLogger().warning("MainDBのInitに失敗しました（ClassNotFoundException）一部の機能は無効化されます。");
                MyMaidLibrary.reportError(getClass(), e);
            }
        } else {
            plugin.getLogger().warning(notFoundConfigKey("main_database"));
        }

        ConfigurationSection zakurohat_database = config.getConfigurationSection("zakurohat_database");
        if (config.contains("zakurohat_database") && zakurohat_database != null) {
            String hostname = zakurohat_database.getString("hostname");
            int port = zakurohat_database.getInt("port");
            String username = zakurohat_database.getString("username");
            String password = zakurohat_database.getString("password");
            String dbname = zakurohat_database.getString("database");

            try {
                MyMaidData.setZKRHatMySQLDBManager(new MySQLDBManager(hostname, port, username, password, dbname));
            } catch (ClassNotFoundException e) {
                plugin.getLogger().warning("ZakuroHatDBのInitに失敗しました（ClassNotFoundException）一部の機能は無効化されます。");
                MyMaidLibrary.reportError(getClass(), e);
            }
        } else {
            plugin.getLogger().warning(notFoundConfigKey("zakurohat_database"));
        }

        if (config.contains("githubAccessToken")) {
            githubAccessToken = config.getString("githubAccessToken");
        } else {
            plugin.getLogger().warning(notFoundConfigKey("githubAccessToken"));
        }

        if (config.contains("rollbarAccessToken")) {
            rollbarAccessToken = config.getString("rollbarAccessToken");
        } else {
            plugin.getLogger().warning(notFoundConfigKey("rollbarAccessToken"));
        }
    }

    String notFoundConfigKey(String key) {
        return String.format("%sコンフィグが見つかりませんでした。一部の機能は無効化されます。", key);
    }

    @Nullable
    public JDA getJDA() {
        return jda;
    }

    public boolean isDevelopmentServer() {
        return isDevelopmentServer;
    }

    public Long getGeneralChannelId() {
        return generalChannelId;
    }

    public Long getJaotanChannelId() {
        return jaotanChannelId;
    }

    public Long getReportChannelId() {
        return reportChannelId;
    }

    public Long getServerChatChannelId() {
        return serverChatChannelId;
    }

    public String getGitHubAccessToken() {
        return githubAccessToken;
    }

    public String getRollbarAccessToken() {
        return rollbarAccessToken;
    }
}
