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
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class AbstractPhotoImporter extends AbstractUpdater<Photo, Element> {

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
    protected Class<Photo> getElementClass() {
        return Photo.class;
    }

    @Override
    public String getRowIdFieldName() {
        return PhotoColumns.ID_PHOTO.name();
    }

    @Override
    protected Element getDocument(int rowId, Row input, Photo output) {
        final Class clazz = registry.getElementType(input);
        final AbstractImporter masterImporter = context.importers.get(clazz);
        if (masterImporter == null) {
            throw new IllegalStateException("Cannot find any importer for type : " + clazz);
        }

        final Integer accessDocId = input.getInt(PhotoColumns.ID_ELEMENT_SOUS_GROUPE.name());
        if (accessDocId == null) {
            throw new IllegalStateException("Input has no valid ID in " + PhotoColumns.ID_ELEMENT_SOUS_GROUPE.name());
        }

        if (Desordre.class.isAssignableFrom(clazz)) {
            final AbstractImporter<Observation> obsImporter = context.importers.get(Observation.class);
            final String importedId;
            try {
                importedId = obsImporter.getImportedId(accessDocId);
            } catch (Exception ex) {
                throw new IllegalStateException("No document found for observation " + accessDocId, ex);
            }
            Optional<? extends Element> element = session.getElement(session.getPreviews().get(importedId));
            if (element.isPresent()) {
                final Element desordre = element.get();
                Element child = desordre.getChildById(importedId);
                if (child instanceof AvecPhotos) {
                    ((AvecPhotos) child).getPhotos().add(output);
                }
                return desordre;
            } else {
                // TODO : report error
                throw new IllegalStateException("No document found for observation " + importedId);
            }
        } else {
            try {
                final String docId = masterImporter.getImportedId(accessDocId);
                return (Element) session.getRepositoryForClass(clazz).get(docId);
            } catch (Exception ex) {
                throw new IllegalStateException("No imported object found for row " + rowId + " from table " + getTableName(), ex);
            }
        }
    }

    @Override
    public String getTableName() {
        return selectedTable;
    }
}
