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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

/**
 * QToggle — Fabric mod, SATU jar dipakai untuk SELURUH versi dalam satu grup:
 *   - jar "1.20.x" → jalan di 1.20.1, 1.20.2, 1.20.4, 1.20.6
 *   - jar "1.21.x" → jalan di 1.21, 1.21.1, 1.21.4, 1.21.5, 1.21.6, 1.21.7, 1.21.8, 1.21.9
 *
 * KENAPA PAKAI REFLECTION DI SINI:
 * Mojang mengganti signature constructor KeyMapping di sekitar 1.21.9:
 *   - 1.21 s.d. 1.21.8 : KeyMapping(String, InputConstants.Type, int, String)
 *   - 1.21.9+          : KeyMapping(String, InputConstants.Type, int, KeyMapping.Category)
 *
 * Jar ini dicompile SEKALI (terhadap versi tertua di grupnya: 1.20.1 atau 1.21)
 * tapi harus tetap jalan di semua versi lain dalam grup itu. Karena itu kita
 * tidak boleh hardcode constructor mana yang dipakai di compile time — dua
 * bentuk constructor dicoba lewat reflection saat mod diinisialisasi, dan
 * yang cocok dengan API runtime yang sedang berjalan otomatis dipakai.
 *
 * Field Options.keyDrop juga diakses lewat reflection sebagai jaga-jaga kalau
 * suatu versi MC mengganti nama field itu — kalau tidak ketemu, fitur Q-lock
 * untuk versi tersebut otomatis nonaktif (mod tetap load, tidak crash).
 *
 * CATATAN JUJUR: pendekatan ini menutup celah yang SUDAH terbukti berubah
 * (KeyMapping.Category). Tidak ada jaminan 100% tidak ada API lain yang juga
 * berubah di versi-versi tersebut — disarankan tetap dites manual di ujung
 * tertua dan ujung terbaru tiap grup sebelum dirilis.
 */
public class QToggleClient implements ClientModInitializer {

    /** true = Q drop normal; false (default) = Q dikunci */
    public static boolean dropEnabled = false;

    private static KeyMapping toggleKey;

    @Override
    public void onInitializeClient() {
        toggleKey = KeyBindingHelper.registerKeyBinding(createToggleKeyMapping());

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            // Cek apakah tombol toggle ditekan
            while (toggleKey.consumeClick()) {
                dropEnabled = !dropEnabled;
                showStatus(client, dropEnabled);
            }

            // Jika mode DROP LOCKED: batalkan semua input Q
            if (!dropEnabled) {
                KeyMapping dropKey = getDropKeyMapping(client.options);
                if (dropKey != null) {
                    dropKey.setDown(false);
                    while (dropKey.consumeClick()) { }
                }
            }
        });
    }

    /**
     * Membuat KeyMapping untuk toggle dengan mencoba dua bentuk constructor
     * yang dipakai Mojang di rentang versi berbeda. Bentuk lama (String
     * category) dicoba duluan karena dipakai di rentang versi yang lebih luas
     * (1.20.x - 1.21.8); bentuk baru (Category enum, 1.21.9+) jadi fallback.
     */
    private static KeyMapping createToggleKeyMapping() {
        final String translationKey = "key.qtoggle.toggle";
        final String categoryName = "key.category.qtoggle.main";

        // Bentuk lama: KeyMapping(String, InputConstants.Type, int, String)
        try {
            Constructor<KeyMapping> ctor = KeyMapping.class.getConstructor(
                    String.class, InputConstants.Type.class, int.class, String.class);
            return ctor.newInstance(translationKey, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, categoryName);
        } catch (NoSuchMethodException ignored) {
            // tidak ada di versi ini, lanjut coba bentuk baru
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("[QToggle] Gagal membuat KeyMapping lewat constructor lama (String category)", e);
        }

        // Bentuk baru (1.21.9+): KeyMapping(String, InputConstants.Type, int, KeyMapping.Category)
        try {
            for (Class<?> inner : KeyMapping.class.getDeclaredClasses()) {
                if (inner.isEnum() && inner.getSimpleName().equals("Category")) {
                    Object categoryValue = null;
                    for (Object constant : inner.getEnumConstants()) {
                        if (((Enum<?>) constant).name().equals("MISC")) {
                            categoryValue = constant;
                            break;
                        }
                    }
                    if (categoryValue == null && inner.getEnumConstants().length > 0) {
                        categoryValue = inner.getEnumConstants()[0];
                    }
                    Constructor<KeyMapping> ctor = KeyMapping.class.getConstructor(
                            String.class, InputConstants.Type.class, int.class, inner);
                    return ctor.newInstance(translationKey, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, categoryValue);
                }
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("[QToggle] Gagal membuat KeyMapping lewat constructor baru (Category)", e);
        }

        throw new IllegalStateException("[QToggle] Tidak menemukan constructor KeyMapping yang cocok di versi Minecraft ini");
    }

    /**
     * Mengambil field Options.keyDrop lewat reflection. Kalau nama field
     * berubah di versi tertentu, return null supaya mod tidak crash — fitur
     * Q-lock untuk versi itu otomatis nonaktif sampai diperbaiki manual.
     */
    private static KeyMapping getDropKeyMapping(Options options) {
        try {
            Field field = Options.class.getField("keyDrop");
            Object value = field.get(options);
            return value instanceof KeyMapping ? (KeyMapping) value : null;
        } catch (ReflectiveOperationException e) {
            return null;
        }
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
