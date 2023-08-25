package net.ilikefood971.forf.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.ilikefood971.forf.util.mixinInterfaces.IPlayerTracker;
import net.minecraft.item.CompassItem;
import net.minecraft.server.world.ServerWorld;

import java.util.ArrayList;
import java.util.List;

public class ServerTickEvent implements ServerTickEvents.EndWorldTick {
    public static List<IPlayerTracker> openPlayerTrackers = new ArrayList<>();
    public static List<IPlayerTracker> toRemove = new ArrayList<>();
    @Override
    public void onEndTick(ServerWorld world) {
        for (IPlayerTracker playerTracker : openPlayerTrackers) {
//            playerTracker.onEndTick(world);
        }
        openPlayerTrackers.removeAll(toRemove);
        toRemove.clear();
    }
}
