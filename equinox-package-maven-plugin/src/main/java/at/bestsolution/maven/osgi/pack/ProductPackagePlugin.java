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
import java.util.List;
import java.util.Map.Entry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

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
    
	@Parameter
	private List<String> noneRootIUs;

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
                
        if( product.launcherArgs != null ) {
        	if(product.launcherArgs.programArguments != null) {
                Xpp3Dom programArgs = new Xpp3Dom("programArgs");
                programArgs.setValue(product.launcherArgs.programArguments.stream().collect(Collectors.joining(" ")));
                launcherArgs.addChild(programArgs);
            }

            if( product.launcherArgs.vmProperties != null || product.launcherArgs.jvmModulOptions != null ) {
                Xpp3Dom vmArgs = new Xpp3Dom( "vmArgs" );
                String vmOptions = "";
                if( product.launcherArgs.vmProperties != null ) {
                    vmOptions = product.launcherArgs.vmProperties.entrySet().stream()
                            .map( e -> "-D" + e.getKey() + "=" + e.getValue() )
                            .collect( Collectors.joining( " " ) );
                }

                String jvmOptions = "";
                if( product.launcherArgs.jvmModulOptions != null ) {
                    jvmOptions = product.launcherArgs.jvmModulOptions.stream()
                            .collect( Collectors.joining( " " ) );
                }
                vmArgs.setValue( String.join( " ", vmOptions, jvmOptions ) );
                launcherArgs.addChild( vmArgs );
            }
        }
        
        if( product.launcherArgsWin != null ) {
        	if( product.launcherArgsWin.programArguments != null && ! product.launcherArgsWin.programArguments.isEmpty() ) {
        		Xpp3Dom programArgPlatform = new Xpp3Dom("programArgsWin");
        		programArgPlatform.setValue(product.launcherArgsWin.programArguments.stream().collect(Collectors.joining(" ")));
            	launcherArgs.addChild(programArgPlatform);	
        	}

            if( product.launcherArgsWin.vmProperties != null || product.launcherArgsWin.jvmModulOptions != null ) {
                Xpp3Dom vmArgsPlatform = new Xpp3Dom( "vmArgsWin" );
                String vmOptions = "";
                if( product.launcherArgsWin.vmProperties != null ) {
                    vmOptions = product.launcherArgsWin.vmProperties.entrySet().stream()
                            .map( e -> "-D" + e.getKey() + "=" + e.getValue() )
                            .collect( Collectors.joining( " " ) );
                }

                String jvmOptions = "";
                if( product.launcherArgsWin.jvmModulOptions != null ) {
                    jvmOptions = product.launcherArgsWin.jvmModulOptions.stream()
                            .collect( Collectors.joining( " " ) );
                }
                vmArgsPlatform.setValue( String.join( " ", vmOptions, jvmOptions ) );
                launcherArgs.addChild( vmArgsPlatform );
            }
        }
        
        if( product.launcherArgsOSX != null ) {
        	if( product.launcherArgsOSX.programArguments != null && ! product.launcherArgsOSX.programArguments.isEmpty() ) {
            	Xpp3Dom programArgPlatform = new Xpp3Dom("programArgsMac");
            	programArgPlatform.setValue(product.launcherArgsOSX.programArguments.stream().collect(Collectors.joining(" ")));
            	launcherArgs.addChild(programArgPlatform);        		
        	}

            if( product.launcherArgsOSX.vmProperties != null || product.launcherArgsOSX.jvmModulOptions != null ) {
                Xpp3Dom vmArgsPlatform = new Xpp3Dom( "vmArgsMac" );
                String vmOptions = "";
                if( product.launcherArgsOSX.vmProperties != null ) {
                    vmOptions = product.launcherArgsOSX.vmProperties.entrySet().stream()
                            .map( e -> "-D" + e.getKey() + "=" + e.getValue() )
                            .collect( Collectors.joining( " " ) );
                }

                String jvmOptions = "";
                if( product.launcherArgsOSX.jvmModulOptions != null ) {
                    jvmOptions = product.launcherArgsOSX.jvmModulOptions.stream()
                            .collect( Collectors.joining( " " ) );
                }
                vmArgsPlatform.setValue( String.join( " ", vmOptions, jvmOptions ) );
                launcherArgs.addChild( vmArgsPlatform );
            }

        }
        
        if( product.launcherArgsLinux != null ) {
        	if( product.launcherArgsLinux.programArguments != null && ! product.launcherArgsLinux.programArguments.isEmpty() ) {
            	Xpp3Dom programArgPlatform = new Xpp3Dom("programArgsLin");
            	programArgPlatform.setValue(product.launcherArgsLinux.programArguments.stream().collect(Collectors.joining(" ")));
            	launcherArgs.addChild(programArgPlatform);        		
        	}

            if( product.launcherArgsLinux.vmProperties != null || product.launcherArgsLinux.jvmModulOptions != null ) {
                Xpp3Dom vmArgsPlatform = new Xpp3Dom( "vmArgsLin" );
                String vmOptions = "";
                if( product.launcherArgsLinux.vmProperties != null ) {
                    vmOptions = product.launcherArgsLinux.vmProperties.entrySet().stream()
                            .map( e -> "-D" + e.getKey() + "=" + e.getValue() )
                            .collect( Collectors.joining( " " ) );
                }

                String jvmOptions = "";
                if( product.launcherArgsLinux.jvmModulOptions != null ) {
                    jvmOptions = product.launcherArgsLinux.jvmModulOptions.stream()
                            .collect( Collectors.joining( " " ) );
                }
                vmArgsPlatform.setValue( String.join( " ", vmOptions, jvmOptions ) );
                launcherArgs.addChild( vmArgsPlatform );
            }
        }

        xppProduct.addChild(launcherArgs);
        
        if( product.launcher != null ) {
        	Xpp3Dom launcher = new Xpp3Dom("launcher");
        	if( product.launcher.name != null ) {
        		launcher.setAttribute("name", product.launcher.name);
        	}
        	if( product.launcher.linux != null && product.launcher.linux.icon != null ) {
        		Xpp3Dom linux = new Xpp3Dom("linux");
        		linux.setAttribute("icon", product.launcher.linux.icon);
        		launcher.addChild(linux);
        	}
        	if( product.launcher.macosx != null && product.launcher.macosx.icon != null ) {
        		Xpp3Dom macosx = new Xpp3Dom("macosx");
        		macosx.setAttribute("icon", product.launcher.macosx.icon);
        		launcher.addChild(macosx);
        	}
        	if( product.launcher.win != null ) {
        		Xpp3Dom win = new Xpp3Dom("win");
        		win.setAttribute("useIco", product.launcher.win.useIco+"");
        		if( product.launcher.win.ico != null && product.launcher.win.ico.path != null ) {
        			Xpp3Dom ico = new Xpp3Dom("ico");
        			ico.setAttribute("path", product.launcher.win.ico.path);
        			win.addChild(ico);
        		}
        		if( product.launcher.win.bmp == null ) {
        			win.addChild(new Xpp3Dom("bmp"));
        		} else {
        			Xpp3Dom bmp = new Xpp3Dom("bmp");
        			if( product.launcher.win.bmp.winSmallHigh != null ) {
        				bmp.setAttribute("winSmallHigh", product.launcher.win.bmp.winSmallHigh);	
        			}
        			if( product.launcher.win.bmp.winSmallLow != null ) {
        				bmp.setAttribute("winSmallLow", product.launcher.win.bmp.winSmallLow);	
        			}
        			if( product.launcher.win.bmp.winMediumHigh != null ) {
        				bmp.setAttribute("winMediumHigh", product.launcher.win.bmp.winMediumHigh);	
        			}
        			if( product.launcher.win.bmp.winMediumLow != null ) {
        				bmp.setAttribute("winMediumLow", product.launcher.win.bmp.winMediumLow);	
        			}
        			if( product.launcher.win.bmp.winLargeHigh != null ) {
        				bmp.setAttribute("winLargeHigh", product.launcher.win.bmp.winLargeHigh);	
        			}
        			if( product.launcher.win.bmp.winLargeLow != null ) {
        				bmp.setAttribute("winLargeLow", product.launcher.win.bmp.winLargeLow);	
        			}
        			if( product.launcher.win.bmp.winExtraLargeHigh != null ) {
        				bmp.setAttribute("winExtraLargeHigh", product.launcher.win.bmp.winExtraLargeHigh);	
        			}
        			
        			win.addChild(bmp);
        		}
        		
        		launcher.addChild(win);
        	}
        	
        	xppProduct.addChild(launcher);
        }
        
        
        xppProduct.addChild(new Xpp3Dom("windowImages"));
        
        if (product.splashLocation != null) {
			Xpp3Dom splash = new Xpp3Dom("splash");
			splash.setAttribute("location", product.splashLocation);
			xppProduct.addChild(splash);
        }

        Xpp3Dom features = new Xpp3Dom("features");
        project.getArtifacts().stream().filter(this::pomFilter).filter(this::featureFilter).map(a -> {
            Xpp3Dom feature = new Xpp3Dom("feature");
            feature.setAttribute("id", a.getArtifactId());
            if( noneRootIUs == null || ! noneRootIUs.contains(a.getGroupId() + ":" + a.getArtifactId()) ) {
            	feature.setAttribute("installMode", "root");	
            }
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
