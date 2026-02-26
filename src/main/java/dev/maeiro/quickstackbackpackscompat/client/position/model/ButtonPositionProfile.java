package dev.maeiro.quickstackbackpackscompat.client.position.model;

public class ButtonPositionProfile {
	public static final String DEFAULT_ANCHOR = "STORAGE_TOP_RIGHT";
	public static final int DEFAULT_OFFSET_X = -90;
	public static final int DEFAULT_OFFSET_Y = -110;
	public static final int DEFAULT_BUTTON_SPACING_X = 12;

	public String anchor;
	public Integer offsetX;
	public Integer offsetY;
	public Integer buttonSpacingX;

	public ButtonPositionProfile() {
	}

	public ButtonPositionProfile(String anchor, Integer offsetX, Integer offsetY, Integer buttonSpacingX) {
		this.anchor = anchor;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.buttonSpacingX = buttonSpacingX;
	}

	public ButtonPositionProfile copy() {
		return new ButtonPositionProfile(anchor, offsetX, offsetY, buttonSpacingX);
	}

	public void applyOverrides(ButtonPositionProfile overrides) {
		if (overrides == null) {
			return;
		}

		if (overrides.anchor != null) {
			anchor = overrides.anchor;
		}
		if (overrides.offsetX != null) {
			offsetX = overrides.offsetX;
		}
		if (overrides.offsetY != null) {
			offsetY = overrides.offsetY;
		}
		if (overrides.buttonSpacingX != null) {
			buttonSpacingX = overrides.buttonSpacingX;
		}
	}
}
