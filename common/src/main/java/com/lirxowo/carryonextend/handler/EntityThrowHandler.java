package com.lirxowo.carryonextend.handler;

import com.lirxowo.carryonextend.trigger.TriggerRegistry;
import com.lirxowo.carryonextend.util.FallingBlockUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import tschipp.carryon.Constants;
import tschipp.carryon.common.carry.CarryOnData;
import tschipp.carryon.common.carry.CarryOnData.CarryType;
import tschipp.carryon.common.carry.CarryOnDataManager;
import tschipp.carryon.common.scripting.CarryOnScript.ScriptEffects;
import tschipp.carryon.networking.clientbound.ClientboundStartRidingPacket;
import tschipp.carryon.platform.Services;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class EntityThrowHandler {

    private static final float BASE_THROW_POWER = 0.8f;
    private static final float BASE_THROW_UPWARD = 0.2f;
    private static final float MAX_POWER_MULTIPLIER = 2.5f;

    private static final float PLAYER_THROW_POWER_BONUS = 1.2f;
    private static final float PLAYER_THROW_UPWARD_BONUS = 0.6f;

    public static void throwCarriedEntity(ServerPlayer player) {
        throwCarriedEntityWithPower(player, 1.0f);
    }

    public static void throwCarriedEntityWithPower(ServerPlayer player, float powerFactor) {
        CarryOnData carry = CarryOnDataManager.getCarryData(player);

        if (!carry.isCarrying(CarryType.ENTITY) && !carry.isCarrying(CarryType.PLAYER)) {
            return;
        }

        Level level = player.level();

        float powerMult = 1.0f + (powerFactor * (MAX_POWER_MULTIPLIER - 1.0f));

        float throwPower = BASE_THROW_POWER * powerMult;
        float throwUpward = BASE_THROW_UPWARD * powerMult;

        if (carry.isCarrying(CarryType.PLAYER)) {
            Entity passenger = player.getFirstPassenger();

            if (passenger != null) {
                Vec3 lookDir = player.getLookAngle();
                Vec3 playerPos = player.position().add(lookDir.multiply(2.0, 0, 2.0)).add(0, 1.5, 0);

                float playerThrowPower = throwPower + PLAYER_THROW_POWER_BONUS;
                float playerThrowUpward = throwUpward + PLAYER_THROW_UPWARD_BONUS;

                Vec3 velocity = new Vec3(
                        lookDir.x * playerThrowPower,
                        playerThrowUpward,
                        lookDir.z * playerThrowPower
                );

                if (level instanceof ServerLevel serverLevel) {
                    List<ServerPlayer> allPlayers = serverLevel.getServer().getPlayerList().getPlayers();
                    ClientboundStartRidingPacket stopRidingPacket = new ClientboundStartRidingPacket(passenger.getId(), false);
                    for (ServerPlayer serverPlayer : allPlayers) {
                        Services.PLATFORM.sendPacketToPlayer(Constants.PACKET_ID_START_RIDING, stopRidingPacket, serverPlayer);
                    }
                }

                passenger.stopRiding();
                player.ejectPassengers();

                passenger.teleportTo(playerPos.x, playerPos.y, playerPos.z);

                //TickTask 不知道为什么并不生效，故用此替代
                ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
                service.schedule(() -> {
                    if (passenger instanceof ServerPlayer thrownPlayer) {
                        thrownPlayer.setDeltaMovement(velocity);
                        thrownPlayer.hurtMarked = true;
                        thrownPlayer.setOnGround(false);
                        thrownPlayer.fallDistance = 0.0f;
                    } else {
                        passenger.setDeltaMovement(velocity);
                        passenger.hurtMarked = true;
                    }

                    float pitch = Math.max(0.5f, Math.min(1.8f, 0.8f + powerFactor * 0.8f));
                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.8F, pitch);
                    player.swing(InteractionHand.MAIN_HAND, true);
                }, 51, TimeUnit.MILLISECONDS);
                carry.clear();
                CarryOnDataManager.setCarryData(player, carry);
                return;
            }
        }

        CompoundTag entityNBT = carry.getContentNbt();
        if (entityNBT == null) {
            carry.clear();
            CarryOnDataManager.setCarryData(player, carry);
            return;
        }

        if (FallingBlockUtil.handleFallingBlockThrow(player, carry, entityNBT, throwPower, throwUpward, powerFactor)) {
            return;
        }

        String entityTypeString = "";
        if (entityNBT.contains("id")) {
            entityTypeString = entityNBT.getString("id");
        }

        UUID entityUUID = null;
        if (entityNBT.hasUUID("UUID")) {
            entityUUID = entityNBT.getUUID("UUID");
        }

        Entity entity = carry.getEntity(level);

        if (entity == null) {
            if (!entityTypeString.isEmpty() && level instanceof ServerLevel serverLevel) {
                try {
                    EntityType<?> entityType = EntityType.byString(entityTypeString).orElse(null);
                    if (entityType != null) {
                        entity = entityType.create(serverLevel);
                        if (entity != null && entityNBT != null) {
                            entity.load(entityNBT);
                            if (entityUUID != null) {
                                entity.setUUID(entityUUID);
                            }
                        }
                    }
                } catch (Exception e) {
                    carry.clear();
                    CarryOnDataManager.setCarryData(player, carry);
                    return;
                }
            }

            if (entity == null) {
                carry.clear();
                CarryOnDataManager.setCarryData(player, carry);
                return;
            }
        }

        Vec3 playerPos = player.position().add(0, 1.0, 0);
        entity.setPos(playerPos);

        if (carry.getActiveScript().isPresent()) {
            ScriptEffects effects = carry.getActiveScript().get().scriptEffects();
            String cmd = effects.commandPlace();
            if (!cmd.isEmpty())
                player.getServer().getCommands().performPrefixedCommand(player.getServer().createCommandSourceStack(),
                        "/execute as " + player.getGameProfile().getName() + " run " + cmd);
        }

        if (entityNBT != null && !entityNBT.isEmpty()) {
            if (entityNBT.contains("Pos")) {
                entityNBT.remove("Pos");
            }

            CompoundTag posTag = new CompoundTag();
            entity.saveWithoutId(posTag);
            entityNBT.merge(posTag);
            entity.load(entityNBT);
        }

        level.addFreshEntity(entity);

        Vec3 lookDir = player.getLookAngle();
        entity.setDeltaMovement(lookDir.x * throwPower, throwUpward, lookDir.z * throwPower);

        if (entity instanceof Mob mob) {
            mob.setNoAi(false);
        }

        if (entity instanceof PrimedTnt primedTnt) {
            primedTnt.addTag("thrownBy:" + player.getUUID().toString());
            TriggerRegistry.TNT_THROW.get().trigger(player);
        }

        float pitch = Math.max(0.5f, Math.min(1.8f, 0.8f + powerFactor * 0.8f));
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.8F, pitch);

        carry.clear();
        CarryOnDataManager.setCarryData(player, carry);
        player.swing(InteractionHand.MAIN_HAND, true);
    }
}
