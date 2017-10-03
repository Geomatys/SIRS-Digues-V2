package fr.sirs.plugins.synchro.ui.database;

import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.core.model.AbstractPhoto;
import fr.sirs.core.model.SIRSFileReference;
import fr.sirs.plugins.synchro.concurrent.AsyncPool;
import fr.sirs.plugins.synchro.ui.PhotoDestination;
import fr.sirs.plugins.synchro.ui.PrefixComposer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class PhotoImport extends VBox {

    public PhotoImport(final Session session, final AsyncPool executor) {
        super(10);
        setFillWidth(true);
        setAlignment(Pos.CENTER);

        final PhotoDestination photoDestination = new PhotoDestination();
        final PrefixComposer prefixPane = new PrefixComposer();

        final ObjectBinding<Function<SIRSFileReference, String>> prefixBuilder = prefixPane.getPrefixBuilder();
        final ObjectBinding<Function<AbstractPhoto, Path>> destBuilder = Bindings.createObjectBinding(() -> {
            final Path p = photoDestination.getDestination().get();
            if (p == null)
                return null;
            Function<SIRSFileReference, String> prefixer = prefixBuilder.get();
            if (prefixer == null) {
                prefixer = file -> {
                    final String chemin = file.getChemin();
                    if (chemin == null) {
                        return file.getId();
                    }
                    return Paths.get(chemin).getFileName().toString();};
            }
            final Function<SIRSFileReference, String> finalPrefixer = prefixer;

            return file -> SIRS.concatenatePaths(p, finalPrefixer.apply(file));

        }, photoDestination.getDestination(), prefixBuilder);

        final PhotoDownload downloadPane = new PhotoDownload(executor, session, destBuilder);

        getChildren().addAll(photoDestination, prefixPane, downloadPane);
    }
}
