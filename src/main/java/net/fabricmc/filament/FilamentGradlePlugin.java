package net.fabricmc.filament;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.BasePlugin;

import net.fabricmc.filament.task.CombineUnpickDefinitionsTask;
import net.fabricmc.filament.task.GeneratePackageInfoMappingsTask;
import net.fabricmc.filament.task.MappingLintTask;
import net.fabricmc.filament.task.RemapUnpickDefinitionsTask;
import net.fabricmc.filament.task.lint.Checker;

public final class FilamentGradlePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getTasks().register("generatePackageInfoMappings", GeneratePackageInfoMappingsTask.class);
		var mappingLint = project.getTasks().register("mappingLint", MappingLintTask.class, task -> {
			task.setGroup("verification");
			task.setDescription("Checks that all mappings follow the conventions.");
			task.getCheckers().addAll(Checker.DEFAULT_CHECKERS);
		});

		project.getPluginManager().withPlugin("org.gradle.base", p -> {
			project.getTasks().named(BasePlugin.CLEAN_TASK_NAME, check -> check.dependsOn(mappingLint));
		});

		var combineUnpickDefinitions = project.getTasks().register("combineUnpickDefinitions", CombineUnpickDefinitionsTask.class);
		project.getTasks().register("remapUnpickDefinitionsIntermediary", RemapUnpickDefinitionsTask.class, task -> {
			task.dependsOn(combineUnpickDefinitions);
			task.getInput().set(combineUnpickDefinitions.flatMap(CombineUnpickDefinitionsTask::getOutput));
			task.getSourceNamespace().set("named");
			task.getTargetNamespace().set("intermediary");
		});
	}
}
