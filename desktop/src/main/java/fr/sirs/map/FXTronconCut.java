
package fr.sirs.map;

import com.vividsolutions.jts.geom.LineString;
import fr.sirs.SIRS;
import fr.sirs.core.model.TronconDigue;
import java.util.function.Function;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import org.geotoolkit.gui.javafx.util.ButtonTableCell;
import org.geotoolkit.gui.javafx.util.FXTableCell;
import org.geotoolkit.internal.GeotkFX;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class FXTronconCut extends VBox{
    
    @FXML private TextField uiTronconLabel;
    @FXML private TableView<CutPoint> uiCutTable;
    @FXML private TableView<Segment> uiSegmentTable;
    @FXML private ToggleButton uiAddCut;

    private final ObjectProperty<TronconDigue> tronconProp = new SimpleObjectProperty<>();
    private final ObservableList<CutPoint> cutpoints = FXCollections.observableArrayList();
    private final ObservableList<Segment> segments = FXCollections.observableArrayList();
    
    public FXTronconCut() {
        SIRS.loadFXML(this);
        uiTronconLabel.textProperty().bind(Bindings.createStringBinding(()->tronconProp.get()==null?"":tronconProp.get().getLibelle(),tronconProp));
        
        uiCutTable.setItems(cutpoints);
        uiCutTable.getColumns().add(new DeleteColumn());
        uiCutTable.getColumns().add(new DistanceColumn());
        
        uiSegmentTable.setItems(segments);
        uiSegmentTable.getColumns().add(new ColorColumn());
        uiSegmentTable.getColumns().add(new TypeColumn());
        
    }

    public ObservableList<CutPoint> getCutpoints() {
        return cutpoints;
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
    
    public static final class CutPoint implements Comparable<CutPoint>{
        public final DoubleProperty distance = new SimpleDoubleProperty(0);

        @Override
        public int compareTo(CutPoint o) {
            return Double.compare(distance.get(),o.distance.get());
        }
    }
    
    public static final class Segment{
        public ObjectProperty<Color> colorProp = new SimpleObjectProperty<>(Color.BLACK);
        public ObjectProperty<SegmentType> typeProp = new SimpleObjectProperty<>(SegmentType.CONSERVER);
        public ObjectProperty<LineString> geometryProp = new SimpleObjectProperty<>();
    }
    
    public static enum SegmentType{
        CONSERVER,
        SECTIONNER,
        ARCHIVER
    }
    
    
    private class DeleteColumn extends TableColumn{

        public DeleteColumn() {
            super("Suppression");          
            setSortable(false);
            setResizable(false);
            setPrefWidth(24);
            setMinWidth(24);
            setMaxWidth(24);
            setGraphic(new ImageView(GeotkFX.ICON_DELETE));
            
            setCellValueFactory(new Callback<TableColumn.CellDataFeatures, ObservableValue>() {
                @Override
                public ObservableValue call(TableColumn.CellDataFeatures param) {
                    return new SimpleObjectProperty<>(param.getValue());
                }
            });
            setCellFactory(new Callback<TableColumn, TableCell>() {

                public TableCell call(TableColumn param) {
                    return new ButtonTableCell(
                            false,new ImageView(GeotkFX.ICON_DELETE), (Object t) -> true, new Function() {
                                @Override
                                public Object apply(Object t) {
                                    final ButtonType res = new Alert(Alert.AlertType.CONFIRMATION,"Confirmer la suppression ?",
                                            ButtonType.NO, ButtonType.YES).showAndWait().get();
                                    if(ButtonType.YES == res){
                                        uiCutTable.getItems().remove(t);
                                    }
                                    return null;
                                }
                            }); 
                }
            });
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
