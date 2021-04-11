package net.fabricmc.filament.minecraft;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

import net.fabricmc.filament.Constants;
import net.fabricmc.filament.util.JsonUtil;

public class MinecraftMetaProviderImpl implements MinecraftMetaProvider {
	// TODO possibly cache this somewhere else
	private MinecraftVersionManifest versionManifest = null;
	private Map<String, MinecraftVersionMetadata> versionMetadataMap = new HashMap<>();

	@Override
	public MinecraftVersionManifest getVersionManifest() {
		if (versionManifest == null) {
			try {
				versionManifest = JsonUtil.parseFromUrl(Constants.MINECRAFT_LAUNCHER_META_V2, MinecraftVersionManifest.class);
			} catch (IOException e) {
				throw new RuntimeException("Failed to get version manifest", e);
			}
		}

		return versionManifest;
	}

	@Override
	public MinecraftVersionMetadata getVersionMetadata(String version) {
		Objects.requireNonNull(version);

		if (versionMetadataMap.containsKey(version)) {
			return versionMetadataMap.get(version);
		}

		MinecraftVersionManifest.Version manifestVersion = getVersionManifest().getVersion(version)
				.orElseThrow(() -> new NoSuchElementException("Could not find version for " + version));

		MinecraftVersionMetadata metadata;

		try {
			metadata = JsonUtil.parseFromUrl(manifestVersion.getUrl(), MinecraftVersionMetadata.class);
		} catch (IOException e) {
			throw new RuntimeException("Failed to get version meta", e);
		}

		versionMetadataMap.put(version, metadata);
		return metadata;
	}
}
