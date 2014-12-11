
package fr.sirs.theme.ui;

import fr.sirs.Session;
import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.Objet;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.theme.AbstractTronconTheme;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class DefaultTronconPojoTable extends PojoTable{
    
    private final ObjectProperty<TronconDigue> troncon = new SimpleObjectProperty<>();
    private final AbstractTronconTheme.ThemeGroup group;
    
    private final Label uiCurrent = new Label();
    private final ImageView previousIcon = new ImageView(SIRS.ICON_PREVIOUS);
    private final Button uiPrevious = new Button();
    private final ImageView nextIcon = new ImageView(SIRS.ICON_NEXT);
    private final Button uiNext = new Button();
    private final HBox navigationToolbar = new HBox();
    
    private final ImageView playIcon = new ImageView(SIRS.ICON_PLAY);
    private final ImageView stopIcon = new ImageView(SIRS.ICON_STOP);
    private final Button uiPlay = new Button();
    
    private List<Node> navigablePanels = null;
    private int currentFiche = 0;
    private Mode mode = Mode.TABLE;
    private enum Mode{TABLE, FICHE};

    public DefaultTronconPojoTable(AbstractTronconTheme.ThemeGroup group) {
        super(group.getDataClass(), group.getTableTitle(), true);
        this.group = group;
        
        
        navigationToolbar.setVisible(false);
        navigationToolbar.getStyleClass().add("buttonbarleft");
        
        uiCurrent.setFont(Font.font(20));
        uiCurrent.setAlignment(Pos.CENTER);
        uiCurrent.setTextFill(Color.WHITE);
        
        uiPrevious.setGraphic(previousIcon);
        uiPrevious.getStyleClass().add("btn-without-style"); 
        uiPrevious.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                if(navigablePanels.size()>0){
                    if(currentFiche>0)
                        currentFiche--;
                    else
                        currentFiche=navigablePanels.size()-1;
                    setCenter(navigablePanels.get(currentFiche));
                    uiCurrent.setText((currentFiche+1)+" / "+navigablePanels.size());
                }
            }
        });
        
        uiNext.setGraphic(nextIcon);
        uiNext.getStyleClass().add("btn-without-style"); 
        uiNext.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                if(navigablePanels.size()>0){
                    if(currentFiche<navigablePanels.size()-1)
                        currentFiche++;
                    else
                        currentFiche=0;
                    setCenter(navigablePanels.get(currentFiche));
                    uiCurrent.setText((currentFiche+1)+" / "+navigablePanels.size());
                }
            }
        });
        navigationToolbar.getChildren().addAll(uiPrevious, uiCurrent, uiNext);
        
        uiPlay.setGraphic(playIcon);
        uiPlay.getStyleClass().add("btn-without-style"); 
        uiPlay.setOnAction(new  EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                if(mode==Mode.FICHE){
                    mode = Mode.TABLE;
                    navigationToolbar.setVisible(false);
                    setCenter(uiTable);
                    editableProperty.setValue(true);
                    uiPlay.setGraphic(playIcon);
                }
                else{
                    mode = Mode.FICHE;
                    navigationToolbar.setVisible(true);
                    if(navigablePanels==null){
                        updateFiches();
                    }
                    if(navigablePanels.size()>0){
                        currentFiche=0;
                        navigablePanels.get(currentFiche);
                        setCenter(navigablePanels.get(currentFiche));
                        uiCurrent.setText((currentFiche+1)+" / "+navigablePanels.size());
                    }
                    else{
                        uiCurrent.setText(0+" / "+0);
                    }
                    editableProperty.setValue(false);
                    uiPlay.setGraphic(stopIcon);
                }
            }
        });
        
        searchEditionToolbar.getChildren().add(0, uiPlay);
        
        topPane.setLeft(navigationToolbar);
        
        final ChangeListener listener = (ChangeListener) (ObservableValue observable, Object oldValue, Object newValue) -> {
            updateTable();
            if(mode==Mode.FICHE){
                mode = Mode.TABLE;
                navigablePanels=null;
                navigationToolbar.setVisible(false);
                setCenter(uiTable);
                editableProperty.setValue(true);
                uiPlay.setGraphic(playIcon);
            }
        };
        
        troncon.addListener(listener);
    }
    
    private void updateFiches(){
        navigablePanels = new ArrayList<>();
        for(final Element elt : getAllValues()){
            navigablePanels.add(generateEditionPane(elt));
        }
    }
    
    private Node generateEditionPane(final Object pojo) {
        return new FXStructurePane((Objet) pojo);
    }
    
    public ObjectProperty<TronconDigue> tronconPropoerty(){
        return troncon;
    }
        
    private void updateTable(){
        final TronconDigue trc = troncon.get();
        if(trc==null || group==null){
            setTableItems(FXCollections::emptyObservableList);
        }
        else{
            //JavaFX bug : sortable is not possible on filtered list
            // http://stackoverflow.com/questions/17958337/javafx-tableview-with-filteredlist-jdk-8-does-not-sort-by-column
            // https://javafx-jira.kenai.com/browse/RT-32091
            setTableItems(() -> {
                final SortedList sortedList = new SortedList(group.getExtractor().apply(trc));
                sortedList.comparatorProperty().bind(getUiTable().comparatorProperty());
                return sortedList;
            });
        }
    }

    @Override
    protected void deletePojos(Element ... pojos) {
        for(Element pojo : pojos){
            final TronconDigue trc = troncon.get();
            if(trc==null) return;
            group.getDeletor().delete(trc, pojo);
            //sauvegarde des modifications du troncon
            final Session session = Injector.getBean(Session.class);
            session.getTronconDigueRepository().update(trc);
        }
    }

    @Override
    protected void elementEdited(TableColumn.CellEditEvent<Element, Object> event) {
        final TronconDigue obj = troncon.get();
        session.getTronconDigueRepository().update(obj);
    }
    
    @Override
    protected Object createPojo() {
        Objet pojo = null;
        try {
            final TronconDigue trc = troncon.get();
            final Constructor pojoConstructor = pojoClass.getConstructor();
            pojo = (Objet) pojoConstructor.newInstance();
            trc.getStructures().add(pojo);
            pojo.setTroncon(trc.getId());
            session.getTronconDigueRepository().update(trc);
//        if (pojoClass==Crete.class) {
//            System.out.println("Création d'un nouvel objet");
//            
//            final Crete nouvelleCrete = new Crete();
//            nouvelleCrete.setTroncon(trc.getId());
//            trc.getStructures().add(nouvelleCrete);
//            session.getTronconDigueRepository().update(trc);
//            updateTable();
//        }else {
//            new Alert(Alert.AlertType.INFORMATION, "Aucune entrée ne peut être créée.").showAndWait();
//        }
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(DefaultTronconPojoTable.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(DefaultTronconPojoTable.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(DefaultTronconPojoTable.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(DefaultTronconPojoTable.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(DefaultTronconPojoTable.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(DefaultTronconPojoTable.class.getName()).log(Level.SEVERE, null, ex);
        }
        return pojo;
    }
}
