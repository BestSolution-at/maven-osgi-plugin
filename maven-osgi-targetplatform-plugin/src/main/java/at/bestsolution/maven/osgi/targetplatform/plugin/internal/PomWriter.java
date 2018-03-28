package at.bestsolution.maven.osgi.targetplatform.plugin.internal;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.DefaultModelWriter;

import at.bestsolution.maven.osgi.targetplatform.lib.LoggingSupport;

/**
 * Responsible for writing the pom file.
 * 
 *
 */
class PomWriter {

    private static final String MAVEN_MODEL_VERSION = "4.0.0";
    private static final String PACKAGING_TYPE = "pom";

    static void writePom(File file, String groupId, String artifactId, String version, Set<Dependency> dependencies) {

        Model model = new Model();

        model.setModelVersion(MAVEN_MODEL_VERSION);
        model.setArtifactId(artifactId);
        model.setGroupId(groupId);
        model.setVersion(version);
        model.setPackaging(PACKAGING_TYPE);

        model.setDependencies(new ArrayList<>(dependencies));

        DefaultModelWriter writer = new DefaultModelWriter();

        try {
            writer.write(file, (Map<String, Object>) null, model);
        } catch (IOException e) {
            LoggingSupport.logErrorMessage(e.getMessage(), e);
        }
    }

}
