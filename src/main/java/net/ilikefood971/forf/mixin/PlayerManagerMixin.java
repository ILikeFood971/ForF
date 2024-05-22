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
import net.minecraft.network.packet.s2c.play.ScoreboardDisplayS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardObjectiveUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardScoreUpdateS2CPacket;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {
    @Inject(method = "sendScoreboard", at = @At("RETURN"))
    protected void sendScoreboardWithLivesList(ServerScoreboard scoreboard, ServerPlayerEntity player, CallbackInfo ci) {
        player.networkHandler.sendPacket(new ScoreboardObjectiveUpdateS2CPacket(Util.FAKE_SCOREBOARD.livesObjective, 0));
        for (ScoreboardEntry entry : Util.FAKE_SCOREBOARD.getScoreboardEntries(Util.FAKE_SCOREBOARD.livesObjective)) {
            player.networkHandler.sendPacket(new ScoreboardScoreUpdateS2CPacket(
                    entry.owner(),
                    Util.FAKE_SCOREBOARD.livesObjective.getName(),
                    entry.value(),
                    Optional.empty(), Optional.empty()
            ));
        }
        if (Util.PERSISTENT_DATA.isStarted()) {
            player.networkHandler.sendPacket(new ScoreboardDisplayS2CPacket(ScoreboardDisplaySlot.LIST, Util.FAKE_SCOREBOARD.livesObjective));
        }
    }
}
