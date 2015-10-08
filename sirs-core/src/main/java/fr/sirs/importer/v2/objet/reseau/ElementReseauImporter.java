package fr.sirs.importer.v2.objet.reseau;

import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.ElementCreator;
import fr.sirs.core.model.ObjetReseau;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.v2.AbstractImporter;
import fr.sirs.importer.v2.CorruptionLevel;
import fr.sirs.importer.v2.ElementModifier;
import fr.sirs.importer.v2.ErrorReport;
import fr.sirs.importer.v2.MultipleSubTypes;
import fr.sirs.importer.v2.mapper.Mapper;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class ElementReseauImporter extends AbstractImporter<ObjetReseau> implements MultipleSubTypes<ObjetReseau> {

    private HashMap<Class, Collection<Mapper>> additionalMappers;
    private HashMap<Class, Collection<ElementModifier>> additionalModifiers;

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
    public Collection<Class<? extends ObjetReseau>> getSubTypes() {
        return registry.allTypes();
    }

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
    protected ObjetReseau createElement(Row input) {
        final Object type = input.get(Columns.ID_TYPE_ELEMENT_RESEAU.toString());
        if (type == null) {
            context.reportError(new ErrorReport(null, input, getTableName(), Columns.ID_TYPE_ELEMENT_RESEAU.name(), null, null, "No wire type defined", CorruptionLevel.ROW));
        }

        // Find what type of element must be imported.
        Class<? extends ObjetReseau> clazz = registry.getElementType(input);
        if (clazz == null) {
            return null;
        }

        Collection<Mapper> tmpMappers = additionalMappers.get(clazz);
        if (tmpMappers == null) {
            tmpMappers = context.getCompatibleMappers(table, (Class) clazz);
            tmpMappers.removeAll(mappers);
            additionalMappers.put(clazz, tmpMappers);
        }

        Collection<ElementModifier> tmpModifiers = additionalModifiers.get(clazz);
        if (tmpModifiers == null) {
            tmpModifiers = context.getCompatibleModifiers(table, (Class) clazz);
            tmpModifiers.removeAll(modifiers);
            additionalModifiers.put(clazz, tmpModifiers);
        }

        return ElementCreator.createAnonymValidElement(clazz);
    }

    @Override
    public ObjetReseau importRow(Row row, ObjetReseau output) throws IOException, AccessDbImporterException {
        output = super.importRow(row, output);
        for (final Mapper m : additionalMappers.get(output.getClass())) {
            m.map(row, output);
        }
        return output;
    }

    @Override
    protected Element prepareToPost(Object rowId, Row row, ObjetReseau output) {
        final Element e = super.prepareToPost(rowId, row, output);
        final Collection<ElementModifier> tmpModifiers = additionalModifiers.get(e.getClass());
        if (tmpModifiers != null) {
            for (final ElementModifier mod : tmpModifiers) {
                mod.modify(e);
            }
        }
        return e;
    }


    @Override
    protected void postCompute() {
        super.postCompute();
        additionalMappers = null;
        additionalModifiers = null;
    }

    @Override
    protected void preCompute() throws AccessDbImporterException {
        super.preCompute();
        additionalMappers = new HashMap<>();
        additionalModifiers = new HashMap<>();
    }


}
