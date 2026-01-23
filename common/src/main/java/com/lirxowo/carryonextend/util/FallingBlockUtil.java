package com.lirxowo.carryonextend.util;

import com.lirxowo.carryonextend.registry.CustomFallingBlockEntity;
import com.lirxowo.carryonextend.registry.EntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import tschipp.carryon.Constants;
import tschipp.carryon.common.carry.CarryOnData;
import tschipp.carryon.common.carry.CarryOnDataManager;


public class FallingBlockUtil {

    private static final ProblemReporter problemReporter = new ProblemReporter.ScopedCollector(Constants.LOG);

    public static boolean isCustomFallingBlock(CompoundTag entityNBT) {
        if (entityNBT == null || !entityNBT.contains("id")) {
            return false;
        }

        String entityId = entityNBT.getStringOr("id", "");
        if (entityId.isEmpty()) {
            return false;
        }

        Identifier entityTypeId = Identifier.parse(entityId);

        EntityType<?> customFallingBlockType = EntityRegistry.CUSTOM_FALLING_BLOCK.get();
        Identifier customTypeId = BuiltInRegistries.ENTITY_TYPE.getKey(customFallingBlockType);

        return customTypeId != null && customTypeId.equals(entityTypeId);
    }

    public static BlockState getBlockStateFromNBT(CompoundTag entityNBT) {
        if (entityNBT == null) {
            return Blocks.STONE.defaultBlockState();
        }

        BlockState blockState = null;

        if (entityNBT.contains("BlockState")) {
            try {
                CompoundTag blockStateTag = entityNBT.getCompoundOrEmpty("BlockState");
                int blockStateId = blockStateTag.getIntOr("id", 0);
                if (blockStateId > 0) {
                    blockState = Block.stateById(blockStateId);
                    if (blockState != null && blockState.getBlock() != Blocks.AIR) {
                        return blockState;
                    }
                }
            } catch (Exception e) {
                // Ignore
            }
        }

        if (entityNBT.contains("BlockId")) {
            try {
                String blockIdStr = entityNBT.getStringOr("BlockId", "");
                if (!blockIdStr.isEmpty()) {
                    Identifier blockId = Identifier.parse(blockIdStr);
                    Block block = BuiltInRegistries.BLOCK.getValue(blockId);
                    if (block != null && block != Blocks.AIR) {
                        return block.defaultBlockState();
                    }
                }
            } catch (Exception e) {
                // Ignore
            }
        }

        if (entityNBT.contains("EntityData")) {
            try {
                CompoundTag entityData = entityNBT.getCompoundOrEmpty("EntityData");
                if (entityData.contains("BLOCK_ID")) {
                    String blockId = entityData.getStringOr("BLOCK_ID", "");
                    if (!blockId.isEmpty()) {
                        Block block = BuiltInRegistries.BLOCK.getValue(Identifier.parse(blockId));
                        if (block != null && block != Blocks.AIR) {
                            blockState = block.defaultBlockState();
                        }
                    }
                }

                if (blockState == null && entityData.contains("BLOCK_STATE_META")) {
                    int stateId = entityData.getIntOr("BLOCK_STATE_META", 0);
                    if (stateId != 0) {
                        blockState = Block.stateById(stateId);
                        if (blockState != null && blockState.getBlock() != Blocks.AIR) {
                            return blockState;
                        }
                    }
                }

                if (blockState != null) {
                    return blockState;
                }
            } catch (Exception e) {
                // Ignore
            }
        }

        return Blocks.STONE.defaultBlockState();
    }

    public static CompoundTag getBlockData(CompoundTag entityNBT) {
        if (entityNBT == null) {
            return null;
        }

        if (entityNBT.contains("CustomBlockData")) {
            return entityNBT.getCompoundOrEmpty("CustomBlockData");
        }

        return null;
    }

    public static void saveBlockDataToNBT(ValueOutput output, BlockState blockState,
                                          CompoundTag blockData, String blockIdValue,
                                          int blockStateMetaValue) {
        if (blockData != null) {
            output.store("CustomBlockData", CompoundTag.CODEC, blockData);
        }

        output.putString("CustomEntityType", "falling_block");

        if (blockState != null) {
            int stateId = Block.getId(blockState);
            ValueOutput blockStateOutput = output.child("BlockState");
            blockStateOutput.putInt("id", stateId);

            Identifier blockId = BuiltInRegistries.BLOCK.getKey(blockState.getBlock());
            if (blockId != null) {
                output.putString("BlockId", blockId.toString());
            }
        }

        ValueOutput entityDataOutput = output.child("EntityData");
        entityDataOutput.putString("BLOCK_ID", blockIdValue);
        entityDataOutput.putInt("BLOCK_STATE_META", blockStateMetaValue);
    }

    public static boolean handleFallingBlockThrow(ServerPlayer player, CarryOnData carry,
                                                 CompoundTag entityNBT, float throwPower,
                                                 float throwUpward, float powerFactor) {
        Level level = player.level();

        if (!isCustomFallingBlock(entityNBT)) {
            return false;
        }

        BlockState blockState = getBlockStateFromNBT(entityNBT);
        CompoundTag blockData = getBlockData(entityNBT);

        if (blockState == null || blockState.getBlock() == Blocks.AIR) {
            return false;
        }

        Vec3 playerPos = player.position().add(0, 1.5, 0);
        Vec3 lookDir = player.getLookAngle();
        Vec3 motion = new Vec3(lookDir.x * throwPower, throwUpward, lookDir.z * throwPower);

        CustomFallingBlockEntity fallingBlock = CustomFallingBlockEntity.throwBlock(
                level, playerPos.x, playerPos.y, playerPos.z, blockState, blockData, motion);

        float pitch = Math.max(0.5f, Math.min(1.8f, 0.8f + powerFactor * 0.8f));
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.8F, pitch);

        carry.clear();
        CarryOnDataManager.setCarryData(player, carry);
        if (!player.isCreative() || Constants.COMMON_CONFIG.settings.slownessInCreative)
            player.removeEffect(MobEffects.SLOWNESS);
        player.swing(InteractionHand.MAIN_HAND, true);

        return true;
    }

    public static boolean hasValidBlockEntity(Level level, BlockPos pos, CompoundTag blockData) {
        return level.getBlockEntity(pos) != null && blockData != null && !blockData.isEmpty();
    }

    public static boolean placeBlock(Level level, BlockPos pos, BlockState blockState, CompoundTag blockData) {
        if (level.setBlock(pos, blockState, 3)) {
            if (hasValidBlockEntity(level, pos, blockData)) {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity != null) {
                    loadBlockEntityData(blockEntity, blockData, level);
                    level.sendBlockUpdated(pos, blockState, blockState, 3);
                    blockEntity.setChanged();
                }
            }
            return true;
        }
        return false;
    }

    public static void loadBlockEntityData(BlockEntity blockEntity, CompoundTag blockData, Level level) {
        if (blockEntity == null || blockData == null || blockData.isEmpty()) {
            return;
        }

        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(blockEntity.problemPath(), Constants.LOG)) {
            ValueInput input = TagValueInput.create(reporter, level.registryAccess(), blockData);
            blockEntity.loadWithComponents(input);
        } catch (Exception e) {
            Constants.LOG.error("Failed to load block entity data", e);
        }
    }

    public static void saveBlockEntityToItem(BlockEntity blockEntity, ItemStack itemStack, Level level) {
        if (blockEntity == null || itemStack == null || level == null) {
            return;
        }

        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(blockEntity.problemPath(), Constants.LOG)) {
            TagValueOutput output = TagValueOutput.createWithContext(reporter, level.registryAccess());
            blockEntity.saveWithoutMetadata(output);
            CompoundTag nbt = output.buildResult();

            // Apply the NBT to the item using the component system
            if (!nbt.isEmpty()) {
                itemStack.applyComponents(blockEntity.collectComponents());
            }
        } catch (Exception e) {
            Constants.LOG.error("Failed to save block entity to item", e);
        }
    }

    public static ItemStack createItemStackWithData(BlockState blockState, CompoundTag blockData, BlockPos pos, Level level) {
        ItemStack itemStack = new ItemStack(blockState.getBlock());

        if (blockData == null || blockData.isEmpty() ||
            !(blockState.getBlock() instanceof EntityBlock entityBlock)) {
            return itemStack;
        }

        BlockEntity tempEntity = entityBlock.newBlockEntity(pos, blockState);
        if (tempEntity != null) {
            try {
                loadBlockEntityData(tempEntity, blockData, level);
                saveBlockEntityToItem(tempEntity, itemStack, level);
            } catch (Exception e) {
                Constants.LOG.error("Failed to create item stack with data", e);
            }
        }

        return itemStack;
    }
}
