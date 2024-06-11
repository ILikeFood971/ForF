/*
 * This file is part of the Friend or Foe project, licensed under the
 * GNU General Public License v3.0
 *
 * Copyright (C) 2024  ILikeFood971 and contributors
 *
 * Friend or Foe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * Friend or Foe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Friend or Foe.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.ilikefood971.forf.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import net.ilikefood971.forf.tracker.PlayerTrackerItem;
import net.ilikefood971.forf.util.Util;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTracker;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.stream.Collectors;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements PlayerTrackerItem.WorldLocationHistory {

    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @WrapOperation(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"))
    private boolean damage(ServerPlayerEntity instance, DamageSource source, float amount, Operation<Boolean> original) {
        Entity sourceEntity = source.getAttacker();
        if (source.getTypeRegistryEntry().matchesKey(DamageTypes.PLAYER_EXPLOSION) && sourceEntity != null && !sourceEntity.equals(instance)) {
            amount -= amount * Util.CONFIG.restrictions().explosionNerf() / 100f;
        }
        return original.call(instance, source, amount);
    }

    @WrapOperation(method = "onDeath", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/damage/DamageTracker;getDeathMessage()Lnet/minecraft/text/Text;"))
    private Text changeDeathMessage(DamageTracker instance, Operation<Text> original) {
        Text originalMessage = original.call(instance);
        return Util.CONFIG.redDeathMessage() ? originalMessage.copy().formatted(Formatting.RED) : originalMessage;
    }

    @Inject(method = "teleportTo", at = @At(value = "FIELD", target = "Lnet/minecraft/server/network/ServerPlayerEntity;inTeleportationState:Z"))
    private void trackDimensionChange(TeleportTarget teleportTarget, CallbackInfoReturnable<Entity> cir) {
        this.locationHistory.put(this.getWorld().getRegistryKey(), this.getBlockPos());
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("HEAD"))
    private void saveLocationHistory(NbtCompound nbt, CallbackInfo ci) {
        List<GlobalPos> list = this.locationHistory.entrySet().stream()
                .map(entry -> GlobalPos.create(entry.getKey(), entry.getValue()))
                .toList();

        NbtElement encoded = CODEC.encodeStart(NbtOps.INSTANCE, list)
                .resultOrPartial(Util.LOGGER::error)
                .orElseThrow();

        nbt.put("locationHistory", encoded);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("HEAD"))
    private void loadLocationHistory(NbtCompound nbt, CallbackInfo ci) {
        NbtCompound nbtCompound = nbt.getCompound("locationHistory");

        Pair<List<GlobalPos>, NbtElement> locationHistory = CODEC.decode(NbtOps.INSTANCE, nbtCompound)
                .result()
                .orElse(null);
        if (locationHistory == null) return;

        this.locationHistory.putAll(locationHistory.getFirst().stream()
                .collect(Collectors.toMap(GlobalPos::dimension, GlobalPos::pos)));
    }
}
