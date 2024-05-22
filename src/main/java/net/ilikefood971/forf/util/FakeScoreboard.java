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

import net.minecraft.network.packet.s2c.play.ScoreboardDisplayS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardScoreUpdateS2CPacket;
import net.minecraft.scoreboard.*;
import net.minecraft.text.Text;

import java.util.Optional;

public class FakeScoreboard extends Scoreboard {
    public final ScoreboardObjective livesObjective;

    public FakeScoreboard() {
        super();
        this.livesObjective = this.addObjective(
                "lives",
                ScoreboardCriterion.DUMMY,
                Text.literal("lives"),
                Util.CONFIG.tablistLivesRenderType(),
                false,
                null
        );
    }

    public void setListSlot() {
        Util.SERVER.getPlayerManager().sendToAll(new ScoreboardDisplayS2CPacket(ScoreboardDisplaySlot.LIST, this.livesObjective));
    }

    public void clearListSlot() {
        Util.SERVER.getPlayerManager().sendToAll(new ScoreboardDisplayS2CPacket(ScoreboardDisplaySlot.LIST, null));
    }

    @Override
    public void updateScore(ScoreHolder scoreHolder, ScoreboardObjective objective, ScoreboardScore score) {
        super.updateScore(scoreHolder, objective, score);
        Util.SERVER.getPlayerManager().sendToAll(new ScoreboardScoreUpdateS2CPacket(
                scoreHolder.getNameForScoreboard(),
                objective.getName(),
                score.getScore(),
                Optional.empty(),
                Optional.empty()
        ));
        Util.LOGGER.debug("Scoreboard Update Packet sent");
    }
}

