package net.ilikefood971.forf.util.mixinInterfaces;

import net.minecraft.nbt.NbtCompound;

public interface IEntityDataSaver {
    NbtCompound getPersistentData();
    int getLives();
    
    void removeLife();
    void setLives(int lives);
}
