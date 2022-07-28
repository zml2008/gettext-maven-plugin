package org.xnap.commons.maven.gettext;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.logging.Log;

public class FileMover {

    private final Log log;
    private final File tmpDir;
    private final String targetBundle;
    private final File outputDirectory;

    public FileMover(Log log, File tmpDir, String targetBundle, File outputDirectory) {
        this.log = log;
        this.tmpDir = tmpDir;
        this.targetBundle = targetBundle;
        this.outputDirectory = outputDirectory;
    }

    public void moveTmpFilesToOutputDirectory(File outputFile) throws IOException {
        final String bundlePackage = targetBundle.substring(0, targetBundle.lastIndexOf("."));
        log.debug("bundlePackage: " + bundlePackage);
        final File bundleDir = Paths.get(tmpDir.getAbsolutePath(), bundlePackage.split("\\.")).toFile();
        log.debug("bundleDir: " + bundleDir);
        final File bundleFile = new File(bundleDir, getTargetFilename(outputFile));
        log.debug("bundleFile: " + bundleFile);
        final Path outDir = Paths.get(outputDirectory.getAbsolutePath(), bundlePackage.split("\\."));
        final File destFile = new File(outDir.toFile(), getTargetFilename(outputFile));
        log.debug("destFile: " + destFile);

        log.info("Moving file '" + bundleFile + "' to '" + destFile + "'");
        FileUtils.delete(destFile);
        FileUtils.moveFile(bundleFile, destFile, StandardCopyOption.REPLACE_EXISTING);
        FileUtils.cleanDirectory(tmpDir);
    }

    private static String getTargetFilename(File outputFile) {
        return outputFile.getName().replace(".class", ".java");
    }
}
