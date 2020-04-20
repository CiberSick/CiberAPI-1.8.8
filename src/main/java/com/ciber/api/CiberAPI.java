package com.ciber.api;

public class CiberAPI extends CiberPlugin {


    @Override
    public void onEnable() {
        registerEvents("event");
        msg("§3[CiberAPI] §aApi habilitada.");
    }

    @Override
    public void onDisable() {
        msg("§3[CiberAPI] §cApi desabilitada.");
    }
}
