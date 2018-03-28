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
import java.io.PrintWriter;
import java.util.Map.Entry;
import java.util.jar.JarFile;

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
import org.codehaus.plexus.util.xml.PrettyPrintXMLWriter;
import org.codehaus.plexus.util.xml.XMLWriter;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomWriter;

@Mojo(name = "package-product", defaultPhase = LifecyclePhase.PREPARE_PACKAGE, requiresDependencyResolution = ResolutionScope.COMPILE)
public class ProductPackagePlugin extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    @Parameter(defaultValue = "${project.basedir}")
    private File projectDir;

    @Parameter(required = true)
    private Product product;

    @Component
    private Logger logger;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Xpp3Dom xppProduct = new Xpp3Dom("product");
        xppProduct.setAttribute("name", product.name);
        xppProduct.setAttribute("uid", product.uid);
        xppProduct.setAttribute("id", product.id);
        xppProduct.setAttribute("application", product.application);
        xppProduct.setAttribute("version", product.version);
        xppProduct.setAttribute("useFeatures", product.useFeatures + "");
        xppProduct.setAttribute("includeLaunchers", product.includeLaunchers + "");

        Xpp3Dom configIni = new Xpp3Dom("configIni");
        configIni.setAttribute("use", "default");
        xppProduct.addChild(configIni);

        Xpp3Dom launcherArgs = new Xpp3Dom("launcherArgs");
        Xpp3Dom programArgs = new Xpp3Dom("programArgs");
        for (String a : product.launcherArgs.programArguments) {
            programArgs.setValue(a);
        }
        launcherArgs.addChild(programArgs);

        Xpp3Dom vmArgs = new Xpp3Dom("vmArgs");
        for (Entry<Object, Object> e : product.launcherArgs.vmProperties.entrySet()) {
            vmArgs.setValue("-D" + e.getKey() + "=" + e.getValue());
        }

        launcherArgs.addChild(vmArgs);

        xppProduct.addChild(launcherArgs);
        xppProduct.addChild(new Xpp3Dom("windowImages"));

        Xpp3Dom features = new Xpp3Dom("features");
        project.getArtifacts().stream().filter(this::pomFilter).filter(this::featureFilter).map(a -> {
            Xpp3Dom feature = new Xpp3Dom("feature");
            feature.setAttribute("id", a.getArtifactId());
            return feature;
        }).forEach(features::addChild);
        xppProduct.addChild(features);

        Xpp3Dom configurations = new Xpp3Dom("configurations");

        for (Entry<String, Integer> e : product.startLevels.entrySet()) {
            Xpp3Dom plugin = new Xpp3Dom("plugin");
            plugin.setAttribute("id", e.getKey());
            plugin.setAttribute("autoStart", "true");
            plugin.setAttribute("startLevel", e.getValue() + "");
            configurations.addChild(plugin);
        }

        xppProduct.addChild(configurations);

        if (!projectDir.exists()) {
            projectDir.mkdirs();
        }

        try (PrintWriter writer = new PrintWriter(new File(projectDir, product.id + ".product"))) {
            XMLWriter xmlWriter = new PrettyPrintXMLWriter(writer, "UTF-8", null);
            Xpp3DomWriter.write(xmlWriter, xppProduct);

        } catch (Throwable e) {
            logger.error("Problems on writing the .product file to " + projectDir, e);
        }
    }

    /**
     * @param artifact
     *            to check the packaging type
     * @return true if the artifact is not a pom file, false otherwise
     */
    private boolean pomFilter(Artifact artifact) {
        return !"pom".equalsIgnoreCase(artifact.getType());
    }

    private boolean featureFilter(Artifact a) {
        if (a.getType().equals("pom")) {
            return false;
        }

        try (JarFile jf = new JarFile(a.getFile())) {
            return jf.getEntry("feature.xml") != null;

        } catch (Exception e) {
            logger.warn("Could not get the JAR entry feature.xml from given artifact: " + a.getFile());
        }

        return false;
    }

}
