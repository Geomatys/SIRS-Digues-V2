package fr.sirs.digue;

import fr.sirs.core.model.SystemeEndiguement;
import fr.sirs.Injector;
import fr.sirs.Session;
import fr.sirs.SIRS;
import fr.sirs.theme.Theme;
import fr.sirs.core.model.Digue;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.index.SearchEngine;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Level;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Popup;
import org.apache.lucene.queryparser.classic.ParseException;
import org.ektorp.BulkDeleteDocument;
import org.springframework.beans.factory.annotation.Autowired;

public class FXDiguesPane extends SplitPane{

    @Autowired
    private Session session;

    @FXML private TreeView uiTree;
    @FXML private BorderPane uiRight;
    @FXML private Button uiSearch;

    //etat de la recherche
    private final ImageView searchNone = new ImageView(SIRS.ICON_SEARCH);
    private final ProgressIndicator searchRunning = new ProgressIndicator();
    private final StringProperty currentSearch = new SimpleStringProperty("");

    public FXDiguesPane() {
        SIRS.loadFXML(this);
        Injector.injectDependencies(this);
        
        this.uiTree.setShowRoot(false);
        this.uiTree.setCellFactory((Object param) -> new CustomizedTreeCell());

        this.uiTree.getSelectionModel().getSelectedIndices().addListener((ListChangeListener.Change c) -> {
            Object obj = this.uiTree.getSelectionModel().getSelectedItem();
            if (obj instanceof TreeItem) {
                obj = ((TreeItem) obj).getValue();
            }

            if (obj instanceof SystemeEndiguement) {
                displaySystemeEndiguement((SystemeEndiguement)obj);
            }else if (obj instanceof Digue) {
                displayDigue((Digue)obj);
            } else if (obj instanceof TronconDigue) {
                //le troncon dans l'arbre est une version 'light'
                TronconDigue td = (TronconDigue)obj;
                td = session.getTronconDigueRepository().get(td.getDocumentId());
                displayTronconDigue(td);
            }
        });
        
        searchRunning.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        searchRunning.setPrefSize(22, 22);
        searchRunning.setStyle(" -fx-progress-color: white;");
        
        uiSearch.setGraphic(searchNone);
        uiSearch.textProperty().bind(currentSearch);
        uiSearch.getStyleClass().add("btn-without-style");
        uiSearch.getStyleClass().add("label-header");
        
        this.buildTreeView();
    }
    
    public final void displayTronconDigue(TronconDigue obj){
        FXTronconDiguePane ctrl = new FXTronconDiguePane();
        ctrl.setElement((TronconDigue) obj);
        uiRight.setCenter(ctrl);
        this.session.prepareToPrint(obj);
    }
    
    public final void displayDigue(Digue obj){
        FXDiguePane digueController = new FXDiguePane();
        digueController.setElement((Digue) obj);
        uiRight.setCenter(digueController);
        this.session.prepareToPrint(obj);
    }
    
    public final void displaySystemeEndiguement(SystemeEndiguement obj){
        FXSystemeEndiguementPane pane = new FXSystemeEndiguementPane();
        pane.systemeEndiguementProp().set(obj);
        uiRight.setCenter(pane);
        this.session.prepareToPrint(obj);
    }

    @FXML
    private void openSearchPopup(ActionEvent event) {
        if(uiSearch.getGraphic()!= searchNone){
            //une recherche est deja en cours
            return;
        }
        
        final Popup popup = new Popup();
        final TextField textField = new TextField(currentSearch.get());
        popup.getContent().add(textField);
        
        textField.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                currentSearch.set(textField.getText());
                popup.hide();
                buildTreeView();
            }
        });
        final Point2D sc = uiSearch.localToScreen(0, 0);
        popup.show(uiSearch, sc.getX(), sc.getY());
    }

    private void buildTreeView() {
        uiSearch.setGraphic(searchRunning);
        
        new Thread(){
            @Override
            public void run() {
                //creation du filtre
                final String str = currentSearch.get();
                final Predicate<Element> filter;
                if(str == null || str.isEmpty()){
                    filter = null;
                }else{
                    final SearchEngine searchEngine = Injector.getSearchEngine();
                    final String type = TronconDigue.class.getSimpleName();
                    final Set<String> result = new HashSet<>();
                    try {
                        result.addAll(searchEngine.search(type, str.split(" ")));
                    } catch (ParseException | IOException ex) {
                        SIRS.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
                    }

                    filter = (Element t) -> {return result.contains(t.getDocumentId());};
                }
                
                final TreeItem treeRootItem = new TreeItem("root");
                        
                final SystemeEndiguement systemeEndiguement = new SystemeEndiguement();
                systemeEndiguement.setLibelle("Un systeme d'endiguement");

                final TreeItem systemeEndiguementItem = new TreeItem(systemeEndiguement);
                treeRootItem.getChildren().add(systemeEndiguementItem);
                systemeEndiguementItem.setExpanded(true);

                //on recupere tous les troncons
                final List<TronconDigue> troncons = session.getTronconDigueRepository().getAllLight();
                
                for(Digue digue : session.getDigues()){
                    final TreeItem digueItem = new TreeItem(digue);
                    for(TronconDigue td : troncons){
                        if(td.getDigueId().equals(digue.getDocumentId()) && (filter==null || filter.test(td))){
                            final TreeItem tronconItem = new TreeItem(td);
                            digueItem.getChildren().add(tronconItem);
                        }
                    }
                    systemeEndiguementItem.getChildren().add(digueItem);
                }
                
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        uiTree.setRoot(treeRootItem);
                        uiSearch.setGraphic(searchNone);
                    }
                });
            }
        }.start();
        
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
                this.setText(((SystemeEndiguement) obj).getLibelle() + " (" + getTreeItem().getChildren().size() + ") ");
                addTronconMenu.getItems().clear();
                addTronconMenu.getItems().add(new NewDigueMenuItem());
                setContextMenu(addTronconMenu);
            } else if (obj instanceof Digue) {
                this.setText(((Digue) obj).getLibelle() + " (" + getTreeItem().getChildren().size() + ") ");
                addTronconMenu.getItems().clear();
                addTronconMenu.getItems().add(new NewTronconMenuItem((Digue) obj));
                addTronconMenu.getItems().add(new DeleteDigueMenuItem((Digue) obj));
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

        private class NewTronconMenuItem extends MenuItem {

            public NewTronconMenuItem(final Digue digue) {
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

        private class DeleteDigueMenuItem extends MenuItem {

            public DeleteDigueMenuItem(final Digue digue) {
                super("Supprimer la digue");

                this.setOnAction((ActionEvent t) -> {

                    final Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                            "La suppression de la digue supprimera les tronçons qui la composent.",
                            ButtonType.OK, ButtonType.CANCEL
                    );
                    final ButtonType res = alert.showAndWait().get();
                    if (res == ButtonType.OK) {

                        final List<TronconDigue> tronconsToDelete = FXDiguesPane.this.session.getTronconDigueRepository().getByDigue(digue);
                        final List<Object> bulkDocs = new ArrayList<>();
                        for (final TronconDigue troncon : tronconsToDelete) {
                            bulkDocs.add(BulkDeleteDocument.of(troncon));
                        }
                        FXDiguesPane.this.session.getConnector().executeBulk(bulkDocs);
                        FXDiguesPane.this.session.getDigueRepository().remove(digue);
                        getTreeItem().getChildren().remove(0, getTreeItem().getChildren().size());
                        getTreeItem().getParent().getChildren().remove(getTreeItem());
                    }
                });
            }
        }

        private class NewDigueMenuItem extends MenuItem {

            public NewDigueMenuItem() {
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
