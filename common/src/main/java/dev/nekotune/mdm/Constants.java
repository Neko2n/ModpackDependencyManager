package dev.nekotune.mdm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.resources.ResourceLocation;

public interface Constants {

	public static final String MOD_ID = "mdm";
	public static final String MOD_NAME = "Modpack Dependency Manager";
	public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

	/**
	 * The versions of Minecraft that this mod can run on.
	 */
	public static final String[] MC_VERSIONS = {
		"1.21.1"
	};

	public static interface Assets {
		public static interface Gui {
			public static record Sprite(ResourceLocation location, int width, int height) {
				public static interface Icon {
					public static final Sprite EDIT = new Sprite(ResourceLocation.fromNamespaceAndPath(
							Constants.MOD_ID, "icon/edit"), 20, 20);
					public static final Sprite DELETE = new Sprite(ResourceLocation.fromNamespaceAndPath(
							Constants.MOD_ID, "icon/delete"), 20, 20);
				}
			}
		}
		public static interface Lang {
			public static final String KEY = Constants.MOD_ID;
			public static interface Gui {
				public static final String KEY = Lang.KEY + ".gui";
				public static interface Screen {
					public static final String KEY = Gui.KEY + ".screen";
				}
				public static interface Widget {
					public static final String KEY = Gui.KEY + ".widget";
					public static interface Input {
						public static final String KEY = Widget.KEY + ".input";
					}
				}
			}
		}
	}
}