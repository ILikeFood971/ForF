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
import net.ilikefood971.forf.util.Util;
import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.server.network.ServerPlayerEntity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UtilTest {
    @BeforeAll
    public static void beforeAll() {
        SharedConstants.createGameVersion();
        Bootstrap.initialize();

        Util.PERSISTENT_DATA = new PersistentData();
    }

    @Test
    public void testAddPlayer() {
        ServerPlayerEntity mock = mock(FakePlayer.class);
        when(mock.getUuid()).thenReturn(FakePlayer.DEFAULT_UUID);

        Util.addNewPlayer(mock.getUuid());
        assertTrue(Util.isForfPlayer(mock));
    }

    @Test
    public void testRemovePlayer() {
        UUID uuid = UUID.randomUUID();

        Util.addNewPlayer(uuid);
        assertTrue(Util.isForfPlayer(uuid));

        Util.removePlayer(uuid);
        assertFalse(Util.isForfPlayer(uuid));
    }

}
