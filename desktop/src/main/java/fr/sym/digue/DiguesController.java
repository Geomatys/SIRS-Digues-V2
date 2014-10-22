package fr.sym.digue;

import fr.sym.theme.Theme;
import fr.sym.*;
import fr.symadrem.sirs.core.model.Digue;
import fr.symadrem.sirs.core.model.TronconDigue;
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
import org.springframework.beans.factory.annotation.Autowired;

public class DiguesController {

    public Parent root;

    @Autowired
    private Session session;

    @FXML
    private TreeView uiTree;

    @FXML
    private BorderPane uiRight;

    @FXML
    private void openSearchPopup(ActionEvent event) {
        System.out.println("TODO");
    }
    
    private void buildTreeView(final TreeItem treeRootItem){
        
        this.session.getDigues().stream().forEach((digue) -> {
            final TreeItem digueItem = new TreeItem(digue);
            
            this.session.getTronconDigueByDigue(digue).stream().forEach((troncon) -> {
                final TreeItem tronconItem = new TreeItem(troncon);
                digueItem.getChildren().add(tronconItem);
            });
            treeRootItem.getChildren().add(digueItem);
        });
        
        this.uiTree.setRoot(treeRootItem);
    }

    private void init() {

        this.buildTreeView(new TreeItem("root"));
        this.uiTree.setShowRoot(false);
        this.uiTree.setCellFactory((Object param) -> new CustomizedTreeCell());

        this.uiTree.getSelectionModel().getSelectedIndices().addListener((ListChangeListener.Change c) -> 
        {
            Object obj = this.uiTree.getSelectionModel().getSelectedItem();
            if (obj instanceof TreeItem) {
                obj = ((TreeItem) obj).getValue();
            }

            if (obj instanceof Digue) {
                uiRight.setCenter(DigueController.create(this.uiTree));
            } else if (obj instanceof TronconDigue) {
                uiRight.setCenter(TronconDigueController.create(this.uiTree));
            }
        });
    }

    public static DiguesController create() {

        final FXMLLoader loader = new FXMLLoader(Symadrem.class.getResource(
                "/fr/sym/digue/diguesDisplay.fxml"));
        final Parent root;

        try {
            root = loader.load();
        } catch (IOException ex) {
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }

        final DiguesController controller = loader.getController();
        Injector.injectDependencies(controller);
        controller.root = root;
        controller.init();
        return controller;
    }

    private static class CustomizedTreeCell extends TreeCell {

        @Override
        protected void updateItem(Object obj, boolean empty) {
            
            super.updateItem(obj, empty);

            if (obj instanceof TreeItem) {
                obj = ((TreeItem) obj).getValue();
            }

            if (obj instanceof Digue) {
                this.setText(((Digue) obj).getLibelle() + " (" + getTreeItem().getChildren().size() + ") ");
            } else if (obj instanceof TronconDigue) {
                this.setText(((TronconDigue) obj).getNom() + " (" + getTreeItem().getChildren().size() + ") ");
            } 
                        
            else if (obj instanceof Theme) {
                setText(((Theme) obj).getName());
            } else {
                setText(null);
            }
        }

    }
}
