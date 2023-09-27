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

package net.ilikefood971.forf.event;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.ilikefood971.forf.Forf;
import net.ilikefood971.forf.util.mixinInterfaces.IEntityDataSaver;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

@SuppressWarnings("DataFlowIssue")
public class PlayerDeathEvent implements ServerLivingEntityEvents.AfterDeath {
    // Remove one life from the player on death
    @Override
    public void afterDeath(LivingEntity entity, DamageSource damageSource) {
        // Check that they are actually a player as this gets called for all entity deaths
        if (entity instanceof ServerPlayerEntity) {
            // Cast the entity to player for later use
            PlayerEntity player = (PlayerEntity) entity;
            IEntityDataSaver data = (IEntityDataSaver) player;
            
            if (data.getLives() > 0) {
                data.removeLife();
                // TODO Make real message
                player.getServer().getPlayerManager().broadcast(Text.translatable("forf.event.death.lifeRemoved", player.getEntityName(), data.getLives()), false);
                
            // Check to see if the player ran out of lives
            }
            if (data.getLives() <= 0) {
                // Kick the player if spectators are not allowed
                if (!Forf.CONFIG.spectators()) {
                    ((ServerPlayerEntity) player).networkHandler.disconnect(Text.translatable("forf.disconnect.outOfLives"));
                }
                // If spectators allowed, switch the players gamemode
                ((ServerPlayerEntity) player).changeGameMode(Forf.CONFIG.spectatorGamemode());
                player.sendMessage(Text.translatable("forf.event.death.spectator"));
            }
            
           
        }
    }
}
