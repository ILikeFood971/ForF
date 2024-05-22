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

package net.ilikefood971.forf.tracker;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

import java.util.UUID;

public class TrackerData {

    private static final String KEY = "trackerData";

    public static PlayerTrackerComponent getData(ItemStack stack) {
        return PlayerTrackerComponent.fromNbt(stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt());
    }

    public static void applyData(ItemStack stack, PlayerTrackerComponent data) {
        stack.apply(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT, comp -> comp.apply(currentNbt -> currentNbt.put(KEY, data.toNbt())));
    }

    public record PlayerTrackerComponent(UUID target, boolean tracking, long expiration) {
        private static final String UUID_KEY = "trackedPlayer";
        private static final String TRACKING_KEY = "tracking";
        private static final String EXPIRATION_KEY = "expiration";

        private static PlayerTrackerComponent fromNbt(NbtCompound compound) {
            return new PlayerTrackerComponent(compound.getUuid(UUID_KEY), compound.getBoolean(TRACKING_KEY), compound.getLong(EXPIRATION_KEY));
        }

        private NbtCompound toNbt() {
            NbtCompound compound = new NbtCompound();

            compound.putUuid(UUID_KEY, this.target);
            compound.putBoolean(TRACKING_KEY, this.tracking);
            compound.putLong(EXPIRATION_KEY, this.expiration);

            return compound;
        }
    }
}
