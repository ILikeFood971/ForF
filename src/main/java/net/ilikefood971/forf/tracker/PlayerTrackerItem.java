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
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.ilikefood971.forf.config.Config;
import net.ilikefood971.forf.util.Util;
import net.ilikefood971.forf.util.mixinInterfaces.IGetPortalPos;
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
import java.util.Objects;
import java.util.UUID;

import static net.ilikefood971.forf.util.Util.CONFIG;
import static net.ilikefood971.forf.util.Util.SERVER;

public class PlayerTrackerItem extends Item implements PolymerItem, Vanishable {
    public static final Item PLAYER_TRACKER = new PlayerTrackerItem(new FabricItemSettings().maxCount(1));
    private static int tickTillNext = 20;

    public PlayerTrackerItem(Settings settings) {
        super(settings);
    }

    private static String getTargetName(UUID targetUUID) {
        ServerPlayerEntity player = SERVER.getPlayerManager().getPlayer(targetUUID);
        String targetName;
        if (player != null) {
            targetName = player.getGameProfile().getName();
        } else {
            GameProfile offlineProfile = Util.getOfflineProfile(targetUUID);
            if (offlineProfile == null) {
                targetName = "Not Found";
            } else {
                targetName = offlineProfile.getName();
            }
        }
        return targetName;
    }

    public static void updateTracker(ItemStack stack, World world) {
        if (!(stack.getItem() instanceof PlayerTrackerItem)) return;
        NbtCompound nbt = stack.getOrCreateNbt();
        ServerPlayerEntity trackedPlayer = SERVER.getPlayerManager().getPlayer(NbtHelper.toUuid(Objects.requireNonNull(nbt.get("TrackedPlayer"))));
        World targetWorld;
        if (trackedPlayer != null) {
            targetWorld = trackedPlayer.getWorld();
        } else {
            if (nbt.contains("LodestoneDimension")) {
                nbt.remove("LodestoneDimension");
            }
            tickTillNext = CONFIG.trackerAutoUpdateDelay();
            return;
        }
        BlockPos blockPos = null;
        // Checks if the player holding the tracker isn't in the same dimension as the target
        if (targetWorld.getRegistryKey() != world.getRegistryKey()) {
            // If the neither of them is in the end
            if (targetWorld.getRegistryKey() != World.END && world.getRegistryKey() != World.END) {
                // Set the block pos to the nether portal that they used
                blockPos = ((IGetPortalPos) trackedPlayer).getLastNetherPortalLocation();
            } else {
                // If one is in the end, set the world to target world and make the compass spin randomly
                world = targetWorld;
            }
        } else {
            blockPos = trackedPlayer.getBlockPos();
        }

        if (blockPos != null) nbt.put("LodestonePos", NbtHelper.fromBlockPos(blockPos));
        nbt.putString("LodestoneDimension", world.getRegistryKey().getValue().toString());
        tickTillNext = CONFIG.trackerAutoUpdateDelay();
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return Items.COMPASS;
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
        return super.hasGlint(stack) || stack.getOrCreateNbt().getBoolean("isTracking");
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if (stack.getOrCreateNbt().getBoolean("isTracking")) {
            assert stack.getNbt() != null;
            UUID targetUUID = stack.getNbt().getUuid("TrackedPlayer");
            String targetName = getTargetName(targetUUID);

            tooltip.set(0, Text.translatable("forf.tracker.trackingTooltip", Text.literal(targetName).formatted(
                    Formatting.RED, Formatting.BOLD
            )));
        } else {
            if (!stack.hasCustomName())
                tooltip.set(0, Text.translatable("item.forf.player_tracker").formatted(Formatting.GREEN));
            tooltip.add(Text.translatable("forf.tracker.tooltip"));
        }
    }

    public void onGuiClick(ItemStack selectedStack, PlayerTrackerGui gui) {
        ItemStack playerTracker = gui.getPlayerTracker();
        ServerPlayerEntity target = SERVER.getPlayerManager().getPlayer(selectedStack.getName().getString());
        if (target == null) {
            gui.getPlayer().sendMessage(Text.translatable("forf.tracker.playerNotFound"), false);
            return;
        }
        // Get the skull owner and put that into the player tracker's NBT
        UUID targetUUID = target.getUuid();
        NbtCompound nbt = playerTracker.getOrCreateNbt();
        nbt.putIntArray("TrackedPlayer", NbtHelper.fromUuid(targetUUID).getIntArray());
        // Mark the player tracker as tracking and put in the expiration time
        nbt.putBoolean("isTracking", true);
        nbt.putLong("Expiration", Instant.now().plus(Duration.ofMinutes(CONFIG.trackerExpirationMinutes())).toEpochMilli());

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

        if (stack.getOrCreateNbt().getBoolean("isTracking")) {
            NbtCompound nbt = stack.getNbt();
            assert nbt != null;

            long expiration = nbt.contains("Expiration") ? nbt.getLong("Expiration") : Instant.now().toEpochMilli();
            if (expiration <= Instant.now().toEpochMilli()) {
                stack.setCount(0);

                Text name = Text.literal(getTargetName(stack.getNbt().getUuid("TrackedPlayer"))).formatted(Formatting.RED);
                entity.sendMessage(Text.translatable("forf.tracker.expired", name));

                return;
            }
            // If it's not in the hand then update it
            boolean isInHand = entity.isPlayer() && (((ServerPlayerEntity) entity).getMainHandStack().equals(stack) || ((ServerPlayerEntity) entity).getOffHandStack().equals(stack));
            if (!isInHand || tickTillNext == 0 && CONFIG.trackerUpdateType() == Config.UpdateType.AUTOMATIC) {
                updateTracker(stack, world);
            }
            tickTillNext--;
        }
    }
}
