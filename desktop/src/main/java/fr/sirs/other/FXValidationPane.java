package fr.sirs.other;

import fr.sirs.Injector;
import static fr.sirs.SIRS.BUNDLE_KEY_CLASS;
import static fr.sirs.SIRS.ICON_CHECK_CIRCLE;
import static fr.sirs.SIRS.ICON_EXCLAMATION_CIRCLE;
import fr.sirs.Session;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.component.Previews;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.Preview;
import fr.sirs.util.FXValiditySummaryToElementTableColumn;
import fr.sirs.util.SirsTableCell;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
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

    private TableView<Preview> usages;
    private final Session session = Injector.getSession();
    private final Map<String, ResourceBundle> bundles = new HashMap<>();
    private final Previews validitySummaryRepository = session.getPreviews();
    
    private enum ValiditySummaryChoice{VALID, INVALID, ALL};

    public FXValidationPane() {
        final ResourceBundle bundle = ResourceBundle.getBundle(Preview.class.getName());

        usages = new TableView<>();
        usages.setEditable(false);

        usages.getColumns().add(new DeleteColumn());
        usages.getColumns().add(new FXValiditySummaryToElementTableColumn());

        final TableColumn<Preview, Map.Entry<String, String>> docClassColumn = new TableColumn<>(bundle.getString("docClass"));
        docClassColumn.setCellValueFactory((TableColumn.CellDataFeatures<Preview, Map.Entry<String, String>> param) -> {
                    return new SimpleObjectProperty<>(new HashMap.SimpleImmutableEntry<String, String>(param.getValue().getDocId(), param.getValue().getDocClass()));
                });
        docClassColumn.setCellFactory((TableColumn<Preview, Map.Entry<String, String>> param) -> {
                return new TableCell<Preview, Map.Entry<String, String>>() {
                    @Override
                    protected void updateItem(Map.Entry<String, String> item, boolean empty) {
                        super.updateItem(item, empty);
                        if(empty || item==null){
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
        usages.getColumns().add(docClassColumn);
        
        final TableColumn<Preview, String> elementClassColumn = new TableColumn<>(bundle.getString("elementClass"));
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
        usages.getColumns().add(elementClassColumn);

        final TableColumn<Preview, String> propertyColumn = new TableColumn<>(bundle.getString("pseudoId"));
        propertyColumn.setCellValueFactory((TableColumn.CellDataFeatures<Preview, String> param) -> {
                return new SimpleObjectProperty<>(param.getValue().getDesignation());
            });
        usages.getColumns().add(propertyColumn);
        
        final TableColumn<Preview, String> labelColumn = new TableColumn<>(bundle.getString("label"));
        labelColumn.setCellValueFactory((TableColumn.CellDataFeatures<Preview, String> param) -> {
                return new SimpleObjectProperty(param.getValue().getLibelle());
            });
        usages.getColumns().add(labelColumn);

        final TableColumn<Preview, String> authorColumn = new TableColumn<>(bundle.getString("author"));
        authorColumn.setCellValueFactory((TableColumn.CellDataFeatures<Preview, String> param) -> {
                return new SimpleObjectProperty(param.getValue().getAuthor());
            });
        authorColumn.setCellFactory((TableColumn<Preview, String> param) -> new SirsTableCell());
        usages.getColumns().add(authorColumn);

        usages.getColumns().add(new ValidColumn());

        setCenter(usages);

        usages.setItems(FXCollections.observableArrayList(validitySummaryRepository.getValidation()));

        final ChoiceBox<ValiditySummaryChoice> choiceBox = new ChoiceBox<>(FXCollections.observableArrayList(ValiditySummaryChoice.values()));
        choiceBox.setConverter(new StringConverter<ValiditySummaryChoice>() {

            @Override
            public String toString(ValiditySummaryChoice object) {
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
            public ValiditySummaryChoice fromString(String string) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        });
        choiceBox.setValue(ValiditySummaryChoice.ALL);
        choiceBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ValiditySummaryChoice>() {

            @Override
            public void changed(ObservableValue<? extends ValiditySummaryChoice> observable, ValiditySummaryChoice oldValue, ValiditySummaryChoice newValue) {
                final List<Preview> requiredList;
                switch(choiceBox.getSelectionModel().getSelectedItem()){
                    case VALID: requiredList = validitySummaryRepository.getAllByValidationState(true); break;
                    case INVALID: requiredList = validitySummaryRepository.getAllByValidationState(false); break;
                    case ALL:
                    default: requiredList= validitySummaryRepository.getValidation();
                }
                usages.setItems(FXCollections.observableArrayList(requiredList));
            }
        });
        
        final HBox hBox = new HBox(choiceBox);
        hBox.setAlignment(Pos.CENTER);
        hBox.setPadding(new Insets(20));
        hBox.setSpacing(100);

        setTop(hBox);
    }

    private ResourceBundle getBundleForClass(final String type) {
        if (type == null) {
            return null;
        }
        if (bundles.get(type) == null) {
            bundles.put(type, ResourceBundle.getBundle(type));
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
                                final Element docu = (Element) repo.get(vSummary.getDocId());

                                // Si l'elementid est null, c'est que l'élément est le document lui-même
                                if (vSummary.getElementId() == null) {
                                    if(!docu.getValid()){
                                        final Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer l'élément ?", ButtonType.NO, ButtonType.YES);
                                        final Optional<ButtonType> res = confirm.showAndWait();
                                        if (res.isPresent() && ButtonType.YES.equals(res.get())) {
                                            repo.remove(docu);
                                            usages.getItems().remove(vSummary);
                                        }
                                    }else{
                                        new Alert(Alert.AlertType.INFORMATION, "Vous ne pouvez supprimer que les éléments invalides.", ButtonType.OK).showAndWait();
                                    }
                                } // Sinon, c'est que l'élément est inclus quelque part dans le document et il faut le rechercher.
                                else {
                                    final Element elt = docu.getChildById(vSummary.getElementId());
                                    if(!elt.getValid()){
                                        final Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer l'élément ?", ButtonType.NO, ButtonType.YES);
                                        final Optional<ButtonType> res = confirm.showAndWait();
                                        if (res.isPresent() && ButtonType.YES.equals(res.get())) {
                                            elt.getParent().removeChild(elt);
                                            repo.update(docu);
                                            usages.getItems().remove(vSummary);
                                        }
                                    } else{
                                        new Alert(Alert.AlertType.INFORMATION, "Vous ne pouvez supprimer que les éléments invalides.", ButtonType.OK).showAndWait();
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
