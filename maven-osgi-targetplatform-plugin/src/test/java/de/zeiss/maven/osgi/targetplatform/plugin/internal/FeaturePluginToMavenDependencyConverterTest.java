package de.zeiss.maven.osgi.targetplatform.plugin.internal;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.maven.model.Dependency;
import org.eclipse.pde.internal.core.ifeature.IFeaturePlugin;
import org.junit.Test;
import org.mockito.Mockito;

public class FeaturePluginToMavenDependencyConverterTest {

	 private final String FEATURE_PLUGIN_ID_FX="org.eclipse.fx.ui.keybindings";
	 private final String FEATURE_PLUGIN_VERSION_FX ="3.0.0.201706050601";
	 
	 private final String FEATURE_PLUGIN_ID_NON_FX = "org.apache.felix.gogo.command";
	 private final String FEATURE_PLUGIN_VERSION_NON_FX = "0.10.0.v201209301215";
	
	
	@Test
	public void testConversionForFXDependency() {
		
		IFeaturePlugin featurePlugin = Mockito.mock(IFeaturePlugin.class);
		Mockito.when(featurePlugin.getId()).thenReturn(FEATURE_PLUGIN_ID_FX);
		Mockito.when(featurePlugin.getVersion()).thenReturn(FEATURE_PLUGIN_VERSION_FX);
		
		Set<Dependency> dependencies = FeaturePluginToMavenDependencyConverter.convert(new HashSet<>(Arrays.asList(featurePlugin)));
		
		
		assertThat(dependencies.size(), equalTo(1));

		if (dependencies.size() == 1) {

			Dependency dependency = dependencies.iterator().next();

			assertThat(dependency.getGroupId(), equalTo("at.bestsolution.efxclipse.rt"));
			assertThat(dependency.getArtifactId(), equalTo(FEATURE_PLUGIN_ID_FX));
			assertThat(dependency.getVersion(), equalTo("3.0.0"));
			
		}
	}
	
	
	@Test
	public void testConversionForNonFXDependency() {
		
		IFeaturePlugin featurePlugin = Mockito.mock(IFeaturePlugin.class);
		Mockito.when(featurePlugin.getId()).thenReturn(FEATURE_PLUGIN_ID_NON_FX);
		Mockito.when(featurePlugin.getVersion()).thenReturn(FEATURE_PLUGIN_VERSION_NON_FX);
		
		Set<Dependency> dependencies = FeaturePluginToMavenDependencyConverter.convert(new HashSet<>(Arrays.asList(featurePlugin)));
		
		
		assertThat(dependencies.size(), equalTo(1));

		if (dependencies.size() == 1) {

			Dependency dependency = dependencies.iterator().next();

			assertThat(dependency.getGroupId(), equalTo("at.bestsolution.efxclipse.eclipse"));
			assertThat(dependency.getArtifactId(), equalTo(FEATURE_PLUGIN_ID_NON_FX));
			assertThat(dependency.getVersion(), equalTo("0.10.0"));
			
		}
	}
}
