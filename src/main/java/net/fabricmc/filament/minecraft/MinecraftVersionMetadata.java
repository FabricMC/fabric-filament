package net.fabricmc.filament.minecraft;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("unused")
public final class MinecraftVersionMetadata {
	// Always use a specific OS here to ensure the inputs are always the same. This isn't an issue as it will never be ran
	public static final String OS = "linux";

	private Map<String, Object> arguments;
	private AssetIndex assetIndex;
	private String assets;
	private int complianceLevel;
	private Map<String, Download> downloads;
	private String id;
	private List<Library> libraries;
	private Map<String, Object> logging;
	private String mainClass;
	private int minimumLauncherVersion;
	private String releaseTime;
	private String time;
	private String type;

	public Download getDownload(String key) {
		return getDownloads().get(key);
	}

	public Map<String, Object> getArguments() {
		return arguments;
	}

	public AssetIndex getAssetIndex() {
		return assetIndex;
	}

	public String getAssets() {
		return assets;
	}

	public int getComplianceLevel() {
		return complianceLevel;
	}

	public Map<String, Download> getDownloads() {
		return downloads;
	}

	public String getId() {
		return id;
	}

	public List<Library> getLibraries() {
		return libraries;
	}

	public Map<String, Object> getLogging() {
		return logging;
	}

	public String getMainClass() {
		return mainClass;
	}

	public int getMinimumLauncherVersion() {
		return minimumLauncherVersion;
	}

	public String getReleaseTime() {
		return releaseTime;
	}

	public String getTime() {
		return time;
	}

	public String getType() {
		return type;
	}

	public static final class AssetIndex extends Downloadable {
		private String id;
		private long totalSize;

		public String getFabricId(String version) {
			return id.equals(version) ? version : version + "-" + id;
		}

		public String getId() {
			return id;
		}

		public long getTotalSize() {
			return totalSize;
		}
	}

	public static final class Download extends Downloadable {
	}

	public static final class Library {
		private Downloads downloads;
		private String name;
		private Map<String, String> natives;
		private List<Rule> rules;

		public boolean isValidForOS() {
			if (rules == null || rules.isEmpty()) {
				return true;
			}

			for (Rule rule : rules) {
				if (rule.appliesToOS() && !rule.isAllowed()) {
					return false;
				}
			}

			return true;
		}

		public boolean hasNatives() {
			return this.natives != null;
		}

		public boolean hasNativesForOS() {
			if (!hasNatives()) {
				return false;
			}

			if (natives.get(OS) == null) {
				return false;
			}

			return isValidForOS();
		}

		public Classifier getClassifierForOS() {
			return getDownloads().getClassifier(natives.get(OS));
		}

		public Downloads getDownloads() {
			return downloads;
		}

		public Artifact getArtifact() {
			if (getDownloads() == null) {
				return null;
			}

			return getDownloads().getArtifact();
		}

		public String getName() {
			return name;
		}

		public Map<String, String> getNatives() {
			return natives;
		}

		public List<Rule> getRules() {
			return rules;
		}
	}

	public static final class Downloads {
		private Artifact artifact;
		private Map<String, Classifier> classifiers;

		public Classifier getClassifier(String os) {
			return classifiers.get(os);
		}

		public Artifact getArtifact() {
			return artifact;
		}

		public Map<String, Classifier> getClassifiers() {
			return classifiers;
		}
	}

	public static final class Artifact extends Downloadable {
	}

	public static final class Classifier extends Downloadable {
	}

	public static final class Rule {
		private String action;
		private OS os;

		public boolean appliesToOS() {
			return getOS() == null || getOS().isValidForOS();
		}

		public boolean isAllowed() {
			return getAction().equals("allow");
		}

		public String getAction() {
			return action;
		}

		public OS getOS() {
			return os;
		}
	}

	public static final class OS {
		private String name;

		public boolean isValidForOS() {
			return getName() == null || getName().equalsIgnoreCase(OS);
		}

		public String getName() {
			return name;
		}
	}

	// A base class for everything that can be downloaded
	public abstract static class Downloadable {
		private String path;
		private String sha1;
		private long size;
		private String url;

		public File getRelativeFile(File baseDirectory) {
			Objects.requireNonNull(getPath(), "Cannot get relative file from a null path");
			return new File(baseDirectory, getPath());
		}

		public String getPath() {
			return path;
		}

		public String getSha1() {
			return sha1;
		}

		public long getSize() {
			return size;
		}

		public String getUrl() {
			return url;
		}
	}
}
