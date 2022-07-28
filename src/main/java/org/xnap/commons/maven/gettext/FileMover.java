package org.xnap.commons.maven.gettext;

import java.io.File;
import java.io.IOException;

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

    public void moveTmpFileToOutputDirectory(File outputFile) throws IOException {
        final String bundleRelativeDir = getBundleRelativeDirectory(targetBundle);
        final String bundleFilename = getTargetFilename(outputFile);

        final File srcDir = new File(tmpDir, bundleRelativeDir);
        final File srcFile = getSrcFile(srcDir, bundleFilename);

        final File destDir = new File(outputDirectory, bundleRelativeDir);
        final File destFile = getDestFile(destDir, bundleFilename);

        log.info("Moving file '" + srcFile + "' to '" + destFile + "'");
        FileUtils.deleteQuietly(destFile);
        FileUtils.moveFile(srcFile, destFile);
        FileUtils.cleanDirectory(tmpDir);
    }

    private String getBundleRelativeDirectory(String bundleName) {
        final String bundlePackage = bundleName.substring(0, bundleName.lastIndexOf("."));
        log.debug("bundlePackage: " + bundlePackage);
        return bundlePackage.replace('.', File.separatorChar);
    }

    private File getSrcFile(File bundleDir, String filename) {
        return new File(bundleDir, filename);
    }

    private File getDestFile(File destDir, String filename) {
        return new File(destDir, filename);
    }

    private String getTargetFilename(File outputFile) {
        return outputFile.getName().replace(".class", ".java");
    }
}
