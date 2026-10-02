package com.jesz.createdieselgenerators.client.sound;

import com.jesz.createdieselgenerators.CDGConfig;
import com.jesz.createdieselgenerators.CDGSoundEvents;
import com.jesz.createdieselgenerators.content.diesel_engine.IEngine;
import com.jesz.createdieselgenerators.content.diesel_engine.huge.HugeDieselEngineBlockEntity;
import com.jesz.createdieselgenerators.content.diesel_engine.modular.ModularDieselEngineBlock;
import com.jesz.createdieselgenerators.content.diesel_engine.modular.ModularDieselEngineBlockEntity;
import com.jesz.createdieselgenerators.content.distillation.DistillationTankBlockEntity;
import com.jesz.createdieselgenerators.content.pumpjack.PumpjackCrankBlockEntity;
import com.jesz.createdieselgenerators.content.pumpjack.PumpjackHoleBlockEntity;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.content.trains.entity.CarriageContraption;
import com.zurrtum.create.content.trains.entity.CarriageContraptionEntity;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * Looping sounds of running machines. The block entities call in through ClientHooks every client tick;
 * the sound instances are kept here so the block entity classes stay free of client code.
 */
public class CDGClientSounds {
    private static final Map<BlockEntity, EngineSoundInstance> ENGINES = new WeakHashMap<>();
    private static final Map<BlockEntity, AbstractTickableSoundInstance> LOOPS = new WeakHashMap<>();
    private static final Map<BlockEntity, AbstractTickableSoundInstance> CRANK_HISS = new WeakHashMap<>();
    private static final Map<Pair<UUID, BlockPos>, EngineSoundInstance> TRAIN_ENGINES = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static <T extends SmartBlockEntity & IEngine> void engineTick(SmartBlockEntity raw) {
        if (!(raw instanceof IEngine))
            return;
        T be = (T) raw;
        IEngine engine = be;
        boolean huge = be instanceof HugeDieselEngineBlockEntity;
        boolean running = huge ? engine.enabled()
                : engine.enabled() && engine.getThrottle() > 0 && !(be instanceof KineticBlockEntity kbe && kbe.isOverStressed());
        EngineSoundInstance soundInstance = ENGINES.get(be);
        if (running) {
            Vec3 pos = Vec3.atCenterOf(be.getBlockPos());
            if (be instanceof ModularDieselEngineBlockEntity modular) {
                if (be.getBlockState().getValue(ModularDieselEngineBlock.FACING).getAxis() == Direction.Axis.X)
                    pos = pos.add((double) modular.length / 2 - 0.5, 0, 0);
                else
                    pos = pos.add(0, 0, (double) modular.length / 2 - 0.5);
            }
            boolean moved = be instanceof ModularDieselEngineBlockEntity && soundInstance != null
                    && (soundInstance.getX() != pos.x || soundInstance.getZ() != pos.z);
            if (soundInstance == null || soundInstance.isStopped() || moved) {
                soundInstance = new EngineSoundInstance(CDGSoundEvents.ENGINE_NORMAL.get(), SoundSource.NEUTRAL, pos, 0.2f);
                ENGINES.put(be, soundInstance);
                Minecraft.getInstance().getSoundManager().play(soundInstance);
            } else if (soundInstance.active()) {
                soundInstance.keepAlive();
                float pitch = engine.getUpgrade().getPitchMultiplier(be) * engine.getFuelSoundPitch();
                if (huge) {
                    soundInstance.setPitch(pitch / 2 * engine.getThrottle());
                    soundInstance.setVolume(engine.getUpgrade().getVolume(be) * engine.getThrottle());
                } else {
                    soundInstance.setPitch(pitch * engine.getThrottle());
                    soundInstance.setVolume(engine.getUpgrade().getVolume(be));
                }
            }
        } else if (soundInstance != null) {
            soundInstance.fadeOut();
            ENGINES.remove(be);
        }
    }

    private static void loop(Map<BlockEntity, AbstractTickableSoundInstance> map, BlockEntity be, boolean active,
                             SoundEvent event, float volume, float pitch, Vec3 pos, boolean restartWhenInactive) {
        AbstractTickableSoundInstance soundInstance = map.get(be);
        if (active) {
            if (soundInstance == null || soundInstance.isStopped() ||
                    (restartWhenInactive && !Minecraft.getInstance().getSoundManager().isActive(soundInstance))) {
                soundInstance = new LoopingSoundInstance(event, volume, pitch, pos);
                map.put(be, soundInstance);
                Minecraft.getInstance().getSoundManager().play(soundInstance);
            }
        } else if (soundInstance != null) {
            Minecraft.getInstance().getSoundManager().stop(soundInstance);
            map.remove(be);
        }
    }

    public static void pumpjackHoleTick(PumpjackHoleBlockEntity be) {
        boolean isActive = be.started && be.valid && be.oilAmount > 0;
        loop(LOOPS, be, isActive, SoundEvents.BUBBLE_COLUMN_WHIRLPOOL_AMBIENT, 0.2f, 0.4f,
                Vec3.atCenterOf(be.getBlockPos()), false);
    }

    public static void distillationTick(DistillationTankBlockEntity be) {
        boolean isProcessing = be.isController() && be.isBottom() && be.processingTime > -1;
        loop(LOOPS, be, isProcessing, SoundEvents.BUBBLE_COLUMN_UPWARDS_AMBIENT, 0.5f, 0.45f,
                Vec3.atCenterOf(be.getBlockPos().offset(be.getWidth() / 2, be.getHeight() / 2, be.getWidth() / 2)), false);
    }

    public static void crankTick(PumpjackCrankBlockEntity be) {
        boolean isActive = be.getSpeed() != 0 && be.getBearing() != null;
        Vec3 center = Vec3.atCenterOf(be.getBlockPos());
        loop(LOOPS, be, isActive, SoundEvents.MINECART_RIDING, 0.3f, 0.4f, center, true);
        loop(CRANK_HISS, be, isActive, SoundEvents.BLASTFURNACE_FIRE_CRACKLE, 0.1f, 1.2f, center, false);
        if (!isActive)
            return;
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null)
            return;

        float prevNorm = be.prevAngle % 360;
        float currNorm = be.angle % 360;

        boolean crossedBottom = (prevNorm < 10 && currNorm >= 10) || (prevNorm > 350 && currNorm <= 10);
        boolean crossedTop = (prevNorm < 190 && currNorm >= 190) || (prevNorm > 170 && currNorm <= 170);
        BlockPos pos = be.getBlockPos();
        double x = pos.getX() + 0.5, y = pos.getY() + 0.5, z = pos.getZ() + 0.5;

        if (crossedBottom) {
            level.playLocalSound(x, y, z, SoundEvents.PISTON_EXTEND, SoundSource.BLOCKS, 0.2f, 0.3f, false);
            level.playLocalSound(x, y, z, SoundEvents.ANVIL_HIT, SoundSource.BLOCKS, 0.1f, 0.5f, false);
        }

        if (crossedTop) {
            level.playLocalSound(x, y, z, SoundEvents.PISTON_CONTRACT, SoundSource.BLOCKS, 0.15f, 0.3f, false);
            level.playLocalSound(x, y, z, SoundEvents.CHAIN_STEP, SoundSource.BLOCKS, 0.1f, 0.6f, false);
        }

        if (crossedBottom && Math.random() < 0.3)
            level.playLocalSound(x, y, z, SoundEvents.IRON_DOOR_OPEN, SoundSource.BLOCKS, 0.15f, 0.5f, false);
    }

    public static void trainEngineTick(MovementContext context) {
        if (!(context.contraption instanceof CarriageContraption contraption)
                || !(contraption.entity instanceof CarriageContraptionEntity entity)
                || entity.getCarriage() == null || context.position == null)
            return;
        if (!CDGConfig.ENGINES_EMIT_SOUND_ON_TRAINS.get() || entity.getCarriage().train.derailed)
            return;

        double trainSpeed = context.motion.length() * 2 / (entity.getCarriage().train.maxSpeed() / 28);
        double lastTrainSpeed = context.data.getDoubleOr("TrainSpeed", 0);
        double acceleration = trainSpeed - lastTrainSpeed;
        context.data.putDouble("TrainSpeed", trainSpeed);
        float throttle = Mth.lerp(0.05f, context.data.getFloatOr("Throttle", 0),
                (float) Math.max(0, Math.min(1, acceleration)));
        context.data.putFloat("Throttle", throttle);

        Pair<UUID, BlockPos> key = Pair.of(entity.getUUID(), context.localPos);
        EngineSoundInstance instance = TRAIN_ENGINES.get(key);
        if (context.disabled) {
            if (instance != null)
                instance.fadeOut();
            return;
        }
        if (instance == null) {
            instance = new EngineSoundInstance(CDGSoundEvents.ENGINE_NORMAL.get(), SoundSource.NEUTRAL, context.position, 0.6f);
            instance.setVolume(1f);
            Minecraft.getInstance().getSoundManager().play(instance);
            TRAIN_ENGINES.put(key, instance);
        } else if (instance.isStopped())
            TRAIN_ENGINES.remove(key);

        instance.setPosition(context.position);
        if (instance.active()) {
            instance.keepAlive();
            float pitch = (float) Math.min(2, Math.max(Math.min(0.14, throttle) * 5 + trainSpeed / 28, 0.1f));
            instance.setPitch(pitch);
        }
    }
}
