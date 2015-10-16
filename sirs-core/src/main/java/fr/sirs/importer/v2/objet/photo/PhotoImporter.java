package fr.sirs.importer.v2.objet.photo;

import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.AvecPhotos;
import fr.sirs.core.model.Desordre;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.Observation;
import fr.sirs.core.model.Photo;
import fr.sirs.importer.AccessDbImporterException;
import static fr.sirs.importer.DbImporter.TableName.PHOTO_LOCALISEE_EN_PR;
import static fr.sirs.importer.DbImporter.TableName.PHOTO_LOCALISEE_EN_XY;
import fr.sirs.importer.v2.AbstractImporter;
import fr.sirs.importer.v2.CorruptionLevel;
import fr.sirs.importer.v2.ErrorReport;
import fr.sirs.importer.v2.mapper.objet.PhotoColumns;
import fr.sirs.importer.v2.AbstractUpdater;
import java.io.IOException;
import org.apache.sis.util.collection.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class PhotoImporter extends AbstractUpdater<Photo, Element> {

    private Cache<String, AvecPhotos> observationCache = new Cache<>(100, 13, true);

    private final String[] tableNames = new String[]{
        PHOTO_LOCALISEE_EN_PR.name(), PHOTO_LOCALISEE_EN_XY.name()
    };

    private String selectedTable = tableNames[0];

    @Autowired
    private PhotoHolderRegistry registry;

    @Override
    public synchronized void compute() throws IOException, AccessDbImporterException {
        /*
         * HACK : As API is designed to map a table to a single object type, we have
         * to call compute method multiple times, changing table name each time.
         */
        try {
            for (final String tableName : tableNames) {
                selectedTable = tableName;
                super.compute();
            }
        } finally {
            selectedTable = tableNames[0];
        }
    }

    @Override
    public void put(Element container, Photo toPut) {
        if (container instanceof Desordre) {
            // nothing to do, we did it into {@link #getDocument()} method.
        } else if (container instanceof AvecPhotos) {
            ((AvecPhotos) container).getPhotos().add(toPut);
        } else {
            context.reportError(new ErrorReport(null, null, getTableName(), null, container, "photos", "Attempt to update an object which cannot contain photos !", CorruptionLevel.ROW));
            //throw new IllegalStateException("Attempt to update an object which cannot contain photos !");
        }
    }

    @Override
    public Class<Photo> getElementClass() {
        return Photo.class;
    }

    @Override
    public String getRowIdFieldName() {
        return PhotoColumns.ID_PHOTO.name();
    }

    @Override
    protected Element getDocument(Object rowId, Row input, Photo output) {
        final Class clazz = registry.getElementType(input);
        if (clazz == null) {
            return null;
        }

        final AbstractImporter masterImporter = context.importers.get(clazz);
        if (masterImporter == null) {
            throw new IllegalStateException("Cannot find any importer for type : " + clazz);
        }

        final Object accessDocId = input.get(PhotoColumns.ID_ELEMENT_SOUS_GROUPE.name());
        if (accessDocId == null) {
            throw new IllegalStateException("Input has no valid ID in " + PhotoColumns.ID_ELEMENT_SOUS_GROUPE.name());
        }

        if (Desordre.class.isAssignableFrom(clazz)) {
            final AbstractImporter<Observation> obsImporter = context.importers.get(Observation.class);
            final String importedId;
            try {
                importedId = obsImporter.getImportedId(accessDocId);
            } catch (Exception ex) {
                //throw new IllegalStateException("No document found for observation " + accessDocId, ex);
                context.reportError(getTableName(), input, new IllegalStateException("No document found for observation " + accessDocId, ex), null);
                return null;
            }

            // Querying previews can be an heavy operation if we multiply calls to it, so whenever we get a disorder in memory, we cache it
            // with all its observations.
            AvecPhotos element = getOrCacheObservations(importedId);
            if (element == null) {
                context.reportError(getTableName(), input, new IllegalStateException("Observation imported from row " + accessDocId + " cannot be found (document id : " + importedId+")"));
                return null;
            }

            element.getPhotos().add(output);
            return ((Element) element).getCouchDBDocument();

        } else {
            try {
                final String docId = masterImporter.getImportedId(accessDocId);
                return (Element) observationCache.getOrCreate(docId, () -> (AvecPhotos) session.getRepositoryForClass(clazz).get(docId));
            } catch (Exception ex) {
                context.reportError(getTableName(), input, new IllegalStateException("No imported object found for row " + accessDocId, ex), null);
                return null;
            }
        }
    }

    @Override
    public String getTableName() {
        return selectedTable;
    }

    @Override
    protected void postCompute() {
        observationCache = null;
    }

    /**
     * Search in cache if wanted observation has already been loaded. If not, we
     * load it from database. As it forces us to make multiple queries, we try
     * to retrieve wanted element from memory by scanning all cached object
     * (searching in their children) first.
     *
     * @param observationId Id of the observation to retrieve.
     * @return The queried observation, or null if we cannot find any with the
     * given ID.
     */
    private AvecPhotos getOrCacheObservations(final String observationId) {
        try {
            return observationCache.getOrCreate(observationId, () -> {
                // scan
                for (final Object o : observationCache.values()) {
                    if (o instanceof Element) {
                        Element child = ((Element) o).getCouchDBDocument().getChildById(observationId);
                        if (child instanceof AvecPhotos) {
                            return (AvecPhotos) child;
                        }
                    }
                }

                // Query database
                final Element element = session.getElement(observationId).orElse(null);
                if (element instanceof AvecPhotos) {
                    return (AvecPhotos) element;
                }

                return null;
            });
        } catch (Exception e) {
            return null;
        }
    }
}
