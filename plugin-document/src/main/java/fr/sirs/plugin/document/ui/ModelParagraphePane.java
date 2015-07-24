package fr.sirs.plugin.document.ui;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.model.RapportModeleDocument;
import fr.sirs.core.model.RapportSectionDocument;
import fr.sirs.core.component.SQLQueryRepository;
import fr.sirs.core.model.SQLQuery;
import fr.sirs.util.SirsStringConverter;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import org.geotoolkit.font.FontAwesomeIcons;
import org.geotoolkit.font.IconBuilder;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.util.Callback;


/**
 * @author Cédric Briançon (Geomatys)
 */
public class ModelParagraphePane extends BorderPane {
    public static final Image ICON_TRASH = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_TRASH_O, 16, Color.BLACK), null);

    @FXML private Label uiSectionNameLbl;

    @FXML private Button uiDeleteSectionBtn;

    @FXML private TextField uiSectionTitleTxtField;
    
    @FXML private ComboBox<String> uiTypeComboBox;

    @FXML private GridPane gridPane;
    
    private final Pane parent;
    private final RapportSectionDocument section;
    private final RapportModeleDocument model;

    public static final String SELECT_DOC     = "Selectionner un document";
    public static final String GENERATE_TAB   = "Générer un tableau";
    public static final String GENERATE_SHEET = "Générer des fiches";
    
    public ModelParagraphePane(final Pane parent, final RapportModeleDocument model, final RapportSectionDocument section, final int index) {
        SIRS.loadFXML(this);
        Injector.injectDependencies(this);

        this.parent = parent;
        this.model = model;
        this.section = section;

        uiSectionNameLbl.setText("Paragraphe " + index);
        uiDeleteSectionBtn.setGraphic(new ImageView(ICON_TRASH));
        uiSectionTitleTxtField.textProperty().bindBidirectional(section.libelleProperty());
        
        final ObservableList prop = FXCollections.observableArrayList();
        prop.add(SELECT_DOC);
        prop.add(GENERATE_TAB);
        prop.add(GENERATE_SHEET);
        uiTypeComboBox.setItems(prop);
        
        uiTypeComboBox.valueProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            List<Node> toRemove = new ArrayList<>();
            for (Node child : gridPane.getChildren()) {
                if ("selectDoc".equals(child.getId()) ||
                    "queryBox".equals(child.getId())  ||
                    "sheet".equals(child.getId())) {
                    toRemove.add(child);
                }
            }
            gridPane.getChildren().removeAll(toRemove);
            
            final SQLQueryRepository queryRepo = Injector.getBean(SQLQueryRepository.class);
            final ComboBox<SQLQuery> queryBox = new ComboBox<>();
            queryBox.setId("queryBox");
            final ObservableList queries = FXCollections.observableArrayList();
            queries.addAll(queryRepo.getAll());
            queryBox.setItems(queries);
            queryBox.setCellFactory(new Callback() {

                @Override
                public ListCell call(Object param) {
                    return new ListCell(){
                        @Override
                        protected void updateItem(Object item, boolean empty) {
                            super.updateItem(item, empty);
                            if (item == null || empty) {
                                setText("Séléctionner une requête");
                            } else {
                                SQLQuery query = (SQLQuery) item;
                                setText("SQLQ : " + query.getLibelle());
                            }
                        }
                    } ;
                }
            });
            queryBox.setConverter(new SirsStringConverter());
            
            
            switch (newValue) {
                case SELECT_DOC     : 
                    final GridPane selectPane = new GridPane();
                    selectPane.setId("selectDoc");
                    
                    final TextField importField = new TextField();
                    importField.setPrefWidth(350.0);
                    selectPane.addColumn(0, importField);
                    
                    final Button chooseButton = new Button("Sélectionner");
                    chooseButton.setPrefWidth(150.0);
                    chooseButton.setOnAction((ActionEvent event) -> {
                        final FileChooser fileChooser = new FileChooser();
                        final File file = fileChooser.showOpenDialog(null);
                        if (file != null) {
                            importField.setText(file.getPath());
                        }
                    });
                    selectPane.addColumn(1, chooseButton);
                    
                    gridPane.add(selectPane, 1, 2);
                    break;
                    
                case GENERATE_TAB   : 
                    queryBox.setPrefWidth(500);
                    gridPane.add(queryBox, 1, 2);
                    
                    break;
                case GENERATE_SHEET : 
                    final GridPane sheetPane = new GridPane();
                    sheetPane.setId("sheet");
                    queryBox.setPrefWidth(300);
                    sheetPane.addColumn(0, queryBox);
                    
                    final ComboBox<String> photoBox = new ComboBox<>();
                    photoBox.setPrefWidth(200);
                    final ObservableList photos = FXCollections.observableArrayList();
                    photos.add("Photos");
                    photos.add("Pas de photos");
                    photos.add("Dernière photo");
                    photos.add("Toutes les photos");
                    photoBox.setItems(photos);
                    sheetPane.addColumn(1, photoBox);
                    
                    gridPane.add(sheetPane, 1, 2);
                    break;
            }
        });
        
        
        uiTypeComboBox.getSelectionModel().selectFirst();
    }

    @FXML
    private void deleteSection() {
        model.getSections().remove(section);
        parent.getChildren().remove(this);
    }
}
