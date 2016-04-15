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
package fr.sirs.importer.v2.objet.photo;

import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.Crete;
import fr.sirs.core.model.Desordre;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.Epi;
import fr.sirs.core.model.Fondation;
import fr.sirs.core.model.LaisseCrue;
import fr.sirs.core.model.LargeurFrancBord;
import fr.sirs.core.model.LigneEau;
import fr.sirs.core.model.MonteeEaux;
import fr.sirs.core.model.OuvertureBatardable;
import fr.sirs.core.model.OuvrageFranchissement;
import fr.sirs.core.model.OuvrageHydrauliqueAssocie;
import fr.sirs.core.model.OuvrageParticulier;
import fr.sirs.core.model.OuvrageRevanche;
import fr.sirs.core.model.OuvrageTelecomEnergie;
import fr.sirs.core.model.OuvrageVoirie;
import fr.sirs.core.model.PiedDigue;
import fr.sirs.core.model.PositionDocument;
import fr.sirs.core.model.PositionProfilTravers;
import fr.sirs.core.model.Prestation;
import fr.sirs.core.model.ProfilLong;
import fr.sirs.core.model.ReseauHydrauliqueCielOuvert;
import fr.sirs.core.model.ReseauHydrauliqueFerme;
import fr.sirs.core.model.ReseauTelecomEnergie;
import fr.sirs.core.model.SommetRisberme;
import fr.sirs.core.model.StationPompage;
import fr.sirs.core.model.TalusDigue;
import fr.sirs.core.model.TalusRisberme;
import fr.sirs.core.model.VoieAcces;
import fr.sirs.core.model.VoieDigue;
import fr.sirs.importer.DbImporter;
import static fr.sirs.importer.DbImporter.TableName.TYPE_DONNEES_SOUS_GROUPE;
import static fr.sirs.importer.DbImporter.TableName.valueOf;
import fr.sirs.importer.v2.ImportContext;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Samuel Andres (Geomatys)
 * @author Alexis Manin (Geomatys)
 */
@Component
public class PhotoHolderRegistry {

    private enum Columns {
        ID_GROUPE_DONNEES,
        ID_SOUS_GROUPE_DONNEES,
        ID_TYPE_DONNEE,
//        LIBELLE_SOUS_GROUPE_DONNEES,
        NOM_TABLE_EVT,
//        ID_NOM_TABLE_EVTTYPE_OBJET_CARTO,
//        DECALAGE,
//        DATE_DERNIERE_MAJ
    }

    private final HashMap<Map.Entry<Object, Object>, Class<Element>> types = new HashMap<>();

    @Autowired
    private PhotoHolderRegistry(ImportContext context) throws IOException {
        Iterator<Row> iterator = context.inputDb.getTable(TYPE_DONNEES_SOUS_GROUPE.name()).iterator();

        while (iterator.hasNext()) {
            final Row row = iterator.next();
            try {
                final DbImporter.TableName table = valueOf(row.getString(Columns.NOM_TABLE_EVT.toString()));
                final Class clazz = getClazz(table);

                if (clazz == null) {
                    // context.reportError(new ErrorReport(null, row, TYPE_ELEMENT_RESEAU.name(), Columns.NOM_TABLE_EVT.name(), null, null, "Unrecognized element type", null));
                } else {
                    final Object type = row.get(Columns.ID_GROUPE_DONNEES.name());
                    final Object subType = row.get(Columns.ID_SOUS_GROUPE_DONNEES.name());

                    types.put(new AbstractMap.SimpleImmutableEntry<>(type, subType), clazz);
                }
            } catch (IllegalArgumentException e) {
                // context.reportError(new ErrorReport(null, row, TYPE_ELEMENT_RESEAU.name(), Columns.NOM_TABLE_EVT.name(), null, null, "Unrecognized element type", null));
            }
        }
    }

    /**
     * @param typeId An id found in {@link Columns#ID_GROUPE_DONNEES} column.
     * @param subTypeId An id found in {@link Columns#ID_SOUS_GROUPE_DONNEES} column.
     * @return document class associated to given document type ID, or null, if
     * given Id is unknown.
     */
    public Class<Element> getElementType(final Object typeId, final Object subTypeId) {
        return types.get(new AbstractMap.SimpleImmutableEntry<>(typeId, subTypeId));
    }

    public Class<Element> getElementType(final Row input) {
        final Object type = input.get(Columns.ID_GROUPE_DONNEES.name());
        final Object subType = input.get(Columns.ID_SOUS_GROUPE_DONNEES.name());
        if (type != null && subType != null) {
            return getElementType(type, subType);
        }
        return null;
    }

    private Class<? extends Element> getClazz(final DbImporter.TableName tableName) {

        switch(tableName){
            // STRUCTURES
            case SYS_EVT_CRETE: return Crete.class;
            case SYS_EVT_EPIS: return Epi.class;
            case SYS_EVT_FONDATION: return Fondation.class;
            case SYS_EVT_OUVRAGE_REVANCHE: return OuvrageRevanche.class;
            case SYS_EVT_PIED_DE_DIGUE: return PiedDigue.class;
//            case SYS_EVT_PIED_FRONT_FRANC_BORD: return PiedFrontFrancBord.class;
            case SYS_EVT_SOMMET_RISBERME: return SommetRisberme.class;
            case SYS_EVT_TALUS_DIGUE: return TalusDigue.class;
//            case SYS_EVT_TALUS_FRANC_BORD: return FrontFrancBord.class;
            case SYS_EVT_TALUS_RISBERME: return TalusRisberme.class;
//            case SYS_EVT_BRISE_LAME:
            // RESEAUX
            case SYS_EVT_AUTRE_OUVRAGE_HYDRAULIQUE: return OuvrageHydrauliqueAssocie.class;
            case SYS_EVT_CHEMIN_ACCES: return VoieAcces.class;
            case SYS_EVT_CONDUITE_FERMEE: return ReseauHydrauliqueFerme.class;
            case SYS_EVT_OUVERTURE_BATARDABLE: return OuvertureBatardable.class;
            case SYS_EVT_OUVRAGE_PARTICULIER: return OuvrageParticulier.class;
            case SYS_EVT_OUVRAGE_TELECOMMUNICATION: return OuvrageTelecomEnergie.class;
            case SYS_EVT_OUVRAGE_VOIRIE: return OuvrageVoirie.class;
            case SYS_EVT_POINT_ACCES: return OuvrageFranchissement.class;
            case SYS_EVT_RESEAU_EAU: return ReseauHydrauliqueCielOuvert.class;
            case SYS_EVT_RESEAU_TELECOMMUNICATION: return ReseauTelecomEnergie.class;
            case SYS_EVT_STATION_DE_POMPAGE: return StationPompage.class;
            case SYS_EVT_VOIE_SUR_DIGUE: return VoieDigue.class;
            // GEOMETRIES
//            case SYS_EVT_PROFIL_FRONT_FRANC_BORD: return ProfilFrontFrancBord.class;
            case SYS_EVT_LARGEUR_FRANC_BORD: return LargeurFrancBord.class;
            // DESORDRES
            case SYS_EVT_DESORDRE: return Desordre.class;
            // PRESTATIONS
            case SYS_EVT_PRESTATION: return Prestation.class;
            // MONTEES DES EAUX
            case SYS_EVT_MONTEE_DES_EAUX_HYDRO: return MonteeEaux.class;
            // LAISSE CRUES
            case SYS_EVT_LAISSE_CRUE: return LaisseCrue.class;
            // LIGNE EAU
            case SYS_EVT_LIGNE_EAU: return LigneEau.class;
            // POSITIONS DE DOCUMENTS
            case SYS_EVT_CONVENTION:
            case SYS_EVT_DOCUMENT_A_GRANDE_ECHELLE:
            case SYS_EVT_JOURNAL:
            case SYS_EVT_MARCHE:
            case SYS_EVT_RAPPORT_ETUDES: return PositionDocument.class;
            // POSITIONS DE PROFILS EN TRAVERS:
            case SYS_EVT_PROFIL_EN_TRAVERS: return PositionProfilTravers.class;
            // PROFILS EN LONG:
            case SYS_EVT_PROFIL_EN_LONG: return ProfilLong.class;
//            case SYS_EVT_DISTANCE_PIED_DE_DIGUE_TRONCON:
//            case SYS_EVT_ILE_TRONCON:
//            case SYS_EVT_PROPRIETAIRE_TRONCON:
//            case SYS_EVT_GARDIEN_TRONCON:
//            case SYS_EVT_VEGETATION:
//            case SYS_EVT_PHOTO_LOCALISEE_EN_PR:
//            case SYS_EVT_EMPRISE_COMMUNALE:
//            case SYS_EVT_EMPRISE_SYNDICAT:
//            case SYS_EVT_SITUATION_FONCIERE:
            default: return null;
//SYS_RQ_EXTRAIT_DESORDRE_TGD
//SYS_RQ_MONTANT_PRESTATION_TGD
//SYS_RQ_PROPRIETAIRE_TRAVERSEE_TGD
//SYS_RQ_SENSIBILITE_EVT_HYDRAU_TGD
        }
    }
}
