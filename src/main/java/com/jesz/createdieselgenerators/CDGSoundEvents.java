package com.jesz.createdieselgenerators;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

import java.util.function.Supplier;

public class CDGSoundEvents {
    public static Supplier<SoundEvent> ENGINE_NORMAL = registerSoundEvent("engine_normal");

    private static Supplier<SoundEvent> registerSoundEvent(String name) {
        Identifier id = CreateDieselGenerators.rl(name);
        SoundEvent event = Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
        return () -> event;
    }

    public static void register() {
    }
}
