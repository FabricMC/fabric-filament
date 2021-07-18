package net.fabricmc.filament.task.lint;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;

import cuchaz.enigma.translation.mapping.EntryMapping;
import cuchaz.enigma.translation.representation.AccessFlags;
import cuchaz.enigma.translation.representation.entry.Entry;
import cuchaz.enigma.translation.representation.entry.LocalVariableEntry;
import cuchaz.enigma.translation.representation.entry.MethodEntry;

public final class JavadocChecker implements Checker<Entry<?>> {
	private static final Pattern PARAM_DOC_LINE = Pattern.compile("^@param\\s+[^<].*$");

	@Override
	public void check(Entry<?> entry, EntryMapping mapping, Function<Entry<?>, AccessFlags> accessProvider, Consumer<String> errorConsumer) {
		String javadoc = mapping.javadoc();

		if (javadoc != null && !javadoc.isEmpty()) {
			if (entry instanceof LocalVariableEntry lv && lv.isArgument()) {
				if (javadoc.endsWith(".")) {
					errorConsumer.accept("parameter javadoc ends with '.'");
				}

				if (Character.isUpperCase(javadoc.charAt(0))) {
					String word = getFirstWord(javadoc);

					// ignore single-letter "words" (like X or Z)
					if (word.length() > 1) {
						errorConsumer.accept("parameter javadoc starts with uppercase word '" + word + "'");
					}
				}
			} else if (entry instanceof MethodEntry) {
				if (javadoc.lines().anyMatch(JavadocChecker::isRegularMethodParameter)) {
					errorConsumer.accept("method javadoc contains parameter docs, which should be on the parameter itself");
				}
			}
		}
	}

	private static boolean isRegularMethodParameter(String line) {
		return PARAM_DOC_LINE.matcher(line).matches();
	}

	private static String getFirstWord(String str) {
		int i = str.indexOf(' ');
		return i != -1 ? str.substring(0, i) : str;
	}
}
