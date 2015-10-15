
package fr.sirs.theme.ui;

import fr.sirs.Session;
import fr.sirs.SIRS;
import fr.sirs.Injector;
import fr.sirs.core.component.*;
import fr.sirs.core.model.*;
import fr.sirs.ui.Growl;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.apache.sis.storage.DataStoreException;
import org.geotoolkit.font.FontAwesomeIcons;
import org.geotoolkit.font.IconBuilder;
import org.geotoolkit.gui.javafx.util.FXUtilities;
import org.geotoolkit.map.CoverageMapLayer;
import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.map.MapItem;
import org.geotoolkit.map.MapLayer;
import org.geotoolkit.security.BasicAuthenticationSecurity;
import org.geotoolkit.security.ClientSecurity;
import org.geotoolkit.storage.coverage.CoverageReference;
import org.geotoolkit.storage.coverage.CoverageStore;
import org.geotoolkit.wms.WebMapClient;
import org.geotoolkit.wms.xml.WMSVersion;
import org.geotoolkit.wmts.WebMapTileClient;
import org.geotoolkit.wmts.xml.WMTSVersion;
import org.opengis.util.GenericName;

/**
 *
 * @author Olivier Nouguier (Geomatys)
 * @author Alexis Manin (Geomatys)
 */
public class FXBookMarkPane extends AbstractFXElementPane<BookMark> {
    private static final Image ICON_SHOWONMAP = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_GLOBE, 16, FontAwesomeIcons.DEFAULT_COLOR),null);

    private static final String WMS_110 = "WMS - 1.1.1";
    private static final String WMS_130 = "WMS - 1.3.0";
    private static final String WMTS_100 = "WMTS - 1.0.0";

    protected final Previews previewRepository;
    protected LabelMapper labelMapper;
    
    // Propriétés de BookMark
    @FXML private PasswordField ui_motDePasse;
    @FXML private TextField ui_identifiant;
    @FXML private TableView<MapLayer> ui_table;
    @FXML private TextField ui_parametres;
    @FXML private TextField ui_description;
    @FXML private TextField ui_titre;
    @FXML private ChoiceBox<String> ui_service;

    public FXBookMarkPane(final BookMark bookMark){
        this();
        this.elementProperty().set(bookMark);
    }

    /**
     * Constructor. Initialize part of the UI which will not require update when 
     * element edited change.
     */
    protected FXBookMarkPane() {
        SIRS.loadFXML(this, BookMark.class);
        previewRepository = Injector.getBean(Session.class).getPreviews();
        elementProperty().addListener(this::initFields);

        /*
         * Disabling rules.
         */
        ui_description.disableProperty().bind(disableFieldsProperty());
        ui_titre.disableProperty().bind(disableFieldsProperty());
        ui_parametres.disableProperty().bind(disableFieldsProperty());
        ui_identifiant.disableProperty().bind(disableFieldsProperty());
        ui_motDePasse.disableProperty().bind(disableFieldsProperty());
        ui_service.disableProperty().bind(disableFieldsProperty());
        ui_service.setItems(FXCollections.observableArrayList(WMS_110,WMS_130,WMTS_100));

        ui_table.getColumns().add(new NameColumn());
        ui_table.getColumns().add(new ViewColumn());
        ui_table.setPlaceholder(new Label(""));
        FXUtilities.hideTableHeader(ui_table);

    }

    public BookMark getElement() {
        return elementProperty.get();
    }
    
    /**
     * Initialize fields at element setting.
     */
    protected void initFields(ObservableValue<? extends BookMark > observableElement, BookMark oldElement, BookMark newElement) {
        // Unbind fields bound to previous element.
        if (oldElement != null) {
        // Propriétés de BookMark
            ui_description.textProperty().unbindBidirectional(oldElement.descriptionProperty());
            ui_titre.textProperty().unbindBidirectional(oldElement.titreProperty());
            ui_parametres.textProperty().unbindBidirectional(oldElement.parametresProperty());
            ui_identifiant.textProperty().unbindBidirectional(oldElement.identifiantProperty());
            ui_motDePasse.textProperty().unbindBidirectional(oldElement.motDePasseProperty());
        }

        final Session session = Injector.getBean(Session.class);        

        /*
         * Bind control properties to Element ones.
         */
        // Propriétés de BookMark
        // * description
        ui_description.textProperty().bindBidirectional(newElement.descriptionProperty());
        // * titre
        ui_titre.textProperty().bindBidirectional(newElement.titreProperty());
        // * parametres
        ui_parametres.textProperty().bindBidirectional(newElement.parametresProperty());
        // * identifiant
        ui_identifiant.textProperty().bindBidirectional(newElement.identifiantProperty());
        // * motDePasse
        ui_motDePasse.textProperty().bindBidirectional(newElement.motDePasseProperty());
    }
    @Override
    public void preSave() {
        final Session session = Injector.getBean(Session.class);
        final BookMark element = (BookMark) elementProperty().get();

        Object cbValue;
    }


    @FXML
    void refreshList(ActionEvent event) {

        final String service = ui_service.getValue();
        final URL url;
        try {
            url = new URL(ui_parametres.getText());
        } catch (MalformedURLException ex) {
            final Growl successGrowl = new Growl(Growl.Type.ERROR, "URL mal formée");
            successGrowl.showAndFade();
            return;
        }

        ClientSecurity security = null;
        if(!ui_motDePasse.getText().trim().isEmpty() && !ui_identifiant.getText().trim().isEmpty()){
            security = new BasicAuthenticationSecurity(ui_identifiant.getText(), ui_motDePasse.getText());
        }

        CoverageStore store = null;
        if(WMS_110.equals(service)){
            store = new WebMapClient(url, security, WMSVersion.v111);
        }else if(WMS_130.equals(service)){
            store = new WebMapClient(url, security, WMSVersion.v130);
        }else if(WMTS_100.equals(service)){
            store = new WebMapTileClient(url, security, WMTSVersion.v100);
        }

        final List<MapLayer> layers = new ArrayList<>();
        if(store!=null){
            try{
                for(GenericName n : store.getNames()){
                    final CoverageReference cref = store.getCoverageReference(n);
                    final CoverageMapLayer layer = MapBuilder.createCoverageLayer(cref);
                    layer.setName(n.tip().toString());
                    layers.add(layer);
                }
            }catch(DataStoreException ex){
                final Growl successGrowl = new Growl(Growl.Type.ERROR, "Echec de connection\n"+ex.getMessage());
                successGrowl.showAndFade();
            }
        }

        ui_table.setItems(FXCollections.observableArrayList(layers));
    }

    private static class NameColumn extends TableColumn<MapLayer, String>{

        public NameColumn() {
            setCellValueFactory((CellDataFeatures<MapLayer, String> param) -> new SimpleStringProperty(param.getValue().getName()));
            setEditable(false);
            setResizable(true);
            setMaxWidth(Double.MAX_VALUE);
        }

    }
    
    private class ViewColumn extends TableColumn<MapLayer, MapLayer>{

        public ViewColumn() {
            setSortable(false);
            setResizable(false);
            setPrefWidth(44);
            setMinWidth(44);
            setMaxWidth(44);
            setCellValueFactory((CellDataFeatures<MapLayer, MapLayer> param) -> new SimpleObjectProperty(param.getValue()));
            setCellFactory((TableColumn<MapLayer, MapLayer> param) -> new ViewCell());
            setEditable(true);
            setMaxWidth(Double.MAX_VALUE);
        }

    }

    private class ViewCell extends TableCell<MapLayer, MapLayer>{

        private final Button button = new Button(null, new ImageView(ICON_SHOWONMAP));

        public ViewCell() {
            setGraphic(button);
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            button.visibleProperty().bind(itemProperty().isNotNull());
            button.setOnAction(this::showOnMap);
        }

        private void showOnMap(ActionEvent event){
            final MapLayer layer = getItem();
            if(layer==null) return;
            final Session session = Injector.getSession();
            final BookMark bookmark = FXBookMarkPane.this.getElement();
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
            
            parent.items().add(layer);
            session.getFrame().getMapTab().show();
        }

    }



}
