package de.zeiss.maven.osgi.targetplatform.lib.internal;


/**
 * Provides all necessary parameters.
 * 
 *
 */
public interface ParameterProvider {

    String getVersion();

    String getArtifactId();

    String getGroupId();

    String getAdditionalDependenciesFile();

    String getWhitelistFile();

    String getFeatureFile();

    String getTargetFeatureJarPrefix();

    String getEfxclipseSite();

    String getEfxclipseGenericRepositoryUrl();

}
