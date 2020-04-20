package com.ciber.api;

import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;

import java.util.HashMap;
import java.util.Map;

public abstract class CiberCommand extends BukkitCommand {

    public CiberCommand(String name) {
        super(name);
    }

    public abstract boolean execute(CommandSender sender, String label, String[] args);

    public void msg(CommandSender sender, String message) {
        sender.sendMessage(message);
    }

    public Map<String, Object> commandManager() {
        Map<String, Object> result = new HashMap<>();
        result.put("nome", getName());
        result.put("usage", getUsage());
        result.put("permission", getPermission());
        result.put("permission-message", getPermissionMessage());
        result.put("aliases", getAliases());
        return result;
    }
}
