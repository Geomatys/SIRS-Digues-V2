package fr.sirs.plugins.mobile;

import fr.sirs.core.component.DatabaseRegistry;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import javafx.application.Application;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/fr/sirs/plugins/mobile/spring/test-context.xml")
public class PhotoImportTest extends Application {

    private static DatabaseRegistry REGISTRY;

    private Path srcDir;
    private Path dstDir;

    @BeforeClass
    public static void beforeClass() throws InterruptedException, IOException {
        // init javafx toolkit
        final Thread jfx = new Thread(()-> launch());
        jfx.setDaemon(true);
        jfx.start();

        REGISTRY = new DatabaseRegistry();
        REGISTRY.connectToSirsDatabase("photImportTest", true, false, false);
    }

    @Before
    public void initPaths() throws IOException {
        srcDir = Files.createTempDirectory("copyTaskTest-src");
        dstDir = Files.createTempDirectory("copyTaskTest-dst");
    }

    @After
    public void destroyPaths() throws IOException {
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
    public void simpleImportTest() {
        PhotoImportPane.PathResolver pathResolver = new PhotoImportPane.PathResolver(null, null, null);
        pathResolver.getClass();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // nothing
    }
}
