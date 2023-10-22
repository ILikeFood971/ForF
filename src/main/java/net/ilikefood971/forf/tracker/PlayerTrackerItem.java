/*
 * This file is part of the Friend or Foe project, licensed under the
 * GNU General Public License v3.0
 *
 * Copyright (C) 2023  ILikeFood971 and contributors
 *
 * Friend or Foe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * Friend or Foe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Friend or Foe.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.ilikefood971.forf.tracker;

import eu.pb4.polymer.core.api.item.PolymerItem;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.ilikefood971.forf.config.Config;
import net.ilikefood971.forf.util.PlayerTrackerGui;
import net.ilikefood971.forf.util.Util;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.Vanishable;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static net.ilikefood971.forf.util.Util.CONFIG;

// TODO Portals
public class PlayerTrackerItem extends Item implements PolymerItem, Vanishable {
    public PlayerTrackerItem(Settings settings) {
        super(settings);
        MutableText temp = Text.translatable("item.forf.player_tracker").formatted(Formatting.GREEN);
        this.defaultName = removeItalics(temp);
    }

    public static final Item PLAYER_TRACKER = new PlayerTrackerItem(new FabricItemSettings().maxCount(1));
    private final MutableText defaultName;
    private int tickTillNext = 20;

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return Items.COMPASS;
    }

    @Override
    public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipContext context, @Nullable ServerPlayerEntity player) {
        if (!itemStack.hasCustomName()) itemStack.setCustomName(defaultName);
        return PolymerItem.super.getPolymerItemStack(itemStack, context, player);
    }

    // Regular use on the server side
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!CONFIG.playerTracker()) return TypedActionResult.consume(user.getStackInHand(hand));
        // We don't need the polymer item because this is on the server
        ItemStack itemStack = user.getStackInHand(hand);
        if (!itemStack.getOrCreateNbt().getBoolean("isTracking")) {
            assert user instanceof ServerPlayerEntity;
            PlayerTrackerGui gui = new PlayerTrackerGui(ScreenHandlerType.GENERIC_9X3, ((ServerPlayerEntity) user), itemStack);

            Text guiTitle = Text.translatable("forf.tracker.guiTitle").formatted(Formatting.RED, Formatting.BOLD);
            gui.setTitle(guiTitle);
            gui.open();

        } else {
            if (itemStack.getNbt().getBoolean("isTracking")) {
                updateTracker(itemStack, user.getWorld());
            }
        }
        return TypedActionResult.success(itemStack);
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return super.hasGlint(stack) || stack.getOrCreateNbt().getBoolean("isTracking");
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("forf.tracker.tooltip").formatted(Formatting.GRAY));
    }

    public void onGuiClick(ItemStack selectedStack, PlayerTrackerGui gui) {
        ItemStack playerTracker = gui.getPlayerTracker();
        ServerPlayerEntity target = Util.SERVER.getPlayerManager().getPlayer(selectedStack.getName().getString());
        // Get the skull owner and put that into the player tracker's NBT
        UUID targetUUID = target.getUuid();
        NbtCompound nbt = playerTracker.getOrCreateNbt();
        nbt.putIntArray("TrackedPlayer", NbtHelper.fromUuid(targetUUID).getIntArray());
        // Mark the player tracker as tracking and put in the expiration time
        nbt.putBoolean("isTracking", true);
        nbt.putLong("Expiration", Instant.now().plus(Duration.ofMinutes(CONFIG.trackerExpirationMinutes())).toEpochMilli());

        MutableText text1 = Text.translatable("item.forf.player_tracker").append(": ").formatted(Formatting.LIGHT_PURPLE);
        MutableText text2 = Text.literal(target.getEntityName()).formatted(Formatting.RED, Formatting.BOLD);
        playerTracker.setCustomName(removeItalics(text1).append(removeItalics(text2)));
        gui.getPlayer().sendMessage(Text.translatable("forf.tracker.tracking", target.getEntityName(), CONFIG.trackerExpirationMinutes()).formatted(Formatting.YELLOW),   false);

        updateTracker(playerTracker, gui.getPlayer().getWorld());
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);
        // If the player tracker is not enabled, don't do anything
        if (!CONFIG.playerTracker()) return;

        if (stack.getOrCreateNbt().getBoolean("isTracking")) {
            NbtCompound nbt = stack.getNbt();
            if (nbt.getLong("Expiration") <= Instant.now().toEpochMilli()) {
                stack.setCount(0);
                return;
            }


            if (tickTillNext == 0 && CONFIG.trackerUpdateType() == Config.UpdateType.AUTOMATIC) {
                updateTracker(stack, world);
            }
            tickTillNext--;
        }
    }
    private void updateTracker(ItemStack stack, World world) {
        NbtCompound nbt = stack.getNbt();
        ServerPlayerEntity trackedPlayer = world.getServer().getPlayerManager().getPlayer(NbtHelper.toUuid(nbt.get("TrackedPlayer")));
        BlockPos blockPos = trackedPlayer.getBlockPos();/*
        if (!(trackedPlayer.getWorld().getRegistryKey() == world.getRegistryKey())) {
            blockPos = trackedPlayer.lastport
        }*/

        nbt.put("LodestonePos", NbtHelper.fromBlockPos(blockPos));
        nbt.putBoolean("LodestoneTracked", true);
        nbt.putString("LodestoneDimension", world.getRegistryKey().getValue().toString());
        tickTillNext = CONFIG.trackerAutoUpdateDelay();
    }

    private static MutableText removeItalics(Text text) {
        return text.copy().setStyle(text.getStyle().withItalic(false));
    }
}
