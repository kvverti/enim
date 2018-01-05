package kvverti.enim.entity.color;

/** Holds vanilla custom dye colors */
public final class MinecraftCustomDyeColors {

    /** The default dye colors */
    public static final CustomDyeColor DEFAULT;

    static {

        DEFAULT = new CustomDyeColor(null).setRegistryName("minecraft:default");
    }

    private MinecraftCustomDyeColors() { }
}