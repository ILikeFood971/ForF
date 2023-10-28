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
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

@SuppressWarnings("unused")
public class Config {
    @Comment("Whether non forf or forf players that ran out of lives can join")
    private boolean spectators = false;
    private GameMode spectatorGamemode = GameMode.SPECTATOR;
    @Comment("When you start forf, how many lives should everyone start with")
    private int startingLives = 10;

    @Comment("Should players be able to go over the starting lives")
    private boolean overfill = false;
    
    @Comment("The text to put at in the header of the tablist. Leave blank to remove")
    private String tablistHeader = "{\"text\":\"Friend or Foe\",\"color\":\"yellow\",\"bold\":true}";
    @Comment("What render type to use when a player looks at the lives in the tablist. Options are INTEGER or HEARTS")
    private ScoreboardCriterion.RenderType tablistLivesRenderType = ScoreboardCriterion.RenderType.INTEGER;
    
    @Comment("Should the player tracker be craft-able and usable?")
    private boolean playerTracker = true;
    @Comment("If player trackers are enabled, when should they update.\nAUTOMATIC means to update every x ticks with x being specified by trackerAutoUpdateDelay\nIf you're using AUTOMATIC, the item will bob in the hand everytime the item is updated so set it to either something high or use USE.")
    private UpdateType trackerUpdateType = UpdateType.USE;
    private int trackerAutoUpdateDelay = 20;
    @Comment("The amount of time that the tracker lasts for before expiring")
    private int trackerExpirationMinutes = 60;
    public enum UpdateType {
        AUTOMATIC,
        USE
    }
    
    private PvPTimer pvPTimer = new PvPTimer();
    
    @Comment("Restrictions to prevent op things for this play-style")
    public Restrictions restrictions = new Restrictions();
    
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
        
        private void save() {
            Util.CONFIG.save();
        }
        
        public boolean enabled() {
            return enabled;
        }
        
        public void enabled(boolean enabled) {
            this.enabled = enabled;
            this.save();
        }
        
        public int minRandomOnTime() {
            return minRandomOnTime;
        }
        
        public void minRandomOnTime(int minRandomOnTime) {
            this.minRandomOnTime = minRandomOnTime;
            this.save();
        }
        
        public int maxRandomOnTime() {
            return maxRandomOnTime;
        }
        
        public void maxRandomOnTime(int maxRandomOnTime) {
            this.maxRandomOnTime = maxRandomOnTime;
            this.save();
        }
        
        public int minRandomOffTime() {
            return minRandomOffTime;
        }
        
        public void minRandomOffTime(int minRandomOffTime) {
            this.minRandomOffTime = minRandomOffTime;
            this.save();
        }
        
        public int maxRandomOffTime() {
            return maxRandomOffTime;
        }
        
        public void maxRandomOffTime(int maxRandomOffTime) {
            this.maxRandomOffTime = maxRandomOffTime;
            this.save();
        }
    }
    
    public static class Restrictions {
        private boolean totemDrops = false;
        private boolean villagerTrading = true;
        private boolean goldenAppleCrafting = false;
        @Comment("Will only prevent elytras in generation\nIf the ship has been generated already elytra will still be there")
        private boolean elytraInEndShip = false;
        private void save() {
            Util.CONFIG.save();
        }
        public boolean totemDrops() {
            return totemDrops;
        }
        
        public void totemDrops(boolean totemDrops) {
            this.totemDrops = totemDrops;
            this.save();
        }
        
        public boolean villagerTrading() {
            return villagerTrading;
        }
        
        public void villagerTrading(boolean villagerTrading) {
            this.villagerTrading = villagerTrading;
            this.save();
        }
        
        public boolean goldenAppleCrafting() {
            return goldenAppleCrafting;
        }
        
        public void goldenAppleCrafting(boolean goldenAppleCrafting) {
            this.goldenAppleCrafting = goldenAppleCrafting;
            this.save();
        }
        
        public boolean elytraInEndShip() {
            return elytraInEndShip;
        }
        
        public void elytraInEndShip(boolean elytraInEndShip) {
            this.elytraInEndShip = elytraInEndShip;
            this.save();
        }
    }
    
    // Methods for Config
    
    private void updateConfig() {
        Config newConfig = loadFromFile();
        copyAllFields(this, newConfig);
    }
    
    private <T> void copyAllFields(T old, T newObject) {
        try {
            Class<?> clazz = old.getClass();
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                
                // Check to see if the field is a nested class that isn't an enum.
                if (field.getType().isNestmateOf(clazz) && !field.getType().isEnum()) {
                    // If it's a nested class, perform this all over again on the nested class
                    // I know it's recursive, but I don't think it will cause issues
                    copyAllFields(field.get(old), field.get(newObject));
                    continue;
                }
                if (!field.get(old).equals(field.get(newObject))) {
                    // Set the old field to the new field
                    field.set(old, field.get(newObject));
                }
            }
        } catch (IllegalAccessException e) {
            Util.LOGGER.error(e.toString());
        }
    }
    
    public static Config loadFromFile() {
        if (!Files.exists(FabricLoader.getInstance().getConfigDir().resolve("forf-config.json5"))) {
            Config config = new Config();
            config.save();
            return config;
        }
        // Create a new Jankson instance
        // (This can also be a static instance, defined outside the function)
        var jankson = Jankson.builder()
                // You can register adapters here to customize deserialization
                //.registerTypeAdapter(...)
                // Likewise, you can customize serializer behavior
                //.registerSerializer(...)
                // In most cases, the default Jankson is all you need.
                .build();
        // Parse the config file into a JSON Object
        try {
            Path resolve = FabricLoader.getInstance().getConfigDir().resolve("forf-config.json5");
            
            File configFile = resolve.toFile();
            JsonObject configJson = jankson.load(configFile);
            // Convert the raw object into your POJO type
            return jankson.fromJson(configJson, Config.class);
        } catch (IOException | SyntaxError e) {
            Util.LOGGER.error(e.toString());
            return new Config(); // You could also throw a RuntimeException instead
        }
    }
    
    
    private void save() {
        Path resolve = FabricLoader.getInstance().getConfigDir().resolve("forf-config.json5");
        File configFile = resolve.toFile();
        
        Jankson jankson = Jankson.builder().build();
        String result = jankson
                .toJson(this)
                .toJson(JsonGrammar.JANKSON);
        try {
            var fileIsUsable = configFile.exists() || configFile.createNewFile();
            if (!fileIsUsable) return;
            var out = new FileOutputStream(configFile, false);
            
            out.write(result.getBytes());
            out.flush();
            out.close();
        } catch (IOException e) {
            Util.LOGGER.error(e.toString());
        }
        
    }
    
    public boolean spectators() {
        this.updateConfig();
        return spectators;
    }

    public GameMode spectatorGamemode() {
        this.updateConfig();
        return spectatorGamemode;
    }

    public int startingLives() {
        this.updateConfig();
        return startingLives;
    }

    public boolean overfill() {
        this.updateConfig();
        return overfill;
    }

    public String tablistHeader() {
        this.updateConfig();
        return tablistHeader;
    }

    public ScoreboardCriterion.RenderType tablistLivesRenderType() {
        this.updateConfig();
        return tablistLivesRenderType;
    }

    public boolean playerTracker() {
        this.updateConfig();
        return playerTracker;
    }

    public UpdateType trackerUpdateType() {
        this.updateConfig();
        return trackerUpdateType;
    }

    public int trackerAutoUpdateDelay() {
        this.updateConfig();
        return trackerAutoUpdateDelay;
    }

    public int trackerExpirationMinutes() {
        this.updateConfig();
        return trackerExpirationMinutes;
    }
    
    public PvPTimer pvPTimer() {
        this.updateConfig();
        return pvPTimer;
    }

    public Restrictions restrictions() {
        this.updateConfig();
        return restrictions;
    }
}
