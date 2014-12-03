package fr.sirs.importer.objet.monteeDesEaux;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.EvenementHydraulique;
import fr.sirs.core.model.MonteeEaux;
import fr.sirs.core.model.RefReferenceHauteur;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.BorneDigueImporter;
import fr.sirs.importer.DbImporter;
import static fr.sirs.importer.DbImporter.cleanNullString;
import fr.sirs.importer.IntervenantImporter;
import fr.sirs.importer.OrganismeImporter;
import fr.sirs.importer.SystemeReperageImporter;
import fr.sirs.importer.TronconGestionDigueImporter;
import fr.sirs.importer.evenementHydraulique.EvenementHydrauliqueImporter;
import fr.sirs.importer.objet.SourceInfoImporter;
import fr.sirs.importer.objet.TypeCoteImporter;
import fr.sirs.importer.objet.TypeFonctionImporter;
import fr.sirs.importer.objet.TypeMateriauImporter;
import fr.sirs.importer.objet.TypeNatureImporter;
import fr.sirs.importer.objet.TypePositionImporter;
import fr.sirs.importer.objet.TypeRefHeauImporter;
import fr.sirs.importer.objet.structure.ElementStructureImporter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
class SysEvtMonteeDesEauHydroImporter extends GenericMonteeDesEauxImporter {

    private final EvenementHydrauliqueImporter evenementHydrauliqueImporter;
    private final TypeRefHeauImporter typeRefHeauImporter;

    SysEvtMonteeDesEauHydroImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector, 
            final TronconGestionDigueImporter tronconGestionDigueImporter, 
            final SystemeReperageImporter systemeReperageImporter, 
            final BorneDigueImporter borneDigueImporter, 
            final OrganismeImporter organismeImporter,
            final IntervenantImporter intervenantImporter,
            final EvenementHydrauliqueImporter evenementHydrauliqueImporter,
            final ElementStructureImporter structureImporter, 
            final SourceInfoImporter typeSourceImporter,
            final TypePositionImporter typePositionImporter,
            final TypeCoteImporter typeCoteImporter,
            final TypeMateriauImporter typeMateriauImporter, 
            final TypeNatureImporter typeNatureImporter, 
            final TypeFonctionImporter typeFonctionImporter,
            final TypeRefHeauImporter typeRefHeauImporter) {
        super(accessDatabase, couchDbConnector, tronconGestionDigueImporter, 
                systemeReperageImporter, borneDigueImporter, organismeImporter, 
                intervenantImporter, typeSourceImporter, typeCoteImporter, 
                typePositionImporter, typeMateriauImporter, typeNatureImporter, 
                typeFonctionImporter);
        this.typeRefHeauImporter = typeRefHeauImporter;
        this.evenementHydrauliqueImporter = evenementHydrauliqueImporter;
    }

    private enum Columns {
        ID_MONTEE_DES_EAUX,
//        id_nom_element, // Redondant avec ID_MONTEE_DES_EAUX
//        ID_SOUS_GROUPE_DONNEES, // Redondant avec le type de données
//        LIBELLE_SOUS_GROUPE_DONNEES, // Redondant avec le type de données
//        DECALAGE_DEFAUT, // Affichage
//        DECALAGE, // Affichage
//        LIBELLE_SYSTEME_REP, // Redondant avec les SR
//        NOM_BORNE, // Redondant avec les bornes
//        NOM_EVENEMENT_HYDRAU, // Redondant avec les événements hydrauliques
//        NomEtIDEchelleLimni, // Redondant avec les échelles limni
        ID_TRONCON_GESTION,
        ID_EVENEMENT_HYDRAU,
        PR_CALCULE,
        X,
        Y,
        ID_SYSTEME_REP,
        ID_BORNEREF,
        AMONT_AVAL,
        DIST_BORNEREF,
        COMMENTAIRE,
//        ID_ECHELLE_LIMNI, // Correspondance ?? Référence quelle table ??
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
        return DbImporter.TableName.SYS_EVT_MONTEE_DES_EAUX_HYDRO.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {

        this.structures = new HashMap<>();
        this.structuresByTronconId = new HashMap<>();
        
        final Map<Integer, BorneDigue> bornes = borneDigueImporter.getBorneDigue();
        final Map<Integer, TronconDigue> troncons = tronconGestionDigueImporter.getTronconsDigues();
        final Map<Integer, SystemeReperage> systemesReperage = systemeReperageImporter.getSystemeRepLineaire();
        final Map<Integer, EvenementHydraulique> evenementsHydrau = evenementHydrauliqueImporter.getEvenementHydraulique();
        final Map<Integer, RefReferenceHauteur> referenceHauteur = typeRefHeauImporter.getTypes();
        
        
        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final MonteeEaux monteeEaux = new MonteeEaux();
            
            final TronconDigue troncon = troncons.get(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
            if (troncon.getId() != null) {
                monteeEaux.setTroncon(troncon.getId());
            } else {
                throw new AccessDbImporterException("Le tronçon "
                        + troncons.get(row.getInt(Columns.ID_TRONCON_GESTION.toString())) + " n'a pas encore d'identifiant CouchDb !");
            }
            
            if(row.getInt(Columns.ID_EVENEMENT_HYDRAU.toString())!=null){
                monteeEaux.setEvenementId(evenementsHydrau.get(row.getInt(Columns.ID_EVENEMENT_HYDRAU.toString())).getId());
            }
            
            if (row.getDouble(Columns.PR_CALCULE.toString()) != null) {
                monteeEaux.setPR_debut(row.getDouble(Columns.PR_CALCULE.toString()).floatValue());
            }
            
            if (row.getDouble(Columns.PR_CALCULE.toString()) != null) {
                monteeEaux.setPR_fin(row.getDouble(Columns.PR_CALCULE.toString()).floatValue());
            }
            
            GeometryFactory geometryFactory = new GeometryFactory();
            final MathTransform lambertToRGF;
            try {
                lambertToRGF = CRS.findMathTransform(CRS.decode("EPSG:27563"), CRS.decode("EPSG:2154"), true);

                try {

                    if (row.getDouble(Columns.X.toString()) != null 
                            && row.getDouble(Columns.Y.toString()) != null) {
                        monteeEaux.setPositionDebut((Point) JTS.transform(
                                geometryFactory.createPoint(new Coordinate(
                                row.getDouble(Columns.X.toString()),
                                row.getDouble(Columns.Y.toString()))), lambertToRGF));
                    }
                } catch (MismatchedDimensionException | TransformException ex) {
                    Logger.getLogger(SysEvtMonteeDesEauHydroImporter.class.getName()).log(Level.SEVERE, null, ex);
                }

                try {

                    if (row.getDouble(Columns.X.toString()) != null 
                            && row.getDouble(Columns.Y.toString()) != null) {
                        monteeEaux.setPositionFin((Point) JTS.transform(
                                geometryFactory.createPoint(new Coordinate(
                                row.getDouble(Columns.X.toString()),
                                row.getDouble(Columns.Y.toString()))), lambertToRGF));
                    }
                } catch (MismatchedDimensionException | TransformException ex) {
                    Logger.getLogger(SysEvtMonteeDesEauHydroImporter.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (FactoryException ex) {
                Logger.getLogger(SysEvtMonteeDesEauHydroImporter.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            if(row.getInt(Columns.ID_SYSTEME_REP.toString())!=null){
                monteeEaux.setSystemeRepId(systemesReperage.get(row.getInt(Columns.ID_SYSTEME_REP.toString())).getId());
            }
            
             if (row.getDouble(Columns.ID_BORNEREF.toString()) != null) {
                final BorneDigue b = bornes.get((int) row.getDouble(Columns.ID_BORNEREF.toString()).doubleValue());
                if(b!=null) monteeEaux.setBorneDebutId(b.getId());
            }
            
            if (row.getDouble(Columns.ID_BORNEREF.toString()) != null) {
                final BorneDigue b = bornes.get((int) row.getDouble(Columns.ID_BORNEREF.toString()).doubleValue());
                if (b!=null) monteeEaux.setBorneFinId(b.getId());
            }
             
            monteeEaux.setBorne_debut_aval(row.getBoolean(Columns.AMONT_AVAL.toString())); 
            
            monteeEaux.setBorne_fin_aval(row.getBoolean(Columns.AMONT_AVAL.toString()));
            
            if (row.getDouble(Columns.DIST_BORNEREF.toString()) != null) {
                monteeEaux.setBorne_debut_distance(row.getDouble(Columns.DIST_BORNEREF.toString()).floatValue());
            }
            
            if (row.getDouble(Columns.DIST_BORNEREF.toString()) != null) {
                monteeEaux.setBorne_fin_distance(row.getDouble(Columns.DIST_BORNEREF.toString()).floatValue());
            }
            
            monteeEaux.setCommentaire(cleanNullString(row.getString(Columns.COMMENTAIRE.toString())));
            
//            if(row.getInt(Columns.ID_TYPE_REF_HEAU.toString())!=null){
//                monteeEaux.setReferenceHauteurId(referenceHauteur.get(row.getInt(Columns.ID_TYPE_REF_HEAU.toString())).getId());
//            }
            
            // Don't set the old ID, but save it into the dedicated map in order to keep the reference.
            structures.put(row.getInt(Columns.ID_MONTEE_DES_EAUX.toString()), monteeEaux);

            // Set the list ByTronconId
            List<MonteeEaux> listByTronconId = structuresByTronconId.get(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
            if (listByTronconId == null) {
                listByTronconId = new ArrayList<>();
            }
            listByTronconId.add(monteeEaux);
            structuresByTronconId.put(row.getInt(Columns.ID_TRONCON_GESTION.toString()), listByTronconId);
        }
    }
}
