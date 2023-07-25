package net.ilikefood971.forf.event;

import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class PlayerUseEntity implements UseEntityCallback {
    
    // On interact with entity
    @Override
    public ActionResult interact(PlayerEntity player, World world, Hand hand, Entity entity, @Nullable EntityHitResult hitResult) {
        // Check for villager
        if (entity instanceof MerchantEntity) {
            // Return Success which will do a hand swing but will be ignored by the server, thus preventing trading
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }
}
