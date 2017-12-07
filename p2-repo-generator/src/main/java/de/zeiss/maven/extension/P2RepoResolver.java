package de.zeiss.maven.extension;

import java.util.List;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.eclipse.tycho.ReactorProject;
import org.eclipse.tycho.artifacts.DependencyArtifacts;
import org.eclipse.tycho.artifacts.TargetPlatform;
import org.eclipse.tycho.core.DependencyResolver;
import org.eclipse.tycho.core.DependencyResolverConfiguration;
import org.eclipse.tycho.core.osgitools.AbstractTychoProject;

//@Component(role = DependencyResolver.class, hint = "p2-test")
public class P2RepoResolver implements DependencyResolver {

    @Requirement
    private Logger logger;

//    @Requirement
//    private DefaultTargetPlatformConfigurationReader configurationReader;
//
//    @Requirement
//    private DefaultDependencyResolverFactory dependencyResolverLocator;
//
//    @Requirement(role = TychoProject.class)
//    private Map<String, TychoProject> projectTypes;
//
//    @Requirement
//    private List<TychoResolver> allResolvers;

//    @Requirement
//    protected PlexusContainer container;

    public P2RepoResolver() {
        System.out.println("P2Resolver created");

    }

    @Override
    public void setupProjects(MavenSession session, MavenProject project, ReactorProject reactorProject) {
        logger.info("......... works");
    }

    @Override
    public TargetPlatform computePreliminaryTargetPlatform(MavenSession session, MavenProject project, List<ReactorProject> reactorProjects) {
        return null;
    }

    @Override
    public DependencyArtifacts resolveDependencies(MavenSession session, MavenProject project, TargetPlatform targetPlatform, List<ReactorProject> reactorProjects, DependencyResolverConfiguration resolverConfiguration) {
        return null;
    }

    @Override
    public void injectDependenciesIntoMavenModel(MavenProject project, AbstractTychoProject projectType, DependencyArtifacts resolvedDependencies, Logger logger) {

    }
}
