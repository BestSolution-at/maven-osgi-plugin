package de.zeiss.maven.extension;

import java.util.Properties;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.eventspy.AbstractEventSpy;
import org.apache.maven.eventspy.EventSpy;
import org.apache.maven.execution.ExecutionEvent;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.codehaus.plexus.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(role = EventSpy.class, hint = "explore")
public class BuildEventSpy extends AbstractEventSpy {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public BuildEventSpy() {
    }

    @Override
    public void init(Context context) throws Exception {
        super.init(context);
    }

    @Override
    public void onEvent(Object event) throws Exception {
       // logger.info("Maven Event: " + event);
//
//        if (event instanceof RepositoryEvent) {
//            logger.info("### RepoEvent: " + ((RepositoryEvent) event).getRepository());
//        }
//
//        if (event instanceof DefaultMavenExecutionResult) {
//            logger.info("--- ExecutionResult: " + ((DefaultMavenExecutionResult) event).getProject());
//        }
//
//        if (event instanceof ExecutionEvent) {
//            onEvent((ExecutionEvent) event);
//        }
    }

    private void onEvent(ExecutionEvent event) throws Exception {
        switch (event.getType()) {
//            case SessionEnded:
//
//                break;
//            case SessionStarted:
//
            default:
                logger.info("Unhandeld Build Event:"  + event.getType());
        }
    }



    // groupId:artifactId:type:classifier:version --> siehe http://www.mojohaus.org/versions-maven-plugin/compare-dependencies-mojo.html
    private String formatArtifact(Artifact artifact) {
        return artifact.getGroupId() +
               ":" + artifact.getArtifactId() +
               ":" + (artifact.getType() != null ? artifact.getType() : "") +
               ":" + (artifact.getClassifier() != null ? artifact.getClassifier() : "") +
               ":" + artifact.getVersion();
    }

    private String formatDependency(Dependency dependency) {
        return dependency.getGroupId() +
               ":" + dependency.getArtifactId() +
               ":" + (dependency.getType() != null ? dependency.getType() : "") +
               ":" + (dependency.getClassifier() != null ? dependency.getClassifier() : "") +
               ":" + dependency.getVersion() +
               ":" + (dependency.getScope() != null ? dependency.getScope() : "");
    }

    private static String getExecutionProperty(final ExecutionEvent event, final String property) {
        MavenSession mavenSession = event.getSession();
        Properties systemProperties = mavenSession.getSystemProperties();
        Properties userProperties = mavenSession.getUserProperties();
        String output = userProperties.getProperty(property);
        output = output == null ? systemProperties.getProperty(property) : output;
        return output;
    }

    @Override
    public void close() throws Exception {
        super.close();
    }

}
