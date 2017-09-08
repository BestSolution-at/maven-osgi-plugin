package at.bestsolution.maven.osgi.pack;

import java.util.Map;

public class Product {
	public String uid;
	public String id;
	public String name;
	public String application;
	public String version;
	public boolean useFeatures;
	public boolean includeLaunchers;
	public LauncherArgs launcherArgs;
	protected Map<String, Integer> startLevels;
}
