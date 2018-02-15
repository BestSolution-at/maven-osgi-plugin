/*******************************************************************************
 * Copyright (c) 2017 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Thomas Fahrmeyer - initial API and implementation
 *******************************************************************************/
package at.bestsolution.maven.osgi.pack;

import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.codehaus.plexus.logging.Logger;

/**
 * Provides utility methods to verifies {@link org.apache.maven.artifact.Artifact}'s whether they are OSGI bundles or not.
 */
final public class OsgiBundleVerifier {

    private static final Attributes.Name MANIFEST_SYMBOLIC_NAME = new Attributes.Name("Bundle-SymbolicName");

    private Logger logger;

    OsgiBundleVerifier(Logger logger) {
        this.logger = logger;
    }

    public boolean isBundle(Artifact artifact) {
        if ("pom".equalsIgnoreCase(artifact.getType())) {
            return false;
        }

        boolean isOsgi = false;

        try (JarFile f = new JarFile(artifact.getFile())) {
            if (f.getManifest() != null && f.getManifest().getMainAttributes().get(MANIFEST_SYMBOLIC_NAME) != null) {
                isOsgi = true;
            }

        } catch (IOException e) {
            logger.error("Can not process artifact " + formatArtifact(artifact) + ". Jar File of " + artifact.getFile() + " can not be created");
        }

        return isOsgi;
    }

    public static String formatArtifact(Artifact artifact) {
        return format(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion());
    }

    public static String formatDependency(Dependency dependency) {
        return format(dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion());
    }

    // ------------------------------------
    // private methods
    // ------------------------------------
    private static String format(String groupId, String artifactId, String version) {
        StringBuilder builder = new StringBuilder();

        builder.append("[");
        builder.append(groupId);
        builder.append(":");
        builder.append(artifactId);
        builder.append(":");
        builder.append(removeQualifier(version));
        builder.append("]");

        return builder.toString();
    }


    private static String removeQualifier(String version) {
        int idx = version.indexOf('-');
        if( idx != -1 ) {
            return version.substring(0,idx);
        }
        return version;
    }
}
