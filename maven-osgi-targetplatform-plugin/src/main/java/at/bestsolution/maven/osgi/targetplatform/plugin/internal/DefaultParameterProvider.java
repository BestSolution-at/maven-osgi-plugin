package at.bestsolution.maven.osgi.targetplatform.plugin.internal;

public class DefaultParameterProvider implements ExtendedParameterProvider {

    private final String version;
    private final String artifactId;
    private final String groupId;
    private final String outputFile;
    private final String additionalDependenciesFile;
    private final String whitelistFile;
    private final String featureFile;
    private final String targetFeatureJarPrefix;
    private final String efxclipseSite;
    private final String efxclipseUpdateSite;

    public DefaultParameterProvider(String version, String artifactId, String groupId, String outputFile, String additionalDependenciesFile,
            String whitelistFile, String featureFile, String targetFeatureJarPrefix, String efxclipseSite, String efxclipseUpdateSite) {
        this.version = version;
        this.artifactId = artifactId;
        this.groupId = groupId;
        this.outputFile = outputFile;
        this.additionalDependenciesFile = additionalDependenciesFile;
        this.whitelistFile = whitelistFile;
        this.featureFile = featureFile;
        this.targetFeatureJarPrefix = targetFeatureJarPrefix;
        this.efxclipseSite = efxclipseSite;
        this.efxclipseUpdateSite = efxclipseUpdateSite;
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
    public String getEfxclipseUpdateSite() {
        return efxclipseUpdateSite;
    }

}
