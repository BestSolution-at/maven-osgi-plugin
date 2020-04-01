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
package at.bestsolution.maven.osgi.support;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.codehaus.plexus.logging.Logger;

/**
 * Provides utility methods to verifies {@link Artifact}'s whether they are OSGI bundles or not.
 */
final public class OsgiBundleVerifier {

    private static final Attributes.Name MANIFEST_SYMBOLIC_NAME = new Attributes.Name("Bundle-SymbolicName");

    private Logger logger;

    public OsgiBundleVerifier(Logger logger) {
        this.logger = logger;
    }

    public boolean isBundle(Artifact artifact) {
        boolean isOsgi = false;

        Optional<Manifest> manifest = getManifest(artifact);

        if (manifest.isPresent()) {
            if (manifest.get().getMainAttributes().get(MANIFEST_SYMBOLIC_NAME) != null) {
                isOsgi = true;
            }
        }

        return isOsgi;
    }

    public Optional<Manifest> getManifest(Artifact artifact) {
        Path pathToArtifact = artifact.getFile().toPath();
        Optional<Manifest> manifest = Optional.empty();

        if ("pom".equals(artifact.getType())) {
            return Optional.empty();
        } else if (Files.isDirectory(pathToArtifact)) {
            Path mf = pathToArtifact.resolve("META-INF").resolve("MANIFEST.MF");
            if( ! Files.exists(mf) ) {
                return Optional.empty();
            }
            try (InputStream in = Files
                    .newInputStream(mf)) {
                return Optional.of(new Manifest(in));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        } else {
            try (JarFile f = new JarFile(pathToArtifact.toFile())) {
                if (f.getManifest() == null) {
                    logger.warn("Ignored artifact " + formatArtifact(artifact) + ". Jar " + artifact.getFile() + " has no MANIFEST.MF");
                }
                manifest = Optional.ofNullable(f.getManifest());

            } catch (IOException e) {
                logger.error("Can not process artifact " + formatArtifact(artifact) + ". Jar " + artifact.getFile() + " can not be opened");
            }
        }

        return manifest;
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
