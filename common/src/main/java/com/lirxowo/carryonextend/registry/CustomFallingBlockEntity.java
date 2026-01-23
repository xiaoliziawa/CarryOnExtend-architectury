package com.lirxowo.carryonextend.registry;

import com.lirxowo.carryonextend.util.FallingBlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class CustomFallingBlockEntity extends FallingBlockEntity {
    private static final EntityDataAccessor<String> BLOCK_ID = SynchedEntityData.defineId(CustomFallingBlockEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> BLOCK_STATE_META = SynchedEntityData.defineId(CustomFallingBlockEntity.class, EntityDataSerializers.INT);

    private BlockState blockState;
    private CompoundTag blockData;
    private int ticksExisted = 0;
    private boolean needsStateUpdate = false;

    public CustomFallingBlockEntity(EntityType<CustomFallingBlockEntity> type, Level level) {
        super(type, level);
    }

    public static CustomFallingBlockEntity throwBlock(Level level, double x, double y, double z, BlockState state, CompoundTag blockData, Vec3 motion) {
        if (state == null) {
            state = Blocks.STONE.defaultBlockState();
        }

        CustomFallingBlockEntity entity = new CustomFallingBlockEntity(
                EntityRegistry.CUSTOM_FALLING_BLOCK.get(), level);

        entity.setPos(x, y, z);
        entity.setBlockState(state);

        if (blockData != null) {
            entity.setBlockData(blockData);
        }

        entity.setDeltaMovement(motion);
        entity.time = 1;
        entity.dropItem = true;

        level.addFreshEntity(entity);

        return entity;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(BLOCK_ID, "minecraft:stone");
        builder.define(BLOCK_STATE_META, 0);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);

        if (key.equals(BLOCK_ID) || key.equals(BLOCK_STATE_META)) {
            this.needsStateUpdate = true;
        }
    }

    public void setBlockState(BlockState state) {
        if (state == null) {
            state = Blocks.STONE.defaultBlockState();
        }

        this.blockState = state;

        Identifier blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        if (blockId != null) {
            this.entityData.set(BLOCK_ID, blockId.toString());
        }

        int stateId = Block.getId(state);
        this.entityData.set(BLOCK_STATE_META, stateId);
    }

    private void updateBlockStateFromData() {
        if (this.needsStateUpdate || this.blockState == null || this.blockState.getBlock() == Blocks.STONE) {
            String blockIdStr = this.entityData.get(BLOCK_ID);
            int stateId = this.entityData.get(BLOCK_STATE_META);

            try {
                if (!blockIdStr.isEmpty() && stateId != 0) {
                    BlockState stateFromId = Block.stateById(stateId);
                    if (stateFromId != Blocks.AIR.defaultBlockState()) {
                        this.blockState = stateFromId;
                        this.needsStateUpdate = false;
                        return;
                    }

                    Identifier blockId = Identifier.parse(blockIdStr);
                    Block block = BuiltInRegistries.BLOCK.getValue(blockId);
                    if (block != null && block != Blocks.AIR) {
                        this.blockState = block.defaultBlockState();
                        this.needsStateUpdate = false;
                        return;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (this.blockState == null) {
                this.blockState = Blocks.STONE.defaultBlockState();
            }
            this.needsStateUpdate = false;
        }
    }

    public void setBlockData(CompoundTag blockData) {
        this.blockData = blockData;
    }

    public CompoundTag getBlockData() {
        return this.blockData;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);

        FallingBlockUtil.saveBlockDataToNBT(
            output,
            this.getBlockState(),
            this.getBlockData(),
            this.entityData.get(BLOCK_ID),
            this.entityData.get(BLOCK_STATE_META)
        );
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);

        // Read custom block data
        this.blockData = input.read("CustomBlockData", CompoundTag.CODEC).orElse(null);

        // Try to read block state from various sources
        if (input.child("BlockState").isPresent()) {
            try {
                ValueInput blockStateInput = input.childOrEmpty("BlockState");
                int stateId = blockStateInput.getIntOr("id", 0);
                if (stateId > 0) {
                    this.blockState = Block.stateById(stateId);
                    this.needsStateUpdate = false;

                    Identifier blockId = BuiltInRegistries.BLOCK.getKey(this.blockState.getBlock());
                    if (blockId != null) {
                        this.entityData.set(BLOCK_ID, blockId.toString());
                    }
                    this.entityData.set(BLOCK_STATE_META, stateId);
                }
            } catch (Exception e) {
            }
        }

        if ((this.blockState == null || this.blockState.getBlock() == Blocks.STONE) && input.getString("BlockId").isPresent()) {
            try {
                String blockIdStr = input.getStringOr("BlockId", "");
                Identifier blockId = Identifier.parse(blockIdStr);
                Block block = BuiltInRegistries.BLOCK.getValue(blockId);
                if (block != null && block != Blocks.AIR) {
                    this.blockState = block.defaultBlockState();
                    this.entityData.set(BLOCK_ID, blockIdStr);
                    this.needsStateUpdate = false;
                }
            } catch (Exception e) {
            }
        }

        if ((this.blockState == null || this.blockState.getBlock() == Blocks.STONE) && input.child("EntityData").isPresent()) {
            try {
                ValueInput entityDataInput = input.childOrEmpty("EntityData");
                if (entityDataInput.getString("BLOCK_ID").isPresent() && entityDataInput.getInt("BLOCK_STATE_META").isPresent()) {
                    String blockIdStr = entityDataInput.getStringOr("BLOCK_ID", "");
                    int stateId = entityDataInput.getIntOr("BLOCK_STATE_META", 0);

                    this.entityData.set(BLOCK_ID, blockIdStr);
                    this.entityData.set(BLOCK_STATE_META, stateId);

                    if (stateId > 0) {
                        this.blockState = Block.stateById(stateId);
                    } else {
                        Identifier blockId = Identifier.parse(blockIdStr);
                        Block block = BuiltInRegistries.BLOCK.getValue(blockId);
                        if (block != null && block != Blocks.AIR) {
                            this.blockState = block.defaultBlockState();
                        }
                    }

                    this.needsStateUpdate = false;
                }
            } catch (Exception e) {
            }
        }
    }

    @Override
    public BlockState getBlockState() {
        updateBlockStateFromData();

        if (this.blockState == null) {
            return Blocks.STONE.defaultBlockState();
        }
        return this.blockState;
    }

    @Override
    public void tick() {
        ticksExisted++;

        if (ticksExisted <= 5) {
            updateBlockStateFromData();
            if (this.blockState != null && this.blockState.getBlock() == Blocks.STONE) {
                this.needsStateUpdate = true;
            }
        }

        if (this.blockState == null) {
            updateBlockStateFromData();
            if (this.blockState == null) {
                this.discard();
                return;
            }
        }

        updateMovement();

        if (!this.level().isClientSide() && !this.isRemoved()) {
            BlockPos pos = this.blockPosition();
            boolean canPlace = this.level().getBlockState(pos).canBeReplaced();

            if (this.onGround()) {
                if (canPlace) {
                    placeBlock(pos);
                } else {
                    dropAsItem();
                }
            }
        }
    }

    private void updateMovement() {
        if (!this.isNoGravity()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.04D, 0.0D));
        }

        this.move(MoverType.SELF, this.getDeltaMovement());
        this.setDeltaMovement(this.getDeltaMovement().scale(0.98D));
    }

    private void placeBlock(BlockPos pos) {
        CompoundTag blockData = this.getBlockData();

        if (FallingBlockUtil.placeBlock(this.level(), pos, this.blockState, blockData)) {
            this.discard();
        }
    }

    private void dropAsItem() {
        Level level = this.level();
        if (level instanceof ServerLevel serverLevel) {
            if (serverLevel.getGameRules().get(GameRules.ENTITY_DROPS)) {
                ItemStack itemStack = createItemStackWithData();
                this.spawnAtLocation(serverLevel, itemStack);
            }
        }

        this.discard();
    }

    private ItemStack createItemStackWithData() {
        ItemStack itemStack = new ItemStack(this.blockState.getBlock());

        CompoundTag blockData = this.getBlockData();
        if (blockData != null && !blockData.isEmpty() &&
            this.blockState.getBlock() instanceof EntityBlock entityBlock) {

            BlockEntity tempEntity = entityBlock.newBlockEntity(this.blockPosition(), this.blockState);
            if (tempEntity != null) {
                FallingBlockUtil.loadBlockEntityData(tempEntity, blockData, this.level());
                FallingBlockUtil.saveBlockEntityToItem(tempEntity, itemStack, this.level());
            }
        }

        return itemStack;
    }
}
