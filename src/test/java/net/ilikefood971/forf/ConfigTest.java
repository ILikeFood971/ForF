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

import net.ilikefood971.forf.config.Config;
import net.minecraft.world.GameMode;
import org.junit.jupiter.api.*;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigTest {
    @Test
    public void testConfig() throws URISyntaxException {
        URL url = Objects.requireNonNull(getClass().getClassLoader().getResource("forf-config.json5"));
        File file = Paths.get(url.toURI()).toFile();

        Config config = Config.loadFromFile(file);

        assertEquals(config.startingLives(), 7);
        assertEquals(config.spectatorGamemode(), GameMode.ADVENTURE);
        assertFalse(config.restrictions().totemDrops());
    }
}
