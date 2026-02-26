package dev.maeiro.quickstackbackpackscompat.client.position;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import dev.maeiro.quickstackbackpackscompat.QuickstackBackpacksCompat;
import dev.maeiro.quickstackbackpackscompat.client.position.model.ButtonPositionConfig;
import dev.maeiro.quickstackbackpackscompat.client.position.model.ButtonPositionProfile;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;

public class ButtonPositionManager {
	public static final String CONFIG_FILE_NAME = "quickstackbackpackscompat_button_positions.json";
	public static final ButtonPositionManager INSTANCE = new ButtonPositionManager();

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

	private final Path configPath = FMLPaths.CONFIGDIR.get().resolve(CONFIG_FILE_NAME);
	private ButtonPositionConfig currentConfig = ButtonPositionConfig.createDefault();
	private long lastModified = Long.MIN_VALUE;

	private ButtonPositionManager() {
	}

	public synchronized ResolvedProfile resolveProfile(String itemProfileKey) {
		reloadIfNeeded();
		ButtonPositionProfile resolved = currentConfig.resolve(itemProfileKey);
		return new ResolvedProfile(
				parseAnchor(resolved.anchor),
				resolved.offsetX,
				resolved.offsetY,
				resolved.buttonSpacingX
		);
	}

	private void reloadIfNeeded() {
		ensureFileExists();

		long modifiedTime = getLastModified();
		if (modifiedTime == lastModified) {
			return;
		}

		try (Reader reader = Files.newBufferedReader(configPath, StandardCharsets.UTF_8)) {
			ButtonPositionConfig loaded = GSON.fromJson(reader, ButtonPositionConfig.class);
			if (loaded == null) {
				throw new JsonParseException("Config file is empty.");
			}
			loaded.ensureNonNullSections();
			currentConfig = loaded;
		} catch (Exception e) {
			QuickstackBackpacksCompat.LOGGER.warn("Failed to parse {}. Keeping previous valid config.", configPath, e);
		}

		lastModified = modifiedTime;
	}

	private void ensureFileExists() {
		if (Files.exists(configPath)) {
			return;
		}

		try {
			Files.createDirectories(configPath.getParent());
			ButtonPositionConfig defaults = ButtonPositionConfig.createDefault();
			try (Writer writer = Files.newBufferedWriter(
					configPath,
					StandardCharsets.UTF_8,
					StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING,
					StandardOpenOption.WRITE)) {
				GSON.toJson(defaults, writer);
			}
			currentConfig = defaults;
			lastModified = getLastModified();
			QuickstackBackpacksCompat.LOGGER.info("Created default button position config at {}", configPath);
		} catch (IOException e) {
			QuickstackBackpacksCompat.LOGGER.warn("Failed to create default button position config at {}", configPath, e);
		}
	}

	private long getLastModified() {
		try {
			return Files.getLastModifiedTime(configPath).toMillis();
		} catch (IOException e) {
			return Long.MIN_VALUE;
		}
	}

	private Anchor parseAnchor(String anchorName) {
		if (anchorName == null || anchorName.isBlank()) {
			return Anchor.STORAGE_TOP_RIGHT;
		}

		try {
			return Anchor.valueOf(anchorName.toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException e) {
			QuickstackBackpacksCompat.LOGGER.warn("Unknown anchor '{}' in {}. Falling back to STORAGE_TOP_RIGHT.", anchorName, configPath);
			return Anchor.STORAGE_TOP_RIGHT;
		}
	}

	public enum Anchor {
		STORAGE_TOP_RIGHT
	}

	public record ResolvedProfile(Anchor anchor, int offsetX, int offsetY, int buttonSpacingX) {
	}
}
