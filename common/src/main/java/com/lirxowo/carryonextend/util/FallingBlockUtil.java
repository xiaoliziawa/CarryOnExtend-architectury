package com.lirxowo.carryonextend.util;

import com.lirxowo.carryonextend.registry.CustomFallingBlockEntity;
import com.lirxowo.carryonextend.registry.EntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import tschipp.carryon.common.carry.CarryOnData;
import tschipp.carryon.common.carry.CarryOnDataManager;


public class FallingBlockUtil {

    public static boolean isCustomFallingBlock(CompoundTag entityNBT) {
        if (entityNBT == null || !entityNBT.contains("id")) {
            return false;
        }

        String entityId = entityNBT.getString("id");
        ResourceLocation entityTypeId = ResourceLocation.parse(entityId);

        EntityType<?> customFallingBlockType = EntityRegistry.CUSTOM_FALLING_BLOCK.get();
        ResourceLocation customTypeId = BuiltInRegistries.ENTITY_TYPE.getKey(customFallingBlockType);

        return customTypeId != null && customTypeId.equals(entityTypeId);
    }

    public static BlockState getBlockStateFromNBT(CompoundTag entityNBT) {
        if (entityNBT == null) {
            return Blocks.STONE.defaultBlockState();
        }

        BlockState blockState = null;

        if (entityNBT.contains("BlockState")) {
            try {
                CompoundTag blockStateTag = entityNBT.getCompound("BlockState");
                int blockStateId = blockStateTag.getInt("id");
                if (blockStateId > 0) {
                    blockState = Block.stateById(blockStateId);
                    if (blockState != null && blockState.getBlock() != Blocks.AIR) {
                        return blockState;
                    }
                }
            } catch (Exception e) {
            }
        }

        if (entityNBT.contains("BlockId")) {
            try {
                String blockIdStr = entityNBT.getString("BlockId");
                ResourceLocation blockId = ResourceLocation.parse(blockIdStr);
                Block block = BuiltInRegistries.BLOCK.get(blockId);
                if (block != null && block != Blocks.AIR) {
                    return block.defaultBlockState();
                }
            } catch (Exception e) {
            }
        }

        CompoundTag blockData = null;
        if (entityNBT.contains("CustomBlockData")) {
            blockData = entityNBT.getCompound("CustomBlockData");
        }

        if (entityNBT.contains("EntityData")) {
            try {
                CompoundTag entityData = entityNBT.getCompound("EntityData");
                if (entityData.contains("BLOCK_ID")) {
                    String blockId = entityData.getString("BLOCK_ID");
                    Block block = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(blockId));
                    if (block != null && block != Blocks.AIR) {
                        blockState = block.defaultBlockState();
                    }
                }

                if (blockState == null && entityData.contains("BLOCK_STATE_META")) {
                    int stateId = entityData.getInt("BLOCK_STATE_META");
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
            }
        }

        return Blocks.STONE.defaultBlockState();
    }

    public static CompoundTag getBlockData(CompoundTag entityNBT) {
        if (entityNBT == null) {
            return null;
        }

        if (entityNBT.contains("CustomBlockData")) {
            return entityNBT.getCompound("CustomBlockData");
        }

        return null;
    }

    public static void saveBlockDataToNBT(CompoundTag tag, BlockState blockState,
                                          CompoundTag blockData, String blockIdValue,
                                          int blockStateMetaValue) {
        if (blockData != null) {
            tag.put("CustomBlockData", blockData);
        }

        tag.putString("CustomEntityType", "falling_block");

        if (blockState != null) {
            int stateId = Block.getId(blockState);
            CompoundTag blockStateTag = new CompoundTag();
            blockStateTag.putInt("id", stateId);
            tag.put("BlockState", blockStateTag);

            ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(blockState.getBlock());
            if (blockId != null) {
                tag.putString("BlockId", blockId.toString());
            }
        }

        CompoundTag entityData = new CompoundTag();
        entityData.putString("BLOCK_ID", blockIdValue);
        entityData.putInt("BLOCK_STATE_META", blockStateMetaValue);
        tag.put("EntityData", entityData);
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

                    blockEntity.loadWithComponents(blockData, level.registryAccess());
                    level.sendBlockUpdated(pos, blockState, blockState, 3);
                    blockEntity.setChanged();
                }
            }
            return true;
        }
        return false;
    }

    public static ItemStack createItemStackWithData(BlockState blockState, CompoundTag blockData, BlockPos pos) {
        ItemStack itemStack = new ItemStack(blockState.getBlock());

        if (blockData == null || blockData.isEmpty() ||
            !(blockState.getBlock() instanceof EntityBlock entityBlock)) {
            return itemStack;
        }

        BlockEntity tempEntity = entityBlock.newBlockEntity(pos, blockState);
        if (tempEntity != null) {
            try {
                tempEntity.loadWithComponents(blockData, tempEntity.getLevel() != null ?
                    tempEntity.getLevel().registryAccess() : null);
                tempEntity.saveToItem(itemStack, tempEntity.getLevel() != null ?
                    tempEntity.getLevel().registryAccess() : null);
            } catch (Exception e) {
            }
        }

        return itemStack;
    }
}
