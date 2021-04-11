package net.fabricmc.filament.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonUtil {
	public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	public static <T> T parseFromUrl(String url, Class<T> clazz) throws IOException {
		try (Reader reader = new BufferedReader(new InputStreamReader(new URL(url).openStream()))) {
			return GSON.fromJson(reader, clazz);
		}
	}
}
