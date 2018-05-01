package at.bestsolution.maven.maven.osgi.targetplatform.plugin.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;

import at.bestsolution.maven.maven.osgi.targetplatform.lib.TargetPlatformDependenciesExtractor;

/**
 * Main Process.
 * 
 *
 */
public class MainApplication {

    private final ExtendedParameterProvider parameterProvider;

    private Set<Dependency> dependencies;
    private File outputFile;
    private MavenProject generatorProject;

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

    private void doPostProcessing(MavenProject generatorProject, MavenSession session) {

        // Update the generator project
        this.generatorProject = generatorProject;
        this.generatorProject.setDependencies(new ArrayList<>(dependencies));
        this.generatorProject.setPomFile(outputFile);

        // Update the other projects that use the platform
        session.getAllProjects().stream().filter(this::onlyRelevantProjects).forEach(this::processProject);
    }

    private boolean onlyRelevantProjects(MavenProject p) {
        return p.getArtifactId() != null && (p.getArtifactId().equals("sample.mvn.app") || p.getArtifactId().equals("sample.mvn.product")
                || p.getArtifactId().equals("sample.mvn.feature"));
    }

    private void processProject(MavenProject p) {

        // clear project references so that the generating project cannot be accessed anymore -> access the repository instead
        p.getProjectReferences().clear();

        // add extracted dependencies to all projects that use the platform
        List<org.apache.maven.model.Dependency> newDependencies = new ArrayList<>();
        newDependencies.addAll(this.generatorProject.getDependencies());
        newDependencies.addAll(p.getDependencies());
        p.setDependencies(newDependencies);
    }

    public static void main(String[] args) {
        CommandLineHandler commandLineHandler = new CommandLineHandler(args);
        if (!commandLineHandler.hasValidArguments()) {
            return;
        }
        new MainApplication(commandLineHandler).rewritePom();
    }

}
