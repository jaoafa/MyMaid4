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

import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NMSManager {
    /**
     * ItemStackの文字列NBTタグを返します
     *
     * @param is ItemStack
     *
     * @return 文字列NBTタグ
     */
    public static String getNBT(ItemStack is) {
        try {
            Class<?> CraftItemStack = getNMSClass("org.bukkit.craftbukkit.%s.inventory.CraftItemStack");
            assert CraftItemStack != null;
            Method asNMSCopy = CraftItemStack.getDeclaredMethod("asNMSCopy", ItemStack.class);

            Class<?> ItemStack = getNMSClass("net.minecraft.server.%s.ItemStack");
            assert ItemStack != null;
            Method getTag = ItemStack.getDeclaredMethod("getTag");

            Class<?> NBTTagCompound = getNMSClass("net.minecraft.server.%s.NBTTagCompound");
            assert NBTTagCompound != null;
            Method toString = NBTTagCompound.getDeclaredMethod("toString");

            Object ItemStackObj = asNMSCopy.invoke(null, is);
            Object NBTTagCompoundObj = getTag.invoke(ItemStackObj);
            if (NBTTagCompoundObj == null) return "{}";
            return (String) toString.invoke(NBTTagCompoundObj);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | NullPointerException e) {
            MyMaidLibrary.reportError(NMSManager.class, e);
        }
        return null;
    }

    private static Class<?> getNMSClass(String nmsClass) {
        String version = null;
        Pattern pattern = Pattern.compile("net\\.minecraft\\.(?:server)?\\.(v(?:\\d+_)+R\\d+)");
        for (Package p : Package.getPackages()) {
            String name = p.getName();
            Matcher m = pattern.matcher(name);
            if (m.matches()) version = m.group(1);
        }

        if (version == null) return null;

        try {
            return Class.forName(String.format(nmsClass, version));
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}
