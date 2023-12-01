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

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.ilikefood971.forf.util.Util;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.server.command.ScoreboardCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//#if MC >= 12002
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
//#endif

@Mixin(ScoreboardCommand.class)
public abstract class ScoreboardCommandMixin {
    @Unique
    private static final SimpleCommandExceptionType CANNOT_SET_LIST_SLOT_WITH_FORF = new SimpleCommandExceptionType(
            Text.translatable("forf.mixin.ScoreboardCommand.noList")
    );
    
    @Inject(method = "executeSetDisplay", at = @At("HEAD"))
    private static void setDisplayExceptToList(ServerCommandSource source,
                                               //#if MC >= 12002
                                               ScoreboardDisplaySlot
                                               //#else
                                               //$$ int
                                               //#endif
                                                       slot, ScoreboardObjective objective, CallbackInfoReturnable<Integer> cir) throws CommandSyntaxException {
        // If we don't have this mixin, you can have weird behavior where the packet gets sent, so it is in the list for a time, but then on a rejoin/restart it goes back to the livesObjective
        if (Util.PERSISTENT_DATA.started && slot
                //#if MC >= 12002
                .equals(ScoreboardDisplaySlot.LIST)
                //#else
                //$$ == 0
                //#endif
        ) {
            throw CANNOT_SET_LIST_SLOT_WITH_FORF.create();
        }
    }
}
