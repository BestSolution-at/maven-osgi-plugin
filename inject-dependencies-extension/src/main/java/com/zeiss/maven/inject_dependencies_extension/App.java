package com.zeiss.maven.inject_dependencies_extension;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.DefaultModelReader;
import org.apache.maven.model.io.DefaultModelWriter;
import org.apache.maven.model.io.ModelParseException;
import org.apache.maven.model.io.ModelReader;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.IOUtil;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFileState;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IPathVariableManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.XMLDefaultHandler;
import org.eclipse.pde.internal.core.feature.WorkspaceFeatureModel;
import org.eclipse.pde.internal.core.ifeature.IFeaturePlugin;
import org.eclipse.pde.internal.core.isite.ISiteFeature;
import org.eclipse.pde.internal.core.site.Site;
import org.eclipse.pde.internal.core.site.WorkspaceSiteModel;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Hello world!
 *
 */
@Component(role = ModelReader.class, hint = "default")
public class App extends DefaultModelReader {

	@Requirement
	Logger log;

	public Model read(final File input, final Map<String, ?> options) throws IOException {
		Model model = null;

		model.clone();
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
		// Object o = null;
		// o.getClass();

		return read(new InputStreamReader(input), options);
	}

	public Model read(Reader input, Map<String, ?> options) throws IOException, ModelParseException {
		Model model = super.read(input, options);


		for (Dependency dependency: load2()){
			model.addDependency(dependency);
		}
		
		DefaultModelWriter writer = new DefaultModelWriter();

		String tmpPath = System.getProperty("java.io.tmpdir");
		writer.write(new File(tmpPath + "/pom.xml"), (Map<String, Object>) null, model);
		
		return model;
	}

	public static List<Dependency> load2() throws IOException {
		List<Dependency> results = new ArrayList<>();

		Dependency dependency = new Dependency();
		dependency.setArtifactId("junit");
		dependency.setGroupId("junit");
		dependency.setVersion("4.8.2");

		results.add(dependency);

		
		return results;
	}
	
	public static List<Dependency> load() throws IOException {

		List<Dependency> results = new ArrayList<>();
		
		java.io.File file = new File("C:\\tmp\\download.eclipse.org.xml");

		WorkspaceSiteModel model = new WorkspaceSiteModel(new MyFile(file));
		model.load();

		String url = "C:\\tmp\\";
		for (ISiteFeature f : model.getSite().getFeatures()) {
			if (f.getURL().startsWith("features/org.eclipse.fx.target.feature_")) {
				url+=f.getURL();
			}
		}
		File jarFile = new File(url);

		if(jarFile.exists()){
			
			File f = new File("C:\\tmp\\feature\\feature.xml");

			JarFile jar = new JarFile(jarFile);
			Enumeration<JarEntry> enum1 = jar.entries();
			while(enum1.hasMoreElements()){
				JarEntry nextElement = enum1.nextElement();
				if (nextElement.getName().equals("feature.xml")){
					
					
					f.mkdirs();
					f.delete();
					InputStream is = jar.getInputStream(nextElement);
					FileOutputStream fos = new FileOutputStream(f);
	 
					// write contents of 'is' to 'fos'
					while (is.available() > 0) {
						fos.write(is.read());
					}
	 
					fos.close();
					is.close();
					break;
				}
			}
			jar.close();
			
			WorkspaceFeatureModel fmodel = new WorkspaceFeatureModel(new MyFile(f));
			
			fmodel.load();
			
			
			
			IFeaturePlugin[] plugins = fmodel.getFeature().getPlugins();
			for (IFeaturePlugin iFeaturePlugin : plugins) {
				
				
				String groupId="";
				if (iFeaturePlugin.getId().indexOf("org.eclipse.fx")==-1){
					groupId="at.bestsolution.efxclipse.eclipse";
				} else {
					groupId="at.bestsolution.efxclipse.rt";
				}
				
				//System.err.println("<dependency>");
				Dependency dependency = new Dependency();
				//System.err.println("\t<groupId>"+groupId+"</groupId>");
				dependency.setGroupId(groupId);
				//System.err.println("\t<artifactId>"+iFeaturePlugin.getId()+"</artifactId>");
				dependency.setArtifactId(iFeaturePlugin.getId());
//				System.err.println("\t<version>"+iFeaturePlugin.getVersion().substring(0, 
//						iFeaturePlugin.getVersion().lastIndexOf("."))+"</version>");
				dependency.setVersion(iFeaturePlugin.getVersion().substring(0, 
						iFeaturePlugin.getVersion().lastIndexOf(".")));
//				System.err.println("</dependency>");
				
				results.add(dependency);
			}
			
		}
		return results;
	}

	public static void main(String[] args) {
		try {
			load();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
