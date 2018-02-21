package de.zeiss.maven.osgi.targetplatform.plugin.internal;


public interface ParameterProvider {

    String getVersion();

    String getArtifactId();

    String getGroupId();

    String getOutputFile();

    String getAdditionalDependenciesFile();

    String getWhitelistFile();

    String getFeatureFile();

    String getTargetFeatureJarPrefix();

    String getEfxclipseSite();

    String getEfxclipseGenericRepositoryUrl();

}
