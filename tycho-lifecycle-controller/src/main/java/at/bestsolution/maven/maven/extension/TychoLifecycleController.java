/*******************************************************************************
 * Copyright (c) 2017 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Thomas Fahrmeyer - initial API and implementation
 *******************************************************************************/
package at.bestsolution.maven.maven.extension;

import java.util.Optional;

import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.eventspy.AbstractEventSpy;
import org.apache.maven.eventspy.EventSpy;
import org.apache.maven.execution.ExecutionEvent;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class controlls a {@link AbstractMavenLifecycleParticipant} implementation provided by Tycho itself.
 * Controlling in this context means that the lifecycle method {@link AbstractMavenLifecycleParticipant#afterProjectsRead(MavenSession)}
 * is delayed until the maven plugin that builds the local p2-repository is finished. If the plugin is finished the mentioned lifecycle method is
 * called. Otherwise the tycho prozess for validating the p2 repository used to build the target platform and the product will fail, because the repository
 * does not yet exists.
 * </p>
 * The functionality was tested with tycho code version 1.0.0.
 *
 */
@Component(role = EventSpy.class, hint = "explore")
public class TychoLifecycleController extends AbstractEventSpy {

    private static final String PACKAGE_PLUGIN_ARTIFACT = "maven-osgi-package-plugin";
    private static final String PACKAGE_PLUGIN_GOAL = "package-p2-repo";

    // tycho specific properties. Captured from Tycho Core Version 1.0.0
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
                disableTychoExecutionInAfterProjectsReadLifecycle(event.getSession());

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
     * That prepares the following tycho process for building the target platform and packaging the product.
     *
     * @param event
     */
    private void startTychoProcess(ExecutionEvent event) {
        Optional<ClassRealm> tychoRealm = findTychoClassRealm(event.getSession().getCurrentProject());
        if (!tychoRealm.isPresent()) {
            logger.error("Could not find the tycho classRealm and can not work properly. Give up.");
            return;
        }

        ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(tychoRealm.get());

        try {
            AbstractMavenLifecycleParticipant tychoMavenLifecycleListener = container.lookup(AbstractMavenLifecycleParticipant.class, "TychoMavenLifecycleListener");
            enableTychoExecutionInAfterProjectsReadLifecycle(event.getSession());

            tychoMavenLifecycleListener.afterProjectsRead(event.getSession());

        } catch (ComponentLookupException e) {
            logger.error("Tycholifecycle was not found.", e);

        } catch (MavenExecutionException e) {
            logger.error("TychoLifecycle prozess couldn't be started.");

        } finally {
            Thread.currentThread().setContextClassLoader(oldContextClassLoader);
        }
    }

    private void enableTychoExecutionInAfterProjectsReadLifecycle(MavenSession session) {
        // enable the tycho stuff
        session.getUserProperties().put(TYCHO_DISABLE_PROPERTY, TYCHO_ON_VALUE);
    }

    private void disableTychoExecutionInAfterProjectsReadLifecycle(MavenSession session) {
        session.getUserProperties().put(TYCHO_DISABLE_PROPERTY, TYCHO_OFF_VALUE);
    }

    /**
     * Scans all available {@link ClassRealm}'s managed by the maven container and find the one with
     * an ID containing {@link #TYCHO_CLASSLOADER_ID_PART}
     *
     * @return an Optional with the found {@code ClassRealm} or {@code null}.
     * @param currentProject
     */
    private Optional<ClassRealm> findTychoClassRealm(MavenProject currentProject) {
        Optional<ClassRealm> tychoRealm = currentProject.getClassRealm().getWorld().getRealms().stream().filter(realm -> realm.getId()
                .contains(TYCHO_CLASSLOADER_ID_PART)).findFirst();

        return tychoRealm;
    }


    @Override
    public void close() throws Exception {
        super.close();
    }

}
