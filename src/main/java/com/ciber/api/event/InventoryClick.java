package com.ciber.api.event;

import com.ciber.api.CiberEvent;
import com.ciber.api.object.Gui;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.function.Consumer;

public class InventoryClick extends CiberEvent {

    @EventHandler
    public void inventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        if(Gui.inventoryList.contains(inventory)) {
            if (this == event.getClickedInventory().getHolder()) {
                Gui.accessibleSlotMap.forEach((slotInMap, acessibleInMap) -> {
                    if (slotInMap == event.getRawSlot()) {
                        if (!acessibleInMap) {
                            event.setCancelled(true);
                        }
                    }
                });

                Player player = (Player) event.getWhoClicked();
                Integer slot = event.getRawSlot();
                Consumer<Player> secoundAction = Gui.secoundActionMap.get(slot);
                secoundAction.accept(player);
            }
        }
    }
}
