package de.zeiss.maven.osgi.targetplatform.lib.internal;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Set;

import org.eclipse.pde.internal.core.ifeature.IFeaturePlugin;
import org.junit.Test;

import de.zeiss.maven.osgi.targetplatform.lib.internal.FeaturePluginExtractor;


public class FeaturePluginExtractorTest {

	private static final String FEATURE_VERSION = "1.0.0.11111";
	private static final String FEATURE_ID = "my.plugin.id";
	private static final String FEATURE_TEST_XML = "/feature-test.xml";

	@Test
	public void testExtractFeaturePlugins() {
		Set<IFeaturePlugin> featurePlugins = FeaturePluginExtractor
				.extractFeaturePlugins(getClass().getResourceAsStream(FEATURE_TEST_XML));

		assertThat(featurePlugins.size(), equalTo(1));

		if (featurePlugins.size() == 1) {

			IFeaturePlugin featurePlugin = featurePlugins.iterator().next();

			assertThat(featurePlugin.getId(), equalTo(FEATURE_ID));
			assertThat(featurePlugin.getVersion(), equalTo(FEATURE_VERSION));

		}
	}

}
