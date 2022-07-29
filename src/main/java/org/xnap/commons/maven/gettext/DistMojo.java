package org.xnap.commons.maven.gettext;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang.LocaleUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.cli.*;

/**
 * Generates ressource bundles.
 *
 * @author Tammo van Lessen
 */
@Mojo(name = "dist", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class DistMojo extends AbstractGettextMojo {

    private static File tmpDir;

    public DistMojo() {
        tmpDir = new File(System.getProperty("java.io.tmpdir"), "gettext-maven-plugin");
        tmpDir.mkdirs();
    }

    /**
     * The msgcat command.
     */
    @Parameter(defaultValue = "msgcat", required = true)
    protected String msgcatCmd;

    /**
     * The msgfmt command.
     */
    @Parameter(defaultValue = "msgfmt", required = true)
    protected String msgfmtCmd;

    /**
     * The package and file name of the generated class or properties files.
     */
    @Parameter(required = true)
    protected String targetBundle;

    /**
     * Output format, can be "class" or "properties".
     */
    @Parameter(defaultValue = "class", required = true)
    protected String outputFormat;

    /**
     * Java version. Can be "1" or "2".
     */
    @Parameter(defaultValue = "2", required = true)
    protected String javaVersion;

    /**
     * The locale of the messages in the source code.
     */
    @Parameter(defaultValue = "en", required = true)
    protected String sourceLocale;

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    protected MavenProject project;

    /**
     * Outputs the result as .java files and not classes if set to true.
     */
    @Parameter(defaultValue = "false")
    protected boolean asSource;

    public void execute() throws MojoExecutionException {
        final FileMover fileMover = new FileMover(getLog(), tmpDir, targetBundle, outputDirectory);

        outputDirectory.mkdirs();

        CommandlineFactory cf;
        if ("class".equals(outputFormat)) {
            cf = new MsgFmtCommandlineFactory();
        } else if ("properties".equals(outputFormat)) {
            cf = new MsgCatCommandlineFactory();
        } else {
            throw new MojoExecutionException("Unknown output format: "
                + outputFormat + ". Should be 'class' or 'properties'.");
        }

        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir(poDirectory);
        ds.setIncludes(new String[] { "**/*.po" });
        ds.scan();

        String[] files = ds.getIncludedFiles();
        for (String file : files) {
            getLog().info("Processing " + file);

            File inputFile = new File(poDirectory, file);
            File outputFile = cf.getOutputFile(inputFile);

            if (!isNewer(inputFile, outputFile)) {
                getLog().info("Not compiling, target is up-to-date: " + outputFile);
                continue;
            }

            Commandline cl = cf.createCommandline(inputFile);

            this.addExtraArguments(cl);
            getLog().debug("Executing: " + cl);
            StreamConsumer out = new LoggerStreamConsumer(getLog(), LoggerStreamConsumer.INFO);
            StreamConsumer err = new LoggerStreamConsumer(getLog(), LoggerStreamConsumer.WARN);
            try {
                CommandLineUtils.executeCommandLine(cl, out, err);
            } catch (CommandLineException e) {
                getLog().error("Could not execute " + cl.getExecutable() + ".", e);
            }

            if (asSource) {
                try {
                    fileMover.moveTmpFileToOutputDirectory(outputFile);
                } catch (IOException e) {
                    throw new MojoExecutionException(
                        "Unable to move files to outputDirectory: " + outputDirectory, e);
                }
            }

        }

        String basepath = targetBundle.replace('.', File.separatorChar);
        getLog().info("Creating resource bundle for source locale");
        touch(new File(outputDirectory, basepath + "_" + sourceLocale + ".properties"));
        getLog().info("Creating default resource bundle");
        touch(new File(outputDirectory, basepath + ".properties"));
        project.addCompileSourceRoot(outputDirectory.getAbsolutePath());
    }

    private boolean isNewer(File inputFile, File outputFile) {
        return inputFile.lastModified() > outputFile.lastModified();
    }

    private void touch(File file) {
        if (!file.exists()) {
            File parent = file.getParentFile();
            if (!parent.exists()) {
                parent.mkdirs();
            }
            try {
                file.createNewFile();
            } catch (IOException e) {
                getLog().warn("Could not touch file: " + file.getName(), e);
            }
        }
    }

    private interface CommandlineFactory {

        Commandline createCommandline(File file);

        /**
         * @return the output file of this command
         */
        File getOutputFile(File input);
    }

    private class MsgFmtCommandlineFactory implements CommandlineFactory {

        public File getOutputFile(File input) {
            String locale = getLocale(input);
            return new File(outputDirectory, targetBundle.replace('.', File.separatorChar) + "_" + locale + ".class");
        }

        private String getLocale(File file) {
            String locale = file.getName().substring(0, file.getName().lastIndexOf('.'));
            return LocaleUtils.toLocale(locale).toString();
        }

        public Commandline createCommandline(File file) {
            Commandline cl = new Commandline();
            cl.setExecutable(msgfmtCmd);

            if ("2".equals(javaVersion)) {
                cl.createArg().setValue("--java2");
            } else {
                cl.createArg().setValue("--java");
            }

            if (asSource) {
                cl.createArg().setValue("--source");
                cl.createArg().setValue("-d");
                cl.createArg().setFile(tmpDir);
            } else {
                cl.createArg().setValue("-d");
                cl.createArg().setFile(outputDirectory);
            }
            cl.createArg().setValue("-r");
            cl.createArg().setValue(targetBundle);
            cl.createArg().setValue("-l");
            cl.createArg().setValue(getLocale(file));
            cl.createArg().setFile(file);
            getLog().warn(cl.toString());
            return cl;
        }
    }

    private class MsgCatCommandlineFactory implements CommandlineFactory {

        public File getOutputFile(File input) {
            String basepath = targetBundle.replace('.', File.separatorChar);
            String locale = input.getName().substring(0, input.getName().lastIndexOf('.'));
            locale = LocaleUtils.toLocale(locale).toString();
            File target = new File(outputDirectory, basepath + "_" + locale + ".properties");
            return target;
        }

        public Commandline createCommandline(File file) {
            Commandline cl = new Commandline();

            File outputFile = getOutputFile(file);
            File parent = outputFile.getParentFile();
            if (!parent.exists()) {
                parent.mkdirs();
            }

            cl.setExecutable(msgcatCmd);

            cl.createArg().setValue("--no-location");
            cl.createArg().setValue("-p");
            cl.createArg().setFile(file);
            cl.createArg().setValue("-o");
            cl.createArg().setFile(outputFile);

            return cl;
        }
    }

}
