
package fr.sirs.map;

import com.vividsolutions.jts.geom.LineString;
import fr.sirs.SIRS;
import fr.sirs.core.InjectorCore;
import fr.sirs.core.LinearReferencingUtilities;
import fr.sirs.core.SessionCore;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.util.SimpleButtonColumn;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import org.geotoolkit.geometry.jts.JTS;
import org.geotoolkit.gui.javafx.util.FXStringCell;
import org.geotoolkit.gui.javafx.util.FXTableCell;
import org.geotoolkit.gui.javafx.util.FXTableView;
import org.geotoolkit.internal.GeotkFX;
import org.geotoolkit.util.StringUtilities;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class FXTronconCut extends VBox {

    public static final Color[] PALETTE = new Color[] {
        Color.BLUE,
        Color.CYAN,
        Color.MAGENTA,
        Color.RED,
        Color.GREEN
    };

    @FXML private TextField uiTronconLabel;
    @FXML private FXTableView<CutPoint> uiCutTable;
    @FXML private FXTableView<Segment> uiSegmentTable;
    @FXML private ToggleButton uiAddCut;
    @FXML private Label TronconTypeNameLabel;

    private final ObjectProperty<TronconDigue> tronconProp = new SimpleObjectProperty<>();
    private final ObservableList<CutPoint> cutPoints = FXCollections.observableArrayList();
    private final ObservableList<Segment> segments = FXCollections.observableArrayList();

    public FXTronconCut(final String typeName) {
        SIRS.loadFXML(this);

        TronconTypeNameLabel.setText(StringUtilities.firstToUpper(typeName) + " :");
        uiTronconLabel.textProperty().bind(Bindings.createStringBinding(()->tronconProp.get()==null?"":tronconProp.get().getLibelle(),tronconProp));

        uiCutTable.setItems(cutPoints);
        uiCutTable.getColumns().add(new DeleteColumn());
        uiCutTable.getColumns().add(new DistanceColumn());

        uiSegmentTable.setItems(segments);
        uiSegmentTable.setEditable(true);
        uiSegmentTable.getColumns().add(new ColorColumn());
        uiSegmentTable.getColumns().add(new TypeColumn());

        final TableColumn<Segment, String> nameColumn = new TableColumn<>("nom");
        nameColumn.setCellValueFactory((TableColumn.CellDataFeatures<Segment, String> param) -> param.getValue().nameProperty);
        nameColumn.setCellFactory((TableColumn<Segment, String> param) -> new FXStringCell());
        nameColumn.setEditable(true);

        uiSegmentTable.getColumns().add(nameColumn);

        // Recalcul des segments lors d'un changement de point de coupe
        cutPoints.addListener(this::cutPointChanged);

        // On vide le panneau lorsqu'on change de tronçon.
        tronconProp.addListener((ObservableValue<? extends TronconDigue> observable, TronconDigue oldValue, TronconDigue newValue) -> {
            cutPoints.clear();
            // Only here because if above list was already empty, the new selected troncon will not be highlighted.
            cutPointChanged(null);
            uiAddCut.setSelected(false);
        });
    }

    public ObservableList<CutPoint> getCutpoints() {
        return cutPoints;
    }

    public ObservableList<Segment> getSegments() {
        return segments;
    }

    public ObjectProperty<TronconDigue> tronconProperty() {
        return tronconProp;
    }

    public boolean isCutMode(){
        return uiAddCut.isSelected();
    }

    private void cutPointChanged(ListChangeListener.Change c) {
        segments.clear();

        final TronconDigue troncon = tronconProp.get();
        if (troncon == null) return;

        final FXTronconCut.CutPoint[] tmpCutPoints = cutPoints.toArray(new FXTronconCut.CutPoint[0]);
        final LineString linear = LinearReferencingUtilities.asLineString(troncon.getGeometry());

        final List<FXTronconCut.Segment> tmpSegments = new ArrayList<>();

        int colorIndex = 0;
        double distanceDebut = 0.0;
        double distanceFin = 0.0;
        for (int i = 0; i < tmpCutPoints.length; i++) {
            if (i > 0) {
                distanceDebut = distanceFin;
            }
            distanceFin = tmpCutPoints[i].distance.doubleValue();

            if (distanceDebut != distanceFin) {
                final FXTronconCut.Segment segment = new FXTronconCut.Segment();
                segment.colorProp.set(PALETTE[colorIndex++ % PALETTE.length]);
                segment.typeProp.set(FXTronconCut.SegmentType.CONSERVER);
                segment.geometryProp.set(LinearReferencingUtilities.cut(linear, distanceDebut, distanceFin));
                JTS.setCRS(segment.geometryProp.get(), InjectorCore.getBean(SessionCore.class).getProjection());
                tmpSegments.add(segment);
            }
        }
        //dernier segment
        distanceDebut = distanceFin;
        distanceFin = Double.MAX_VALUE;
        if (distanceDebut != distanceFin) {
            final FXTronconCut.Segment segment = new FXTronconCut.Segment();
            segment.colorProp.set(PALETTE[colorIndex++ % PALETTE.length]);
            segment.typeProp.set(FXTronconCut.SegmentType.CONSERVER);
            segment.geometryProp.set(LinearReferencingUtilities.cut(linear, distanceDebut, distanceFin));
            JTS.setCRS(segment.geometryProp.get(), InjectorCore.getBean(SessionCore.class).getProjection());
            tmpSegments.add(segment);
        }

        // If no cut points defined, we highlight the entire troncon as "A conserver".
        if (tmpSegments.isEmpty()) {
            final FXTronconCut.Segment segment = new FXTronconCut.Segment();
            segment.colorProp.set(PALETTE[colorIndex++ % PALETTE.length]);
            segment.typeProp.set(FXTronconCut.SegmentType.CONSERVER);
            segment.geometryProp.set(linear);
            JTS.setCRS(segment.geometryProp.get(), InjectorCore.getBean(SessionCore.class).getProjection());
            tmpSegments.add(segment);
        }
        segments.setAll(tmpSegments);
    }

    /*
     * UTILITY CLASSES
     */

    public static final class CutPoint implements Comparable<CutPoint>{
        public final DoubleProperty distance = new SimpleDoubleProperty(0);

        @Override
        public int compareTo(CutPoint o) {
            return Double.compare(distance.get(),o.distance.get());
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof CutPoint) {
                return compareTo((CutPoint)obj) == 0;
            }
            return false;
        }
    }

    public static final class Segment{
        public ObjectProperty<Color> colorProp = new SimpleObjectProperty<>(Color.BLACK);
        public ObjectProperty<SegmentType> typeProp = new SimpleObjectProperty<>(SegmentType.CONSERVER);
        public ObjectProperty<LineString> geometryProp = new SimpleObjectProperty<>();
        public final StringProperty nameProperty = new SimpleStringProperty();
    }

    public static enum SegmentType{
        CONSERVER,
        SECTIONNER,
        ARCHIVER
    }

    private class DeleteColumn extends SimpleButtonColumn {

        public DeleteColumn() {
            super(GeotkFX.ICON_DELETE,
                    new Callback<TableColumn.CellDataFeatures, ObservableValue>() {
                        @Override
                        public ObservableValue call(TableColumn.CellDataFeatures param) {
                            return new SimpleObjectProperty<>(param.getValue());
                        }
                    },
                    (Object t) -> true,
                    new Function() {

                        public Object apply(Object t) {
                            final Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Confirmer la suppression ?",
                                    ButtonType.NO, ButtonType.YES);
                            alert.setResizable(true);
                            final ButtonType res = alert.showAndWait().get();
                            if (ButtonType.YES == res) {
                                uiCutTable.getItems().remove(t);
                            }
                            return null;
                        }
                    },
                    null
            );
        }
    }

    private class DistanceColumn extends TableColumn<CutPoint, Number>{

        public DistanceColumn() {
            super();
            setSortable(false);
            setEditable(true);
            setCellValueFactory((CellDataFeatures<CutPoint, Number> param) -> param.getValue().distance);
        }

    }

    private class ColorColumn extends TableColumn<Segment,Color>{

        public ColorColumn() {
            setSortable(false);
            setResizable(false);
            setEditable(false);
            setPrefWidth(24);
            setMinWidth(24);
            setMaxWidth(24);
            setGraphic(new ImageView(GeotkFX.ICON_STYLE));

            setCellValueFactory((CellDataFeatures<Segment, Color> param) -> param.getValue().colorProp);
            setCellFactory(new Callback<TableColumn<Segment, Color>, TableCell<FXTronconCut.Segment, Color>>() {

                @Override
                public TableCell<Segment, Color> call(TableColumn<Segment, Color> param) {
                    final TableCell<Segment,Color> cell = new TableCell<Segment,Color>(){
                        @Override
                        protected void updateItem(Color item, boolean empty) {
                            super.updateItem(item, empty);
                            setBackground(Background.EMPTY);
                            if(!empty && item!=null){
                                setBackground(new Background(new BackgroundFill(item, CornerRadii.EMPTY, Insets.EMPTY)));
                            }
                        }
                    };
                    return cell;
                }
            });
        }
    }

    private class TypeColumn extends TableColumn<Segment,Segment>{

        public TypeColumn() {
            setSortable(false);
            setEditable(true);
            setCellValueFactory((CellDataFeatures<Segment, Segment> param) -> new SimpleObjectProperty(param.getValue()));

            setCellFactory(new Callback<TableColumn<Segment, Segment>, TableCell<FXTronconCut.Segment, FXTronconCut.Segment>>() {

                @Override
                public TableCell<Segment, Segment> call(TableColumn<Segment, Segment> param) {
                    return new EnumTableCell();
                }
            });

        }

    }

    private static class EnumTableCell extends FXTableCell<Segment, Segment>{

        private final ChoiceBox<SegmentType> choiceBox = new ChoiceBox<>();

        public EnumTableCell() {
            final ObservableList<SegmentType> lst = FXCollections.observableArrayList(SegmentType.values());
            choiceBox.setItems(lst);

            choiceBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<SegmentType>() {
                @Override
                public void changed(ObservableValue<? extends SegmentType> observable, SegmentType oldValue, SegmentType newValue) {
                    if(isEditing()){
                        getItem().typeProp.set(newValue);
                        commitEdit(getItem());
                    }
                }
            });
        }

        @Override
        protected void updateItem(Segment item, boolean empty) {
            super.updateItem(item, empty);

            setText(null);
            setGraphic(null);
            if(item !=null && !empty){
                setText(item.typeProp.get().name());
            }
        }

        @Override
        public void startEdit() {
            choiceBox.getSelectionModel().select(getItem().typeProp.get());
            super.startEdit();
            setText(null);
            setGraphic(choiceBox);
        }

        @Override
        public void commitEdit(Segment newValue) {
            super.commitEdit(newValue);
            setText(getItem().typeProp.get().name());
            setGraphic(null);
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();
            setText(getItem().typeProp.get().name());
            setGraphic(null);
        }

        @Override
        public void terminateEdit() {
            super.terminateEdit();
            setText(getItem().typeProp.get().name());
            setGraphic(null);
        }

    }

}
