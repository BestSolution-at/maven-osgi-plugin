package de.zeiss.maven.osgi.targetplatform.extension.internal;

import java.util.Properties;

import de.zeiss.maven.osgi.targetplatform.lib.ParameterProvider;

public class PropertyBasedParameterProvider implements ParameterProvider {

    private String additionalDependenciesFile;
    private String whitelistFile;
    private String featureFile;
    private String targetFeatureJarPrefix;
    private String efxclipseSite;
    private String efxclipseGenericRepositoryUrl;

    public PropertyBasedParameterProvider(Properties properties) {
        this.additionalDependenciesFile = properties.getProperty("additional.dependencies.file", "/additional-dependencies.txt");
        this.whitelistFile = properties.getProperty("whitelist.file", "/whitelist.txt");
        this.featureFile = properties.getProperty("feature.file", "feature.xml");
        this.targetFeatureJarPrefix = properties.getProperty("target.feature.jar.prefix", "features/org.eclipse.fx.target.feature_");
        this.efxclipseSite = properties.getProperty("efxclipse.site", "site.xml");
        this.efxclipseGenericRepositoryUrl = properties.getProperty("efxclipse.generic.repository.url");
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
