package fr.sirs.importer.objet.ligneEau;

import fr.sirs.core.SirsCore;
import java.util.logging.Level;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.EvenementHydraulique;
import fr.sirs.core.model.LigneEau;
import fr.sirs.core.model.MesureLigneEau;
import fr.sirs.core.model.RefReferenceHauteur;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.BorneDigueImporter;
import fr.sirs.importer.DbImporter;
import static fr.sirs.importer.DbImporter.cleanNullString;
import fr.sirs.importer.SystemeReperageImporter;
import fr.sirs.importer.troncon.TronconGestionDigueImporter;
import fr.sirs.importer.evenementHydraulique.EvenementHydrauliqueImporter;
import fr.sirs.importer.objet.TypeRefHeauImporter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
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
public class LigneEauImporter extends GenericLigneEauImporter {
    
    private final LigneEauMesuresPrzImporter ligneEauMesuresPrzImporter;
    private final LigneEauMesuresXyzImporter ligneEauMesuresXyzImporter;
    private final SysEvtLigneEauImporter sysEvtLigneEauImporter;

    public LigneEauImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector, 
            final TronconGestionDigueImporter tronconGestionDigueImporter, 
            final SystemeReperageImporter systemeReperageImporter, 
            final BorneDigueImporter borneDigueImporter, 
            final EvenementHydrauliqueImporter evenementHydrauliqueImporter,
            final TypeRefHeauImporter typeRefHeauImporter) {
        super(accessDatabase, couchDbConnector, tronconGestionDigueImporter, 
                systemeReperageImporter, borneDigueImporter, 
                evenementHydrauliqueImporter, typeRefHeauImporter);
        ligneEauMesuresPrzImporter = new LigneEauMesuresPrzImporter(
                accessDatabase, couchDbConnector);
        ligneEauMesuresXyzImporter = new LigneEauMesuresXyzImporter(
                accessDatabase, couchDbConnector);
        sysEvtLigneEauImporter = new SysEvtLigneEauImporter(
                accessDatabase, couchDbConnector, tronconGestionDigueImporter, 
                systemeReperageImporter, borneDigueImporter, 
                evenementHydrauliqueImporter, ligneEauMesuresPrzImporter, 
                ligneEauMesuresXyzImporter, typeRefHeauImporter);
    }

    private enum Columns {
        ID_LIGNE_EAU,
        ID_EVENEMENT_HYDRAU,
        ID_TRONCON_GESTION,
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
        ID_TYPE_REF_HEAU,
//        ID_SYSTEME_REP_PRZ,
        DATE,
        COMMENTAIRE,
        DATE_DERNIERE_MAJ
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
        return DbImporter.TableName.LIGNE_EAU.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        
        this.structures = sysEvtLigneEauImporter.getById();
        this.structuresByTronconId = sysEvtLigneEauImporter.getByTronconId();
        
        final Map<Integer, BorneDigue> bornes = borneDigueImporter.getBorneDigue();
        final Map<Integer, TronconDigue> troncons = tronconGestionDigueImporter.getTronconsDigues();
        final Map<Integer, SystemeReperage> systemesReperage = systemeReperageImporter.getSystemeRepLineaire();
        
        final Map<Integer, EvenementHydraulique> evenementsHydrau = evenementHydrauliqueImporter.getEvenementHydraulique();
        
        final Map<Integer, RefReferenceHauteur> referenceHauteur = typeRefHeauImporter.getTypeReferences();
        
        final Map<Integer, List<MesureLigneEau>> mesuresPrz = ligneEauMesuresPrzImporter.getMesuresByLigneEau();
        final Map<Integer, List<MesureLigneEau>> mesuresXyz = ligneEauMesuresXyzImporter.getMesuresByLigneEau();
        
        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final LigneEau ligneEau;
            final boolean nouvelleLigneEau;
            if(structures.get(row.getInt(Columns.ID_LIGNE_EAU.toString()))!=null){
                ligneEau = structures.get(row.getInt(Columns.ID_LIGNE_EAU.toString()));
                nouvelleLigneEau=false;
            }
            else{
                SirsCore.LOGGER.log(Level.FINE, "Nouvelle ligne eau !!");
                ligneEau = new LigneEau();
                nouvelleLigneEau=true;
            }
            
            if(row.getInt(Columns.ID_EVENEMENT_HYDRAU.toString())!=null){
                if(nouvelleLigneEau){
                    ligneEau.setEvenementId(evenementsHydrau.get(row.getInt(Columns.ID_EVENEMENT_HYDRAU.toString())).getId());
                }
            }
            
//            if (row.getInt(Columns.ID_TRONCON_GESTION.toString()) != null) {
//                final TronconDigue troncon = troncons.get(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
//                if (troncon.getId() != null && ligneEau.getTroncon()==null) {
//                    ligneEau.setTroncon(troncon.getId());
//                } else if(troncon.getId()==null) {
//                    throw new AccessDbImporterException("Le tronçon "
//                            + troncons.get(row.getInt(Columns.ID_TRONCON_GESTION.toString())) + " n'a pas encore d'identifiant CouchDb !");
//                } else if(!ligneEau.getTroncon().equals(troncon.getId())){
//                    throw new AccessDbImporterException("Inconsistent data.");
//                }
//            }
            
            if (row.getDouble(Columns.PR_DEBUT_CALCULE.toString()) != null) {
                final float v = row.getDouble(Columns.PR_DEBUT_CALCULE.toString()).floatValue();
                if(nouvelleLigneEau){
                    ligneEau.setPR_debut(v);
                }
//                else if(v!=desordre.getPR_debut()) {
//                    throw new AccessDbImporterException("Inconsistent data : "+v+" != "+desordre.getPR_debut()+" (id="+row.getInt(DesordreColumns.ID_DESORDRE.toString()));
//                }
            }
            
            if (row.getDouble(Columns.PR_FIN_CALCULE.toString()) != null) {
                final float v = row.getDouble(Columns.PR_FIN_CALCULE.toString()).floatValue();
                if(nouvelleLigneEau){
                    ligneEau.setPR_fin(v);
                }
//                else if(v!=desordre.getPR_fin()) {
//                    throw new AccessDbImporterException("Inconsistent data.");
//                }
            }
            
            GeometryFactory geometryFactory = new GeometryFactory();
            final MathTransform lambertToRGF;
            try {
                lambertToRGF = CRS.findMathTransform(CRS.decode("EPSG:27563"), getOutputCrs(), true);

                try {

                    if (row.getDouble(Columns.X_DEBUT.toString()) != null 
                            && row.getDouble(Columns.Y_DEBUT.toString()) != null) {
                        final Point positionDebut = (Point) JTS.transform(geometryFactory.createPoint(new Coordinate(
                                row.getDouble(Columns.X_DEBUT.toString()),
                                row.getDouble(Columns.Y_DEBUT.toString()))), lambertToRGF);
                        if(ligneEau.getPositionDebut()==null){
                            ligneEau.setPositionDebut(positionDebut);
                        } else if(!ligneEau.getPositionDebut().equals(positionDebut)){
                            throw new AccessDbImporterException("Inconsistent data.");
                        }
                    }
                } catch (MismatchedDimensionException | TransformException ex) {
                    Logger.getLogger(LigneEauImporter.class.getName()).log(Level.SEVERE, null, ex);
                }

                try {

                    if (row.getDouble(Columns.X_FIN.toString()) != null 
                            && row.getDouble(Columns.Y_FIN.toString()) != null) {
                        final Point positionFin = (Point) JTS.transform(geometryFactory.createPoint(new Coordinate(
                                    row.getDouble(Columns.X_FIN.toString()),
                                    row.getDouble(Columns.Y_FIN.toString()))), lambertToRGF);
                        if(ligneEau.getPositionFin()==null){
                            ligneEau.setPositionFin(positionFin);
                        } else if(!ligneEau.getPositionFin().equals(positionFin)){
                            throw new AccessDbImporterException("Inconsistent data.");
                        }
                    }
                } catch (MismatchedDimensionException | TransformException ex) {
                    Logger.getLogger(LigneEauImporter.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (FactoryException ex) {
                Logger.getLogger(LigneEauImporter.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            if(row.getInt(Columns.ID_SYSTEME_REP.toString())!=null){
                final SystemeReperage sr = systemesReperage.get(row.getInt(Columns.ID_SYSTEME_REP.toString()));
                if(sr!=null){
                    if(ligneEau.getSystemeRepId()==null){
                        ligneEau.setSystemeRepId(sr.getId());
                    }
                    else if(!ligneEau.getSystemeRepId().equals(sr.getId())){
                        throw new AccessDbImporterException("Inconsistent data.");
                    }
                }
            }
            
            if (row.getDouble(Columns.ID_BORNEREF_DEBUT.toString()) != null) {
                final BorneDigue b = bornes.get((int) row.getDouble(Columns.ID_BORNEREF_DEBUT.toString()).doubleValue());
                if(b!=null) {
                    if(nouvelleLigneEau || ligneEau.getBorneDebutId()==null){
                        ligneEau.setBorneDebutId(b.getId());
                    }
                    else if(!ligneEau.getBorneDebutId().equals(b.getId())){
                        throw new AccessDbImporterException("Inconsistent data.");
                    }
                }
            }

            {
                final boolean bda = row.getBoolean(Columns.AMONT_AVAL_DEBUT.toString());
                if(nouvelleLigneEau){
                    ligneEau.setBorne_debut_aval(bda); 
                } 
//                else if(bda!=desordre.getBorne_debut_aval()){
//                    throw new AccessDbImporterException("Inconsistent data.");
//                }
            }
            
            if (row.getDouble(Columns.DIST_BORNEREF_DEBUT.toString()) != null) {
                final float v = row.getDouble(Columns.DIST_BORNEREF_DEBUT.toString()).floatValue();
                if(nouvelleLigneEau){
                    ligneEau.setBorne_debut_distance(v);
                }
//                else if(v!=desordre.getBorne_debut_distance()) {
//                    throw new AccessDbImporterException("Inconsistent data : "+v+" != "+desordre.getBorne_debut_distance()+" (id="+row.getInt(DesordreColumns.ID_DESORDRE.toString()));
//                }
            }
            
            if (row.getDouble(Columns.ID_BORNEREF_FIN.toString()) != null) {
                final BorneDigue b = bornes.get((int) row.getDouble(Columns.ID_BORNEREF_FIN.toString()).doubleValue());
                if (b!=null) {
                    if(ligneEau.getBorneFinId()==null){
                        ligneEau.setBorneFinId(b.getId());
                    }
                    else if(!ligneEau.getBorneFinId().equals(b.getId())){
                        throw new AccessDbImporterException("Inconsistent data.");
                    }
                }
            }
            
            {
                final boolean bfa = row.getBoolean(Columns.AMONT_AVAL_FIN.toString());
                if(nouvelleLigneEau){
                    ligneEau.setBorne_fin_aval(bfa);
                }
//                else if(bda!=desordre.getBorne_debut_aval()){
//                    throw new AccessDbImporterException("Inconsistent data.");
//                }
            }
            
            if (row.getDouble(Columns.DIST_BORNEREF_FIN.toString()) != null) {
                final float v = row.getDouble(Columns.DIST_BORNEREF_FIN.toString()).floatValue();
                if(nouvelleLigneEau){
                    ligneEau.setBorne_fin_distance(v);
                }
//                else if(v!=desordre.getBorne_fin_distance()) {
//                    throw new AccessDbImporterException("Inconsistent data : "+v+" != "+desordre.getBorne_fin_distance()+" (id="+row.getInt(DesordreColumns.ID_DESORDRE.toString()));
//                }
            }
            
            {
                final Date date = row.getDate(Columns.DATE.toString());
                if (date != null) {
                    if(nouvelleLigneEau){
                        ligneEau.setDateMaj(LocalDateTime.parse(date.toString(), dateTimeFormatter));
                    }
                    else if(!ligneEau.getDate().equals(LocalDateTime.parse(date.toString(), dateTimeFormatter))){
                        throw new AccessDbImporterException("Inconsistent data.");
                    }
                }
            }
            
            {
                final String c = cleanNullString(row.getString(Columns.COMMENTAIRE.toString()));
                if (c!=null){
                    if(nouvelleLigneEau){
                        ligneEau.setCommentaire(c);
                    } 
                    else if(!c.equals(ligneEau.getCommentaire())){
                        throw new AccessDbImporterException("Inconsistent data.");
                    }
                }
            }
            
            if(row.getInt(Columns.ID_TYPE_REF_HEAU.toString())!=null){
                final RefReferenceHauteur typeRefHauteur = referenceHauteur.get(row.getInt(Columns.ID_TYPE_REF_HEAU.toString()));
                if(typeRefHauteur!=null){
                    if(ligneEau.getReferenceHauteurId()==null){
                        ligneEau.setReferenceHauteurId(typeRefHauteur.getId());
                    }
                    else if(!ligneEau.getReferenceHauteurId().equals(typeRefHauteur.getId())){
                        throw new AccessDbImporterException("Inconsistent data.");
                    }
                }
            }
            
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                ligneEau.setDateMaj(LocalDateTime.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            
            if (nouvelleLigneEau) {
                
            
                if(mesuresPrz.get(row.getInt(Columns.ID_LIGNE_EAU.toString()))!=null){
                    ligneEau.getMesureId().addAll(mesuresPrz.get(row.getInt(Columns.ID_LIGNE_EAU.toString())));
                }
                
                if(mesuresXyz.get(row.getInt(Columns.ID_LIGNE_EAU.toString()))!=null){
                    ligneEau.getMesureId().addAll(mesuresXyz.get(row.getInt(Columns.ID_LIGNE_EAU.toString())));
                }
                
                // Don't set the old ID, but save it into the dedicated map in order to keep the reference.
                structures.put(row.getInt(Columns.ID_LIGNE_EAU.toString()), ligneEau);

                // Set the list ByTronconId
                List<LigneEau> listByTronconId = structuresByTronconId.get(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
                if (listByTronconId == null) {
                    listByTronconId = new ArrayList<>();
                    structuresByTronconId.put(row.getInt(Columns.ID_TRONCON_GESTION.toString()), listByTronconId);
                }
                listByTronconId.add(ligneEau);
            }
        }
    }
}
