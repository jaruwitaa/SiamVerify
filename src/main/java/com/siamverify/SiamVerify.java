package com.siamverify;

import org.bukkit.plugin.java.JavaPlugin;

public class SiamVerify extends JavaPlugin {
    private String apiUrl;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        apiUrl = getConfig().getString("api-url", "http://localhost:50004");
        boolean blockBedrock = getConfig().getBoolean("block-bedrock", true);
        getCommand("verify").setExecutor(new VerifyCommand(apiUrl, blockBedrock));
        getLogger().info("SiamVerify enabled!");
    }
}
