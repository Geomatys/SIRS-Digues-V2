
package fr.sirs.ui;

import fr.sirs.Injector;
import fr.sirs.core.model.report.ModeleElement;
import fr.sirs.theme.ui.PojoTable;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;

/**
 *
 * @author Johann Sorel
 */
public class TemplatesTable extends PojoTable {

    public TemplatesTable() {
        super(Injector.getSession().getRepositoryForClass(ModeleElement.class), "Modèles .odt enregistrés en base");
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
        getColumns().add((TableColumn) new StateColumn());
    }

    @Override
    protected ModeleElement createPojo() {
        final ModeleElement ele = TemplatePane.showCreateDialog();
        if(ele!=null){
            repo.add(ele);
            getAllValues().add(ele);
        }
        return ele;
    }

    @Override
    protected void editPojo(Object pojo) {
        final ModeleElement rapport = TemplatePane.showEditDialog((ModeleElement) pojo);
        if(rapport!=null){
            repo.update(rapport);
        }
    }

    private static class StateColumn extends TableColumn<ModeleElement, byte[]>{
        public StateColumn(){
            super("État du modèle");
            setCellValueFactory((CellDataFeatures<ModeleElement, byte[]> param) -> param.getValue().odtProperty());
            setCellFactory((TableColumn<ModeleElement, byte[]> param) ->  new StateCell());
        }
    }

    private static class StateCell extends TableCell<ModeleElement, byte[]>{
        public StateCell(){
            super();
            setEditable(false);
        }

        @Override
        public void updateItem(final byte[] item, final boolean empty){

            setGraphic(null);
            if(empty){
                setStyle(null);
                setText(null);
            }
            else if(item==null || item.length==0){
                setStyle("-fx-background-color: red");
                setText("Aucun modèle défini");
            }
            else{
                setStyle("-fx-background-color: green");
                setText("Modèle présent");
            }
        }
    }
}
