package fr.sirs.importer.v2.objet.reseau;

import fr.sirs.core.model.GestionObjet;
import fr.sirs.core.model.ObjetReseau;
import static fr.sirs.importer.DbImporter.TableName.ELEMENT_RESEAU_GESTIONNAIRE;
import org.springframework.stereotype.Component;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class ElementReseauGestionImporter extends AbstractElementReseauGestionImporter<GestionObjet> {

    private enum Columns {
        ID_ORG_GESTION
    }

    @Override
    public void put(ObjetReseau container, GestionObjet toPut) {
        container.gestions.add(toPut);
    }

    @Override
    protected Class<GestionObjet> getElementClass() {
        return GestionObjet.class;
    }

    @Override
    public String getTableName() {
        return ELEMENT_RESEAU_GESTIONNAIRE.name();
    }

    @Override
    public String getRowIdFieldName() {
        return Columns.ID_ORG_GESTION.name();
    }

}
