package com.lirxowo.carryonextend.client.renderer;

import com.lirxowo.carryonextend.client.renderer.state.CustomFallingBlockRenderState;
import com.lirxowo.carryonextend.registry.CustomFallingBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;

public class CustomFallingBlockRenderer extends EntityRenderer {

    public CustomFallingBlockRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.5F;
    }

    @Override
    public boolean shouldRender(Entity entity, Frustum culler, double camX, double camY, double camZ) {
        if (!super.shouldRender(entity, culler, camX, camY, camZ)) {
            return false;
        }
        if (entity instanceof CustomFallingBlockEntity customEntity) {
            return customEntity.getBlockState() != customEntity.level().getBlockState(customEntity.blockPosition());
        }
        return true;
    }

    @Override
    public CustomFallingBlockRenderState createRenderState() {
        return new CustomFallingBlockRenderState();
    }

    @Override
    public void extractRenderState(Entity entity, EntityRenderState renderState, float partialTicks) {
        super.extractRenderState(entity, renderState, partialTicks);

        if (!(entity instanceof CustomFallingBlockEntity customEntity) || !(renderState instanceof CustomFallingBlockRenderState state)) {
            return;
        }

        BlockState blockState = customEntity.getBlockState();
        BlockPos pos = BlockPos.containing(customEntity.getX(), customEntity.getBoundingBox().maxY, customEntity.getZ());
        Level level = customEntity.level();

        // Set up MovingBlockRenderState for basic block rendering
        state.movingBlockRenderState.randomSeedPos = customEntity.getStartPos();
        state.movingBlockRenderState.blockPos = pos;
        state.movingBlockRenderState.blockState = blockState;
        state.movingBlockRenderState.biome = level.getBiome(pos);
        state.movingBlockRenderState.level = level;

        // Check if block has a block entity
        state.hasBlockEntity = blockState.hasBlockEntity();
        if (state.hasBlockEntity) {
            state.blockEntityType = findBlockEntityType(blockState);
            state.blockData = customEntity.getBlockData();

            // Try to extract block entity render state
            if (state.blockEntityType != null) {
                BlockEntity blockEntity = createBlockEntityInstance(blockState, state.blockEntityType, pos);
                if (blockEntity != null) {
                    blockEntity.setLevel(level);
                    CompoundTag blockData = state.blockData;
                    if (blockData != null && !blockData.isEmpty()) {
                        try {
                            blockEntity.loadCustomOnly(TagValueInput.create(
                                    ProblemReporter.DISCARDING,
                                    level.registryAccess(),
                                    blockData
                            ));
                        } catch (Exception e) {
                            // Ignore loading errors
                        }
                    }

                    // Get the block entity renderer and extract its state
                    BlockEntityRenderDispatcher dispatcher = Minecraft.getInstance().getBlockEntityRenderDispatcher();
                    state.blockEntityRenderState = dispatcher.tryExtractRenderState(blockEntity, partialTicks, null);
                }
            }
        }
    }

    @Override
    public void submit(EntityRenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        if (!(renderState instanceof CustomFallingBlockRenderState state)) {
            return;
        }

        BlockState blockState = state.movingBlockRenderState.blockState;

        if (blockState.getRenderShape() == RenderShape.INVISIBLE) {
            return;
        }

        poseStack.pushPose();
        try {
            poseStack.translate(-0.5D, 0.0D, -0.5D);

            // Render the block model
            if (blockState.getRenderShape() == RenderShape.MODEL) {
                submitNodeCollector.submitMovingBlock(poseStack, state.movingBlockRenderState);
            }

            // Render block entity if present
            if (state.hasBlockEntity && state.blockEntityRenderState != null) {
                poseStack.pushPose();
                try {
                    poseStack.translate(0.5D, 0.5D, 0.5D);

                    BlockEntityRenderDispatcher dispatcher = Minecraft.getInstance().getBlockEntityRenderDispatcher();
                    dispatcher.submit(state.blockEntityRenderState, poseStack, submitNodeCollector, camera);
                } catch (Exception e) {
                    // Ignore rendering errors
                } finally {
                    poseStack.popPose();
                }
            }
        } finally {
            poseStack.popPose();
        }

        super.submit(renderState, poseStack, submitNodeCollector, camera);
    }

    private BlockEntityType<?> findBlockEntityType(BlockState blockState) {
        for (BlockEntityType<?> type : BuiltInRegistries.BLOCK_ENTITY_TYPE) {
            try {
                if (type.isValid(blockState)) {
                    return type;
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private BlockEntity createBlockEntityInstance(BlockState blockState, BlockEntityType<?> blockEntityType, BlockPos pos) {
        try {
            return blockEntityType.create(pos, blockState);
        } catch (Exception e) {
            return null;
        }
    }
}
