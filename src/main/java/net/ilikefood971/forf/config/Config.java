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
package net.ilikefood971.forf.config;

import blue.endless.jankson.Comment;
import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonGrammar;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.api.SyntaxError;
import net.fabricmc.loader.api.FabricLoader;
import net.ilikefood971.forf.util.Util;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.world.GameMode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal", "CanBeFinal"})
public class Config {
    @Comment("Whether non forf or forf players that ran out of lives can join")
    private boolean spectators = false;
    @Comment("If spectators are allowed, what gamemode should they be in?")
    private GameMode spectatorGamemode = GameMode.SPECTATOR;
    @Comment("When you start forf, how many lives should everyone start with")
    private int startingLives = 10;
    @Comment("How many lives should the server assassin start out with")
    private int assassinStartingLives = 1;

    @Comment("Should players be able to go over the starting lives")
    private boolean overfill = false;

    @Comment("The text to put at in the header of the tablist. Leave blank to remove")
    private String tablistHeader = "{\"text\":\"Friend or Foe\",\"color\":\"yellow\",\"bold\":true}";
    @Comment("What render type to use when a player looks at the lives in the tablist. Options are INTEGER or HEARTS")
    private ScoreboardCriterion.RenderType tablistLivesRenderType = ScoreboardCriterion.RenderType.INTEGER;

    @Comment("Should the player tracker be craft-able and usable?")
    private boolean playerTracker = true;
    @Comment("If player trackers are enabled, when should they update.\nAUTOMATIC means to update every tick\nIf you're using AUTOMATIC, the item will bob in the hand every time the item is updated.")
    private UpdateType trackerUpdateType = UpdateType.AUTOMATIC;
    @Comment("The amount of time that the tracker lasts for before expiring")
    private int trackerExpirationMinutes = 60;
    @Comment("Should the first kill give a mending book?")
    private boolean firstKillMendingBook = true;
    @Comment("Should the player death messages be red?")
    private boolean redDeathMessage = true;
    @Comment("Should the first player that gets 10 kill earn a life?")
    private boolean tenKillsLifeQuest = true;
    @Comment("Timer that automatically turns PvP on and off")
    private PvPTimer pvPTimer = new PvPTimer();
    @Comment("Restrictions to prevent op things for this play-style")
    private Restrictions restrictions = new Restrictions();


    // Methods for Config

    public static Config loadFromFile() {
        return loadFromFile(FabricLoader.getInstance().getConfigDir().resolve("forf-config.json5").toFile());
    }

    public static Config loadFromFile(File file) {
        if (!Files.exists(file.toPath())) {
            Config config = new Config();
            config.save(file);
            return config;
        }
        // Create a new Jankson instance
        Jankson jankson = Jankson.builder().build();
        // Parse the config file into a JSON Object
        try {
            JsonObject configJson = jankson.load(file);
            // Convert the raw object into the POJO type
            Config config = jankson.fromJson(configJson, Config.class);
            config.save(file); // Save it in case there's new fields after mod update
            return config;
        } catch (IOException | SyntaxError e) {
            Util.LOGGER.error(e.toString());
            return new Config(); // You could also throw a RuntimeException instead
        }
    }

    // This only ever gets used to create the config or update it to when new fields are added
    private void save(File file) {
        Jankson jankson = Jankson.builder().build();
        String result = jankson
                .toJson(this)
                .toJson(JsonGrammar.JANKSON);
        try {
            boolean fileIsUsable = file.exists() || file.createNewFile();
            if (!fileIsUsable) return;
            FileOutputStream out = new FileOutputStream(file, false);

            out.write(result.getBytes());
            out.flush();
            out.close();
        } catch (IOException e) {
            Util.LOGGER.error(e.toString());
        }
    }

    // Getters
    public boolean spectators() {
        return spectators;
    }

    public GameMode spectatorGamemode() {
        return spectatorGamemode;
    }

    public int startingLives() {
        return startingLives;
    }

    public int assassinStartingLives() {
        return assassinStartingLives;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean overfill() {
        return overfill;
    }

    public String tablistHeader() {
        return tablistHeader;
    }

    public ScoreboardCriterion.RenderType tablistLivesRenderType() {
        return tablistLivesRenderType;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean playerTracker() {
        return playerTracker;
    }

    public UpdateType trackerUpdateType() {
        return trackerUpdateType;
    }

    public int trackerExpirationMinutes() {
        return trackerExpirationMinutes;
    }

    public boolean firstKillMendingBook() {
        return firstKillMendingBook;
    }

    public boolean redDeathMessage() {
        return redDeathMessage;
    }

    public boolean tenKillsLifeQuest() {
        return tenKillsLifeQuest;
    }

    public PvPTimer pvPTimer() {
        return pvPTimer;
    }

    public Restrictions restrictions() {
        return restrictions;
    }

    // Tracker Options

    public enum UpdateType {
        AUTOMATIC,
        USE
    }

    // Nested Options

    @SuppressWarnings("CanBeFinal")
    public static class PvPTimer {
        @Comment("Use this to disable the PvP Timer completely")
        private boolean enabled = true;
        @Comment("When PvP turns on, what is the minimum random minutes for it to be on for")
        private int minRandomOnTime = 20;
        @Comment("Same as before but for the maximum amount of minutes it can be on for")
        private int maxRandomOnTime = 20;

        @Comment("The minimum time until the PvP Timer turns on again")
        private int minRandomOffTime = 10;
        @Comment("The maximum time until the PvP Timer turns on again")
        private int maxRandomOffTime = 30;

        public boolean enabled() {
            return enabled;
        }

        public int minRandomOnTime() {
            return minRandomOnTime;
        }

        public int maxRandomOnTime() {
            return maxRandomOnTime;
        }

        public int minRandomOffTime() {
            return minRandomOffTime;
        }

        public int maxRandomOffTime() {
            return maxRandomOffTime;
        }
    }

    @SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal", "CanBeFinal"})
    public static class Restrictions {
        private boolean totemDrops = false;
        private boolean villagerTrading = false;
        private boolean goldenAppleCrafting = false;
        @Comment("Will only prevent elytras in generation\nIf the ship has been generated already elytra will still be there")
        private boolean elytraInEndShip = false;
        @Comment("Explosions do this much percent less damage to players except for the player who detonated it\nSet to 0 to disable the nerf")
        private int explosionNerf = 90;
        @Comment("The time in seconds that it takes for the pearl cooldown to reset. Set to 1 for vanilla behavior")
        private int pearlCooldownTime = 7;

        public boolean totemDrops() {
            return totemDrops;
        }

        public boolean villagerTrading() {
            return villagerTrading;
        }

        public boolean goldenAppleCrafting() {
            return goldenAppleCrafting;
        }

        public boolean elytraInEndShip() {
            return elytraInEndShip;
        }

        public int explosionNerf() {
            return explosionNerf;
        }

        public int pearlCooldownTime() {
            return pearlCooldownTime;
        }
    }
}
