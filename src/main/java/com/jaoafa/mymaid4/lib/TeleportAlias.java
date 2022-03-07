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
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class TeleportAlias {
    static Map<String, String> alias = new HashMap<>();

    public static boolean setAlias(String target, String replacement) {
        load();
        alias.put(target, replacement);
        save();
        return true;
    }

    public static boolean removeAlias(String target) {
        load();
        if (!alias.containsKey(target)) {
            return false;
        }
        alias.remove(target);
        save();
        return true;
    }

    public static String getReplaceAlias(String target) {
        load();
        if (alias.containsKey(target)) {
            return alias.get(target);
        }
        return null;
    }

    public static Map<String, String> getAlias() {
        load();
        return alias;
    }

    public static void save() {
        Path path = Path.of(Main.getJavaPlugin().getDataFolder().getAbsolutePath(), "teleportAlias.json");
        try {
            Files.writeString(path, new JSONObject(alias).toString());
        } catch (IOException e) {
            MyMaidLibrary.reportError(TeleportAlias.class, e);
        }
    }

    public static void load() {
        Path path = Path.of(Main.getJavaPlugin().getDataFolder().getAbsolutePath(), "teleportAlias.json");
        try {
            if (!Files.exists(path)) {
                return;
            }
            JSONObject object = new JSONObject(Files.readString(path));
            alias = toMap(object);
        } catch (IOException e) {
            MyMaidLibrary.reportError(TeleportAlias.class, e);
        }
    }

    static Map<String, String> toMap(JSONObject json) {
        Map<String, String> tmp = new HashMap<>();
        for (String target : json.keySet()) {
            tmp.put(target, json.getString(target));
        }
        return tmp;
    }
}
