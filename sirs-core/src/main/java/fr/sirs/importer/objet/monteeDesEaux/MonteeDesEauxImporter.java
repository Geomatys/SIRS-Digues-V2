package fr.sirs.importer.objet.monteeDesEaux;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.EvenementHydraulique;
import fr.sirs.core.model.MesureMonteeEaux;
import fr.sirs.core.model.MonteeEaux;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.BorneDigueImporter;
import fr.sirs.importer.DbImporter;
import static fr.sirs.importer.DbImporter.cleanNullString;
import fr.sirs.importer.IntervenantImporter;
import fr.sirs.importer.SystemeReperageImporter;
import fr.sirs.importer.TronconGestionDigueImporter;
import fr.sirs.importer.evenementHydraulique.EvenementHydrauliqueImporter;
import fr.sirs.importer.objet.SourceInfoImporter;
import fr.sirs.importer.objet.TypeRefHeauImporter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
public class MonteeDesEauxImporter extends GenericMonteeDesEauxImporter {
    
    private final MonteeDesEauxMesuresImporter monteeDesEauxMesuresImporter;
    private final SysEvtMonteeDesEauHydroImporter sysEvtMonteeDesEauHydroImporter;

    public MonteeDesEauxImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector, 
            final TronconGestionDigueImporter tronconGestionDigueImporter, 
            final SystemeReperageImporter systemeReperageImporter, 
            final BorneDigueImporter borneDigueImporter, 
            final EvenementHydrauliqueImporter evenementHydrauliqueImporter,
            final IntervenantImporter intervenantImporter,
            final TypeRefHeauImporter typeRefHeauImporter,
            final SourceInfoImporter sourceInfoImporter) {
        super(accessDatabase, couchDbConnector, tronconGestionDigueImporter, 
                systemeReperageImporter, borneDigueImporter, 
                evenementHydrauliqueImporter);
        monteeDesEauxMesuresImporter = new MonteeDesEauxMesuresImporter(
                accessDatabase, couchDbConnector, intervenantImporter, 
                sourceInfoImporter, typeRefHeauImporter);
        sysEvtMonteeDesEauHydroImporter = new SysEvtMonteeDesEauHydroImporter(
                accessDatabase, couchDbConnector, tronconGestionDigueImporter, 
                systemeReperageImporter, borneDigueImporter, 
                evenementHydrauliqueImporter, monteeDesEauxMesuresImporter);
    }

    private enum Columns {
        ID_MONTEE_DES_EAUX,
        ID_EVENEMENT_HYDRAU,
        ID_TRONCON_GESTION,
        PR_CALCULE,
        X,
        Y,
        ID_SYSTEME_REP,
        ID_BORNEREF,
        AMONT_AVAL,
        DIST_BORNEREF,
        COMMENTAIRE,
//        ID_ECHELLE_LIMNI,// Correspondance ?? Référence quelle table ??
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
        return DbImporter.TableName.MONTEE_DES_EAUX.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        
        this.structures = sysEvtMonteeDesEauHydroImporter.getById();
        this.structuresByTronconId = sysEvtMonteeDesEauHydroImporter.getByTronconId();
        
        final Map<Integer, BorneDigue> bornes = borneDigueImporter.getBorneDigue();
        final Map<Integer, TronconDigue> troncons = tronconGestionDigueImporter.getTronconsDigues();
        final Map<Integer, SystemeReperage> systemesReperage = systemeReperageImporter.getSystemeRepLineaire();
        
        final Map<Integer, EvenementHydraulique> evenementsHydrau = evenementHydrauliqueImporter.getEvenementHydraulique();
        
        final Map<Integer, List<MesureMonteeEaux>> mesures = monteeDesEauxMesuresImporter.getMesuresByMonteeDesEaux();
        
        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final MonteeEaux monteeEaux;
            final boolean nouvelleLaisseCrue;
            if(structures.get(row.getInt(Columns.ID_MONTEE_DES_EAUX.toString()))!=null){
                monteeEaux = structures.get(row.getInt(Columns.ID_MONTEE_DES_EAUX.toString()));
                nouvelleLaisseCrue=false;
            }
            else{
                System.out.println("Nouvelle montee des eaux !!");
                monteeEaux = new MonteeEaux();
                nouvelleLaisseCrue=true;
            }
            
            if(row.getInt(Columns.ID_EVENEMENT_HYDRAU.toString())!=null){
                if(nouvelleLaisseCrue){
                    monteeEaux.setEvenementId(evenementsHydrau.get(row.getInt(Columns.ID_EVENEMENT_HYDRAU.toString())).getId());
                }
            }
            
            if (row.getInt(Columns.ID_TRONCON_GESTION.toString()) != null) {
                final TronconDigue troncon = troncons.get(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
                if (troncon.getId() != null && monteeEaux.getTroncon()==null) {
                    monteeEaux.setTroncon(troncon.getId());
                } else if(troncon.getId()==null) {
                    throw new AccessDbImporterException("Le tronçon "
                            + troncons.get(row.getInt(Columns.ID_TRONCON_GESTION.toString())) + " n'a pas encore d'identifiant CouchDb !");
                } else if(!monteeEaux.getTroncon().equals(troncon.getId())){
                    throw new AccessDbImporterException("Inconsistent data.");
                }
            }
            
            if (row.getDouble(Columns.PR_CALCULE.toString()) != null) {
                final float v = row.getDouble(Columns.PR_CALCULE.toString()).floatValue();
                if(nouvelleLaisseCrue){
                    monteeEaux.setPR_debut(v);
                }
//                else if(v!=desordre.getPR_debut()) {
//                    throw new AccessDbImporterException("Inconsistent data : "+v+" != "+desordre.getPR_debut()+" (id="+row.getInt(DesordreColumns.ID_DESORDRE.toString()));
//                }
            }
            
            if (row.getDouble(Columns.PR_CALCULE.toString()) != null) {
                final float v = row.getDouble(Columns.PR_CALCULE.toString()).floatValue();
                if(nouvelleLaisseCrue){
                    monteeEaux.setPR_fin(v);
                }
//                else if(v!=desordre.getPR_fin()) {
//                    throw new AccessDbImporterException("Inconsistent data.");
//                }
            }
            
            GeometryFactory geometryFactory = new GeometryFactory();
            final MathTransform lambertToRGF;
            try {
                lambertToRGF = CRS.findMathTransform(CRS.decode("EPSG:27563"), CRS.decode("EPSG:2154"), true);

                try {

                    if (row.getDouble(Columns.X.toString()) != null 
                            && row.getDouble(Columns.Y.toString()) != null) {
                        final Point positionDebut = (Point) JTS.transform(geometryFactory.createPoint(new Coordinate(
                                row.getDouble(Columns.X.toString()),
                                row.getDouble(Columns.Y.toString()))), lambertToRGF);
                        if(monteeEaux.getPositionDebut()==null){
                            monteeEaux.setPositionDebut(positionDebut);
                        } else if(!monteeEaux.getPositionDebut().equals(positionDebut)){
                            throw new AccessDbImporterException("Inconsistent data.");
                        }
                    }
                } catch (MismatchedDimensionException | TransformException ex) {
                    Logger.getLogger(MonteeDesEauxImporter.class.getName()).log(Level.SEVERE, null, ex);
                }

                try {

                    if (row.getDouble(Columns.X.toString()) != null 
                            && row.getDouble(Columns.Y.toString()) != null) {
                        final Point positionFin = (Point) JTS.transform(geometryFactory.createPoint(new Coordinate(
                                    row.getDouble(Columns.X.toString()),
                                    row.getDouble(Columns.Y.toString()))), lambertToRGF);
                        if(monteeEaux.getPositionFin()==null){
                            monteeEaux.setPositionFin(positionFin);
                        } else if(!monteeEaux.getPositionFin().equals(positionFin)){
                            throw new AccessDbImporterException("Inconsistent data.");
                        }
                    }
                } catch (MismatchedDimensionException | TransformException ex) {
                    Logger.getLogger(MonteeDesEauxImporter.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (FactoryException ex) {
                Logger.getLogger(MonteeDesEauxImporter.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            if(row.getInt(Columns.ID_SYSTEME_REP.toString())!=null){
                final SystemeReperage sr = systemesReperage.get(row.getInt(Columns.ID_SYSTEME_REP.toString()));
                if(sr!=null){
                    if(monteeEaux.getSystemeRepId()==null){
                        monteeEaux.setSystemeRepId(sr.getId());
                    }
                    else if(!monteeEaux.getSystemeRepId().equals(sr.getId())){
                        throw new AccessDbImporterException("Inconsistent data.");
                    }
                }
            }
            
            if (row.getDouble(Columns.ID_BORNEREF.toString()) != null) {
                final BorneDigue b = bornes.get((int) row.getDouble(Columns.ID_BORNEREF.toString()).doubleValue());
                if(b!=null) {
                    if(nouvelleLaisseCrue || monteeEaux.getBorneDebutId()==null){
                        monteeEaux.setBorneDebutId(b.getId());
                    }
                    else if(!monteeEaux.getBorneDebutId().equals(b.getId())){
                        throw new AccessDbImporterException("Inconsistent data.");
                    }
                }
            }
            
            if (row.getDouble(Columns.ID_BORNEREF.toString()) != null) {
                final BorneDigue b = bornes.get((int) row.getDouble(Columns.ID_BORNEREF.toString()).doubleValue());
                if (b!=null) {
                    if(monteeEaux.getBorneFinId()==null){
                        monteeEaux.setBorneFinId(b.getId());
                    }
                    else if(!monteeEaux.getBorneFinId().equals(b.getId())){
                        throw new AccessDbImporterException("Inconsistent data.");
                    }
                }
            }

            {
                final boolean bda = row.getBoolean(Columns.AMONT_AVAL.toString());
                if(nouvelleLaisseCrue){
                    monteeEaux.setBorne_debut_aval(bda); 
                } 
//                else if(bda!=desordre.getBorne_debut_aval()){
//                    throw new AccessDbImporterException("Inconsistent data.");
//                }
            }
            
            {
                final boolean bfa = row.getBoolean(Columns.AMONT_AVAL.toString());
                if(nouvelleLaisseCrue){
                    monteeEaux.setBorne_fin_aval(bfa);
                }
//                else if(bda!=desordre.getBorne_debut_aval()){
//                    throw new AccessDbImporterException("Inconsistent data.");
//                }
            }
            
            if (row.getDouble(Columns.DIST_BORNEREF.toString()) != null) {
                final float v = row.getDouble(Columns.DIST_BORNEREF.toString()).floatValue();
                if(nouvelleLaisseCrue){
                    monteeEaux.setBorne_debut_distance(v);
                }
//                else if(v!=desordre.getBorne_debut_distance()) {
//                    throw new AccessDbImporterException("Inconsistent data : "+v+" != "+desordre.getBorne_debut_distance()+" (id="+row.getInt(DesordreColumns.ID_DESORDRE.toString()));
//                }
            }
            
            if (row.getDouble(Columns.DIST_BORNEREF.toString()) != null) {
                final float v = row.getDouble(Columns.DIST_BORNEREF.toString()).floatValue();
                if(nouvelleLaisseCrue){
                    monteeEaux.setBorne_fin_distance(v);
                }
//                else if(v!=desordre.getBorne_fin_distance()) {
//                    throw new AccessDbImporterException("Inconsistent data : "+v+" != "+desordre.getBorne_fin_distance()+" (id="+row.getInt(DesordreColumns.ID_DESORDRE.toString()));
//                }
            }
            
            {
                final String c = cleanNullString(row.getString(Columns.COMMENTAIRE.toString()));
                if (c!=null){
                    if(nouvelleLaisseCrue){
                        monteeEaux.setCommentaire(c);
                    } 
                    else if(!c.equals(monteeEaux.getCommentaire())){
                        throw new AccessDbImporterException("Inconsistent data.");
                    }
                }
            }
            
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                monteeEaux.setDateMaj(LocalDateTime.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            
            if (nouvelleLaisseCrue) {
                
                if(mesures.get(row.getInt(Columns.ID_MONTEE_DES_EAUX.toString()))!=null){
                    monteeEaux.setMesureId(mesures.get(row.getInt(Columns.ID_MONTEE_DES_EAUX.toString())));
                }
                
                // Don't set the old ID, but save it into the dedicated map in order to keep the reference.
                structures.put(row.getInt(Columns.ID_MONTEE_DES_EAUX.toString()), monteeEaux);

                // Set the list ByTronconId
                List<MonteeEaux> listByTronconId = structuresByTronconId.get(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
                if (listByTronconId == null) {
                    listByTronconId = new ArrayList<>();
                    structuresByTronconId.put(row.getInt(Columns.ID_TRONCON_GESTION.toString()), listByTronconId);
                }
                listByTronconId.add(monteeEaux);
            }
        }
    }
}
