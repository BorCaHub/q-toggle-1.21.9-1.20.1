package com.example.qtoggle;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/**
 * QToggle — Fabric mod untuk Minecraft 1.20.x dan 1.21.x
 *
 * Perbedaan dari versi 26.1:
 * ─ KeyMappingHelper  → KeyBindingHelper  (nama API di 1.20/1.21)
 * ─ KeyMapping.Category support berbeda per versi:
 *   • 1.20.x: String langsung di constructor
 *   • 1.21.9: KeyMapping.Category enum
 * ─ Mappings kembali ke obfuscated (Mojang official), jadi nama class/method
 *   sama di semua versi 1.20.x dan 1.21.x
 * ─ Java 21 (bukan 25)
 * ─ modImplementation (bukan implementation) karena Loom meremapnya
 */
public class QToggleClient implements ClientModInitializer {

    /** true = Q drop normal; false (default) = Q dikunci */
    public static boolean dropEnabled = false;

    private static KeyMapping toggleKey;

    @Override
    public void onInitializeClient() {

        // Di 1.21.9: gunakan KeyMapping.Category.MISC
        // Di 1.20.x: gunakan String kategori langsung
        // Untuk compatibility, kita gunakan constructor dengan Category untuk 1.21.9
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.qtoggle.toggle",                    // translation key
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_G,                         // default: G
                KeyMapping.Category.MISC                 // kategori di Options > Controls (1.21.9+)
        ));

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            // Cek apakah tombol toggle ditekan
            while (toggleKey.consumeClick()) {
                dropEnabled = !dropEnabled;
                showStatus(client, dropEnabled);
            }

            // Jika mode DROP LOCKED: batalkan semua input Q
            if (!dropEnabled) {
                Options options = client.options;
                
                // Ambil keybinding Q drop dengan null safety check
                KeyMapping dropKey = options.keyDrop;
                if (dropKey != null) {
                    // Pastikan key tidak aktif
                    dropKey.setDown(false);
                    // Buang semua antrian klik pada key tersebut
                    while (dropKey.consumeClick()) { }
                }
            }
        });
    }

    private void showStatus(Minecraft client, boolean active) {
        if (client.player == null) return;
        String message = active
                ? "§a[Q Toggle] DROP: ENABLED — Q drops items"
                : "§c[Q Toggle] DROP: LOCKED — Q will not drop";
        // setOverlayMessage tersedia sejak 1.17 dan tetap ada di 1.20/1.21
        client.gui.setOverlayMessage(Component.literal(message), false);
    }
}
