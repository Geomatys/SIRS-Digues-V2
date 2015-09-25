package fr.sirs.importer.v2.objet.reseau;

import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.ObjetReseau;
import fr.sirs.core.model.PeriodeObjet;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.v2.AbstractImporter;
import fr.sirs.importer.v2.AbstractUpdater;
import java.io.IOException;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public abstract class AbstractElementReseauGestionImporter<T extends PeriodeObjet> extends AbstractUpdater<T, ObjetReseau> {

    private enum Columns {
        ID_ELEMENT_RESEAU,
        ID_INTERV_GARDIEN,
        DATE_DEBUT_GARDIEN,
        DATE_FIN_GARDIEN,
        DATE_DERNIERE_MAJ
    }

    private AbstractImporter<ObjetReseau> reseauImporter;

    @Override
    protected void postCompute() {
        super.postCompute();
        reseauImporter = null;
    }

    @Override
    protected void preCompute() throws AccessDbImporterException {
        super.preCompute();
        reseauImporter = context.importers.get(ObjetReseau.class);
    }

    @Override
    protected ObjetReseau getDocument(int rowId, Row input, T output) {
        final Integer reseauId;
        try {
            reseauId = input.getInt(Columns.ID_ELEMENT_RESEAU.name());
            String importedId = reseauImporter.getImportedId(reseauId);
            if (importedId == null) {
                throw new IllegalArgumentException("No document found for "+Columns.ID_ELEMENT_RESEAU.name()+" "+reseauId);
            }
            final Element element = session.getElement(importedId).orElse(null);
            if (element instanceof ObjetReseau) {
                return (ObjetReseau) element;
            } else {
                throw new IllegalArgumentException("No document found for "+Columns.ID_ELEMENT_RESEAU.name()+" "+reseauId);
            }
        } catch (IOException | AccessDbImporterException ex) {
            throw new IllegalStateException("No document found for "+Columns.ID_ELEMENT_RESEAU.name(), ex);
        }
    }
}
