package net.ilikefood971.forf.mixin;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.ilikefood971.forf.Forf;
import net.ilikefood971.forf.config.ForfConfig;
import net.ilikefood971.forf.util.PlayerTrackerGui;
import net.ilikefood971.forf.util.mixinInterfaces.IPlayerTracker;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("MissingUnique")
@Mixin(CompassItem.class)
public abstract class CompassItemMixin extends Item implements Vanishable, IPlayerTracker {
    
    @Shadow
    public static boolean hasLodestone(ItemStack stack) {
        return false;
    }
    
    private final static SimpleInventory playerHeadsInventory = new SimpleInventory(27);
    private boolean isTracker;
    private String trackedPlayerName;
    private int ticksLeftToUpdate = Forf.getCONFIG().trackerAutoUpdateDelay();
    
    public CompassItemMixin(Settings settings) {
        super(settings);
    }
    
    public void updatePlayerHeadList(PlayerManager playerManager) {
        playerHeadsInventory.clear();
        
        int i = 0;
        for (ServerPlayerEntity player : playerManager.getPlayerList()) {
            
            String playerName = player.getEntityName();
            ItemStack playerHead = new ItemStack(Items.PLAYER_HEAD);
            
            playerHead.getOrCreateNbt().putString("SkullOwner", playerName);
            NbtCompound displayNbt = new NbtCompound();
            
            NbtString nbtString = NbtString.of("{\"text\":\"Click to track this player\",\"color\":\"yellow\",\"italic\":false}");
            NbtList nbtList = new NbtList();
            nbtList.add(nbtString);
            
            displayNbt.putString("Name", "{\"text\":\"" + playerName + "\",\"color\":\"red\",\"italic\":false}");
            displayNbt.put("Lore", nbtList);
            
            playerHead.getNbt().put("display", displayNbt);
            
            playerHeadsInventory.setStack(i, playerHead);
            i++;
        }
    }
    @Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
    public void preventUseIfTracker(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        if (((IPlayerTracker) context.getStack().getItem()).isPlayerTracker()) cir.setReturnValue(ActionResult.PASS); cir.cancel();
    }
    
    @Override
    public boolean isPlayerTracker() {
        return this.isTracker;
    }
    
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        if (this.hasLodestone(itemStack)) {
            itemStack.setNbt(getUpdatedTracker(this.trackedPlayerName, itemStack).getNbt());
            return TypedActionResult.pass(itemStack);
        }
        // FIXME use isTracker
        if (true && !world.isClient() && Forf.getCONFIG().playerTracker()) {
            updatePlayerHeadList(world.getServer().getPlayerManager());
            
            SimpleGui simpleGui = new PlayerTrackerGui(ScreenHandlerType.GENERIC_9X3, Forf.SERVER.getPlayerManager().getPlayer(user.getUuid()), false, ((CompassItem)((Object) this)));
            int i = 0;
            for (ItemStack itemStack1 : playerHeadsInventory.stacks) {
                simpleGui.setSlot(i, GuiElementBuilder.from(itemStack1));
                i++;
            }
            
            
            try {
                StringReader stringReader = new StringReader("{\"text\":\"Tracker Target\",\"color\":\"red\",\"bold\":true}");
                Text parsed = TextArgumentType.text().parse(stringReader);
                
                simpleGui.setTitle(parsed);
                simpleGui.open();
                
            } catch (CommandSyntaxException e) {
                Forf.LOGGER.error(e.toString());
                return TypedActionResult.fail(itemStack);
            }
            
            
            return TypedActionResult.consume(itemStack);
        }
        
        return TypedActionResult.pass(itemStack);
    }
    
    public void onClicked(ItemStack selectedStack) {
        String skullOwner = selectedStack.getNbt().getString("SkullOwner");
        this.trackedPlayerName = skullOwner;
        this.isTracker = true;
        this.ticksLeftToUpdate = 0;
    }
    private ItemStack getUpdatedTracker(String player1, ItemStack itemStack) {
        ServerPlayerEntity target = null;
        for (ServerPlayerEntity player : Forf.SERVER.getPlayerManager().getPlayerList()) {
            if (player.getEntityName().equals(player1)) target = player;
        }
        
        if (target == null) {
            Forf.LOGGER.info(player1 + " is offline");
            return itemStack;
        }
        this.isTracker = true;
        
        ItemStack oldCompass = itemStack.copy();
        ItemStack newCompass = itemStack.copy();
        NbtCompound itemTag = newCompass.getOrCreateNbt().copy();
        itemTag.putBoolean("LodestoneTracked", false);
        itemTag.putString("LodestoneDimension", target.getServerWorld().getRegistryKey().getValue().toString());
        NbtCompound lodestonePos = new NbtCompound();
        lodestonePos.putInt("X", target.getBlockX());
        lodestonePos.putInt("Y", target.getBlockY());
        lodestonePos.putInt("Z", target.getBlockZ());
        itemTag.put("LodestonePos", lodestonePos);
        newCompass.setNbt(itemTag);
        
        
        return newCompass;
        
    }
    
    @Inject(method = "inventoryTick", at = @At("HEAD") , cancellable = true)
    public void addPlayerTracker(ItemStack stack, World world, Entity entity, int slot, boolean selected, CallbackInfo ci) {
        if (this.isTracker && Forf.SERVER.getPlayerManager().getPlayer(this.trackedPlayerName) != null && entity instanceof PlayerEntity) {
            if (Forf.getCONFIG().trackerUpdateType() == ForfConfig.UpdateType.AUTOMATIC && (Forf.getCONFIG().trackerAutoUpdateDelay() == 0 || ticksLeftToUpdate == 0)) {
                ((PlayerEntity) entity).getInventory().setStack(slot, this.getUpdatedTracker(this.trackedPlayerName, stack));
                ticksLeftToUpdate = Forf.getCONFIG().trackerAutoUpdateDelay();
            }
            ticksLeftToUpdate -= 1;
            ci.cancel();
        } else Forf.LOGGER.info("NBT removed");
    }
}
