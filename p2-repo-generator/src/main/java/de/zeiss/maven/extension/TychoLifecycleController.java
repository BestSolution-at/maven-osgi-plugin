package de.zeiss.maven.extension;

import java.util.Optional;

import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.eventspy.AbstractEventSpy;
import org.apache.maven.eventspy.EventSpy;
import org.apache.maven.execution.ExecutionEvent;
import org.apache.maven.execution.MavenSession;
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
    private static final String TYCHO_CLASSLOADER_ID_PART = "tycho-maven-plugin";
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

    /**
     * Starts the tycho process by retrieving the tycho {@link AbstractMavenLifecycleParticipant} implementation and executing
     * the method {@link AbstractMavenLifecycleParticipant#afterProjectsRead(MavenSession)}.
     * That prepares the following tycho process.
     *
     * @param event
     */
    private void startTychoProcess(ExecutionEvent event) {
        Optional<ClassRealm> tychoRealm = findTychoClassRealm(event);
        if (!tychoRealm.isPresent()) {
            logger.error("Could not find the tycho classRealm and can not work properly. Give up.");
            return;
        }

        ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(tychoRealm.get());

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

    private Optional<ClassRealm> findTychoClassRealm(ExecutionEvent event) {
        Optional<ClassRealm> tychoRealm = event.getSession().getCurrentProject().getClassRealm().getWorld().getRealms().stream().filter(realm -> realm.getId()
                .contains(TYCHO_CLASSLOADER_ID_PART)).findFirst();

        return tychoRealm;
    }


    @Override
    public void close() throws Exception {
        super.close();
    }

}
