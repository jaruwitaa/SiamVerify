package com.siamverify;

import org.bukkit.command.*;
import org.bukkit.entity.Player;
import java.net.http.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;

public class VerifyCommand implements CommandExecutor {
    private final String apiUrl;
    private final boolean blockBedrock;
    private final HttpClient http = HttpClient.newHttpClient();

    public VerifyCommand(String apiUrl, boolean blockBedrock) {
        this.apiUrl = apiUrl;
        this.blockBedrock = blockBedrock;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("§cUsage: /verify <code>");
            return true;
        }

        // Block Bedrock players if configured
        if (blockBedrock && player.getUniqueId().toString().startsWith("00000000-0000-0000-")) {
            player.sendMessage("§c❌ ผู้เล่น Bedrock ไม่สามารถเชื่อมบัญชีได้");
            return true;
        }

        String code = args[0].toUpperCase();
        String mcname = player.getName();
        String uuid = player.getUniqueId().toString().replace("-", "");

        String json = String.format(
            "{\"mcname\":\"%s\",\"uuid\":\"%s\",\"code\":\"%s\"}",
            mcname, uuid, code
        );

        player.sendMessage("§eกำลังตรวจสอบ...");

        http.sendAsync(
            HttpRequest.newBuilder()
                .uri(URI.create(apiUrl + "/api/verify"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build(),
            HttpResponse.BodyHandlers.ofString()
        ).thenAccept(response -> {
            if (response.statusCode() == 200) {
                player.sendMessage("§a✅ เชื่อมบัญชีสำเร็จ! ยินดีต้อนรับ " + mcname);
            } else if (response.statusCode() == 404) {
                player.sendMessage("§c❌ โค้ดไม่ถูกต้องหรือหมดอายุแล้ว");
            } else if (response.statusCode() == 409) {
                player.sendMessage("§c❌ บัญชี Minecraft นี้ถูกเชื่อมแล้ว");
            } else if (response.statusCode() == 410) {
                player.sendMessage("§c❌ โค้ดหมดอายุแล้ว กรุณาขอโค้ดใหม่");
            } else {
                player.sendMessage("§c❌ เกิดข้อผิดพลาด กรุณาลองใหม่");
            }
        }).exceptionally(err -> {
            player.sendMessage("§c❌ ไม่สามารถเชื่อมต่อกับ bot ได้");
            return null;
        });

        return true;
    }
}
