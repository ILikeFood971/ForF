package net.ilikefood971.forf.config;

import blue.endless.jankson.Jankson;
import io.wispforest.owo.config.ConfigWrapper;
import io.wispforest.owo.config.Option;
import io.wispforest.owo.util.Observable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Config extends ConfigWrapper<net.ilikefood971.forf.config.ForfConfig> {

    public final Keys keys = new Keys();

    private final Option<java.lang.Boolean> spectators = this.optionForKey(this.keys.spectators);
    private final Option<net.minecraft.world.GameMode> spectatorGamemode = this.optionForKey(this.keys.spectatorGamemode);
    private final Option<java.lang.Integer> startingLives = this.optionForKey(this.keys.startingLives);
    private final Option<java.lang.String> tablistHeader = this.optionForKey(this.keys.tablistHeader);
    private final Option<java.lang.Boolean> playerTracker = this.optionForKey(this.keys.playerTracker);
    private final Option<net.ilikefood971.forf.config.ForfConfig.UpdateType> trackerUpdateType = this.optionForKey(this.keys.trackerUpdateType);
    private final Option<java.lang.Integer> trackerAutoUpdateDelay = this.optionForKey(this.keys.trackerAutoUpdateDelay);
    private final Option<java.lang.Boolean> pvPTimer_pvpTimer = this.optionForKey(this.keys.pvPTimer_pvpTimer);
    private final Option<java.lang.Integer> pvPTimer_minRandomOnTime = this.optionForKey(this.keys.pvPTimer_minRandomOnTime);
    private final Option<java.lang.Integer> pvPTimer_maxRandomOnTime = this.optionForKey(this.keys.pvPTimer_maxRandomOnTime);
    private final Option<java.lang.Integer> pvPTimer_minRandomOffTime = this.optionForKey(this.keys.pvPTimer_minRandomOffTime);
    private final Option<java.lang.Integer> pvPTimer_getMaxRandomOnTime = this.optionForKey(this.keys.pvPTimer_getMaxRandomOnTime);
    private final Option<java.lang.Boolean> restrictions_totemDrops = this.optionForKey(this.keys.restrictions_totemDrops);
    private final Option<java.lang.Boolean> restrictions_villagerTrading = this.optionForKey(this.keys.restrictions_villagerTrading);
    private final Option<java.lang.Boolean> restrictions_goldenAppleCrafting = this.optionForKey(this.keys.restrictions_goldenAppleCrafting);
    private final Option<java.lang.Boolean> restrictions_elytraInEndShip = this.optionForKey(this.keys.restrictions_elytraInEndShip);
    private final Option<java.util.Set<java.lang.String>> forfPlayersUUIDs = this.optionForKey(this.keys.forfPlayersUUIDs);
    private final Option<java.lang.Boolean> started = this.optionForKey(this.keys.started);

    private Config() {
        super(net.ilikefood971.forf.config.ForfConfig.class);
    }

    private Config(Consumer<Jankson.Builder> janksonBuilder) {
        super(net.ilikefood971.forf.config.ForfConfig.class, janksonBuilder);
    }

    public static Config createAndLoad() {
        var wrapper = new Config();
        wrapper.load();
        return wrapper;
    }

    public static Config createAndLoad(Consumer<Jankson.Builder> janksonBuilder) {
        var wrapper = new Config(janksonBuilder);
        wrapper.load();
        return wrapper;
    }

    public boolean spectators() {
        return spectators.value();
    }

    public void spectators(boolean value) {
        spectators.set(value);
    }

    public net.minecraft.world.GameMode spectatorGamemode() {
        return spectatorGamemode.value();
    }

    public void spectatorGamemode(net.minecraft.world.GameMode value) {
        spectatorGamemode.set(value);
    }

    public int startingLives() {
        return startingLives.value();
    }

    public void startingLives(int value) {
        startingLives.set(value);
    }

    public java.lang.String tablistHeader() {
        return tablistHeader.value();
    }

    public void tablistHeader(java.lang.String value) {
        tablistHeader.set(value);
    }

    public boolean playerTracker() {
        return playerTracker.value();
    }

    public void playerTracker(boolean value) {
        playerTracker.set(value);
    }

    public net.ilikefood971.forf.config.ForfConfig.UpdateType trackerUpdateType() {
        return trackerUpdateType.value();
    }

    public void trackerUpdateType(net.ilikefood971.forf.config.ForfConfig.UpdateType value) {
        trackerUpdateType.set(value);
    }

    public int trackerAutoUpdateDelay() {
        return trackerAutoUpdateDelay.value();
    }

    public void trackerAutoUpdateDelay(int value) {
        trackerAutoUpdateDelay.set(value);
    }

    public final PvPTimer_ pvPTimer = new PvPTimer_();
    public class PvPTimer_ implements PvPTimer {
        public boolean pvpTimer() {
            return pvPTimer_pvpTimer.value();
        }

        public void pvpTimer(boolean value) {
            pvPTimer_pvpTimer.set(value);
        }

        public int minRandomOnTime() {
            return pvPTimer_minRandomOnTime.value();
        }

        public void minRandomOnTime(int value) {
            pvPTimer_minRandomOnTime.set(value);
        }

        public int maxRandomOnTime() {
            return pvPTimer_maxRandomOnTime.value();
        }

        public void maxRandomOnTime(int value) {
            pvPTimer_maxRandomOnTime.set(value);
        }

        public int minRandomOffTime() {
            return pvPTimer_minRandomOffTime.value();
        }

        public void minRandomOffTime(int value) {
            pvPTimer_minRandomOffTime.set(value);
        }

        public int getMaxRandomOnTime() {
            return pvPTimer_getMaxRandomOnTime.value();
        }

        public void getMaxRandomOnTime(int value) {
            pvPTimer_getMaxRandomOnTime.set(value);
        }

    }
    public final Restrictions_ restrictions = new Restrictions_();
    public class Restrictions_ implements Restrictions {
        public boolean totemDrops() {
            return restrictions_totemDrops.value();
        }

        public void totemDrops(boolean value) {
            restrictions_totemDrops.set(value);
        }

        public boolean villagerTrading() {
            return restrictions_villagerTrading.value();
        }

        public void villagerTrading(boolean value) {
            restrictions_villagerTrading.set(value);
        }

        public boolean goldenAppleCrafting() {
            return restrictions_goldenAppleCrafting.value();
        }

        public void goldenAppleCrafting(boolean value) {
            restrictions_goldenAppleCrafting.set(value);
        }

        public boolean elytraInEndShip() {
            return restrictions_elytraInEndShip.value();
        }

        public void elytraInEndShip(boolean value) {
            restrictions_elytraInEndShip.set(value);
        }

    }
    public java.util.Set<java.lang.String> forfPlayersUUIDs() {
        return forfPlayersUUIDs.value();
    }

    public void forfPlayersUUIDs(java.util.Set<java.lang.String> value) {
        forfPlayersUUIDs.set(value);
    }

    public boolean started() {
        return started.value();
    }

    public void started(boolean value) {
        started.set(value);
    }

    public interface PvPTimer {
        boolean pvpTimer();
        void pvpTimer(boolean value);
        int minRandomOnTime();
        void minRandomOnTime(int value);
        int maxRandomOnTime();
        void maxRandomOnTime(int value);
        int minRandomOffTime();
        void minRandomOffTime(int value);
        int getMaxRandomOnTime();
        void getMaxRandomOnTime(int value);
    }
    public interface Restrictions {
        boolean totemDrops();
        void totemDrops(boolean value);
        boolean villagerTrading();
        void villagerTrading(boolean value);
        boolean goldenAppleCrafting();
        void goldenAppleCrafting(boolean value);
        boolean elytraInEndShip();
        void elytraInEndShip(boolean value);
    }
    public static class Keys {
        public final Option.Key spectators = new Option.Key("spectators");
        public final Option.Key spectatorGamemode = new Option.Key("spectatorGamemode");
        public final Option.Key startingLives = new Option.Key("startingLives");
        public final Option.Key tablistHeader = new Option.Key("tablistHeader");
        public final Option.Key playerTracker = new Option.Key("playerTracker");
        public final Option.Key trackerUpdateType = new Option.Key("trackerUpdateType");
        public final Option.Key trackerAutoUpdateDelay = new Option.Key("trackerAutoUpdateDelay");
        public final Option.Key pvPTimer_pvpTimer = new Option.Key("pvPTimer.pvpTimer");
        public final Option.Key pvPTimer_minRandomOnTime = new Option.Key("pvPTimer.minRandomOnTime");
        public final Option.Key pvPTimer_maxRandomOnTime = new Option.Key("pvPTimer.maxRandomOnTime");
        public final Option.Key pvPTimer_minRandomOffTime = new Option.Key("pvPTimer.minRandomOffTime");
        public final Option.Key pvPTimer_getMaxRandomOnTime = new Option.Key("pvPTimer.getMaxRandomOnTime");
        public final Option.Key restrictions_totemDrops = new Option.Key("restrictions.totemDrops");
        public final Option.Key restrictions_villagerTrading = new Option.Key("restrictions.villagerTrading");
        public final Option.Key restrictions_goldenAppleCrafting = new Option.Key("restrictions.goldenAppleCrafting");
        public final Option.Key restrictions_elytraInEndShip = new Option.Key("restrictions.elytraInEndShip");
        public final Option.Key forfPlayersUUIDs = new Option.Key("forfPlayersUUIDs");
        public final Option.Key started = new Option.Key("started");
    }
}

