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

import net.ilikefood971.forf.util.Util;
import net.ilikefood971.forf.util.mixinInterfaces.IEntityDataSaver;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.ilikefood971.forf.util.Util.livesObjective;


@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(Entity.class)
public abstract class PlayerEntityDataSaver implements IEntityDataSaver {
    
    @Shadow public abstract String getEntityName();
    
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
    public void setLives(int lives) {
        NbtCompound nbt = this.getPersistentData();
        nbt.putInt("lives", lives);
        
        Util.SERVER.getScoreboard().getPlayerScore(this.getEntityName(), livesObjective).setScore(lives);
    }
    
    @Override
    public int getLives() {
        NbtCompound nbt = this.getPersistentData();
        return nbt.getInt("lives");
    }
    
}
