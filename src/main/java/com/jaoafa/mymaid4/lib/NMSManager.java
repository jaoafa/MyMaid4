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
import org.jetbrains.annotations.Nullable;

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
    @Nullable
    public static String getNBT(ItemStack is) {
        try {
            Class<?> ItemStack = getNMSClass("net.minecraft.server.%s.ItemStack");
            assert ItemStack != null;
            Method getTag = ItemStack.getDeclaredMethod("getTag");

            Class<?> NBTTagCompound = getNMSClass("net.minecraft.server.%s.NBTTagCompound");
            assert NBTTagCompound != null;
            Method toString = NBTTagCompound.getDeclaredMethod("toString");

            Object ItemStackObj = getNMSItem(is);
            Object NBTTagCompoundObj = getTag.invoke(ItemStackObj);
            if (NBTTagCompoundObj == null) return "{}";
            return (String) toString.invoke(NBTTagCompoundObj);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | NullPointerException e) {
            MyMaidLibrary.reportError(NMSManager.class, e);
        }
        return null;
    }

    /**
     * ItemStackに指定されたNBTタグをつけたItemStackを返します
     *
     * @param is    ItemStack
     * @param key   NBTタグのキー
     * @param value NBTタグの値
     *
     * @return 指定されたNBTタグをつけたItemStack
     */
    @Nullable
    public static ItemStack setNBTString(ItemStack is, String key, String value) {
        try {
            Class<?> ItemStack = getNMSClass("net.minecraft.server.%s.ItemStack");
            assert ItemStack != null;
            Method getTag = ItemStack.getDeclaredMethod("getTag");

            Class<?> NBTTagCompound = getNMSClass("net.minecraft.server.%s.NBTTagCompound");
            assert NBTTagCompound != null;
            Method setString = NBTTagCompound.getDeclaredMethod("setString", String.class, String.class);

            Method setTag = ItemStack.getDeclaredMethod("setTag", NBTTagCompound);

            Object ItemStackObj = getNMSItem(is);
            Object NBTTagCompoundObj = getTag.invoke(ItemStackObj);
            if (NBTTagCompoundObj == null) return null;
            setString.invoke(NBTTagCompoundObj, key, value);
            setTag.invoke(ItemStackObj, NBTTagCompoundObj);
            return getItemStack(ItemStackObj);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | NullPointerException e) {
            MyMaidLibrary.reportError(NMSManager.class, e);
        }
        return null;
    }

    /**
     * ItemStackに指定されたNBTタグがついているかどうかを判定します
     *
     * @param is  ItemStack
     * @param key NBTタグのキー
     *
     * @return NBTタグがついているか
     */
    public static boolean hasNBT(ItemStack is, String key) {
        try {
            Class<?> ItemStack = getNMSClass("net.minecraft.server.%s.ItemStack");
            assert ItemStack != null;
            Method getTag = ItemStack.getDeclaredMethod("getTag");

            Class<?> NBTTagCompound = getNMSClass("net.minecraft.server.%s.NBTTagCompound");
            assert NBTTagCompound != null;
            Method hasKey = NBTTagCompound.getDeclaredMethod("hasKey", String.class);

            Object ItemStackObj = getNMSItem(is);
            Object NBTTagCompoundObj = getTag.invoke(ItemStackObj);
            if (NBTTagCompoundObj == null) return false;
            return (boolean) hasKey.invoke(NBTTagCompoundObj, key);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | NullPointerException e) {
            MyMaidLibrary.reportError(NMSManager.class, e);
        }
        return false;
    }

    /**
     * ItemStackから指定されたNBTタグの値を取得します
     *
     * @param is  ItemStack
     * @param key NBTタグのキー
     *
     * @return NBTタグの値
     */
    public static String getNBTString(ItemStack is, String key) {
        try {
            Class<?> ItemStack = getNMSClass("net.minecraft.server.%s.ItemStack");
            assert ItemStack != null;
            Method getTag = ItemStack.getDeclaredMethod("getTag");

            Class<?> NBTTagCompound = getNMSClass("net.minecraft.server.%s.NBTTagCompound");
            assert NBTTagCompound != null;
            Method getString = NBTTagCompound.getDeclaredMethod("getString", String.class);

            Object ItemStackObj = getNMSItem(is);
            Object NBTTagCompoundObj = getTag.invoke(ItemStackObj);
            if (NBTTagCompoundObj == null) return null;
            return (String) getString.invoke(NBTTagCompoundObj, key);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | NullPointerException e) {
            MyMaidLibrary.reportError(NMSManager.class, e);
        }
        return null;
    }

    /**
     * ItemStackから変換したNMSのアイテムを返します。
     *
     * @param is ItemStack
     *
     * @return NMSのアイテム
     */
    @Nullable
    public static Object getNMSItem(ItemStack is) {
        try {
            Class<?> CraftItemStack = getNMSClass("org.bukkit.craftbukkit.%s.inventory.CraftItemStack");
            assert CraftItemStack != null;
            Method asNMSCopy = CraftItemStack.getDeclaredMethod("asNMSCopy", ItemStack.class);
            return asNMSCopy.invoke(null, is);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | NullPointerException e) {
            MyMaidLibrary.reportError(NMSManager.class, e);
        }
        return null;
    }

    /**
     * NMSのアイテムから変換したItemStackを返します。
     *
     * @param nmsItem NMSのアイテム
     *
     * @return ItemStack
     */
    @Nullable
    public static ItemStack getItemStack(Object nmsItem) {
        try {
            Class<?> ItemStack = getNMSClass("net.minecraft.server.%s.ItemStack");
            assert ItemStack != null;
            Class<?> CraftItemStack = getNMSClass("org.bukkit.craftbukkit.%s.inventory.CraftItemStack");
            assert CraftItemStack != null;
            Method asNMSCopy = CraftItemStack.getDeclaredMethod("asBukkitCopy", ItemStack);
            return (ItemStack) asNMSCopy.invoke(null, nmsItem);
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
