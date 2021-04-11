package net.fabricmc.filament.task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import daomephsta.unpick.constantmappers.datadriven.parser.FieldKey;
import daomephsta.unpick.constantmappers.datadriven.parser.MethodKey;
import daomephsta.unpick.constantmappers.datadriven.parser.v2.UnpickV2Reader;
import daomephsta.unpick.constantmappers.datadriven.parser.v2.UnpickV2Remapper;
import daomephsta.unpick.constantmappers.datadriven.parser.v2.UnpickV2Writer;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import net.fabricmc.filament.util.FileUtil;
import net.fabricmc.mapping.tree.ClassDef;
import net.fabricmc.mapping.tree.FieldDef;
import net.fabricmc.mapping.tree.MethodDef;
import net.fabricmc.mapping.tree.TinyMappingFactory;
import net.fabricmc.mapping.tree.TinyTree;

public class RemapUnpickDefinitionsTask extends DefaultTask {
	private final RegularFileProperty input = getProject().getObjects().fileProperty();
	private final RegularFileProperty mappings = getProject().getObjects().fileProperty();
	private final Property<String> sourceNamespace = getProject().getObjects().property(String.class);
	private final Property<String> targetNamespace = getProject().getObjects().property(String.class);
	private final RegularFileProperty output = getProject().getObjects().fileProperty();

	@InputFile
	public RegularFileProperty getInput() {
		return input;
	}

	@InputFile
	public RegularFileProperty getMappings() {
		return mappings;
	}

	@Input
	public Property<String> getSourceNamespace() {
		return sourceNamespace;
	}

	@Input
	public Property<String> getTargetNamespace() {
		return targetNamespace;
	}

	@OutputFile
	public RegularFileProperty getOutput() {
		return output;
	}

	@TaskAction
	public void run() throws IOException {
		File output = getOutput().getAsFile().get();
		FileUtil.deleteIfExists(output);

		Map<String, String> classMappings = new HashMap<>();
		Map<MethodKey, String> methodMappings = new HashMap<>();
		Map<FieldKey, String> fieldMappings = new HashMap<>();
		String fromM = getSourceNamespace().get();
		String toM = getTargetNamespace().get();

		try (BufferedReader reader = new BufferedReader(new FileReader(getMappings().getAsFile().get()))) {
			TinyTree tinyTree = TinyMappingFactory.loadWithDetection(reader);

			for (ClassDef classDef : tinyTree.getClasses()) {
				classMappings.put(classDef.getName(fromM), classDef.getName(toM));

				for (MethodDef methodDef : classDef.getMethods()) {
					methodMappings.put(
							new MethodKey(classDef.getName(fromM), methodDef.getName(fromM), methodDef.getDescriptor(fromM)),
							methodDef.getName(toM)
					);
				}

				for (FieldDef fieldDef : classDef.getFields()) {
					fieldMappings.put(
							new FieldKey(classDef.getName(fromM), fieldDef.getName(fromM)),
							fieldDef.getName(toM)
					);
				}
			}
		}

		try (UnpickV2Reader reader = new UnpickV2Reader(new FileInputStream(getInput().getAsFile().get()))) {
			UnpickV2Writer writer = new UnpickV2Writer();
			reader.accept(new UnpickV2Remapper(classMappings, methodMappings, fieldMappings, writer));
			FileUtil.write(output, writer.getOutput());
		}
	}
}
