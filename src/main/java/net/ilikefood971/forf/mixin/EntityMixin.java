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
import net.ilikefood971.forf.util.Util;
import net.ilikefood971.forf.util.mixinInterfaces.IEntityDataSaver;
import net.ilikefood971.forf.util.mixinInterfaces.IGetPortalPos;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.ilikefood971.forf.util.Util.CONFIG;
import static net.ilikefood971.forf.util.Util.fakeScoreboard;

/**
 * Used for storing NBT data to the player as well as
 * adding some things required for the tracker to function
 */

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(Entity.class)
public abstract class EntityMixin implements IEntityDataSaver, IGetPortalPos, ServerEntityWorldChangeEvents.AfterPlayerChange {
    
    @Shadow public abstract String getEntityName();

    @Shadow public abstract String getUuidAsString();

    @Shadow protected BlockPos lastNetherPortalPosition;
    @Unique
    private NbtCompound persistentData;
    
    @Override
    public NbtCompound getPersistentData() {
        if (this.persistentData == null) {
            this.persistentData = new NbtCompound();
        }
        return persistentData;
    }
    
    @SuppressWarnings("rawtypes")
    @Inject(method = "writeNbt", at = @At("HEAD"))
    protected void injectWriteMethod(NbtCompound nbt, CallbackInfoReturnable info) {
        if (persistentData != null) {
            nbt.put("forf.data", persistentData);
        }
    }
    
    @Inject(method = "readNbt", at = @At("HEAD"))
    protected void injectReadMethod(NbtCompound nbt, CallbackInfo info) {
        if (nbt.contains("forf.data")) {
            persistentData = nbt.getCompound("forf.data");
        }
    }
    
    @Override
    public void removeLife() {
        int newLives = getLives();
        newLives -= 1;
        setLives(newLives);
    }
    
    @Override
    public int setLives(int lives) {
        if (!CONFIG.overfill()) {
            // Prevent the player from going over if overfill is disabled
            lives = Math.min(lives, CONFIG.startingLives());
        }
        // Prevent the player from having negative lives
        lives = Math.max(lives, 0);

        NbtCompound nbt = this.getPersistentData();
        int oldLives = nbt.getInt("lives");
        
        ScoreboardPlayerScore score = fakeScoreboard.getPlayerScore(this.getEntityName(), fakeScoreboard.livesObjective);
        score.setScore(lives);

        // Optimize a bit and check to see if they're the same
        if (oldLives == lives) {
            // Update the score because if you start forf and the player already has previous nbt, the score won't update
            fakeScoreboard.updateScore(score);
            return oldLives;
        }
        nbt.putInt("lives", lives);


        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        // Check to see if the player ran out of lives
        if (lives == 0 && Util.PERSISTENT_DATA.started) {
            // Kick the player if spectators are not allowed
            if (!Util.CONFIG.spectators()) {
                player.networkHandler.disconnect(Text.translatable("forf.disconnect.outOfLives"));
            } else {
                // If spectators allowed, switch the players gamemode
                player.changeGameMode(Util.CONFIG.spectatorGamemode());
                player.sendMessage(Text.translatable("forf.event.death.spectator"));
            }
        } else if (oldLives == 0) {
            player.changeGameMode(GameMode.DEFAULT);
        }
        return lives;
    }
    
    @Override
    public int getLives() {
        if (Util.PERSISTENT_DATA.forfPlayersUUIDs.contains(this.getUuidAsString())) {
            NbtCompound nbt = this.getPersistentData();
            return nbt.getInt("lives");
        } else return 0;
    }

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
