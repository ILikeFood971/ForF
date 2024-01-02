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

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.ilikefood971.forf.util.Util;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class PlayerTrackerGui extends SimpleGui {
    private final ItemStack playerTracker;

    /**
     * Constructs a new simple container gui for the supplied player.
     *
     * @param type          the screen handler that the client should display
     * @param player        the player to server this gui to
     * @param playerTracker the player tracker item
     */
    public PlayerTrackerGui(ScreenHandlerType<?> type, ServerPlayerEntity player, ItemStack playerTracker) {
        super(type, player, false);
        this.playerTracker = playerTracker;
        this.getPlayerHeadsAndPutIntoInventory(player);
    }

    @Override
    public boolean onClick(int index, ClickType type, SlotActionType action, GuiElementInterface element) {
        if (element != null && element.getItemStack().isOf(Items.PLAYER_HEAD)) {
            ((PlayerTrackerItem) PlayerTrackerItem.PLAYER_TRACKER).onGuiClick(element.getItemStack(), this);
            this.close();
        }
        return false;
    }

    private void getPlayerHeadsAndPutIntoInventory(ServerPlayerEntity excludedPlayer) {
        List<ServerPlayerEntity> playerList = Util.SERVER.getPlayerManager().getPlayerList();
        playerList.remove(excludedPlayer);

        for (ServerPlayerEntity player : playerList) {

            String playerName = player.getGameProfile().getName();

            Text lore = Text.translatable("forf.tracker.gui.lore");
            Text name = Text.literal(playerName).formatted(Formatting.RED);

            GuiElementBuilder skullBuilder = new GuiElementBuilder();
            skullBuilder.getOrCreateNbt().put("SkullOwner", NbtHelper.writeGameProfile(new NbtCompound(), player.getGameProfile()));

            skullBuilder.setItem(Items.PLAYER_HEAD);
            skullBuilder.addLoreLine(lore);
            skullBuilder.setName(name);

            this.addSlot(skullBuilder.build());
        }
    }

    public ItemStack getPlayerTracker() {
        return playerTracker;
    }
}
