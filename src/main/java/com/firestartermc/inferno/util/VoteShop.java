package com.firestartermc.inferno.util;

import com.firestartermc.inferno.Inferno;
import com.firestartermc.kerosene.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class VoteShop {

    private final List<Reward> rewards;

    public VoteShop(Inferno inferno) {
        this.rewards = new ArrayList<>();

        var rewards = inferno.getConfig().getConfigurationSection("voteshop.rewards");
        for (var key : rewards.getKeys(false)) {
            var reward = rewards.getConfigurationSection(key);
            this.rewards.add(new Reward(
                    ItemBuilder.of(Material.valueOf(reward.getString("item")))
                            .name(reward.getString("name"))
                            .lore(reward.getStringList("lore")),
                    reward.getInt("slot"),
                    reward.getInt("tokens"),
                    reward.getStringList("commands"),
                    reward.getStringList("messages")
            ));
        }
    }

    @NotNull
    public List<Reward> getRewards() {
        return rewards;
    }

    public static class Reward {

        private final ItemStack item;
        private final int slot, tokens;
        private final List<String> commands, messages;

        public Reward(ItemBuilder item, int slot, int tokens, List<String> commands, List<String> messages) {
            this.item = item
                    .addLore(" ", "&6&lPURCHASE: &f" + tokens + " tokens")
                    .enchantUnsafe(Enchantment.MENDING, 1)
                    .addAllItemFlags()
                    .build();
            this.slot = slot;
            this.tokens = tokens;
            this.commands = commands;
            this.messages = messages;
        }

        @NotNull
        public ItemStack getItem() {
            return item;
        }

        public int getSlot() {
            return slot;
        }

        public int getTokens() {
            return tokens;
        }

        @NotNull
        public List<String> getCommands() {
            return commands;
        }

        @NotNull
        public List<String> getMessages() {
            return messages;
        }
    }
}
