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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.zip.ZipFile;

import org.apache.commons.lang3.SystemUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.logging.Logger;

import at.bestsolution.maven.osgi.support.OsgiBundleVerifier;

public abstract class MVNBaseOSGiLaunchPlugin extends AbstractMojo {
	public enum Mode {
		configure, start, restart
	}

	protected static final String OSGI_FRAMEWORK_EXTENSIONS = "osgi.framework.extensions";

	private static final String LF = System.getProperty("line.separator");

	@Parameter
	protected List<String> programArguments;

	@Parameter
	protected Properties vmProperties;

	@Parameter
	protected Map<String, Integer> startLevels;

	@Parameter(defaultValue = "${project}", required = true, readonly = true)
	protected MavenProject project;

	@Parameter(defaultValue = "${project.build.directory}")
	private String projectBuildDir;

	@Parameter(defaultValue = "${project.build.finalName}")
	private String filename;

	@Parameter(property = "exec.mode", defaultValue = "START")
	protected Mode mode;

	@Parameter
	private boolean debug;

	@Component
	protected Logger logger;

	private OsgiBundleVerifier osgiVerifier;

	private OsgiBundleVerifier getOsgiVerifier() {
		if (osgiVerifier == null) {
			osgiVerifier = new OsgiBundleVerifier(logger);
		}
		return osgiVerifier;
	}

	private String toReferenceURL(Bundle element, boolean project) throws IOException {
		StringBuilder w = new StringBuilder();
		w.append("reference\\:" + element.path.toUri().toString());

		if (element.startLevel != null) {
			w.append("@" + element.startLevel + "\\:start");
		} else {
			w.append("@start");
		}
		return w.toString();
	}

	protected Path generateConfigIni(MavenProject project, Set<Path> extensionPaths) {
		Set<Artifact> artifacts = new HashSet<>(project.getArtifacts());
		if (project.getPackaging().equals("jar")) {
			artifacts.add(project.getArtifact());
		}
		Set<Bundle> bundles = artifacts.stream().map(this::map).filter(Optional::isPresent)
				.map(Optional::get).collect(Collectors.toSet());

		Path p = Paths.get(project.getBuild().getDirectory()).resolve(project.getArtifactId())
				.resolve("configuration");

		Optional<Bundle> simpleConfigurator = bundles.stream()
				.filter(b -> "org.eclipse.equinox.simpleconfigurator".equals(b.symbolicName)).findFirst();

		Optional<Bundle> equinox = bundles.stream().filter(b -> "org.eclipse.osgi".equals(b.symbolicName)).findFirst();

		try {
			Files.createDirectories(p);
		} catch (IOException e1) {
			logger.error("Can not create directories for " + p);
		}

		if (simpleConfigurator.isPresent()) {
			Path configIni = p.resolve("config.ini");
			try (BufferedWriter writer = Files.newBufferedWriter(configIni, StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING)) {
				Path explosionPath = p.resolve(".explode");
				Path bundlesInfo = generateBundlesInfo(p, explosionPath, bundles, extensionPaths);

				writer.append("osgi.bundles=" + toReferenceURL(simpleConfigurator.get(), false));
				writer.append(LF);
				writer.append("osgi.bundles.defaultStartLevel=4");
				writer.append(LF);
				writer.append("osgi.install.area=" + toInstallAreaURL(p));
				writer.append(LF);
				writer.append("osgi.framework=" + toFrameworkURL(equinox.get(), explosionPath));
				writer.append(LF);
				writer.append("eclipse.p2.data.area=@config.dir/.p2");
				writer.append(LF);
				writer.append(
						"org.eclipse.equinox.simpleconfigurator.configUrl=" + toSimpleConfigurationURL(bundlesInfo));
				writer.append(LF);
				writer.append("osgi.configuration.cascaded=false");
				writer.append(LF);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			throw new RuntimeException("Only 'org.eclipse.equinox.simpleconfigurator' is supported");
		}

		return p;
	}

	private static String toInstallAreaURL(Path p) {
		String rv = p.getParent().resolve("install").toString();
		if (SystemUtils.IS_OS_WINDOWS) {
			rv = rv.replace("\\", "\\\\").replace(":", "\\:");
		}
		return "file\\:" + rv;
	}

	private static String toSimpleConfigurationURL(Path bundlesInfo) {
		String rv = bundlesInfo.toAbsolutePath().toString();
		if (SystemUtils.IS_OS_WINDOWS) {
			rv = rv.replace('\\', '/').replace(":", "\\:");
		}
		return "file\\:" + rv;
	}

	private String toFrameworkURL(Bundle bundle, Path explodePath) {
		String rv = bundle.path.toString();
		if (vmProperties.containsKey(OSGI_FRAMEWORK_EXTENSIONS)) {
			try {
				if (!Files.exists(explodePath)) {
					Files.createDirectories(explodePath);
				}

				Path targetPath = explodePath.resolve(bundle.path.getFileName());
				Files.copy(bundle.path, targetPath, StandardCopyOption.REPLACE_EXISTING);
				rv = targetPath.toString();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		if (SystemUtils.IS_OS_WINDOWS) {
			rv = rv.replace('\\', '/').replace(":", "\\:");
		}
		return "file\\:" + rv;
	}

	private Path generateBundlesInfo(Path configurationDir, Path explosionPath, Set<Bundle> bundles,
			Set<Path> extensionPaths) {
		Path bundleInfo = configurationDir.resolve("org.eclipse.equinox.simpleconfigurator").resolve("bundles.info");
		try {
			Files.createDirectories(bundleInfo.getParent());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		try (BufferedWriter writer = Files.newBufferedWriter(bundleInfo, StandardOpenOption.CREATE,
				StandardOpenOption.TRUNCATE_EXISTING)) {
			writer.append("#encoding=UTF-8");
			writer.append(LF);
			writer.append("#version=1");
			writer.append(LF);

			for (Bundle b : bundles) {
				if ("org.eclipse.osgi".equals(b.symbolicName)) {
					writer.append(b.symbolicName);
					writer.append("," + b.version);
					writer.append("," + generateLocalPath(b, explosionPath, extensionPaths).toUri().toASCIIString());
					writer.append(",-1"); // Start Level
					writer.append(",true"); // Auto-Start
					writer.append(LF);
				} else {
					writer.append(b.symbolicName);
					writer.append("," + b.version);
					writer.append(",file:" + generateLocalPath(b, explosionPath, extensionPaths).toString());
					writer.append("," + b.startLevel); // Start Level
					writer.append("," + b.autoStart); // Auto-Start
					writer.append(LF);

				}
			}

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return bundleInfo;
	}

	private Path generateLocalPath(Bundle b, Path explodeDir, Set<Path> extensionPaths) {
		if (b.dirShape && Files.isRegularFile(b.path)) {
			Path p = explodeDir.resolve(b.symbolicName + "_" + b.version);
			if (!Files.exists(p)) {
				try (ZipFile z = new ZipFile(b.path.toFile())) {
					z.stream().forEach(e -> {
						Path ep = p.resolve(e.getName());
						if (e.isDirectory()) {
							try {
								Files.createDirectories(ep);
							} catch (IOException e1) {
								throw new RuntimeException(e1);
							}
						} else {
							if (!Files.exists(ep.getParent())) {
								try {
									Files.createDirectories(ep.getParent());
								} catch (IOException e1) {
									throw new RuntimeException(e1);
								}
							}
							try (OutputStream out = Files.newOutputStream(ep); InputStream in = z.getInputStream(e)) {
								byte[] buf = new byte[1024];
								int l;
								while ((l = in.read(buf)) != -1) {
									out.write(buf, 0, l);
								}
							} catch (IOException e2) {
								throw new RuntimeException(e2);
							}
						}
					});
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
			return p;
		} else if (vmProperties.containsKey(OSGI_FRAMEWORK_EXTENSIONS)) {
			List<String> extensions = Arrays.asList(((String) vmProperties.get(OSGI_FRAMEWORK_EXTENSIONS)).split(","));

			if ("org.eclipse.osgi".equals(b.symbolicName)
					|| extensions.stream().anyMatch(v -> v.trim().equals(b.symbolicName))) {
				try {
					if (!Files.exists(explodeDir)) {
						Files.createDirectories(explodeDir);
					}

					Path targetFile = explodeDir.resolve(b.path.getFileName());
					Files.copy(b.path, targetFile, StandardCopyOption.REPLACE_EXISTING);

					if (!"org.eclipse.osgi".equals(b.symbolicName)) {
						extensionPaths.add(targetFile);
					}

					return targetFile;
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
		return b.path.toAbsolutePath();
	}

	private Optional<Bundle> map(Artifact a) {
		Path pathToArtifact = a.getFile().toPath();
		return getOsgiVerifier().getManifest(a).filter(MVNBaseOSGiLaunchPlugin::isBundle)
				.map(m -> new Bundle(m, pathToArtifact));

	}

	private static String bundleName(Manifest m) {
		String name = m.getMainAttributes().getValue("Bundle-SymbolicName");
		return name.split(";")[0];
	}

	private static boolean isBundle(Manifest m) {
		return m.getMainAttributes().getValue("Bundle-SymbolicName") != null;
	}

	private Integer getStartLevel(Manifest m) {
		String name = bundleName(m);
		if (startLevels != null) {
			return startLevels.get(name);
		} else {
			switch (name) {
			case "org.eclipse.core.runtime":
				return 4;
			case "org.eclipse.equinox.common":
				return 2;
			case "org.eclipse.equinox.ds":
				return 2;
			case "org.eclipse.equinox.event":
				return 2;
			case "org.eclipse.equinox.simpleconfigurator":
				return 1;
			case "org.eclipse.osgi":
				return -1;
			default:
				return null;
			}
		}
	}

	public class Bundle {
		public final String symbolicName;
		public final String version;
		public final Integer startLevel;
		public final Path path;
		public final boolean dirShape;
		public final boolean autoStart;

		public Bundle(Manifest m, Path path) {
			this(bundleName(m), m.getMainAttributes().getValue("Bundle-Version"), getStartLevel(m), path,
					getStartLevel(m) != null, "dir".equals(m.getMainAttributes().getValue("Eclipse-BundleShape")));
		}

		public Bundle(String symbolicName, String version, Integer startLevel, Path path, boolean autoStart,
				boolean dirShape) {
			this.symbolicName = symbolicName;
			this.version = version;
			this.startLevel = startLevel == null ? 4 : startLevel;
			this.path = path;
			this.autoStart = autoStart;
			this.dirShape = dirShape;
		}
	}
}
