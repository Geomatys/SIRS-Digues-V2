
package fr.sirs.ui;

import fr.sirs.Injector;
import fr.sirs.core.model.TemplateOdt;
import fr.sirs.theme.ui.PojoTable;
import javafx.collections.FXCollections;
import javafx.scene.control.TableColumn;

/**
 *
 * @author Johann Sorel
 */
public class TemplatesTable extends PojoTable {

    public TemplatesTable() {
        super(Injector.getSession().getRepositoryForClass(TemplateOdt.class), "Modèles disponibles");
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
    protected TemplateOdt createPojo() {
        final TemplateOdt ele = TemplatePane.showCreateDialog();
        if(ele!=null){
            repo.add(ele);
            getAllValues().add(ele);
        }
        return ele;
    }

    @Override
    protected void editPojo(Object pojo) {
        final TemplateOdt rapport = TemplatePane.showEditDialog((TemplateOdt) pojo);
        if(rapport!=null){
            repo.update(rapport);

        }
    }

}
