

package fr.sirs.core.component;


import fr.sirs.core.InjectorCore;
import fr.sirs.core.SessionCore;

import fr.sirs.core.SirsCore;
import fr.sirs.core.model.HorodatageReference;
import fr.sirs.util.ConvertPositionableCoordinates;
import org.ektorp.CouchDbConnector;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;
import fr.sirs.core.model.Prestation;

import java.util.logging.Level;

/**
 * Outil g�rant les �changes avec la bdd CouchDB pour tous les objets Prestation.
 *
 * @author Olivier Nouguier (Geomatys)
 * @author Alexis Manin     (Geomatys)
 * @author Estelle Idée     (Geomatys)
 */

@Component("fr.sirs.core.component.PrestationRepository")
public class PrestationRepository extends AbstractPositionableRepository<Prestation> {

    @Autowired
    private PrestationRepository ( CouchDbConnector db) {
       super(Prestation.class, db);
       initStandardDesignDocument();
   }

    @Override
    public Prestation create() {
        final SessionCore bean = InjectorCore.getBean(SessionCore.class);
        final Prestation prestation = bean.getElementCreator().createElement(Prestation.class);
        prestation.setHorodatageStatusId(HorodatageReference.getRefNonTimeStampedStatus());

        return prestation;
    }

    @Override
    public Prestation onLoad(Prestation prestation) {
        prestation = super.onLoad(prestation);
        boolean toSave = false;
        try {
            toSave = ConvertPositionableCoordinates.COMPUTE_MISSING_COORD.test(prestation);
        } catch (ClassCastException cce) {
            SirsCore.LOGGER.log(Level.WARNING, "Echec du calcul de coordonnées pour l'élément chargé : \n" + prestation.toString(), cce);
        }

        if (prestation.getGeometry() == null) {
            ConvertPositionableCoordinates.updateGeometryAndPRs(prestation);
        }

        if (prestation.getHorodatageStatusId() == null) {
            toSave = true;
            prestation.setHorodatageStatusId(HorodatageReference.getRefNonTimeStampedStatus());
        }

        if (toSave) {
            update(prestation);
        }

        return prestation;
    }
}

