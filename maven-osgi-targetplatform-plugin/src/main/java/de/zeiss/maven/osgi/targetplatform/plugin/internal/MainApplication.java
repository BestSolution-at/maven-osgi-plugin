package de.zeiss.maven.osgi.targetplatform.plugin.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;

import de.zeiss.maven.osgi.targetplatform.lib.TargetPlatformDependenciesExtractor;

/**
 * Main Process.
 * 
 *
 */
public class MainApplication {

    private final ExtendedParameterProvider parameterProvider;
    private Set<Dependency> dependencies;
    private File outputFile;

    public MainApplication(ExtendedParameterProvider parameterProvider) {
        this.parameterProvider = parameterProvider;
    }

    public void run(MavenProject project, MavenSession session) {
        rewritePom();
        doPostProcessing(project, session);
    }

    private void rewritePom() {
        dependencies = new TargetPlatformDependenciesExtractor(parameterProvider).doMavenDependenciesGeneration();
        outputFile = new File(parameterProvider.getOutputFile());
        PomWriter.writePom(outputFile, parameterProvider.getGroupId(), parameterProvider.getArtifactId(), parameterProvider.getVersion(), dependencies);
    }

    public void doPostProcessing(MavenProject project, MavenSession session) {
        project.setDependencies(new ArrayList<>(dependencies));

        project.setPomFile(outputFile);

        List<MavenProject> allProjects = session.getAllProjects();

        allProjects.stream().filter(p -> p.getArtifactId() != null && (p.getArtifactId().equals("sample.mvn.app")
                || p.getArtifactId().equals("sample.mvn.product") || p.getArtifactId().equals("sample.mvn.feature"))).forEach(p -> {
                    p.getProjectReferences().clear();
                    List<org.apache.maven.model.Dependency> newDependencies = new ArrayList<>();
                    newDependencies.addAll(project.getDependencies());
                    newDependencies.addAll(p.getDependencies());
                    p.setDependencies(newDependencies);
                });
    }

    public static void main(String[] args) {
        CommandLineHandler commandLineHandler = new CommandLineHandler(args);
        if (!commandLineHandler.hasValidArguments()) {
            return;
        }
        new MainApplication(commandLineHandler).rewritePom();
    }

}
