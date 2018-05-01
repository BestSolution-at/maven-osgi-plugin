package at.bestsolution.maven.maven.osgi.targetplatform.plugin.internal;

import at.bestsolution.maven.maven.osgi.targetplatform.lib.ParameterProvider;

/**
 * Extends the parameter provider with parameters only needed by the plugin.
 *
 */
public interface ExtendedParameterProvider extends ParameterProvider {

    String getVersion();

    String getArtifactId();

    String getGroupId();

    String getOutputFile();

}
