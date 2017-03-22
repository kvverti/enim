package kvverti.enim.entity.animation;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityRabbit;
import net.minecraft.entity.monster.EntitySlime;

import kvverti.enim.entity.Entities;
import kvverti.enim.entity.EntityInfo;

/** Holds animation types for vanilla minecraft. */
public final class MinecraftAnimTypes {

	public static final AnimType IDLE =
		new AnimType(true, AnimPredicate.alwaysTrue()).setRegistryName("minecraft:idle");
	public static final AnimType MOVE =
		new AnimType(true, (e, i) -> i.speedSq > 0.0025f).setRegistryName("minecraft:moving");
	public static final AnimType AIR =
		new AnimType(true, (e, i) -> !e.isInWater() && !e.onGround).setRegistryName("minecraft:airborne");
	public static final AnimType SWIM =
		new AnimType(true, (e, i) -> e.isInWater() && !e.onGround).setRegistryName("minecraft:swimming");
	public static final AnimType TRACK =
		new AnimType(true, (e, i) -> Entities.hasAttackTarget(e)).setRegistryName("minecraft:tracking");
	public static final AnimType JUMP =
		new AnimType(false, AnimPredicate.alwaysFalse()).setRegistryName("minecraft:jump");

	static {

		JUMP.setCustomAnimPredicate(EntitySlime.class, (e, i) -> e.motionY > 0.20f);
		JUMP.setCustomAnimPredicate(EntityLivingBase.class, new AnimPredicate.ServerDependentPredicate<EntityLivingBase>() {

			@Override
			protected boolean computeMultiplayer(EntityLivingBase e, EntityInfo i) {

				return e.motionY > 0.42f || (e instanceof EntityRabbit && e.motionY > 0.10f);
			}

			@Override
			protected boolean computeSingleplayer(EntityLivingBase e, EntityInfo i) {

				return computeMultiplayer(e, i);
			}
		});
	}

	private MinecraftAnimTypes() { }
}