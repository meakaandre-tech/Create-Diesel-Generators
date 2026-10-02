package com.jesz.createdieselgenerators;

import com.jesz.createdieselgenerators.content.distillation.DistillationTankBlockEntity;
import com.jesz.createdieselgenerators.content.pumpjack.PumpjackCrankBlockEntity;
import com.jesz.createdieselgenerators.content.pumpjack.PumpjackHoleBlockEntity;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

/**
 * Client-only behaviour that common classes trigger. The client entrypoint fills these in;
 * on a dedicated server they stay as no-ops.
 */
public class ClientHooks {
    @FunctionalInterface
    public interface PouringLiquid {
        void spawn(Level level, BlockPos pos, FluidStack fluid);
    }

    @FunctionalInterface
    public interface SprayParticle {
        void spawn(Level level, FluidStack fluid, double x, double y, double z, double dx, double dy, double dz);
    }

    /** Spawns the fluid particle trailing a chemical sprayer projectile. */
    public static SprayParticle SPRAY_PARTICLE = (level, fluid, x, y, z, dx, dy, dz) -> {};
    /** Ticks the looping engine sound of an engine block entity. */
    public static Consumer<SmartBlockEntity> ENGINE_SOUND_TICK = be -> {};
    /** Ticks the looping bubbling sound of a pumpjack hole. */
    public static Consumer<PumpjackHoleBlockEntity> PUMPJACK_SOUND_TICK = be -> {};
    /** Ticks the bubbling sound of a running distillation tower. */
    public static Consumer<DistillationTankBlockEntity> DISTILLATION_SOUND_TICK = be -> {};
    /** Ticks the sounds of a pumpjack crank. */
    public static Consumer<PumpjackCrankBlockEntity> CRANK_SOUND_TICK = be -> {};
    /** Ticks the engine sound of a diesel engine riding on a train. */
    public static Consumer<MovementContext> TRAIN_ENGINE_TICK = context -> {};
    /** Spawns the pouring-liquid particles of a pumpjack hole. */
    public static PouringLiquid PUMPJACK_POURING = (level, pos, fluid) -> {};
}
