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
package org.geotoolkit.gui.javafx.chooser;

import java.awt.geom.NoninvertibleTransformException;
import java.io.File;
import java.util.prefs.Preferences;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javax.xml.bind.JAXBException;
import javax.xml.bind.PropertyException;
import org.apache.sis.storage.DataStoreException;
import org.geotoolkit.gui.javafx.render2d.FXMap;
import org.geotoolkit.map.MapContext;
import org.geotoolkit.owc.xml.OwcXmlIO;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class FXContextChooser extends BorderPane {
    
    private FXContextChooser(){
    }
    
    public static MapContext showOpenChooser(final FXMap map) throws JAXBException, FactoryException, DataStoreException, NoninvertibleTransformException, TransformException{
        
        final Window owner = map.getScene().getWindow();
        final FileChooser chooser = new FileChooser();
        chooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("OWC Context", "xml"));        
        
        final String prevPath = getPreviousPath();
        if (prevPath != null) {
            final File f = new File(prevPath).getParentFile();
            if(f.exists() && f.isDirectory()){
                chooser.setInitialDirectory(f);
            }
        }
        
        final File file = chooser.showOpenDialog(owner);
        if(file!=null){
            setPreviousPath(file.getAbsolutePath());
            final MapContext context = OwcXmlIO.read(file);
            map.getCanvas().setVisibleArea(context.getAreaOfInterest());
            return context;
        }
        
        return null;
    }
    
    public static File showSaveChooser(FXMap map) throws JAXBException, PropertyException, FactoryException, TransformException{
        
        final MapContext context = map.getContainer().getContext();
        final FileChooser chooser = new FileChooser();
        chooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("OWC Context", "xml"));        
        
        final String prevPath = getPreviousPath();
        if (prevPath != null) {
            final File f = new File(prevPath).getParentFile();
            if(f.exists() && f.isDirectory()){
                chooser.setInitialDirectory(f);
            }
        }
        
        final File file = chooser.showSaveDialog(null);
        
        if(file!=null){
            context.setAreaOfInterest(map.getCanvas().getVisibleEnvelope2D());
            setPreviousPath(file.getAbsolutePath());
            OwcXmlIO.write(file,context);
        }
        
        return file;
    }
    
    public static String getPreviousPath() {
        final Preferences prefs = Preferences.userNodeForPackage(FXContextChooser.class);
        return prefs.get("owc_xml_path", null);
    }

    public static void setPreviousPath(final String path) {
        final Preferences prefs = Preferences.userNodeForPackage(FXContextChooser.class);
        prefs.put("owc_xml_path", path);
    }
    
}
