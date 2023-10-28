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

import com.mojang.authlib.GameProfile;
import net.minecraft.server.PlayerManager;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.SocketAddress;

import static net.ilikefood971.forf.util.Util.CONFIG;
import static net.ilikefood971.forf.util.Util.PERSISTENT_DATA;

@Mixin(PlayerManager.class)
public abstract class PlayerLoginMixin {
    @Inject(method = "checkCanJoin", at = @At("HEAD"), cancellable = true)
    public void checkCanJoin(SocketAddress address, GameProfile profile, CallbackInfoReturnable<Text> cir) {
        // When a player joins, make sure that they are allowed to join from the config
        if (PERSISTENT_DATA.started && !PERSISTENT_DATA.forfPlayersUUIDs.contains(profile.getId().toString()) && !CONFIG.spectators()) {
            cir.setReturnValue(Text.translatable("forf.disconnect.noSpectators"));
        }
        // Returning nothing allows the other method checks to run
    }
}
