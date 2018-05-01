package at.bestsolution.maven.osgi.targetplatform.lib;

import at.bestsolution.maven.osgi.targetplatform.lib.internal.AbstractDependenciesExtractor;

/**
 * Responsible for accessing the target platform site and extracting all the dependencies from the feature.xml file included in the target platform jar. The
 * extracted dependencies will be provided as MavenDependencies.
 *
 */
public class TargetPlatformDependenciesExtractor  extends AbstractDependenciesExtractor{

    private final at.bestsolution.maven.osgi.targetplatform.lib.ParameterProvider parameterProvider;

    public TargetPlatformDependenciesExtractor(ParameterProvider parameterProvider) {
        this.parameterProvider = parameterProvider;
    }

    @Override
    protected ParameterProvider getParameterProvider() {
        return parameterProvider;
    }
   
}
