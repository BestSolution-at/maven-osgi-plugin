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
package at.bestsolution.maven.osgi.exec;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.shared.utils.cli.CommandLineUtils;
import org.codehaus.plexus.logging.Logger;

import com.google.common.base.Strings;

@Mojo(name = "exec-osgi", requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class MVNExecOSGiLaunch extends MVNBaseOSGiLaunchPlugin {
	@Parameter
	private Map<String, String> environmentVariables = new HashMap<>();

	@Parameter(property = "exec.workingdir")
	private File workingDirectory;

	@Parameter(property = "exec.args")
	private String commandlineArgs;

	@Parameter(readonly = true, required = true, defaultValue = "${basedir}")
	private File basedir;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		Set<Path> extensionPaths = new HashSet<>();
		Path ini = generateConfigIni(project, extensionPaths);
		
		Optional<File> launcherJar = project.getArtifacts().stream()
				.filter(a -> "org.eclipse.equinox.launcher".equals(a.getArtifactId())).findFirst()
				.map(a -> {
						return a.getFile();
				});
		
		String argsProp = System.getProperty("exec.args");

		List<String> commandArguments = new ArrayList<>();
		commandArguments.addAll(vmProperties.entrySet().stream().map( e -> "-D" + e.getKey()+"="+e.getValue()).collect(Collectors.toList()));
		if( vmProperties.containsKey(OSGI_FRAMEWORK_EXTENSIONS) ) {
			String extensionClasspath = extensionPaths.stream().map(Path::toString).collect(Collectors.joining(",","file:",""));
			if( ! extensionClasspath.trim().isEmpty() ) {
				commandArguments.add("-Dosgi.frameworkClassPath=.," + extensionClasspath);
			}
		}
		if (!Strings.isNullOrEmpty(argsProp)) {
			handleSystemPropertyArguments(argsProp, commandArguments);
		}
		commandArguments.add("-jar");
		commandArguments.add(launcherJar.get().toPath().toAbsolutePath().toString());
				
		commandArguments.add("-configuration");
		commandArguments.add("file:" + ini.toString());
		commandArguments.addAll(programArguments);

		Map<String, String> enviro = handleSystemEnvVariables();

		CommandLine commandLine = new CommandLine( System.getProperty("java.home") + "/bin/java");

		String[] args = commandArguments.toArray(new String[commandArguments.size()]);

		commandLine.addArguments(args, false);
		
		Executor exec = new DefaultExecutor();
		exec.setWorkingDirectory(workingDirectory);

		PumpStreamHandler psh = new PumpStreamHandler(System.out, System.err, System.in);
		exec.setStreamHandler(psh);
		psh.start();

		try {
			exec.execute(commandLine, enviro);

		} catch (IOException e) {
			logger.error("Error on executing commandline: " + commandLine, e);

		} finally {
			try {
				psh.stop();
			} catch (IOException e) {
				logger.warn("Couldn't stop reading the process output.", e);
			}
		}
	}

	private void handleSystemPropertyArguments(String argsProp, List<String> commandArguments)
			throws MojoExecutionException {
		try {
			String[] args = CommandLineUtils.translateCommandline(argsProp);
			commandArguments.addAll(Arrays.asList(args));
		} catch (Exception e) {
			throw new MojoExecutionException("Couldn't parse systemproperty 'exec.args'");
		}
	}

	private Map<String, String> handleSystemEnvVariables() throws MojoExecutionException {
		Map<String, String> enviro = new HashMap<String, String>();
		Properties systemEnvVars = CommandLineUtils.getSystemEnvVars();
		for (Map.Entry<?, ?> entry : systemEnvVars.entrySet()) {
			enviro.put((String) entry.getKey(), (String) entry.getValue());
		}

		if (environmentVariables != null) {
			enviro.putAll(environmentVariables);
		}

		return enviro;
	}

	private void handleWorkingDirectory() throws MojoExecutionException {
		if (workingDirectory == null) {
			workingDirectory = basedir;
		}

		if (!workingDirectory.exists()) {
			getLog().debug("Making working directory '" + workingDirectory.getAbsolutePath() + "'.");
			if (!workingDirectory.mkdirs()) {
				throw new MojoExecutionException(
						"Could not make working directory: '" + workingDirectory.getAbsolutePath() + "'");
			}
		}
	}
}
