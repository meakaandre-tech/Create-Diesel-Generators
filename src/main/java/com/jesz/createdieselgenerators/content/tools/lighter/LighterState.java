package com.jesz.createdieselgenerators.content.tools.lighter;

import com.mojang.serialization.Codec;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;

public enum LighterState implements StringRepresentable {
    CLOSED,
    OPEN,
    OPEN_IGNITED;


    public static final Codec<LighterState> CODEC = StringRepresentable.fromEnum(LighterState::values);
    public static final StreamCodec<ByteBuf, LighterState> STREAM_CODEC = CatnipStreamCodecBuilders.ofEnum(LighterState.class);


    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
