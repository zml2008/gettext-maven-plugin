package org.xnap.commons.maven.gettext;

import java.io.File;
import java.io.IOException;

import org.apache.maven.monitor.logging.DefaultLog;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FileMoverTest {

    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();
    private FileMover fileMover;
    private File tmpDir;
    private File output;

    @Before
    public void setUp() throws Exception {
        tmpDir = tmp.newFolder("tmp");
        final DefaultLog log = new DefaultLog(new ConsoleLogger(Logger.LEVEL_DEBUG, "console"));
         output = tmp.newFolder("output");
        fileMover = new FileMover(log,
            tmpDir,
            "com.my.bundle.Messages",
            output);
    }

    @Test( expected = IOException.class)
    public void shouldFailIfBundleSourceMissing() throws IOException {
        // act
        fileMover.moveTmpFileToOutputDirectory(new File("Messages_de_DE.class"));
    }

    @Test
    public void shouldCleanTmpDirectory() throws IOException {
        // arrange
        final File bundleDir = new File(tmpDir, "com/my/bundle/");
        bundleDir.mkdirs();
        new File(bundleDir, "Messages_de_DE.java").createNewFile();

        // act
        fileMover.moveTmpFileToOutputDirectory(new File("Messages_de_DE.class"));

        // assert
        assertEquals(0, tmpDir.listFiles().length);
    }

    @Test
    public void shouldMoveBundleFileToOutputDirectory() throws IOException {
        // arrange
        final File bundleDir = new File(tmpDir, "com/my/bundle/");
        bundleDir.mkdirs();
        new File(bundleDir, "Messages_de_DE.java").createNewFile();

        // act
        fileMover.moveTmpFileToOutputDirectory(new File("Messages_de_DE.class"));

        // assert
        assertTrue(new File(output, "com/my/bundle/Messages_de_DE.java").exists());
    }
}