package kvverti.enim.entity;

import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Iterator;

import net.minecraft.client.renderer.texture.LayeredColorMaskTexture;
import net.minecraft.tileentity.TileEntityBanner;
import net.minecraft.tileentity.BannerPattern;
import net.minecraft.util.ResourceLocation;

import kvverti.enim.Enim;
import kvverti.enim.Util;
import kvverti.enim.model.EntityState;

import static java.util.stream.Collectors.toList;

/** Based on BannerTextures.Cache */
public class TextureByStateCache {

	//until implementing changeable locations
	private static final String BANNER_PATTERNS = "minecraft:textures/entity/banner/";

	private final String id;
	private final int maxSize;
	private final int maxSinceUse;
	private final Map<String, Entry> cacheMap = new LinkedHashMap<>();

	public TextureByStateCache(String cacheId, int maxSize, int persistenceSinceLastUse) {

		this.id = cacheId;
		this.maxSize = maxSize;
		this.maxSinceUse = persistenceSinceLastUse;
	}

	public ResourceLocation getLocation(TileEntityBanner obj, EntityState state) {

		if(state.texture() == Util.MISSING_LOCATION)
			return Util.MISSING_LOCATION;
		ResourceLocation baseTexture = state.texture();
		String key = String.join("/", id, baseTexture.getResourceDomain(), baseTexture.getResourcePath(), obj.getPatternResourceLocation());
		Entry entry = cacheMap.get(key);
		if(entry == null) {
			//create entry
			if(size() > maxSize && !clearStale())
				return baseTexture;
			entry = new Entry(new ResourceLocation(Enim.ID, key));
			List<String> patterns = obj.getPatternList()
				.stream()
				.map(BannerPattern::getFileName)
				.map(name -> BANNER_PATTERNS + name + ".png")
				.collect(toList());
			Entities.textureManager().loadTexture(entry.texture, new LayeredColorMaskTexture(state.texture(), patterns, obj.getColorList()));
			cacheMap.put(key, entry);
		}
		entry.lastUsed = System.currentTimeMillis();
		return entry.texture;
	}

	public int size() { return cacheMap.size(); }

	public void clearAll() {

		for(Iterator<Entry> itr = cacheMap.values().iterator(); itr.hasNext(); ) {

			Entities.textureManager().deleteTexture(itr.next().texture);
			itr.remove();
		}
	}

	public boolean clearStale() {

		for(Iterator<Entry> itr = cacheMap.values().iterator(); itr.hasNext(); ) {

			Entry e = itr.next();
			if(System.currentTimeMillis() - e.lastUsed > maxSinceUse) {

				Entities.textureManager().deleteTexture(e.texture);
				itr.remove();
			}
		}
		return cacheMap.size() <= maxSize;
	}

	private static class Entry {

		long lastUsed;
		ResourceLocation texture;

		Entry(ResourceLocation texture) {

			this.lastUsed = System.currentTimeMillis();
			this.texture = texture;
		}
	}
}