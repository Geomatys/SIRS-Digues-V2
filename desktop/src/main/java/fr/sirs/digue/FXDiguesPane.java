package fr.sirs.digue;

import fr.sirs.core.model.SystemeEndiguement;
import fr.sirs.Injector;
import fr.sirs.Session;
import fr.sirs.SIRS;
import fr.sirs.theme.Theme;
import fr.sirs.core.model.Digue;
import fr.sirs.core.model.TronconDigue;
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

public class FXDiguesPane extends SplitPane{

    @Autowired
    private Session session;

    @FXML private TreeView uiTree;
    @FXML private BorderPane uiRight;

    public FXDiguesPane() {
        SIRS.loadFXML(this);
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
        FXTronconDiguePane ctrl = new FXTronconDiguePane();
        ctrl.setTroncon((TronconDigue) obj);
        uiRight.setCenter(ctrl);
        this.session.prepareToPrint(obj);
    }
    
    public final void displayDigue(Digue obj){
        FXDiguePane digueController = new FXDiguePane();
        digueController.setDigue((Digue) obj);
        uiRight.setCenter(digueController);
        this.session.prepareToPrint(obj);
    }

    @FXML
    private void openSearchPopup(ActionEvent event) {
        System.out.println("TODO");
    }

    private void buildTreeView(final TreeItem treeRootItem) {

        final SystemeEndiguement systemeEndiguement = new SystemeEndiguement();
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

            if (obj instanceof SystemeEndiguement) {
                this.setText(((SystemeEndiguement) obj).nom + " (" + getTreeItem().getChildren().size() + ") ");
                addTronconMenu.getItems().clear();
                addTronconMenu.getItems().add(new NouvelleDigueMenuItem());
                setContextMenu(addTronconMenu);
            } else if (obj instanceof Digue) {
                this.setText(((Digue) obj).getLibelle() + " (" + getTreeItem().getChildren().size() + ") ");
                addTronconMenu.getItems().clear();
                addTronconMenu.getItems().add(new NouveauTronconMenuItem((Digue) obj));
                setContextMenu(addTronconMenu);
            } else if (obj instanceof TronconDigue) {
                this.setText(((TronconDigue) obj).getLibelle() + " (" + getTreeItem().getChildren().size() + ") ");
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
                    troncon.setLibelle("Tronçon vide");
                    troncon.setDigueId(digue.getId());
                    FXDiguesPane.this.session.getTronconDigueRepository().add(troncon);
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
                    FXDiguesPane.this.session.getDigueRepository().add(digue);
                    final TreeItem newDigue = new TreeItem<>(digue);
                    getTreeItem().getChildren().add(newDigue);
                });
            }
        }
    }
    
}
