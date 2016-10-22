package kvverti.enim.entity;

import java.lang.reflect.Method;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import net.minecraft.block.properties.IProperty;
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

import com.google.common.collect.ImmutableSet;

import kvverti.enim.entity.state.RenderState;
import kvverti.enim.entity.state.StateManager;
import kvverti.enim.model.EntityState;
import kvverti.enim.model.EntityStateMap;
import kvverti.enim.Keys;
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

	private final ResourceLocation entityStateFile;
	private final StateManager stateManager;
	private EntityState currentState;

	protected ENIMRender(RenderManager manager, String modDomain, String entityStateFile, IProperty<?>... properties) {

		super(manager);
		this.entityStateFile = new ResourceLocation(modDomain, Keys.STATES_DIR + entityStateFile + Keys.JSON);
		this.stateManager = new StateManager(properties);
	}

	public abstract RenderState getStateFromEntity(T entity);

	protected final StateManager getStateManager() {

		return stateManager;
	}

	protected final EntityState getCurrentEntityState() {

		return currentState;
	}

	@Override
	public final ResourceLocation getEntityStateFile() {

		return entityStateFile;
	}

	@Override
	public final ImmutableSet<String> getEntityStateNames() {

		return stateManager.stateStringNames();
	}

	@Override
	public final void doRender(T entity, double x, double y, double z, float yaw, float partialTicks) {

		if(shouldRender(entity)) {

			final float VIEW_LOCK = 60.0f;
			GlStateManager.pushMatrix();
			GlStateManager.disableCull();
			GlStateManager.translate((float) x, (float) y, (float) z);
			GlStateManager.rotate(180.0f, 1.0f, 0.0f, 0.0f);
			float diff = headYaw(entity, yaw);
			if     (diff >  VIEW_LOCK) entity.rotationYaw = yaw += diff - VIEW_LOCK;
			else if(diff < -VIEW_LOCK) entity.rotationYaw = yaw += diff + VIEW_LOCK;
			GlStateManager.rotate(yaw, 0.0f, 1.0f, 0.0f);

			RenderState renderState = getStateFromEntity(entity);
			currentState = stateManager.getState(renderState);
			ENIMModel model = stateManager.getModel(renderState);
			bindEntityTexture(entity);
			GlStateManager.rotate(currentState.y(), 0.0f, 1.0f, 0.0f);
			EntityInfo info = new EntityInfo();
			info.speedSq = speedSq(entity);
			info.partialTicks = partialTicks;
			info.entityYaw = yaw;
			info.headYaw = headYaw(entity, yaw);
			info.entityPitch = entity.rotationPitch;
			info.scale = 0.0625f * currentState.scale();
			preRender(entity, info);
			model.render(entity, info);
			postRender(entity, info);
			GlStateManager.popMatrix();
		}
		super.doRender(entity, x, y, z, yaw, partialTicks);
		if(entity instanceof EntityLiving)
			Util.invokeUnchecked(proxy, renderLeash, entity, x, y, z, yaw, partialTicks);
	}

	private float speedSq(Entity entity) {

		double dx = entity.posX - entity.lastTickPosX;
		double dz = entity.posZ - entity.lastTickPosZ;
		return (float) (dx * dx + dz * dz);
	}

	private float headYaw(Entity entity, float bodyYaw) {

		return entity instanceof EntityLivingBase ? ((EntityLivingBase) entity).rotationYawHead - bodyYaw : 0.0f;
	}

	public boolean shouldRender(T entity) { return true; }

	public void preRender(T entity, EntityInfo info) { }

	public void postRender(T entity, EntityInfo info) { }

	@Override
	protected final ResourceLocation getEntityTexture(T entity) {

		return stateManager.getState(getStateFromEntity(entity)).texture();
	}

	@Override
	public final void reload(EntityStateMap states) {

		stateManager.reloadStates(states);
	}

	@Override
	public final void setMissingno() {

		stateManager.setAllInvalid();
	}
}