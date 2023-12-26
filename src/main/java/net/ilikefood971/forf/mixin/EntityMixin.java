/*
 * This file is part of the Friend or Foe project, licensed under the
 * GNU General Public License v3.0
 *
 * Copyright (C) 2023  ILikeFood971 and contributors
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

import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.ilikefood971.forf.tracker.PlayerTrackerItem;
import net.ilikefood971.forf.util.mixinInterfaces.IGetPortalPos;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Used for storing NBT data to the player as well as
 * adding some things required for the tracker to function
 */

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(Entity.class)
public abstract class EntityMixin implements IGetPortalPos, ServerEntityWorldChangeEvents.AfterPlayerChange {
    
    @Shadow protected BlockPos lastNetherPortalPosition;
    
    // For the player tracker
    @Override
    public BlockPos getLastNetherPortalLocation() {
        return this.lastNetherPortalPosition;
    }
    
    @Override
    public void afterChangeWorld(ServerPlayerEntity player, ServerWorld origin, ServerWorld destination) {
        if (player.getMainHandStack().isOf(PlayerTrackerItem.PLAYER_TRACKER)) PlayerTrackerItem.updateTracker(player.getMainHandStack(), destination);
        if (player.getOffHandStack().isOf(PlayerTrackerItem.PLAYER_TRACKER)) PlayerTrackerItem.updateTracker(player.getOffHandStack(), destination);
    }
}
