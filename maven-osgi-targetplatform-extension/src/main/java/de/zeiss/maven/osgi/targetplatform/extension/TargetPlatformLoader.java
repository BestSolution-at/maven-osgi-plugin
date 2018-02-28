package de.zeiss.maven.osgi.targetplatform.extension;

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
import org.codehaus.plexus.util.IOUtil;

import de.zeiss.maven.osgi.targetplatform.extension.internal.PropertyBasedParameterProvider;
import de.zeiss.maven.osgi.targetplatform.lib.LoggingSupport;
import de.zeiss.maven.osgi.targetplatform.lib.TargetPlatformDependenciesExtractor;

/**
 * Accesses the target platform site, extracts dependencies from there and loads the dependencies into the current maven model.
 *
 */
@Component(role = ModelReader.class, hint = "default")
public class TargetPlatformLoader extends DefaultModelReader {

    private static final String REPOSITORY_URL_PROPERTY_KEY = "efxclipse.generic.repository.url";

    @Requirement
    Logger logger;

    public Model read(final File input, final Map<String, ?> options) throws IOException {
        Model model = null;

        Reader reader = new BufferedReader(new FileReader(input));
        try {
            model = read(reader, options);
            model.setPomFile(input);
        } finally {
            IOUtil.close(reader);
        }
        return model;
    }

    public Model read(final InputStream input, final Map<String, ?> options) throws IOException {
        return read(new InputStreamReader(input), options);
    }

    public Model read(Reader input, Map<String, ?> options) throws IOException, ModelParseException {

        LoggingSupport.setLogger(logger);

        Model model = super.read(input, options);
        String repositoryUrl = model.getProperties().getProperty(REPOSITORY_URL_PROPERTY_KEY, "false");
        if (!"false".equals(repositoryUrl)) {
            
            TargetPlatformDependenciesExtractor targetPlatformDependenciesExtractor = new TargetPlatformDependenciesExtractor(
                    new PropertyBasedParameterProvider(model.getProperties()));

            for (Dependency dependency : targetPlatformDependenciesExtractor.doMavenDependenciesGeneration()) {
                model.addDependency(dependency);
            }
        }
        return model;
    }
}
