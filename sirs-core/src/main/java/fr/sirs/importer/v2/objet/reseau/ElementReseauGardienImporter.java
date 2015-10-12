package fr.sirs.importer.v2.objet.reseau;

import fr.sirs.core.model.GardeObjet;
import fr.sirs.core.model.ObjetReseau;
import static fr.sirs.importer.DbImporter.TableName.ELEMENT_RESEAU_GARDIEN;
import org.springframework.stereotype.Component;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class ElementReseauGardienImporter extends AbstractElementReseauGestionImporter<GardeObjet> {

    private enum Columns {
        ID_ELEMENT_RESEAU,
        ID_INTERV_GARDIEN
    }

    @Override
    public void put(ObjetReseau container, GardeObjet toPut) {
        container.gardes.add(toPut);
    }

    @Override
    public Class<GardeObjet> getElementClass() {
        return GardeObjet.class;
    }

    @Override
    public String getTableName() {
        return ELEMENT_RESEAU_GARDIEN.name();
    }

    @Override
    public String getRowIdFieldName() {
        return Columns.ID_INTERV_GARDIEN.name();
    }

}
