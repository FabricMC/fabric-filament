package net.fabricmc.filament.minecraft;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MinecraftVersionManifest {
	private Map<String, String> latest;
	private List<Version> versions;

	public Map<String, String> getLatest() {
		return latest;
	}

	public List<Version> getVersions() {
		return versions;
	}

	public Optional<Version> getVersion(String id) {
		return getVersions().stream()
				.filter(version -> version.getId().equals(id))
				.findAny();
	}

	public static class Version {
		String id;
		String type;
		String url;
		String time;
		String releaseTime;
		String sha1;
		int complianceLevel;

		public String getId() {
			return id;
		}

		public String getType() {
			return type;
		}

		public String getUrl() {
			return url;
		}

		public String getTime() {
			return time;
		}

		public String getReleaseTime() {
			return releaseTime;
		}

		public String getSha1() {
			return sha1;
		}

		public int getComplianceLevel() {
			return complianceLevel;
		}
	}
}
