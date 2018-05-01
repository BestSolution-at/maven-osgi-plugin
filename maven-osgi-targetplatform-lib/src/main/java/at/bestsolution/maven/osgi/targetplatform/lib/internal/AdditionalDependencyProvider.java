package at.bestsolution.maven.osgi.targetplatform.lib.internal;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import org.apache.maven.model.Dependency;

import at.bestsolution.maven.osgi.targetplatform.lib.LoggingSupport;

/**
 * Responsible for adding additional dependencies form a text file that are not included in the feature site.
 * 
 *
 */
class AdditionalDependencyProvider {

    private static final int GROUP_ID_POSITION = 0;
    private static final int ARTIFACT_ID_POSITION = 1;
    private static final int VERSION_POSITION = 2;

    private static final int DEPENDENCY_PART_COUNT = 3;
    private static final String DEPENDENCY_DELIMITER = ",";

    static Set<Dependency> readAdditionalDependencies(InputStream additionalDependenciesFile) {
        Set<Dependency> additionalDependencies = new HashSet<>();
        try (Scanner sc = new Scanner(additionalDependenciesFile)) {
            while (sc.hasNextLine()) {
                handleDependency(additionalDependencies, sc.nextLine(), additionalDependenciesFile.toString());
            }
        }
        return additionalDependencies;
    }

    private static void handleDependency(Set<Dependency> additionalDependencies, String line, String additionalDependenciesFile) {
        String[] rawDependency = line.split(DEPENDENCY_DELIMITER);
        if (isValidDependency(rawDependency)) {
            additionalDependencies
                    .add(createDependency(rawDependency[GROUP_ID_POSITION], rawDependency[ARTIFACT_ID_POSITION], rawDependency[VERSION_POSITION]));
        } else {

            LoggingSupport.logErrorMessage(String.format("Additional dependency %s from %s cannot be parsed.", line, additionalDependenciesFile));
        }
    }

    private static Dependency createDependency(String groupId, String artifactId, String version) {
        Dependency dependency = new Dependency();
        dependency.setGroupId(groupId);
        dependency.setArtifactId(artifactId);
        dependency.setVersion(version);
        return dependency;
    }

    private static boolean isValidDependency(String[] rawDependency) {
        return rawDependency.length == DEPENDENCY_PART_COUNT;
    }
}
