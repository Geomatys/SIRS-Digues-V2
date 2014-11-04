package fr.sym.digue;

import fr.sym.theme.Theme;
import fr.sym.*;
import fr.symadrem.sirs.core.model.Digue;
import fr.symadrem.sirs.core.model.TronconDigue;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import org.springframework.beans.factory.annotation.Autowired;

public class DiguesController extends SplitPane{

    @Autowired
    private Session session;

    @FXML private TreeView uiTree;
    @FXML private BorderPane uiRight;

    public DiguesController() {
        Symadrem.loadFXML(this);
        Injector.injectDependencies(this);
        
        this.buildTreeView(new TreeItem("root"));
        this.uiTree.setShowRoot(false);
        this.uiTree.setCellFactory((Object param) -> new CustomizedTreeCell());

        this.uiTree.getSelectionModel().getSelectedIndices().addListener((ListChangeListener.Change c) -> {
            Object obj = this.uiTree.getSelectionModel().getSelectedItem();
            if (obj instanceof TreeItem) {
                obj = ((TreeItem) obj).getValue();
            }

            if (obj instanceof Digue) {
                displayDigue((Digue) obj);
            } else if (obj instanceof TronconDigue) {
                displayTronconDigue((TronconDigue) obj);
            }
        });
    }
    
    public final void displayTronconDigue(TronconDigue obj){
        TronconDigueController ctrl = new TronconDigueController();
        ctrl.setTroncon((TronconDigue) obj);
        uiRight.setCenter(ctrl);
        this.session.prepareToPrint(obj);
    }
    
    public final void displayDigue(Digue obj){
        DigueController digueController = new DigueController();
        digueController.setDigue((Digue) obj);
        uiRight.setCenter(digueController);
        this.session.prepareToPrint(obj);
    }

    @FXML
    private void openSearchPopup(ActionEvent event) {
        System.out.println("TODO");
    }

    private void buildTreeView(final TreeItem treeRootItem) {

        final SystemeEndiguementProvisoire systemeEndiguement = new SystemeEndiguementProvisoire();
        systemeEndiguement.nom = "Un systeme d'endiguement";
        
        final TreeItem systemeEndiguementItem = new TreeItem(systemeEndiguement);
        treeRootItem.getChildren().add(systemeEndiguementItem);
        
        this.session.getDigues().stream().forEach((digue) -> {
            final TreeItem digueItem = new TreeItem(digue);

            this.session.getTronconDigueByDigue(digue).stream().forEach((troncon) -> {
                final TreeItem tronconItem = new TreeItem(troncon);
                digueItem.getChildren().add(tronconItem);
            });
            systemeEndiguementItem.getChildren().add(digueItem);
        });

        this.uiTree.setRoot(treeRootItem);
    }

    private class CustomizedTreeCell extends TreeCell {

        private final ContextMenu addTronconMenu;

        public CustomizedTreeCell() {
            addTronconMenu = new ContextMenu();
        }

        @Override
        protected void updateItem(Object obj, boolean empty) {

            super.updateItem(obj, empty);

            if (obj instanceof TreeItem) {
                obj = ((TreeItem) obj).getValue();
            }

            if (obj instanceof SystemeEndiguementProvisoire) {
                this.setText(((SystemeEndiguementProvisoire) obj).nom + " (" + getTreeItem().getChildren().size() + ") ");
                addTronconMenu.getItems().clear();
                addTronconMenu.getItems().add(new NouvelleDigueMenuItem());
                setContextMenu(addTronconMenu);
            } else if (obj instanceof Digue) {
                this.setText(((Digue) obj).getLibelle() + " (" + getTreeItem().getChildren().size() + ") ");
                addTronconMenu.getItems().clear();
                addTronconMenu.getItems().add(new NouveauTronconMenuItem((Digue) obj));
                setContextMenu(addTronconMenu);
            } else if (obj instanceof TronconDigue) {
                this.setText(((TronconDigue) obj).getNom() + " (" + getTreeItem().getChildren().size() + ") ");
                setContextMenu(null);
            } else if (obj instanceof Theme) {
                setText(((Theme) obj).getName());
            } else {
                setText(null);
            }
        }

        private class NouveauTronconMenuItem extends MenuItem {

            public NouveauTronconMenuItem(final Digue digue) {
                super("Créer un nouveau tronçon");

                this.setOnAction((ActionEvent t) -> {
                    final TronconDigue troncon = new TronconDigue();
                    troncon.setNom("Tronçon vide");
                    troncon.setDigueId(digue.getId());
                    DiguesController.this.session.getTronconDigueRepository().add(troncon);
                    final TreeItem newTroncon = new TreeItem<>(troncon);
                    getTreeItem().getChildren().add(newTroncon);
                });
            }
        }

        private class NouvelleDigueMenuItem extends MenuItem {

            public NouvelleDigueMenuItem() {
                super("Créer une nouvelle digue");

                this.setOnAction((ActionEvent t) -> {
                    final Digue digue = new Digue();
                    digue.setLibelle("Digue vide");
                    DiguesController.this.session.getDigueRepository().add(digue);
                    final TreeItem newDigue = new TreeItem<>(digue);
                    getTreeItem().getChildren().add(newDigue);
                });
            }
        }
    }
    
}
