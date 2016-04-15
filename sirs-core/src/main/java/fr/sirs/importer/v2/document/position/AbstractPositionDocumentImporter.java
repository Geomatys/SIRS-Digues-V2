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
package fr.sirs.importer.v2.document.position;

import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.AbstractPositionDocument;
import fr.sirs.core.model.ElementCreator;
import fr.sirs.core.model.PositionDocument;
import fr.sirs.core.model.PositionProfilTravers;
import fr.sirs.core.model.ProfilLong;
import fr.sirs.core.model.ProfilTravers;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.v2.AbstractImporter;
import fr.sirs.importer.v2.MultipleSubTypes;
import fr.sirs.importer.v2.document.DocTypeRegistry;
import java.util.ArrayList;
import java.util.Collection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Import all types of positioned documents referenced in DOCUMENT table. This
 * importer is a bit special, because each row of the table can contain information
 * about a different implementation of {@link AbstractPositionDocument}.
 *
 * @author Alexis Manin (Geomatys)
 * @author Samuel Andres (Geomatys)
 */
@Component
public class AbstractPositionDocumentImporter extends AbstractImporter<AbstractPositionDocument> implements MultipleSubTypes<AbstractPositionDocument> {

    @Autowired
    protected DocTypeRegistry docTypes;

    @Override
    public Collection<Class<? extends AbstractPositionDocument>> getSubTypes() {
        final ArrayList<Class<? extends AbstractPositionDocument>> list = new ArrayList<>();
        list.add(PositionDocument.class);
        list.add(PositionProfilTravers.class);

        return list;
    }

    private enum Columns {
        ID_DOC,
        ID_TRONCON_GESTION,
        ID_TYPE_DOCUMENT,
////        ID_DOSSIER, // Pas dans le nouveau modèle
////        REFERENCE_PAPIER, // Pas dans le nouveau modèle
////        REFERENCE_NUMERIQUE, // Pas dans le nouveau modèle
////        REFERENCE_CALQUE, // Pas dans le nouveau modèle
////        DATE_DOCUMENT,
////        DATE_DEBUT_VAL, // Pas dans le nouveau modèle
////        DATE_FIN_VAL, // Pas dans le nouveau modèle
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
////        NOM,
////        ID_MARCHE,
////        ID_INTERV_CREATEUR,
////        ID_ORG_CREATEUR,
//        ID_ARTICLE_JOURNAL,
//        ID_PROFIL_EN_TRAVERS,
////        ID_PROFIL_EN_LONG, // Utilisation interdite ! C'est ID_DOC qui est utilisé par les profils en long !
////        ID_TYPE_DOCUMENT_A_GRANDE_ECHELLE,
//        ID_CONVENTION,
////        DATE_DERNIERE_MAJ,
////        AUTEUR_RAPPORT,
//        ID_RAPPORT_ETUDE
    }

    @Override
    public String getTableName() {
        return DbImporter.TableName.DOCUMENT.name();
    }

    @Override
    public String getRowIdFieldName() {
        return Columns.ID_DOC.name();
    }

    @Override
    public Class<AbstractPositionDocument> getElementClass() {
        return AbstractPositionDocument.class;
    }

    @Override
    protected AbstractPositionDocument createElement(Row input) {
        Integer docType = input.getInt(Columns.ID_TYPE_DOCUMENT.toString());
        if (docType == null) {
            return null;
        }

        // Find what type of element must be imported.
        Class docClass = docTypes.getDocType(docType);
        if (docClass == null) {
            return null;
        } else if (ProfilTravers.class.isAssignableFrom(docClass)) {
            docClass = PositionProfilTravers.class;
        } else if (!ProfilLong.class.isAssignableFrom(docClass)) {
            docClass = PositionDocument.class;
        }

        return (AbstractPositionDocument) ElementCreator.createAnonymValidElement(docClass);
    }
}
