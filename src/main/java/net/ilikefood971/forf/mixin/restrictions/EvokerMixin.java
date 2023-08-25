package net.ilikefood971.forf.mixin.restrictions;

import net.ilikefood971.forf.Forf;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.EvokerEntity;
import net.minecraft.entity.mob.SpellcastingIllagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;

@Mixin(EvokerEntity.class)
public abstract class EvokerMixin extends SpellcastingIllagerEntity {
    protected EvokerMixin(EntityType<? extends SpellcastingIllagerEntity> entityType, World world) {
        super(entityType, world);
    }
    
    @Nullable
    @Override
    public ItemEntity dropStack(ItemStack stack) {
        if (stack.isOf(Items.TOTEM_OF_UNDYING) && !Forf.getCONFIG().restrictions.totemDrops()) return null;
        return super.dropStack(stack);
    }
}
