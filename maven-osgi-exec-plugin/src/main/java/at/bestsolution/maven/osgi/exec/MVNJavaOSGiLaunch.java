/*******************************************************************************
 * Copyright (c) 2017 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl<tom.schindl@bestsolution.at> - initial API and implementation
 *******************************************************************************/
package at.bestsolution.maven.osgi.exec;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

@Mojo(name="exec-osgi-java", requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class MVNJavaOSGiLaunch extends MVNBaseOSGiLaunchPlugin {
	
	public void execute() throws MojoExecutionException, MojoFailureException {
		Path ini = generateConfigIni(project);
		
		Optional<URL> launcherJar = project.getArtifacts().stream()
				.filter(a -> "org.eclipse.equinox.launcher".equals(a.getArtifactId())).findFirst()
				.map(a -> {
					try {
						return a.getFile().toURL();
					} catch (MalformedURLException e) {
						throw new RuntimeException(e);
					}
				});
		
		URLClassLoader l = new URLClassLoader( new URL[] { launcherJar.get() } );
		
		List<String> cmd = new ArrayList<>();
		cmd.add("-configuration");
		cmd.add("file:" + ini.toString());
		cmd.addAll(programArguments);
		
		System.getProperties().putAll(vmProperties);
		
		Thread t = new Thread() {
			public void run() {
				try {
					Class<?> cl = getContextClassLoader().loadClass("org.eclipse.equinox.launcher.Main");
					Method m = cl.getDeclaredMethod("main", String[].class);
					m.invoke(null, new Object[] {cmd.toArray(new String[0])});
				} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		t.setContextClassLoader(l);
		t.start();
		try {
			t.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
