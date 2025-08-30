package com.example.morphabilities;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.*;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.Explosion;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

public class ServerAbilityHandler {
    private static final String CD = "morphabilities_cd";
    private static final String PRIME = "morphabilities_prime";
    private static final String LASER = "morphabilities_laser";
    private static final String LASER_TARGET = "morphabilities_laser_target";

    private static final String CREEPER = "minecraft:creeper";
    private static final String ENDERMAN = "minecraft:enderman";
    private static final String GHAST = "minecraft:ghast";
    private static final String BLAZE = "minecraft:blaze";
    private static final String WITHER = "minecraft:wither";
    private static final String ENDER_DRAGON = "minecraft:ender_dragon";
    private static final String ELDER_GUARDIAN = "minecraft:elder_guardian";
    private static final String SKELETON = "minecraft:skeleton";
    private static final String ZOMBIE = "minecraft:zombie";
    private static final String SNOW_GOLEM = "minecraft:snow_golem";

    public static void trigger(ServerPlayerEntity p, AbilityKind kind){
        if (p.level.isClientSide) return;
        String id = currentMorphId(p);
        if (id == null) return;
        int cd = p.getPersistentData().getInt(CD);
        if (cd > 0) return;

        switch (kind){
            case PRIMARY: primary(p, id); break;
            case SECONDARY: secondary(p, id); break;
        }
    }

    private static void primary(ServerPlayerEntity p, String id){
        if (id.equals(CREEPER)){
            p.getPersistentData().putInt(PRIME, 100); // 5s
            p.level.playSound(null, p.blockPosition(), SoundEvents.CREEPER_PRIMED, SoundCategory.PLAYERS, 1f, 1f);
        } else if (id.equals(ENDERMAN)){
            enderBlink(p); setCd(p, 40);
        } else if (id.equals(GHAST)){
            ghastFireball(p); setCd(p, 40);
        } else if (id.equals(BLAZE)){
            blazeFireballs(p); setCd(p, 20);
        } else if (id.equals(WITHER)){
            witherSkull(p); setCd(p, 30);
        } else if (id.equals(ENDER_DRAGON)){
            dragonFireball(p); setCd(p, 50);
        } else if (id.equals(ELDER_GUARDIAN)){
            elderLaserStart(p); setCd(p, 60);
        } else if (id.equals(SKELETON)){
            skeletonBarrage(p); setCd(p, 30);
        } else if (id.equals(ZOMBIE)){
            zombieSlam(p); setCd(p, 40);
        } else if (id.equals(SNOW_GOLEM)){
            snowGolemThrow(p); setCd(p, 20);
        }
    }
    private static void secondary(ServerPlayerEntity p, String id){}

    private static void setCd(ServerPlayerEntity p, int t){ p.getPersistentData().putInt(CD, t); }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent e){
        if (!(e.player instanceof ServerPlayerEntity)) return;
        if (e.phase != TickEvent.Phase.END) return;
        ServerPlayerEntity p = (ServerPlayerEntity)e.player;
        World lvl = p.level;

        int cd = p.getPersistentData().getInt(CD);
        if (cd > 0) p.getPersistentData().putInt(CD, cd-1);

        int prime = p.getPersistentData().getInt(PRIME);
        if (prime > 0){
            List<LivingEntity> list = lvl.getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(7.0), e2-> e2 != p);
            for (LivingEntity e2 : list){
                Vector3d dir = new Vector3d(p.getX()-e2.getX(), p.getY()-e2.getY(), p.getZ()-e2.getZ()).normalize().scale(0.25);
                e2.push(dir.x, Math.max(dir.y*0.2, 0.05), dir.z);
            }
            double r = 1.5 + (100 - prime) * 0.01;
            for (int i=0;i<20;i++){
                double ang = (i/20.0)*Math.PI*2.0;
                lvl.sendParticles(ParticleTypes.END_ROD, p.getX()+r*Math.cos(ang), p.getY()+1.0, p.getZ()+r*Math.sin(ang), 1, 0.0, 0.02, 0.0, 0.0);
            }
            p.getPersistentData().putInt(PRIME, prime-1);
            if (prime-1 <= 0){
                lvl.explode(p, p.getX(), p.getY(), p.getZ(), 3.5f, Explosion.Mode.NONE);
                setCd(p, 60);
            }
        }

        int laser = p.getPersistentData().getInt(LASER);
        if (laser > 0){
            LivingEntity target = null;
            try {
                String uuidStr = p.getPersistentData().getString(LASER_TARGET);
                if (!uuidStr.isEmpty()){
                    UUID u = UUID.fromString(uuidStr);
                    net.minecraft.entity.Entity e2 = lvl.getEntity(u);
                    if (e2 instanceof LivingEntity) target = (LivingEntity)e2;
                }
            } catch(Exception ignored){}
            if (target != null){
                Vector3d from = p.position().add(0, p.getEyeHeight(), 0);
                Vector3d to = target.position().add(0, target.getEyeHeight()*0.6, 0);
                Vector3d delta = to.subtract(from);
                int steps = 20;
                for (int i=0;i<=steps;i++){
                    Vector3d pos = from.add(delta.scale(i/(double)steps));
                    lvl.sendParticles(ParticleTypes.BUBBLE, pos.x, pos.y, pos.z, 1, 0.01,0.01,0.01, 0.0);
                    lvl.sendParticles(ParticleTypes.END_ROD, pos.x, pos.y, pos.z, 0, 0.0,0.0,0.0, 0.0);
                }
                if (laser == 1){
                    target.hurt(DamageSource.MAGIC, 10.0f);
                    lvl.playSound(null, target.blockPosition(), SoundEvents.ELDER_GUARDIAN_CURSE, SoundCategory.PLAYERS, 1f, 1f);
                }
            }
            p.getPersistentData().putInt(LASER, laser-1);
        }
    }

    // abilities
    private static void enderBlink(ServerPlayerEntity p){
        World lvl = p.level;
        double yaw = Math.toRadians(p.yHeadRot);
        double dx = -Math.sin(yaw)*6.0;
        double dz =  Math.cos(yaw)*6.0;
        p.teleportTo(p.getX()+dx, p.getY(), p.getZ()+dz);
        lvl.playSound(null, p.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1f, 1f);
        lvl.sendParticles(ParticleTypes.PORTAL, p.getX(), p.getY()+1.0, p.getZ(), 30, 0.5,0.5,0.5, 0.1);
    }
    private static void ghastFireball(ServerPlayerEntity p){
        World lvl = p.level;
        Vector3d look = p.getLookAngle();
        FireballEntity f = new FireballEntity(lvl, p, look.x, look.y, look.z, 1);
        f.setPos(p.getX(), p.getY()+p.getEyeHeight(), p.getZ());
        lvl.addFreshEntity(f);
        lvl.playSound(null, p.blockPosition(), SoundEvents.GHAST_SHOOT, SoundCategory.PLAYERS, 1f, 1f);
    }
    private static void blazeFireballs(ServerPlayerEntity p){
        World lvl = p.level;
        Vector3d look = p.getLookAngle();
        for (int i=0;i<3;i++){
            SmallFireballEntity f = new SmallFireballEntity(lvl, p, look.x, look.y, look.z);
            f.setPos(p.getX(), p.getY()+p.getEyeHeight(), p.getZ());
            lvl.addFreshEntity(f);
        }
        lvl.playSound(null, p.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundCategory.PLAYERS, 1f, 1f);
    }
    private static void witherSkull(ServerPlayerEntity p){
        World lvl = p.level;
        Vector3d look = p.getLookAngle();
        WitherSkullEntity skull = new WitherSkullEntity(lvl, p, look.x, look.y, look.z);
        skull.setPos(p.getX(), p.getY()+p.getEyeHeight(), p.getZ());
        lvl.addFreshEntity(skull);
        lvl.playSound(null, p.blockPosition(), SoundEvents.WITHER_SHOOT, SoundCategory.PLAYERS, 1f, 1f);
    }
    private static void dragonFireball(ServerPlayerEntity p){
        World lvl = p.level;
        Vector3d look = p.getLookAngle();
        DragonFireballEntity ball = new DragonFireballEntity(lvl, p, look.x, look.y, look.z);
        ball.setPos(p.getX(), p.getY()+p.getEyeHeight(), p.getZ());
        lvl.addFreshEntity(ball);
        lvl.playSound(null, p.blockPosition(), SoundEvents.ENDER_DRAGON_SHOOT, SoundCategory.PLAYERS, 1f, 1f);
    }
    private static void skeletonBarrage(ServerPlayerEntity p){
        World lvl = p.level;
        double[][] dirs = {{0.0,0.1,1.2},{0.8,0.1,0.9},{-0.8,0.1,0.9},{1.2,0.1,0.0},{-1.2,0.1,0.0},{0.8,0.1,-0.9},{-0.8,0.1,-0.9},{0.0,0.1,-1.2}};
        for (double[] m : dirs){
            ArrowEntity a = new ArrowEntity(lvl, p);
            a.setDeltaMovement(m[0], m[1], m[2]);
            a.setOwner(p);
            a.setPos(p.getX(), p.getY()+p.getEyeHeight(), p.getZ());
            lvl.addFreshEntity(a);
        }
    }
    private static void zombieSlam(ServerPlayerEntity p){
        World lvl = p.level;
        lvl.playSound(null, p.blockPosition(), SoundEvents.ZOMBIE_ATTACK_IRON_DOOR, SoundCategory.PLAYERS, 1f, 1f);
        lvl.sendParticles(ParticleTypes.CLOUD, p.getX(), p.getY() + 1.0, p.getZ(), 10, 0.8, 0.1, 0.8, 0.1);
        List<LivingEntity> list = lvl.getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(7.0), e -> e != p);
        for (LivingEntity e : list){
            e.hurt(DamageSource.playerAttack(p), 4.0f);
            e.push(e.getX()-p.getX(), 0.2, e.getZ()-p.getZ());
        }
    }
    private static void snowGolemThrow(ServerPlayerEntity p){
        World lvl = p.level;
        SnowballEntity s = new SnowballEntity(lvl, p);
        Vector3d look = p.getLookAngle().scale(1.5);
        s.setDeltaMovement(look);
        s.setPos(p.getX(), p.getY()+p.getEyeHeight(), p.getZ());
        lvl.addFreshEntity(s);
        lvl.playSound(null, p.blockPosition(), SoundEvents.SNOWBALL_THROW, SoundCategory.PLAYERS, 1f, 1f);
    }
    private static void elderLaserStart(ServerPlayerEntity p){
        LivingEntity target = rayPickLiving(p, 20.0);
        if (target == null) target = nearestHostile(p, 12.0);
        if (target == null) return;
        p.getPersistentData().putInt(LASER, 40); // ~2s charge
        p.getPersistentData().putString(LASER_TARGET, target.getUUID().toString());
        p.level.playSound(null, p.blockPosition(), SoundEvents.ELDER_GUARDIAN_AMBIENT, SoundCategory.PLAYERS, 0.8f, 1f);
    }
    private static LivingEntity nearestHostile(ServerPlayerEntity p, double r){
        World lvl = p.level;
        List<LivingEntity> list = lvl.getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(r), e-> isHostile(e) && e != p);
        LivingEntity best=null; double bd = Double.MAX_VALUE;
        for (LivingEntity e: list){
            double d = e.distanceToSqr(p);
            if (d<bd){ bd=d; best=e; }
        }
        return best;
    }
    private static boolean isHostile(LivingEntity e){
        return e.getType().getCategory() == net.minecraft.entity.EntityClassification.MONSTER && !(e instanceof PlayerEntity);
    }
    private static LivingEntity rayPickLiving(ServerPlayerEntity p, double range){
        World lvl = p.level;
        Vector3d eye = new Vector3d(p.getX(), p.getY()+p.getEyeHeight(), p.getZ());
        Vector3d look = p.getLookAngle();
        Vector3d reach = eye.add(look.scale(range));
        AxisAlignedBB bb = p.getBoundingBox().expandTowards(look.scale(range)).inflate(1.0,1.0,1.0);
        LivingEntity best = null;
        double bestDist = range*range;
        for (LivingEntity e : lvl.getEntitiesOfClass(LivingEntity.class, bb, x->x!=p)){
            AxisAlignedBB ebb = e.getBoundingBox().inflate(0.3);
            if (ebb.clip(eye, reach).isPresent()){
                double d = e.distanceToSqr(p);
                if (d < bestDist){ bestDist = d; best = e; }
            }
        }
        return best;
    }

    // iChun Morph: try reflection to get current morph id
    private static String currentMorphId(PlayerEntity player){
        try {
            Class<?> morphHandler = Class.forName("morph.common.core.MorphHandler");
            java.lang.reflect.Method inst = morphHandler.getMethod("instance");
            Object handler = inst.invoke(null);
            java.lang.reflect.Method get = morphHandler.getMethod("getMorphEntityId", PlayerEntity.class);
            Object val = get.invoke(handler, player);
            if (val != null) return val.toString();
        } catch (Throwable ignored){}
        try {
            Class<?> api = Class.forName("morph.common.core.Api");
            java.lang.reflect.Method m = api.getMethod("getCurrentMorphId", PlayerEntity.class);
            Object id = m.invoke(null, player);
            if (id != null) return id.toString();
        } catch (Throwable ignored){}
        return null;
    }
}