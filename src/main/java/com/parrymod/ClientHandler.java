package com.parrymod;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

/** Client-only: key binding (default F, rebindable in Options > Controls). */
public class ClientHandler {
    public static final KeyMapping PARRY_KEY = new KeyMapping(
            "key.parry.parry",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F,
            "key.categories.parry");

    private static final long FLASH_MS = 250L;
    private static long flashStart = -FLASH_MS;

    public static void startFlash() {
        flashStart = System.currentTimeMillis();
    }

    @Mod.EventBusSubscriber(modid = ParryMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ModEvents {
        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event) {
            event.register(PARRY_KEY);
        }
    }

    @Mod.EventBusSubscriber(modid = ParryMod.MODID, value = Dist.CLIENT)
    public static class ForgeEvents {
        @SubscribeEvent
        public static void onRenderGui(RenderGuiEvent.Post event) {
            long elapsed = System.currentTimeMillis() - flashStart;
            if (elapsed < 0 || elapsed >= FLASH_MS) return;
            float fade = 1.0F - (float) elapsed / FLASH_MS;
            int alpha = (int) (0.55F * fade * 255.0F);
            int color = (alpha << 24) | 0xFFFFFF;
            event.getGuiGraphics().fill(0, 0,
                    event.getWindow().getGuiScaledWidth(), event.getWindow().getGuiScaledHeight(), color);
        }

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            // START phase runs before vanilla handles keybinds, so we can steal the F press
            if (event.phase != TickEvent.Phase.START) return;
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;

            while (PARRY_KEY.consumeClick()) {
                if (mc.screen != null) continue;
                Network.CHANNEL.sendToServer(new ParryPacket());
                // Vanilla "swap hands" is also F by default: don't let it fire at the same time.
                if (PARRY_KEY.getKey().equals(mc.options.keySwapOffhand.getKey())) {
                    while (mc.options.keySwapOffhand.consumeClick()) { }
                }
            }
        }
    }
}
