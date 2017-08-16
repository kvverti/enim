package kvverti.enim.entity;

import java.util.List;
import java.util.concurrent.TimeUnit;

import net.minecraft.client.renderer.texture.LayeredColorMaskTexture;
import net.minecraft.tileentity.BannerPattern;
import net.minecraft.tileentity.TileEntityBanner;
import net.minecraft.util.ResourceLocation;

import com.google.common.cache.LoadingCache;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.CacheBuilder;

import kvverti.enim.Keys;
import kvverti.enim.Util;
import kvverti.enim.model.EntityState;

import static java.util.stream.Collectors.toList;

/**
 * An organizer and cache for banner textures. Banner textures are any texture that is composed of a base texture
 * and overlay textures which correspond to banner patterns, such as the banner and shield textures.
 * Each BannerTextures instance has a (preferably) unique ID and a banner pattern directory, which contains
 * the banner pattern overlays.
 */
public final class BannerTextures {

	/**
	 * Internal cache containing the banner textures.
	 */
	private final LoadingCache<TextureKey, ResourceLocation> textures;

	/** Cache id - to prevent collisions */
	private final String id;

	/** Pattern directory */
	private final String patterns;

	public BannerTextures(String id, String patternDir) {

		this(id, patternDir, 255, 5000);
	}

	public BannerTextures(String id, String patternDir, int maxSize, int millisPersistance) {

		this.id = id;
		this.patterns = patternDir;
		this.textures = CacheBuilder.newBuilder()
			.expireAfterAccess(millisPersistance, TimeUnit.MILLISECONDS)
			.initialCapacity(Math.min(100, maxSize))
			.maximumSize(maxSize)
			.removalListener(note -> kvverti.enim.Logger.info("Removed banner texture: %s", note.getValue()))
			.build(new TextureLoader());
	}

	/** Retrieves the banner texture with overlays given by the banner object, creating a new texture if necessary. */
	public ResourceLocation getTexture(TileEntityBanner obj, EntityState state) {

		if(state.texture().equals(Util.MISSING_LOCATION) || obj.getPatternList() == null) //for reasons who knows
			return state.texture();
		return textures.getUnchecked(new TextureKey(obj, state));
	}

	public void clearAll() {

		textures.invalidateAll();
	}

	/** Loader for textures. Values are of the form base_texture_modid:id/base_texture_path/pattern_location */
	private final class TextureLoader extends CacheLoader<TextureKey, ResourceLocation> {

		@Override
		public ResourceLocation load(TextureKey key) {

			ResourceLocation bannerTex = new ResourceLocation(key.baseTexture.getResourceDomain(),
				String.join("/", id, key.baseTexture.getResourcePath(), key.bannerPatterns));
			List<String> patternList = key.banner.getPatternList()
				.stream()
				.map(BannerPattern::getFileName)
				.map(name -> patterns + name + Keys.PNG)
				.collect(toList());
			Entities.textureManager().loadTexture(bannerTex,
				new LayeredColorMaskTexture(key.baseTexture, patternList, key.banner.getColorList()));
			kvverti.enim.Logger.info("Created banner texture: %s", bannerTex);
			return bannerTex;
		}
	}

	/**
	 * Key for texture cache. All fields are non-null. Uniqueness of key is based on the base texture and patterns.
	 * Patterns are stored separately because the TileEntityBanner is shared between all item instances.
	 */
	private static final class TextureKey {

		transient final TileEntityBanner banner;
		final String bannerPatterns;
		final ResourceLocation baseTexture;

		TextureKey(TileEntityBanner b, EntityState t) {

			banner = b;
			bannerPatterns = b.getPatternResourceLocation();
			baseTexture = t.texture();
		}

		@Override
		public boolean equals(Object obj) {

			if(!(obj instanceof TextureKey))
				return false;
			TextureKey key = (TextureKey) obj;
			return bannerPatterns.equals(key.bannerPatterns) && baseTexture.equals(key.baseTexture);
		}

		@Override
		public int hashCode() {

			return baseTexture.hashCode();
		}
	}
}