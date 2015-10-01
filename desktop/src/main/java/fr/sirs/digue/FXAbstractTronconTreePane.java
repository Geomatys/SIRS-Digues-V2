package fr.sirs.digue;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.core.component.DocumentListener;
import fr.sirs.core.model.AvecBornesTemporelles;
import fr.sirs.core.model.Element;
import fr.sirs.theme.ui.AbstractFXElementPane;
import java.time.LocalDate;
import java.util.Set;
import java.util.function.Predicate;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Popup;
import org.apache.sis.referencing.CommonCRS;
import org.geotoolkit.geometry.jts.JTS;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Samuel Andrés
 */
public abstract class FXAbstractTronconTreePane extends SplitPane implements DocumentListener {


    /** Cette géométrie sert de base pour tous les nouveaux troncons */
    public static final Geometry TRONCON_GEOM_WGS84;
    static {
        TRONCON_GEOM_WGS84 = new GeometryFactory().createLineString(new Coordinate[]{
            new Coordinate(0, 48),
            new Coordinate(5, 48)
        });
        JTS.setCRS(TRONCON_GEOM_WGS84, CommonCRS.WGS84.normalizedGeographic());
    }
    
    @FXML protected Label uiTitle;
    @FXML protected TreeView uiTree;
    @FXML protected BorderPane uiRight;
    @FXML protected Button uiSearch;
    @FXML protected Button uiDelete;
    @FXML protected ToggleButton uiArchived;
    @FXML protected MenuButton uiAdd;

    @Autowired protected Session session;
    
    //etat de la recherche
    protected final ImageView searchNone = new ImageView(SIRS.ICON_SEARCH_WHITE);
    protected final ProgressIndicator searchRunning = new ProgressIndicator();
    protected final StringProperty currentSearch = new SimpleStringProperty("");
    
    protected final Predicate<AvecBornesTemporelles> nonArchivedPredicate = (AvecBornesTemporelles t) -> {
        return (t.getDate_fin()==null || t.getDate_fin().isAfter(LocalDate.now()));
    };

    public FXAbstractTronconTreePane(final String title) {
        SIRS.loadFXML(this, FXAbstractTronconTreePane.class, null);
        Injector.injectDependencies(this);
        uiTitle.setText(title);
        uiTree.setShowRoot(false);

        uiTree.getSelectionModel().getSelectedIndices().addListener((ListChangeListener.Change c) -> {
            Object obj = uiTree.getSelectionModel().getSelectedItem();
            if (obj instanceof TreeItem) {
                obj = ((TreeItem) obj).getValue();
            }

            if (obj instanceof Element) {
                displayElement((Element) obj);
            }
        });

        searchRunning.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        searchRunning.setPrefSize(22, 22);
        searchRunning.setStyle(" -fx-progress-color: white;");

        uiArchived.setSelected(false);
        uiArchived.setGraphic(new ImageView(SIRS.ICON_ARCHIVE_WHITE));
        uiArchived.setOnAction(event -> updateTree());
        uiArchived.setTooltip(new Tooltip("Voir les troncons archivés"));
        uiArchived.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(uiArchived!=null && uiArchived.getTooltip()!=null)
                    uiArchived.getTooltip().setText(uiArchived.isSelected() ? "Masquer les troncons archivés" : "Voir les troncons archivés");
            }
        });

        uiSearch.setGraphic(searchNone);
        uiSearch.textProperty().bind(currentSearch);

        uiDelete.setGraphic(new ImageView(SIRS.ICON_TRASH_WHITE));
        uiDelete.setOnAction(this::deleteSelection);
        uiDelete.setDisable(!session.nonGeometryEditionProperty().get());
        uiAdd.setGraphic(new ImageView(SIRS.ICON_ADD_WHITE));
        uiAdd.setDisable(!session.nonGeometryEditionProperty().get());

        //listen to changes in the db to update tree
        Injector.getDocumentChangeEmiter().addListener(this);

    }

    abstract public void deleteSelection(ActionEvent event);
    abstract public void updateTree();


    @FXML
    private void openSearchPopup(ActionEvent event) {
        if (uiSearch.getGraphic() != searchNone) {
            //une recherche est deja en cours
            return;
        }

        final Popup popup = new Popup();
        final TextField textField = new TextField(currentSearch.get());
        popup.setAutoHide(true);
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
    
    /**
     * Affiche un éditeur pour l'élément en entrée.
     * @param obj L'élément à éditer.
     */
    public void displayElement(final Element obj) {
        AbstractFXElementPane ctrl = SIRS.generateEditionPane(obj);
        uiRight.setCenter(ctrl);
        session.getPrintManager().prepareToPrint(obj);
    }

    public static void searchExtended(TreeItem<?> ti, Set objects){
        if(ti==null) return;
        if(ti.isExpanded()){
            objects.add(ti.getValue());
        }
        for(TreeItem t : ti.getChildren()){
            searchExtended(t, objects);
        }
    }
}
