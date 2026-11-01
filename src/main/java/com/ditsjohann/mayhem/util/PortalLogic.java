package com.ditsjohann.mayhem.util;

import com.ditsjohann.mayhem.entity.PortalEntity;
import com.ditsjohann.mayhem.registry.ModDimensions;
import com.ditsjohann.mayhem.registry.ModEntities;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class PortalLogic {
    private static final List<RegistryKey<World>> SCRAMBLE_POOL = List.of(World.OVERWORLD, World.NETHER, ModDimensions.GLOOPVERSE, ModDimensions.MEATVERSE);

    public record Target(ServerWorld world, BlockPos feet) {
    }

    @Nullable
    public static Target resolve(ServerWorld origin, BlockPos from, @Nullable ServerPlayerEntity owner, PortalDestination dest, @Nullable GlobalPos card) {
        MinecraftServer server = origin.getServer();
        ServerWorld world;
        BlockPos approx;
        if (card != null) {
            world = server.getWorld(card.dimension());
            if (world == null) return null;
            approx = card.pos().up();
        } else if (dest == PortalDestination.HOME) {
            if (owner != null && owner.getSpawnPointPosition() != null && server.getWorld(owner.getSpawnPointDimension()) != null) {
                world = server.getWorld(owner.getSpawnPointDimension());
                approx = owner.getSpawnPointPosition().up();
            } else {
                world = server.getOverworld();
                approx = world.getSpawnPos();
            }
        } else if (dest == PortalDestination.SCRAMBLE) {
            world = server.getWorld(SCRAMBLE_POOL.get(origin.random.nextInt(SCRAMBLE_POOL.size())));
            if (world == null) world = server.getOverworld();
            approx = scaled(origin, world, from).add(origin.random.nextBetween(-1500, 1500), 0, origin.random.nextBetween(-1500, 1500));
        } else {
            if (dest.world == null) return null;
            world = server.getWorld(dest.world);
            if (world == null) return null;
            approx = scaled(origin, world, from);
            if (dest == PortalDestination.END) approx = new BlockPos(origin.random.nextBetween(-60, 60), 70, origin.random.nextBetween(-60, 60));
        }
        approx = world.getWorldBorder().clamp(approx.getX(), approx.getY(), approx.getZ());
        BlockPos feet = SafeSpotFinder.find(world, approx);
        return new Target(world, feet);
    }

    private static BlockPos scaled(ServerWorld from, ServerWorld to, BlockPos pos) {
        double f = DimensionType.getCoordinateScaleFactor(from.getDimension(), to.getDimension());
        return BlockPos.ofFloored(pos.getX() * f, pos.getY(), pos.getZ() * f);
    }

    /**
     * Opens a linked pair of portals: one at {@code at} in {@code origin}, one at the resolved destination.
     */
    public static boolean openPair(ServerWorld origin, Vec3d at, float yaw, @Nullable ServerPlayerEntity owner, PortalDestination dest, @Nullable GlobalPos card, int lifetime, int variant) {
        Target target = resolve(origin, BlockPos.ofFloored(at), owner, dest, card);
        if (target == null) {
            if (owner != null) owner.sendMessage(Text.translatable("message.mayhem.portal_failed").formatted(Formatting.RED), true);
            return false;
        }
        Vec3d exitPos = Vec3d.ofBottomCenter(target.feet());
        target.world().getChunkManager().addTicket(ChunkTicketType.PORTAL, new ChunkPos(target.feet()), 3, target.feet());

        PortalEntity entry = ModEntities.PORTAL.create(origin);
        PortalEntity exit = ModEntities.PORTAL.create(target.world());
        if (entry == null || exit == null) return false;

        entry.refreshPositionAndAngles(at.x, at.y, at.z, yaw, 0);
        entry.setLifetime(lifetime);
        entry.setVariant(variant);
        entry.setDestination(target.world().getRegistryKey(), exitPos, yaw);

        exit.refreshPositionAndAngles(exitPos.x, exitPos.y, exitPos.z, yaw, 0);
        exit.setLifetime(lifetime);
        exit.setVariant(variant);
        exit.setDestination(origin.getRegistryKey(), at, yaw + 180f);

        origin.spawnEntity(entry);
        target.world().spawnEntity(exit);
        return true;
    }

    private PortalLogic() {
    }
}
