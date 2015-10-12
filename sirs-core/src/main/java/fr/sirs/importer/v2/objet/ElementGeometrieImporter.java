package fr.sirs.importer.v2.objet;

import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.ElementCreator;
import fr.sirs.core.model.LargeurFrancBord;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.core.model.ObjetPhotographiable;
import fr.sirs.importer.v2.AbstractImporter;
import fr.sirs.importer.v2.MultipleSubTypes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
@Component
public class ElementGeometrieImporter extends AbstractImporter<ObjetPhotographiable> implements MultipleSubTypes<ObjetPhotographiable> {

    @Override
    public String getRowIdFieldName() {
        return Columns.ID_ELEMENT_GEOMETRIE.name();
    }

    @Override
    public Class<ObjetPhotographiable> getElementClass() {
        return ObjetPhotographiable.class;
    }

    @Override
    protected ObjetPhotographiable createElement(Row input) {
        final Class<? extends ObjetPhotographiable> classe;
                // La colonne NOM_TABLE_EVT étant vide dans la table de l'Isère,
        // on gère la correspondance en dur en espérant que toutes les
        //bases font le même lien !
//                final DbImporter.TableName table = valueOf(row.getString(TypeElementGeometryColumns.NOM_TABLE_EVT.toString()));
        final int table = (int) input.getInt(Columns.ID_TYPE_ELEMENT_GEOMETRIE.name());
        switch (table) {
            case 1:
                classe = LargeurFrancBord.class;
                break;
//                    case 2:
//                        classe = ProfilFrontFrancBord.class;
//                        break;
//                    case 3:
//                        classe = Distance.class;
//                        break;
            default:
                classe = null;
        }

        if (classe != null) {
            return ElementCreator.createAnonymValidElement(classe);
        } else {
            return null;
        }
    }

    @Override
    public Collection<Class<? extends ObjetPhotographiable>> getSubTypes() {
        return (Collection) Collections.singleton( LargeurFrancBord.class);
    }

    private enum Columns {
        ID_ELEMENT_GEOMETRIE,
        ID_TYPE_ELEMENT_GEOMETRIE,
//        ID_SOURCE,
//        DATE_DEBUT_VAL,
//        DATE_FIN_VAL,
//        PR_DEBUT_CALCULE,
//        PR_FIN_CALCULE,
//        X_DEBUT,
//        Y_DEBUT,
//        X_FIN,
//        Y_FIN,
//        ID_SYSTEME_REP,
//        ID_BORNEREF_DEBUT,
//        AMONT_AVAL_DEBUT,
//        DIST_BORNEREF_DEBUT,
//        ID_BORNEREF_FIN,
//        AMONT_AVAL_FIN,
//        DIST_BORNEREF_FIN,
//        COMMENTAIRE,
//        ID_TYPE_LARGEUR_FB,
//        ID_TYPE_PROFIL_FB,
//        ID_TYPE_DIST_DIGUE_BERGE,
    };

    @Override
    protected List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (Columns c : Columns.values()) {
            columns.add(c.toString());
        }
        return columns;
    }

    @Override
    public String getTableName() {
        return ELEMENT_GEOMETRIE.toString();
    }
}
