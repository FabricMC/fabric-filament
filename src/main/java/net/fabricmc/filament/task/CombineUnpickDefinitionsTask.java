package net.fabricmc.filament.task;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import daomephsta.unpick.constantmappers.datadriven.parser.v2.UnpickV2Reader;
import daomephsta.unpick.constantmappers.datadriven.parser.v2.UnpickV2Writer;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import net.fabricmc.filament.util.FileUtil;

public class CombineUnpickDefinitionsTask extends DefaultTask {
	private final DirectoryProperty input = getProject().getObjects().directoryProperty();
	private final RegularFileProperty output = getProject().getObjects().fileProperty();

	@InputDirectory
	public DirectoryProperty getInput() {
		return input;
	}

	@OutputFile
	public RegularFileProperty getOutput() {
		return output;
	}

	@TaskAction
	public void run() throws IOException {
		File output = getOutput().getAsFile().get();
		FileUtil.deleteIfExists(output);

		UnpickV2Writer writer = new UnpickV2Writer();

		for (File file : getInput().getAsFileTree().getFiles()) {
			if (!file.getName().endsWith(".unpick")) {
				continue;
			}

			try (UnpickV2Reader reader = new UnpickV2Reader(new FileInputStream(file))) {
				reader.accept(writer);
			}
		}

		FileUtil.write(output, writer.getOutput());
	}
}
