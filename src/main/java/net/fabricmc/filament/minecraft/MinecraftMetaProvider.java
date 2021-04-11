package net.fabricmc.filament.minecraft;

public interface MinecraftMetaProvider {
	MinecraftVersionManifest getVersionManifest();

	MinecraftVersionMetadata getVersionMetadata(String id);
}
