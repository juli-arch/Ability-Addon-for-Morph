package com.example.morphabilities;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraft.network.PacketBuffer;
import net.minecraft.entity.player.ServerPlayerEntity;

import java.util.function.Supplier;

public class Net {
    private static final String PROTO = "1";
    private static SimpleChannel CH;

    public static void register(){
        if (CH != null) return;
        CH = NetworkRegistry.newSimpleChannel(new ResourceLocation(MorphAbilitiesMod.MODID, "main"),
                ()->PROTO, PROTO::equals, PROTO::equals);
        CH.registerMessage(0, Trig.class, Trig::enc, Trig::dec, Trig::handle);
    }

    public static void send(AbilityKind k){
        CH.sendToServer(new Trig(k));
    }

    public static class Trig {
        public final AbilityKind kind;
        public Trig(AbilityKind k){ this.kind=k; }
        public static void enc(Trig t, PacketBuffer buf){ buf.writeInt(t.kind.ordinal()); }
        public static Trig dec(PacketBuffer buf){ return new Trig(AbilityKind.values()[buf.readInt()]); }
        public static void handle(Trig t, Supplier<NetworkEvent.Context> ctx){
            ctx.get().enqueueWork(()->{
                ServerPlayerEntity sp = ctx.get().getSender();
                if (sp!=null) ServerAbilityHandler.trigger(sp, t.kind);
            });
            ctx.get().setPacketHandled(true);
        }
    }
}