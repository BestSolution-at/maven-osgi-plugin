package at.bestsolution.maven.osgi.targetplatform.lib.internal;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.pde.internal.core.ifeature.IFeaturePlugin;
import org.junit.Test;
import org.mockito.Mockito;

import at.bestsolution.maven.osgi.targetplatform.lib.internal.FeaturePluginFilter;

public class FeaturePluginFilterTest {

    private static final String WHITELIST_TEST_FILE_NAME = "/whitelist-test.txt";

    private static final String FEATURE_PLUGIN_ID_1 = "org.eclipse.something";
    private static final String FEATURE_PLUGIN_VERSION_1 = "1.0.0";
    private static final String FEATURE_PLUGIN_ID_2 = "org.maven.other";
    private static final String FEATURE_PLUGIN_VERSION_2 = "2.0.0";

    @Test
    public void testFilter() {

        IFeaturePlugin featurePlugin1 = Mockito.mock(IFeaturePlugin.class);
        Mockito.when(featurePlugin1.getId()).thenReturn(FEATURE_PLUGIN_ID_1);
        Mockito.when(featurePlugin1.getVersion()).thenReturn(FEATURE_PLUGIN_VERSION_1);

        IFeaturePlugin featurePlugin2 = Mockito.mock(IFeaturePlugin.class);
        Mockito.when(featurePlugin2.getId()).thenReturn(FEATURE_PLUGIN_ID_2);
        Mockito.when(featurePlugin2.getVersion()).thenReturn(FEATURE_PLUGIN_VERSION_2);

        Set<IFeaturePlugin> input = new HashSet<>(Arrays.asList(featurePlugin1, featurePlugin2));

        InputStream resource = FeaturePluginFilter.class.getResourceAsStream(WHITELIST_TEST_FILE_NAME);
        

        Set<IFeaturePlugin> filteredSet = input.stream().filter(new FeaturePluginFilter( resource)).collect(Collectors.toSet());

        assertThat(filteredSet.size(), equalTo(1));

        if (filteredSet.size() == 1) {

            IFeaturePlugin resultingFeaturePlugin = filteredSet.iterator().next();

            assertThat(resultingFeaturePlugin.getId(), equalTo(FEATURE_PLUGIN_ID_1));
            assertThat(resultingFeaturePlugin.getVersion(), equalTo(FEATURE_PLUGIN_VERSION_1));
        }

    }
}
