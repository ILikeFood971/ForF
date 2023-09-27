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

package net.ilikefood971.forf.util;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.ilikefood971.forf.util.mixinInterfaces.IPlayerTracker;
import net.minecraft.item.CompassItem;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

public class PlayerTrackerGui extends SimpleGui implements ServerPlayConnectionEvents.Disconnect{
    private final CompassItem playerTracker;
    
    /**
     * Constructs a new simple container gui for the supplied player.
     *
     * @param type                  the screen handler that the client should display
     * @param player                the player to server this gui to
     * @param manipulatePlayerSlots if <code>true</code> the players inventory
     *                              will be treated as slots of this gui
     */
    public PlayerTrackerGui(ScreenHandlerType<?> type, ServerPlayerEntity player, boolean manipulatePlayerSlots, CompassItem playerTracker) {
        super(type, player, manipulatePlayerSlots);
        this.playerTracker = playerTracker;
        ServerPlayConnectionEvents.DISCONNECT.register(this);
    }
    
    @Override
    public boolean onClick(int index, ClickType type, SlotActionType action, GuiElementInterface element) {
        if (element != null && element.getItemStack().isOf(Items.PLAYER_HEAD)) {
            ((IPlayerTracker) this.playerTracker).onClicked(element.getItemStack());
            this.close();
        }
        return false;
    }
    
    @Override
    public void onPlayDisconnect(ServerPlayNetworkHandler handler, MinecraftServer server) {
        ((IPlayerTracker) this.playerTracker).updatePlayerHeadList(server.getPlayerManager());
        for (GuiElementInterface element : this.elements) {
            if (element.getItemStack().getNbt().getString("SkullOwner").equals(handler.getPlayer().getEntityName()))
                element.getItemStack().setCount(0);
        }
    }
}
