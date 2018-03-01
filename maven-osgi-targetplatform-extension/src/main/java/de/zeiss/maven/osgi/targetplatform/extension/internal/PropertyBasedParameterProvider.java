package de.zeiss.maven.osgi.targetplatform.extension.internal;

import java.util.Properties;

import de.zeiss.maven.osgi.targetplatform.lib.ParameterProvider;

public class PropertyBasedParameterProvider implements ParameterProvider {

    private static final String ADDITIONAL_DEPENDENCIES_FILE_PROPERTY_KEY = "additional.dependencies.file";
    private static final String WHITELIST_FILE_PROPERTY_KEY = "whitelist.file";
    private static final String FEATURE_FILE_PROPERTY_KEY = "feature.file";
    private static final String TARGET_FEATURE_JAR_PREFIX_PROPERTY_KEY = "target.feature.jar.prefix";
    private static final String EFXCLIPSE_SITE_PROPERTY_KEY = "efxclipse.site";
    public static final String EFXCLIPSE_UPDATE_SITE_PROPERTY_KEY = "efxclipse.update.site";

    private final String additionalDependenciesFile;
    private final String whitelistFile;
    private final String featureFile;
    private final String targetFeatureJarPrefix;
    private final String efxclipseSite;
    private final String efxclipseUpdateSite;

    public PropertyBasedParameterProvider(Properties properties) {
        this.additionalDependenciesFile = properties.getProperty(ADDITIONAL_DEPENDENCIES_FILE_PROPERTY_KEY, "/additional-dependencies.txt");
        this.whitelistFile = properties.getProperty(WHITELIST_FILE_PROPERTY_KEY, "/whitelist.txt");
        this.featureFile = properties.getProperty(FEATURE_FILE_PROPERTY_KEY, "feature.xml");
        this.targetFeatureJarPrefix = properties.getProperty(TARGET_FEATURE_JAR_PREFIX_PROPERTY_KEY, "features/org.eclipse.fx.target.feature_");
        this.efxclipseSite = properties.getProperty(EFXCLIPSE_SITE_PROPERTY_KEY, "site.xml");
        this.efxclipseUpdateSite = properties.getProperty(EFXCLIPSE_UPDATE_SITE_PROPERTY_KEY);
    }

    public boolean activatePlugin() {
        return efxclipseUpdateSite != null;
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
