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

import com.jaoafa.mymaid4.lib.EventPremise;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFormEvent;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Event_DisableConcreteTransition extends MyMaidLibrary implements Listener, EventPremise {
    @Override
    public String description() {
        return "コンクリートパウダーの変化を無効化します。";
    }

    @EventHandler
    public void onBlockFormEvent(BlockFormEvent event) {
        Block block = event.getBlock();
        if (!isConcretePowder(block.getType())) {
            return;
        }
        Location loc = block.getLocation();
        List<Particle> particles = Arrays.stream(Particle.values())
            .filter(p -> p.getDataType() == Void.class)
            .filter(p -> !p.name().startsWith("LEGACY_")).toList();

        Random rnd = new Random();
        int i = rnd.nextInt(particles.size());
        Particle particle = particles.get(i);

        loc.getWorld().spawnParticle(particle, loc, 30, 0.3, 0, 0.3);
        event.setCancelled(true);
    }

    boolean isConcretePowder(Material material) {
        List<Material> concretes = Arrays.asList(
            Material.WHITE_CONCRETE_POWDER,
            Material.ORANGE_CONCRETE_POWDER,
            Material.MAGENTA_CONCRETE_POWDER,
            Material.LIGHT_BLUE_CONCRETE_POWDER,
            Material.YELLOW_CONCRETE_POWDER,
            Material.LIME_CONCRETE_POWDER,
            Material.PINK_CONCRETE_POWDER,
            Material.GRAY_CONCRETE_POWDER,
            Material.LIGHT_GRAY_CONCRETE_POWDER,
            Material.CYAN_CONCRETE_POWDER,
            Material.PURPLE_CONCRETE_POWDER,
            Material.BLUE_CONCRETE_POWDER,
            Material.BROWN_CONCRETE_POWDER,
            Material.GREEN_CONCRETE_POWDER,
            Material.RED_CONCRETE_POWDER,
            Material.BLACK_CONCRETE_POWDER);

        return concretes.contains(material);
    }
}