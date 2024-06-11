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

package net.ilikefood971.forf.event;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.ilikefood971.forf.data.DataHandler;
import net.ilikefood971.forf.util.LivesHelper;
import net.ilikefood971.forf.util.Util;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.ilikefood971.forf.util.Util.CONFIG;
import static net.ilikefood971.forf.util.Util.SERVER;

public class PlayerDeathEvent implements ServerLivingEntityEvents.AfterDeath {
    // Remove one life from the player on death
    @Override
    public void afterDeath(LivingEntity entity, DamageSource damageSource) {
        // Check that they are actually a player as this gets called for all entity deaths
        if (entity instanceof ServerPlayerEntity player) {
            LivesHelper lives = new LivesHelper(player);

            // Remove the life
            if (lives.get() > 0) {
                lives.decrement(1);
                player.sendMessage(Text.translatable(
                        "forf.event.death.livesLeft",
                        lives.get()
                ).formatted(Formatting.RED), false);
            }
            if (damageSource.getAttacker() instanceof ServerPlayerEntity killer) {
                if (DataHandler.getInstance().isFirstKill() && CONFIG.firstKillMendingBook()) {
                    DataHandler.getInstance().setFirstKill(false);

                    RegistryEntry<Enchantment> entry = SERVER
                            .getRegistryManager()
                            .get(RegistryKeys.ENCHANTMENT)
                            .getEntry(Enchantments.MENDING)
                            .orElseThrow();
                    ItemStack itemStack = EnchantedBookItem.forEnchantment(new EnchantmentLevelEntry(entry, 1));

                    killer.giveItemStack(itemStack);
                    SERVER.getPlayerManager().broadcast(Text.translatable(
                            "forf.event.death.firstKill",
                            killer.getName(),
                            player.getName()
                    ).formatted(Formatting.RED), false);
                } else if (DataHandler.getInstance().isTenKillsLifeQuest() && Util.getKills(killer.getUuid()) >= 10 && CONFIG.tenKillsLifeQuest()) {
                    LivesHelper killerLives = new LivesHelper(killer);
                    DataHandler.getInstance().setTenKillsLifeQuest(false);
                    int livesBefore = killerLives.get();
                    LivesHelper.increment(killer, 1);
                    Text message;
                    if (killerLives.get() != livesBefore) {
                        message = Text.translatable("forf.event.death.tenKills.lifeAwarded", killer.getName()).formatted(Formatting.RED);
                    } else
                        message = Text.translatable("forf.event.death.tenKills.alreadyFull", killer.getName()).formatted(Formatting.RED);
                    SERVER.getPlayerManager().broadcast(message, false);
                }
            }
        }
    }
}
