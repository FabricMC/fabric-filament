package net.fabricmc.filament;

import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;

import net.fabricmc.filament.minecraft.MinecraftMetaProviderImpl;
import net.fabricmc.filament.minecraft.MinecraftVersionMetadata;
import net.fabricmc.filament.task.CombineUnpickDefinitionsTask;
import net.fabricmc.filament.task.GeneratePackageInfoMappingsTask;
import net.fabricmc.filament.task.JavadocLintTask;
import net.fabricmc.filament.task.RemapUnpickDefinitionsTask;

public final class FilamentGradlePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		NamedDomainObjectProvider<Configuration> minecraftLibrariesConfiguration = project.getConfigurations().register(Constants.MINECRAFT_LIBRARIES_CONFIGURATION);

		FilamentExtension extension = project.getExtensions().create("filament", FilamentExtension.class,
				minecraftLibrariesConfiguration,
				new MinecraftMetaProviderImpl()
		);

		project.getTasks().register("generatePackageInfoMappings", GeneratePackageInfoMappingsTask.class);
		project.getTasks().register("javadocLint", JavadocLintTask.class);

		var combineUnpickDefinitions = project.getTasks().register("combineUnpickDefinitions", CombineUnpickDefinitionsTask.class);
		project.getTasks().register("remapUnpickDefinitionsIntermediary", RemapUnpickDefinitionsTask.class, task -> {
			task.dependsOn(combineUnpickDefinitions);
			task.getInput().set(combineUnpickDefinitions.flatMap(CombineUnpickDefinitionsTask::getOutput));
			task.getSourceNamespace().set("named");
			task.getTargetNamespace().set("intermediary");
		});

		project.afterEvaluate(this::afterEvaluate);
	}

	private void afterEvaluate(Project project) {
		FilamentExtension extension = project.getExtensions().getByType(FilamentExtension.class);

		project.getRepositories().maven(repo -> {
			repo.setName("mojang");
			repo.setUrl(Constants.MOJANG_MAVEN_REPO);
			repo.metadataSources(MavenArtifactRepository.MetadataSources::artifact);
		});

		extension.getMinecraftLibrariesConfiguration().configure(configuration -> {
			MinecraftVersionMetadata metadata = extension.getMinecraftMetaProvider().getVersionMetadata(extension.getMinecraftVersion().get());

			configuration.setTransitive(false);
			configuration.withDependencies(dependencies -> {
				for (MinecraftVersionMetadata.Library library : metadata.getLibraries()) {
					if (library.isValidForOS() && !library.hasNatives() && library.getArtifact() != null) {
						dependencies.add(project.getDependencies().create(library.getName()));
					}
				}
			});
		});
	}
}
