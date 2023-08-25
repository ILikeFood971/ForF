package net.ilikefood971.forf.mixin.restrictions;

import com.google.gson.JsonElement;
import net.ilikefood971.forf.Forf;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(RecipeManager.class)
public class RecipeManagerMixin {
    // Inject to the method that registers all the base recipes
    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/profiler/Profiler;)V", at = @At("HEAD"/*value = "INVOKE_ASSIGN", target = "Lcom/google/common/collect/ImmutableMap;builder()Lcom/google/common/collect/ImmutableMap$Builder;"*/))
    private void noGappleApply(Map<Identifier, JsonElement> map, ResourceManager resourceManager, Profiler profiler, CallbackInfo ci) {
        // Check config first
        if (!Forf.getCONFIG().restrictions.goldenAppleCrafting()) {
            // Remove the golden_apple recipe from the list of recipes to be registered
            map.remove(new Identifier("minecraft:golden_apple"));
        }
    }
}
