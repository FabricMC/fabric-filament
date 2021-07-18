package net.fabricmc.filament.task;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import javax.inject.Inject;

import cuchaz.enigma.Enigma;
import cuchaz.enigma.EnigmaProject;
import cuchaz.enigma.ProgressListener;
import cuchaz.enigma.analysis.index.EntryIndex;
import cuchaz.enigma.classprovider.ClasspathClassProvider;
import cuchaz.enigma.translation.mapping.EntryMapping;
import cuchaz.enigma.translation.mapping.serde.MappingParseException;
import cuchaz.enigma.translation.mapping.serde.enigma.EnigmaMappingsReader;
import cuchaz.enigma.translation.mapping.tree.EntryTree;
import cuchaz.enigma.translation.representation.AccessFlags;
import cuchaz.enigma.translation.representation.entry.ClassEntry;
import cuchaz.enigma.translation.representation.entry.Entry;
import cuchaz.enigma.translation.representation.entry.MethodEntry;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileType;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.work.ChangeType;
import org.gradle.work.FileChange;
import org.gradle.work.Incremental;
import org.gradle.work.InputChanges;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;
import org.gradle.workers.WorkQueue;
import org.gradle.workers.WorkerExecutor;

import net.fabricmc.filament.task.lint.Checker;
import net.fabricmc.filament.util.FileUtil;

public abstract class MappingLintTask extends DefaultTask {
	public MappingLintTask() {
		// Ignore outputs for up-to-date checks as there aren't any (so only inputs are checked)
		getOutputs().upToDateWhen(task -> true);
	}

	@Incremental
	@InputDirectory
	public abstract DirectoryProperty getMappingDirectory();

	/**
	 * The game jar in obfuscated/intermediary names (the source namespace).
	 */
	@InputFile
	public abstract RegularFileProperty getGameJar();

	@Input
	public abstract SetProperty<Checker<Entry<?>>> getCheckers();

	@Inject
	protected abstract WorkerExecutor getWorkerExecutor();

	@TaskAction
	public void run(InputChanges changes) {
		WorkQueue workQueue = getWorkerExecutor().noIsolation();

		workQueue.submit(LintAction.class, parameters -> {
			parameters.getGameJar().set(getGameJar());
			parameters.getCheckers().set(getCheckers());

			for (FileChange change : changes.getFileChanges(getMappingDirectory())) {
				if (change.getChangeType() != ChangeType.REMOVED && change.getFileType() == FileType.FILE) {
					parameters.getMappingFiles().from(change.getFile());
				}
			}
		});

		workQueue.await();
	}

	private static EntryTree<EntryMapping> readMappings(FileCollection files) throws IOException, MappingParseException {
		Path[] paths = FileUtil.toPaths(files).toArray(new Path[0]);
		return EnigmaMappingsReader.readFiles(ProgressListener.none(), paths);
	}

	private static String getFullName(EntryTree<EntryMapping> mappings, Entry<?> entry) {
		String name = mappings.get(entry).targetName();

		if (name == null) {
			name = "<anonymous>";
		}

		if (entry instanceof MethodEntry method) {
			name += method.getDesc().toString();
		}

		if (entry.getParent() != null) {
			name = getFullName(mappings, entry.getParent()) + '.' + name;
		}

		return name;
	}

	public interface LintParameters extends WorkParameters {
		ConfigurableFileCollection getMappingFiles();
		RegularFileProperty getGameJar();
		SetProperty<Checker<Entry<?>>> getCheckers();
	}

	public abstract static class LintAction implements WorkAction<LintParameters> {
		private static final Logger LOGGER = Logging.getLogger(LintAction.class);

		@Inject
		public LintAction() {
		}

		@Override
		public void execute() {
			try {
				LintParameters params = getParameters();
				Set<Checker<Entry<?>>> checkers = getParameters().getCheckers().get();
				List<String> errors = new ArrayList<>();

				// Set up Enigma
				Enigma enigma = Enigma.create();
				EnigmaProject project = enigma.openJar(params.getGameJar().get().getAsFile().toPath(), new ClasspathClassProvider(), ProgressListener.none());
				EntryTree<EntryMapping> mappings = readMappings(getParameters().getMappingFiles());
				project.setMappings(mappings);
				Function<Entry<?>, AccessFlags> accessProvider = entry -> {
					EntryIndex index = project.getJarIndex().getEntryIndex();

					if (entry instanceof ClassEntry c) {
						return index.getClassAccess(c);
					} else {
						return index.getEntryAccess(entry);
					}
				};

				mappings.getAllEntries().parallel().forEach(entry -> {
					EntryMapping mapping = mappings.get(entry);
					assert mapping != null;
					List<String> localErrors = new ArrayList<>();

					for (Checker<Entry<?>> checker : checkers) {
						checker.check(entry, mapping, accessProvider, localErrors::add);
					}

					if (!localErrors.isEmpty()) {
						String name = getFullName(mappings, entry);

						for (String error : localErrors) {
							errors.add(name + ": " + error);
						}
					}
				});

				if (!errors.isEmpty()) {
					for (String error : errors) {
						LOGGER.error("lint: {}", error);
					}

					throw new GradleException("Found " + errors.size() + " format errors! See the log for details.");
				}
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			} catch (MappingParseException e) {
				throw new GradleException("Could not parse mappings", e);
			}
		}
	}
}
