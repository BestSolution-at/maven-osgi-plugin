package de.zeiss.maven.osgi.targetplatform.plugin;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.logging.Logger;

import de.zeiss.maven.osgi.targetplatform.plugin.internal.DefaultParameterProvider;
import de.zeiss.maven.osgi.targetplatform.plugin.internal.MainApplication;

@Mojo(name = "provide-target-dependencies", defaultPhase = LifecyclePhase.PREPARE_PACKAGE, requiresDependencyResolution = ResolutionScope.COMPILE)
public class TargetPlatformGenerator extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    @Parameter(required = true, readonly = true)
    private String outputFile;

    @Parameter(required = true, readonly = true)
    private String additionalDependenciesFile;

    @Parameter(required = true, readonly = true)
    private String whitelistFile;

    @Parameter(required = true, readonly = true)
    private String featureFile;

    @Parameter(required = true, readonly = true)
    private String targetFeatureJarPrefix;

    @Parameter(defaultValue = "site.xml", required = false, readonly = true)
    private String efxclipseSite;

    @Parameter(required = true, readonly = true)
    private String efxclipseGenericRepositoryUrl;

    @Component
    private Logger logger;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        File outputFileO = MainApplication.run(new DefaultParameterProvider(project.getVersion(), project.getArtifactId(), project.getGroupId(), outputFile,
                additionalDependenciesFile, whitelistFile, featureFile, targetFeatureJarPrefix, efxclipseSite, efxclipseGenericRepositoryUrl),
                this.project);
        
        logger.info("XXX12:"+project.getVersion()+"-"+project.getArtifactId()+"-"+project.getGroupId());

        this.project.setPomFile(outputFileO);

    }

}
