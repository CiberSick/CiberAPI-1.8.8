package com.ciber.api;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public abstract class CiberEvent implements Listener {

    public void msg(Player player, String message) {
        player.sendMessage(message);
    }
}
