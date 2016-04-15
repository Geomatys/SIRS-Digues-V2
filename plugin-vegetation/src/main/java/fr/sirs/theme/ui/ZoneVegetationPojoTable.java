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

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.ArbreVegetation;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.GeometryType;
import fr.sirs.core.model.InvasiveVegetation;
import fr.sirs.core.model.LabelMapper;
import fr.sirs.core.model.PeuplementVegetation;
import fr.sirs.core.model.TraitementZoneVegetation;
import fr.sirs.core.model.ZoneVegetation;
import fr.sirs.plugin.vegetation.PluginVegetation;
import static fr.sirs.plugin.vegetation.PluginVegetation.DEFAULT_INVASIVE_VEGETATION_TYPE;
import static fr.sirs.plugin.vegetation.PluginVegetation.DEFAULT_PEUPLEMENT_VEGETATION_TYPE;
import static fr.sirs.plugin.vegetation.PluginVegetation.paramTraitement;
import fr.sirs.util.SirsStringConverter;
import java.text.NumberFormat;
import java.util.function.Consumer;
import java.util.logging.Level;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.stage.Modality;
import javafx.util.Callback;
import javax.measure.unit.SI;
import org.geotoolkit.display.MeasureUtilities;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class ZoneVegetationPojoTable extends ListenPropertyPojoTable<String> {

    private static final SirsStringConverter converter = new SirsStringConverter();

    public ZoneVegetationPojoTable(String title) {
        super(ZoneVegetation.class, title);
        setDeletor(new Consumer<Element>() {

            @Override
            public void accept(Element pojo) {
                if(pojo instanceof ZoneVegetation) ((AbstractSIRSRepository) Injector.getSession().getRepositoryForClass(pojo.getClass())).remove(pojo);
            }
        });
        getTable().getColumns().add(4, (TableColumn) new VegetationClassColumm());
        getTable().getColumns().add(5, (TableColumn) new VegetationTypeColumm());
        getTable().getColumns().add(6, (TableColumn) new VegetationAreaColumm());
    }

    @Override
    protected ZoneVegetation createPojo() {

        final ZoneVegetation zone;

        final ChoiceStage stage = new ChoiceStage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();

        final Class<? extends ZoneVegetation> retrievedClass = stage.getRetrievedElement().get();
        if(retrievedClass!=null){
            //Création de la zone
            final AbstractSIRSRepository zoneVegetationRepo = Injector.getSession().getRepositoryForClass(retrievedClass);
            zone = (ZoneVegetation) zoneVegetationRepo.create();
            zone.setForeignParentId(getPropertyReference());
            zoneVegetationRepo.add(zone);
            getAllValues().add(zone);

            //Création du traitement associé
            zone.setTraitement(Injector.getSession().getElementCreator().createElement(TraitementZoneVegetation.class));

            // S'il s'agit d'une zone d'invasive ou de peuplement, il faut affecter le type par défaut et effectuer le paramétrage éventuel

            if(retrievedClass==PeuplementVegetation.class){
                ((PeuplementVegetation) zone).setTypeVegetationId(DEFAULT_PEUPLEMENT_VEGETATION_TYPE);
                paramTraitement(PeuplementVegetation.class, (PeuplementVegetation) zone, DEFAULT_PEUPLEMENT_VEGETATION_TYPE);
            }
            else if(retrievedClass==InvasiveVegetation.class){
                ((InvasiveVegetation) zone).setTypeVegetationId(DEFAULT_INVASIVE_VEGETATION_TYPE);
                paramTraitement(InvasiveVegetation.class, (InvasiveVegetation) zone, DEFAULT_INVASIVE_VEGETATION_TYPE);
            }
            else if(retrievedClass==ArbreVegetation.class){
                zone.setGeometryType(GeometryType.PONCTUAL);
            }

        }
        else {
            zone = null;
        }
        return zone;
    }

    @Override
    protected void elementEdited(TableColumn.CellEditEvent<Element, Object> event){
        final Element obj = event.getRowValue();
        if(obj != null){
            ((AbstractSIRSRepository) Injector.getSession().getRepositoryForClass(obj.getClass())).update(obj);
        }
    }

    private static class VegetationClassColumm extends TableColumn<ZoneVegetation, Class<? extends ZoneVegetation>>{

        private VegetationClassColumm(){
            super("Type de zone");
            setEditable(false);
            setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ZoneVegetation, Class<? extends ZoneVegetation>>, ObservableValue<Class<? extends ZoneVegetation>>>() {

                @Override
                public ObservableValue<Class<? extends ZoneVegetation>> call(CellDataFeatures<ZoneVegetation, Class<? extends ZoneVegetation>> param) {
                    if(param!=null && param.getValue()!=null)
                        return new SimpleObjectProperty<>(param.getValue().getClass());
                    else return null;
                }
            });
            setCellFactory(new Callback<TableColumn<ZoneVegetation, Class<? extends ZoneVegetation>>, TableCell<ZoneVegetation, Class<? extends ZoneVegetation>>>() {

                @Override
                public TableCell<ZoneVegetation, Class<? extends ZoneVegetation>> call(TableColumn<ZoneVegetation, Class<? extends ZoneVegetation>> param) {
                    return new VegetationClassCell();
                }
            });
        }
    }

    private static class VegetationClassCell extends TableCell<ZoneVegetation, Class<? extends ZoneVegetation>> {
        @Override
        public void updateItem(final Class<? extends ZoneVegetation> item, final boolean empty){
            super.updateItem(item, empty);
            if(item==null || empty){
                setText("");
            }
            else{
                try{
                    final String className = LabelMapper.get(item).mapClassName();
                    setText(className);
                } catch(Exception e){
                    SIRS.LOGGER.log(Level.WARNING, e.getMessage());
                    setText("");
                }
            }
        }
    }

    private static class VegetationTypeColumm extends TableColumn<ZoneVegetation, String>{

        private VegetationTypeColumm(){
            super("Type de végétation");
            setEditable(false);
            setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ZoneVegetation, String>, ObservableValue<String>>() {

                @Override
                public ObservableValue<String> call(CellDataFeatures<ZoneVegetation, String> param) {
                    if(param!=null){
                        final ZoneVegetation zone = param.getValue();
                        if(zone instanceof PeuplementVegetation){
                           return ((PeuplementVegetation)zone).typeVegetationIdProperty();
                        }else if(zone instanceof InvasiveVegetation){
                            return ((InvasiveVegetation)zone).typeVegetationIdProperty();
                        }
                        else return null;
                    }
                    else return null;
                }
            });
            setCellFactory(new Callback<TableColumn<ZoneVegetation, String>, TableCell<ZoneVegetation, String>>() {

                @Override
                public TableCell<ZoneVegetation, String> call(TableColumn<ZoneVegetation, String> param) {
                    return new VegetationTypeCell();
                }
            });
        }
    }

    private static class VegetationTypeCell extends TableCell<ZoneVegetation, String> {
        @Override
        public void updateItem(final String item, final boolean empty){
            super.updateItem(item, empty);
            if(item==null || empty){
                setText("");
            }
            else{
                try{
                    final String vegetationType = converter.toString(Injector.getSession().getPreviews().get(item));
                    setText(vegetationType);
                } catch(Exception e){
                    SIRS.LOGGER.log(Level.WARNING, e.getMessage());
                    setText("");
                }
            }
        }
    }

    private static class VegetationAreaColumm extends TableColumn<ZoneVegetation, Geometry>{

        private VegetationAreaColumm(){
            super("Surface (m²)");
            setEditable(false);
            setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ZoneVegetation, Geometry>, ObservableValue<Geometry>>() {

                @Override
                public ObservableValue<Geometry> call(TableColumn.CellDataFeatures<ZoneVegetation, Geometry> param) {
                    if(param!=null){
                        final ZoneVegetation zone = param.getValue();
                        if(zone!=null) return zone.geometryProperty();
                        else return null;
                    }
                    else return null;
                }
            });
            setCellFactory((TableColumn<ZoneVegetation, Geometry> param) -> new VegetationAreaCell());
        }
    }

    private static class VegetationAreaCell extends TableCell<ZoneVegetation, Geometry> {
        @Override
        public void updateItem(final Geometry item, final boolean empty){
            super.updateItem(item, empty);
            if(item==null || empty){
                setText("");
            }
            else{
                setText(getGeometryInfo(item));
            }
        }
    }

    /**
     * Calcul de la surface en m².
     * @param geometry
     * @return
     */
    private static String getGeometryInfo(final Geometry geometry) {
        if (geometry != null && (geometry instanceof Polygon || geometry instanceof MultiPolygon)) {
            final String surface = NumberFormat.getNumberInstance().format(
                    MeasureUtilities.calculateArea(geometry, Injector.getSession().getProjection(), SI.SQUARE_METRE));
            return surface;
        }
        else {
            return "";
        }
    }


    /**
     * Window allowing to define the type of ZoneVegetation at creation stage.
     */
    private static class ChoiceStage extends PojoTableComboBoxChoiceStage<Class<? extends ZoneVegetation>, Class<? extends ZoneVegetation>> {

        private ChoiceStage(){
            super();
            setTitle("Choix du type de zone");
            comboBox.setItems(PluginVegetation.zoneVegetationClasses());
            retrievedElement.bind(comboBox.getSelectionModel().selectedItemProperty());
        }
    }
}
