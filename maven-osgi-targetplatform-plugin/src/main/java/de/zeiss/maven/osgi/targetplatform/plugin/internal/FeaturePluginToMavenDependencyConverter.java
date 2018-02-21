package de.zeiss.maven.osgi.targetplatform.plugin.internal;

import java.util.Set;
import java.util.stream.Collectors;

import org.apache.maven.model.Dependency;
import org.eclipse.pde.internal.core.ifeature.IFeaturePlugin;

/**
 * Responsible for converting the FeaturePlugins to MavenDependencies.
 *
 */
public class FeaturePluginToMavenDependencyConverter {

	private static final String FEATURE_PLUGIN_FX_INDICATOR = "org.eclipse.fx";
	private static final String FX_GROUP = "at.bestsolution.efxclipse.eclipse";
	private static final String RT_GROUP = "at.bestsolution.efxclipse.rt";

	/**
	 * Does the conversion of all featurePlugins.
	 * 
	 * @param featurePlugins
	 * @return
	 */
	public static Set<Dependency> convert(Set<IFeaturePlugin> featurePlugins) {
		return featurePlugins.stream().map(FeaturePluginToMavenDependencyConverter::convert)
				.collect(Collectors.toSet());
	}

	/**
	 * Does the conversion of one feature plugin.
	 * 
	 * @param featurePlugin
	 * @return
	 */
	private static Dependency convert(IFeaturePlugin featurePlugin) {
		Dependency dependency = new Dependency();
		dependency.setGroupId(computeGroupId(featurePlugin));
		dependency.setArtifactId(featurePlugin.getId());
		dependency.setVersion(computeVersion(featurePlugin));
		
		if (dependency.getArtifactId().equals("com.google.guava")){
			dependency.setVersion("21.0.0");
		}
		return dependency;
	}

	private static String computeVersion(IFeaturePlugin featurePlugin) {
		return featurePlugin.getVersion().substring(0, featurePlugin.getVersion().lastIndexOf("."));
	}

	private static String computeGroupId(IFeaturePlugin featurePlugin) {
		if (featurePlugin.getId().indexOf(FEATURE_PLUGIN_FX_INDICATOR) == -1) {
			return FX_GROUP;
		} else {
			return RT_GROUP;
		}
	}

}
