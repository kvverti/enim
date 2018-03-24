package kvverti.enim.entity.animation;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntityRabbit;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.item.ItemBow;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityEnderChest;

import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;

import kvverti.enim.entity.Entities;
import kvverti.enim.entity.EntityInfo;
import kvverti.enim.entity.GEntity;

/** Holds animation types for vanilla minecraft. */
public final class MinecraftAnimTypes {

    /** An entity is always idle. Use this for animations that are always present. */
    public static final AnimType IDLE;

    /** AnimType active when an entity is moving (its speed is non-zero). */
    public static final AnimType MOVE;

    /** AnimType for when an entity is in the air or flying. */
    public static final AnimType AIR;

    /** AnimType for when an entity is underwater */
    public static final AnimType SWIM;

    /** AnimType for when an entity is tracking another. */
    public static final AnimType TRACK;

    /** AnimType for when an entity is jumping. */
    public static final AnimType JUMP;

    /** AnimType for when an entity attacks another. */
    public static final AnimType ATTACK;

    /** AnimType for when an entity is damaged. */
    public static final AnimType DAMAGE;

    /** AnimType for when a living entity is eating. Only used for entities that eat as part of their AI. */
    public static final AnimType EAT;

    /** AnimType for when a chest opens. */
    public static final AnimType OPEN;

    /** AnimType for when a chest closes. */
    public static final AnimType CLOSE;
    
    /** AnimType for when an entity holds a bow. */
    public static final AnimType BOWHOLD;

    static {

        IDLE = new AnimType(true, AnimPredicate.alwaysTrue()).setRegistryName("minecraft:idle");
        IDLE.addAnimPredicate(TileEntity.class, AnimPredicate.alwaysTrue());
        MOVE = new AnimType(true, (e, i) -> i.speedSq > 0.0025f).setRegistryName("minecraft:moving");
        AIR = new AnimType(true, (e, i) -> !e.isInWater() && !e.onGround).setRegistryName("minecraft:airborne");
        AIR.addAnimPredicate(EntityBat.class, (e, i) -> !e.getIsBatHanging());
        SWIM = new AnimType(true, (e, i) -> e.isInWater()).setRegistryName("minecraft:swimming");
        TRACK = new AnimType(true).setRegistryName("minecraft:tracking");
        TRACK.addAnimPredicate(EntityLivingBase.class, new EventBasedPredicate<EntityLivingBase, LivingSetAttackTargetEvent>(true) {

            @Override
            protected boolean shouldAnimate(LivingSetAttackTargetEvent event) {

                return event.getTarget() != null;
            }
        }.create());
        TRACK.addAnimPredicate(EntityZombie.class, (e, i) -> e.isArmsRaised());
        JUMP = new AnimType(false).setRegistryName("minecraft:jump");
        JUMP.addAnimPredicate(EntitySlime.class, (e, i) -> e.motionY > 0.20f);
        JUMP.addAnimPredicate(EntityLivingBase.class, new EventBasedPredicate<EntityLivingBase, LivingJumpEvent>(false) {

            @Override
            protected boolean multiplayerFallback(EntityLivingBase e, EntityInfo i) {

                return e.motionY > 0.42f || (e instanceof EntityRabbit && e.motionY > 0.10f);
            }
        }.create());
        ATTACK = new AnimType(false).setRegistryName("minecraft:attack");
        ATTACK.addAnimPredicate(EntityLivingBase.class, new EventBasedPredicate<EntityLivingBase, LivingAttackEvent>(false) {

            @Override
            protected Entity getAssociatedEntity(LivingAttackEvent event) {

                return event.getSource().getImmediateSource();
            }

            @Override
            protected boolean shouldAnimate(LivingAttackEvent event) {

                return getAssociatedEntity(event) instanceof EntityLivingBase;
            }
        }.create());
        DAMAGE = new AnimType(false).setRegistryName("minecraft:damage");
        EAT = new AnimType(false).setRegistryName("minecraft:eat");
        EAT.addAnimPredicate(EntitySheep.class, (e, i) -> e.getHeadRotationPointY(i.partialTicks) > 0.0f);
        OPEN = new AnimType(false).setRegistryName("minecraft:open");
        OPEN.addAnimPredicate(TileEntityChest.class, (e, i) -> e.lidAngle - e.prevLidAngle > 0.0f);
        OPEN.addAnimPredicate(TileEntityEnderChest.class, (e, i) -> e.lidAngle - e.prevLidAngle > 0.0f);
        CLOSE = new AnimType(false).setRegistryName("minecraft:close");
        CLOSE.addAnimPredicate(TileEntityChest.class, (e, i) -> e.lidAngle - e.prevLidAngle < 0.0f);
        CLOSE.addAnimPredicate(TileEntityEnderChest.class, (e, i) -> e.lidAngle - e.prevLidAngle < 0.0f);
        BOWHOLD = new AnimType(true).setRegistryName("minecraft:bowhold");
        BOWHOLD.addAnimPredicate(EntityLivingBase.class,
            (e, i) -> TRACK.shouldAnimate(new GEntity(e), i) && e.getHeldItemMainhand().getItem() instanceof ItemBow);
    }

    private MinecraftAnimTypes() { }
}