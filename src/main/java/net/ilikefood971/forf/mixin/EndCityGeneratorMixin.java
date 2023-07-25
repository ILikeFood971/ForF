package net.ilikefood971.forf.mixin;

import net.ilikefood971.forf.Forf;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.structure.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EndCityGenerator.Piece.class)
public abstract class EndCityGeneratorMixin extends SimpleStructurePiece {
    public EndCityGeneratorMixin(StructurePieceType type, int length, StructureTemplateManager structureTemplateManager, Identifier id, String template, StructurePlacementData placementData, BlockPos pos) {
        super(type, length, structureTemplateManager, id, template, placementData, pos);
    }
    
    // Prevent the end city generator from actually putting any elytra item into the item frame. Still generates the item frame though
    @Inject(method = "handleMetadata", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/decoration/ItemFrameEntity;<init>(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;)V"
    ), cancellable = true)
    protected void handleMetadataWithoutElytra(String metadata, BlockPos pos, ServerWorldAccess world, Random random, BlockBox boundingBox, CallbackInfo ci) {
        ItemFrameEntity itemFrameEntity = new ItemFrameEntity(world.toServerWorld(), pos, this.placementData.getRotation().rotate(Direction.SOUTH));
        // This is where the itemFrameEntity could set it's item to an elytra but because of this inject there won't be any item
        world.spawnEntity(itemFrameEntity);
        ci.cancel();
    }
}
