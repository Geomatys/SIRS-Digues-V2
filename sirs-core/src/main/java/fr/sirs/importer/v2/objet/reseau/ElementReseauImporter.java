package fr.sirs.importer.v2.objet.reseau;

import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.ElementCreator;
import fr.sirs.core.model.ObjetReseau;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.v2.AbstractImporter;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class ElementReseauImporter extends AbstractImporter<ObjetReseau> {

    private enum Columns {
        ID_ELEMENT_RESEAU,
        //        id_nom_element,
        //        ID_SOUS_GROUPE_DONNEES,
        //        LIBELLE_TYPE_ELEMENT_RESEAU,
        //        DECALAGE_DEFAUT,
        //        DECALAGE,
        //        LIBELLE_SOURCE,
        //        LIBELLE_TYPE_COTE,
        //        LIBELLE_SYSTEME_REP,
        //        NOM_BORNE_DEBUT,
        //        NOM_BORNE_FIN,
        //        LIBELLE_ECOULEMENT,
        //        LIBELLE_IMPLANTATION,
        //        LIBELLE_UTILISATION_CONDUITE,
        //        LIBELLE_TYPE_CONDUITE_FERMEE,
        //        LIBELLE_TYPE_OUVR_HYDRAU_ASSOCIE,
        //        LIBELLE_TYPE_RESEAU_COMMUNICATION,
        //        LIBELLE_TYPE_VOIE_SUR_DIGUE,
        //        NOM_OUVRAGE_VOIRIE,
        //        LIBELLE_TYPE_POSITION,
        //        LIBELLE_TYPE_OUVRAGE_VOIRIE,
        //        LIBELLE_TYPE_RESEAU_EAU,
        //        LIBELLE_TYPE_REVETEMENT,
        //        LIBELLE_TYPE_USAGE_VOIE,
        NOM,
                ID_TYPE_ELEMENT_RESEAU,
        ID_TYPE_COTE,
        ID_SOURCE,
        //        N_SECTEUR,
        //        ID_ECOULEMENT,
        //        ID_IMPLANTATION,
        //        ID_UTILISATION_CONDUITE,
        //        ID_TYPE_CONDUITE_FERMEE,
        //        AUTORISE,
        ID_TYPE_OUVR_HYDRAU_ASSOCIE,
        //        ID_TYPE_RESEAU_COMMUNICATION,
        //        ID_OUVRAGE_COMM_NRJ,
        //        ID_TYPE_VOIE_SUR_DIGUE,
        //        ID_OUVRAGE_VOIRIE,
        //        ID_TYPE_REVETEMENT,
        //        ID_TYPE_USAGE_VOIE,
        ID_TYPE_POSITION,
//        LARGEUR,
//        ID_TYPE_OUVRAGE_VOIRIE,
//        HAUTEUR,
//        DIAMETRE,
//        ID_TYPE_RESEAU_EAU,
//        ID_TYPE_NATURE,
//        LIBELLE_TYPE_NATURE,
//        ID_TYPE_NATURE_HAUT,
//        LIBELLE_TYPE_NATURE_HAUT,
//        ID_TYPE_NATURE_BAS,
//        LIBELLE_TYPE_NATURE_BAS,
//        ID_TYPE_REVETEMENT_HAUT,
//        LIBELLE_TYPE_REVETEMENT_HAUT,
//        ID_TYPE_REVETEMENT_BAS,
//        LIBELLE_TYPE_REVETEMENT_BAS,
//        ID_AUTO
    }

    @Autowired
    private ReseauRegistry registry;

    @Override
    protected Class<ObjetReseau> getElementClass() {
        return ObjetReseau.class;
    }

    @Override
    public String getTableName() {
        return DbImporter.TableName.ELEMENT_RESEAU.name();
    }

    @Override
    public String getRowIdFieldName() {
        return Columns.ID_ELEMENT_RESEAU.name();
    }

    @Override
    protected ObjetReseau getOrCreateElement(Row input) {
        Integer type = input.getInt(Columns.ID_TYPE_ELEMENT_RESEAU.toString());
        if (type == null) {
            throw new IllegalArgumentException("No valid element type in row "+input.getInt(getRowIdFieldName())+ " of table "+getTableName());
        }

        // Find what type of element must be imported.
        Class<ObjetReseau> clazz = registry.getElementType(type);
        if (clazz == null) {
            throw new IllegalArgumentException("No valid element type in row "+input.getInt(getRowIdFieldName())+ " of table "+getTableName());
        }

        return ElementCreator.createAnonymValidElement(clazz);
    }
}
