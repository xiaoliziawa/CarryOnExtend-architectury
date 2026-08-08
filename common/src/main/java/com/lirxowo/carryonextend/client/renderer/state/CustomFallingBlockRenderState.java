package com.lirxowo.carryonextend.client.renderer.state;

import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.jspecify.annotations.Nullable;

public class CustomFallingBlockRenderState extends EntityRenderState {
    public MovingBlockRenderState movingBlockRenderState = new MovingBlockRenderState();
    public @Nullable BlockEntityRenderState blockEntityRenderState;
}
