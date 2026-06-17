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
 * ─ KeyMapping.Category.register() tidak ada → pakai String kategori langsung
 * ─ Mappings kembali ke obfuscated (Mojang official), jadi nama class/method
 *   sama di semua versi 1.20.x dan 1.21.x
 * ─ Java 21 (bukan 25)
 * ─ modImplementation (bukan implementation) karena Loom meremapnya
 */
public class QToggleClient implements ClientModInitializer {

    /** true = Q drop normal; false (default) = Q dikunci */
    public static boolean dropEnabled = false;

    private static KeyMapping toggleKey;

    // Nama kategori di Controls menu
    // Harus cocok dengan key di en_us.json: "key.category.qtoggle.main"
    private static final String CATEGORY = "key.category.qtoggle.main";

    @Override
    public void onInitializeClient() {

        // Di 1.20.x / 1.21.x: KeyBindingHelper.registerKeyBinding()
        // Konstruktor KeyMapping: (translationKey, type, keyCode, categoryTranslationKey)
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.qtoggle.toggle",          // translation key — cocok dengan lang/*.json
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_G,               // default: G
                CATEGORY                        // kategori di Options > Controls
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
                KeyMapping dropKey = options.keyDrop;
                dropKey.setDown(false);
                while (dropKey.consumeClick()) { /* buang antrian klik */ }
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
