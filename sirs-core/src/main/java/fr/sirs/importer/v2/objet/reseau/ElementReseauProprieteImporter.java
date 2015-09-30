package fr.sirs.importer.v2.objet.reseau;

import fr.sirs.core.model.ProprieteObjet;
import fr.sirs.core.model.ObjetReseau;
import static fr.sirs.importer.DbImporter.TableName.ELEMENT_RESEAU_PROPRIETAIRE;
import org.springframework.stereotype.Component;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class ElementReseauProprieteImporter extends AbstractElementReseauGestionImporter<ProprieteObjet> {

    private enum Columns {
        ID_ORG_PROPRIO,
        ID_INTERV_PROPRIO
    }

    @Override
    public void put(ObjetReseau container, ProprieteObjet toPut) {
        container.proprietes.add(toPut);
    }

    @Override
    protected Class<ProprieteObjet> getElementClass() {
        return ProprieteObjet.class;
    }

    @Override
    public String getTableName() {
        return ELEMENT_RESEAU_PROPRIETAIRE.name();
    }

    @Override
    public String getRowIdFieldName() {
        return Columns.ID_INTERV_PROPRIO.name();
    }

}
