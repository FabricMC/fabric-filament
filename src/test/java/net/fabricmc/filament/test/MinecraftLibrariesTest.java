package net.fabricmc.filament.test;

import static org.assertj.core.api.Assertions.assertThat;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.Test;

public class MinecraftLibrariesTest extends ProjectTest {
	private BuildResult runGradleBuild() {
		GradleRunner runner = GradleRunner.create()
				.withPluginClasspath()
				.withProjectDir(projectDirectory)
				.withArguments("printLibraries");

		return runner.build();
	}

	@Test
	public void printLibraries() {
		setupProject("minecraftLibraries");
		BuildResult result = runGradleBuild();

		assertThat(result.task(":printLibraries").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
		assertThat(result.getOutput()).contains("datafixerupper-4.0.26.jar"); // Just test we have at least this lib.
	}
}
