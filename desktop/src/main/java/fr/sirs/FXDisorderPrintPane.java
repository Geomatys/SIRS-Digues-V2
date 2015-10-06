package fr.sirs;

import com.vividsolutions.jts.geom.Point;
import fr.sirs.core.LinearReferencingUtilities;
import fr.sirs.core.TronconUtils;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.Desordre;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.RefTypeDesordre;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.theme.ui.PojoTable;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Tab;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;
import org.apache.sis.measure.NumberRange;
import org.geotoolkit.display2d.GO2Utilities;
import org.geotoolkit.gui.javafx.util.FXNumberCell;
import org.geotoolkit.referencing.LinearReferencing;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class FXDisorderPrintPane extends BorderPane {
    
    @FXML Tab uiTronconChoice;
    @FXML Tab uiDisorderTypeChoice;
    @FXML Tab uiOptionChoice;
    
    @FXML CheckBox uiOptionPhoto;
    @FXML CheckBox uiOptionReseauOuvrage;
    @FXML CheckBox uiOptionVoirie;
    
    @FXML DatePicker uiOptionDebut;
    @FXML DatePicker uiOptionFin;
    
    @FXML CheckBox uiOptionArchive;
    
    private final Map<String, ObjectProperty<Number>[]> prsByTronconId = new HashMap<>();
    private final TronconChoicePojoTable tronconsTable = new TronconChoicePojoTable();
    private final DisorderTypeChoicePojoTable disordreTypesTable = new DisorderTypeChoicePojoTable();
    
    public FXDisorderPrintPane(){
        SIRS.loadFXML(this, FXDisorderPrintPane.class);
        tronconsTable.setTableItems(()-> (ObservableList) FXCollections.observableList(Injector.getSession().getRepositoryForClass(TronconDigue.class).getAll()));
        uiTronconChoice.setContent(tronconsTable);
        disordreTypesTable.setTableItems(()-> (ObservableList) FXCollections.observableList(Injector.getSession().getRepositoryForClass(RefTypeDesordre.class).getAll()));
        uiDisorderTypeChoice.setContent(disordreTypesTable);
    }
    
    @FXML private void cancel(){
        
    }
    
    
    @FXML 
    private void print(){
        Injector.getSession().getTaskManager().submit("Génération de fiches détaillées de désordres",
        new Thread(() -> {
            
            final List<Desordre> desordres = Injector.getSession().getRepositoryForClass(Desordre.class).getAll();
            
            final List<String> tronconIds = new ArrayList<>();
            for(final Element element : tronconsTable.getSelectedItems()){
                tronconIds.add(element.getId());
            }
            final List<String> typeDesordresIds = new ArrayList<>();
            for(final Element element : disordreTypesTable.getSelectedItems()){
                typeDesordresIds.add(element.getId());
            }
            
            long minTimeSelected = Long.MIN_VALUE;
            long maxTimeSelected = Long.MAX_VALUE;
            LocalDateTime tmpTimeSelected = uiOptionDebut.getValue()==null ? null : uiOptionDebut.getValue().atTime(LocalTime.MIDNIGHT);
            if (tmpTimeSelected !=null) minTimeSelected = Timestamp.valueOf(tmpTimeSelected).getTime();

            tmpTimeSelected = uiOptionFin.getValue()==null ? null : uiOptionFin.getValue().atTime(LocalTime.MIDNIGHT);
            if (tmpTimeSelected !=null) maxTimeSelected = Timestamp.valueOf(tmpTimeSelected).getTime();

            final NumberRange<Long> selectedRange = NumberRange.create(minTimeSelected, true, maxTimeSelected, true);
                        
            // On retire les désordres de la liste dans les cas suivants :
            desordres.removeIf(
                    (Desordre desordre) -> {
                        
                        final boolean conditionRetrait;
                        
                        final boolean conditionSurTronconEtType;
                        /*
                        CONDITIONS DE RETRAIT PORTANT SUR LES TRONÇONS 
                        SÉLECTIONNÉS, LES PR LIMITES ET LES TYPES DE DÉSORDRES.
                        */
                        if(!tronconIds.isEmpty() && desordre.getForeignParentId()!=null){
                            final String linearId = desordre.getForeignParentId();
                            
                            /*
                            Sous-condition de retrait 1 : si le désordre est 
                            associé à un tronçon qui n'est pas sélectionné dans 
                            la liste.
                            */
                            final boolean linearSelected = !tronconIds.contains(linearId);
                            
                            /*
                            Sous-condition de retrait 2 : si le désordre est 
                            d'un type qui n'est pas sélectionné dans la liste. 
                            */
                            final boolean typeSelected;
                                    // Si on n'a sélectionné aucun désordre, on laisse passer a priori quel que soit le type de désordre.
                                    if (typeDesordresIds.isEmpty()) typeSelected = false;
                                    // Si la liste de sélection des types de désordres n'est pas vide on vérifie de type de désordre
                                    else typeSelected = (desordre.getTypeDesordreId()==null 
                                            || !typeDesordresIds.contains(desordre.getTypeDesordreId()));
                                    
                            /* 
                            Sous-condition de retrait 3 : si le désordre a des PRs de 
                            début et de fin et si le tronçon a des PRs de début et 
                            de fin (i.e. s'il a un SR par défaut qui a permi de les 
                            calculer), alors on vérifie :
                            */
                            final boolean prOutOfRange;
                            if(desordre.getPrDebut()!=Float.NaN 
                                    && desordre.getPrFin()!=Float.NaN
                                    && prsByTronconId.get(linearId)!=null
                                    && prsByTronconId.get(linearId)!=null
                                    && prsByTronconId.get(linearId)[0]!=null
                                    && prsByTronconId.get(linearId)[1]!=null
                                    && prsByTronconId.get(linearId)[0].get()!=null
                                    && prsByTronconId.get(linearId)[1].get()!=null){
                                final float prInf, prSup;
                                if(desordre.getPrDebut() < desordre.getPrFin()) {
                                    prInf=desordre.getPrDebut();
                                    prSup=desordre.getPrFin();
                                } else {
                                    prInf=desordre.getPrFin();
                                    prSup=desordre.getPrDebut();
                                }
                                prOutOfRange = (prInf < prsByTronconId.get(linearId)[0].get().floatValue()) // Si le désordre s'achève avant le début de la zone du tronçon que l'on souhaite.
                                || (prSup > prsByTronconId.get(linearId)[1].get().floatValue()); // Si le désordre débute après la fin de la zone du tronçon que l'on souhaite.
                            } 
                            else prOutOfRange=false;

                            conditionSurTronconEtType = linearSelected // Si le tronçon ne figure pas parmi les tronçons sélectionnés.
                                    || typeSelected // Si le type du désordre n'est pas parmi les types sélectionnés
                                    || prOutOfRange; // Si le désordre est en dehors des PR indiqués pour le tronçon
                        } 
                        else conditionSurTronconEtType = false;
                        
                        
                        final boolean conditionOptions;
                        /*
                        CONDITION PORTANT SUR LES OPTIONS
                        */
                        // 1- Si on a décidé de ne pas générer de fiche pour les désordres archivés.
                        final boolean archiveCondition = (uiOptionArchive.isSelected() && desordre.getDate_fin()!=null);
                        
                        // 2- Si le désordre n'a pas eu lieu durant la période retenue
                        final boolean periodeCondition;

                        long minTime = Long.MIN_VALUE;
                        long maxTime = Long.MAX_VALUE;
                        LocalDateTime tmpTime = desordre.getDate_debut()==null ? null : desordre.getDate_debut().atTime(LocalTime.MIDNIGHT);
                        if (tmpTime != null) minTime = Timestamp.valueOf(tmpTime).getTime();
                        
                        tmpTime = desordre.getDate_fin()==null ? null : desordre.getDate_fin().atTime(LocalTime.MIDNIGHT);
                        if (tmpTime != null) maxTime = Timestamp.valueOf(tmpTime).getTime();

                        final NumberRange<Long> desordreRange = NumberRange.create(minTime, true, maxTime, true);
                        periodeCondition = !selectedRange.intersects(desordreRange);
                        
                        conditionOptions = archiveCondition || periodeCondition;
                        
                        return conditionSurTronconEtType || conditionOptions;
                        
                    });
            
            try {
                if(!desordres.isEmpty()){
                    Injector.getSession().getPrintManager().printDesordres(desordres, uiOptionPhoto.isSelected(), uiOptionReseauOuvrage.isSelected(), uiOptionVoirie.isSelected());
                }
            } catch (Exception ex) {
                SIRS.LOGGER.log(Level.WARNING, null, ex);
            }
        }));
    }
    
    
    private class TronconChoicePojoTable extends PojoTable {
        
        public TronconChoicePojoTable() {
            super(TronconDigue.class, "Tronçons");
            getColumns().remove(editCol);
            editableProperty.set(false);
            TableView table = getTable();
            table.editableProperty().unbind();
            table.setEditable(true);
            for(final Object o : table.getColumns()){
                if(o instanceof TableColumn){
                    final TableColumn c = (TableColumn)o;
                    c.editableProperty().unbind();
                    c.setEditable(false);
                }
            }
            getColumns().add(new SelectPRColumn("PR début", ExtremiteTroncon.DEBUT));
            getColumns().add(new SelectPRColumn("PR fin", ExtremiteTroncon.FIN));
        }
    }
    
    private class DisorderTypeChoicePojoTable extends PojoTable {
        
        public DisorderTypeChoicePojoTable() {
            super(RefTypeDesordre.class, "Types de désordres");
            getColumns().remove(editCol);
            editableProperty.set(false);
        }
    }
    
    private enum ExtremiteTroncon {DEBUT, FIN}
    
    private class SelectPRColumn extends TableColumn {
        
        
        public SelectPRColumn(final String text, final ExtremiteTroncon extremite){
            super(text);
            
            setEditable(true);
            
            setCellFactory(new Callback<TableColumn<TronconDigue, Number>, TableCell<TronconDigue, Number>>() {

                @Override
                public TableCell<TronconDigue, Number> call(TableColumn<TronconDigue, Number> param) {
                    TableCell<TronconDigue, Number> tableCell = new FXNumberCell(Float.class);
                    tableCell.setEditable(true);
                    return tableCell;
                }
            });
            setCellValueFactory(new Callback<TableColumn.CellDataFeatures<TronconDigue, Number>, ObservableValue<Number>>() {

                
                @Override
                public ObservableValue<Number> call(CellDataFeatures<TronconDigue, Number> param) {
                    
                    if(param!=null && param.getValue()!=null){
                        final TronconDigue troncon = param.getValue();
                        if(troncon.getSystemeRepDefautId()!=null 
                                && troncon.getGeometry()!=null 
                                && troncon.getId()!=null){
                            final int index = extremite==ExtremiteTroncon.FIN ? 1:0; // Si on est à la fin du tronçon le pr se trouve à l'index 1 du tableau, sinon, par défaut on se place au début et on met l'index à 0
                            final ObjectProperty<Number> prProperty;
                            if(prsByTronconId.get(troncon.getId())==null) prsByTronconId.put(troncon.getId(), new ObjectProperty[2]);
                            if(prsByTronconId.get(troncon.getId())[index]==null){
                                prProperty = new SimpleObjectProperty<>();
                                final SystemeReperage sr = Injector.getSession().getRepositoryForClass(SystemeReperage.class).get(troncon.getSystemeRepDefautId());
                                final LinearReferencing.SegmentInfo[] tronconSegments = LinearReferencingUtilities.buildSegments(LinearReferencing.asLineString(troncon.getGeometry()));

                                final Point point;
                                switch(extremite){
                                    case FIN: 
                                        final LinearReferencing.SegmentInfo lastSegment = tronconSegments[tronconSegments.length-1];
                                        point = GO2Utilities.JTS_FACTORY.createPoint(lastSegment.getPoint(lastSegment.length, 0));
                                        break;
                                    case DEBUT:
                                    default:
                                        point = GO2Utilities.JTS_FACTORY.createPoint(tronconSegments[0].getPoint(0, 0));
                                        break;
                                }
                                prProperty.set(TronconUtils.computePR(tronconSegments, sr, point, Injector.getSession().getRepositoryForClass(BorneDigue.class)));
                                prsByTronconId.get(troncon.getId())[index] = prProperty;
                            }
                            else prProperty = prsByTronconId.get(troncon.getId())[index];
                            
                            return prProperty;
                        }
                    }
                    return null;
                }
            });
        }
    }
}
