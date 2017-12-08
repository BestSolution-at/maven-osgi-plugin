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
public class BuildEventSpy extends AbstractEventSpy {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Requirement
    PlexusContainer container;

    public BuildEventSpy() {
    }

    @Override
    public void init(Context context) throws Exception {
        super.init(context);
    }

    @Override
    public void onEvent(Object event) throws Exception {
//       logger.info("Maven Event: " + event);
//
//        if (event instanceof RepositoryEvent) {
//            logger.info("### RepoEvent: " + ((RepositoryEvent) event).getRepository());
//        }
//

        if (event instanceof ExecutionEvent) {
            onEvent((ExecutionEvent) event);
        }
    }

    private void onEvent(ExecutionEvent event) throws Exception {
        switch (event.getType()) {
            case ProjectDiscoveryStarted:
                System.setProperty("tycho.mode", "maven");
                event.getSession().getUserProperties().put("tycho.mode", "maven");

                break;
            case SessionStarted:
                logger.info("#### Session started.");
                break;
            case MojoSucceeded:
                String artifactId = event.getMojoExecution().getPlugin().getArtifactId();
                if ("maven-osgi-package-plugin".equals(artifactId) && "package-p2-repo".equals(event.getMojoExecution().getGoal())) {
                    startTychoProcess(event);

                }
                break;


            default:
                logger.info("Unhandeld Build Event:"  + event.getType());
        }
    }

    private void startTychoProcess(ExecutionEvent event) {
        // Hack to get the lifecycleListener from tycho
        Collection<ClassRealm> realms = event.getSession().getCurrentProject().getClassRealm().getWorld().getRealms();
        ClassRealm tychoRealm = (ClassRealm) realms.toArray()[4];

        ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(tychoRealm);
        try {
            AbstractMavenLifecycleParticipant tychoMavenLifecycleListener = container.lookup(AbstractMavenLifecycleParticipant.class, "TychoMavenLifecycleListener");
            // enable the tycho stuff
            System.setProperty("tycho.mode", "");
            event.getSession().getUserProperties().put("tycho.mode", "");

            tychoMavenLifecycleListener.afterProjectsRead(event.getSession());

        } catch (ComponentLookupException e) {
            e.printStackTrace();

        } catch (MavenExecutionException e) {
            e.printStackTrace();
        } finally {
            Thread.currentThread().setContextClassLoader(oldContextClassLoader);
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
