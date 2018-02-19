/*******************************************************************************
 * Copyright (c) 2017 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl - initial API and implementation
 *******************************************************************************/
package at.bestsolution.maven.osgi.pack;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.StandardCopyOption;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.apache.maven.artifact.Artifact;
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
import org.eclipse.sisu.equinox.launching.internal.P2ApplicationLauncher;

import static at.bestsolution.maven.osgi.pack.OsgiBundleVerifier.formatArtifact;

@Mojo(name = "package-p2-repo", defaultPhase = LifecyclePhase.PREPARE_PACKAGE, requiresDependencyResolution = ResolutionScope.COMPILE)
public class P2RepositoryPackagePlugin extends AbstractMojo {

	@Parameter(defaultValue = "${project}", required = true, readonly = true)
	private MavenProject project;

	@Parameter(required = true, defaultValue = "${project.build.directory}/source")
	private File directory;

	@Parameter(defaultValue = "${project.build.directory}/source-repository")
	private File repositoryLocation;

	@Parameter(defaultValue = "true")
	private boolean publishArtifacts;

	@Parameter(defaultValue = "true")
	private boolean compress;

	@Component
	private P2ApplicationLauncher launcher;

	@Component
	private Logger logger;

	private OsgiBundleVerifier osgiVerifier;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

	    project.getArtifacts().stream().filter(this::pomFilter).forEach(a -> {

            String coloredOsgiFlag = getOsgiVerifier().isBundle(a) ? "true" : DebugSupport.TerminalOutputStyling.RED.style("false");
            String message = String.format("Processing artifact: %0$-70s - OSGI Bundle: %s", formatArtifact(a), coloredOsgiFlag);
		    logger.debug(message);

			if (!a.getType().equals("pom"))
				try (JarFile f = new JarFile(a.getFile())) {
					handleJar(a, f);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					throw new IllegalStateException(e);
				}
        });


		try {
			publishContent();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			throw new IllegalStateException(e);
		}
	}

    private boolean pomFilter(Artifact artifact) {
        return ! "pom".equalsIgnoreCase(artifact.getType());
    }

    private void publishContent() throws MojoFailureException, MalformedURLException {
		launcher.setWorkingDirectory(project.getBasedir());
		launcher.setApplicationName("org.eclipse.equinox.p2.publisher.FeaturesAndBundlesPublisher");
		launcher.addArguments("-artifactRepository", repositoryLocation.toURI().toURL().toString());
		launcher.addArguments("-metadataRepository", repositoryLocation.toURI().toURL().toString());
		launcher.addArguments("-source", directory.toString());

		if (publishArtifacts) {
			launcher.addArguments("-publishArtifacts");
		}

		// launcher.addArguments(getAppendFlag());
		if (compress) {
			launcher.addArguments("-compress");
		}

		// launcher.addArguments(getReusePack200FilesFlag());
		// launcher.addArguments(getAdditionalArgs());

		int result = launcher.execute(0);
		if (result != 0) {
			throw new MojoFailureException("P2 publisher return code was " + result);
		}
	}

	private void handleJar(Artifact a, JarFile jf) throws IOException {
		if (jf.getManifest() == null) {
//			throw new NoSuchFileException("The JAR file " + jf.getName() + " of artifact " + formatArtifact(a)
//					+ " has NO Manfifest file and is not an OSGI " + "bundle.");
		}

		ZipEntry entry = jf.getEntry("feature.xml");
		File dir;
		if (entry == null) {
			if (!getOsgiVerifier().isBundle(a)) {
				return;
			}

			dir = new File(directory, "plugins");
		} else {
			dir = new File(directory, "features");
		}

		if (!dir.exists()) {
			dir.mkdirs();
		}
		Files.copy(a.getFile().toPath(), dir.toPath().resolve(a.getFile().getName()),
				StandardCopyOption.REPLACE_EXISTING);
	}

	private OsgiBundleVerifier getOsgiVerifier() {
		if (osgiVerifier==null){
			 osgiVerifier = new OsgiBundleVerifier(logger);
		}
		return osgiVerifier;
	}

}
