package net.ilikefood971.forf.util.mixinInterfaces;

import net.minecraft.item.ItemStack;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.world.ServerWorld;

public interface IPlayerTracker {
    public void onClicked(ItemStack itemStack);
    
    public void updatePlayerHeadList(PlayerManager playerManager);
    public boolean isPlayerTracker();
}
