package fr.sirs.importer.v2;

import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.SessionCore;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.Element;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.GenericImporter;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Analyze a table content to put its data in already existing documents.
 * @author Alexis Manin (Geomatys)
 */
public abstract class DocumentUpdater<T extends Element> extends GenericImporter<T> {

    @Autowired
    protected SessionCore session;

    protected GenericImporter<T> documentImporter;
    protected AbstractSIRSRepository<T> documentRepo;

    @Override
    protected void postCompute() throws AccessDbImporterException {
        super.postCompute();
        documentImporter = null;
        documentRepo = null;
    }

    @Override
    protected void preCompute() throws AccessDbImporterException {
        super.preCompute();
        final Class<T> elementClass = getDocumentClass();
        documentImporter = context.importers.get(elementClass);
        if (documentImporter == null) {
            throw new IllegalStateException("Cannot find any importer for type : "+elementClass);
        }

        AbstractSIRSRepository<T> documentRepo = session.getRepositoryForClass(elementClass);
        if (documentRepo == null) {
            throw new IllegalStateException("No repository found to read elements of type : "+elementClass);
        }

    }

    @Override
    protected T getOrCreateElement(Row input) {
        final Integer rowId = input.getInt(documentImporter.getRowIdFieldName());
        if (rowId == null) {
            throw new IllegalStateException("Input has no valid ID.");
        }

        final String elementId;
        try {
            elementId = documentImporter.getImportedId(rowId);
        } catch (Exception ex) {
            throw new IllegalStateException("No imported object found for row "+rowId+" from table "+getTableName());
        }

        return documentRepo.get(elementId);
    }
}
