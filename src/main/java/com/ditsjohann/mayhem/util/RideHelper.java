package com.ditsjohann.mayhem.util;

import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Players do not normally learn about their own passengers, so we tell them.
 */
public final class RideHelper {
    public static boolean mountOnHead(Entity rider, ServerPlayerEntity player) {
        if (!player.getPassengerList().isEmpty()) return false;
        boolean ok = rider.startRiding(player, true);
        if (ok) sync(player);
        return ok;
    }

    public static void dismount(Entity rider) {
        Entity vehicle = rider.getVehicle();
        rider.stopRiding();
        if (vehicle instanceof ServerPlayerEntity player) sync(player);
    }

    public static void sync(ServerPlayerEntity player) {
        player.networkHandler.sendPacket(new EntityPassengersSetS2CPacket(player));
    }

    private RideHelper() {
    }
}
