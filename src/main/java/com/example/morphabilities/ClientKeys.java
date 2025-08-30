package com.example.morphabilities;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = MorphAbilitiesMod.MODID, value = Dist.CLIENT)
public class ClientKeys {
    public static KeyBinding KEY_PRIMARY;
    public static KeyBinding KEY_SECONDARY;

    public static void init(){
        KEY_PRIMARY = new KeyBinding("key.morphabilities.primary", KeyConflictContext.IN_GAME, GLFW.GLFW_KEY_R, "Morph Abilities");
        KEY_SECONDARY = new KeyBinding("key.morphabilities.secondary", KeyConflictContext.IN_GAME, GLFW.GLFW_KEY_X, "Morph Abilities");
        ClientRegistry.registerKeyBinding(KEY_PRIMARY);
        ClientRegistry.registerKeyBinding(KEY_SECONDARY);
    }

    @SubscribeEvent
    public static void onKey(InputEvent.KeyInputEvent e){
        if (KEY_PRIMARY.isDown() && KEY_PRIMARY.consumeClick()) Net.send(AbilityKind.PRIMARY);
        if (KEY_SECONDARY.isDown() && KEY_SECONDARY.consumeClick())  Net.send(AbilityKind.SECONDARY);
    }
}