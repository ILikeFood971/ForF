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

package net.ilikefood971.forf.mixin.restrictions;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.ilikefood971.forf.util.Util;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.structure.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ServerWorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EndCityGenerator.Piece.class)
public abstract class EndCityGeneratorMixin extends SimpleStructurePiece {
    @SuppressWarnings("unused")
    public EndCityGeneratorMixin(StructurePieceType type, int length, StructureTemplateManager structureTemplateManager, Identifier id, String template, StructurePlacementData placementData, BlockPos pos) {
        super(type, length, structureTemplateManager, id, template, placementData, pos);
    }

    // Prevent the end city generator from actually putting any elytra item into the item frame.
    @WrapWithCondition(
            method = "handleMetadata",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/ServerWorldAccess;spawnEntity(Lnet/minecraft/entity/Entity;)Z")
    )
    protected boolean handleMetadataWithoutElytra(ServerWorldAccess instance, Entity entity) {
        // If it's not the item frame (shulker) then check if elytra can spawn in the end ship
        return !(entity instanceof ItemFrameEntity) || Util.CONFIG.restrictions().elytraInEndShip();
    }
}
