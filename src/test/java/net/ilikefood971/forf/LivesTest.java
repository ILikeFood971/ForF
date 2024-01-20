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

package net.ilikefood971.forf;

import net.fabricmc.fabric.api.entity.FakePlayer;
import net.ilikefood971.forf.util.Lives;
import net.ilikefood971.forf.util.Util;
import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LivesTest {
    private static final int INITIAL_LIVES = 0;
    private static final int MAX_LIVES = 10;
    private static final int NEGATIVE_LIVES = -1;
    private static final int EXCESS_LIVES = 1000;
    private static final int VALID_LIVES = 5;
    private final ServerPlayerEntity player = mock(FakePlayer.class);

    @BeforeAll
    public static void beforeAll() {
        SharedConstants.createGameVersion();
        Bootstrap.initialize();
    }

    @BeforeEach
    public void beforeEach() {
        Util.PERSISTENT_DATA = new PersistentData();
        Util.SERVER = mock(MinecraftServer.class);
        when(Util.SERVER.getPlayerManager()).thenReturn(mock(PlayerManager.class)); // Prevent exception being thrown by FakeScoreboard
    }

    @Test
    @DisplayName("Lives should initialize to zero")
    public void livesShouldInitializeToZero() {
        Lives lives = new Lives(player);
        assertEquals(INITIAL_LIVES, lives.get());
    }

    @Test
    @DisplayName("Lives should not exceed maximum limit")
    public void livesShouldNotExceedMaximumLimit() {
        Lives lives = new Lives(player);
        lives.set(EXCESS_LIVES);
        assertEquals(MAX_LIVES, lives.get());
    }

    @Test
    @DisplayName("Lives should not go below zero")
    public void livesShouldNotGoBelowZero() {
        Lives lives = new Lives(player);
        lives.set(NEGATIVE_LIVES);
        assertEquals(INITIAL_LIVES, lives.get());
    }

    @Test
    @DisplayName("Lives should correctly increment and decrement")
    public void livesShouldCorrectlyIncrementAndDecrement() {
        Lives lives = new Lives(player);

        lives.set(VALID_LIVES);
        lives.increment(3);
        assertEquals(VALID_LIVES + 3, lives.get());

        lives.set(VALID_LIVES);
        lives.decrement(4);
        assertEquals(VALID_LIVES - 4, lives.get());
    }
}
