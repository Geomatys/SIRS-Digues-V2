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
package fr.sirs.plugin.lit.map;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.HBox;
import org.controlsfx.control.action.ActionUtils;
import org.geotoolkit.gui.javafx.render2d.FXMap;

/**
 *
 * @author guilhem
 */
public class LitToolBar extends ToolBar {

    private static final String LEFT = "buttongroup-left";
    private static final String CENTER = "buttongroup-center";
    private static final String RIGHT = "buttongroup-right";

    public LitToolBar(FXMap map) {
        getStylesheets().add("/org/geotoolkit/gui/javafx/buttonbar.css");

        getItems().add(new Label("Lits"));
        
        final ToggleButton butEditLit = new LitEditAction(map).createToggleButton(ActionUtils.ActionTextBehavior.HIDE);
        butEditLit.getStyleClass().add(LEFT);
        final ToggleButton butCut = new LitCutAction(map).createToggleButton(ActionUtils.ActionTextBehavior.HIDE);
        butCut.getStyleClass().add(CENTER);
        final ToggleButton butMerge = new LitMergeAction(map).createToggleButton(ActionUtils.ActionTextBehavior.HIDE);
        butMerge.getStyleClass().add(CENTER);
        final ToggleButton butEditSr = new LitBorneEditAction(map).createToggleButton(ActionUtils.ActionTextBehavior.HIDE);
        butEditSr.getStyleClass().add(CENTER);
        final ToggleButton butCalc = new LitPointCalculatorAction(map).createToggleButton(ActionUtils.ActionTextBehavior.HIDE);
        butCalc.getStyleClass().add(CENTER);
        final ToggleButton butEditConvert = new ConvertGeomToLitAction(map).createToggleButton(ActionUtils.ActionTextBehavior.HIDE);
        butEditConvert.getStyleClass().add(CENTER);
        final Button importBornes = new FXImportBornesLitAction(map).createButton(ActionUtils.ActionTextBehavior.HIDE);
        importBornes.getStyleClass().add(RIGHT);
        
        getItems().add(new HBox(butEditLit, butCut, butMerge, butEditSr, butCalc, butEditConvert, importBornes));

    }
    
}
