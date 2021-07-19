package net.fabricmc.filament.test;

import static org.assertj.core.api.Assertions.assertThat;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.Test;

class FieldNamingLintTest extends ProjectTest {
	private BuildResult runGradleBuild(boolean shouldSucceed) {
		GradleRunner runner = GradleRunner.create()
				.withPluginClasspath()
				.withProjectDir(projectDirectory)
				.withArguments("mappingLint");

		return shouldSucceed ? runner.build() : runner.buildAndFail();
	}

	@Test
	public void staticFinalLowercase() {
		setupProject("fieldNamingLint", "mappings/StaticFinalLowercase.mapping");
		BuildResult result = runGradleBuild(false);

		assertThat(result.task(":mappingLint").getOutcome()).isEqualTo(TaskOutcome.FAILED);
		assertThat(result.getOutput()).contains("static final field is not in CONSTANT_CASE");
	}

	@Test
	public void instanceFinalUppercase() {
		setupProject("fieldNamingLint", "mappings/InstanceFinalUppercase.mapping");
		BuildResult result = runGradleBuild(true);

		assertThat(result.task(":mappingLint").getOutcome()).isEqualTo(TaskOutcome.SUCCESS); // soft warning
		assertThat(result.getOutput()).contains("field starts with uppercase character 'I'");
	}

	@Test
	public void staticNonFinalUppercase() {
		setupProject("fieldNamingLint", "mappings/StaticNonFinalUppercase.mapping");
		BuildResult result = runGradleBuild(true);

		assertThat(result.task(":mappingLint").getOutcome()).isEqualTo(TaskOutcome.SUCCESS); // soft warning
		assertThat(result.getOutput()).contains("field starts with uppercase character 'S'");
	}

	@Test
	public void multipleErrors() {
		setupProject(
				"fieldNamingLint",
				"mappings/StaticFinalLowercase.mapping",
				"mappings/InstanceFinalUppercase.mapping",
				"mappings/StaticNonFinalUppercase.mapping"
		);
		BuildResult result = runGradleBuild(false);

		assertThat(result.task(":mappingLint").getOutcome()).isEqualTo(TaskOutcome.FAILED);
		assertThat(result.getOutput()).contains("Found 1 errors and 2 warnings");
	}

	@Test
	public void successful() {
		setupProject("fieldNamingLint", "mappings/Successful.mapping");
		BuildResult result = runGradleBuild(true);

		assertThat(result.task(":mappingLint").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
	}
}
