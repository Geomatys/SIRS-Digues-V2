package fr.sirs.importer.objet.laisseCrue;

import fr.sirs.importer.objet.TypeRefHeauImporter;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.Contact;
import fr.sirs.core.model.EvenementHydraulique;
import fr.sirs.core.model.LaisseCrue;
import fr.sirs.core.model.RefReferenceHauteur;
import fr.sirs.core.model.RefSource;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.BorneDigueImporter;
import fr.sirs.importer.DbImporter;
import static fr.sirs.importer.DbImporter.cleanNullString;
import fr.sirs.importer.IntervenantImporter;
import fr.sirs.importer.SystemeReperageImporter;
import fr.sirs.importer.troncon.TronconGestionDigueImporter;
import fr.sirs.importer.evenementHydraulique.EvenementHydrauliqueImporter;
import fr.sirs.importer.objet.SourceInfoImporter;
import java.io.IOException;
import java.time.LocalDateTime;
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
class SysEvtLaisseCrueImporter extends GenericLaisseCrueImporter {

    SysEvtLaisseCrueImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector, 
            final TronconGestionDigueImporter tronconGestionDigueImporter, 
            final SystemeReperageImporter systemeReperageImporter, 
            final BorneDigueImporter borneDigueImporter, 
            final IntervenantImporter intervenantImporter,
            final EvenementHydrauliqueImporter evenementHydrauliqueImporter,
            final SourceInfoImporter typeSourceImporter,
            final TypeRefHeauImporter typeRefHeauImporter) {
        super(accessDatabase, couchDbConnector, tronconGestionDigueImporter, 
                systemeReperageImporter, borneDigueImporter,
                intervenantImporter, evenementHydrauliqueImporter, 
                typeSourceImporter, typeRefHeauImporter);
    }

    private enum Columns {
        ID_LAISSE_CRUE,
//        id_nom_element, // Redondant avec ID_LAISSE_CRUE
//        ID_SOUS_GROUPE_DONNEES, // Redondant avec le type de données
//        LIBELLE_SOUS_GROUPE_DONNEES, // Redondant avec le type de données
//        DECALAGE_DEFAUT, // Affichage
//        DECALAGE, // Affichage
//        LIBELLE_SOURCE, // Redondant avec l'importation des sources
//        LIBELLE_SYSTEME_REP, // Redondant avec l'importation des SR
//        NOM_BORNE_DEBUT, // Redondant avec l'importation des bornes
//        NOM_BORNE_FIN, // Redondant avec l'importation des bornes
//        NOM_EVENEMENT_HYDRAU, // Redondant avec l'importation des événements hydrauliques
//        TypeRefHEau, // Redondant avec l'importation des hauteurs d'eau
//        NomPrenomObservateur, // Redondant avec l'importation des intervenants observateurs
        ID_TRONCON_GESTION,
        ID_SOURCE,
        ID_EVENEMENT_HYDRAU,
        DATE,
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
        COMMENTAIRE,
        ID_TYPE_REF_HEAU,
        HAUTEUR_EAU,
        ID_INTERV_OBSERVATEUR,
        POSITION,
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
        return DbImporter.TableName.SYS_EVT_LAISSE_CRUE.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {

        this.structures = new HashMap<>();
        this.structuresByTronconId = new HashMap<>();
        
        final Map<Integer, BorneDigue> bornes = borneDigueImporter.getBorneDigue();
        final Map<Integer, TronconDigue> troncons = tronconGestionDigueImporter.getTronconsDigues();
        final Map<Integer, SystemeReperage> systemesReperage = systemeReperageImporter.getSystemeRepLineaire();
        
        final Map<Integer, RefSource> typesSource = sourceInfoImporter.getTypes();
        
        final Map<Integer, Contact> intervenants = intervenantImporter.getIntervenants();
        final Map<Integer, EvenementHydraulique> evenementsHydrau = evenementHydrauliqueImporter.getEvenementHydraulique();
        
        final Map<Integer, RefReferenceHauteur> referenceHauteur = typeRefHeauImporter.getTypes();
        
        
        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final LaisseCrue laisseCrue = new LaisseCrue();
            
//            final TronconDigue troncon = troncons.get(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
//            if (troncon.getId() != null) {
//                laisseCrue.setTroncon(troncon.getId());
//            } else {
//                throw new AccessDbImporterException("Le tronçon "
//                        + troncons.get(row.getInt(Columns.ID_TRONCON_GESTION.toString())) + " n'a pas encore d'identifiant CouchDb !");
//            }
            
            if(row.getInt(Columns.ID_SOURCE.toString())!=null){
                laisseCrue.setPosition_structure(typesSource.get(row.getInt(Columns.ID_SOURCE.toString())).getId());
            }
            
            if(row.getInt(Columns.ID_EVENEMENT_HYDRAU.toString())!=null){
                laisseCrue.setEvenementId(evenementsHydrau.get(row.getInt(Columns.ID_EVENEMENT_HYDRAU.toString())).getId());
            }
            
            if (row.getDate(Columns.DATE.toString()) != null) {
                laisseCrue.setDate(LocalDateTime.parse(row.getDate(Columns.DATE.toString()).toString(), dateTimeFormatter));
            }
            
            if (row.getDouble(Columns.PR_DEBUT_CALCULE.toString()) != null) {
                laisseCrue.setPR_debut(row.getDouble(Columns.PR_DEBUT_CALCULE.toString()).floatValue());
            }
            
            if (row.getDouble(Columns.PR_FIN_CALCULE.toString()) != null) {
                laisseCrue.setPR_fin(row.getDouble(Columns.PR_FIN_CALCULE.toString()).floatValue());
            }
            
            GeometryFactory geometryFactory = new GeometryFactory();
            final MathTransform lambertToRGF;
            try {
                lambertToRGF = CRS.findMathTransform(CRS.decode("EPSG:27563"), getOutputCrs(), true);

                try {

                    if (row.getDouble(Columns.X_DEBUT.toString()) != null && row.getDouble(Columns.Y_DEBUT.toString()) != null) {
                        laisseCrue.setPositionDebut((Point) JTS.transform(geometryFactory.createPoint(new Coordinate(
                                row.getDouble(Columns.X_DEBUT.toString()),
                                row.getDouble(Columns.Y_DEBUT.toString()))), lambertToRGF));
                    }
                } catch (MismatchedDimensionException | TransformException ex) {
                    Logger.getLogger(SysEvtLaisseCrueImporter.class.getName()).log(Level.SEVERE, null, ex);
                }

                try {

                    if (row.getDouble(Columns.X_FIN.toString()) != null && row.getDouble(Columns.Y_FIN.toString()) != null) {
                        laisseCrue.setPositionFin((Point) JTS.transform(geometryFactory.createPoint(new Coordinate(
                                row.getDouble(Columns.X_FIN.toString()),
                                row.getDouble(Columns.Y_FIN.toString()))), lambertToRGF));
                    }
                } catch (MismatchedDimensionException | TransformException ex) {
                    Logger.getLogger(SysEvtLaisseCrueImporter.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (FactoryException ex) {
                Logger.getLogger(SysEvtLaisseCrueImporter.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            if(row.getInt(Columns.ID_SYSTEME_REP.toString())!=null){
                laisseCrue.setSystemeRepId(systemesReperage.get(row.getInt(Columns.ID_SYSTEME_REP.toString())).getId());
            }
            
             if (row.getDouble(Columns.ID_BORNEREF_DEBUT.toString()) != null) {
                final BorneDigue b = bornes.get((int) row.getDouble(Columns.ID_BORNEREF_DEBUT.toString()).doubleValue());
                if(b!=null) laisseCrue.setBorneDebutId(b.getId());
            }
             
            laisseCrue.setBorne_debut_aval(row.getBoolean(Columns.AMONT_AVAL_DEBUT.toString())); 
            
            if (row.getDouble(Columns.DIST_BORNEREF_DEBUT.toString()) != null) {
                laisseCrue.setBorne_debut_distance(row.getDouble(Columns.DIST_BORNEREF_DEBUT.toString()).floatValue());
            }
            
            if (row.getDouble(Columns.ID_BORNEREF_FIN.toString()) != null) {
                final BorneDigue b = bornes.get((int) row.getDouble(Columns.ID_BORNEREF_FIN.toString()).doubleValue());
                if (b!=null) laisseCrue.setBorneFinId(b.getId());
            }
            
            laisseCrue.setBorne_fin_aval(row.getBoolean(Columns.AMONT_AVAL_FIN.toString()));
            
            if (row.getDouble(Columns.DIST_BORNEREF_FIN.toString()) != null) {
                laisseCrue.setBorne_fin_distance(row.getDouble(Columns.DIST_BORNEREF_FIN.toString()).floatValue());
            }
            
            laisseCrue.setCommentaire(cleanNullString(row.getString(Columns.COMMENTAIRE.toString())));
            
            if(row.getInt(Columns.ID_TYPE_REF_HEAU.toString())!=null){
                laisseCrue.setReferenceHauteurId(referenceHauteur.get(row.getInt(Columns.ID_TYPE_REF_HEAU.toString())).getId());
            }
            
            if (row.getDouble(Columns.HAUTEUR_EAU.toString()) != null) {
                laisseCrue.setHauteur(row.getDouble(Columns.HAUTEUR_EAU.toString()).floatValue());
            }
            
            if(row.getInt(Columns.ID_INTERV_OBSERVATEUR.toString())!=null){
                laisseCrue.setObservateurId(intervenants.get(row.getInt(Columns.ID_INTERV_OBSERVATEUR.toString())).getId());
            }
            
            laisseCrue.setPosition_laisse(cleanNullString(row.getString(Columns.POSITION.toString())));
            
            
            // Don't set the old ID, but save it into the dedicated map in order to keep the reference.
            structures.put(row.getInt(Columns.ID_LAISSE_CRUE.toString()), laisseCrue);

            // Set the list ByTronconId
            List<LaisseCrue> listByTronconId = structuresByTronconId.get(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
            if (listByTronconId == null) {
                listByTronconId = new ArrayList<>();
            }
            listByTronconId.add(laisseCrue);
            structuresByTronconId.put(row.getInt(Columns.ID_TRONCON_GESTION.toString()), listByTronconId);
        }
    }
}
