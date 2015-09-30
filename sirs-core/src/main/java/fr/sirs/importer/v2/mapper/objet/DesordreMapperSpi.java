package fr.sirs.importer.v2.mapper.objet;

import fr.sirs.core.model.Desordre;
import fr.sirs.importer.v2.mapper.GenericMapperSpi;
import java.beans.IntrospectionException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class DesordreMapperSpi extends GenericMapperSpi<Desordre> {

    private enum Columns {

        //            id_nom_element,// Aucun intéret
        //            ID_SOUS_GROUPE_DONNEES,// Aucun intéret
        //            LIBELLE_SOUS_GROUPE_DONNEES,// Aucun intéret
        ID_TYPE_DESORDRE,
        //            LIBELLE_TYPE_DESORDRE,// Dans TypeDesordreImporter
        //            DECALAGE_DEFAUT, // Info d'affichage
        //            DECALAGE, // Info d'affichage
        //            LIBELLE_SOURCE, // Dans TypeSourceImporter
        //            LIBELLE_TYPE_COTE, // Dans TypeCoteImporter
        //            LIBELLE_SYSTEME_REP, // Dans SystemeRepImporter
        //            NOM_BORNE_DEBUT, //Dans BorneImporter
        //            NOM_BORNE_FIN, //Dans BorneImporter
        //            DISPARU_OUI_NON,
        //            DEJA_OBSERVE_OUI_NON,
        //            LIBELLE_TYPE_POSITION,// Dans typePositionImporter
        ID_TYPE_COTE,
        ID_TYPE_POSITION,
        ID_SOURCE,
        LIEU_DIT_DESORDRE
        //            ID_AUTO

        //Empty fields
        //     ID_PRESTATION, // obsolète ? voir table DESORDRE_PRESTATION
        //     LIBELLE_PRESTATION, // Dans l'importateur de prestations
    }

    private final HashMap<String, String> bindings = new HashMap<>();

    public DesordreMapperSpi() throws IntrospectionException {
        super(Desordre.class);

        bindings.put(Columns.ID_TYPE_DESORDRE.name(), "typeDesordreId");
        bindings.put(Columns.ID_TYPE_COTE.name(), "coteId");
        bindings.put(Columns.ID_TYPE_POSITION.name(), "positionId");
        bindings.put(Columns.ID_SOURCE.name(), "sourceId");
        bindings.put(Columns.LIEU_DIT_DESORDRE.name(), "lieuDit");
    }

    @Override
    public Map<String, String> getBindings() {
        return bindings;
    }
}
