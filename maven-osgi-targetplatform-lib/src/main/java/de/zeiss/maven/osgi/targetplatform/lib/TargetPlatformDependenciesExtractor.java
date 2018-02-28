package de.zeiss.maven.osgi.targetplatform.lib;

import java.io.InputStream;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.maven.model.Dependency;
import org.eclipse.pde.internal.core.ifeature.IFeaturePlugin;

import de.zeiss.maven.osgi.targetplatform.lib.internal.AdditionalDependencyProvider;
import de.zeiss.maven.osgi.targetplatform.lib.internal.FeaturePluginExtractor;
import de.zeiss.maven.osgi.targetplatform.lib.internal.FeaturePluginFilter;
import de.zeiss.maven.osgi.targetplatform.lib.internal.FeaturePluginToMavenDependencyConverter;
import de.zeiss.maven.osgi.targetplatform.lib.internal.JarAccessor;
import de.zeiss.maven.osgi.targetplatform.lib.internal.ParameterProvider;
import de.zeiss.maven.osgi.targetplatform.lib.internal.UpdateSiteAccessor;

/**
 * Responsible for accessing the target platform site and extracting all the dependencies from the feature.xml file included in the target platform jar. The
 * extracted dependencies will be provided as MavenDependencies.
 *
 */
public class TargetPlatformDependenciesExtractor {

    /**
     * Does the dependencies extraction.
     * 
     * @param parameterProvider
     *            contains the parameters
     * @return the maven dependencies
     */
    public static Set<Dependency> doMavenDependenciesGeneration(ParameterProvider parameterProvider) {
        String parentUrl = parameterProvider.getEfxclipseGenericRepositoryUrl();

        String relativeUrlToJarFile = UpdateSiteAccessor.readRelativeTargetPlatformFeatureJarUrl(parentUrl + "/" + parameterProvider.getEfxclipseSite(),
                parameterProvider.getTargetFeatureJarPrefix());

        InputStream featureFileInputStream = JarAccessor.readEntry(parentUrl + "/" + relativeUrlToJarFile, parameterProvider.getFeatureFile());

        Set<IFeaturePlugin> featurePlugins = FeaturePluginExtractor.extractFeaturePlugins(featureFileInputStream);

        InputStream resource = FeaturePluginFilter.class.getResourceAsStream(parameterProvider.getWhitelistFile());

        featurePlugins = featurePlugins.stream().filter(new FeaturePluginFilter(resource)).collect(Collectors.toSet());

        Set<Dependency> dependencies = FeaturePluginToMavenDependencyConverter.convert(featurePlugins);

        resource = FeaturePluginFilter.class.getResourceAsStream(parameterProvider.getAdditionalDependenciesFile());

        dependencies.addAll(AdditionalDependencyProvider.readAdditionalDependencies(resource));
        return dependencies;
    }
}
