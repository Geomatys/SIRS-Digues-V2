package fr.sirs.plugins.synchro;

import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.core.authentication.SIRSAuthenticator;
import fr.sirs.core.component.DatabaseRegistry;
import fr.sirs.core.model.AbstractPhoto;
import fr.sirs.core.model.SIRSFileReference;
import fr.sirs.plugins.synchro.concurrent.AsyncPool;
import fr.sirs.plugins.synchro.ui.PhotoDestination;
import fr.sirs.plugins.synchro.ui.PhotoDownload;
import fr.sirs.plugins.synchro.ui.PrefixComposer;
import java.net.Authenticator;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.context.ConfigurableApplicationContext;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class TestPhotoImport extends Application {

    ConfigurableApplicationContext ctx;

    @Override
    public void init() throws Exception {
        Authenticator.setDefault(new SIRSAuthenticator());
        DatabaseRegistry dbReg = new DatabaseRegistry();
        ctx = dbReg.connectToSirsDatabase("test_isere", false, false, false);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        final Session session = ctx.getBean(Session.class);

        final PhotoDestination photoDestination = new PhotoDestination();
        final PrefixComposer photoDest = new PrefixComposer();
        final ObjectBinding<Function<SIRSFileReference, String>> prefixBuilder = photoDest.getPrefixBuilder();
        ObjectBinding<Function<AbstractPhoto, Path>> destBuilder = Bindings.createObjectBinding(() -> {
            final Path p = photoDestination.getDestination().get();
            if (p == null)
                return null;
            Function<SIRSFileReference, String> prefixer = photoDest.getPrefixBuilder().get();
            if (prefixer == null) {
                prefixer = file -> Paths.get(file.getChemin()).getFileName().toString();
            }
            final Function<SIRSFileReference, String> finalPrefixer = prefixer;

            return file -> SIRS.concatenatePaths(p, finalPrefixer.apply(file));

        }, photoDestination.getDestination(), prefixBuilder);
        final PhotoDownload photoImport = new PhotoDownload(new AsyncPool(2), session, destBuilder);
        primaryStage.setScene(new Scene(new VBox(10, photoDestination, photoDest, photoImport)));

        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        ctx.close();
    }

    public static void main(String... args) {
        TestPhotoImport.launch(args);
    }
}
