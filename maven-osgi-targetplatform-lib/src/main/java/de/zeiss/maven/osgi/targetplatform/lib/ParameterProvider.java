package de.zeiss.maven.osgi.targetplatform.lib;

import java.net.Proxy;

/**
 * Provides all necessary parameters.
 * 
 *
 */
public interface ParameterProvider {

    String getAdditionalDependenciesFile();

    String getWhitelistFile();

    String getFeatureFile();

    String getTargetFeatureJarPrefix();

    String getEfxclipseSite();

    String getEfxclipseUpdateSite();

    Proxy getProxy();

}