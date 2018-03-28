package at.bestsolution.maven.osgi.targetplatform.lib.internal;

import java.io.InputStream;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.maven.model.Dependency;
import org.eclipse.pde.internal.core.ifeature.IFeaturePlugin;

import at.bestsolution.maven.osgi.targetplatform.lib.ParameterProvider;

public abstract class AbstractDependenciesExtractor {

    protected abstract ParameterProvider getParameterProvider();

    /**
     * Does the dependencies extraction.
     * 
     * @param parameterProvider
     *            contains the parameters
     * @return the maven dependencies
     */
    public Set<Dependency> doMavenDependenciesGeneration() {
        String parentUrl = getParameterProvider().getEfxclipseUpdateSite();

        String relativeUrlToJarFile = UpdateSiteAccessor.readRelativeTargetPlatformFeatureJarUrl(parentUrl + "/" + getParameterProvider().getEfxclipseSite(),
                getParameterProvider().getTargetFeatureJarPrefix());

        InputStream featureFileInputStream = JarAccessor.readEntry(parentUrl + "/" + relativeUrlToJarFile, getParameterProvider().getFeatureFile());

        Set<IFeaturePlugin> featurePlugins = FeaturePluginExtractor.extractFeaturePlugins(featureFileInputStream);

        InputStream resource = FeaturePluginFilter.class.getResourceAsStream(getParameterProvider().getWhitelistFile());

        featurePlugins = featurePlugins.stream().filter(new FeaturePluginFilter(resource)).collect(Collectors.toSet());

        Set<Dependency> dependencies = FeaturePluginToMavenDependencyConverter.convert(featurePlugins);

        resource = FeaturePluginFilter.class.getResourceAsStream(getParameterProvider().getAdditionalDependenciesFile());

        dependencies.addAll(AdditionalDependencyProvider.readAdditionalDependencies(resource));
        return dependencies;
    }
}
