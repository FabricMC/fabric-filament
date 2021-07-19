package net.fabricmc.filament.task.lint;

import java.io.Serializable;
import java.util.Set;
import java.util.function.Function;

import cuchaz.enigma.translation.mapping.EntryMapping;
import cuchaz.enigma.translation.representation.AccessFlags;
import cuchaz.enigma.translation.representation.entry.Entry;
import cuchaz.enigma.translation.representation.entry.FieldEntry;

@FunctionalInterface
public interface Checker<E extends Entry<?>> extends Serializable {
	Set<Checker<Entry<?>>> DEFAULT_CHECKERS = Set.of(
			new JavadocChecker(),
			new FieldNamingChecker().withTypeGuard(FieldEntry.class)
	);

	void check(E entry, EntryMapping mapping, Function<Entry<?>, AccessFlags> accessProvider, ErrorReporter errorReporter);

	default Checker<Entry<?>> withTypeGuard(Class<E> entryType) {
		return (entry, mapping, access, errorReporter) -> {
			if (entryType.isInstance(entry)) {
				check(entryType.cast(entry), mapping, access, errorReporter);
			}
		};
	}
}
