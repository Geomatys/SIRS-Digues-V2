package fr.sirs.other;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import static fr.sirs.SIRS.ICON_CHECK_CIRCLE;
import static fr.sirs.SIRS.ICON_EXCLAMATION_CIRCLE;
import fr.sirs.Session;
import fr.sirs.core.Repository;
import fr.sirs.core.component.ValiditySummaryRepository;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.ValiditySummary;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Function;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.geotoolkit.gui.javafx.util.ButtonTableCell;
import org.geotoolkit.internal.GeotkFX;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class FXValidationPane extends BorderPane {

    private TableView<ValiditySummary> usages;
    private final Session session = Injector.getSession();
    private final Map<String, ResourceBundle> bundles = new HashMap<>();
    final ValiditySummaryRepository validitySummaryRepository = session.getValiditySummaryRepository();
    
    public static final Callback<TableColumn.CellDataFeatures<ValiditySummary, ValiditySummary>, ObservableValue<ValiditySummary>> DEFAULT_CELL_VALUE_FACTORY
            = new Callback<TableColumn.CellDataFeatures<ValiditySummary, ValiditySummary>, ObservableValue<ValiditySummary>>() {

                @Override
                public ObservableValue<ValiditySummary> call(TableColumn.CellDataFeatures<ValiditySummary, ValiditySummary> param) {
                    return new SimpleObjectProperty<>(param.getValue());
                }
            };
    
    private enum ValiditySummaryChoice{VALID, INVALID, ALL};

    public FXValidationPane() {
        final ResourceBundle bundle = ResourceBundle.getBundle(ValiditySummary.class.getName());

        final List<ValiditySummary> referenceUsages = validitySummaryRepository.getValidation();

        usages = new TableView<>();
        usages.setEditable(false);

        usages.getColumns().add(new StateColumn());
        
        

        final TableColumn<ValiditySummary, ValiditySummary> docClassColumn = new TableColumn<>(bundle.getString("docClass"));
        docClassColumn.setCellValueFactory(DEFAULT_CELL_VALUE_FACTORY);
        docClassColumn.setCellFactory(new Callback<TableColumn<ValiditySummary, ValiditySummary>, TableCell<ValiditySummary, ValiditySummary>>() {

            @Override
            public TableCell<ValiditySummary, ValiditySummary> call(TableColumn<ValiditySummary, ValiditySummary> param) {
                return new TableCell<ValiditySummary, ValiditySummary>() {
                    @Override
                    protected void updateItem(ValiditySummary item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null) {
                            setText(getBundleForClass(item.getDocClass()).getString("class"));
                            setTooltip(new Tooltip(item.getDocId()));
                        }
                    }
                };
            }
        });
        docClassColumn.setComparator(new Comparator<ValiditySummary>() {

            @Override
            public int compare(ValiditySummary o1, ValiditySummary o2) {
                if(o1==null && o2==null) return 0;
                if(o1==null && o2!=null) return -1;
                if(o1!=null && o2==null) return 1;
                else{
                    final ResourceBundle rb1 = getBundleForClass(o1.getDocClass());
                    final ResourceBundle rb2 = getBundleForClass(o2.getDocClass());
                    return rb1.getString("class").compareTo(rb2.getString("class"));
                }
            }
        });
        usages.getColumns().add(docClassColumn);

        final TableColumn<ValiditySummary, ValiditySummary> elementClassColumn = new TableColumn<>(bundle.getString("elementClass"));
        elementClassColumn.setCellValueFactory(DEFAULT_CELL_VALUE_FACTORY);
        elementClassColumn.setCellFactory(new Callback<TableColumn<ValiditySummary, ValiditySummary>, TableCell<ValiditySummary, ValiditySummary>>() {

            @Override
            public TableCell<ValiditySummary, ValiditySummary> call(TableColumn<ValiditySummary, ValiditySummary> param) {
                return new TableCell<ValiditySummary, ValiditySummary>() {
                    @Override
                    protected void updateItem(ValiditySummary item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null) {
                            if(item.getElementClass()!=null){
                                final ResourceBundle rb = getBundleForClass(item.getElementClass());
                                setText((rb == null) ? null : rb.getString("class"));
                                if(item.getElementId()!=null){
                                    setTooltip(new Tooltip(item.getElementId()));       
                                }
                            }
                        }
                    }
                };
            }
        });
        elementClassColumn.setComparator(new Comparator<ValiditySummary>() {

            @Override
            public int compare(ValiditySummary o1, ValiditySummary o2) {
                if(o1==null && o2==null) return 0;
                else if(o1==null && o2!=null) return -1;
                else if(o1!=null && o2==null) return 1;
                else if(o1.getElementClass()==null && o2.getElementClass()==null) return 0;
                else if(o1.getElementClass()==null && o2.getElementClass()!=null) return -1;
                else if(o1.getElementClass()!=null && o2.getElementClass()==null) return 1;
                else{
                    final ResourceBundle rb1 = getBundleForClass(o1.getElementClass());
                    final ResourceBundle rb2 = getBundleForClass(o2.getElementClass());
                    return rb1.getString("class").compareTo(rb2.getString("class"));
                }
            }
        });
        usages.getColumns().add(elementClassColumn);

        final TableColumn<ValiditySummary, String> propertyColumn = new TableColumn<>(bundle.getString("pseudoId"));
        propertyColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ValiditySummary, String>, ObservableValue<String>>() {

            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<ValiditySummary, String> param) {
                return new SimpleObjectProperty<>(param.getValue().getPseudoId());
            }
        });
        usages.getColumns().add(propertyColumn);
        
        final TableColumn<ValiditySummary, String> labelColumn = new TableColumn<>(bundle.getString("label"));
        labelColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ValiditySummary, String>, ObservableValue<String>>() {

            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<ValiditySummary, String> param) {
                return new SimpleObjectProperty(param.getValue().getLabel());
            }
        });
        usages.getColumns().add(labelColumn);

        final TableColumn<ValiditySummary, String> authorColumn = new TableColumn<>(bundle.getString("author"));
        authorColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ValiditySummary, String>, ObservableValue<String>>() {

            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<ValiditySummary, String> param) {
                return new SimpleObjectProperty(param.getValue().getAuthor());
            }
        });
        usages.getColumns().add(authorColumn);

        final TableColumn<ValiditySummary, Object> validColumn = new ValidColumn();
        usages.getColumns().add(validColumn);

        setCenter(usages);

        usages.setItems(FXCollections.observableArrayList(referenceUsages));

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
                final List<ValiditySummary> referenceUsages;
                switch(choiceBox.getSelectionModel().getSelectedItem()){
                    case VALID: referenceUsages = validitySummaryRepository.getValidation(true); break;
                    case INVALID: referenceUsages = validitySummaryRepository.getValidation(false); break;
                    case ALL:
                    default: referenceUsages= validitySummaryRepository.getValidation();
                }
                usages.setItems(FXCollections.observableArrayList(referenceUsages));
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

    public class StateButtonTableCell extends ButtonTableCell<ValiditySummary, Object> {

        public StateButtonTableCell(Node graphic) {
            super(false, graphic, (Object t) -> true, new Function<Object, Object>() {
                @Override
                public Object apply(Object t) {

                    if (t != null && t instanceof ValiditySummary) {
                        final Session session = Injector.getSession();
                        final Repository repo = session.getRepositoryForType(((ValiditySummary) t).getDocClass());
                        final Element docu = (Element) repo.get(((ValiditySummary) t).getDocId());

                        // Si l'elementid est null, c'est que l'élément est le document lui-même
                        if (((ValiditySummary) t).getElementId() == null) {
                            session.getFrame().addTab(session.getOrCreateElementTab(docu));
                        } // Sinon, c'est que l'élément est inclus quelque part dans le document et il faut le rechercher.
                        else {
                            final Element elt = docu.getChildById(((ValiditySummary) t).getElementId());
                            session.getFrame().addTab(session.getOrCreateElementTab(elt));
                        }
                    }
                    return t;
                }
            });
        }

        @Override
        protected void updateItem(Object item, boolean empty) {
            super.updateItem(item, empty);

            if (item != null) {
                button.setGraphic(new ImageView(SIRS.ICON_EYE));
            }
        }
    }

    private class StateColumn extends TableColumn<ValiditySummary, Object> {

        public StateColumn() {
            super("Détail");
            setEditable(false);
            setSortable(false);
            setResizable(true);
            setPrefWidth(70);
//            setPrefWidth(24);
//            setMinWidth(24);
//            setMaxWidth(24);
            setGraphic(new ImageView(GeotkFX.ICON_MOVEUP));

            setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ValiditySummary, Object>, ObservableValue<Object>>() {
                @Override
                public ObservableValue<Object> call(TableColumn.CellDataFeatures<ValiditySummary, Object> param) {
                    return new SimpleObjectProperty<>(param.getValue());
                }
            });

            setCellFactory(new Callback<TableColumn<ValiditySummary, Object>, TableCell<ValiditySummary, Object>>() {

                @Override
                public TableCell<ValiditySummary, Object> call(TableColumn<ValiditySummary, Object> param) {

                    return new StateButtonTableCell(new ImageView(ICON_CHECK_CIRCLE));
                }
            });
        }
    }

    private class ValidButtonTableCell extends TableCell<ValiditySummary, Object> {

        private final Node defaultGraphic;
        protected final Button button = new Button();

        public ValidButtonTableCell(Node graphic) {
            super();
            defaultGraphic = graphic;
            button.setGraphic(defaultGraphic);
            setGraphic(button);
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            setAlignment(Pos.CENTER);
            button.setOnAction(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent event) {
                    Object t = getItem();
                    if (t != null && t instanceof ValiditySummary) {
                        final Session session = Injector.getSession();
                        final Repository repo = session.getRepositoryForType(((ValiditySummary) t).getDocClass());
                        final Element docu = (Element) repo.get(((ValiditySummary) t).getDocId());

                        // Si l'elementid est null, c'est que l'élément est le document lui-même
                        if (((ValiditySummary) t).getElementId() == null) {
                            docu.setValid(!docu.getValid());
                        } // Sinon, c'est que l'élément est inclus quelque part dans le document et il faut le rechercher.
                        else {
                            final Element elt = docu.getChildById(((ValiditySummary) t).getElementId());
                            elt.setValid(!elt.getValid());
                        }
                        repo.update(docu);
                        ((ValiditySummary) t).setValid(!((ValiditySummary) t).getValid());
                        updateButton(((ValiditySummary) t).getValid());
                    }
                }
            });
        }

        protected void updateButton(final boolean valid) {
            if (!valid) {
                button.setGraphic(new ImageView(ICON_EXCLAMATION_CIRCLE));
                button.setText("Invalidé");
            } else {
                button.setGraphic(defaultGraphic);
                button.setText("Validé");
            }
        }

        @Override
        protected void updateItem(Object item, boolean empty) {
            super.updateItem(item, empty);

            if (item != null) {
                updateButton(((ValiditySummary) item).getValid());
            }
        }
    }

    private class ValidColumn extends TableColumn<ValiditySummary, Object> {

        public ValidColumn() {
            super("État");
            setEditable(false);
            setSortable(false);
            setResizable(true);
            setPrefWidth(120);
//            setPrefWidth(24);
//            setMinWidth(24);
//            setMaxWidth(24);
//            setGraphic(new ImageView(GeotkFX.ICON_MOVEUP));

            setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ValiditySummary, Object>, ObservableValue<Object>>() {
                @Override
                public ObservableValue<Object> call(TableColumn.CellDataFeatures<ValiditySummary, Object> param) {
                    return new SimpleObjectProperty<>(param.getValue());
                }
            });

            setCellFactory(new Callback<TableColumn<ValiditySummary, Object>, TableCell<ValiditySummary, Object>>() {

                @Override
                public TableCell<ValiditySummary, Object> call(TableColumn<ValiditySummary, Object> param) {

                    return new ValidButtonTableCell(new ImageView(ICON_CHECK_CIRCLE));
                }
            });
        }
    }
}
