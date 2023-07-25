package net.ilikefood971.forf.event;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.ilikefood971.forf.util.IEntityDataSaver;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;

public class PlayerDeathEventCopyFrom implements ServerPlayerEvents.CopyFrom {
    
    @Override
    public void copyFromPlayer(ServerPlayerEntity oldPlayer, ServerPlayerEntity newPlayer, boolean alive) {
        IEntityDataSaver original = ((IEntityDataSaver) oldPlayer);
        NbtCompound nbtOriginal = original.getPersistentData();
        
        IEntityDataSaver player = ((IEntityDataSaver) newPlayer);
        NbtCompound nbtNew = player.getPersistentData();
        
        nbtNew.copyFrom(nbtOriginal);
        
    }
}
