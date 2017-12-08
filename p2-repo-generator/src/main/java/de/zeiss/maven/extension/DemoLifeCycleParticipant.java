package de.zeiss.maven.extension;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Repository;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.logging.Logger;

import at.bestsolution.maven.osgi.pack.GenerationParameters;
import at.bestsolution.maven.osgi.pack.P2RepositoryPackager;

//@Component(role = AbstractMavenLifecycleParticipant.class)
public class DemoLifeCycleParticipant  extends AbstractMavenLifecycleParticipant {


    @Requirement
    private Logger logger;

    @Requirement
    private P2RepositoryPackager packager;

    @Requirement
    private Invoker invoker;

    @Requirement
    PlexusContainer container;

    @Override
    public void afterProjectsRead(MavenSession session) throws MavenExecutionException {
        super.afterProjectsRead(session);

        Repository p2Repo = null;

        for (Repository repo : session.getCurrentProject().getRepositories()) {
            if ("p2".equals(repo.getLayout())) {
                p2Repo = repo;
                break;
            }
        }

        // ---------------
        // Hack to get the lifecycleListener from tycho
        Collection<ClassRealm> realms = session.getCurrentProject().getClassRealm().getWorld().getRealms();
        ClassRealm tychoRealm = (ClassRealm) realms.toArray()[4];

        ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(tychoRealm);
        try {
            container.lookup(AbstractMavenLifecycleParticipant.class,"TychoMavenLifecycleListener");
        } catch (ComponentLookupException e) {
            e.printStackTrace();
        } finally {
            Thread.currentThread().setContextClassLoader(oldContextClassLoader);
        }
        // ---------------

        if (p2Repo != null) {
            if (!repositoryExists(p2Repo)) {
                createRepository(session, p2Repo);
            }

        }

    }
    private boolean repositoryExists(Repository p2Repo) {
        File file = new File(p2Repo.getUrl());
        return file.exists();
    }

    private void createRepository(MavenSession session, Repository p2Repo) {
        logger.info("##### REPO does not exist");
        if ("true".equals(System.getProperty("p2.repo.processing"))) {
            logger.info("#### Repo is currently being created.");
            return;
        }

        GenerationParameters parameters = GenerationParameters.builder().build();
        packager.execute(parameters);

        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile( session.getCurrentProject().getFile());
        request.setGoals(Collections.singletonList("prepare-package" ) );
        request.setProfiles(Collections.singletonList("create-repo"));
        Properties props = new Properties();
        props.put("p2.repo.processing", "true");
        props.put("tycho.mode", "maven");

        request.setProperties(props);

        Invoker invoker = new DefaultInvoker();
        //invoker.setMavenHome(new File("/usr"));

        try
        {
            invoker.execute( request );
        }
        catch (MavenInvocationException e)
        {
            e.printStackTrace();
        }

    }

}
