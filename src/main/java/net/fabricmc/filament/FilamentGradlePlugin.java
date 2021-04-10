package net.fabricmc.filament;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import net.fabricmc.filament.task.GeneratePackageInfoMappingsTask;
import net.fabricmc.filament.task.JavadocLintTask;

public final class FilamentGradlePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getTasks().register("generatePackageInfoMappings", GeneratePackageInfoMappingsTask.class);
		project.getTasks().register("javadocLint", JavadocLintTask.class);
	}
}
