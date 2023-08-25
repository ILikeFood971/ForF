package net.ilikefood971.forf.config;

import blue.endless.jankson.Comment;
import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.ExcludeFromScreen;
import io.wispforest.owo.config.annotation.Nest;
import io.wispforest.owo.config.annotation.RestartRequired;
import net.minecraft.world.GameMode;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("unused")
@Config(name = "forf-config", wrapperName = "Config")
public class ForfConfig {
    @Comment("Whether non forf or forf players that ran out of lives can join")
    public boolean spectators = false;
    public GameMode spectatorGamemode = GameMode.SPECTATOR;
    public int startingLives = 10;
    
    @Comment("The text to put at in the header of the tablist. Leave blank to remove")
    public String tablistHeader = "{\"text\":\"Friend or Foe\",\"color\":\"yellow\",\"bold\":true}";
    
    @Comment("Should the player tracker be craft-able and usable?")
    public boolean playerTracker = true;
    @Comment("If player trackers are enabled, when should they update.\nAUTOMATIC means to update every x ticks with x being specified by trackerAutoUpdateDelay\nIf you're using AUTOMATIC, the item will bob in the hand everytime the item is updated so set it to either something high or use USE.\nNOTE: If you install the mod client-side, the bobbing will quit when the update is received")
    public UpdateType trackerUpdateType = UpdateType.USE;
    public int trackerAutoUpdateDelay = 20;
    public enum UpdateType {
        AUTOMATIC,
        USE;
    }
    
    @Nest
    public PvPTimer pvPTimer = new PvPTimer();
    
    @Comment("Restrictions to prevent op things for this play-style")
    @Nest
    public Restrictions restrictions = new Restrictions();
    
    public static class PvPTimer {
        @Comment("Use this to disable the PvP Timer completely")
        public boolean pvpTimer = true;
        @Comment("When PvP turns on, what is the minimum random minutes for it to be on for")
        public int minRandomOnTime = 20;
        @Comment("Same as before but for the maximum amount of minutes it can be on for")
        public int maxRandomOnTime = 20;
        
        @Comment("The minimum time until the PvP Timer turns on again")
        public int minRandomOffTime = 10;
        @Comment("The maximum time until the PvP Timer turns on again")
        public int getMaxRandomOnTime = 30;
    }
    
    public static class Restrictions {
        public boolean totemDrops = false;
        public boolean villagerTrading = true;
        @RestartRequired
        public boolean goldenAppleCrafting = false;
        @Comment("Will only prevent elytras in generation\nIf the ship has been generated already elytra will still be there")
        public boolean elytraInEndShip = false;
        
    }
    
    @Comment("Do not mess with these unless you are sure you know what you're doing")
    @ExcludeFromScreen
    public Set<String> forfPlayersUUIDs = new HashSet<>();
    @ExcludeFromScreen
    public boolean started = false;
    
}
