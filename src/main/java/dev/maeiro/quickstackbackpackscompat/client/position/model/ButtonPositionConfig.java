package dev.maeiro.quickstackbackpackscompat.client.position.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class ButtonPositionConfig {
	public static final int CURRENT_VERSION = 1;
	public static final String WILDCARD_PROFILE_KEY = "*";

	public int version = CURRENT_VERSION;
	public ButtonPositionProfile defaults = createDefaultRootProfile();
	public Map<String, ButtonPositionProfile> profiles = createDefaultProfiles();

	public static ButtonPositionConfig createDefault() {
		return new ButtonPositionConfig();
	}

	public ButtonPositionProfile resolve(String itemProfileKey) {
		ButtonPositionProfile resolved = new ButtonPositionProfile(
				ButtonPositionProfile.DEFAULT_ANCHOR,
				ButtonPositionProfile.DEFAULT_OFFSET_X,
				ButtonPositionProfile.DEFAULT_OFFSET_Y,
				ButtonPositionProfile.DEFAULT_BUTTON_SPACING_X
		);

		resolved.applyOverrides(defaults);
		resolved.applyOverrides(getProfile(WILDCARD_PROFILE_KEY));
		if (itemProfileKey != null && !itemProfileKey.isBlank()) {
			resolved.applyOverrides(getProfile(itemProfileKey));
		}

		if (resolved.anchor == null) {
			resolved.anchor = ButtonPositionProfile.DEFAULT_ANCHOR;
		}
		if (resolved.offsetX == null) {
			resolved.offsetX = ButtonPositionProfile.DEFAULT_OFFSET_X;
		}
		if (resolved.offsetY == null) {
			resolved.offsetY = ButtonPositionProfile.DEFAULT_OFFSET_Y;
		}
		if (resolved.buttonSpacingX == null) {
			resolved.buttonSpacingX = ButtonPositionProfile.DEFAULT_BUTTON_SPACING_X;
		}

		return resolved;
	}

	public void ensureNonNullSections() {
		if (defaults == null) {
			defaults = createDefaultRootProfile();
		}
		if (profiles == null) {
			profiles = createDefaultProfiles();
		}
	}

	private ButtonPositionProfile getProfile(String key) {
		if (profiles == null) {
			return null;
		}
		return profiles.get(key);
	}

	private static ButtonPositionProfile createDefaultRootProfile() {
		return new ButtonPositionProfile(
				ButtonPositionProfile.DEFAULT_ANCHOR,
				ButtonPositionProfile.DEFAULT_OFFSET_X,
				ButtonPositionProfile.DEFAULT_OFFSET_Y,
				ButtonPositionProfile.DEFAULT_BUTTON_SPACING_X
		);
	}

	private static Map<String, ButtonPositionProfile> createDefaultProfiles() {
		Map<String, ButtonPositionProfile> result = new LinkedHashMap<>();
		result.put(WILDCARD_PROFILE_KEY, new ButtonPositionProfile(null, -90, -110, 12));
		result.put("sophisticatedbackpacks:backpack", new ButtonPositionProfile(null, -165, 53, null));
		result.put("sophisticatedbackpacks:copper_backpack", new ButtonPositionProfile(null, -165, 89, null));
		result.put("sophisticatedbackpacks:iron_backpack", new ButtonPositionProfile(null, -165, 107, null));
		result.put("sophisticatedbackpacks:gold_backpack", new ButtonPositionProfile(null, -165, 161, null));
		result.put("sophisticatedbackpacks:diamond_backpack", new ButtonPositionProfile(null, -190, 161, null));
		result.put("sophisticatedbackpacks:netherite_backpack", new ButtonPositionProfile(null, -190, 179, null));
		return result;
	}
}
