package de.zeiss.maven.osgi.targetplatform.plugin.internal;

import java.net.InetSocketAddress;

import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.Settings;

import de.zeiss.maven.osgi.targetplatform.lib.ProxySettingsExtractor;

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
    private final Settings settings;

    public DefaultParameterProvider(String version, String artifactId, String groupId, String outputFile, String additionalDependenciesFile,
            String whitelistFile, String featureFile, String targetFeatureJarPrefix, String efxclipseSite, String efxclipseUpdateSite, Settings settings) {
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
        this.settings = settings;
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

    @Override
    public java.net.Proxy getProxy() {

        Proxy proxy = ProxySettingsExtractor.getProxy(settings);
        java.net.Proxy javaProxy = null;
        if (null != proxy) {
            javaProxy = new java.net.Proxy(java.net.Proxy.Type.HTTP, new InetSocketAddress(proxy.getHost(), proxy.getPort()));
        }
        return javaProxy;
    }
}
