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
    @ModifyReturnValue(method = "checkCanJoin", at = @At("RETURN"))
    private Text checkCanJoinWithForf(Text original, @Local GameProfile profile) {
        if (original == null) {
            // When a player joins, make sure that they are allowed to join from the config
            // When checking if they're a player we can't use the Util method as we don't have a ServerPlayerEntity
            if (PERSISTENT_DATA.isStarted() && !PERSISTENT_DATA.getPlayersAndLives().containsKey(profile.getId()) && !CONFIG.spectators()) {
                // Unfortunately we can't make this translatable on the server side because it is too early in the process
                return Text.literal("You must be a Friend or Foe member to join because spectators aren't allowed!");
            }
        }
        return original;
    }
}
