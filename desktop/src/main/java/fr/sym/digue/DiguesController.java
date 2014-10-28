package fr.sym.digue;

import fr.sym.theme.Theme;
import fr.sym.*;
import fr.symadrem.sirs.core.model.Digue;
import fr.symadrem.sirs.core.model.TronconDigue;
import java.io.IOException;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;
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

    private void buildTreeView(final TreeItem treeRootItem) {

        SystemeEndiguementProvisoire systemeEndiguement = new SystemeEndiguementProvisoire();
        systemeEndiguement.nom = "Un systeme d'endiguement";
        
        TreeItem systemeEndiguementItem = new TreeItem(systemeEndiguement);
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

    private void init() {

        this.buildTreeView(new TreeItem("root"));
        this.uiTree.setShowRoot(false);
        this.uiTree.setCellFactory((Object param) -> new CustomizedTreeCell());

        this.uiTree.getSelectionModel().getSelectedIndices().addListener((ListChangeListener.Change c) -> {
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

        this.uiTree.setOnMouseClicked(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {
                System.out.println(event.getButton().toString());
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

    private class CustomizedTreeCell extends TreeCell {

        private ContextMenu addTronconMenu = new ContextMenu();
//        private Object currentObject;

        public CustomizedTreeCell() {

//            MenuItem addTronconMenuItem = new MenuItem("Nouveau tronçon");
//            addTronconMenu.getItems().add(addTronconMenuItem);
//            addTronconMenuItem.setOnAction(new EventHandler() {
//                public void handle(Event t) {
//                    TronconDigue toto = new TronconDigue();
//                    toto.setNom("Tronçon toto");
//                    toto.setDigueId(((Digue) currentObject).getId());
//                    DiguesController.this.session.getTronconDigueRepository().add(toto);
//                    TreeItem newTroncon = new TreeItem<TronconDigue>(toto);
//                    getTreeItem().getChildren().add(newTroncon);
//                }
//            });
            
            
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
//                this.currentObject = obj;
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
                super("Nouveau tronçon !");

                this.setOnAction(new EventHandler() {
                    public void handle(Event t) {
                        TronconDigue toto = new TronconDigue();
                        toto.setNom("Tronçon toto");
                        toto.setDigueId(digue.getId());
                        DiguesController.this.session.getTronconDigueRepository().add(toto);
                        TreeItem newTroncon = new TreeItem<TronconDigue>(toto);
                        getTreeItem().getChildren().add(newTroncon);
                    }
                });

            }
        }

        private class NouvelleDigueMenuItem extends MenuItem {

            public NouvelleDigueMenuItem() {
                super("Nouvelle digue !");

                this.setOnAction(new EventHandler() {
                    public void handle(Event t) {
                        Digue toto = new Digue();
                        toto.setLibelle("Digue toto");
                        DiguesController.this.session.getDigueRepository().add(toto);
                        TreeItem newDigue = new TreeItem<Digue>(toto);
                        getTreeItem().getChildren().add(newDigue);
                    }
                });

            }
        }

    }
    
    
    
    
    
    
    
    
    
    
    
    
    
}
