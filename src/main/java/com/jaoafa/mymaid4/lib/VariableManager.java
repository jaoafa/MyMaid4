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
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class VariableManager {
    final Map<String, String> map = new HashMap<>();

    public boolean set(String key, String value) {
        map.put(key, value);
        try {
            save();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean set(String key, int value) {
        map.put(key, String.valueOf(value));
        try {
            save();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean remove(String key) {
        map.remove(key);
        try {
            save();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean isDefined(String key) {
        load(false);
        return map.containsKey(key);
    }

    public String getString(String key) {
        load(false);
        return map.get(key);
    }

    public int getInt(String key) throws NumberFormatException {
        load(false);
        return Integer.parseInt(getString(key));
    }

    public Map<String, String> getVariables() {
        load(false);
        return map;
    }

    public void save() throws IOException {
        File file = new File(Main.getJavaPlugin().getDataFolder(), "variables.yml");
        YamlConfiguration yaml = new YamlConfiguration();
        map.forEach(yaml::set);
        yaml.save(file);
    }

    public void load(boolean force) {
        if (!force && !map.isEmpty()) {
            return;
        }
        File file = new File(Main.getJavaPlugin().getDataFolder(), "variables.yml");
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        yaml.getKeys(false).forEach(key -> map.put(key, yaml.getString(key)));
    }
}
