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
package fr.sirs;

import com.vividsolutions.jts.geom.Point;
import fr.sirs.core.LinearReferencingUtilities;
import fr.sirs.core.TronconUtils;
import fr.sirs.core.model.AvecForeignParent;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.Positionable;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.theme.ui.PojoTable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;
import org.geotoolkit.display2d.GO2Utilities;
import org.geotoolkit.gui.javafx.util.FXNumberCell;
import org.geotoolkit.referencing.LinearReferencing;

/**
 * Classe abstraite de factorisation des fonctionnalités communes aux panneaux
 * d'impression permettant de choisir des tronçons et des PRs pour restreindre
 * les objets à inclure dans le document à imprimer.
 *
 * @author Samuel Andrés (Geomatys)
 */
public abstract class TronconChoicePrintPane extends BorderPane {

    @FXML protected Tab uiTronconChoice;
    protected final Map<String, ObjectProperty<Number>[]> prsByTronconId = new HashMap<>();
    protected final TronconChoicePojoTable tronconsTable = new TronconChoicePojoTable();

    public TronconChoicePrintPane(final Class forBundle) {
        SIRS.loadFXML(this, forBundle);
        tronconsTable.setTableItems(()-> (ObservableList) SIRS.observableList(Injector.getSession().getRepositoryForClass(TronconDigue.class).getAll()));
        tronconsTable.commentAndPhotoProperty().set(false);
        uiTronconChoice.setContent(tronconsTable);
    }

    protected class TronconChoicePojoTable extends PojoTable {

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
                public ObservableValue<Number> call(TableColumn.CellDataFeatures<TronconDigue, Number> param) {

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
                            else
                                prProperty = prsByTronconId.get(troncon.getId())[index];

                            return prProperty;
                        }
                    }
                    return null;
                }
            });
        }
    }

    protected class TypeChoicePojoTable extends PojoTable {

        public TypeChoicePojoTable(final Class clazz, final String title) {
            super(clazz, title);
            getColumns().remove(editCol);
            editableProperty.set(false);
        }
    }


    final protected class LocationPredicate<T extends Positionable & AvecForeignParent> implements Predicate<T>{

        final List<String> tronconSelectedIds = new ArrayList<>();

        public LocationPredicate(){
            for(final Element element : tronconsTable.getSelectedItems()){
                tronconSelectedIds.add(element.getId());
            }
        }

        @Override
        public boolean test(final T candidate) {
            if (tronconSelectedIds.isEmpty() || candidate.getForeignParentId()==null) {
                return false;
            }

            final String linearId = candidate.getForeignParentId();

            /*
            Sous-condition de retrait 1 : si le désordre est
            associé à un tronçon qui n'est pas sélectionné dans
            la liste.
            */
            final boolean linearNotSelected = !tronconSelectedIds.contains(linearId);
            if (linearNotSelected) {
                // On ne le garde pas
                return true;
            }

            /*
            Sous-condition de retrait 2 : si le désordre a des PRs de
            début et de fin et si le tronçon a des PRs de début et
            de fin (i.e. s'il a un SR par défaut qui a permi de les
            calculer), alors on vérifie :
            */
            if(!Float.isNaN(candidate.getPrDebut()) && !Float.isNaN(candidate.getPrFin())
                    && prsByTronconId.get(linearId)!=null
                    && prsByTronconId.get(linearId)[0]!=null
                    && prsByTronconId.get(linearId)[1]!=null
                    && prsByTronconId.get(linearId)[0].get()!=null
                    && prsByTronconId.get(linearId)[1].get()!=null)
            {
                final float prInf, prSup;
                if(candidate.getPrDebut() < candidate.getPrFin()) {
                    prInf=candidate.getPrDebut();
                    prSup=candidate.getPrFin();
                } else {
                    prInf=candidate.getPrFin();
                    prSup=candidate.getPrDebut();
                }
                return (prInf < prsByTronconId.get(linearId)[0].get().floatValue()) // Si le désordre s'achève avant le début de la zone du tronçon que l'on souhaite.
                || (prSup > prsByTronconId.get(linearId)[1].get().floatValue()); // Si le désordre débute après la fin de la zone du tronçon que l'on souhaite.
            } else {
                // On le garde s'il manque des informations
                return false;
            }
        }

    }

}
