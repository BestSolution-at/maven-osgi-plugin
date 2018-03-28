package at.bestsolution.maven.osgi.targetplatform.plugin.internal;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.maven.model.Dependency;
import org.junit.Test;

import at.bestsolution.maven.osgi.targetplatform.plugin.internal.PomWriter;

public class PomWriterTest {

    private static final String DELIMITER = "\\Z";
    private static final String POM_CMP_XML = "/pom-cmp.xml";
    private static final String XML_EXTENSION = ".xml";

    private final String POM_FILE_NAME = "pom-test";

    private final String ARTIFACT_ID = "my.test.artifactid";
    private final String GROUP_ID = "my.test.groupid";
    private final String VERSION = "1.1.0";

    private final String DEPENDENCY_ARTIFACT_ID = "junit";
    private final String DEPENDENCY_GROUP_ID = "junit";
    private final String DEPENDENCY_VERSION = "4.2.8";

    @Test
    public void testPomGeneration() throws IOException {

        File pomFile = createDestinationFile();

        Set<Dependency> dependencies = generateDependencies();
        PomWriter.writePom(pomFile, GROUP_ID, ARTIFACT_ID, VERSION, dependencies);
        Scanner scanner = new Scanner(pomFile);
        StringWriter writer = new StringWriter();
        String generatedFileContent = scanner.useDelimiter(DELIMITER).next();
        IOUtils.copy(getClass().getResourceAsStream(POM_CMP_XML), writer);
        String expectedFileContent = writer.toString();
        scanner.close();
        assertThat(generatedFileContent.replaceAll("\\s+", ""), equalTo(expectedFileContent.replaceAll("\\s+", "")));
        pomFile.delete();
    }

    private Set<Dependency> generateDependencies() {
        Dependency dependency = new Dependency();
        dependency.setArtifactId(DEPENDENCY_ARTIFACT_ID);
        dependency.setGroupId(DEPENDENCY_GROUP_ID);
        dependency.setVersion(DEPENDENCY_VERSION);

        Set<Dependency> dependencies = new HashSet<>();
        dependencies.add(dependency);
        return dependencies;
    }

    private File createDestinationFile() throws IOException {
        return File.createTempFile(POM_FILE_NAME, XML_EXTENSION);
    }

}
