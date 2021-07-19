package net.fabricmc.filament.task.lint;

import java.util.function.BiConsumer;

@FunctionalInterface
public interface ErrorReporter extends BiConsumer<Severity, String> {
	default void error(String message) {
		accept(Severity.ERROR, message);
	}

	default void warning(String message) {
		accept(Severity.WARNING, message);
	}
}
