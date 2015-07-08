
package fr.sirs.plugin.reglementaire.ui;

import fr.sirs.Injector;
import fr.sirs.core.model.TemplateObligationReglementaire;
import fr.sirs.theme.ui.PojoTable;
import javafx.collections.FXCollections;
import javafx.scene.control.TableColumn;

/**
 *
 * @author Johann Sorel
 */
public class TemplatesTable extends PojoTable {

    public TemplatesTable() {
        super(Injector.getSession().getRepositoryForClass(TemplateObligationReglementaire.class), "Modèles de mise en forme");
        setTableItems(() -> FXCollections.observableList(repo.getAll()));
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
    protected TemplateObligationReglementaire createPojo() {
        final TemplateObligationReglementaire ele = TemplatePane.showCreateDialog();
        if(ele!=null){
            repo.add(ele);
            getAllValues().add(ele);
        }
        return ele;
    }

    @Override
    protected void editPojo(Object pojo) {
        final TemplateObligationReglementaire rapport = TemplatePane.showEditDialog((TemplateObligationReglementaire) pojo);
        if(rapport!=null){
            repo.update(rapport);

        }
    }

}
