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
package fr.sirs.digue;

import fr.sirs.map.FXMapTab;
import fr.sirs.util.FXFreeTab;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class DiguesTab extends FXFreeTab {
    
    private final static String TITLE = "Systèmes d'endiguement";
    
    private final TabPane tabs;
    private final FXDiguesPane ctrl;

    public DiguesTab(TabPane tabs) {
        this.tabs = tabs;
        this.ctrl = new FXDiguesPane();
        setText(TITLE);
        setContent(ctrl);
    }    
    
    public synchronized void show(){
        int index = 0;
        if(!tabs.getTabs().contains(this)){
            //on place l'onglet toujours apres la carte si possible
            for(Tab t : tabs.getTabs()){
                if(t instanceof FXMapTab){
                    index = tabs.getTabs().indexOf(t)+1;
                }
            }
            tabs.getTabs().add(index,this);
        }
        tabs.getSelectionModel().clearAndSelect(tabs.getTabs().indexOf(this));
    }
    
    public synchronized FXDiguesPane getDiguesController(){return this.ctrl;}
}
