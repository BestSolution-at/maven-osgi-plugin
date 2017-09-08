package at.bestsolution.maven.osgi.pack;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.function.Predicate;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.PrettyPrintXMLWriter;
import org.codehaus.plexus.util.xml.XMLWriter;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomWriter;

import aQute.bnd.version.MavenVersion;
import aQute.bnd.version.Version;

@Mojo(name="package-feature", defaultPhase = LifecyclePhase.PREPARE_PACKAGE, requiresDependencyResolution = ResolutionScope.COMPILE)
public class FeaturePackagePlugin extends AbstractMojo {
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

		for( Dependency a : project.getDependencies() ) {
			Xpp3Dom p = new Xpp3Dom("plugin");
			Manifest mm = getManifest(project.getArtifacts().stream().filter(filter(a)).findFirst().get());
			p.setAttribute("id", bundleName(mm));
//			p.setAttribute("download-size", "1"); // FIXME
//			p.setAttribute("install-size", "1"); // FIXME
			p.setAttribute("version", bundleVersion(mm));
			p.setAttribute("unpack", dirShape(mm) + "");
			
			d.addChild(p);
		}
		
		;
		try(PrintWriter writer = new PrintWriter(new File(classesDir,"feature.xml"))) {
			XMLWriter xmlWriter = new PrettyPrintXMLWriter( writer, "UTF-8", null );
			
			Xpp3DomWriter.write(xmlWriter, d);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		Manifest m = new Manifest();
		File file = new File(classesDir, "META-INF/MANIFEST.MF");
		file.getParentFile().mkdirs();
		try( FileOutputStream out = new FileOutputStream(file) ) {
			m.write(out);			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private Predicate<Artifact> filter(Dependency d) {
		return a -> 
			d.getArtifactId().equals(a.getArtifactId())
			&& d.getGroupId().equals(a.getGroupId())
			&& d.getVersion().equals(a.getVersion());
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
}