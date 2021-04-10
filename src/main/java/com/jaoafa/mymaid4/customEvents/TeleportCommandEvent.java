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

package com.jaoafa.mymaid4.customEvents;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TeleportCommandEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final CommandSender sender;
    @NotNull
    private final Player fromPlayer;
    private Player toPlayer;
    private Location location;
    private Location facingLocation;
    private Player facingPlayer;
    private FacingAnchor facingAnchor;
    private boolean isCanceled = false;

    /**
     * TeleportCommandEventを作成
     *
     * @param sender   コマンド実行者
     * @param toPlayer テレポート先のプレイヤー
     */
    public TeleportCommandEvent(@NotNull Player sender, @NotNull Player toPlayer) {
        this.sender = sender;
        this.fromPlayer = sender;
        this.toPlayer = toPlayer;
    }

    /**
     * TeleportCommandEventを作成
     *
     * @param sender     コマンド実行者
     * @param fromPlayer テレポートするプレイヤー
     * @param toPlayer   テレポート先のプレイヤー
     */
    public TeleportCommandEvent(@NotNull CommandSender sender, @NotNull Player fromPlayer, @NotNull Player toPlayer) {
        this.sender = sender;
        this.fromPlayer = fromPlayer;
        this.toPlayer = toPlayer;
    }

    /**
     * TeleportCommandEventを作成
     *
     * @param sender   コマンド実行者
     * @param location テレポート先の座標
     */
    public TeleportCommandEvent(@NotNull Player sender, @NotNull Location location) {
        this.sender = sender;
        this.fromPlayer = sender;
        this.location = location;
    }

    /**
     * TeleportCommandEventを作成
     *
     * @param sender     コマンド実行者
     * @param fromPlayer テレポートするプレイヤー
     * @param location   テレポート先の座標
     */
    public TeleportCommandEvent(@NotNull CommandSender sender, @NotNull Player fromPlayer, @NotNull Location location) {
        this.sender = sender;
        this.fromPlayer = fromPlayer;
        this.location = location;
    }

    /**
     * TeleportCommandEventを作成
     *
     * @param sender         コマンド実行者
     * @param fromPlayer     テレポートするプレイヤー
     * @param location       テレポート先の座標
     * @param facingLocation 向く座標
     */
    public TeleportCommandEvent(@NotNull CommandSender sender, @NotNull Player fromPlayer, @NotNull Location location, @NotNull Location facingLocation) {
        this.sender = sender;
        this.fromPlayer = fromPlayer;
        this.location = location;
        this.facingLocation = facingLocation;
    }

    /**
     * TeleportCommandEventを作成
     *
     * @param sender       コマンド実行者
     * @param fromPlayer   テレポートするプレイヤー
     * @param location     テレポート先の座標
     * @param facingPlayer 向くプレイヤー
     */
    public TeleportCommandEvent(@NotNull CommandSender sender, @NotNull Player fromPlayer, @NotNull Location location, @NotNull Player facingPlayer) {
        this.sender = sender;
        this.fromPlayer = fromPlayer;
        this.location = location;
        this.facingPlayer = facingPlayer;
    }

    /**
     * TeleportCommandEventを作成
     *
     * @param sender       コマンド実行者
     * @param fromPlayer   テレポートするプレイヤー
     * @param location     テレポート先の座標
     * @param facingPlayer 向くプレイヤー
     * @param facingAnchor 目と足どちらを向くか
     */
    public TeleportCommandEvent(@NotNull CommandSender sender, @NotNull Player fromPlayer, @NotNull Location location, @NotNull Player facingPlayer, @NotNull FacingAnchor facingAnchor) {
        this.sender = sender;
        this.fromPlayer = fromPlayer;
        this.location = location;
        this.facingPlayer = facingPlayer;
        this.facingAnchor = facingAnchor;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public CommandSender getSender() {
        return sender;
    }

    public @NotNull Player getFromPlayer() {
        return fromPlayer;
    }

    @Nullable
    public Player getToPlayer() {
        return toPlayer;
    }

    @Nullable
    public Location getLocation() {
        return location;
    }

    @Nullable
    public Location getFacingLocation() {
        return facingLocation;
    }

    @Nullable
    public Player getFacingPlayer() {
        return facingPlayer;
    }

    @Nullable
    public FacingAnchor getFacingAnchor() {
        return facingAnchor;
    }

    @Override
    public String toString() {
        return "TeleportCommandEvent{" +
            "sender=" + sender +
            ", fromPlayer=" + fromPlayer +
            ", toPlayer=" + toPlayer +
            ", location=" + location +
            ", facingLocation=" + facingLocation +
            ", facingPlayer=" + facingPlayer +
            ", facingAnchor=" + facingAnchor +
            '}';
    }

    @Override
    public boolean isCancelled() {
        return isCanceled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        isCanceled = cancel;
    }

    public enum FacingAnchor {
        EYES, FEET
    }
}