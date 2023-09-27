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
import net.ilikefood971.forf.Forf;
import net.minecraft.network.packet.s2c.play.OverlayMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import static net.ilikefood971.forf.Forf.CONFIG;
import static net.ilikefood971.forf.Forf.PERSISTENT_DATA;

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
        if (!CONFIG.pvPTimer().enabled() || !PERSISTENT_DATA.started) return;
        if (ticksTillSecond != 0) {ticksTillSecond--; return;}
        Forf.LOGGER.debug(secondsLeft + " seconds left with pvp " + pvPState);
        // Check to see if the timer has run out
        if (secondsLeft <= 0) {
            // Get the random seconds for the opposite of the current pvPState as it hasn't changed yet
            int seconds = getRandomSeconds(pvPState == PvPState.ON ? PvPState.OFF : PvPState.ON);
            // Using what the random seconds are, change the timer to a random amount of seconds
            changePvpTimer(seconds);
            // Now the pvPState has changed so plan accordingly
            Forf.LOGGER.debug("PvP has been changed with " + secondsLeft + " seconds left and pvp " + pvPState);
        } else secondsLeft--;
        ticksTillSecond = 20;
        
        if (pvPState == PvPState.OFF) return;
        // All this will be skipped if pvp is off
        Text text = getParsedTextFromKey(getEnabledUnparsedString());
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
        MinecraftServer server = Forf.SERVER;
        if (pvPState == PvPState.OFF && PvPTimer.pvPState != pvPState) {
            for (ServerPlayerEntity serverPlayerEntity : server.getPlayerManager().getPlayerList()) {
                serverPlayerEntity.networkHandler.sendPacket(new OverlayMessageS2CPacket(getParsedTextFromKey("forf.timer.actionBar.disabled")));
                serverPlayerEntity.sendMessage(getParsedTextFromKey("forf.timer.chat.disabled"));
                server.setPvpEnabled(false);
            }
            PvPTimer.pvPState = PvPState.OFF;
            return true;
        }  else if (pvPState == PvPState.ON && PvPTimer.pvPState != pvPState) {
            for (ServerPlayerEntity serverPlayerEntity : server.getPlayerManager().getPlayerList()) {
                serverPlayerEntity.networkHandler.sendPacket(new TitleS2CPacket(getParsedTextFromKey("forf.timer.title.enabled")));
                serverPlayerEntity.sendMessage(getParsedTextFromKey("forf.timer.chat.enabled"));
                server.setPvpEnabled(true);
            }
            PvPTimer.pvPState = PvPState.ON;
            return true;
        }
        Forf.LOGGER.debug("State wasn't changed");
        return false;
    }
    
    private static Text getParsedTextFromKey(String string, Object... args) {
        if (args.length > 0) return Text.Serializer.fromJson(Text.translatable(string, args).getString());
        return Text.Serializer.fromJson(Text.translatable(string).getString());
    }
    @NotNull
    private static String getEnabledUnparsedString() {
        int min;
        int sec;
        min = Math.floorDiv(secondsLeft, 60);
        sec = secondsLeft % 60;
        Text message;
        
        if (min > 0) {
            message = getParsedTextFromKey("forf.actionBar.enabledMoreThanMinute", min, sec);
        } else message = getParsedTextFromKey("forf.actionBar.enabled", sec);
        return message.getString();
    }
    
    private static int getRandomSeconds(PvPState pvPState) {
        int minTime;
        int maxTime;
        if (pvPState == PvPState.ON) {
            minTime = CONFIG.pvPTimer().minRandomOnTime();
            maxTime = CONFIG.pvPTimer().maxRandomOnTime();
//            pvPTimer().pvPState = PvPState.ON;
        } else {
            minTime = CONFIG.pvPTimer().minRandomOffTime();
            maxTime = CONFIG.pvPTimer().maxRandomOffTime();
//            pvPTimer().pvPState = PvPState.OFF;
        }
        int seconds = Forf.SERVER.getWorld(World.OVERWORLD).random.nextBetween(minTime * 60, maxTime * 60);
        Forf.LOGGER.debug("For pvp " + pvPState + " returning " + seconds + "s between " + minTime + "m  and " + maxTime + "m");
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
