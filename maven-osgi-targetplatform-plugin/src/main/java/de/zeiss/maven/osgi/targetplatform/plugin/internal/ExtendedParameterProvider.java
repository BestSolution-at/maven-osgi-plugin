package de.zeiss.maven.osgi.targetplatform.plugin.internal;

import de.zeiss.maven.osgi.targetplatform.lib.internal.ParameterProvider;

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
