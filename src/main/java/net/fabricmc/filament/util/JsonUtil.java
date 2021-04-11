package net.fabricmc.filament.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import com.squareup.moshi.Moshi;
import okio.Okio;

public class JsonUtil {
	private static final Moshi MOSHI = new Moshi.Builder().build();

	public static <T> T parseFromUrl(String url, Class<T> clazz) throws IOException {
		try (InputStream in = new URL(url).openStream()) {
			return MOSHI.adapter(clazz).fromJson(Okio.buffer(Okio.source(in)));
		}
	}
}
