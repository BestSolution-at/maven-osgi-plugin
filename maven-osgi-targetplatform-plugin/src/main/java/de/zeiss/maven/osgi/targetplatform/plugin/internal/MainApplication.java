package de.zeiss.maven.osgi.targetplatform.plugin.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.Set;

import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;

import de.zeiss.maven.osgi.targetplatform.lib.TargetPlatformDependenciesExtractor;


/**
 * Main Process.
 * 
 *
 */
public class MainApplication {

    public static void main(String[] args) {

        CommandLineHandler commandLineHandler = new CommandLineHandler(args);
        if (!commandLineHandler.hasValidArguments()) {
            return;
        }

        run(commandLineHandler, null);

    }

    public static File run(ExtendedParameterProvider parameterProvider, MavenProject project) {

        Set<Dependency> dependencies = TargetPlatformDependenciesExtractor.doMavenDependenciesGeneration(parameterProvider);

        if (project != null) {
            project.setDependencies(new ArrayList<>(dependencies));
            
        }

        File outputFile = new File(parameterProvider.getOutputFile());
        PomWriter.writePom(outputFile, parameterProvider.getGroupId(), parameterProvider.getArtifactId(), parameterProvider.getVersion(), dependencies);

        return outputFile;
    }

   
}
