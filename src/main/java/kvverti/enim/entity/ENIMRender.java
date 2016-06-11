package kvverti.enim.entity;

import java.lang.reflect.Method;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;

import kvverti.enim.modelsystem.ModelElement;
import kvverti.enim.modelsystem.EntityState;
import kvverti.enim.modelsystem.Keys;
import kvverti.enim.Logger;
import kvverti.enim.Util;

public abstract class ENIMRender<T extends Entity> extends Render<T> implements ReloadableRender {

//	protected final RenderManager renderManager;

	private static final Method renderLeash;
	private static final RenderLiving<EntityLiving> proxy =
		new RenderLiving<EntityLiving>(Minecraft.getMinecraft().getRenderManager(), new ENIMModel(), 1.0f) {

			@Override
			public ResourceLocation getEntityTexture(EntityLiving entity) {

				return Util.MISSING_LOCATION;
			}
		};

	static {

		renderLeash = Util.findMethod(RenderLiving.class,
			void.class,
			new String[] { "func_110827_b", "renderLeash" },
			EntityLiving.class,
			double.class,
			double.class,
			double.class,
			float.class,
			float.class);
	}

	private final Map<String, EntityState> states;
	private final ResourceLocation entityStateFile;

	protected ENIMRender(RenderManager manager, String modDomain, String entityStateFile, String... stateNames) {

		super(manager);
		this.entityStateFile = new ResourceLocation(modDomain, Keys.STATES_DIR + entityStateFile + Keys.JSON);
		states = new HashMap<>();
		for(String s : stateNames) { states.put(s, new EntityState(s)); }
		ReloadableRender.renders.add(this);
	}

	public abstract EntityState getStateFromEntity(T entity);

	protected EntityState getState(String name) {

		return states.get(name);
	}

	@Override
	public final ResourceLocation getEntityStateFile() {

		return entityStateFile;
	}

	@Override
	public final Set<String> getEntityStateNames() {

		return new HashSet<>(states.keySet());
	}

	@Override
	public final void doRender(T entity, double x, double y, double z, float yaw, float partialTicks) {

		if(shouldRender(entity)) {

			final float VIEW_LOCK = 60.0f;
			GlStateManager.pushMatrix();
			GlStateManager.translate((float) x, (float) y, (float) z);
			GlStateManager.rotate(180.0f, 1.0f, 0.0f, 0.0f);
			float diff = headYaw(entity, yaw);
			if     (diff >  VIEW_LOCK) entity.rotationYaw = yaw += diff - VIEW_LOCK;
			else if(diff < -VIEW_LOCK) entity.rotationYaw = yaw += diff + VIEW_LOCK;
			GlStateManager.rotate(yaw, 0.0f, 1.0f, 0.0f);

			EntityState state = getStateFromEntity(entity);
			ENIMModel model = state.model();
			bindEntityTexture(entity);
			GlStateManager.rotate(state.rotation(), 0.0f, 1.0f, 0.0f);
			preRender(entity, state, yaw);
			model.render(entity,
				speed(entity),
				yaw,
				entity.ticksExisted + partialTicks,
				headYaw(entity, yaw),
				entity.rotationPitch,
				0.0625f * state.scale());
			postRender(entity);
			GlStateManager.popMatrix();
		}
		super.doRender(entity, x, y, z, yaw, partialTicks);
		if(entity instanceof EntityLiving)
			Util.invokeUnchecked(proxy, renderLeash, entity, x, y, z, yaw, partialTicks);
	}

	private float speed(Entity entity) {

		double dx = entity.posX - entity.lastTickPosX;
		double dz = entity.posZ - entity.lastTickPosZ;
		return (float) Math.sqrt(dx * dx + dz * dz);
	}

	private float headYaw(Entity entity, float bodyYaw) {

		return entity instanceof EntityLivingBase ? ((EntityLivingBase) entity).rotationYawHead - bodyYaw : 0.0f;
	}

	public boolean shouldRender(T entity) { return true; }

	public void preRender(T entity, EntityState state, float yaw) { }

	public void postRender(T entity) { }

	@Override
	protected final ResourceLocation getEntityTexture(T entity) {

		return getStateFromEntity(entity).texture();
	}

	@Override
	public final void reloadRender(EntityState state) {

		if(getEntityStateNames().contains(state.name())) {

			EntityState realState = states.get(state.name());
			realState.reloadState(state);
			realState.model().textureWidth = state.xSize();
			realState.model().textureHeight = state.ySize();
		}
	}

	@Override
	public final void setMissingno() {

		states.values().forEach(state -> state.model().setMissingno());
	}
}