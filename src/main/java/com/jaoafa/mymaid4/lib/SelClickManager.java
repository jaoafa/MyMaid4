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
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SelClickManager {
    static final Map<UUID, Boolean> cache = new HashMap<>();
    static final File file = new File(Main.getJavaPlugin().getDataFolder(), "selclick.yml");

    public static boolean isEnable(Player player) {
        if (cache.containsKey(player.getUniqueId())) {
            return cache.get(player.getUniqueId());
        } else {
            FileConfiguration conf = YamlConfiguration.loadConfiguration(file);
            if (conf.contains(player.getUniqueId().toString())) {
                cache.put(player.getUniqueId(), conf.getBoolean(player.getUniqueId().toString()));
                return conf.getBoolean(player.getUniqueId().toString());
            } else {
                cache.put(player.getUniqueId(), false);
                return false;
            }
        }
    }

    public static void setStatus(Player player, boolean newValue) {
        cache.put(player.getUniqueId(), newValue);
        FileConfiguration conf = YamlConfiguration.loadConfiguration(file);
        conf.set(player.getUniqueId().toString(), newValue);
        try {
            conf.save(file);
        } catch (IOException e) {
            MyMaidLibrary.reportError(SelClickManager.class, e);
        }
    }
}