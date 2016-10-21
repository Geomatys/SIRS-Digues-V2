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
import fr.sirs.core.model.Positionable;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.theme.ui.PojoTable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
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
            createNewProperty.set(false);
            fichableProperty.set(false);
            uiAdd.setVisible(false);
            uiFicheMode.setVisible(false);
            uiDelete.setVisible(false);
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
            createNewProperty.set(false);
            fichableProperty.set(false);
            uiAdd.setVisible(false);
            uiFicheMode.setVisible(false);
            uiDelete.setVisible(false);
        }
    }

    /**
     * Check that given object is located in one of the specified linears.
     * @param <T>
     */
    final protected class LinearPredicate<T extends AvecForeignParent> implements Predicate<T> {

        final Set<String> acceptedIds;

        public LinearPredicate() {
            acceptedIds = tronconsTable.getSelectedItems().stream().map(input -> input.getId()).collect(Collectors.toSet());
        }

        @Override
        public boolean test(T t) {
            return acceptedIds.isEmpty() || (t.getForeignParentId() != null && acceptedIds.contains(t.getForeignParentId()));
        }
    }

    /**
     * Check that given object PRs are found in selected PRs in its parent linear.
     * @param <T> Type of object to test.
     */
    final protected class PRPredicate<T extends Positionable & AvecForeignParent> implements Predicate<T> {

        @Override
        public boolean test(final T candidate) {
            final String linearId = candidate.getForeignParentId();
            final ObjectProperty<Number>[] userPRs;
            if (linearId == null || (userPRs = prsByTronconId.get(linearId)) == null)
                return false;

            final float startPR = userPRs[0].get() == null ? Float.NaN : userPRs[0].get().floatValue();
            final float endPR = userPRs[1].get() == null ? Float.NaN : userPRs[1].get().floatValue();

            if (!Float.isNaN(startPR) && candidate.getPrFin() < startPR)
                return false;

            return Float.isNaN(endPR) || candidate.getPrDebut() <= endPR;
        }
    }
}
