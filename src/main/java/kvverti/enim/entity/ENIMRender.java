package kvverti.enim.entity;

import java.lang.reflect.Method;
import java.util.List;

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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.ResourceLocation;

import com.google.common.collect.ImmutableList;

import kvverti.enim.entity.state.RenderState;
import kvverti.enim.entity.state.StateManager;
import kvverti.enim.model.EntityState;
import kvverti.enim.model.EntityStateMap;
import kvverti.enim.Keys;
import kvverti.enim.Util;
import kvverti.enim.Vec3f;

/** Base class for ENIM reloadable entity renders. */
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

	private final StateManager stateManager;
	private EntityState currentState;

	protected ENIMRender(RenderManager manager, IProperty<?>... properties) {

		super(manager);
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
		bindTexture(currentState.texture());
		GlStateManager.rotate(currentState.y(), 0.0f, 1.0f, 0.0f);
		EntityInfo info = new EntityInfo();
		info.speedSq = Entities.speedSq(entity);
		info.partialTicks = partialTicks;
		info.entityYaw = yaw;
		info.headYaw = headYaw(entity, yaw);
		info.entityPitch = entity.rotationPitch;
		info.scale = 0.0625f * currentState.scale();
		info.color = i -> i < 0 ? getBaseColor(entity, info) : getBaseColor(entity, info).scale(getColorOverlay(entity, info, i));
		info.alpha = getBaseAlpha(entity, info);
		preRender(entity, info);
		GEntity e = new GEntity(entity);
		if(shouldRender(entity)) {

			model.render(e, info);
			ResourceLocation overlay = currentState.overlay();
			if(overlay != null)
				renderOverlay(e, info, model, overlay);
			List<EntityState> layers = currentState.getLayers();
			for(int i = 0; i < layers.size(); i++)
				renderLayer(e, info, layers.get(i), stateManager.getLayerModel(renderState, i));
		} else {
			//because invisibility doesn't work with your texture layers, Mojang
			//or model layers, for that matter, but WHOOP-DE-DOO
			ResourceLocation overlay = currentState.overlay();
			if(overlay != null)
				renderOverlay(e, info, model, overlay);
			List<EntityState> layers = currentState.getLayers();
			for(int i = 0; i < layers.size(); i++) {

				overlay = layers.get(i).overlay();
				if(overlay != null)
					renderOverlay(e, info, stateManager.getLayerModel(renderState, i), overlay);
			}
		}
		postRender(entity, info);
		GlStateManager.popMatrix();
		super.doRender(entity, x, y, z, yaw, partialTicks);
		if(entity instanceof EntityLiving)
			Util.invokeUnchecked(proxy, renderLeash, entity, x, y, z, yaw, partialTicks);
	}

	private void renderLayer(GEntity entity, EntityInfo info, EntityState layer, ENIMModel model) {

		GlStateManager.pushMatrix();
		bindTexture(layer.texture());
		GlStateManager.rotate(layer.y(), 0.0f, 1.0f, 0.0f);
		info.scale = 0.0625f * layer.scale();
		model.render(entity, info);
		ResourceLocation overlay = layer.overlay();
		if(overlay != null)
			renderOverlay(entity, info, model, overlay);
		GlStateManager.popMatrix();
	}

	private void renderOverlay(GEntity entity, EntityInfo info, ENIMModel model, ResourceLocation overlay) {

		bindTexture(overlay);
		GlStateManager.enableBlend();
		GlStateManager.disableAlpha();
		GlStateManager.blendFunc(1, 1);
		//begin magic
		GlStateManager.depthMask(!entity.getEntity().isInvisible());
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 0xf0f0, 0.0f);
		//end magic
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		model.render(entity, info);
		//begin magic
		int brightness = entity.getEntity().getBrightnessForRender(info.partialTicks);
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, brightness % 0x10000, brightness / 0x10000);
		//end magic
		GlStateManager.disableBlend();
		GlStateManager.enableAlpha();
	}

	private float headYaw(Entity entity, float bodyYaw) {

		return entity instanceof EntityLivingBase ? ((EntityLivingBase) entity).rotationYawHead - bodyYaw : 0.0f;
	}

	/**
	 * Whether the entity should render. The entity model will be rendered only if this method returns true.
	 * Note that texture overlays will still be rendered if this method returns false, because Mojang.
	 */
	public boolean shouldRender(T entity) { return true; }

	/**
	 * Method called immediately before the main model is rendered.
	 */
	protected void preRender(T entity, EntityInfo info) { }

	/**
	 * Method called immediately after the main model and any layers are rendered.
	 */
	protected void postRender(T entity, EntityInfo info) { }

	/** Returns the color in RGB format that will be multiplied onto all model elements, regardless of tintindex. */
	public Vec3f getBaseColor(T entity, EntityInfo info) {

		return Vec3f.IDENTITY;
	}

	/** Returns the alpha value that will be applied to all model elements, regardless of tintindex. */
	public float getBaseAlpha(T entity, EntityInfo info) {

		return 1.0f;
	}

	/**
	 * Returns the color in RGB format that will be overlayed (multiplied) onto applicable model elements. This method is partially bound
	 * to the entity being rendered in the EntityInfo structure passed to the render callbacks.
	 */
	public Vec3f getColorOverlay(T entity, EntityInfo info, int colorIndex) {

		return Vec3f.IDENTITY;
	}

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
		//render fire
		//seems like the second half of this conditional could be added to the EntityPlayer class,
		//but oh well, I'm not Mojang
		if(entity.canRenderOnFire() && !(entity instanceof EntityPlayer && ((EntityPlayer) entity).isSpectator()))
			Util.invokeUnchecked(this, renderEntityOnFire, entity, x, y, z, partialTicks);
	}

	private static final Method renderEntityOnFire = Util.findMethod(Render.class,
		void.class,
		new String[] { "func_76977_a", "renderEntityOnFire" },
		Entity.class,
		double.class,
		double.class,
		double.class,
		float.class);

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

	/** Modified from Render#renderShadow to accept custom shadow size */
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
	public final ImmutableList<RenderState> getValidStates() {

		return stateManager.getRenderStates();
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