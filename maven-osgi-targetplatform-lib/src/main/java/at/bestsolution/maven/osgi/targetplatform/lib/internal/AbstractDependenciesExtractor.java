package at.bestsolution.maven.osgi.targetplatform.lib.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.maven.model.Dependency;
import org.eclipse.pde.internal.core.ifeature.IFeaturePlugin;

import at.bestsolution.maven.osgi.targetplatform.lib.LoggingSupport;
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

        Proxy javaProxy = getParameterProvider().getProxy();

        String relativeUrlToJarFile =  UpdateSiteAccessor.readRelativeTargetPlatformFeatureJarUrl(parentUrl + "/" + getParameterProvider().getEfxclipseSite(),
                getParameterProvider().getTargetFeatureJarPrefix(), javaProxy);

        InputStream featureFileInputStream = JarAccessor.readEntry(parentUrl + "/" + relativeUrlToJarFile, getParameterProvider().getFeatureFile());

        Set<IFeaturePlugin> featurePlugins = FeaturePluginExtractor.extractFeaturePlugins(featureFileInputStream);

        Set<Dependency> dependencies = new HashSet<>();

        try (
                InputStream whiteListFile = openWhiteListFile();
                InputStream additionalDepenceniesFile = openAdditionalDepenceniesFile()
        ) {

            featurePlugins = featurePlugins.stream().filter(new FeaturePluginFilter(whiteListFile)).collect(Collectors.toSet());
            dependencies = FeaturePluginToMavenDependencyConverter.convert(featurePlugins);

            dependencies.addAll(AdditionalDependencyProvider.readAdditionalDependencies(additionalDepenceniesFile));

        } catch (IOException e) {
            LoggingSupport.logErrorMessage("Error on reading/closing  whitelist file or additional depencies file");
        }

        return dependencies;
    }

    private InputStream openAdditionalDepenceniesFile() {
        return FeaturePluginFilter.class.getResourceAsStream(getParameterProvider().getAdditionalDependenciesFile());
    }

    private InputStream openWhiteListFile() {
        return FeaturePluginFilter.class.getResourceAsStream(getParameterProvider().getWhitelistFile());
    }
}
