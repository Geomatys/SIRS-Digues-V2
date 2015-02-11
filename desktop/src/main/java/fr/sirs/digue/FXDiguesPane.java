package fr.sirs.digue;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import fr.sirs.core.model.SystemeEndiguement;
import fr.sirs.Injector;
import fr.sirs.Session;
import fr.sirs.SIRS;
import fr.sirs.core.SirsCore;
import fr.sirs.core.component.DocumentListener;
import fr.sirs.theme.Theme;
import fr.sirs.core.model.Digue;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.index.SearchEngine;
import fr.sirs.core.TronconUtils;
import fr.sirs.theme.ui.AbstractFXElementPane;
import fr.sirs.theme.ui.FXElementPane;
import java.io.IOException;
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
import org.apache.sis.referencing.CommonCRS;
import org.geotoolkit.geometry.jts.JTS;
import org.geotoolkit.referencing.CRS;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;
import org.springframework.beans.factory.annotation.Autowired;

public class FXDiguesPane extends SplitPane implements DocumentListener{

    /** Cette géométrie sert de base pour tous les nouveaux troncons */
    private static final Geometry TRONCON_GEOM_WGS84;
    static {
        TRONCON_GEOM_WGS84 = new GeometryFactory().createLineString(new Coordinate[]{
            new Coordinate(0, 48),
            new Coordinate(5, 48)
        });
        JTS.setCRS(TRONCON_GEOM_WGS84, CommonCRS.WGS84.normalizedGeographic());
    }
    
    @Autowired
    private Session session;

    @FXML private TreeView uiTree;
    @FXML private BorderPane uiRight;
    @FXML private Button uiSearch;
    @FXML private Button uiDelete;
    @FXML private MenuButton uiAdd;

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
        
        uiDelete.setGraphic(new ImageView(SIRS.ICON_TRASH));
        uiDelete.setOnAction(this::deleteSelection);
        uiDelete.setDisable(!session.nonGeometryEditionProperty().get());
        uiAdd.setGraphic(new ImageView(SIRS.ICON_ADD_WHITE));
        uiAdd.getItems().add(new NewSystemeMenuItem(null));
        uiAdd.getItems().add(new NewDigueMenuItem(null));
        uiAdd.getItems().add(new NewTronconMenuItem(null));
        uiAdd.setDisable(!session.nonGeometryEditionProperty().get());
        
        this.updateTree();
        
        
        //listen to changes in the db to update tree
        Injector.getDocumentChangeEmiter().addListener(this);
        
    }
    
    public final void displayTronconDigue(TronconDigue obj){
        AbstractFXElementPane ctrl = SIRS.generateEditionPane(obj);
        uiRight.setCenter(ctrl);
        this.session.prepareToPrint(obj);
    }
    
    public final void displayDigue(Digue obj){
        AbstractFXElementPane ctrl = SIRS.generateEditionPane(obj);
        uiRight.setCenter(ctrl);
        this.session.prepareToPrint(obj);
    }
    
    public final void displaySystemeEndiguement(SystemeEndiguement obj){
        AbstractFXElementPane ctrl = SIRS.generateEditionPane(obj);
        uiRight.setCenter(ctrl);
        this.session.prepareToPrint(obj);
    }

    private void deleteSelection(ActionEvent event) {
        Object obj = uiTree.getSelectionModel().getSelectedItem();
        if(obj instanceof TreeItem){
            obj = ((TreeItem)obj).getValue();
        }
        
        if(obj instanceof SystemeEndiguement){
            final SystemeEndiguement se = (SystemeEndiguement) obj;
            
            final Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                    "La suppression de la digue "+se.getLibelle()+" ne supprimera pas les digues qui la compose, "
                   +"celles ci seront déplacées dans le groupe 'Non classés. Confirmer la suppression ?",
                    ButtonType.YES, ButtonType.NO
            );
            final ButtonType res = alert.showAndWait().get();
            if (res == ButtonType.YES) {
                FXDiguesPane.this.session.getSystemeEndiguementRepository().remove(se);
            }
                        
        }else if(obj instanceof Digue){
            final Digue digue = (Digue) obj;
            final Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                    "La suppression de la digue "+digue.getLibelle()+" ne supprimera pas les tronçons qui la compose, "
                   +"ceux ci seront déplacés dans le groupe 'Non classés. Confirmer la suppression ?",
                    ButtonType.YES, ButtonType.NO
            );
            final ButtonType res = alert.showAndWait().get();
            if (res == ButtonType.YES) {
                //on enleve la reference a la digue dans les troncons
                final List<TronconDigue> troncons = session.getTronconDigueByDigue(digue);
                for(TronconDigue td : troncons){
                    td.setDigueId(null);
                    FXDiguesPane.this.session.getTronconDigueRepository().update(td);
                }
                //on supprime la digue
                FXDiguesPane.this.session.getDigueRepository().remove(digue);
            }
        }else if(obj instanceof TronconDigue){
            final TronconDigue td = (TronconDigue) obj;
            final Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                    "Confirmer la suppression du tronçon "+td.getLibelle()+" ?",
                    ButtonType.YES, ButtonType.NO
            );
            final ButtonType res = alert.showAndWait().get();
            if (res == ButtonType.YES) {
                FXDiguesPane.this.session.getTronconDigueRepository().remove(td);
            }
        }
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
                updateTree();
            }
        });
        final Point2D sc = uiSearch.localToScreen(0, 0);
        popup.show(uiSearch, sc.getX(), sc.getY());
    }

    private void updateTree() {
        
        new Thread(){
            @Override
            public void run() {
                Platform.runLater(() -> {
                    uiSearch.setGraphic(searchRunning);
                });
                
                //on stoque les noeuds ouverts
                final Set extendeds = new HashSet();
                searchExtended(uiTree.getRoot(),extendeds);
                
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
                final Set<TronconDigue> troncons = new HashSet<>(session.getTronconDigueRepository().getAllLight());
                final Set<Digue> diguesFound = new HashSet<>();
                final Set<TronconDigue> tronconsFound = new HashSet<>();
                
                for(SystemeEndiguement sd : sds){
                    final TreeItem sdItem = new TreeItem(sd);
                    treeRootItem.getChildren().add(sdItem);
                    sdItem.setExpanded(extendeds.contains(sd));

                    final List<String> digueIds = sd.getDigue();
                    for(Digue digue : digues){
                        if(!digueIds.contains(digue.getDocumentId())) continue;
                        diguesFound.add(digue);
                        final TreeItem digueItem = toNode(digue, troncons, tronconsFound, filter);
                        digueItem.setExpanded(extendeds.contains(digue));
                        sdItem.getChildren().add(digueItem);
                    }
                }
                
                //on place toute les digues et troncons non trouvé dans un group a part
                digues.removeAll(diguesFound);
                final TreeItem ncItem = new TreeItem("Non classés"); 
                ncItem.setExpanded(extendeds.contains(ncItem.getValue()));
                treeRootItem.getChildren().add(ncItem);
                
                for(Digue digue : digues){
                    final TreeItem digueItem = toNode(digue, troncons, tronconsFound, filter);
                    ncItem.getChildren().add(digueItem);
                    digueItem.setExpanded(extendeds.contains(digue));
                }
                troncons.removeAll(tronconsFound);
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

    private static void searchExtended(TreeItem<?> ti, Set objects){
        if(ti==null) return;
        if(ti.isExpanded()){
            objects.add(ti.getValue());
        }
        for(TreeItem t : ti.getChildren()){
            searchExtended(t, objects);
        }
    }
    
    private static TreeItem toNode(Digue digue, Set<TronconDigue> troncons, Set<TronconDigue> tronconsFound, Predicate<Element> filter){
        final TreeItem digueItem = new TreeItem(digue);
        for(TronconDigue td : troncons){
            if(td.getDigueId()==null || !td.getDigueId().equals(digue.getDocumentId())) continue;
            tronconsFound.add(td);
            if(filter==null || filter.test(td)){
                final TreeItem tronconItem = new TreeItem(td);
                digueItem.getChildren().add(tronconItem);
            }
        }
        return digueItem;
    }

    @Override
    public void documentCreated(Element candidate) {
        if(candidate instanceof SystemeEndiguement ||
           candidate instanceof Digue ||
           candidate instanceof TronconDigue){
            updateTree();
        }
    }

    @Override
    public void documentChanged(Element candidate) {
        if(candidate instanceof SystemeEndiguement ||
           candidate instanceof Digue ||
           candidate instanceof TronconDigue){
            updateTree();
        }
    }

    @Override
    public void documentDeleted(Element candidate) {
        if(candidate instanceof SystemeEndiguement ||
           candidate instanceof Digue ||
           candidate instanceof TronconDigue){
            updateTree();
        }
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

                try {
                    //on crée un géométrie au centre de la france
                    final Geometry geom = JTS.transform(TRONCON_GEOM_WGS84, 
                            CRS.findMathTransform(CommonCRS.WGS84.normalizedGeographic(),SirsCore.getEpsgCode(),true));
                    troncon.setGeometry(geom);
                } catch (FactoryException | TransformException | MismatchedDimensionException ex) {
                    SIRS.LOGGER.log(Level.WARNING, ex.getMessage(),ex);
                    troncon.setGeometry((Geometry) TRONCON_GEOM_WGS84.clone());
                }

                FXDiguesPane.this.session.getTronconDigueRepository().add(troncon);
                //mise en place du SR élémentaire
                TronconUtils.updateSRElementaire(troncon,session);
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
                    final SystemeEndiguement se = (SystemeEndiguement) parent.getValue();
                    se.getDigue().add(digue.getDocumentId());
                    FXDiguesPane.this.session.getSystemeEndiguementRepository().update(se);
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
                if(session.nonGeometryEditionProperty().get()){
                    addTronconMenu.getItems().add(new NewDigueMenuItem(getTreeItem()));
                    setContextMenu(addTronconMenu);
                }
            } else if (obj instanceof Digue) {
                this.setText(((Digue) obj).getLibelle() + " (" + getTreeItem().getChildren().size() + ") ");
                addTronconMenu.getItems().clear();
                if(session.nonGeometryEditionProperty().get()){
                    addTronconMenu.getItems().add(new NewTronconMenuItem(getTreeItem()));
                    setContextMenu(addTronconMenu);
                }
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

    }
    
}
