package com.ditsjohann.mayhem.registry;

import com.ditsjohann.mayhem.MultiverseMayhem;
import com.mojang.serialization.Codec;
import net.minecraft.component.ComponentType;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.math.GlobalPos;

import java.util.function.UnaryOperator;

public final class ModComponents {
    /** Remaining portal charges inside a portal gun. */
    public static final ComponentType<Integer> CHARGE = register("charge",
            b -> b.codec(Codec.INT).packetCodec(PacketCodecs.VAR_INT));
    /** Index into {@link com.ditsjohann.mayhem.util.PortalDestination}. */
    public static final ComponentType<Integer> DESTINATION = register("destination",
            b -> b.codec(Codec.INT).packetCodec(PacketCodecs.VAR_INT));
    /** Generic gadget mode toggle (size ray grow/shrink etc.). */
    public static final ComponentType<Integer> MODE = register("mode",
            b -> b.codec(Codec.INT).packetCodec(PacketCodecs.VAR_INT));
    /** Location stored on a coordinate card. */
    public static final ComponentType<GlobalPos> STORED_POS = register("stored_pos",
            b -> b.codec(GlobalPos.CODEC).packetCodec(GlobalPos.PACKET_CODEC));

    private static <T> ComponentType<T> register(String name, UnaryOperator<ComponentType.Builder<T>> op) {
        return Registry.register(Registries.DATA_COMPONENT_TYPE, MultiverseMayhem.id(name), op.apply(ComponentType.builder()).build());
    }

    public static void init() {
    }

    private ModComponents() {
    }
}
