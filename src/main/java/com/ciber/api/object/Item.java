package com.ciber.api.object;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Item {

    private ItemStack item;
    public static List<ItemStack> itemList = new ArrayList<>();

    public Item(String name, Material material, String displayName,
                short itemType, Map<Enchantment, Integer> enchantments, String... lores) {

        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(displayName);
        item.setDurability(itemType);
        enchantments.forEach(item::addUnsafeEnchantment);
        meta.setLore(Arrays.asList(lores));
        setItem(item);
        itemList.add(item);
    }

    public ItemStack getItem() {
        return item;
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }
}
