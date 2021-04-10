package net.fabricmc.filament.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.io.TempDir;

abstract class ProjectTest {
	@TempDir
	protected File projectDirectory;

	protected final void setupProject(String name, String... extraFiles) {
		try {
			copyProjectFile(name, "build.gradle");
			copyProjectFile(name, "settings.gradle");

			for (String file : extraFiles) {
				copyProjectFile(name, file);
			}
		} catch (IOException e) {
			throw new UncheckedIOException("Could not set up test for project " + name, e);
		}
	}

	private void copyProjectFile(String projectName, String file) throws IOException {
		try (InputStream in = ProjectTest.class.getResourceAsStream("/projects/" + projectName + '/' + file)) {
			Path target = projectDirectory.toPath().resolve(file);
			Files.createDirectories(target.getParent());
			Files.copy(in, target);
		}
	}
}
