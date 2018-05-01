package at.bestsolution.maven.maven.osgi.targetplatform.extension;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.DefaultModelReader;
import org.apache.maven.model.io.ModelParseException;
import org.apache.maven.model.io.ModelReader;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;

import at.bestsolution.maven.maven.osgi.targetplatform.extension.internal.PropertyBasedParameterProvider;
import at.bestsolution.maven.maven.osgi.targetplatform.lib.LoggingSupport;
import at.bestsolution.maven.maven.osgi.targetplatform.lib.TargetPlatformDependenciesExtractor;

/**
 * Accesses the target platform site, extracts dependencies from there and loads the dependencies into the current maven model.
 *
 */
@Component(role = ModelReader.class, hint = "default")
public class TargetPlatformLoader extends DefaultModelReader {

    @Requirement
    Logger logger;

    public Model read(final File input, final Map<String, ?> options) throws IOException {
        Model model = null;

        try (Reader reader = new BufferedReader(new FileReader(input))) {
            model = read(reader, options);
            model.setPomFile(input);
        }

        return model;
    }

    public Model read(final InputStream input, final Map<String, ?> options) throws IOException {
        Model model = null;
        try (InputStreamReader reader = new InputStreamReader(input)) {
            model = read(reader, options);
        }
        return model;
    }

    public Model read(Reader input, Map<String, ?> options) throws IOException, ModelParseException {
        Model model = super.read(input, options);
        PropertyBasedParameterProvider parameterProvider = new PropertyBasedParameterProvider(model.getProperties());
        if (parameterProvider.activateExtension()) {
            providePlatformDependencies(model, parameterProvider);
        }
        return model;
    }

    public void providePlatformDependencies(Model model, PropertyBasedParameterProvider parameterProvider) {
        LoggingSupport.setLogger(logger);

        TargetPlatformDependenciesExtractor targetPlatformDependenciesExtractor = new TargetPlatformDependenciesExtractor(parameterProvider);

        for (Dependency dependency : targetPlatformDependenciesExtractor.doMavenDependenciesGeneration()) {
            model.addDependency(dependency);
        }
    }
}
