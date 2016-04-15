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
package fr.sirs.theme;

import fr.sirs.theme.ui.FXTronconThemePane;

import javafx.scene.Parent;

/**
 *
 * @author Johann Sorel (Geomatys)
 * @author Cédric Briançon (Geomatys)
 */
public class TronconTheme extends AbstractTheme {

    
    public TronconTheme(String name, Class... classes) {
        super(name, classes);
    }
    
    protected TronconTheme(String name, ThemeManager... managers) {
        super(name, managers);
    }
    
    @Override
    public Parent createPane(){
        return new FXTronconThemePane(managers);
    }

    @Override
    public void initThemeManager(final ThemeManager... managers) {
        if(managers.length>1){
            for(ThemeManager manager : managers){
                final Theme subtheme = new TronconTheme(manager.getName(), manager);
                getSubThemes().add(subtheme);
            }
        }
    }
}
