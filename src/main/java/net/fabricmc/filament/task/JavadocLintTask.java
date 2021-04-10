package net.fabricmc.filament.task;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import cuchaz.enigma.ProgressListener;
import cuchaz.enigma.translation.mapping.EntryMapping;
import cuchaz.enigma.translation.mapping.serde.MappingFileNameFormat;
import cuchaz.enigma.translation.mapping.serde.MappingParseException;
import cuchaz.enigma.translation.mapping.serde.MappingSaveParameters;
import cuchaz.enigma.translation.mapping.serde.enigma.EnigmaMappingsReader;
import cuchaz.enigma.translation.mapping.tree.EntryTree;
import cuchaz.enigma.translation.representation.entry.Entry;
import cuchaz.enigma.translation.representation.entry.LocalVariableEntry;
import cuchaz.enigma.translation.representation.entry.MethodEntry;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.TaskAction;

public class JavadocLintTask extends DefaultTask {
	private static final Pattern PARAM_DOC_LINE = Pattern.compile("^@param\\s+[^<].*$");
	private final DirectoryProperty mappingDirectory = getProject().getObjects().directoryProperty();

	@InputDirectory
	public DirectoryProperty getMappingDirectory() {
		return mappingDirectory;
	}

	public JavadocLintTask() {
		// Ignore outputs for up-to-date checks as there aren't any (so only inputs are checked)
		getOutputs().upToDateWhen(task -> true);
	}

	@TaskAction
	public void run() throws IOException, MappingParseException {
		var saveParameters = new MappingSaveParameters(MappingFileNameFormat.BY_DEOBF);
		Path directory = mappingDirectory.getAsFile().get().toPath();
		EntryTree<EntryMapping> mappings = EnigmaMappingsReader.DIRECTORY.read(directory, ProgressListener.none(), saveParameters);
		List<String> errors = new ArrayList<>();

		mappings.getAllEntries().parallel().forEach(entry -> {
			EntryMapping mapping = mappings.get(entry);
			String javadoc = mapping.getJavadoc();

			if (javadoc != null && !javadoc.isEmpty()) {
				List<String> localErrors = new ArrayList<>();

				if (entry instanceof LocalVariableEntry && ((LocalVariableEntry) entry).isArgument()) {
					if (javadoc.endsWith(".")) {
						localErrors.add("parameter javadoc ends with '.'");
					}

					if (Character.isUpperCase(javadoc.charAt(0))) {
						String word = getFirstWord(javadoc);

						// ignore single-letter "words" (like X or Z)
						if (word.length() > 1) {
							localErrors.add("parameter javadoc starts with uppercase word '" + word + "'");
						}
					}
				} else if (entry instanceof MethodEntry) {
					if (javadoc.lines().anyMatch(JavadocLintTask::isRegularMethodParameter)) {
						localErrors.add("method javadoc contains parameter docs, which should be on the parameter itself");
					}
				}

				// new rules can be added here in the future

				if (!localErrors.isEmpty()) {
					String name = getFullName(mappings, entry);

					for (String error : localErrors) {
						errors.add(name + ": " + error);
					}
				}
			}
		});

		if (!errors.isEmpty()) {
			for (String error : errors) {
				getLogger().error("lint: {}", error);
			}

			throw new GradleException("Found " + errors.size() + " javadoc format errors! See the log for details.");
		}
	}

	private static boolean isRegularMethodParameter(String line) {
		return PARAM_DOC_LINE.matcher(line).matches();
	}

	private static String getFirstWord(String str) {
		int i = str.indexOf(' ');
		return i != -1 ? str.substring(0, i) : str;
	}

	private static String getFullName(EntryTree<EntryMapping> mappings, Entry<?> entry) {
		String name = mappings.get(entry).getTargetName();

		if (entry.getParent() != null) {
			name = getFullName(mappings, entry.getParent()) + '.' + name;
		}

		return name;
	}
}
