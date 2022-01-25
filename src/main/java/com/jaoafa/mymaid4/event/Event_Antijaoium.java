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

package com.jaoafa.mymaid4.event;

import com.jaoafa.jaosuperachievement2.api.Achievementjao;
import com.jaoafa.jaosuperachievement2.lib.Achievement;
import com.jaoafa.mymaid4.Main;
import com.jaoafa.mymaid4.lib.*;
import com.jaoafa.mymaid4.tasks.Task_AutoRemoveJailByjaoium;
import net.dv8tion.jda.api.entities.TextChannel;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

public class Event_Antijaoium extends MyMaidLibrary implements Listener, EventPremise {
    final Set<String> sendHashes = new HashSet<>();
    final Map<String, String> Reason = new HashMap<>(); // プレイヤー : 理由

    @Override
    public String description() {
        return "jaoium制限に関する処理を行います。";
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
        String displayName = PlainTextComponentSerializer.plainText().serialize(component);
        if (displayName.contains("§4§lDEATH")) {
            // Wurst?
            return "Wurst";
        }
        if (displayName.contains("§4§lINSTANT DEATH")) {
            // Wurst
            // https://github.com/Wurst-Imperium/Wurst7/blob/8572e76dfe6851cc88156aab9e4d41fa2d4aa272/src/main/java/net/wurstclient/hacks/KillPotionHack.java#L57
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
            Main.getMyMaidLogger().info("Create jaoium data directory: " + bool);
            if (!bool) return;
        }

        String hash = DigestUtils.md5Hex(output);
        if (sendHashes.contains(hash)) {
            return;
        }
        sendHashes.add(hash);
        File file = new File(saveDir, hash + ".txt");
        boolean exists = file.exists();
        if (!exists) {
            try {
                Files.writeString(file.toPath(), output);
            } catch (IOException e) {
                reportError(getClass(), e);
            }
        }

        boolean isWarning = false;
        String displayName = null;
        if (is.getType() == Material.SPLASH_POTION || is.getType() == Material.LINGERING_POTION) {
            PotionMeta meta = (PotionMeta) is.getItemMeta();
            Component componentDisplayName = meta.displayName();
            displayName = componentDisplayName != null ? PlainTextComponentSerializer.plainText().serialize(componentDisplayName) : "";
            List<Component> componentLore = meta.lore();
            String lore = componentLore != null ? componentLore.stream().map(c -> PlainTextComponentSerializer.plainText().serialize(c)).collect(Collectors.joining()) : "";
            if (!displayName.contains("jaoium") && !lore.contains("jaoium")) {
                isWarning = true;
            }
            if (exists) {
                return;
            }
        }

        if (Main.getMyMaidConfig().getJDA() == null) {
            return;
        }

        TextChannel channel = Main.getMyMaidConfig().getJDA().getTextChannelById(837137823177768990L); // #jaoium-items
        if (channel == null) {
            return;
        }

        channel.sendMessage(MessageFormat.format("`{0}` - {1} | `{2}` (exists: `{3}`){4}",
            player.getName(),
            sdfFormat(new Date()),
            hash,
            exists,
            isWarning ? String.format("\n**[警告]** jaoiumという文字列が含まれていません: `%s`", displayName) : "")).queue();
        channel.sendFile(file, hash + ".txt").queue();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void OnPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
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
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        Inventory click_inv = event.getClickedInventory();

        check(event, player, click_inv, false);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerItemHeldEvent(PlayerItemHeldEvent event) {
        check(event, event.getPlayer(), null, true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        check(event, event.getPlayer(), null, true);
    }

    @EventHandler
    public void onProjectileLaunchEvent(ProjectileLaunchEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player)) {
            return;
        }
        check(event, (Player) event.getEntity().getShooter(), null, true);
    }

    @EventHandler
    public void onPotionSplashEvent(PotionSplashEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player)) {
            return;
        }
        check(event, (Player) event.getEntity().getShooter(), null, true);
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

    void check(Cancellable event, Player player, Inventory clickedChest, boolean checkEnderChest) {
        Inventory inv = player.getInventory();
        Inventory ender_inv = player.getEnderChest();

        boolean isMatched = false;
        String malicious = null;
        ItemStack is = null;

        Optional<ItemStack> matched = Arrays.stream(inv.getContents())
            .filter(Objects::nonNull)
            .filter(i -> i.getType() == Material.SPLASH_POTION || i.getType() == Material.LINGERING_POTION)
            .filter(i -> isjaoium(((PotionMeta) i.getItemMeta()).getCustomEffects()))
            .findFirst();
        if (matched.isPresent()) {
            // jaoium有
            saveItem(player, matched.get());
            is = matched.get();
            event.setCancelled(true);
            inv.clear();
            isMatched = true;
            malicious = isMalicious((PotionMeta) matched.get().getItemMeta());
        }

        if (clickedChest != null) {
            Optional<ItemStack> clicked_matched = Arrays.stream(clickedChest.getContents())
                .filter(Objects::nonNull)
                .filter(i -> i.getType() == Material.SPLASH_POTION || i.getType() == Material.LINGERING_POTION)
                .filter(i -> isjaoium(((PotionMeta) i.getItemMeta()).getCustomEffects()))
                .findFirst();
            if (clicked_matched.isPresent()) {
                // jaoium有
                saveItem(player, clicked_matched.get());
                is = clicked_matched.get();
                event.setCancelled(true);
                clickedChest.clear();
                isMatched = true;
                malicious = isMalicious((PotionMeta) clicked_matched.get().getItemMeta());
            }
        }

        if (checkEnderChest) {
            Optional<ItemStack> ender_matched = Arrays.stream(ender_inv.getContents())
                .filter(Objects::nonNull)
                .filter(i -> i.getType() == Material.SPLASH_POTION || i.getType() == Material.LINGERING_POTION)
                .filter(i -> isjaoium(((PotionMeta) i.getItemMeta()).getCustomEffects()))
                .findFirst();
            if (ender_matched.isPresent()) {
                // jaoium有
                saveItem(player, ender_matched.get());
                is = ender_matched.get();
                event.setCancelled(true);
                ender_inv.clear();
                isMatched = true;
                malicious = isMalicious((PotionMeta) ender_matched.get().getItemMeta());
            }
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

        PotionMeta meta = (PotionMeta) is.getItemMeta();
        Component componentDisplayName = meta.displayName();
        String displayName = componentDisplayName != null ? PlainTextComponentSerializer.plainText().serialize(componentDisplayName) : "";
        List<Component> componentLore = meta.lore();
        String lore = componentLore != null ? componentLore.stream().map(c -> PlainTextComponentSerializer.plainText().serialize(c)).collect(Collectors.joining()) : "";
        if (!displayName.contains("jaoium") && !lore.contains("jaoium") && !isAMRV(player)) {
            Historyjao.getHistoryjao(player).autoAdd("jaoiumの所持・使用", "(" + displayName + ")");
        }

        checkjaoiumLocation(player);
        Achievementjao.getAchievementAsync(player, Achievement.DRUGADDICTION);
        player.getInventory().clear();
        if (malicious != null) {
            eban.addBan("jaotan", String.format("禁止クライアントMod「%s」使用の疑い。サーバルール内「Modについて」の「禁止事項」への違反", malicious));
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
            Player p = Bukkit.getPlayerExact(selector);
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
        if (!(event.getSender() instanceof BlockCommandSender sender))
            return;

        if (!(sender.getBlock().getState() instanceof CommandBlock cmdb))
            return;

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
            Player p = Bukkit.getPlayerExact(selector);
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
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
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
