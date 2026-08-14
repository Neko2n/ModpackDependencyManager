package dev.nekotune.mdm.client.gui.config.dependencies;

import dev.nekotune.mdm.Constants;
import dev.nekotune.mdm.definition.DependencyInfo;
import net.minecraft.resources.ResourceLocation;

public record ToggleSprites(int width, int height, ResourceLocation onSprite, ResourceLocation offSprite) {
    
    public static interface Hosts {
        public static final String PATH = "hosts/";
        public static final ToggleSprites MODRINTH = new ToggleSprites(20, 20,
                ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, PATH + "modrinth_on"),
                ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, PATH + "modrinth_off"));
        public static final ToggleSprites CURSEFORGE = new ToggleSprites(20, 20,
                ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, PATH + "curseforge_on"),
                ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, PATH + "curseforge_off"));
        
        /**
         * @param host The host to fetch sprites for.
         * @return The button sprites associated with the given host.
         */
        public static ToggleSprites get(final DependencyInfo.Host host) {
            switch (host) {
                case CURSEFORGE:
                    return CURSEFORGE;
                case MODRINTH:
                    return MODRINTH;
                default:
                    throw new UnsupportedOperationException();
            }
        }
    }
}