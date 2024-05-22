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

import com.mojang.authlib.GameProfile;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.ilikefood971.forf.config.Config;
import net.ilikefood971.forf.mixin.IGetPortalPos;
import net.ilikefood971.forf.util.Util;
import net.minecraft.client.item.TooltipType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LodestoneTrackerComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static net.ilikefood971.forf.util.Util.CONFIG;
import static net.ilikefood971.forf.util.Util.SERVER;

public class PlayerTrackerItem extends Item implements PolymerItem {
    public static final Item PLAYER_TRACKER = new PlayerTrackerItem(new Item.Settings());
    private static int tickTillNext = 20;

    public PlayerTrackerItem(Settings settings) {
        super(settings);
    }

    private static String getName(UUID targetUUID) {
        ServerPlayerEntity player = SERVER.getPlayerManager().getPlayer(targetUUID);
        String targetName;
        if (player != null) {
            targetName = player.getGameProfile().getName();
        } else {
            GameProfile offlineProfile = Util.getOfflineProfile(targetUUID);
            targetName = offlineProfile.getName();
        }
        return targetName;
    }

    public static void updateTracker(ItemStack stack, World world) {
        if (!(stack.getItem() instanceof PlayerTrackerItem)) return;

        TrackerData.PlayerTrackerComponent data = TrackerData.getData(stack);
        ServerPlayerEntity trackedPlayer = SERVER.getPlayerManager().getPlayer(data.target());

        World targetWorld;
        if (trackedPlayer != null) {
            targetWorld = trackedPlayer.getWorld();
        } else {
            // The target player is offline
            stack.set(DataComponentTypes.LODESTONE_TRACKER, new LodestoneTrackerComponent(Optional.empty(), true));
            tickTillNext = CONFIG.trackerAutoUpdateDelay();
            return;
        }

        BlockPos blockPos = null;
        // Checks if the player holding the tracker isn't in the same dimension as the target
        if (targetWorld.getRegistryKey() != world.getRegistryKey()) {
            // If the neither of them is in the end
            if (targetWorld.getRegistryKey() != World.END && world.getRegistryKey() != World.END) {
                // Set the block pos to the nether portal that they used
                blockPos = ((IGetPortalPos) trackedPlayer).getLastNetherPortalPosition();
            } else {
                // If one is in the end, set the world to target world and make the compass spin randomly
                world = targetWorld;
            }
        } else {
            blockPos = trackedPlayer.getBlockPos();
        }

        LodestoneTrackerComponent lodestoneTrackerComponent = new LodestoneTrackerComponent(Optional.of(GlobalPos.create(world.getRegistryKey(), blockPos)), true);
        stack.set(DataComponentTypes.LODESTONE_TRACKER, lodestoneTrackerComponent);

        tickTillNext = CONFIG.trackerAutoUpdateDelay();
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return Items.COMPASS;
    }

    // Regular use on the server side
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        // We don't need the polymer item because this is on the server
        ItemStack itemStack = user.getStackInHand(hand);

        if (!CONFIG.playerTracker()) return TypedActionResult.consume(itemStack);

        if (!TrackerData.getData(itemStack).tracking() && user instanceof ServerPlayerEntity serverUser) {
            PlayerTrackerGui gui = new PlayerTrackerGui(ScreenHandlerType.GENERIC_9X3, serverUser, itemStack);

            Text guiTitle = Text.translatable("forf.tracker.gui.title");
            gui.setTitle(guiTitle);
            gui.open();
        } else {
            updateTracker(itemStack, user.getWorld());
        }
        return TypedActionResult.success(itemStack);
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return TrackerData.getData(stack).tracking() || super.hasGlint(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        TrackerData.PlayerTrackerComponent data = TrackerData.getData(stack);
        if (data.tracking()) {
            String targetName = getName(data.target());

            tooltip.set(0, Text.translatable("forf.tracker.trackingTooltip", Text.literal(targetName).formatted(
                    Formatting.RED, Formatting.BOLD
            )));
        } else {
            boolean hasCustomName = stack.contains(DataComponentTypes.CUSTOM_NAME);
            if (!hasCustomName) {
                tooltip.set(0, Text.translatable("item.forf.player_tracker").formatted(Formatting.GREEN));
            }
            tooltip.add(Text.translatable("forf.tracker.tooltip"));
        }
    }

    public void onGuiClick(ItemStack selectedHead, PlayerTrackerGui gui) {
        ItemStack playerTracker = gui.getPlayerTracker();
        ServerPlayerEntity target = SERVER.getPlayerManager().getPlayer(selectedHead.getName().getString());
        if (target == null) {
            gui.getPlayer().sendMessage(Text.translatable("forf.tracker.playerNotFound"), false);
            return;
        }

        Instant expiration = Instant.now().plus(Duration.ofMinutes(CONFIG.trackerExpirationMinutes()));
        // Mark the player tracker as tracking, put in the target, and put in the expiration time
        TrackerData.PlayerTrackerComponent playerTrackerComponent = new TrackerData.PlayerTrackerComponent(target.getUuid(), true, expiration.toEpochMilli());
        TrackerData.applyData(playerTracker, playerTrackerComponent);
        // Send the message to the player
        gui.getPlayer().sendMessage(
                Text.translatable("forf.tracker.tracking",
                        target.getGameProfile().getName(),
                        String.valueOf(CONFIG.trackerExpirationMinutes())
                ).formatted(Formatting.YELLOW),
                false
        );

        updateTracker(playerTracker, gui.getPlayer().getWorld());
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);
        // If the player tracker is not enabled, don't do anything
        if (!CONFIG.playerTracker()) return;

        TrackerData.PlayerTrackerComponent data = TrackerData.getData(stack);
        if (data.tracking()) {
            long expiration = data.expiration();
            if (expiration <= Instant.now().toEpochMilli()) {
                stack.setCount(0);

                Text name = Text.literal(getName(data.target())).formatted(Formatting.RED);
                entity.sendMessage(Text.translatable("forf.tracker.expired", name));

                return;
            }
            // If it's not in the hand then update it
            boolean isInHand = entity instanceof ServerPlayerEntity player && (player.getMainHandStack().equals(stack) || player.getOffHandStack().equals(stack));
            if (!isInHand || (tickTillNext == 0 && CONFIG.trackerUpdateType() == Config.UpdateType.AUTOMATIC)) {
                updateTracker(stack, world);
            }
            tickTillNext--;
        }
    }
}
