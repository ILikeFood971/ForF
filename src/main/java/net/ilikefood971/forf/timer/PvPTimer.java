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

package net.ilikefood971.forf.timer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.ilikefood971.forf.assassin.AssassinHandler;
import net.ilikefood971.forf.data.DataHandler;
import net.ilikefood971.forf.util.Util;
import net.minecraft.network.packet.s2c.play.OverlayMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.ilikefood971.forf.util.Util.CONFIG;
import static net.ilikefood971.forf.util.Util.SERVER;

public class PvPTimer implements ServerTickEvents.EndTick {
    private static int secondsLeft;
    private static PvPState pvPState;

    public static int getSecondsLeft() {
        return secondsLeft;
    }

    public static PvPState getPvPState() {
        return pvPState;
    }

    public static void serverStarted() {
        secondsLeft = DataHandler.getInstance().getSecondsLeft();
        pvPState = DataHandler.getInstance().getPvPState();
        SERVER.setPvpEnabled(pvPState.getValue());
    }

    public static void changePvpTimer(PvPState pvPState) {
        changePvpTimer(pvPState, getRandomSeconds(pvPState));
    }

    public static void changePvpTimer(PvPState pvPState, int seconds) {
        secondsLeft = seconds;

        if (PvPTimer.pvPState != pvPState) {
            SERVER.setPvpEnabled(pvPState.getValue());
            PvPTimer.pvPState = pvPState;
            switch (pvPState) {
                case OFF -> {
                    for (ServerPlayerEntity serverPlayerEntity : SERVER.getPlayerManager().getPlayerList()) {
                        serverPlayerEntity.networkHandler.sendPacket(new OverlayMessageS2CPacket(Text.translatable("forf.timer.actionBar.disabled")));
                        serverPlayerEntity.sendMessage(Text.translatable("forf.timer.chat.disabled"));
                    }
                }
                case ON -> {
                    for (ServerPlayerEntity serverPlayerEntity : SERVER.getPlayerManager().getPlayerList()) {
                        serverPlayerEntity.networkHandler.sendPacket(new TitleS2CPacket(Text.translatable("forf.timer.title.enabled")));
                        serverPlayerEntity.sendMessage(Text.translatable("forf.timer.chat.enabled"));
                    }
                }
            }
        }

        Util.LOGGER.debug("PvP state wasn't changed");
    }

    private static Text getEnabledActionbarText() {
        if (PvPTimer.pvPState == PvPState.ASSASSIN) {
            Text assassinName = Text.literal(Util.getProfile(AssassinHandler.getInstance().getAssassin()).getName()).formatted(Formatting.BOLD, Formatting.DARK_RED);
            return Text.translatable("forf.timer.actionBar.assassin", assassinName);
        }

        int min;
        int sec;
        min = Math.floorDiv(secondsLeft, 60);
        sec = secondsLeft % 60;
        Text message;

        Text minText = Text.literal(String.valueOf(min)).formatted(Formatting.BOLD, Formatting.DARK_RED); // If you don't do it this way, the args aren't formatted
        Text secText = Text.literal(String.valueOf(sec)).formatted(Formatting.BOLD, Formatting.DARK_RED);

        if (min > 0) {
            message = Text.translatable("forf.timer.actionBar.enabledMoreThanMinute", minText, secText);
        } else message = Text.translatable("forf.timer.actionBar.enabled", secText);

        return message;
    }

    private static int getRandomSeconds(PvPState pvPState) {
        int minTime;
        int maxTime;
        if (pvPState == PvPState.ON) {
            minTime = CONFIG.pvPTimer().minRandomOnTime();
            maxTime = CONFIG.pvPTimer().maxRandomOnTime();
        } else {
            minTime = CONFIG.pvPTimer().minRandomOffTime();
            maxTime = CONFIG.pvPTimer().maxRandomOffTime();
        }
        int seconds = Util.SERVER.getOverworld().random.nextBetween(minTime * 60, maxTime * 60);
        Util.LOGGER.debug("For pvp {} returning {}s between {}m  and {}m", pvPState, seconds, minTime, maxTime);
        return seconds;
    }

    @Override
    public void onEndTick(MinecraftServer server) {
        if (!CONFIG.pvPTimer().enabled() || !DataHandler.getInstance().isStarted() || !SERVER.getTickManager().shouldTick() || server.getTicks() % 20 != 0)
            return;

        Util.LOGGER.trace("{} seconds left with pvp {}", secondsLeft, pvPState);
        // Check to see if the timer has run out
        if (secondsLeft <= 0 && pvPState != PvPState.ASSASSIN) {
            // Change the timer
            changePvpTimer(pvPState == PvPState.ON ? PvPState.OFF : PvPState.ON);
            // Now the pvPState has changed so plan accordingly
            Util.LOGGER.debug("PvP has been changed with {} seconds left and pvp {}", secondsLeft, pvPState);
        } else secondsLeft--;

        if (pvPState == PvPState.ASSASSIN && !AssassinHandler.getInstance().isAssassinOnline()) {
            changePvpTimer(PvPState.OFF);
        }
        if (pvPState == PvPState.OFF) return;

        // All this will be skipped if pvp is off
        Text text = getEnabledActionbarText();
        for (ServerPlayerEntity serverPlayerEntity : SERVER.getPlayerManager().getPlayerList()) {
            serverPlayerEntity.networkHandler.sendPacket(new OverlayMessageS2CPacket(text));
        }

    }

    public enum PvPState {
        ON(true),
        OFF(false),
        ASSASSIN(true);

        private final boolean enabled;

        PvPState(boolean enabled) {
            this.enabled = enabled;
        }

        public static PvPState convertFromBool(boolean b) {
            if (b) return ON;
            return OFF;
        }

        public boolean getValue() {
            return enabled;
        }
    }

}
