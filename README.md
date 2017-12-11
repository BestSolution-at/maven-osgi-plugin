# E4 - Pure Maven build support

This is a reactor project contains plugins and maven extensions, which are needed to build a Eclipse 
e4 application with pure maven. That means it is a Maven-first-approach, which don't use P2-repositories and Eclipse PDE tools. Tycho is only used
at the end to construct the target platform from a local generated p2-repository and to build the assembled product. 


