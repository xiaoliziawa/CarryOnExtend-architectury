package com.lirxowo.carryonextend.client.renderer.state;

import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jspecify.annotations.Nullable;

public class CustomFallingBlockRenderState extends EntityRenderState {
    public MovingBlockRenderState movingBlockRenderState = new MovingBlockRenderState();

    // For block entity rendering
    public boolean hasBlockEntity = false;
    public @Nullable BlockEntityType<?> blockEntityType;
    public @Nullable CompoundTag blockData;
    public @Nullable BlockEntityRenderState blockEntityRenderState;
}
