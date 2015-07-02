
package fr.sirs.plugin.reglementaire.ui;

import fr.sirs.core.model.Element;
import fr.sirs.core.model.RapportModeleObligationReglementaire;
import fr.sirs.theme.ui.PojoTable;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class RapportsTable extends PojoTable {

    public RapportsTable() {
        super(RapportModeleObligationReglementaire.class, "Modèles de document préenregistrées");
        editableProperty().set(true);
        detaillableProperty().set(false);
        fichableProperty().set(false);
        importPointProperty().set(false);
        commentAndPhotoProperty().set(false);
        searchVisibleProperty().set(false);
        exportVisibleProperty().set(false);
        ficheModeVisibleProperty().set(false);
        filterVisibleProperty().set(false);
    }

    @Override
    protected Element createPojo() {
        final RapportModeleObligationReglementaire rapport = RapportPane.showCreateDialog();
        if(rapport!=null){
            repo.add(rapport);
            filteredValues.add(rapport);
        }
        return rapport;
    }

}
