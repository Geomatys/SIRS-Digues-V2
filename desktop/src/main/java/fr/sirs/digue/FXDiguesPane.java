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
import javafx.scene.control.MenuButton;
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
    @FXML private MenuButton uiAdd;

    //etat de la recherche
    private final ImageView searchNone = new ImageView(SIRS.ICON_SEARCH);
    private final ImageView iconAdd = new ImageView(SIRS.ICON_ADD_WHITE);
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
        
        uiAdd.setGraphic(iconAdd);
        uiAdd.getItems().add(new NewSystemeMenuItem(null));
        uiAdd.getItems().add(new NewDigueMenuItem(null));
        uiAdd.getItems().add(new NewTronconMenuItem(null));
        
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
                
                //creation de l'arbre
                final TreeItem treeRootItem = new TreeItem("root");
                
                //on recupere tous les elements
                final List<SystemeEndiguement> sds = session.getSystemeEndiguementRepository().getAll();
                final Set<Digue> digues = new HashSet<>(session.getDigues());
                final List<TronconDigue> troncons = session.getTronconDigueRepository().getAllLight();
                final Set<Digue> diguesFound = new HashSet<>();
                
                for(SystemeEndiguement sd : sds){
                    final TreeItem sdItem = new TreeItem(sd);
                    treeRootItem.getChildren().add(sdItem);
                    sdItem.setExpanded(true);

                    final List<String> digueIds = sd.getDigue();
                    for(Digue digue : digues){
                        if(!digueIds.contains(digue.getDocumentId())) continue;
                        diguesFound.add(digue);
                        final TreeItem digueItem = toNode(digue, troncons, filter);
                        sdItem.getChildren().add(digueItem);
                    }
                }
                
                //on place toute les digues et troncons non trouvé dans un group a part
                digues.removeAll(diguesFound);
                final TreeItem ncItem = new TreeItem("Non classés"); 
                ncItem.setExpanded(true);
                treeRootItem.getChildren().add(ncItem);
                
                for(Digue digue : digues){
                    final TreeItem digueItem = toNode(digue, troncons, filter);
                    ncItem.getChildren().add(digueItem);
                }
                for(TronconDigue tc : troncons){
                    ncItem.getChildren().add(new TreeItem(tc));
                }
                
                Platform.runLater(() -> {
                    uiTree.setRoot(treeRootItem);
                    uiSearch.setGraphic(searchNone);
                });
            }
        }.start();
        
    }

    private static TreeItem toNode(Digue digue, List<TronconDigue> troncons, Predicate<Element> filter){
        final TreeItem digueItem = new TreeItem(digue);
        for(int i=troncons.size()-1;i>=0;i--){
            final TronconDigue td = troncons.get(i);
            if(!td.getDigueId().equals(digue.getDocumentId())) continue;
            troncons.remove(i);
            if(filter==null || filter.test(td)){
                final TreeItem tronconItem = new TreeItem(td);
                digueItem.getChildren().add(tronconItem);
            }
        }
        return digueItem;
    }
    
    private class NewTronconMenuItem extends MenuItem {

        public NewTronconMenuItem(TreeItem parent) {
            super("Créer un nouveau tronçon",new ImageView(SIRS.ICON_ADD_WHITE));

            this.setOnAction((ActionEvent t) -> {
                final TronconDigue troncon = new TronconDigue();
                troncon.setLibelle("Tronçon vide");
                if(parent!=null){
                    final Digue digue = (Digue) parent.getValue();
                    troncon.setDigueId(digue.getId());
                }
                FXDiguesPane.this.session.getTronconDigueRepository().add(troncon);
                if(parent != null){
                    final TreeItem newTroncon = new TreeItem<>(troncon);
                    parent.getChildren().add(newTroncon);
                }else{
                    buildTreeView();
                }
            });
        }
    }
        
    private class NewDigueMenuItem extends MenuItem {

        public NewDigueMenuItem(TreeItem parent) {
            super("Créer une nouvelle digue",new ImageView(SIRS.ICON_ADD_WHITE));

            this.setOnAction((ActionEvent t) -> {
                final Digue digue = new Digue();
                digue.setLibelle("Digue vide");
                FXDiguesPane.this.session.getDigueRepository().add(digue);
                if(parent!=null){
                    final TreeItem newDigue = new TreeItem<>(digue);
                    parent.getChildren().add(newDigue);
                }else{
                    buildTreeView();
                }
            });
        }
    }
    
    private class NewSystemeMenuItem extends MenuItem {

        public NewSystemeMenuItem(TreeItem parent) {
            super("Créer un nouveau système d'endiguement",new ImageView(SIRS.ICON_ADD_WHITE));

            this.setOnAction((ActionEvent t) -> {
                final SystemeEndiguement candidate = new SystemeEndiguement();
                candidate.setLibelle("Système vide");
                FXDiguesPane.this.session.getSystemeEndiguementRepository().add(candidate);
                if(parent!=null){
                    final TreeItem newDigue = new TreeItem<>(candidate);
                    parent.getChildren().add(newDigue);
                }else{
                    buildTreeView();
                }
            });
        }
    }
    
    private class CustomizedTreeCell extends TreeCell {

        private final ContextMenu addTronconMenu;

        public CustomizedTreeCell() {
            addTronconMenu = new ContextMenu();
        }

        @Override
        protected void updateItem(Object obj, boolean empty) {
            super.updateItem(obj, empty);
            setContextMenu(null);

            if (obj instanceof TreeItem) {
                obj = ((TreeItem) obj).getValue();
            }

            if (obj instanceof SystemeEndiguement) {
                this.setText(((SystemeEndiguement) obj).getLibelle() + " (" + getTreeItem().getChildren().size() + ") ");
                addTronconMenu.getItems().clear();
                addTronconMenu.getItems().add(new NewDigueMenuItem(getTreeItem()));
                setContextMenu(addTronconMenu);
            } else if (obj instanceof Digue) {
                this.setText(((Digue) obj).getLibelle() + " (" + getTreeItem().getChildren().size() + ") ");
                addTronconMenu.getItems().clear();
                addTronconMenu.getItems().add(new NewTronconMenuItem(getTreeItem()));
                addTronconMenu.getItems().add(new DeleteDigueMenuItem((Digue) obj));
                setContextMenu(addTronconMenu);
            } else if (obj instanceof TronconDigue) {
                this.setText(((TronconDigue) obj).getLibelle() + " (" + getTreeItem().getChildren().size() + ") ");
                setContextMenu(null);
            } else if (obj instanceof Theme) {
                setText(((Theme) obj).getName());
            } else if( obj instanceof String){
                setText((String)obj);
            } else {
                setText(null);
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

    }
    
}
