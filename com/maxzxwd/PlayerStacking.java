package com.maxzxwd;

import org.bukkit.plugin.java.JavaPlugin;

public class PlayerStacking extends JavaPlugin {
    @Override
    public void onEnable() {
        Utils.init();
        getServer().getPluginManager().registerEvents(new Listeners(), this);
    }
}
