package fr.sirs.importer.v2.objet;

import fr.sirs.core.model.LigneEau;
import fr.sirs.core.model.MesureLigneEauPrZ;
import static fr.sirs.importer.DbImporter.TableName.LIGNE_EAU_MESURES_PRZ;
import fr.sirs.importer.v2.SimpleUpdater;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class MesureLigneEauPrZImporter extends SimpleUpdater<MesureLigneEauPrZ, LigneEau> {

    @Override
    public String getDocumentIdField() {
        return masterImporter.getRowIdFieldName();
    }

    @Override
    protected Class<MesureLigneEauPrZ> getElementClass() {
        return MesureLigneEauPrZ.class;
    }

    @Override
    public String getTableName() {
        return LIGNE_EAU_MESURES_PRZ.name();
    }

    @Override
    public String getRowIdFieldName() {
        return "ID_POINT";
    }

    @Override
    public void put(LigneEau container, MesureLigneEauPrZ toPut) {
        container.getMesuresDZ().add(toPut);
    }

    @Override
    public Class<LigneEau> getDocumentClass() {
        return LigneEau.class;
    }

}
