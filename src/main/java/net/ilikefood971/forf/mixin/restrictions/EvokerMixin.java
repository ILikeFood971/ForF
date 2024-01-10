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

import net.ilikefood971.forf.util.Util;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.EvokerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EvokerEntity.class)
public abstract class EvokerMixin extends EntityMixin {

    @Override
    protected void dropStack(ItemStack stack, float yOffset, CallbackInfoReturnable<ItemEntity> cir) {
        if (stack.isOf(Items.TOTEM_OF_UNDYING) && !Util.CONFIG.restrictions().totemDrops()) {
            Util.LOGGER.debug("Totem drop cancelled");
            cir.setReturnValue(null);
        }
    }
}

@Mixin(Entity.class)
abstract class EntityMixin { // Used for more compatible Mixin inheritance
    @SuppressWarnings("CancellableInjectionUsage")
    @Inject(method = "dropStack(Lnet/minecraft/item/ItemStack;F)Lnet/minecraft/entity/ItemEntity;", at = @At("HEAD"), cancellable = true)
    protected void dropStack(ItemStack stack, float yOffset, CallbackInfoReturnable<ItemEntity> cir) {
    }
}