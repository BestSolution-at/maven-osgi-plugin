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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
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
import org.codehaus.plexus.util.xml.PrettyPrintXMLWriter;
import org.codehaus.plexus.util.xml.XMLWriter;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomWriter;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;

import aQute.bnd.version.MavenVersion;
import aQute.bnd.version.Version;

import static at.bestsolution.maven.osgi.pack.OsgiBundleVerifier.formatArtifact;
import static at.bestsolution.maven.osgi.pack.OsgiBundleVerifier.formatDependency;

/**
 * Creates a feature xml from the referenced dependencies.
 *
 * Supports multiplatform bundles with evaluating the maven dependency artifact classifier and maps it to OSGI platform
 * properties in the following way:
 * <ul>
 *     <li>mac: os=macosx, ws=carbon, arch=x86_64</li>
 *     <li>win32: os=win32, ws=win32, arch=x86</li>
 *     <li>x64: os=win32, ws=win32, arch=x86_64</li>
 * </ul>
 */
@Mojo(name="package-feature", defaultPhase = LifecyclePhase.PREPARE_PACKAGE, requiresDependencyResolution = ResolutionScope.COMPILE)
public class FeaturePackagePlugin extends AbstractMojo {

	/** constant string for the recoginized maven artifact classfier to support multiplatform feature bundles */
	private static final String CLASSIFIER_MAC = "mac";
	private static final String CLASSIFIER_MAC_AARCH64 = "mac_aarch64";
	private static final String CLASSIFIER_WIN32 = "win32";
	private static final String CLASSIFIER_X64 = "x64";
	private static final String CLASSIFIER_WIN_64 = "win32_64";
	private static final String CLASSIFIER_LINUX_64 = "linux_64";
	private static final String CLASSIFIER_LINUX_AARCH64 = "linux_aarch64";


	@Parameter(defaultValue = "${project}", required = true, readonly = true)
	private MavenProject project;
	
	@Parameter(defaultValue = "${project.build.outputDirectory}", readonly = true)
	private File classesDir;
	
	@Parameter(required=true)
	private String providerName;
	
	@Parameter(required=true)
	private String qualifier;
	
	@Parameter
	private String copyright;
	
	@Parameter
	private String description;
	
	@Parameter
	private String license;

    @Component
    private Logger logger;

    private OsgiBundleVerifier osgiVerifier;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		Xpp3Dom d = new Xpp3Dom("feature");
		d.setAttribute("id", project.getArtifactId());
		d.setAttribute("label", project.getName());
		
		Version version = MavenVersion.parseString(project.getVersion()).getOSGiVersion();
		d.setAttribute("version", version.getWithoutQualifier().toString()+"."+qualifier);
		d.setAttribute("provider-name", providerName);
		
		if( description != null ) {
			Xpp3Dom n = new Xpp3Dom("description");
			n.setValue(description);
			d.addChild(n);
		}
				
		if( copyright != null ) {
			Xpp3Dom n = new Xpp3Dom("copyright");
			n.setValue(copyright);
			d.addChild(n);
		}
		
		if( license != null ) {
			Xpp3Dom n = new Xpp3Dom("license");
			n.setValue(license);
			d.addChild(n);
		}

        List<Artifact> nonOsgiArtifacts = new ArrayList<>();

		for( Dependency a : project.getDependencies() ) {
			
			Optional<Artifact> first = project.getArtifacts().stream().filter(filter(a)).findFirst();
			if( ! first.isPresent() ) {
				throw new IllegalStateException("Could not find artifact for '" + formatDependency(a) + "'");
			}

			OsgiBundleVerifier osgiVerifier = getOsgiVerifier();
			if( osgiVerifier.isFeature(first.get()) ) {
				Xpp3Dom p = new Xpp3Dom("includes");
				p.setAttribute("id", a.getArtifactId());
				p.setAttribute("version", a.getVersion());
				d.addChild(p);
			} else if( osgiVerifier.isBundle(first.get()) ) {
				Xpp3Dom p = new Xpp3Dom("plugin");
				Manifest mm = getManifest(first.get());
				p.setAttribute("id", bundleName(mm));
//				p.setAttribute("download-size", "1"); // FIXME
//				p.setAttribute("install-size", "1"); // FIXME
				p.setAttribute("version", bundleVersion(mm));
				p.setAttribute("unpack", dirShape(mm) + "");
				generatePlatformAttributes(p, mm, first.get());
				
				d.addChild(p);
			} else {
                nonOsgiArtifacts.add(first.get());
            }
		}

        if (!nonOsgiArtifacts.isEmpty()) {
            printNonOsgiBundles(nonOsgiArtifacts);
            throw new IllegalStateException("There are dependencies which are no OSGI bundles. They can not be used in the product. Convert them to valid " +
                                            "OSGI bundles. See the list of artifacts above.");
        }

		if( ! classesDir.exists() ) {
			classesDir.mkdirs();
		}
		
		try(PrintWriter writer = new PrintWriter(new File(classesDir,"feature.xml"))) {
			XMLWriter xmlWriter = new PrettyPrintXMLWriter( writer, "UTF-8", null );
			
			Xpp3DomWriter.write(xmlWriter, d);
		} catch (FileNotFoundException e) {
			throw new MojoExecutionException("Unable to write feature.xml in '"+classesDir.getAbsolutePath()+"'",e);
		}
		
		Manifest m = new Manifest();
		File file = new File(classesDir, "META-INF/MANIFEST.MF");
		file.getParentFile().mkdirs();
		try( FileOutputStream out = new FileOutputStream(file) ) {
			m.write(out);			
		} catch (IOException e) {
			throw new MojoExecutionException("Unable to write META-INF/MANIFEST.MF in '"+classesDir.getAbsolutePath()+"'",e);
		}
	}

	private void generatePlatformAttributes(Xpp3Dom childNode, Manifest mm, Artifact artifact) {
		if (CLASSIFIER_MAC.equalsIgnoreCase(artifact.getClassifier())) {
			childNode.setAttribute("os", "macosx");
			childNode.setAttribute("ws", "cocoa");
			childNode.setAttribute("arch", "x86_64");

		} else if (CLASSIFIER_MAC_AARCH64.equalsIgnoreCase(artifact.getClassifier())) {
			childNode.setAttribute("os", "macosx");
			childNode.setAttribute("ws", "cocoa");
			childNode.setAttribute("arch", "aarch64");

		} else if (CLASSIFIER_WIN32.equalsIgnoreCase(artifact.getClassifier())) {
			childNode.setAttribute("os", "win32");
			childNode.setAttribute("ws", "win32");
			childNode.setAttribute("arch", "x86_64");

		} else if (CLASSIFIER_X64.equalsIgnoreCase(artifact.getClassifier()) || CLASSIFIER_WIN_64.equalsIgnoreCase(artifact.getClassifier())) {
			childNode.setAttribute("os", "win32");
			childNode.setAttribute("ws", "win32");
			childNode.setAttribute("arch", "x86_64");
			
		} else if( CLASSIFIER_LINUX_64.equalsIgnoreCase(artifact.getClassifier()) ) {
			childNode.setAttribute("os", "linux");
			childNode.setAttribute("ws", "gtk");
			childNode.setAttribute("arch", "x86_64");
			
		} else if( CLASSIFIER_LINUX_AARCH64.equalsIgnoreCase(artifact.getClassifier()) ) {
			childNode.setAttribute("os", "linux");
			childNode.setAttribute("ws", "gtk");
			childNode.setAttribute("arch", "aarch64");
			
		}else if( mm.getMainAttributes().getValue("Eclipse-PlatformFilter") != null ) {
			try {
				Filter filter = FrameworkUtil.createFilter(mm.getMainAttributes().getValue("Eclipse-PlatformFilter"));
				String[][] filterTypes = {
						{ "win32", "win32", "x86_64" },
						{ "win32", "win32", "x86" },
						{ "macosx", "cocoa", "x86_64" },
						{ "macosx", "cocoa", "aarch64" },
						{ "linux", "gtk", "x86" },
						{ "linux", "gtk", "x86_64" },
						{ "linux", "gtk", "aarch64" }
				};
				for( String[] filterType : filterTypes ) {
					if( filter.matches(createMap(filterType[0], filterType[1], filterType[2])) ) {
						childNode.setAttribute("os", filterType[0]);
						childNode.setAttribute("ws", filterType[1]);
						childNode.setAttribute("arch", filterType[2]);
						break;
					}
				}
			} catch (InvalidSyntaxException e) {
				
			}
		}
	}

	private Map<String, String> createMap(String os, String ws, String arch) {
		Map<String, String> m = new HashMap<>();
		m.put("osgi.os", os);
		m.put("osgi.ws", ws);
		m.put("osgi.arch", arch);
		return m;
	}
	
	private void printNonOsgiBundles(List<Artifact> nonOsgiArtifacts) {
        if (nonOsgiArtifacts.isEmpty()) {
            return;
        }

        logger.error("List of artifacts which are no valid OSGI bundles: ");
	    nonOsgiArtifacts.forEach(a -> {
            String message = "\t" + DebugSupport.TerminalOutputStyling.HIGH_INTENSITY.style(formatArtifact(a));
            logger.error(message);
        });
    }

    private Predicate<Artifact> filter(Dependency d) {
		String version = removeQualifier(d.getVersion());
		return a ->
			d.getArtifactId().equals(a.getArtifactId())
			&& d.getGroupId().equals(a.getGroupId())
			&& (d.getClassifier() == null || ((d.getClassifier() != null) && d.getClassifier().equalsIgnoreCase(a.getClassifier())))
			&& version.equals(removeQualifier(a.getVersion()));
	}
	
	private static String removeQualifier(String version) {
		int idx = version.indexOf('-');
		if( idx != -1 ) {
			return version.substring(0,idx);
		}
		return version;
	}
	
	private Manifest getManifest(Artifact a) {
		try (JarFile f = new JarFile(a.getFile())) {
			return f.getManifest();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw new IllegalStateException(e);
		}
	}

	private static String bundleName(Manifest m) {
		String name = m.getMainAttributes().getValue("Bundle-SymbolicName");
		return name.split(";")[0];
	}
	
	private static String bundleVersion(Manifest m) {
		String name = m.getMainAttributes().getValue("Bundle-Version");
		return name.split(";")[0];
	}

	private static boolean dirShape(Manifest m) {
		return "dir".equals(m.getMainAttributes().getValue("Eclipse-BundleShape"));
	}
	
	private OsgiBundleVerifier getOsgiVerifier() {
		if (osgiVerifier == null) {
			osgiVerifier = new OsgiBundleVerifier(logger);
		}
		return osgiVerifier;
	}
}