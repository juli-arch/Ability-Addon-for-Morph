package com.example.morphabilities;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.common.MinecraftForge;

@Mod(MorphAbilitiesMod.MODID)
public class MorphAbilitiesMod {
    public static final String MODID = "morphabilities";
    public MorphAbilitiesMod() {
        MinecraftForge.EVENT_BUS.register(new ServerAbilityHandler());
        Net.register();
    }
    @SubscribeEvent public void onClientSetup(final FMLClientSetupEvent e){ ClientKeys.init(); }
}