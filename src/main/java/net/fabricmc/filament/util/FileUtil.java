package net.fabricmc.filament.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public final class FileUtil {
	private FileUtil() {
	}

	public static void deleteDirectory(File directory) throws IOException {
		for (File child : directory.listFiles()) {
			if (child.isDirectory()) {
				deleteDirectory(child);
			} else {
				if (!child.delete()) {
					throw new IOException("Could not delete file " + child.getName() + " in directory " + directory.getAbsolutePath());
				}
			}
		}

		if (!directory.delete()) {
			throw new IOException("Could not delete directory " + directory.getAbsolutePath());
		}
	}

	public static void deleteIfExists(File file) throws IOException {
		Files.deleteIfExists(file.toPath());
	}

	public static void write(File file, String content) throws IOException {
		Files.writeString(file.toPath(), content, StandardCharsets.UTF_8);
	}
}
