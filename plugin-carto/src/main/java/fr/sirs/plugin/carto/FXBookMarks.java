
package fr.sirs.plugin.carto;

import fr.sirs.FXEditMode;
import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.BookMark;
import fr.sirs.core.model.Role;
import fr.sirs.theme.ui.AbstractFXElementPane;
import fr.sirs.theme.ui.FXBookMarkPane;
import java.util.List;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import org.geotoolkit.font.FontAwesomeIcons;
import org.geotoolkit.font.IconBuilder;
import org.geotoolkit.gui.javafx.util.FXUtilities;
import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.map.MapItem;
import org.geotoolkit.map.MapLayer;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class FXBookMarks extends GridPane {

    private static final Image ICON_SHOWONMAP = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_GLOBE, 16, FontAwesomeIcons.DEFAULT_COLOR),null);

    @FXML private TableView<BookMark> uiTable;
    @FXML private BorderPane uiConfig;
    @FXML private Button uiDelete;
    @FXML private Button uiAdd;
    private final AbstractSIRSRepository<BookMark> repo = Injector.getSession().getRepositoryForClass(BookMark.class);

    public FXBookMarks(){
        SIRS.loadFXML(this, FXBookMarks.class);

        uiDelete.setGraphic(new ImageView(SIRS.ICON_TRASH_WHITE));
        uiAdd.setGraphic(new ImageView(SIRS.ICON_ADD_WHITE));
        uiDelete.disableProperty().bind(uiTable.getSelectionModel().selectedItemProperty().isNull());
        uiTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        uiTable.getSelectionModel().selectedItemProperty().addListener(this::selectionChanged);
        uiTable.setPlaceholder(new Label(""));

        final TableColumn<BookMark,String> col = new TableColumn<>();
        col.setEditable(false);
        col.setCellValueFactory((TableColumn.CellDataFeatures<BookMark, String> param) -> param.getValue().titreProperty());

        uiTable.getColumns().add(col);
        uiTable.getColumns().add(new ViewColumn());
        FXUtilities.hideTableHeader(uiTable);
        refreshList();

        final Session session = Injector.getSession();
        final Role role = session.getRole();
        if(!Role.ADMIN.equals(role)){
            uiDelete.setVisible(false);
            uiAdd.setVisible(false);
        }
    }

    @FXML
    void deleteBookmark(ActionEvent event) {
        final BookMark bookmark = uiTable.getSelectionModel().getSelectedItem();
        if(bookmark==null) return;

        final Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Confirmer la suppression du favori "+bookmark.getDesignation()+" ?",
                ButtonType.YES, ButtonType.NO);
        alert.setResizable(true);
        final ButtonType res = alert.showAndWait().get();
        if (res == ButtonType.YES) {
            repo.remove(bookmark);
            uiConfig.setCenter(null);
            refreshList();
        }
        
    }

    @FXML
    void addBookMark(ActionEvent event) {
        final BookMark bookmark = repo.create();
        bookmark.setDescription("");
        bookmark.setDesignation("");
        bookmark.setTitre("pas de nom");
        repo.add(bookmark);
        refreshList();
    }

    private void selectionChanged(ObservableValue<? extends BookMark> observable, BookMark oldValue, BookMark newValue){
        uiConfig.setCenter(null);
        if(newValue!=null){
            final AbstractFXElementPane editPane = SIRS.generateEditionPane(newValue);

            final Session session = Injector.getSession();
            final Role role = session.getRole();
            if(!Role.ADMIN.equals(role)){
                final FXEditMode searchEditMode = searchEditMode(editPane);
                if(searchEditMode!=null){
                    searchEditMode.setVisible(false);
                    searchEditMode.setManaged(false);
                }
            }

            uiConfig.setCenter(editPane);
        }
    }

    private void refreshList(){
        final List<BookMark> all = repo.getAll();
        uiTable.setItems(FXCollections.observableArrayList(all));
    }

    private static FXEditMode searchEditMode(Node node){
        if(node instanceof FXEditMode) return (FXEditMode) node;

        if(node instanceof Parent){
            for(Node child : ((Parent)node).getChildrenUnmodifiable()){
                final FXEditMode cdt = searchEditMode(child);
                if(cdt!=null) return cdt;
            }
        }

        return null;
    }


    private class ViewColumn extends TableColumn<BookMark, BookMark>{

        public ViewColumn() {
            setSortable(false);
            setResizable(false);
            setPrefWidth(44);
            setMinWidth(44);
            setMaxWidth(44);
            setCellValueFactory((TableColumn.CellDataFeatures<BookMark, BookMark> param) -> new SimpleObjectProperty(param.getValue()));
            setCellFactory((TableColumn<BookMark, BookMark> param) -> new ViewCell());
            setEditable(true);
        }

    }

    private class ViewCell extends TableCell<BookMark, BookMark>{

        private final Button button = new Button(null, new ImageView(ICON_SHOWONMAP));

        public ViewCell() {
            setGraphic(button);
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            button.visibleProperty().bind(itemProperty().isNotNull());
            button.setOnAction(this::showOnMap);
        }

        private void showOnMap(ActionEvent event){
            BookMark bookmark = getItem();

            if(bookmark==null) return;

            List<MapLayer> layers = FXBookMarkPane.listLayers(bookmark);

            final Session session = Injector.getSession();
            final String titre = bookmark.getTitre();

            MapItem parent = null;
            for(MapItem mi : session.getMapContext().items()){
                if(titre.equals(mi.getName())){
                    parent = mi;
                    break;
                }
            }
            if(parent == null){
                parent = MapBuilder.createItem();
                parent.setName(titre);
                session.getMapContext().items().add(parent);
            }

            parent.items().addAll(layers);
            session.getFrame().getMapTab().show();
        }

    }


}
