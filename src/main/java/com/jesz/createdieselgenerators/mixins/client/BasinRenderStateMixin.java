package com.jesz.createdieselgenerators.mixins.client;

import com.jesz.createdieselgenerators.client.render.MoldBasinRender;
import com.zurrtum.create.client.content.processing.basin.BasinRenderer;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;

@Mixin(BasinRenderer.BasinRenderState.class)
public class BasinRenderStateMixin implements MoldBasinRender.Holder {
    @Unique
    private @Nullable List<MoldBasinRender.Entry> cdg$moldItems;

    @Override
    public @Nullable List<MoldBasinRender.Entry> cdg$getMoldItems() {
        return cdg$moldItems;
    }

    @Override
    public void cdg$setMoldItems(@Nullable List<MoldBasinRender.Entry> items) {
        cdg$moldItems = items;
    }
}
