package fr.sirs.other;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import static fr.sirs.SIRS.BUNDLE_KEY_CLASS;
import static fr.sirs.SIRS.ICON_CHECK_CIRCLE;
import static fr.sirs.SIRS.ICON_EXCLAMATION_CIRCLE;
import static fr.sirs.SIRS.PREVIEW_BUNDLE_KEY_AUTHOR;
import static fr.sirs.SIRS.PREVIEW_BUNDLE_KEY_DESIGNATION;
import static fr.sirs.SIRS.PREVIEW_BUNDLE_KEY_DOC_CLASS;
import static fr.sirs.SIRS.PREVIEW_BUNDLE_KEY_ELEMENT_CLASS;
import static fr.sirs.SIRS.PREVIEW_BUNDLE_KEY_LIBELLE;
import fr.sirs.Session;
import static fr.sirs.core.SirsCore.INFO_DOCUMENT_ID;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.component.Previews;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.Preview;
import fr.sirs.core.model.Utilisateur;
import fr.sirs.util.FXPreviewToElementTableColumn;
import fr.sirs.util.ReferenceTableCell;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;
import org.geotoolkit.gui.javafx.util.ButtonTableCell;
import org.geotoolkit.internal.GeotkFX;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class FXValidationPane extends BorderPane {

    private TableView<Preview> table;
    private final Session session = Injector.getSession();
    private final Previews repository = session.getPreviews();
    private final ChoiceBox<Choice> choiceBox = new ChoiceBox<>(FXCollections.observableArrayList(Choice.values()));
    
    private List<Preview> previews;
    private final Map<String, ResourceBundle> bundles = new HashMap<>();
    
    private enum Choice{VALID, INVALID, ALL};

    public FXValidationPane() {
        final ResourceBundle previewBundle = ResourceBundle.getBundle(Preview.class.getName(), Locale.getDefault(), Thread.currentThread().getContextClassLoader());

        table = new TableView<>();
        table.setEditable(false);

        table.getColumns().add(new DeleteColumn());
        table.getColumns().add(new FXPreviewToElementTableColumn());

        final TableColumn<Preview, Map.Entry<String, String>> docClassColumn = new TableColumn<>(previewBundle.getString(PREVIEW_BUNDLE_KEY_DOC_CLASS));
        docClassColumn.setCellValueFactory((TableColumn.CellDataFeatures<Preview, Map.Entry<String, String>> param) -> {
                    return new SimpleObjectProperty<>(new HashMap.SimpleImmutableEntry<String, String>(param.getValue().getDocId(), param.getValue().getDocClass()));
                });
        docClassColumn.setCellFactory((TableColumn<Preview, Map.Entry<String, String>> param) -> {
                return new TableCell<Preview, Map.Entry<String, String>>() {
                    @Override
                    protected void updateItem(Map.Entry<String, String> item, boolean empty) {
                        super.updateItem(item, empty);
                        if(empty || item==null || INFO_DOCUMENT_ID.equals(item.getKey())){
                            setText(null);
                            setGraphic(null);
                        }
                        else{
                            setText(getBundleForClass(item.getValue()).getString(BUNDLE_KEY_CLASS));
                            setTooltip(new Tooltip(item.getKey()));
                        }
                    }
                };
            });
        docClassColumn.setComparator((Map.Entry<String, String> o1, Map.Entry<String, String> o2) -> {
                if(o1==null && o2==null) return 0;
                if(o1==null && o2!=null) return -1;
                if(o1!=null && o2==null) return 1;
                else{
                    final ResourceBundle rb1 = getBundleForClass(o1.getValue());
                    final ResourceBundle rb2 = getBundleForClass(o2.getValue());
                    return rb1.getString(BUNDLE_KEY_CLASS).compareTo(rb2.getString(BUNDLE_KEY_CLASS));
                }
            });
        table.getColumns().add(docClassColumn);
        
        final TableColumn<Preview, String> elementClassColumn = new TableColumn<>(previewBundle.getString(PREVIEW_BUNDLE_KEY_ELEMENT_CLASS));
        elementClassColumn.setCellValueFactory((TableColumn.CellDataFeatures<Preview, String> param) -> {
                    return new SimpleStringProperty(param.getValue().getElementClass());
                });
        elementClassColumn.setCellFactory((TableColumn<Preview, String> param) -> {
                return new TableCell<Preview, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if(empty || item==null){
                            setText(null);
                            setGraphic(null);
                        }
                        else{
                            final ResourceBundle rb = getBundleForClass(item);
                            setText((rb == null) ? null : rb.getString(BUNDLE_KEY_CLASS));
                        }
                    }
                };
            });
        elementClassColumn.setComparator((String o1, String o2) -> {
                final String elementClass1 = o1;
                final String elementClass2 = o2;
                if(elementClass1==null && elementClass2==null) return 0;
                else if(elementClass1==null && elementClass2!=null) return -1;
                else if(elementClass1!=null && elementClass2==null) return 1;
                else{
                    final ResourceBundle rb1 = getBundleForClass(elementClass1);
                    final ResourceBundle rb2 = getBundleForClass(elementClass2);
                    return rb1.getString(BUNDLE_KEY_CLASS).compareTo(rb2.getString(BUNDLE_KEY_CLASS));
                }
            });
        table.getColumns().add(elementClassColumn);

        final TableColumn<Preview, String> propertyColumn = new TableColumn<>(previewBundle.getString(PREVIEW_BUNDLE_KEY_DESIGNATION));
        propertyColumn.setCellValueFactory((TableColumn.CellDataFeatures<Preview, String> param) -> {
                return new SimpleObjectProperty<>(param.getValue().getDesignation());
            });
        table.getColumns().add(propertyColumn);
        
        final TableColumn<Preview, String> labelColumn = new TableColumn<>(previewBundle.getString(PREVIEW_BUNDLE_KEY_LIBELLE));
        labelColumn.setCellValueFactory((TableColumn.CellDataFeatures<Preview, String> param) -> {
                return new SimpleObjectProperty(param.getValue().getLibelle());
            });
        table.getColumns().add(labelColumn);

        final TableColumn<Preview, String> authorColumn = new TableColumn<>(previewBundle.getString(PREVIEW_BUNDLE_KEY_AUTHOR));
        authorColumn.setCellValueFactory((TableColumn.CellDataFeatures<Preview, String> param) -> {
                return new SimpleObjectProperty(param.getValue().getAuthor());
            });
        authorColumn.setCellFactory((TableColumn<Preview, String> param) -> new ReferenceTableCell(Utilisateur.class));
        table.getColumns().add(authorColumn);

        table.getColumns().add(new ValidColumn());

        setCenter(table);

        table.setItems(FXCollections.observableArrayList(repository.getValidation()));

        choiceBox.setConverter(new StringConverter<Choice>() {

            @Override
            public String toString(Choice object) {
                final String result;
                switch(object){
                    case VALID: result = "Éléments validés"; break;
                    case INVALID: result = "Éléments invalidés"; break;
                    case ALL:
                    default: result="Tous les états";
                }
                return result;
            }

            @Override
            public Choice fromString(String string) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        });
        choiceBox.setValue(Choice.ALL);
        choiceBox.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends Choice> observable, Choice oldValue, Choice newValue) -> {
                fillTable();
        });
        
        final Button reload = new Button("Recharger", new ImageView(SIRS.ICON_ROTATE_LEFT_ALIAS));
        reload.setOnAction((ActionEvent e) -> {fillTable();});
        
        final HBox hBox = new HBox(choiceBox, reload);
        hBox.setAlignment(Pos.CENTER);
        hBox.setPadding(new Insets(20));
        hBox.setSpacing(100);

        setTop(hBox);
    }
    
    private void fillTable(){
        final List<Preview> requiredList;
        switch(choiceBox.getSelectionModel().getSelectedItem()){
            case VALID: requiredList = repository.getAllByValidationState(true); break;
            case INVALID: requiredList = repository.getAllByValidationState(false); break;
            case ALL:
            default: requiredList= repository.getValidation();
        }
        table.setItems(FXCollections.observableArrayList(requiredList));
    }

    private ResourceBundle getBundleForClass(final String type) {
        if (type == null) {
            return null;
        }
        if (bundles.get(type) == null) {
            bundles.put(type, ResourceBundle.getBundle(type, Locale.getDefault(), Thread.currentThread().getContextClassLoader()));
        }
        return bundles.get(type);
    }
    

    private class DeleteColumn extends TableColumn<Preview, Preview>{
        
        public DeleteColumn() {
            super("Suppression");            
            setSortable(false);
            setResizable(false);
            setPrefWidth(24);
            setMinWidth(24);
            setMaxWidth(24);
            setGraphic(new ImageView(GeotkFX.ICON_DELETE));
            
            setCellValueFactory((TableColumn.CellDataFeatures<Preview, Preview> param) -> {
                    return new SimpleObjectProperty<>(param.getValue());
                });
            

            setCellFactory((TableColumn<Preview, Preview> param) -> {
                    return new ButtonTableCell<>(
                        false, new ImageView(GeotkFX.ICON_DELETE) , (Preview t) -> true, (Preview vSummary) -> {
                            if (vSummary != null) {
                                final Session session = Injector.getSession();
                                final AbstractSIRSRepository repo = session.getRepositoryForType(vSummary.getDocClass());
                                final Element docu = (Element) repo.get(vSummary.getDocId() == null ? vSummary.getElementId() : vSummary.getDocId());

                                // Si l'elementid est null, c'est que l'élément est le document lui-même
                                if (vSummary.getElementId() == null) {
                                    if(!docu.getValid()){
                                        final Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer l'élément ?", ButtonType.NO, ButtonType.YES);
                                        confirm.setResizable(true);
                                        final Optional<ButtonType> res = confirm.showAndWait();
                                        if (res.isPresent() && ButtonType.YES.equals(res.get())) {
                                            repo.remove(docu);
                                            table.getItems().remove(vSummary);
                                        }
                                    }else{
                                        final Alert alert = new Alert(Alert.AlertType.INFORMATION, "Vous ne pouvez supprimer que les éléments invalides.", ButtonType.OK);
                                        alert.setResizable(true);
                                        alert.showAndWait();
                                    }
                                } // Sinon, c'est que l'élément est inclus quelque part dans le document et il faut le rechercher.
                                else {
                                    final Element elt = docu.getChildById(vSummary.getElementId());
                                    if(!elt.getValid()){
                                        final Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer l'élément ?", ButtonType.NO, ButtonType.YES);
                                        confirm.setResizable(true);
                                        final Optional<ButtonType> res = confirm.showAndWait();
                                        if (res.isPresent() && ButtonType.YES.equals(res.get())) {
                                            elt.getParent().removeChild(elt);
                                            repo.update(docu);
                                            table.getItems().remove(vSummary);
                                        }
                                    } else{
                                        final Alert alert = new Alert(Alert.AlertType.INFORMATION, "Vous ne pouvez supprimer que les éléments invalides.", ButtonType.OK);
                                        alert.setResizable(true);
                                        alert.showAndWait();
                                    }
                                }
                            }
                            return vSummary;
                        });
                });
        }  
    }

    private class ValidButtonTableCell extends TableCell<Preview, Preview> {

        protected final Button button = new Button();

        public ValidButtonTableCell() {
            super();
            setGraphic(button);
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            setAlignment(Pos.CENTER);
            button.setOnAction((ActionEvent event) -> {
                    final Preview vSummary = getItem();
                    if (vSummary != null) {
                        final Session session = Injector.getSession();
                        final AbstractSIRSRepository repo = session.getRepositoryForType(vSummary.getDocClass());
                        final Element document = (Element) repo.get(vSummary.getDocId());

                        // Si l'elementid est null, c'est que l'élément est le document lui-même
                        if (vSummary.getElementId() == null) {
                            document.setValid(!document.getValid());
                        } 
                        // Sinon, c'est que l'élément est inclus quelque part dans le document et il faut le rechercher.
                        else {
                            final Element elt = document.getChildById(vSummary.getElementId());
                            elt.setValid(!elt.getValid());
                        }
                        repo.update(document);
                        
                        vSummary.setValid(!vSummary.getValid());
                        updateButton(vSummary.getValid());
                    }
                });
        }

        private void updateButton(final boolean valid) {
            if (!valid) {
                button.setGraphic(new ImageView(ICON_EXCLAMATION_CIRCLE));
                button.setText("Invalidé");
            } else {
                button.setGraphic(new ImageView(ICON_CHECK_CIRCLE));
                button.setText("Validé");
            }
        }

        @Override
        protected void updateItem(Preview item, boolean empty) {
            super.updateItem(item, empty);

            if(empty || item==null){
                setText(null);
                setGraphic(null);
            }
            else{
                setGraphic(button);
                updateButton(item.getValid());
            }
        }
    }

    private class ValidColumn extends TableColumn<Preview, Preview> {

        public ValidColumn() {
            super("État");
            setEditable(false);
            setSortable(false);
            setResizable(true);
            setPrefWidth(120);

            setCellValueFactory((TableColumn.CellDataFeatures<Preview, Preview> param) -> {
                    return new SimpleObjectProperty<>(param.getValue());
                });

            setCellFactory((TableColumn<Preview, Preview> param) -> {
                    return new ValidButtonTableCell();
                });
        }
    }
}
