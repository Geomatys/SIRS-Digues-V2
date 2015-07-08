
package fr.sirs.plugin.reglementaire.ui;

import fr.sirs.Injector;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.RapportModeleObligationReglementaire;
import fr.sirs.theme.ui.PojoTable;
import javafx.scene.control.TableColumn;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class RapportsTable extends PojoTable {

    public RapportsTable() {
        super(Injector.getSession().getRepositoryForClass(RapportModeleObligationReglementaire.class), "Modèles de document préenregistrés");
        editableProperty().set(true);
        detaillableProperty().set(true);
        fichableProperty().set(false);
        importPointProperty().set(false);
        commentAndPhotoProperty().set(false);
        searchVisibleProperty().set(false);
        exportVisibleProperty().set(false);
        ficheModeVisibleProperty().set(false);
        filterVisibleProperty().set(false);
        openEditorOnNewProperty().set(false);
        
        for(TableColumn col : getColumns()){
            if("Désignation".equalsIgnoreCase(col.getText())){
                col.setVisible(false);
            }
        }
    }

    @Override
    protected Element createPojo() {
        final RapportModeleObligationReglementaire rapport = RapportPane.showCreateDialog();
        if(rapport!=null){
            repo.add(rapport);
            getAllValues().add(rapport);
        }
        return rapport;
    }

    @Override
    protected void editPojo(Object pojo) {
        final RapportModeleObligationReglementaire rapport = RapportPane.showEditDialog((RapportModeleObligationReglementaire) pojo);
        if(rapport!=null){
            repo.update(rapport);
            
        }
    }

}
