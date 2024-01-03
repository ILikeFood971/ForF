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

package net.ilikefood971.forf.util;

import net.minecraft.nbt.NbtList;
import net.minecraft.network.packet.s2c.play.ScoreboardDisplayS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardScoreUpdateS2CPacket;
import net.minecraft.scoreboard.*;
import net.minecraft.text.Text;

public class FakeScoreboard extends Scoreboard {
    public final ScoreboardObjective livesObjective;

    public FakeScoreboard() {
        super();
        this.livesObjective = this.addObjective(
                "lives",
                ScoreboardCriterion.DUMMY,
                Text.of("lives"),
                Util.CONFIG.tablistLivesRenderType()
                //#if MC >= 12003
                , false,
                null
                //#endif
        );
    }

    public NbtList toNbt() {
        return super.toNbt();
    }

    public void readNbt(NbtList list) {
        super.readNbt(list);
    }

    public void setListSlot() {
        Util.SERVER.getPlayerManager().sendToAll(new ScoreboardDisplayS2CPacket(
                Util.getScoreboardListSlot(), Util.fakeScoreboard.livesObjective
        ));
    }

    public void clearListSlot() {
        Util.SERVER.getPlayerManager().sendToAll(new ScoreboardDisplayS2CPacket(
                Util.getScoreboardListSlot(), null
        ));
    }

    @Override
    public void updateScore(
            //#if MC >= 12003
            ScoreHolder scoreHolder,
            ScoreboardObjective objective,
            //#endif
            ScoreboardScore score
    ) {
        super.updateScore(
                //#if MC >= 12003
                scoreHolder,
                objective,
                //#endif
                score);
        Util.SERVER
                .getPlayerManager()
                .sendToAll(
                        new ScoreboardScoreUpdateS2CPacket(
                                //#if MC >= 12003
                                scoreHolder.getNameForScoreboard(),
                                objective.getName(),
                                score.getScore(),
                                objective.getDisplayName(),
                                null
                                //#else
                                //$$ServerScoreboard.UpdateMode.CHANGE,
                                //$$score.getObjective().getName(),
                                //$$score.getPlayerName(),
                                //$$score.getScore()
                                //#endif
                        )
                );
        Util.LOGGER.debug("Scoreboard Update Packet sent");
    }
}

