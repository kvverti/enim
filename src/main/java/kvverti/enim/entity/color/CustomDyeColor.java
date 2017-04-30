package kvverti.enim.entity.color;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;
import java.util.EnumMap;

import net.minecraftforge.fml.common.registry.IForgeRegistryEntry;

import net.minecraft.client.resources.IResourceManager;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.ResourceLocation;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.JsonParseException;

import kvverti.enim.Keys;
import kvverti.enim.Logger;
import kvverti.enim.Vec3f;

/**
 * This class provides custom colors for EnumDyeColor that may be reloaded through resource packs. Instances may specify a fallback
 * instance to use if this instance does not provide a given color.
 */
public class CustomDyeColor extends IForgeRegistryEntry.Impl<CustomDyeColor> {

	/** The fallback instance to use. May be null. */
	private final CustomDyeColor fallback;

	/** Map containing the color mappings. Not final in the case that someone adds a constant to EnumDyeColor. */
	private Map<EnumDyeColor, Vec3f> colors;

	/**
	 * Creates a new instance with the default fallback.
	 */
	public CustomDyeColor() {

		this(MinecraftCustomDyeColors.DEFAULT);
	}

	/**
	 * Creates a new instance with the specified fallback. If fallback is null,
	 * the color values in the MapColor class are used as a fallback.
	 * @param fallback The fallback instance to use. May be null.
	 */
	public CustomDyeColor(CustomDyeColor fallback) {

		this.fallback = fallback;
		this.colors = Collections.emptyMap();
	}

	/** Returns the location associated with this instance. */
	public ResourceLocation getResourceLocation() {

		ResourceLocation loc = getRegistryName();
		return new ResourceLocation(loc.getResourceDomain(), Keys.COLORS_DIR + loc.getResourcePath() + Keys.JSON);
	}

	/** Gets the color associated with the given EnumDyeColor as a Vec3f containing the RGB components. */
	public Vec3f getColor(EnumDyeColor color) {

		if(colors.containsKey(color))
			return colors.get(color);
		else if(fallback != null)
			return fallback.getColor(color);
		else 
			return getColor(color.getMapColor().colorValue);
	}

	private Vec3f getColor(int rgb) {

		float r = ((rgb >> 16) & 0xff) / 255.0f;
		float g = ((rgb >> 8) & 0xff) / 255.0f;
		float b = (rgb & 0xff) / 255.0f;
		return Vec3f.of(r, g, b);
	}

	private static final Type colormapType = new TypeToken<Map<String, String>>(){}.getType();

	/** Reloads the colors from resources. Note that this does <em>not</em> reload fallback colors. */
	public void reloadColors(IResourceManager manager) {

		colors.clear();
		ResourceLocation location = getResourceLocation();
		try(Reader reader = Util.getReaderFor(manager, location)) {

			Map<String, String> initColorMap = new Gson().fromJson(reader, colormapType);
			Map<EnumDyeColor, Vec3f> finalColorMap = new EnumMap<>(EnumDyeColor.class);
			for(Map.Entry<String, String> entry : initColorMap.entrySet()) {

				EnumDyeColor key = byName(entry.getKey());
				if(key != null) {

					int col;
					try { col = Integer.parseInt(entry.getValue(), 16); }
					catch(NumberFormatException e) { continue; }
					Vec3f value = getColor(col);
					finalColorMap.put(key, value);
				}
			}
			colors = finalColorMap;

		} catch(IOException e) { Logger.warn("Could not read file " + location); }
		catch(JsonParseException e) { Logger.error(e); }
	}

	private EnumDyeColor byName(String name) {

		for(EnumDyeColor color : EnumDyeColor.values())
			if(color.getName().equals(name))
				return color;
		return null;
	}
}