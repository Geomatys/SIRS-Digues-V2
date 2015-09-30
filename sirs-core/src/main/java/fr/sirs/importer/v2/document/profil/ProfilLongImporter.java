package fr.sirs.importer.v2.document.profil;

import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.AbstractPositionDocument;
import fr.sirs.core.model.ProfilLong;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.v2.AbstractImporter;
import fr.sirs.importer.v2.AbstractUpdater;
import fr.sirs.importer.v2.document.position.AbstractPositionDocumentImporter;
import org.springframework.stereotype.Component;

/**
 * ProfilLong objects are created / imported using DOCUMENT table (see {@link AbstractPositionDocumentImporter},
 * but there is additional information into PROFIL_EN_LONG table, which we will use to update them.
 * @author Alexis Manin (Geomatys)
 */
@Component
public class ProfilLongImporter extends AbstractUpdater<ProfilLong, ProfilLong> {

    private AbstractImporter<AbstractPositionDocument> docImporter;
    private AbstractSIRSRepository<ProfilLong> profilRepo;

    @Override
    protected Class<ProfilLong> getElementClass() {
        return ProfilLong.class;
    }

    @Override
    public String getTableName() {
        return DbImporter.TableName.PROFIL_EN_LONG.toString();
    }

    @Override
    public String getRowIdFieldName() {
        return "ID_PROFIL_EN_LONG";
    }

    @Override
    protected void postCompute() {
        super.postCompute();
        docImporter = null;
        profilRepo = null;
    }

    @Override
    protected void preCompute() throws AccessDbImporterException {
        super.preCompute();
        docImporter = context.importers.get(AbstractPositionDocument.class);
        if (docImporter == null) {
            throw new IllegalStateException("No importer found for type "+AbstractPositionDocument.class);
        }

        profilRepo = session.getRepositoryForClass(ProfilLong.class);
    }


    @Override
    protected ProfilLong createElement(Row input) {
        final Object id = input.get(getRowIdFieldName());
        try {
            final String importedId = docImporter.getImportedId(id);
            return profilRepo.get(importedId);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Cannot find any imported "+AbstractPositionDocument.class.getCanonicalName()+" element for ID "+id, ex);
        }
    }


    @Override
    public void put(ProfilLong container, ProfilLong toPut) {}

    @Override
    protected ProfilLong getDocument(Object rowId, Row input, ProfilLong output) {
        return output;
    }
}
