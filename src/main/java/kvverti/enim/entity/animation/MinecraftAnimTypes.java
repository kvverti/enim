package kvverti.enim.entity.animation;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntityRabbit;
import net.minecraft.entity.monster.EntitySlime;

import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;

import kvverti.enim.Ticker;
import kvverti.enim.entity.Entities;
import kvverti.enim.entity.EntityInfo;

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

	static {

		IDLE = new AnimType(true, AnimPredicate.alwaysTrue()).setRegistryName("minecraft:idle");
		MOVE = new AnimType(true, (e, i) -> i.speedSq > 0.0025f).setRegistryName("minecraft:moving");
		AIR = new AnimType(true, (e, i) -> !e.isInWater() && !e.onGround).setRegistryName("minecraft:airborne");
		AIR.setCustomAnimPredicate(EntityBat.class, (e, i) -> !e.getIsBatHanging());
		SWIM = new AnimType(true, (e, i) -> e.isInWater() && !e.onGround).setRegistryName("minecraft:swimming");
		TRACK = new AnimType(true, AnimPredicate.alwaysFalse()).setRegistryName("minecraft:tracking");
		TRACK.setCustomAnimPredicate(EntityLivingBase.class, new EventBasedPredicate<EntityLivingBase, LivingSetAttackTargetEvent>() {

			@Override
			protected boolean shouldAnimate(LivingSetAttackTargetEvent event) {

				return event.getTarget() != null;
			}
		}.create());
		JUMP = new AnimType(false, AnimPredicate.alwaysFalse()).setRegistryName("minecraft:jump");
		JUMP.setCustomAnimPredicate(EntitySlime.class, (e, i) -> e.motionY > 0.20f);
		JUMP.setCustomAnimPredicate(EntityLivingBase.class, new EventBasedPredicate<EntityLivingBase, LivingJumpEvent>() {

			@Override
			protected boolean multiplayerFallback(EntityLivingBase e, EntityInfo i) {

				return e.motionY > 0.42f || (e instanceof EntityRabbit && e.motionY > 0.10f);
			}
		}.create());
		ATTACK = new AnimType(false, AnimPredicate.alwaysFalse()).setRegistryName("minecraft:attack");
		DAMAGE = new AnimType(false, AnimPredicate.alwaysFalse()).setRegistryName("minecraft:damage");
	}

	private MinecraftAnimTypes() { }
}