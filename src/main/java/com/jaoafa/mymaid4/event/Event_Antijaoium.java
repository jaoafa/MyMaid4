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

package com.jaoafa.mymaid4.event;

import com.google.common.io.Files;
import com.jaoafa.jaosuperachievement2.api.Achievementjao;
import com.jaoafa.jaosuperachievement2.lib.Achievement;
import com.jaoafa.mymaid4.Main;
import com.jaoafa.mymaid4.lib.*;
import com.jaoafa.mymaid4.tasks.Task_AutoRemoveJailByjaoium;
import net.dv8tion.jda.api.entities.TextChannel;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
import org.apache.commons.codec.digest.DigestUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.CommandBlock;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

public class Event_Antijaoium extends MyMaidLibrary implements Listener, EventPremise {
    List<Integer> heal = Arrays.asList(
        -3,
        29,
        125,
        253
    );
    List<Integer> health_boost = Collections.singletonList(
        -7
    );
    Set<String> sendHashes = new HashSet<>();
    Map<String, String> Reason = new HashMap<>(); // プレイヤー : 理由

    @Override
    public String description() {
        return "jaoium制限に関する処理を行います。";
    }

    /**
     * jaoiumと判定されるアイテムかどうか
     *
     * @param list PotionEffectのList
     *
     * @return jaoiumかどうか
     *
     * @author mine_book000
     */
    private boolean isjaoium(List<PotionEffect> list) {
        boolean jaoium = false;
        for (PotionEffect po : list) {
            if (po.getType().equals(PotionEffectType.HEAL)) {
                if (heal.contains(po.getAmplifier())) {
                    // アウト
                    jaoium = true;
                }
            }
            if (po.getType().equals(PotionEffectType.HEALTH_BOOST)) {
                if (health_boost.contains(po.getAmplifier())) {
                    // アウト
                    jaoium = true;
                }
            }
        }
        return jaoium;
    }

    /**
     * 悪意のあるアイテムかどうか
     *
     * @param potion PotionMeta
     *
     * @return 悪意のあるアイテムかどうか
     */
    private String isMalicious(PotionMeta potion) {
        Component component = potion.displayName();
        if (component == null) {
            return null;
        }
        String displayName = PlainComponentSerializer.plain().serialize(component);
        if (displayName.contains("§4§lDEATH")) {
            // Wurst?
            return "Wurst";
        }
        return null;
    }

    void saveItem(Player player, ItemStack is) {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("data", is);
        String yamlString = yaml.saveToString();
        String nbt = NMSManager.getNBT(is);
        String output = nbt + "\n\n" + yamlString;

        File saveDir = new File(Main.getJavaPlugin().getDataFolder(), "jaoium");
        if (!saveDir.exists()) {
            boolean bool = saveDir.mkdirs();
            System.out.println("Create jaoium data directory: " + bool);
            if (!bool) return;
        }

        String hash = DigestUtils.md5Hex(output);
        if (sendHashes.contains(hash)) {
            return;
        }
        sendHashes.add(hash);
        File file = new File(saveDir, hash + ".txt");
        boolean exists = file.exists();
        if (!file.exists()) {
            try {
                //noinspection UnstableApiUsage
                Files.write(output, file, Charset.defaultCharset());
            } catch (IOException e) {
                reportError(getClass(), e);
            }
        }

        if (Main.getMyMaidConfig().getJDA() == null) {
            return;
        }

        TextChannel channel = Main.getMyMaidConfig().getJDA().getTextChannelById(837137823177768990L); // #jaoium-items
        if (channel == null) {
            return;
        }

        channel.sendMessage("`" + player.getName() + "` - " + sdfFormat(new Date()) + " | `" + hash + "` (exists: `" + exists + "`)").queue();
        channel.sendFile(file, hash + ".txt").queue();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void OnPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity();
        Item item = event.getItem();
        ItemStack hand = item.getItemStack();
        if (hand.getType() != Material.SPLASH_POTION && hand.getType() != Material.LINGERING_POTION) {
            return;
        }
        PotionMeta potion = (PotionMeta) hand.getItemMeta();
        if (!isjaoium(potion.getCustomEffects())) {
            return;
        }
        player.sendMessage("[jaoium_Checker] " + ChatColor.GREEN
            + "あなたはjaoiumを拾いました。何か行動をする前に/clearをしないと、自動的に投獄されてしまうかもしれません！");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void InvClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        Inventory inv = event.getInventory();
        Inventory click_inv = event.getClickedInventory();
        ItemStack[] click_is = click_inv != null ? click_inv.getContents() : new ItemStack[]{};

        boolean isMatched = false;
        String malicious = null;

        Optional<ItemStack> matched = Arrays.stream(inv.getContents())
            .filter(Objects::nonNull)
            .filter(i -> i.getType() == Material.SPLASH_POTION || i.getType() == Material.LINGERING_POTION)
            .filter(i -> isjaoium(((PotionMeta) i.getItemMeta()).getCustomEffects()))
            .findFirst();
        if (matched.isPresent()) {
            // jaoium有
            saveItem(player, matched.get());
            inv.clear();
            isMatched = true;
            malicious = isMalicious((PotionMeta) matched.get().getItemMeta());
        }

        Optional<ItemStack> click_matched = Arrays.stream(click_is)
            .filter(Objects::nonNull)
            .filter(i -> i.getType() == Material.SPLASH_POTION || i.getType() == Material.LINGERING_POTION)
            .filter(i -> isjaoium(((PotionMeta) i.getItemMeta()).getCustomEffects()))
            .findFirst();
        if (click_matched.isPresent()) {
            // jaoium有
            saveItem(player, click_matched.get());
            click_inv.clear();
            isMatched = true;
            malicious = isMalicious((PotionMeta) click_matched.get().getItemMeta());
        }

        if (!isMatched) {
            return;
        }

        Jail jail = Jail.getInstance(player);
        if (jail.isStatus()) {
            return;
        }
        EBan eban = EBan.getInstance(player);
        if (eban.isStatus()) {
            return;
        }

        checkjaoiumLocation(player);
        Achievementjao.getAchievementAsync(player, Achievement.DRUGADDICTION);
        player.getInventory().clear();
        if (malicious != null) {
            eban.addBan("jaotan", String.format("禁止クライアントMod「%s」使用の疑い。方針「クライアントModの導入・利用に関する規則」の「禁止事項」への違反", malicious));
        } else {
            jail.addBan("jaotan", "jaoium所持");
            new Task_AutoRemoveJailByjaoium(player).runTaskLater(Main.getJavaPlugin(), 1200L); // 60s
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerItemHeldEvent(PlayerItemHeldEvent event) {
        check(event, event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        check(event, event.getPlayer());
    }

    @EventHandler
    public void onProjectileLaunchEvent(ProjectileLaunchEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player)) {
            return;
        }
        check(event, (Player) event.getEntity().getShooter());
    }

    @EventHandler
    public void onPotionSplashEvent(PotionSplashEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player)) {
            return;
        }
        check(event, (Player) event.getEntity().getShooter());
    }

    @EventHandler
    public void OnBlockDispenseEvent(BlockDispenseEvent event) {
        ItemStack is = event.getItem();

        if (is.getType() != Material.SPLASH_POTION && is.getType() != Material.LINGERING_POTION) {
            return;
        }

        PotionMeta potion = (PotionMeta) is.getItemMeta();
        if (isjaoium(potion.getCustomEffects())) {
            event.setCancelled(true);
        }
    }

    void check(Cancellable event, Player player) {
        Inventory inv = player.getInventory();
        Inventory ender_inv = player.getEnderChest();

        boolean isMatched = false;
        String malicious = null;

        Optional<ItemStack> matched = Arrays.stream(inv.getContents())
            .filter(Objects::nonNull)
            .filter(i -> i.getType() == Material.SPLASH_POTION || i.getType() == Material.LINGERING_POTION)
            .filter(i -> isjaoium(((PotionMeta) i.getItemMeta()).getCustomEffects()))
            .findFirst();
        if (matched.isPresent()) {
            // jaoium有
            saveItem(player, matched.get());
            event.setCancelled(true);
            inv.clear();
            isMatched = true;
            malicious = isMalicious((PotionMeta) matched.get().getItemMeta());
        }

        Optional<ItemStack> ender_matched = Arrays.stream(ender_inv.getContents())
            .filter(Objects::nonNull)
            .filter(i -> i.getType() == Material.SPLASH_POTION || i.getType() == Material.LINGERING_POTION)
            .filter(i -> isjaoium(((PotionMeta) i.getItemMeta()).getCustomEffects()))
            .findFirst();
        if (ender_matched.isPresent()) {
            // jaoium有
            saveItem(player, ender_matched.get());
            event.setCancelled(true);
            inv.clear();
            isMatched = true;
            malicious = isMalicious((PotionMeta) ender_matched.get().getItemMeta());
        }

        if (!isMatched) {
            return;
        }

        Jail jail = Jail.getInstance(player);
        if (jail.isStatus()) {
            return;
        }
        EBan eban = EBan.getInstance(player);
        if (eban.isStatus()) {
            return;
        }

        checkjaoiumLocation(player);
        Achievementjao.getAchievementAsync(player, Achievement.DRUGADDICTION);
        player.getInventory().clear();
        if (malicious != null) {
            eban.addBan("jaotan", String.format("禁止クライアントMod「%s」使用の疑い。方針「クライアントModの導入・利用に関する規則」の「禁止事項」への違反", malicious));
        } else {
            jail.addBan("jaotan", "jaoium所持");
            new Task_AutoRemoveJailByjaoium(player).runTaskLater(Main.getJavaPlugin(), 1200L); // 60s
        }
    }

    private void checkjaoiumLocation(Player player) {
        Location loc = player.getLocation();
        String reason = "null";
        if (Reason.containsKey(player.getName())) {
            reason = Reason.get(player.getName());
            Reason.remove(player.getName());
        }

        if (Main.getMyMaidConfig().getJDA() == null) {
            return;
        }

        TextChannel channel = Main.getMyMaidConfig().getJDA().getTextChannelById(837137823177768990L); // #jaoium-items
        if (channel == null) {
            return;
        }

        channel.sendMessage("**jaoium Location & Reason Notice**\n"
            + "Player: " + player.getName() + "\n"
            + "Location: " + loc.getWorld().getName() + " " + loc.getBlockX() + " " + loc.getBlockY() + " "
            + loc.getBlockZ() + "\n"
            + "Reason: `" + reason + "`").queue();
    }

    /* ---------------- どうやって入手した？ ---------------- */

    @EventHandler(priority = EventPriority.MONITOR)
    public void ByPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage();

        if (!command.startsWith("/give")) {
            return;
        }
        if (command.equalsIgnoreCase("/give")) {
            return;
        }
        String[] commands = command.split(" ", 0);
        if (commands.length < 3) {
            return;
        }

        String item = commands[2];
        if (!item.startsWith("splash_potion") && !item.startsWith("minecraft:splash_potion")) {
            return;
        }

        String selector = commands[1];
        boolean SelectorToMe = false;
        boolean ALLPlayer = false;
        String ToPlayer = "";
        if (selector.equalsIgnoreCase("@p")) {
            // 自分
            SelectorToMe = true;
        } else if (selector.equalsIgnoreCase(player.getName())) {
            // 自分
            SelectorToMe = true;
        } else if (selector.equalsIgnoreCase("@a")) {
            // 自分(プレイヤーすべて)
            SelectorToMe = true;
            ALLPlayer = true;
        } else if (selector.equalsIgnoreCase("@e")) {
            // 自分(エンティティすべて)
            SelectorToMe = true;
            ALLPlayer = true;
        } else if (selector.equalsIgnoreCase("@s")) {
            // 自分(実行者)
            SelectorToMe = true;
        } else {
            Player p = Bukkit.getPlayer(selector);
            if (p != null) {
                ToPlayer = selector;
            }
        }
        if (SelectorToMe) {
            Reason.put(player.getName(), player.getName() + "の実行したコマンド : " + command);
        }
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (ToPlayer.equalsIgnoreCase(p.getName())) {
                Reason.put(p.getName(), player.getName() + "の実行したコマンド : " + command);
                continue;
            }
            if (ALLPlayer) {
                Reason.put(p.getName(), player.getName() + "の実行したコマンド : " + command);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void ByCommandBlock(ServerCommandEvent event) {
        if (!(event.getSender() instanceof BlockCommandSender))
            return;
        BlockCommandSender sender = (BlockCommandSender) event.getSender();

        if (!(sender.getBlock().getState() instanceof CommandBlock))
            return;
        CommandBlock cmdb = (CommandBlock) sender.getBlock().getState();

        String command = cmdb.getCommand();
        if (!command.startsWith("/give") && !command.startsWith("give")) {
            return;
        }
        if (command.equalsIgnoreCase("/give") || command.equalsIgnoreCase("give")) {
            return;
        }
        String[] commands = command.split(" ", 0);
        if (commands.length < 3) {
            return;
        }

        String item = commands[2];
        if (!item.startsWith("splash_potion") && !item.startsWith("minecraft:splash_potion")) {
            return;
        }

        String selector = commands[1];
        boolean ALLPlayer = false;
        String ToPlayer = null;
        if (selector.equalsIgnoreCase("@p")) {
            // 一番近い
            Player p = getNearestPlayer(cmdb.getLocation());
            if (p == null) {
                return;
            }
            ToPlayer = p.getName();
        } else if (selector.equalsIgnoreCase("@a")) {
            // プレイヤーすべて
            ALLPlayer = true;
        } else if (selector.equalsIgnoreCase("@e")) {
            // エンティティすべて
            ALLPlayer = true;
        } else {
            Player p = Bukkit.getPlayer(selector);
            if (p != null) {
                ToPlayer = selector;
            }
        }
        if (ToPlayer == null && !ALLPlayer) {
            return;
        }
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (ToPlayer != null && ToPlayer.equalsIgnoreCase(p.getName())) {
                Reason.put(p.getName(),
                    "コマンドブロック(" + cmdb.getLocation().getWorld().getName() + " " + cmdb.getLocation().getBlockX()
                        + " " + cmdb.getLocation().getBlockY() + " " + cmdb.getLocation().getBlockZ()
                        + ")の実行したコマンド : " + command);
                continue;
            }
            if (ALLPlayer) {
                Reason.put(p.getName(),
                    "コマンドブロック(" + cmdb.getLocation().getWorld().getName() + " " + cmdb.getLocation().getBlockX()
                        + " " + cmdb.getLocation().getBlockY() + " " + cmdb.getLocation().getBlockZ()
                        + ")の実行したコマンド : " + command);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void ByItemPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity();
        Item item = event.getItem();
        ItemStack hand = item.getItemStack();
        if (hand.getType() == Material.SPLASH_POTION || hand.getType() == Material.LINGERING_POTION) {
            PotionMeta potion = (PotionMeta) hand.getItemMeta();
            if (isjaoium(potion.getCustomEffects())) {
                Reason.put(player.getName(),
                    player.getLocation().getWorld().getName() + " " + player.getLocation().getBlockX() + " "
                        + player.getLocation().getBlockY() + " " + player.getLocation().getBlockZ()
                        + "にて拾ったアイテム");
            }
        }
    }

    @EventHandler
    public void PlayerCreativeInv(InventoryCreativeEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack hand = event.getCurrentItem();
        if (hand == null) {
            return;
        }
        if (hand.getType() == Material.SPLASH_POTION || hand.getType() == Material.LINGERING_POTION) {
            PotionMeta potion = (PotionMeta) hand.getItemMeta();
            if (isjaoium(potion.getCustomEffects())) {
                Reason.put(player.getName(), "クリエイティブインベントリから取得したアイテム(保存したツールバーからの可能性あり)　DebugDATA: "
                    + event.getAction().name() + " / " + event.getClick().name() + " / " + event.getHotbarButton());
            }
        }
    }
}
