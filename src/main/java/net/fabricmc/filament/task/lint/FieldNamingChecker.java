package net.fabricmc.filament.task.lint;

import java.util.function.Function;

import cuchaz.enigma.translation.mapping.EntryMapping;
import cuchaz.enigma.translation.representation.AccessFlags;
import cuchaz.enigma.translation.representation.entry.Entry;
import cuchaz.enigma.translation.representation.entry.FieldEntry;

public final class FieldNamingChecker implements Checker<FieldEntry> {
	@Override
	public void check(FieldEntry entry, EntryMapping mapping, Function<Entry<?>, AccessFlags> accessProvider, ErrorReporter errorReporter) {
		AccessFlags access = accessProvider.apply(entry);

		if (access.isStatic() && access.isFinal()) {
			if (!isConstantCase(mapping.targetName())) {
				errorReporter.error("static final field is not in CONSTANT_CASE");
			}
		} else {
			if (startsWithUppercase(mapping.targetName())) {
				errorReporter.warning("non-static or non-final field starts with uppercase character '" + mapping.targetName().charAt(0) + "'");
			}
		}
	}

	private static boolean isConstantCase(String s) {
		for (char c : s.toCharArray()) {
			if (Character.isLowerCase(c)) {
				return false;
			}
		}

		return true;
	}

	private static boolean startsWithUppercase(String s) {
		return Character.isUpperCase(s.charAt(0));
	}
}
