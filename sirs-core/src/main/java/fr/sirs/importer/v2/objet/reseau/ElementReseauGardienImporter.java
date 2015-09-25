package fr.sirs.importer.v2.objet.reseau;

import fr.sirs.core.model.GardeObjet;
import fr.sirs.core.model.ObjetReseau;
import static fr.sirs.importer.DbImporter.TableName.ELEMENT_RESEAU_GARDIEN;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
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
    protected Class<GardeObjet> getElementClass() {
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
