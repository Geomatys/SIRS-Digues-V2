package fr.sirs.importer.objet.prestation;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import static fr.sirs.core.LinearReferencingUtilities.buildGeometry;
import fr.sirs.core.model.BorneDigue;
import static fr.sirs.core.model.ElementCreator.createAnonymValidElement;
import fr.sirs.core.model.Marche;
import fr.sirs.core.model.Prestation;
import fr.sirs.core.model.RefCote;
import fr.sirs.core.model.RefPosition;
import fr.sirs.core.model.RefPrestation;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.v2.linear.BorneDigueImporter;
import fr.sirs.importer.DbImporter;
import static fr.sirs.importer.DbImporter.TableName.*;
import static fr.sirs.importer.DbImporter.cleanNullString;
import fr.sirs.importer.v2.linear.SystemeReperageImporter;
import fr.sirs.importer.v2.references.TypeCoteImporter;
import fr.sirs.importer.objet.TypePositionImporter;
import fr.sirs.importer.documentTroncon.document.marche.MarcheImporter;
import fr.sirs.importer.v2.linear.TronconGestionDigueImporter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ektorp.CouchDbConnector;
import org.geotoolkit.geometry.jts.JTS;
import org.geotoolkit.referencing.CRS;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
class SysEvtPrestationImporter extends GenericPrestationImporter {

    private static final String OUI = "Oui";
    private static final String NON = "Non";
    
    private final TypePrestationImporter typePrestationImporter;

    SysEvtPrestationImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector, 
            final TronconGestionDigueImporter tronconGestionDigueImporter,
            final SystemeReperageImporter systemeReperageImporter, 
            final BorneDigueImporter borneDigueImporter, 
            final MarcheImporter marcheImporter,
            final TypePositionImporter typePositionImporter,
            final TypeCoteImporter typeCoteImporter,
            final TypePrestationImporter typePrestationImporter) {
        super(accessDatabase, couchDbConnector, tronconGestionDigueImporter,
                systemeReperageImporter, borneDigueImporter,  marcheImporter, 
                null, typePositionImporter, typeCoteImporter);
        this.typePrestationImporter = typePrestationImporter;
    }

    private enum Columns {
        ID_PRESTATION,
//        id_nom_element, // Redondance avec ID_PRESTATION
//        ID_SOUS_GROUPE_DONNEES, // Redondance avec le type de données
//        LIBELLE_SOUS_GROUPE_DONNEES, // Redondance avec le type de données 
        ID_TYPE_PRESTATION,
//        LIBELLE_TYPE_PRESTATION, // Redondance avec l'importation des types de prestations.
//        DECALAGE_DEFAUT, // Affichage
//        DECALAGE, // Affichage
//        LIBELLE_TYPE_COTE, // Redondance avec l'importation des cotes
//        LIBELLE_SYSTEME_REP, // Redondance avec l'importation des SR
//        NOM_BORNE_DEBUT, // Redondance avec l'importation des bornes
//        NOM_BORNE_FIN, // Redondance avec l'importation des bornes
        ID_MARCHE,
//        LIBELLE_MARCHE, // Redondance avec l'importation des marchés
        REALISATION_INTERNE_OUI_NON,
//        ID_INTERV_REALISATEUR, // Ne sert à rien (l'association N/N est gérée via la table prestation/intervenant
//        NOM_INTERVENANT_REALISATEUR, // Redondance avec l'importation des intervenants
//        PRENOM_INTERVENANT_REALISATEUR, // Redondance avec l'importation des intervenants
        ID_TYPE_POSITION,
//        LIBELLE_TYPE_POSITION, // Redondance avec l'importation des positions.
        ID_TYPE_COTE,
        ID_TRONCON_GESTION,
        DATE_DEBUT_VAL,
        DATE_FIN_VAL,
        PR_DEBUT_CALCULE,
        PR_FIN_CALCULE,
        X_DEBUT,
        Y_DEBUT,
        X_FIN,
        Y_FIN,
        ID_SYSTEME_REP,
        ID_BORNEREF_DEBUT,
        AMONT_AVAL_DEBUT,
        DIST_BORNEREF_DEBUT,
        ID_BORNEREF_FIN,
        AMONT_AVAL_FIN,
        DIST_BORNEREF_FIN,
        LIBELLE_PRESTATION,
        DESCRIPTION_PRESTATION,
//        ID_AUTO
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
        return SYS_EVT_PRESTATION.toString();
    }
    
    @Override
    public public  importRow(Row row) throws IOException, AccessDbImporterException {
        
        final TronconDigue troncon = tronconGestionDigueImporter.getTronconsDigues().get(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
        final Map<Integer, BorneDigue> bornes = borneDigueImporter.getBorneDigue();
        final Map<Integer, SystemeReperage> systemesReperage = systemeReperageImporter.getSystemeRepLineaire();
        
        final Map<Integer, RefPosition> typesPosition = typePositionImporter.getTypeReferences();
        final Map<Integer, RefCote> typesCote = typeCoteImporter.getTypeReferences();
        
        final Map<Integer, RefPrestation> typesPrestation = typePrestationImporter.getTypeReferences();
        
        final Map<Integer, Marche> marches = marcheImporter.getRelated();
        
        final Prestation prestation = createAnonymValidElement(Prestation.class);
        
        prestation.setLinearId(troncon.getId());
        prestation.setDesignation(String.valueOf(row.getInt(Columns.ID_PRESTATION.toString())));
        
        if (row.getInt(Columns.ID_TYPE_PRESTATION.toString()) != null) {
            prestation.setTypePrestationId(typesPrestation.get(row.getInt(Columns.ID_TYPE_PRESTATION.toString())).getId());
        }
        
        if (row.getInt(Columns.ID_MARCHE.toString()) != null) {
            prestation.setMarcheId(marches.get(row.getInt(Columns.ID_MARCHE.toString())).getId());
        }
        
        if (row.getInt(Columns.ID_TYPE_POSITION.toString()) != null) {
            prestation.setPositionId(typePositionImporter.getImportedId(row.getInt(Columns.ID_TYPE_POSITION.toString())).getId());
        }
        
        if (row.getInt(Columns.ID_TYPE_COTE.toString()) != null) {
            prestation.setCoteId(typesCote.get(row.getInt(Columns.ID_TYPE_COTE.toString())).getId());
        }
        
        if (row.getDate(Columns.DATE_DEBUT_VAL.toString()) != null) {
            prestation.setDate_debut(DbImporter.parseLocalDate(row.getDate(Columns.DATE_DEBUT_VAL.toString()), dateTimeFormatter, prestation));
        }
        
        if (row.getDate(Columns.DATE_FIN_VAL.toString()) != null) {
            prestation.setDate_fin(DbImporter.parseLocalDate(row.getDate(Columns.DATE_FIN_VAL.toString()), dateTimeFormatter, prestation));
        }
        
        if (row.getDouble(Columns.PR_DEBUT_CALCULE.toString()) != null) {
            prestation.setPrDebut(row.getDouble(Columns.PR_DEBUT_CALCULE.toString()).floatValue());
        }
        
        if (row.getDouble(Columns.PR_FIN_CALCULE.toString()) != null) {
            prestation.setPrFin(row.getDouble(Columns.PR_FIN_CALCULE.toString()).floatValue());
        }
        
        GeometryFactory geometryFactory = new GeometryFactory();
        final MathTransform lambertToRGF;
        try {
            lambertToRGF = CRS.findMathTransform(DbImporter.IMPORT_CRS, getOutputCrs(), true);
            
            try {
                
                if (row.getDouble(Columns.X_DEBUT.toString()) != null && row.getDouble(Columns.Y_DEBUT.toString()) != null) {
                    prestation.setPositionDebut((Point) JTS.transform(geometryFactory.createPoint(new Coordinate(
                            row.getDouble(Columns.X_DEBUT.toString()),
                            row.getDouble(Columns.Y_DEBUT.toString()))), lambertToRGF));
                }
            } catch (MismatchedDimensionException | TransformException ex) {
                Logger.getLogger(SysEvtPrestationImporter.class.getName()).log(Level.WARNING, null, ex);
            }
            
            try {
                
                if (row.getDouble(Columns.X_FIN.toString()) != null && row.getDouble(Columns.Y_FIN.toString()) != null) {
                    prestation.setPositionFin((Point) JTS.transform(geometryFactory.createPoint(new Coordinate(
                            row.getDouble(Columns.X_FIN.toString()),
                            row.getDouble(Columns.Y_FIN.toString()))), lambertToRGF));
                }
            } catch (MismatchedDimensionException | TransformException ex) {
                Logger.getLogger(SysEvtPrestationImporter.class.getName()).log(Level.WARNING, null, ex);
            }
        } catch (FactoryException ex) {
            Logger.getLogger(SysEvtPrestationImporter.class.getName()).log(Level.WARNING, null, ex);
        }
        
        if (row.getInt(Columns.ID_SYSTEME_REP.toString()) != null) {
            prestation.setSystemeRepId(systemesReperage.get(row.getInt(Columns.ID_SYSTEME_REP.toString())).getId());
        }
        
        if (row.getDouble(Columns.ID_BORNEREF_DEBUT.toString()) != null) {
            final BorneDigue b = bornes.get((int) row.getDouble(Columns.ID_BORNEREF_DEBUT.toString()).doubleValue());
            if (b != null) {
                prestation.setBorneDebutId(b.getId());
            }
        }
        
        prestation.setBorne_debut_aval(row.getBoolean(Columns.AMONT_AVAL_DEBUT.toString()));        
        
        if (row.getDouble(Columns.DIST_BORNEREF_DEBUT.toString()) != null) {
            prestation.setBorne_debut_distance(row.getDouble(Columns.DIST_BORNEREF_DEBUT.toString()).floatValue());
        }
        
        if (row.getDouble(Columns.ID_BORNEREF_FIN.toString()) != null) {
            final BorneDigue b = bornes.get((int) row.getDouble(Columns.ID_BORNEREF_FIN.toString()).doubleValue());
            if (b != null) {
                prestation.setBorneFinId(b.getId());
            }
        }
        
        prestation.setBorne_fin_aval(row.getBoolean(Columns.AMONT_AVAL_FIN.toString()));
        
        if (row.getDouble(Columns.DIST_BORNEREF_FIN.toString()) != null) {
            prestation.setBorne_fin_distance(row.getDouble(Columns.DIST_BORNEREF_FIN.toString()).floatValue());
        }
        
        prestation.setLibelle(cleanNullString(row.getString(Columns.LIBELLE_PRESTATION.toString())));
        
        prestation.setCommentaire(cleanNullString(row.getString(Columns.DESCRIPTION_PRESTATION.toString())));
        
        prestation.setGeometry(buildGeometry(troncon.getGeometry(), prestation, tronconGestionDigueImporter.getBorneDigueRepository()));
        
        return prestation;
    }
}
