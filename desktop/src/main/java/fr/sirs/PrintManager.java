package fr.sirs;

import static fr.sirs.SIRS.BORNE_IDS_REFERENCE;
import static fr.sirs.SIRS.COUCH_DB_DOCUMENT_FIELD;
import static fr.sirs.SIRS.DOCUMENT_ID_FIELD;
import static fr.sirs.SIRS.FOREIGN_PARENT_ID_FIELD;
import static fr.sirs.SIRS.GEOMETRY_FIELD;
import static fr.sirs.SIRS.ID_FIELD;
import static fr.sirs.SIRS.LATITUDE_MAX_FIELD;
import static fr.sirs.SIRS.LATITUDE_MIN_FIELD;
import static fr.sirs.SIRS.LONGITUDE_MAX_FIELD;
import static fr.sirs.SIRS.LONGITUDE_MIN_FIELD;
import static fr.sirs.SIRS.PARENT_FIELD;
import static fr.sirs.SIRS.POSITION_DEBUT_FIELD;
import static fr.sirs.SIRS.POSITION_FIN_FIELD;
import static fr.sirs.SIRS.REVISION_FIELD;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.util.PrinterUtilities;
import fr.sirs.util.SirsStringConverter;
import java.awt.Desktop;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.data.FeatureStoreUtilities;
import org.geotoolkit.feature.Feature;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class PrintManager {
    
    private List<Element> elementsToPrint = null;
    private FeatureCollection featuresToPrint = null;
    
    public List<? extends Element> getElementsToPrint(){return elementsToPrint;}
    public FeatureCollection getFeaturesToPrint(){return featuresToPrint;}

    public void prepareToPrint(final Element object){
        featuresToPrint = null;
        elementsToPrint = new ArrayList<>();
        elementsToPrint.add(object);
    }

    public void prepareToPrint(final List<Element> objects){
        featuresToPrint = null;
        elementsToPrint = objects;
    }
    
    public void prepareToPrint(final Feature feature){
        elementsToPrint = null;
        featuresToPrint = FeatureStoreUtilities.collection(feature);
    }
    
    public void prepareToPrint(final FeatureCollection featureCollection){
        elementsToPrint = null;
        featuresToPrint = featureCollection;
    }
    
    public final void printFeatures(){
        try {
            final File fileToPrint = PrinterUtilities.print(null, featuresToPrint);
            Desktop.getDesktop().open(fileToPrint);
        } catch (Exception ex) {
            Logger.getLogger(FXMainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public final void printElements(){
        final List avoidFields = new ArrayList<>();
        final File fileToPrint;
        avoidFields.add(GEOMETRY_FIELD);
        avoidFields.add(DOCUMENT_ID_FIELD);
        avoidFields.add(ID_FIELD);
        avoidFields.add(LONGITUDE_MIN_FIELD);
        avoidFields.add(LONGITUDE_MAX_FIELD);
        avoidFields.add(LATITUDE_MIN_FIELD);
        avoidFields.add(LATITUDE_MAX_FIELD);
        avoidFields.add(FOREIGN_PARENT_ID_FIELD);
        avoidFields.add(REVISION_FIELD);
        avoidFields.add(POSITION_DEBUT_FIELD);
        avoidFields.add(POSITION_FIN_FIELD);
        avoidFields.add(PARENT_FIELD);
        avoidFields.add(COUCH_DB_DOCUMENT_FIELD);
        
        for(final Element element : elementsToPrint){
            if(element instanceof TronconDigue){
                if(!avoidFields.contains(BORNE_IDS_REFERENCE)) avoidFields.add(BORNE_IDS_REFERENCE);
            }
        }

        try {
            fileToPrint = PrinterUtilities.print(avoidFields, Injector.getSession().getPreviews(), new SirsStringConverter(), elementsToPrint);
            Desktop.getDesktop().open(fileToPrint);
        } catch (Exception e) {
            Logger.getLogger(FXMainFrame.class.getName()).log(Level.SEVERE, null, e);
        }
    }
    
}
