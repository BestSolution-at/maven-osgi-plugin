package de.zeiss.maven.osgi.targetplatform.lib.internal;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Set;

import org.apache.maven.model.Dependency;
import org.junit.Test;

import de.zeiss.maven.osgi.targetplatform.lib.internal.AdditionalDependencyProvider;
import de.zeiss.maven.osgi.targetplatform.lib.internal.FeaturePluginFilter;

public class AdditionalDependencyProviderTest {

    private static final String ADDITIONAL_DEPENDENCIES_FILE = "/additional-dependencies-test.txt";
    private static final String VERSION = "1.0.200";
    private static final String ARTIFACT_ID = "org.eclipse.osgi.compatibility.state";
    private static final String GROUP_ID = "at.bestsolution.efxclipse.eclipse";

    @Test
    public void testDependencyAdder() {

        InputStream resource = FeaturePluginFilter.class.getResourceAsStream(ADDITIONAL_DEPENDENCIES_FILE);

        Set<Dependency> dependencies = AdditionalDependencyProvider.readAdditionalDependencies(resource);

        assertThat(dependencies.size(), equalTo(1));

        if (dependencies.size() == 1) {

            Dependency dependency = dependencies.iterator().next();

            assertThat(dependency.getGroupId(), equalTo(GROUP_ID));
            assertThat(dependency.getArtifactId(), equalTo(ARTIFACT_ID));
            assertThat(dependency.getVersion(), equalTo(VERSION));

        }
    }
}
