package kvverti.enim.entity;

import java.lang.reflect.Method;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.properties.IProperty;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.ResourceLocation;

import com.google.common.collect.ImmutableSet;

import kvverti.enim.entity.state.RenderState;
import kvverti.enim.entity.state.StateManager;
import kvverti.enim.model.EntityState;
import kvverti.enim.model.EntityStateMap;
import kvverti.enim.Keys;
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

	public static final ResourceLocation SHADOW_TEXTURE = new ResourceLocation("minecraft:textures/misc/shadow.png");

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
		//bindEntityTexture(entity);
		bindTexture(currentState.texture());
		GlStateManager.rotate(currentState.y(), 0.0f, 1.0f, 0.0f);
		EntityInfo info = new EntityInfo();
		info.speedSq = Entities.speedSq(entity);
		info.partialTicks = partialTicks;
		info.entityYaw = yaw;
		info.headYaw = headYaw(entity, yaw);
		info.entityPitch = entity.rotationPitch;
		info.scale = 0.0625f * currentState.scale();
		preRender(entity, info);
		if(shouldRender(entity))
			model.render(entity, info);
		//because invisibility doesn't work with your texture layers, Mojang
		ResourceLocation overlay = currentState.overlay();
		if(overlay != null) {

			bindTexture(overlay);
			GlStateManager.enableBlend();
			GlStateManager.disableAlpha();
			GlStateManager.blendFunc(1, 1);
			//begin magic
			GlStateManager.depthMask(!entity.isInvisible());
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 0xf0f0, 0.0f);
			//end magic
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			model.render(entity, info);
			//begin magic
			int brightness = entity.getBrightnessForRender(partialTicks);
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, brightness % 0x10000, brightness / 0x10000);
			//end magic
			GlStateManager.disableBlend();
			GlStateManager.enableAlpha();
		}
		postRender(entity, info);
		GlStateManager.popMatrix();
		super.doRender(entity, x, y, z, yaw, partialTicks);
		if(entity instanceof EntityLiving)
			Util.invokeUnchecked(proxy, renderLeash, entity, x, y, z, yaw, partialTicks);
	}

	private float headYaw(Entity entity, float bodyYaw) {

		return entity instanceof EntityLivingBase ? ((EntityLivingBase) entity).rotationYawHead - bodyYaw : 0.0f;
	}

	public boolean shouldRender(T entity) { return true; }

	protected void preRender(T entity, EntityInfo info) { }

	protected void postRender(T entity, EntityInfo info) { }

	/** Modified from Render#doRenderShadowAndFire */
	@Override
	@SuppressWarnings("unchecked")
	public final void doRenderShadowAndFire(Entity entity, double x, double y, double z, float yaw, float partialTicks) {

		if(renderManager.options == null)
			return;
		//render shadow
		float shadowSize = 0.0625f * currentState.scale() * currentState.model().properties().shadowSize();
		if(renderManager.options.entityShadows && shadowSize > 0.0f && shouldRender((T) entity) && renderManager.isRenderShadow()) {

			double distance = renderManager.getDistanceToCamera(entity.posX, entity.posY, entity.posZ);
			float weightedOpacity = (float) ((1.0 - (distance / 256.0)) * this.shadowOpaque);
			if(weightedOpacity > 0.0f)
				renderShadow(entity, x, y, z, shadowSize, weightedOpacity, partialTicks);
		}
		//render fire: TODO
	}

	private static final Method mapShadowOnBlock = Util.findMethod(Render.class,
		void.class,
		new String[] { "func_180549_a", "renderShadowSingle" },
		IBlockState.class,
		double.class,
		double.class,
		double.class,
		BlockPos.class,
		float.class,
		float.class,
		double.class,
		double.class,
		double.class);

	/** Modified from Render#renderShadow */
	private void renderShadow(Entity entity, double x, double y, double z, float size, float opacity, float partialTicks) {

		GlStateManager.enableBlend();
		GlStateManager.blendFunc(770, 771);
		GlStateManager.depthMask(false);
		bindTexture(SHADOW_TEXTURE);
		Tessellator tez = Tessellator.getInstance();
		VertexBuffer renderer = tez.getBuffer();
		renderer.begin(7, net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION_TEX_COLOR);
		double px = Entities.interpolate((float) entity.lastTickPosX, (float) entity.posX, partialTicks);
		double py = Entities.interpolate((float) entity.lastTickPosY, (float) entity.posY, partialTicks);
		double pz = Entities.interpolate((float) entity.lastTickPosZ, (float) entity.posZ, partialTicks);
		int minX = (int) Math.floor(px - size);
		int maxX = (int) Math.floor(px + size);
		int minY = (int) Math.floor(py - size);
		int maxY = (int) Math.floor(py);
		int minZ = (int) Math.floor(pz - size);
		int maxZ = (int) Math.floor(pz + size);
		for(BlockPos pos : BlockPos.getAllInBoxMutable(new BlockPos(minX, minY, minZ), new BlockPos(maxX, maxY, maxZ))) {

			IBlockState state = renderManager.world.getBlockState(pos.down());
			if(state.getRenderType() != EnumBlockRenderType.INVISIBLE && renderManager.world.getLightFromNeighbors(pos) > 3)
				Util.invokeUnchecked(this, mapShadowOnBlock, state, x, y, z, pos, opacity, size, x - px, y - py, z - pz);
		}
		tez.draw();
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		GlStateManager.depthMask(true);
		GlStateManager.disableBlend();
	}

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