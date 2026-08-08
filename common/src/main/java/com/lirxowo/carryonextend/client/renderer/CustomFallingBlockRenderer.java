package com.lirxowo.carryonextend.client.renderer;

import com.lirxowo.carryonextend.client.renderer.state.CustomFallingBlockRenderState;
import com.lirxowo.carryonextend.registry.CustomFallingBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;

public class CustomFallingBlockRenderer extends EntityRenderer<CustomFallingBlockEntity, CustomFallingBlockRenderState> {

    public CustomFallingBlockRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.5F;
    }

    @Override
    public boolean shouldRender(CustomFallingBlockEntity entity, Frustum culler, double camX, double camY, double camZ) {
        if (!super.shouldRender(entity, culler, camX, camY, camZ)) {
            return false;
        }
        return entity.getBlockState() != entity.level().getBlockState(entity.blockPosition());
    }

    @Override
    public CustomFallingBlockRenderState createRenderState() {
        return new CustomFallingBlockRenderState();
    }

    @Override
    public void extractRenderState(CustomFallingBlockEntity entity, CustomFallingBlockRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);

        BlockState blockState = entity.getBlockState();
        BlockPos pos = BlockPos.containing(entity.getX(), entity.getBoundingBox().maxY, entity.getZ());
        Level level = entity.level();

        state.movingBlockRenderState.randomSeedPos = entity.getStartPos();
        state.movingBlockRenderState.blockPos = pos;
        state.movingBlockRenderState.blockState = blockState;
        if (level instanceof ClientLevel clientLevel) {
            state.movingBlockRenderState.biome = clientLevel.getBiome(pos);
            state.movingBlockRenderState.cardinalLighting = clientLevel.cardinalLighting();
            state.movingBlockRenderState.lightEngine = clientLevel.getLightEngine();
        }

        state.blockEntityRenderState = null;
        if (blockState.getBlock() instanceof EntityBlock entityBlock) {
            BlockEntity blockEntity = entityBlock.newBlockEntity(pos, blockState);
            if (blockEntity != null) {
                blockEntity.setLevel(level);
                CompoundTag blockData = entity.getBlockData();
                if (blockData != null && !blockData.isEmpty()) {
                    try {
                        blockEntity.loadCustomOnly(TagValueInput.create(
                                ProblemReporter.DISCARDING,
                                level.registryAccess(),
                                blockData
                        ));
                    } catch (Exception ignored) {
                    }
                }

                BlockEntityRenderDispatcher dispatcher = Minecraft.getInstance().getBlockEntityRenderDispatcher();
                state.blockEntityRenderState = dispatcher.tryExtractRenderState(blockEntity, partialTicks, null);
            }
        }
    }

    @Override
    public void submit(CustomFallingBlockRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        BlockState blockState = state.movingBlockRenderState.blockState;

        if (blockState.getRenderShape() == RenderShape.INVISIBLE) {
            return;
        }

        poseStack.pushPose();
        try {
            poseStack.translate(-0.5D, 0.0D, -0.5D);

            if (blockState.getRenderShape() == RenderShape.MODEL) {
                submitNodeCollector.submitMovingBlock(poseStack, state.movingBlockRenderState);
            }

            if (state.blockEntityRenderState != null) {
                BlockEntityRenderDispatcher dispatcher = Minecraft.getInstance().getBlockEntityRenderDispatcher();
                dispatcher.submit(state.blockEntityRenderState, poseStack, submitNodeCollector, camera);
            }
        } finally {
            poseStack.popPose();
        }

        super.submit(state, poseStack, submitNodeCollector, camera);
    }
}
