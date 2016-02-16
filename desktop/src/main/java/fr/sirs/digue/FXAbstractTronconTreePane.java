package fr.sirs.digue;

import fr.sirs.Injector;
import fr.sirs.Printable;
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.core.component.DocumentListener;
import fr.sirs.core.model.AvecBornesTemporelles;
import fr.sirs.core.model.Element;
import fr.sirs.theme.ui.AbstractFXElementPane;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Node;
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
import org.apache.sis.util.ArgumentChecks;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public abstract class FXAbstractTronconTreePane extends SplitPane implements DocumentListener, Printable {
    
    @FXML protected Label uiTitle;
    @FXML protected TreeView<? extends Element> uiTree;
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
        setFocusTraversable(true);
        uiTitle.setText(title);
        uiTree.setShowRoot(false);

        uiTree.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null || newValue.getValue() == null) {
                uiRight.setCenter(null);
            } else {
                editElement(newValue.getValue());
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
    abstract public Task updateTree();


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
        if (obj == null) return;
        // If node selected in tree does not fit requested item, we'll update it.
        final TreeItem<? extends Element> selectedItem = uiTree.getSelectionModel().getSelectedItem();
        if (selectedItem == null || !obj.equals(selectedItem.getValue())) {
            final Runnable search = () -> {
                final Optional<TreeItem> found = searchItem(obj, uiTree.getRoot());
                if (found.isPresent()) {
                    uiTree.getSelectionModel().select(found.get());
                }
            };

            if (uiTree.getRoot() == null)
                updateTree().setOnSucceeded(evt -> SIRS.fxRun(false, search));
            else SIRS.fxRun(false, search);
        }
    }

    /**
     * Recursive (depth-first) search in given tree item and its children.
     * @param toSearch The element that must be contained by returned tree item.
     * @param searchRoot A tree item to search into.
     * @return A tree item whose value is queried element, null if we cannot find any.
     */
    protected Optional<TreeItem> searchItem(final Element toSearch, final TreeItem<? extends Element> searchRoot) {
        if (toSearch == null)
            return Optional.empty();

        if (toSearch.equals(searchRoot.getValue())) {
            return Optional.of(searchRoot);
        }

        Optional found;
        for (final TreeItem child : searchRoot.getChildren()) {
            found = searchItem(toSearch, child);
            if (found.isPresent()) {
                return found;
            }
        }

        return Optional.empty();
    }

    /**
     * Request focus on an editor for input parameter.
     * @param obj The element to edit.
     */
    private void editElement(final Element obj) {
        ArgumentChecks.ensureNonNull("Object to display", obj);
        final Node n = uiRight.getCenter();
        AbstractFXElementPane editor = null;
        if (n instanceof AbstractFXElementPane) {
            editor = (AbstractFXElementPane) n;
            final Object element = editor.elementProperty().get();
            if (element != null && element.getClass().equals(obj.getClass())) {
                editor.setElement(obj);
            } else {
                editor = null;
            }
        }

        if (editor == null) {
            editor = SIRS.generateEditionPane(obj);
            uiRight.setCenter(editor);
        }

        editor.requestFocus();
    }

    @Override
    public String getPrintTitle() {
        final Node right = uiRight.getCenter();
        if(right instanceof Printable){
            return ((Printable)right).getPrintTitle();
        }else{
            return null;
        }
    }

    @Override
    public ObjectProperty getPrintableElements() {
        final Node right = uiRight.getCenter();
        if(right instanceof Printable){
            return ((Printable)right).getPrintableElements();
        }else{
            return new SimpleObjectProperty();
        }
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
