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
	public MinecraftVersionMetadata getVersionMetadata(String id) {
		Objects.requireNonNull(id);

		if (versionMetadataMap.containsKey(id)) {
			return versionMetadataMap.get(id);
		}

		MinecraftVersionManifest.Version manifestVersion = getVersionManifest().getVersion(id)
				.orElseThrow(() -> new NoSuchElementException("Could not find version for " + id));

		MinecraftVersionMetadata metadata;

		try {
			metadata = JsonUtil.parseFromUrl(manifestVersion.getUrl(), MinecraftVersionMetadata.class);
		} catch (IOException e) {
			throw new RuntimeException("Failed to get version meta", e);
		}

		versionMetadataMap.put(id, metadata);
		return metadata;
	}
}
