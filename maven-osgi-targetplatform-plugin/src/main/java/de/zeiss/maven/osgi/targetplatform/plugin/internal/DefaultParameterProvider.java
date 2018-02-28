package de.zeiss.maven.osgi.targetplatform.plugin.internal;

public class DefaultParameterProvider implements ExtendedParameterProvider {

    private String version;
    private String artifactId;
    private String groupId;
    private String outputFile;
    private String additionalDependenciesFile;
    private String whitelistFile;
    private String featureFile;
    private String targetFeatureJarPrefix;
    private String efxclipseSite;
    private String efxclipseGenericRepositoryUrl;

    public DefaultParameterProvider(String version, String artifactId, String groupId, String outputFile, String additionalDependenciesFile,
            String whitelistFile, String featureFile, String targetFeatureJarPrefix, String efxclipseSite, String efxclipseGenericRepositoryUrl) {
        this.version = version;
        this.artifactId = artifactId;
        this.groupId = groupId;
        this.outputFile = outputFile;
        this.additionalDependenciesFile = additionalDependenciesFile;
        this.whitelistFile = whitelistFile;
        this.featureFile = featureFile;
        this.targetFeatureJarPrefix = targetFeatureJarPrefix;
        this.efxclipseSite = efxclipseSite;
        this.efxclipseGenericRepositoryUrl = efxclipseGenericRepositoryUrl;
    }

    @Override
    public String getVersion() {

        return version;
    }

    @Override
    public String getArtifactId() {

        return artifactId;
    }

    @Override
    public String getGroupId() {

        return groupId;
    }

    @Override
    public String getOutputFile() {

        return outputFile;
    }

    @Override
    public String getAdditionalDependenciesFile() {

        return additionalDependenciesFile;
    }

    @Override
    public String getWhitelistFile() {

        return whitelistFile;
    }

    @Override
    public String getFeatureFile() {

        return featureFile;
    }

    @Override
    public String getTargetFeatureJarPrefix() {

        return targetFeatureJarPrefix;
    }

    @Override
    public String getEfxclipseSite() {

        return efxclipseSite;
    }

    @Override
    public String getEfxclipseGenericRepositoryUrl() {
        return efxclipseGenericRepositoryUrl;
    }

}
