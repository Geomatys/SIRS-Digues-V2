package fr.sirs.util;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import javafx.application.Application;
import javafx.stage.Stage;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Ensure copy task will move properly all files given, with appropriate renaming.
 * @author Alexis Manin (Geomatys)
 */
public class CopyTaskTest extends Application {

    static final String FIRST = "FIrST";
    static final String SECOND = "SecOND";

    private Path srcDir;
    private Path dstDir;

    @BeforeClass
    public static void beforeClass() throws InterruptedException {
        // init javafx toolkit
        final Thread jfx = new Thread(()-> launch());
        jfx.setDaemon(true);
        jfx.start();
    }

    @Before
    public void initTest() throws IOException {
        srcDir = Files.createTempDirectory("copyTaskTest-src");
        dstDir = Files.createTempDirectory("copyTaskTest-dst");
    }

    @After
    public void destroy() throws IOException {
        // Clean temp directories.
        Files.walkFileTree(srcDir, new SimpleFileVisitor<Path>(){
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return super.postVisitDirectory(dir, exc);
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return super.visitFile(file, attrs);
            }
        });
    }

    @Test
    public void testBrutCopy() throws Exception {
        final HashSet<Path> inputs = new HashSet<>(2);
        inputs.add(Files.write(srcDir.resolve(FIRST), FIRST.getBytes(), StandardOpenOption.CREATE_NEW));
        inputs.add(Files.write(srcDir.resolve(SECOND), SECOND.getBytes(), StandardOpenOption.CREATE_NEW));

        final CopyTask t = new CopyTask(inputs, dstDir);
        t.call();

        final Path firstCopy = dstDir.resolve(FIRST);
        final Path secondCopy = dstDir.resolve(SECOND);
        // Ensure new files have been created
        assertTrue("First file exists", Files.isRegularFile(firstCopy));
        assertTrue("Second file exists", Files.isRegularFile(secondCopy));

        // check copied file content
        assertArrayEquals("first file content", FIRST.getBytes(), Files.readAllBytes(firstCopy));
        assertArrayEquals("second file content", SECOND.getBytes(), Files.readAllBytes(secondCopy));
    }

    @Test
    public void testFolderCopy() throws Exception {
        final Path subDir = srcDir.resolve("tmpDir");
        Files.createDirectory(subDir);

        Files.write(subDir.resolve(FIRST), FIRST.getBytes(), StandardOpenOption.CREATE_NEW);
        Files.write(subDir.resolve(SECOND), SECOND.getBytes(), StandardOpenOption.CREATE_NEW);

        final HashSet<Path> inputs = new HashSet<>(1);
        inputs.add(subDir);

        final CopyTask t = new CopyTask(inputs, dstDir);
        t.call();

        final Path firstCopy = dstDir.resolve(FIRST);
        final Path secondCopy = dstDir.resolve(SECOND);
        // Ensure new files have been created
        assertTrue("First file exists", Files.isRegularFile(firstCopy));
        assertTrue("Second file exists", Files.isRegularFile(secondCopy));

        // check copied file content
        assertArrayEquals("first file content", FIRST.getBytes(), Files.readAllBytes(firstCopy));
        assertArrayEquals("second file content", SECOND.getBytes(), Files.readAllBytes(secondCopy));
    }

    @Test
    public void testCopyRename() throws Exception {
        final HashSet<Path> inputs = new HashSet<>(2);
        inputs.add(Files.write(srcDir.resolve(FIRST), FIRST.getBytes(), StandardOpenOption.CREATE_NEW));
        inputs.add(Files.write(srcDir.resolve(SECOND), SECOND.getBytes(), StandardOpenOption.CREATE_NEW));

        final String suffix = "_file";
        final CopyTask t = new CopyTask(inputs, dstDir, path -> Paths.get(path.getFileName().toString()+suffix));
        t.call();

        final Path firstCopy = dstDir.resolve(FIRST+suffix);
        final Path secondCopy = dstDir.resolve(SECOND+suffix);

        // Ensure new files have been created
        assertTrue("First file exists", Files.isRegularFile(firstCopy));
        assertTrue("Second file exists", Files.isRegularFile(secondCopy));

        // check copied file content
        assertArrayEquals("first file content", FIRST.getBytes(), Files.readAllBytes(firstCopy));
        assertArrayEquals("second file content", SECOND.getBytes(), Files.readAllBytes(secondCopy));
    }

    @Test
    public void testCopyInSubDir() throws Exception {
        final HashSet<Path> inputs = new HashSet<>(2);
        inputs.add(Files.write(srcDir.resolve(FIRST), FIRST.getBytes(), StandardOpenOption.CREATE_NEW));
        inputs.add(Files.write(srcDir.resolve(SECOND), SECOND.getBytes(), StandardOpenOption.CREATE_NEW));

        final Path subDir = Paths.get("myDir");
        final CopyTask t = new CopyTask(inputs, dstDir, path -> subDir.resolve(path.getFileName()));
        t.call();

        final Path firstCopy = dstDir.resolve(subDir).resolve(FIRST);
        final Path secondCopy = dstDir.resolve(subDir).resolve(SECOND);

        // Ensure new files have been created
        assertTrue("First file exists", Files.isRegularFile(firstCopy));
        assertTrue("Second file exists", Files.isRegularFile(secondCopy));

        // check copied file content
        assertArrayEquals("first file content", FIRST.getBytes(), Files.readAllBytes(firstCopy));
        assertArrayEquals("second file content", SECOND.getBytes(), Files.readAllBytes(secondCopy));
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

    }
}
