package de.zeiss.maven.osgi.targetplatform.plugin.internal;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.eclipse.pde.internal.core.ifeature.IFeaturePlugin;

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

    public static File run(ParameterProvider parameterProvider, MavenProject project) {

        String parentUrl = parameterProvider.getEfxclipseGenericRepositoryUrl();

        String relativeUrlToJarFile = UpdateSiteAccessor.readRelativeTargetPlatformFeatureJarUrl(parentUrl + "/" + parameterProvider.getEfxclipseSite(),
                parameterProvider.getTargetFeatureJarPrefix());

        InputStream featureFileInputStream = JarAccessor.readEntry(parentUrl + "/" + relativeUrlToJarFile, parameterProvider.getFeatureFile());

        Set<IFeaturePlugin> featurePlugins = FeaturePluginExtractor.extractFeaturePlugins(featureFileInputStream);

        InputStream resource = FeaturePluginFilter.class.getResourceAsStream(parameterProvider.getWhitelistFile());

        featurePlugins = featurePlugins.stream().filter(new FeaturePluginFilter(resource)).collect(Collectors.toSet());

        Set<Dependency> dependencies = FeaturePluginToMavenDependencyConverter.convert(featurePlugins);

        resource = FeaturePluginFilter.class.getResourceAsStream(parameterProvider.getAdditionalDependenciesFile());

        dependencies.addAll(AdditionalDependencyProvider.readAdditionalDependencies(resource));

        if (project != null) {
            project.setDependencies(new ArrayList<>(dependencies));
            
        }

        File outputFile = new File(parameterProvider.getOutputFile());
        PomWriter.writePom(outputFile, parameterProvider.getGroupId(), parameterProvider.getArtifactId(), parameterProvider.getVersion(), dependencies);

        return outputFile;
    }
}
