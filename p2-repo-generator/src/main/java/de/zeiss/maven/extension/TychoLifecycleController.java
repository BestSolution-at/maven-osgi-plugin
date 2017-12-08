package de.zeiss.maven.extension;

import java.util.Collection;
import java.util.Properties;

import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.eventspy.AbstractEventSpy;
import org.apache.maven.eventspy.EventSpy;
import org.apache.maven.execution.ExecutionEvent;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(role = EventSpy.class, hint = "explore")
public class TychoLifecycleController extends AbstractEventSpy {

    private static final String PACKAGE_PLUGIN_ARTIFACT = "maven-osgi-package-plugin";
    private static final String PACKAGE_PLUGIN_GOAL = "package-p2-repo";
    private static final String TYCHO_DISABLE_PROPERTY = "tycho.mode";
    private static final String TYCHO_OFF_VALUE = "maven";
    private static final String TYCHO_ON_VALUE = "";
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Requirement
    PlexusContainer container;

    public TychoLifecycleController() {
    }

    @Override
    public void init(Context context) throws Exception {
        super.init(context);
    }

    @Override
    public void onEvent(Object event) throws Exception {

        if (event instanceof ExecutionEvent) {
            onEvent((ExecutionEvent) event);
        }
    }

    private void onEvent(ExecutionEvent event) throws Exception {
        switch (event.getType()) {
            case ProjectDiscoveryStarted:
                event.getSession().getUserProperties().put(TYCHO_DISABLE_PROPERTY, TYCHO_OFF_VALUE);

                break;

            case MojoSucceeded:
                String artifactId = event.getMojoExecution().getPlugin().getArtifactId();
                if (PACKAGE_PLUGIN_ARTIFACT.equals(artifactId) && PACKAGE_PLUGIN_GOAL.equals(event.getMojoExecution().getGoal())) {
                    startTychoProcess(event);
                }
                break;

            default:

        }
    }

    private void startTychoProcess(ExecutionEvent event) {
        ClassRealm tychoRealm = findTychoClassRealm(event);

        ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(tychoRealm);

        try {
            AbstractMavenLifecycleParticipant tychoMavenLifecycleListener = container.lookup(AbstractMavenLifecycleParticipant.class, "TychoMavenLifecycleListener");
            // enable the tycho stuff
            event.getSession().getUserProperties().put(TYCHO_DISABLE_PROPERTY, TYCHO_ON_VALUE);

            tychoMavenLifecycleListener.afterProjectsRead(event.getSession());

        } catch (ComponentLookupException e) {
            logger.error("Tycholifecycle was not found.", e);

        } catch (MavenExecutionException e) {
            logger.error("TychoLifecycle prozess couldn't be started.");

        } finally {
            Thread.currentThread().setContextClassLoader(oldContextClassLoader);
        }
    }

    private ClassRealm findTychoClassRealm(ExecutionEvent event) {
        Collection<ClassRealm> realms = event.getSession().getCurrentProject().getClassRealm().getWorld().getRealms();

        return (ClassRealm) realms.toArray()[4];
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
