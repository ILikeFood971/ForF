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
import net.ilikefood971.forf.util.Util;
import net.minecraft.network.packet.s2c.play.OverlayMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.ilikefood971.forf.util.Util.*;

@SuppressWarnings("UnusedReturnValue")
public class PvPTimer implements ServerTickEvents.EndTick {
    public static int getSecondsLeft() {
        return secondsLeft;
    }
    
    public static PvPState getPvPState() {
        return pvPState;
    }
    
    private static int secondsLeft;
    private static PvPState pvPState;
    private byte ticksTillSecond = 0;
    public static void serverStarted() {
        secondsLeft = PERSISTENT_DATA.secondsLeft;
        pvPState = PERSISTENT_DATA.pvPState;
    }
    
    @Override
    public void onEndTick(MinecraftServer server) {
        if (!CONFIG.pvPTimer().enabled() || !PERSISTENT_DATA.started
                //#if MC >= 12003
                || !SERVER.getTickManager().shouldTick()
                //#endif
        ) return;
        if (ticksTillSecond != 0) {
            ticksTillSecond--;
            return;
        }
        Util.LOGGER.debug(secondsLeft + " seconds left with pvp " + pvPState);
        // Check to see if the timer has run out
        if (secondsLeft <= 0) {
            // Get the random seconds for the opposite of the current pvPState as it hasn't changed yet
            int seconds = getRandomSeconds(pvPState == PvPState.ON ? PvPState.OFF : PvPState.ON);
            // Using what the random seconds are, change the timer to a random amount of seconds
            changePvpTimer(seconds);
            // Now the pvPState has changed so plan accordingly
            Util.LOGGER.debug("PvP has been changed with " + secondsLeft + " seconds left and pvp " + pvPState);
        } else secondsLeft--;
        ticksTillSecond = 20;
        
        if (pvPState == PvPState.OFF) return;
        // All this will be skipped if pvp is off
        Text text = getEnabledActionbarText();
        for (ServerPlayerEntity serverPlayerEntity : server.getPlayerManager().getPlayerList()) {
            serverPlayerEntity.networkHandler.sendPacket(new OverlayMessageS2CPacket(text));
        }
        
    }
    public static boolean changePvpTimer(int seconds) {
        if (PvPTimer.pvPState == PvPState.OFF) {
            return changePvpTimer(PvPState.ON, seconds);
        }
        return changePvpTimer(PvPState.OFF, seconds);
    }
    public static boolean changePvpTimer(PvPState pvPState) {
        return changePvpTimer(pvPState, getRandomSeconds(pvPState));
    }
    
    public static boolean changePvpTimer(PvPState pvPState, int seconds) {
        secondsLeft = seconds;
        MinecraftServer server = Util.SERVER;
        if (pvPState == PvPState.OFF && PvPTimer.pvPState != pvPState) {
            for (ServerPlayerEntity serverPlayerEntity : server.getPlayerManager().getPlayerList()) {
                serverPlayerEntity.networkHandler.sendPacket(new OverlayMessageS2CPacket(Text.translatable("forf.timer.actionBar.disabled")));
                serverPlayerEntity.sendMessage(Text.translatable("forf.timer.chat.disabled"));
                server.setPvpEnabled(false);
            }
            PvPTimer.pvPState = PvPState.OFF;
            return true;
        }  else if (pvPState == PvPState.ON && PvPTimer.pvPState != pvPState) {
            for (ServerPlayerEntity serverPlayerEntity : server.getPlayerManager().getPlayerList()) {
                serverPlayerEntity.networkHandler.sendPacket(new TitleS2CPacket(Text.translatable("forf.timer.title.enabled")));
                serverPlayerEntity.sendMessage(Text.translatable("forf.timer.chat.enabled"));
                server.setPvpEnabled(true);
            }
            PvPTimer.pvPState = PvPState.ON;
            return true;
        }
        Util.LOGGER.debug("State wasn't changed");
        return false;
    }
    
    private static Text getEnabledActionbarText() {
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
        Util.LOGGER.debug("For pvp " + pvPState + " returning " + seconds + "s between " + minTime + "m  and " + maxTime + "m");
        return seconds;
    }
    
    public enum PvPState {
        ON(true),
        OFF(false);
        
        private final boolean enabled;
        
        PvPState(boolean enabled) {
            this.enabled = enabled;
        }
        
        public boolean getValue() {
            return enabled;
        }
        public static PvPState convertToBoolean(boolean b) {
            if (b) return ON;
            return OFF;
        }
    }
    
}
