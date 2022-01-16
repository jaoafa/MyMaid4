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
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleCreateEvent;

public class Event_AntiTNTCart extends MyMaidLibrary implements Listener, EventPremise {
    @Override
    public String description() {
        return "TNTカートを設置できないようにします。";
    }

    @EventHandler
    public void onVehicleCreateEvent(VehicleCreateEvent event) {
        Vehicle vehicle = event.getVehicle();
        if (vehicle.getType() != EntityType.MINECART_TNT) {
            return;
        }
        Location loc = vehicle.getLocation();
        loc = loc.add(0, 0.5, 0);
        loc.getWorld().spawnParticle(Particle.BLOCK_MARKER, loc, 1, 0, 0, 0, Material.BARRIER.createBlockData());
        vehicle.remove();
    }
}
