package com.jesz.createdieselgenerators.content.turret;

import com.jesz.createdieselgenerators.CreateDieselGenerators;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;

/**
 * The turret an entity is operating, saved with the entity. Fabric attachment replacing the
 * mixin-added "TurretPos" field of the NeoForge version.
 */
public final class TurretData {
    public static final AttachmentType<BlockPos> TURRET_POS = AttachmentRegistry.<BlockPos>builder()
            .persistent(BlockPos.CODEC)
            .buildAndRegister(CreateDieselGenerators.rl("turret_pos"));

    private TurretData() {
    }

    public static BlockPos getTurretPos(Entity entity) {
        return entity.getAttached(TURRET_POS);
    }

    public static void setTurretPos(Entity entity, BlockPos pos) {
        if (pos == null)
            entity.removeAttached(TURRET_POS);
        else
            entity.setAttached(TURRET_POS, pos);
    }

    public static void init() {
    }
}
