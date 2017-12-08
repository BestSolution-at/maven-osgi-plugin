package at.bestsolution.maven.osgi.pack;

import java.io.File;

import org.apache.maven.project.MavenProject;


public class GenerationParameters {

    private MavenProject project;

    private File directory;

    private File repositoryLocation;

    private boolean publishArtifacts = true;

    private boolean compress = true;

    public static Builder builder() {
        return new Builder();
    }

    // ------------------------------------
    // private builder class
    // ------------------------------------

    public static class Builder {
        private MavenProject project;
        private File directory;
        private File repositoryLocation;
        private boolean publishArtifacts = true;
        private boolean compress = true;

        public Builder project(MavenProject project) {
            this.project = project;
            return this;
        }

        public Builder directory(File directory) {
            this.directory = directory;
            return this;
        }

        public Builder repositoryLocation(File repositoryLocation) {
            this.repositoryLocation = repositoryLocation;
            return this;
        }

        public Builder publishArtifacts(boolean publishArtifacts) {
            this.publishArtifacts = publishArtifacts;
            return this;
        }

        public Builder compress(boolean compress) {
            this.compress = compress;
            return this;
        }

        public GenerationParameters build() {
            GenerationParameters parameters = new GenerationParameters();

            parameters.compress = compress;
            parameters.project = project;
            parameters.directory = directory;
            parameters.repositoryLocation = repositoryLocation;
            parameters.publishArtifacts = publishArtifacts;

            return parameters;
        }
    }
}
