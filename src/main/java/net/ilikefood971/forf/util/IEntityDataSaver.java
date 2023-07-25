package net.ilikefood971.forf.util;

import net.minecraft.nbt.NbtCompound;

public interface IEntityDataSaver {
    NbtCompound getPersistentData();
    int getLives();
    
    void removeLife();
    void setLives(int lives);
    
    boolean setForf(boolean forfPlayer);
    
    boolean isForfPlayer();
}
