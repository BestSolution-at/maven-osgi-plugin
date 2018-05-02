# E4 - Pure Maven build support

This is a reactor project contains plugins and maven extensions, which are needed to build a Eclipse 
e4 application with pure maven. That means it is a Maven-first-approach, which don't use P2-repositories and Eclipse PDE tools. Tycho is only used
at the end to construct the target platform from a local generated p2-repository and to build the assembled product. 

# Maven extension to build the target platform dependencies
In order to build and package a Eclipse e4 application a target platform is needed. It brings in all the dependencies from the Eclipse platform and efxclipse. The target platform is provided by a all-in-one update site holding the latest and tested release artifacts of Eclipse and a certain version of efxclipse itself. The update site for efxclipse 3.0 for instance is:

```http://download.eclipse.org/efxclipse/runtime-released/3.0.0/site```

This includes a Eclipse feature defining the target platform. That means all the information about dependencies for a certain efxclipse version can be retrieved from the target platform feature. All the referenced artifacts are deployed in a Maven repository managed by _Bestsolution.at_ In order to not create the Maven dependencies manually, a Maven extension is provided by this repository.

This Maven extension get just the update site (above for instance) as parameter and will generate the Maven model with all the Maven dependencies derived from the target platform feature from the mentioned update site.

## Pro

A new version of efxclipse can be adapted by just changing the URL for the update site. It is quite fast and does not reduce the build speed in a significant manner.

## Contra
 * the dependencies are needed not only at build time, but even at compile time in your favorite IDE. Most of modern IDE's have a good Maven support. The problem is that Eclipse is not able to execute Maven extensions. That brings the problem that in Eclipse the target platform can not be generated on-the-fly by the Maven extension. In IntelliJ everything works fine. We have not yet tested it at Netbeans.
 * the pom, which is generated on-the-fly is not part of your version control system 
 * as an alternative to generate the target platform Maven model, a Maven plugin is provided to generate the pom.xml with all dependencies. The content is the same as the Maven extension generates at build time. 

# Maven plugin to generate the target platform pom.xml with all dependencies
TODO

# Proxy support while accessing the update site

With the fix of [#7](https://github.com/BestSolution-at/maven-osgi-plugin/issues/7) it is possible to access the target platform update site through a proxy. 

The __first found active__ proxy from the current Maven settings file will be taken to make the connection.