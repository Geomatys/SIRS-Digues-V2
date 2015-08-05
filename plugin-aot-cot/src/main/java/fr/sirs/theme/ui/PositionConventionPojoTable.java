package fr.sirs.theme.ui;

import fr.sirs.Injector;
import fr.sirs.core.model.PositionConvention;
import fr.sirs.core.model.TronconDigue;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.Property;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 *
 * @author Samuel Andrés (Geomaty)
 */
public class PositionConventionPojoTable extends ListenPropertyPojoTable<String>{

    public PositionConventionPojoTable(String title) {
        super(PositionConvention.class, title);
    }
    
    
    
//    @Override
//    protected PositionConvention createPojo() {
//        final Alert choice = new Alert(Alert.AlertType.NONE, "Le positionnement de convention est associable à un objet.\nL'association à un objet est facultative et peut être réalisée ultérieurement.\nVoulez-vous associer un objet maintenant ?\n", ButtonType.YES, ButtonType.NO);
//        choice.setResizable(true);
//        final Optional<ButtonType> result = choice.showAndWait();
//        
//        final PositionConvention position;
//        if(result.isPresent()&&result.get()==ButtonType.OK){
//            final TronconDigue premierTroncon = Injector.getSession().getRepositoryForClass(TronconDigue.class).getOne();
//            position = (PositionConvention) PositionDocumentPojoTable.this.createPojo(premierTroncon);
//
//            try {
//                ((Property<String>) propertyMethodToListen.invoke(position)).setValue(propertyReference);
//            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
//                Logger.getLogger(PositionDocumentPojoTable.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        } else {
//            position = super.createPojo();
//        }
//        
//        
//        return position;
//    }
    
    
}
