package de.zeiss.maven.osgi.targetplatform.plugin.internal;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import de.zeiss.maven.osgi.targetplatform.lib.internal.LoggingSupport;
import de.zeiss.maven.osgi.targetplatform.lib.internal.ParameterProvider;

/**
 * Responsible for handling command line input.
 *
 */
public class CommandLineHandler implements ExtendedParameterProvider {

    private static final String EFXCLIPSE_GENERIC_REPOSITORY_URL = "efxclipseGenericRepositoryUrl";
    private static final String EFXCLIPSE_SITE = "efxclipseSite";
    private static final String TARGET_FEATURE_JAR_PREFIX = "targetFeatureJarPrefix";
    private static final String FEATURE_FILE_ARGUMENT = "feature_file";
    private static final String WHITELIST_FILE_ARGUMENT = "whitelist_file";
    private static final String ADDITIONAL_DEPENDENCIES_FILE = "additional_dependencies_file";
    private static final String OUTPUT_FILE_ARGUMENT = "output_file";
    private static final String OUTPUT_FILE_GROUP_ID_ARGUMENT = "output_file_groupid";
    private static final String OUTPUT_FILE_ARTIFACT_ID_ARGUMENT = "output_file_artifactid";
    private static final String OUTPUT_FILE_VERSION_ARGUMENT = "output_file_version";

    private final Options commandLineOptionsDefinition = createCommandLineOptionsDefintion();
    private CommandLine line;
    private boolean valid;

    public CommandLineHandler(String args[]) {
        try {
            line = new GnuParser().parse(commandLineOptionsDefinition, args);
            valid = true;
        } catch (ParseException e) {

            LoggingSupport.logErrorMessage("Illegal command line arguments: " + e.getMessage());

            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("-", commandLineOptionsDefinition);

            valid = false;
        }
    }

    public boolean hasValidArguments() {
        return valid;
    }

    @Override
    public String getEfxclipseGenericRepositoryUrl() {
        return line.getOptionValue(EFXCLIPSE_GENERIC_REPOSITORY_URL);
    }

    @Override
    public String getEfxclipseSite() {
        return line.getOptionValue(EFXCLIPSE_SITE, "site.xml");
    }

    @Override
    public String getTargetFeatureJarPrefix() {
        return line.getOptionValue(TARGET_FEATURE_JAR_PREFIX);
    }

    @Override
    public String getFeatureFile() {
        return line.getOptionValue(FEATURE_FILE_ARGUMENT, "feature.xml");
    }

    @Override
    public String getWhitelistFile() {
        return line.getOptionValue(WHITELIST_FILE_ARGUMENT);
    }

    @Override
    public String getAdditionalDependenciesFile() {
        return line.getOptionValue(ADDITIONAL_DEPENDENCIES_FILE);
    }

    @Override
    public String getOutputFile() {
        return line.getOptionValue(OUTPUT_FILE_ARGUMENT);
    }

    @Override
    public String getGroupId() {
        return line.getOptionValue(OUTPUT_FILE_GROUP_ID_ARGUMENT);
    }

    @Override
    public String getArtifactId() {
        return line.getOptionValue(OUTPUT_FILE_ARTIFACT_ID_ARGUMENT);
    }

    @Override
    public String getVersion() {
        return line.getOptionValue(OUTPUT_FILE_VERSION_ARGUMENT);
    }

    private Options createCommandLineOptionsDefintion() {
        Options opt = new Options();
        opt.addRequiredOption(EFXCLIPSE_GENERIC_REPOSITORY_URL, null, true, "url of efxclipse generic repository");
        opt.addOption(EFXCLIPSE_SITE, null, true, "file name of site");
        opt.addRequiredOption(TARGET_FEATURE_JAR_PREFIX, null, true, "prefix of target feature jar");
        opt.addOption(FEATURE_FILE_ARGUMENT, null, true, "path to feature file");
        opt.addRequiredOption(WHITELIST_FILE_ARGUMENT, null, true, "path to white list file");
        opt.addRequiredOption(ADDITIONAL_DEPENDENCIES_FILE, null, true, "path to additional dependencies file");
        opt.addRequiredOption(OUTPUT_FILE_ARGUMENT, null, true, "path to output file");
        opt.addRequiredOption(OUTPUT_FILE_GROUP_ID_ARGUMENT, null, true, "group id of generated pom");
        opt.addRequiredOption(OUTPUT_FILE_ARTIFACT_ID_ARGUMENT, null, true, "artifact id of generated pom");
        opt.addRequiredOption(OUTPUT_FILE_VERSION_ARGUMENT, null, true, "version of generated pom");
        return opt;
    }

    public static void main(String[] args) {
        new CommandLineHandler(new String[] {});
    }
}
