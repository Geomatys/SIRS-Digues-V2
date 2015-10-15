
package fr.sirs.plugin.carto;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.BookMark;
import fr.sirs.theme.ui.AbstractFXElementPane;
import fr.sirs.theme.ui.FXBookMarkPane;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import org.geotoolkit.gui.javafx.util.FXUtilities;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class FXBookMarks extends GridPane {

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
        FXUtilities.hideTableHeader(uiTable);
        refreshList();

    }

    @FXML
    void deleteBookmark(ActionEvent event) {
        final BookMark bookmark = uiTable.getSelectionModel().getSelectedItem();
        if(bookmark==null) return;

        repo.remove(bookmark);
        uiConfig.setCenter(null);
        refreshList();
    }

    @FXML
    void addBookMark(ActionEvent event) {
        final BookMark bookmark = repo.create();
        bookmark.setDescription("pas de nom");
        bookmark.setDesignation("pas de nom");
        bookmark.setTitre("pas de nom");
        repo.add(bookmark);
        refreshList();
    }

    private void selectionChanged(ObservableValue<? extends BookMark> observable, BookMark oldValue, BookMark newValue){
        uiConfig.setCenter(null);
        if(newValue!=null){
            final AbstractFXElementPane editPane = SIRS.generateEditionPane(newValue);
            uiConfig.setCenter(editPane);
        }
    }

    private void refreshList(){
        final List<BookMark> all = repo.getAll();
        uiTable.setItems(FXCollections.observableArrayList(all));
    }

}
