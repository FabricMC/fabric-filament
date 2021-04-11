package net.fabricmc.filament;

import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.provider.Property;

import net.fabricmc.filament.minecraft.MinecraftMetaProvider;

public abstract class FilamentExtension {
	public abstract Property<String> getMinecraftVersion();

	private final NamedDomainObjectProvider<Configuration> minecraftLibrariesConfiguration;
	private final MinecraftMetaProvider minecraftMetaProvider;

	public FilamentExtension(NamedDomainObjectProvider<Configuration> minecraftLibrariesConfiguration, MinecraftMetaProvider minecraftMetaProvider) {
		this.minecraftLibrariesConfiguration = minecraftLibrariesConfiguration;
		this.minecraftMetaProvider = minecraftMetaProvider;

		getMinecraftVersion().finalizeValueOnRead();
	}

	public NamedDomainObjectProvider<Configuration> getMinecraftLibrariesConfiguration() {
		return minecraftLibrariesConfiguration;
	}

	public MinecraftMetaProvider getMinecraftMetaProvider() {
		return minecraftMetaProvider;
	}
}
