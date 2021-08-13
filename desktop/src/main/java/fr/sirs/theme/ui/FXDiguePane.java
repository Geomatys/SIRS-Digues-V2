/**
 * This file is part of SIRS-Digues 2.
 *
 * Copyright (C) 2016, FRANCE-DIGUES,
 *
 * SIRS-Digues 2 is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * SIRS-Digues 2 is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SIRS-Digues 2. If not, see <http://www.gnu.org/licenses/>
 */
package fr.sirs.theme.ui;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.core.component.TronconDigueRepository;
import fr.sirs.core.model.AbstractAmenagementHydraulique;
import fr.sirs.core.model.AbstractObservation;
import fr.sirs.core.model.Digue;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.Preview;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.util.SirsStringConverter;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.geotoolkit.gui.javafx.util.ButtonTableCell;
import org.geotoolkit.gui.javafx.util.FXTableCell;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class FXDiguePane extends FXDiguePaneStub {

    @Autowired private Session session;

    @FXML private VBox centerContent;

    private final TronconPojoTable table;


    protected FXDiguePane() {
        super();
        Injector.injectDependencies(this);
        table = new TronconPojoTable(elementProperty());
        table.editableProperty().set(false);
        table.parentElementProperty().bind(elementProperty);
        centerContent.getChildren().add(table);
    }

    public FXDiguePane(final Digue digue){
        this();
        this.elementProperty().set(digue);
    }

    /**
     *
     * @param observable
     * @param oldValue
     * @param newValue
     */
    @Override
    public void initFields(ObservableValue<? extends Digue> observable, Digue oldValue, Digue newValue) {
        super.initFields(observable, oldValue, newValue);
        if (newValue != null) {
            table.setTableItems(()->FXCollections.observableArrayList(((TronconDigueRepository) session.getRepositoryForClass(TronconDigue.class)).getByDigue(newValue)));
        }
    }

    private class TronconPojoTable extends PojoTable {

        public TronconPojoTable(final ObjectProperty<? extends Element> container) {
            super(TronconDigue.class, "Tronçons de la digue", container);
            createNewProperty.set(false);
            fichableProperty.set(false);
            uiAdd.setVisible(false);
            uiFicheMode.setVisible(false);
            uiDelete.setVisible(false);
            setDeletor(input -> {
                if (input instanceof TronconDigue) {
                    ((TronconDigue)input).setDigueId(null);
                    session.getRepositoryForClass((Class)input.getClass()).update(input);
                }
            });

            getColumns().add(new TestColumn());
        }

        @Override
        protected TronconDigue createPojo() {
            throw new UnsupportedOperationException("Vous ne devez pas créer de tronçon à partir d'ici !");
        }
    }

    private class TestColumn extends TableColumn<Element, Element>{

        public TestColumn() {
            super("Test");
            setEditable(false);
            setSortable(false);
            setResizable(true);
            setPrefWidth(70);

            setCellValueFactory((TableColumn.CellDataFeatures<Element, Element> param) -> {
                return new SimpleObjectProperty<>(param.getValue());
            });

            setCellFactory((TableColumn<Element, Element> param) -> {
                return new TestTableCell();
            });
        }
    }

    private class TestTableCell extends FXTableCell<Element, Element> {

        private final CheckBox isAmenagementHydrauliqueCheckBox = new CheckBox();
        private final ComboBox<Preview> uiAmenagementHydrauliqueBox = new ComboBox();

        public TestTableCell() {
            super();
            HBox hb = new HBox();
            hb.getChildren().add(uiAmenagementHydrauliqueBox);
            hb.getChildren().add(isAmenagementHydrauliqueCheckBox);
            setGraphic(hb);
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            setAlignment(Pos.CENTER);

            uiAmenagementHydrauliqueBox.setItems(SIRS.observableList(
                Injector.getSession().getPreviews().getByClass(AbstractAmenagementHydraulique.class)).sorted());
            final SirsStringConverter stringConverter = new SirsStringConverter();
            uiAmenagementHydrauliqueBox.setConverter(stringConverter);

//            uiAmenagementHydrauliqueBox.valueProperty().addListener(this::updateAHList);
        }

//        private void updateAHList(ObservableValue<? extends Preview> observable, Preview oldValue, Preview newValue){
//            if(newValue==null){
//                uiAmenagementHydrauliqueBox.setItems(FXCollections.emptyObservableList());
//            } else {
//                final fr.sirs.Session session = Injector.getSession();
//                final TronconDigue troncon = session.getRepositoryForClass(typeClass).get(newValue.getElementId());
//                final List<SystemeReperage> srs = ((SystemeReperageRepository) session.getRepositoryForClass(SystemeReperage.class)).getByLinear(troncon);
//                uiAmenagementHydrauliqueBox.setItems(FXCollections.observableArrayList(srs));
//                uiAmenagementHydrauliqueBox.getItems().add(null);
//                uiAmenagementHydrauliqueBox.getSelectionModel().selectFirst();
//
//                final String defaultSRID = troncon.getSystemeRepDefautId();
//                if (defaultSRID != null) {
//                    for (final SystemeReperage sr : srs) {
//                        if (defaultSRID.equals(sr.getId())) {
//                            uiAmenagementHydrauliqueBox.getSelectionModel().select(sr);
//                            break;
//                        }
//                    }
//                }
//            }
//        }

        @Override
        protected void updateItem(Element item, boolean empty) {
            super.updateItem(item, empty);

            if (item instanceof TronconDigue) {
                final TronconDigue td = (TronconDigue) item;
                final String amenagementHydrauliqueId = td.getAmenagementHydrauliqueId();

                if (amenagementHydrauliqueId == null) {
                    isAmenagementHydrauliqueCheckBox.selectedProperty().setValue(false);
                } else {
                    isAmenagementHydrauliqueCheckBox.selectedProperty().setValue(true);
                    final Preview preview = Injector.getSession().getPreviews().get(amenagementHydrauliqueId);
                    SingleSelectionModel<Preview> selectionModel = uiAmenagementHydrauliqueBox.getSelectionModel();
                    selectionModel.select(preview);
                }
            }
        }
    }
}
