package net.ilikefood971.forf.mixin;

import net.ilikefood971.forf.util.IEntityDataSaver;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(Entity.class)
public abstract class PlayerEntityDataSaver implements IEntityDataSaver {
    
    @Unique
    private NbtCompound persistentData;
    
    @Override
    public NbtCompound getPersistentData() {
        if (this.persistentData == null) {
            this.persistentData = new NbtCompound();
        }
        return persistentData;
    }
    
    @SuppressWarnings("rawtypes")
    @Inject(method = "writeNbt", at = @At("HEAD"))
    protected void injectWriteMethod(NbtCompound nbt, CallbackInfoReturnable info) {
        if (persistentData != null) {
            nbt.put("forf.data", persistentData);
        }
    }
    
    @Inject(method = "readNbt", at = @At("HEAD"))
    protected void injectReadMethod(NbtCompound nbt, CallbackInfo info) {
        if (nbt.contains("forf.data")) {
            persistentData = nbt.getCompound("forf.data");
        }
    }
    
    @Override
    public void removeLife() {
        // Don't remove life from non forf player
        NbtCompound nbt = this.getPersistentData();
        if (nbt.getBoolean("player")) {
            
            int newLives = nbt.getInt("lives");
            newLives -= 1;
            nbt.putInt("lives", newLives);
        }
    }
    
    @Override
    public void setLives(int lives) {
        NbtCompound nbt = this.getPersistentData();
        nbt.putInt("lives", lives);
    }
    
    @Override
    public int getLives() {
        NbtCompound nbt = this.getPersistentData();
        
        // Prevent non-forf-players from being included
        if (!nbt.getBoolean("player")) return -1;
        
        return nbt.getInt("lives");
    }
    
    @Override
    public boolean setForf(boolean forfPlayer) {
        NbtCompound nbt = this.getPersistentData();
        boolean before = nbt.getBoolean("player");
        
        if (before == forfPlayer) {
            return false;
        }
        
        nbt.putBoolean("player", forfPlayer);
        return true;
    }
    
    @Override
    public boolean isForfPlayer() {
        return this.getPersistentData().contains("player") && this.getPersistentData().getBoolean("player");
    }
    
}
