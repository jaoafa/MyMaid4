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
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class MyMaidConfig {
    private JDA jda;

    public MyMaidConfig() {
        JavaPlugin plugin = Main.getJavaPlugin();
        if (!new File(plugin.getDataFolder(), "config.yml").exists()) {
            plugin.getLogger().warning("コンフィグファイルが見つかりませんでした。一部の機能は無効化されます。");
        }
    }
}
