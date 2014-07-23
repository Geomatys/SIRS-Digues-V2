

package fr.sym.digue;

import fr.sym.*;
import fr.sym.digue.dto.Dam;
import fr.sym.digue.dto.DamSystem;
import fr.sym.digue.dto.Section;
import fr.sym.util.WrapTreeItem;
import java.io.IOException;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;

public class FXDigueController {

    public Parent root;
     
    @FXML
    private BorderPane uiRight;
    
    @FXML
    private TreeView uiTree;

    private void init(){
        
        final TreeItem root = new TreeItem("root");
        
        //build the tree
        for(DamSystem ds : Session.getInstance().getDamSystems()){
            root.getChildren().add(new WrapTreeItem(ds));
        }
        
        uiTree.setRoot(root);
        uiTree.setShowRoot(false);
        uiTree.setCellFactory((Object param) -> new TC());
        
        uiTree.getSelectionModel().getSelectedIndices().addListener(new ListChangeListener() {

            @Override
            public void onChanged(ListChangeListener.Change c) {
                Object obj = uiTree.getSelectionModel().getSelectedItem();
                if(obj instanceof TreeItem){
                    obj = ((TreeItem)obj).getValue();
                }
                
                if(obj instanceof Dam){
                    final FXDamController ctrl = FXDamController.create();
                    uiRight.setCenter(ctrl.root);
                }else if(obj instanceof Section){
                    final FXSectionController ctrl = FXSectionController.create();
                    uiRight.setCenter(ctrl.root);
                }
                
            }
        });
        
    }
    
    @FXML
    void openSearchPopup(ActionEvent event) {
        System.out.println("TODO");
    }
    
    
    public static FXDigueController create() {
        final FXMLLoader loader = new FXMLLoader(Symadrem.class.getResource("/fr/sym/digue/diguesBase.fxml"));
        final Parent root;
        try {
            root = loader.load();
        } catch (IOException ex) {
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }
        final FXDigueController controller = loader.getController();
        controller.root = root;
        controller.init();
        return controller;
    }
    
    public static class TC extends TreeCell{

        @Override
        protected void updateItem(Object obj, boolean empty) {
            super.updateItem(obj, empty);
            
            if(obj instanceof TreeItem){
                obj = ((TreeItem)obj).getValue();
            }
            if(obj instanceof DamSystem){
                setText( ((DamSystem)obj).getName().getValue()+" ("+getTreeItem().getChildren().size()+")");
            }else if(obj instanceof Dam){
                setText( ((Dam)obj).getName().getValue()+" ("+getTreeItem().getChildren().size()+")");
            }else if(obj instanceof Section){
                setText( ((Section)obj).getName().getValue()+" ("+getTreeItem().getChildren().size()+")");
            }else if(obj instanceof Theme){
                setText( ((Theme)obj).getName());
            }else{
                setText(null);
            }
        }
        
    }
    
}
