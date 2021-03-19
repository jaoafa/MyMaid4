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

import com.jaoafa.mymaid4.Main;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.AnnotatedEventManager;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * config.ymlで定義されるコンフィグのクラス
 */
public class MyMaidConfig {
    private JDA jda;
    private Long generalChannelId = null;
    private Long jaotanChannelId = null;
    private Long reportChannelId = null;
    private Long serverChatChannelId = null;

    public MyMaidConfig() {
        JavaPlugin plugin = Main.getJavaPlugin();
        if (!new File(plugin.getDataFolder(), "config.yml").exists()) {
            plugin.getLogger().warning("コンフィグファイルが見つかりませんでした。一部の機能は無効化されます。");
            return;
        }

        FileConfiguration config = plugin.getConfig();

        ConfigurationSection discord = config.getConfigurationSection("discord");
        if (config.contains("discord") && discord != null) {
            if (discord.contains("token")) {
                try {
                    JDABuilder jdabuilder = JDABuilder.createDefault(config.getString("discordtoken"))
                        .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_PRESENCES)
                        .setAutoReconnect(true)
                        .setBulkDeleteSplittingEnabled(false)
                        .setContextEnabled(false)
                        .setEventManager(new AnnotatedEventManager());

                    Main.registerDiscordEvent(jdabuilder);

                    jda = jdabuilder.build().awaitReady();
                } catch (Exception e) {
                    plugin.getLogger().warning("Discordへの接続に失敗しました。(" + e.getMessage() + ")");
                    plugin.getLogger().warning("プラグインを無効化します。");
                    plugin.getServer().getPluginManager().disablePlugin(plugin);
                }
            }

            if (discord.contains("general_id")) {
                generalChannelId = discord.getLong("general_id");
            } else plugin.getLogger().warning(notFoundConfigKey("discord.general_id"));

            if (discord.contains("jaotan_id")) {
                jaotanChannelId = discord.getLong("jaotan_id");
            } else plugin.getLogger().warning(notFoundConfigKey("discord.jaotan_id"));

            if (discord.contains("jaotan_id")) {
                reportChannelId = discord.getLong("report_id");
            } else plugin.getLogger().warning(notFoundConfigKey("discord.report_id"));

            if (discord.contains("serverchat_id")) {
                serverChatChannelId = discord.getLong("serverchat_id");
            } else plugin.getLogger().warning(notFoundConfigKey("discord.report_id"));
        } else {
            plugin.getLogger().warning(notFoundConfigKey("discord"));
        }
    }

    String notFoundConfigKey(String key) {
        return String.format("%sコンフィグが見つかりませんでした。一部の機能は無効化されます。", key);
    }

    public JDA getJDA() {
        return jda;
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
}
