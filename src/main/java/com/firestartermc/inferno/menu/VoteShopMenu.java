package com.firestartermc.inferno.menu;

import com.firestartermc.inferno.Inferno;
import com.firestartermc.inferno.entity.PlayerData;
import com.firestartermc.inferno.util.VoteShop;
import com.firestartermc.kerosene.gui.GuiDefaults;
import com.firestartermc.kerosene.gui.GuiPosition;
import com.firestartermc.kerosene.gui.PlayerGui;
import com.firestartermc.kerosene.gui.components.buttons.ButtonComponent;
import com.firestartermc.kerosene.gui.components.cosmetic.BorderAlternatingComponent;
import com.firestartermc.kerosene.gui.components.item.ItemComponent;
import com.firestartermc.kerosene.gui.predefined.ConfirmationGui;
import com.firestartermc.kerosene.item.SkullBuilder;
import com.firestartermc.kerosene.util.ConcurrentUtils;
import com.firestartermc.kerosene.util.Constants;
import com.firestartermc.kerosene.util.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class VoteShopMenu extends PlayerGui {

    private final Inferno inferno;
    private final VoteShop voteShop;
    private final PlayerData data;

    public VoteShopMenu(Inferno inferno, Player player) {
        super(player, "Vote Shop", 6);
        this.inferno = inferno;
        this.voteShop = inferno.getVoteShop();
        this.data = inferno.getCache().getData(player);

        if (data == null) {
            player.sendMessage(Constants.FAILED_TO_LOAD_DATA);
            return;
        }

        addElement(new BorderAlternatingComponent(Material.MAGENTA_STAINED_GLASS_PANE, Material.PINK_STAINED_GLASS_PANE));

        for (var reward : voteShop.getRewards()) {
            var slot = GuiPosition.fromSlot(reward.getSlot());
            addElement(new ButtonComponent(slot, reward.getItem(), event -> handleClick(reward)));
        }

        var skull = new SkullBuilder()
                .player(player)
                .name("&#fcdcfa&l" + data.getTokens() + " VOTE TOKENS")
                .lore("&f/vote for more tokens")
                .build();

        addElement(new ItemComponent(4, 5, skull));
        open();
    }

    private void handleClick(VoteShop.Reward reward) {
        if (data.getTokens() < reward.getTokens()) {
            GuiDefaults.playBadSelectSound(getPlayer());
        } else {
            ConfirmationGui.builder()
                    .details("Are you sure you want to purchase this item for " + reward.getTokens() + " tokens?")
                    .parent(this)
                    .item(reward.getItem())
                    .confirm(event -> {
                        if (data.getTokens() < reward.getTokens()) {
                            return;
                        }

                        data.setTokens(data.getTokens() - reward.getTokens());
                        ConcurrentUtils.ensureMain(() -> {
                            for (var command : reward.getCommands()) {
                                command = command.replace("[player]", getPlayer().getName());
                                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
                            }
                        });

                        reward.getMessages().forEach(message -> getPlayer().sendMessage(MessageUtils.formatColors(message, true)));
                        getPlayer().playSound(getPlayer().getLocation(), Sound.ENTITY_VILLAGER_YES, 1.0f, 1.0f);
                        event.getGui().close();
                    })
                    .cancel(event -> new VoteShopMenu(inferno, getPlayer()).open())
                    .open(getPlayer());
        }
    }
}
