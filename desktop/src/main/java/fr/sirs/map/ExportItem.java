/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.map;

import javafx.scene.control.MenuItem;
import org.geotoolkit.gui.javafx.contexttree.TreeMenuItem;

/**
 *
 * @author husky
 */
public class ExportItem extends TreeMenuItem {

    public ExportItem() {
        item = new MenuItem("Exporter");
    }

}
