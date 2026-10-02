package com.jesz.createdieselgenerators.client.sound;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

/** A looping block sound at a fixed position (pumpjack hole, crank, distillation tower). */
public class LoopingSoundInstance extends AbstractTickableSoundInstance {
    public LoopingSoundInstance(SoundEvent event, float volume, float pitch, Vec3 pos) {
        super(event, SoundSource.BLOCKS, RandomSource.create());
        this.x = pos.x;
        this.y = pos.y;
        this.z = pos.z;
        this.volume = volume;
        this.pitch = pitch;
        this.looping = true;
        this.delay = 0;
        this.attenuation = Attenuation.LINEAR;
    }

    @Override
    public void tick() {}
}
