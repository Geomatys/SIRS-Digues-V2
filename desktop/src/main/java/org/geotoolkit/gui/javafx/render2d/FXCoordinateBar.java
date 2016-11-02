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
package org.geotoolkit.gui.javafx.render2d;

import fr.sirs.core.SirsCore;
import fr.sirs.util.DatePickerConverter;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Level;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import javafx.util.converter.LongStringConverter;
import org.apache.sis.geometry.DirectPosition2D;
import org.controlsfx.control.StatusBar;
import org.geotoolkit.display.canvas.AbstractCanvas2D;
import org.geotoolkit.display2d.canvas.painter.SolidColorPainter;
import org.geotoolkit.gui.javafx.crs.FXCRSButton;
import org.geotoolkit.gui.javafx.util.FXUtilities;
import org.geotoolkit.internal.GeotkFX;
import org.geotoolkit.internal.Loggers;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class FXCoordinateBar extends GridPane {

    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance();

    private final FXMap map;
    private final PropertyChangeListener listener = new PropertyChangeListener() {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            final String propertyName = evt.getPropertyName();

            if(AbstractCanvas2D.OBJECTIVE_CRS_KEY.equals(propertyName)){
                //update crs button
                crsButton.crsProperty().set((CoordinateReferenceSystem)evt.getNewValue());
            }

            //update scale box
            Platform.runLater(() -> {
                scaleCombo.valueProperty().removeListener(action);
                try {
                    final double scale = map.getCanvas().getGeographicScale();
                    scaleCombo.setValue((long)scale);
                    scaleCombo.valueProperty().addListener(action);
                } catch (TransformException ex) {
                    Loggers.JAVAFX.log(Level.WARNING, null, ex);
                }
            });

        }
    };
    private final ChangeListener action = new ChangeListener() {

        @Override
        public void changed(ObservableValue observable, Object oldValue, Object newValue) {
            if (map != null) {
                try {
                    map.getCanvas().setGeographicScale((Long)newValue);
                } catch (TransformException ex) {
                    Loggers.JAVAFX.log(Level.WARNING, null, ex);
                }
            }
        }
    };


    private final StatusBar statusBar = new StatusBar();
    private final ComboBox scaleCombo = new ComboBox();
    private final ColorPicker colorPicker = new ColorPicker(Color.WHITE);
    private final FXCRSButton crsButton = new FXCRSButton();
    private final DatePicker dateField = new DatePicker();

    public FXCoordinateBar(FXMap map) {
        this.map = map;

        colorPicker.setStyle("-fx-color-label-visible:false;");

        statusBar.setMaxWidth(Double.MAX_VALUE);
        add(statusBar, 0, 1);

        final ColumnConstraints col0 = new ColumnConstraints();
        col0.setHgrow(Priority.ALWAYS);
        final RowConstraints row0 = new RowConstraints();
        row0.setVgrow(Priority.ALWAYS);
        final RowConstraints row1 = new RowConstraints();
        row1.setVgrow(Priority.NEVER);
        getColumnConstraints().addAll(col0);
        getRowConstraints().addAll(row0,row1);

        final ChangeListener<LocalDate> rangeListener = (obs, oldValue, newValue) -> {
            try {
                if(newValue==null){
                    map.getCanvas().setTemporalRange(null,null);
                }else{
                    final ZonedDateTime atStartOfDay = newValue.atStartOfDay(SirsCore.PARIS_ZONE_ID);
                    final ZonedDateTime atEndOfDay = atStartOfDay.plusDays(1).minusNanos(1);
                    map.getCanvas().setTemporalRange(
                            Date.from(atStartOfDay.toInstant()),
                            Date.from(atEndOfDay.toInstant())
                    );
                }
            } catch (TransformException ex) {
                Loggers.JAVAFX.log(Level.INFO, ex.getMessage(), ex);
            }
        };

        dateField.valueProperty().addListener(rangeListener);
        DatePickerConverter.register(dateField);
        
        statusBar.getLeftItems().add(dateField);

        scaleCombo.getItems().addAll(  1000l,
                                 5000l,
                                20000l,
                                50000l,
                               100000l,
                               500000l);
        scaleCombo.setEditable(true);
        scaleCombo.setConverter(new LongStringConverter());

        statusBar.getRightItems().add(scaleCombo);
        statusBar.getRightItems().add(colorPicker);
        statusBar.getRightItems().add(crsButton);

        map.addEventHandler(MouseEvent.ANY, new myListener());

        if (this.map != null) {
            this.map.getCanvas().addPropertyChangeListener(listener);
        }

        colorPicker.setOnAction(new EventHandler() {
            public void handle(Event t) {
                if (map != null) {
                    map.getCanvas().setBackgroundPainter(new SolidColorPainter(FXUtilities.toSwingColor(colorPicker.getValue())));
                    map.getCanvas().repaint();
                }
            }
        });

        crsButton.crsProperty().setValue(map.getCanvas().getObjectiveCRS());
        crsButton.crsProperty().addListener((ObservableValue<? extends CoordinateReferenceSystem> observable,
                CoordinateReferenceSystem oldValue, CoordinateReferenceSystem newValue) -> {
            try {
                if(newValue!=null){
                    map.getCanvas().setObjectiveCRS(newValue);
                }
            } catch (TransformException ex) {
                Loggers.JAVAFX.log(Level.INFO, ex.getMessage(), ex);
            }
        });

        // Set button tooltips
        statusBar.setTooltip(new Tooltip(GeotkFX.getString(FXCoordinateBar.class, "coordinateTooltip")));
        scaleCombo.setTooltip(new Tooltip(GeotkFX.getString(FXCoordinateBar.class, "scaleTooltip")));
        colorPicker.setTooltip(new Tooltip(GeotkFX.getString(FXCoordinateBar.class, "bgColorTooltip")));
        crsButton.setTooltip(new Tooltip(GeotkFX.getString(FXCoordinateBar.class, "crsTooltip")));
    }

    public void setCrsButtonVisible(boolean visible){
        if(statusBar.getRightItems().contains(crsButton)){
            statusBar.getRightItems().remove(crsButton);
        }else{
            statusBar.getRightItems().add(crsButton);
        }
    }

    public boolean isCrsButtonVisible(){
        return crsButton.isVisible();
    }

    /**
     * TODO change this, we should be able to control multiple crs axis at the same time.
     * @return temporal axis crs viewer
     */
    public DatePicker getDateField() {
        return dateField;
    }

    /**
     * Set scale values displayed in the right corner combo box.
     *
     * @param scales
     */
    public void setScaleBoxValues(Long[] scales){
        scaleCombo.getItems().setAll(Arrays.asList(scales));
    }

    private class myListener implements EventHandler<MouseEvent>{

        @Override
        public void handle(MouseEvent event) {

            final Point2D pt = new Point2D.Double(event.getX(), event.getY());
            Point2D coord = new DirectPosition2D();
            try {
                coord = map.getCanvas().getObjectiveToDisplay().inverseTransform(pt, coord);
            } catch (NoninvertibleTransformException ex) {
                statusBar.setText("");
                return;
            }

            final CoordinateReferenceSystem crs = map.getCanvas().getObjectiveCRS();

            final StringBuilder sb = new StringBuilder("  ");
            sb.append(crs.getCoordinateSystem().getAxis(0).getAbbreviation());
            sb.append(" : ");
            sb.append(NUMBER_FORMAT.format(coord.getX()));
            sb.append("   ");
            sb.append(crs.getCoordinateSystem().getAxis(1).getAbbreviation());
            sb.append(" : ");
            sb.append(NUMBER_FORMAT.format(coord.getY()));
            statusBar.setText(sb.toString());
        }

    }

}
