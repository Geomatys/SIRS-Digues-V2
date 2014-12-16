
package fr.sirs.query;

import static fr.sirs.SIRS.CSS_PATH;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import org.geotoolkit.display2d.GO2Utilities;
import org.geotoolkit.feature.type.AssociationType;
import org.geotoolkit.feature.type.ComplexType;
import org.geotoolkit.feature.type.PropertyDescriptor;
import org.geotoolkit.feature.type.PropertyType;
import org.geotoolkit.gui.javafx.util.TextFieldCompletion;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class FXSQLFilterEditor extends GridPane {

    private static final FilterFactory2 FF = GO2Utilities.FILTER_FACTORY;
    private static final int SPACING = 5;
    private static final Image DIV_HAUT = new Image("/fr/sirs/div_haut.png");
    private static final Image DIV_BAS = new Image("/fr/sirs/div_bas.png");
    
    public static enum Type{
        NONE("-"),
        PROPERTY("PROP"),
        AND("ET"),
        OR("OU");

        private final String text;

        private Type(String text) {
            this.text = text;
        }
        
        @Override
        public String toString() {
            return text;
        }
    }
    public static enum Condition{
        EQUAL("="),
        LIKE("LIKE"),
        SUPERIOR(">"),
        INFERIOR("<"),
        SUPERIOR_EQUAL(">="),
        INFERIOR_EQUAL("<=");
        
        private final String text;

        private Condition(String text) {
            this.text = text;
        }
        
        @Override
        public String toString() {
            return text;
        }
    }
    
    private final ChoiceBox<Type> uiTypeBox = new ChoiceBox<>(FXCollections.observableArrayList(Type.values()));
    private final ChoiceBox<Condition> uiConditionBox = new ChoiceBox<>(FXCollections.observableArrayList(Condition.values()));
    private final TextField uiPropertyName = new TextField();
    private final TextField uiPropertyValue = new TextField();
    private final GridPane uiPropertyPane = new GridPane();
    private ObservableList<String> choices = FXCollections.observableArrayList();
    
    private ComplexType type;
    
    private FXSQLFilterEditor uiSub1 = null;
    private FXSQLFilterEditor uiSub2 = null;
    
    public FXSQLFilterEditor() {
        this(Type.NONE);
    }
    
    public FXSQLFilterEditor(Type type) {
        getStylesheets().add(CSS_PATH);
        alignmentProperty().set(Pos.CENTER_LEFT);
        setHgap(0);
        setVgap(0);
        //setGridLinesVisible(true);
        
        getColumnConstraints().add(new ColumnConstraints());
        getColumnConstraints().add(new ColumnConstraints());
        getColumnConstraints().add(new ColumnConstraints());
        
        final RowConstraints rc1 = new RowConstraints();
        rc1.setVgrow(Priority.ALWAYS);
        rc1.setValignment(VPos.BOTTOM);
        final RowConstraints rc2 = new RowConstraints();
        rc2.setMinHeight(15);
        rc2.setVgrow(Priority.NEVER);
        final RowConstraints rc3 = new RowConstraints();
        rc3.setMinHeight(15);
        rc3.setVgrow(Priority.NEVER);
        final RowConstraints rc4 = new RowConstraints();
        rc4.setVgrow(Priority.ALWAYS);
        rc4.setValignment(VPos.TOP);
        
        getRowConstraints().add(rc1);
        getRowConstraints().add(rc2);
        getRowConstraints().add(rc3);
        getRowConstraints().add(rc4);
        
        uiTypeBox.valueProperty().addListener(this::typeChanged);
        uiTypeBox.getSelectionModel().select(Type.NONE);
        uiTypeBox.getStyleClass().add("btn-gray");
        add(uiTypeBox,0,1,1,2);
        
        uiConditionBox.getStyleClass().add("btn-gray");
        uiConditionBox.setValue(Condition.EQUAL);
        
        uiPropertyPane.setHgap(SPACING);
        uiPropertyPane.setVgap(SPACING);
        uiPropertyPane.setPadding(new Insets(SPACING, SPACING, SPACING, SPACING));
        uiPropertyPane.getColumnConstraints().add(new ColumnConstraints());
        uiPropertyPane.getColumnConstraints().add(new ColumnConstraints());
        uiPropertyPane.getColumnConstraints().add(new ColumnConstraints());
        uiPropertyPane.getRowConstraints().add(new RowConstraints());
        uiPropertyPane.add(uiPropertyName, 0, 0);
        uiPropertyPane.add(uiConditionBox, 1, 0);
        uiPropertyPane.add(uiPropertyValue, 2, 0);
             
        //autocompletion sur les champs
        final TextFieldCompletion textFieldCompletion = new TextFieldCompletion(uiPropertyName){
            @Override
            protected ObservableList<String> getChoices(String text) {
                return FXSQLFilterEditor.this.getChoices();
            }
        };
        
    }

    public ObservableList<String> getChoices() {
        if(type!=null){
            final String text = uiPropertyName.getText();
            final String[] parts = text.split("/");
            
            final ObservableList<String> candidates = FXCollections.observableArrayList();
            
            final StringBuilder sb = new StringBuilder();
            
            ComplexType ct = type;
            for(int i=0;i<parts.length;i++){
                PropertyDescriptor desc = ct.getDescriptor(parts[i]);
                if(desc==null) break;
                final PropertyType propType = desc.getType();
                if(propType instanceof AssociationType){
                    ct = (ComplexType) ((AssociationType) propType).getRelatedType();
                    sb.append(parts[i]).append('/');
                }
            }
            
            if(ct!=null){
                candidates.addAll(listProps(sb.toString(), ct));
            }
            return candidates;
        }
        
        return choices;
    }

    public void setChoices(ObservableList<String> choices) {
        this.choices = choices;
        uiTypeBox.getSelectionModel().select(Type.NONE);
    }

    public ComplexType getType() {
        return type;
    }

    public void setType(ComplexType type) {
        this.type = type;
    }
    
    private static List<String> listProps(String base, ComplexType ct){
        final List<String> lst = new ArrayList<>();
        for(PropertyDescriptor desc : ct.getDescriptors()){
            lst.add(base+desc.getName().getLocalPart());
        }
        Collections.sort(lst);
        return lst;
    }
    
    private void typeChanged(ObservableValue<? extends Type> observable, Type oldValue, Type newValue){
        while(getChildren().size()>1){
            getChildren().remove(1);
        }
        
        if(Type.PROPERTY.equals(newValue)){
            add(uiPropertyPane, 1, 1, 1, 2);
        }else if(Type.AND.equals(newValue) || Type.OR.equals(newValue)){
            uiSub1 = new FXSQLFilterEditor();
            uiSub2 = new FXSQLFilterEditor();
            uiSub1.setChoices(choices);
            uiSub2.setChoices(choices);
            uiSub1.setType(type);
            uiSub2.setType(type);
            add(uiSub1,2,0,1,2);  
            add(uiSub2,2,2,1,2);
            
            
            final ImageView ivhaut = new ImageView(DIV_HAUT);
            final ScrollPane scrollhaut = new ScrollPane(ivhaut);
            scrollhaut.getStyleClass().add("btn-without-style");
            scrollhaut.setMaxHeight(Double.MAX_VALUE);
            scrollhaut.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollhaut.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollhaut.setPrefWidth(20);
            scrollhaut.setPrefHeight(1);
            ivhaut.fitHeightProperty().bind(scrollhaut.heightProperty());
            add(scrollhaut,1,0,1,2);  
            
            final ImageView ivbas = new ImageView(DIV_BAS);
            final ScrollPane scrollbas = new ScrollPane(ivbas);
            scrollbas.getStyleClass().add("btn-without-style");
            scrollbas.setMaxHeight(Double.MAX_VALUE);
            scrollbas.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollbas.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollbas.setPrefWidth(20);
            scrollbas.setPrefHeight(1);
            ivbas.fitHeightProperty().bind(scrollbas.heightProperty());
            add(scrollbas,1,2,1,2);
        }
    }
    
    public Filter toFilter(){
        
        final Type t = uiTypeBox.getValue();
        if(Type.PROPERTY.equals(t)){
            final PropertyName propName = FF.property(uiPropertyName.getText());
            final Literal propValue = FF.literal(uiPropertyValue.getText());
            
            final Condition condition = uiConditionBox.getValue();
            if(Condition.INFERIOR.equals(condition)){
                return FF.less(propName, propValue);
            }else if(Condition.INFERIOR_EQUAL.equals(condition)){
                return FF.lessOrEqual(propName, propValue);
            }else if(Condition.SUPERIOR.equals(condition)){
                return FF.greater(propName, propValue);
            }else if(Condition.SUPERIOR_EQUAL.equals(condition)){
                return FF.greaterOrEqual(propName, propValue);
            }else if(Condition.LIKE.equals(condition)){
                return FF.like(propName, String.valueOf(propValue.getValue()));
            }else{
                return FF.equals(propName, propValue);
            }
            
        }else if(Type.AND.equals(t)){
            return FF.and(uiSub1.toFilter(), uiSub2.toFilter());
        }else if(Type.OR.equals(t)){
            return FF.or(uiSub1.toFilter(), uiSub2.toFilter());
        }else{
            return Filter.INCLUDE;
        }
    }
    
    
}
