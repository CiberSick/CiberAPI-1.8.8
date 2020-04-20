package com.ciber.api.object;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class Gui implements InventoryHolder {

    private String title;
    private int lines;

    public static List<Inventory> inventoryList = new ArrayList<>();
    public static Map<Integer, Consumer<Player>> actionMap = new HashMap<>();
    public static Map<Integer, Consumer<Player>> secoundActionMap = new HashMap<>();
    public static Map<Integer, Boolean> accessibleSlotMap = new HashMap<>();
    public static Map<Integer, ItemStack> itemMap = new HashMap<>();

    public void open(Player player) {
        if(lines > 5) setLines(5);
        Inventory gui = Bukkit.createInventory(player, lines * 9, title);
        itemMap.forEach((slot, item) -> {
            gui.setItem(slot, item);
        });
        inventoryList.add(gui);
    }

    public void add(Integer slot, ItemStack item, Consumer<Player> action) {
        itemMap.put(slot, item);
        accessibleSlotMap.put(slot, false);
        secoundActionMap.put(slot, action);
    }

    public void add(Integer slot, Boolean acessible, ItemStack item, Consumer<Player> action) {
        itemMap.put(slot, item);
        accessibleSlotMap.put(slot, acessible);
        secoundActionMap.put(slot, action);
    }

    @Override
    public Inventory getInventory() {
        return null;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getLines() {
        return lines;
    }

    public void setLines(int lines) {
        this.lines = lines;
    }
}
