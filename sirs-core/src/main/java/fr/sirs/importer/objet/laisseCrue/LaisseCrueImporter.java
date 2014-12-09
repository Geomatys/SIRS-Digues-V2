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
public class LaisseCrueImporter extends GenericLaisseCrueImporter {
    
    private final SysEvtLaisseCrueImporter sysEvtLaisseCrueImporter;

    public LaisseCrueImporter(final Database accessDatabase,
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
        sysEvtLaisseCrueImporter = new SysEvtLaisseCrueImporter(
                accessDatabase, couchDbConnector, tronconGestionDigueImporter, 
                systemeReperageImporter, borneDigueImporter,
                intervenantImporter, evenementHydrauliqueImporter,
                typeSourceImporter, typeRefHeauImporter);
    }

    private enum Columns {
        ID_LAISSE_CRUE,
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
        COMMENTAIRE,
        DATE,
        ID_TYPE_REF_HEAU,
        HAUTEUR_EAU,
        ID_INTERV_OBSERVATEUR,
        ID_SOURCE,
        POSITION,
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
        return DbImporter.TableName.LAISSE_CRUE.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        
        this.structures = sysEvtLaisseCrueImporter.getById();
        this.structuresByTronconId = sysEvtLaisseCrueImporter.getByTronconId();
        
        final Map<Integer, BorneDigue> bornes = borneDigueImporter.getBorneDigue();
        final Map<Integer, TronconDigue> troncons = tronconGestionDigueImporter.getTronconsDigues();
        final Map<Integer, SystemeReperage> systemesReperage = systemeReperageImporter.getSystemeRepLineaire();
        
        final Map<Integer, EvenementHydraulique> evenementsHydrau = evenementHydrauliqueImporter.getEvenementHydraulique();
        final Map<Integer, Contact> intervenants = intervenantImporter.getIntervenants();
        final Map<Integer, RefSource> typesSource = sourceInfoImporter.getTypes();
        
        
        final Map<Integer, RefReferenceHauteur> referenceHauteur = typeRefHeauImporter.getTypes();
        
        
        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final LaisseCrue laisseCrue;
            final boolean nouvelleLaisseCrue;
            if(structures.get(row.getInt(Columns.ID_LAISSE_CRUE.toString()))!=null){
                laisseCrue = structures.get(row.getInt(Columns.ID_LAISSE_CRUE.toString()));
                nouvelleLaisseCrue=false;
            }
            else{
                System.out.println("Nouvelle laisse crue !!");
                laisseCrue = new LaisseCrue();
                nouvelleLaisseCrue=true;
            }
            
            if(row.getInt(Columns.ID_EVENEMENT_HYDRAU.toString())!=null){
                if(nouvelleLaisseCrue){
                    laisseCrue.setEvenementId(evenementsHydrau.get(row.getInt(Columns.ID_EVENEMENT_HYDRAU.toString())).getId());
                }
            }
            
            if (row.getInt(Columns.ID_TRONCON_GESTION.toString()) != null) {
                final TronconDigue troncon = troncons.get(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
                if (troncon.getId() != null && laisseCrue.getTroncon()==null) {
                    laisseCrue.setTroncon(troncon.getId());
                } else if(troncon.getId()==null) {
                    throw new AccessDbImporterException("Le tronçon "
                            + troncons.get(row.getInt(Columns.ID_TRONCON_GESTION.toString())) + " n'a pas encore d'identifiant CouchDb !");
                } else if(!laisseCrue.getTroncon().equals(troncon.getId())){
                    throw new AccessDbImporterException("Inconsistent data.");
                }
            }
            
            if (row.getDouble(Columns.PR_DEBUT_CALCULE.toString()) != null) {
                final float v = row.getDouble(Columns.PR_DEBUT_CALCULE.toString()).floatValue();
                if(nouvelleLaisseCrue){
                    laisseCrue.setPR_debut(v);
                }
//                else if(v!=desordre.getPR_debut()) {
//                    throw new AccessDbImporterException("Inconsistent data : "+v+" != "+desordre.getPR_debut()+" (id="+row.getInt(DesordreColumns.ID_DESORDRE.toString()));
//                }
            }
            
            if (row.getDouble(Columns.PR_FIN_CALCULE.toString()) != null) {
                final float v = row.getDouble(Columns.PR_FIN_CALCULE.toString()).floatValue();
                if(nouvelleLaisseCrue){
                    laisseCrue.setPR_fin(v);
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

                    if (row.getDouble(Columns.X_DEBUT.toString()) != null 
                            && row.getDouble(Columns.Y_DEBUT.toString()) != null) {
                        final Point positionDebut = (Point) JTS.transform(geometryFactory.createPoint(new Coordinate(
                                row.getDouble(Columns.X_DEBUT.toString()),
                                row.getDouble(Columns.Y_DEBUT.toString()))), lambertToRGF);
                        if(laisseCrue.getPositionDebut()==null){
                            laisseCrue.setPositionDebut(positionDebut);
                        } else if(!laisseCrue.getPositionDebut().equals(positionDebut)){
                            throw new AccessDbImporterException("Inconsistent data.");
                        }
                    }
                } catch (MismatchedDimensionException | TransformException ex) {
                    Logger.getLogger(LaisseCrueImporter.class.getName()).log(Level.SEVERE, null, ex);
                }

                try {

                    if (row.getDouble(Columns.X_FIN.toString()) != null 
                            && row.getDouble(Columns.Y_FIN.toString()) != null) {
                        final Point positionFin = (Point) JTS.transform(geometryFactory.createPoint(new Coordinate(
                                    row.getDouble(Columns.X_FIN.toString()),
                                    row.getDouble(Columns.Y_FIN.toString()))), lambertToRGF);
                        if(laisseCrue.getPositionFin()==null){
                            laisseCrue.setPositionFin(positionFin);
                        } else if(!laisseCrue.getPositionFin().equals(positionFin)){
                            throw new AccessDbImporterException("Inconsistent data.");
                        }
                    }
                } catch (MismatchedDimensionException | TransformException ex) {
                    Logger.getLogger(LaisseCrueImporter.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (FactoryException ex) {
                Logger.getLogger(LaisseCrueImporter.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            if(row.getInt(Columns.ID_SYSTEME_REP.toString())!=null){
                final SystemeReperage sr = systemesReperage.get(row.getInt(Columns.ID_SYSTEME_REP.toString()));
                if(sr!=null){
                    if(laisseCrue.getSystemeRepId()==null){
                        laisseCrue.setSystemeRepId(sr.getId());
                    }
                    else if(!laisseCrue.getSystemeRepId().equals(sr.getId())){
                        throw new AccessDbImporterException("Inconsistent data.");
                    }
                }
            }
            
            if (row.getDouble(Columns.ID_BORNEREF_DEBUT.toString()) != null) {
                final BorneDigue b = bornes.get((int) row.getDouble(Columns.ID_BORNEREF_DEBUT.toString()).doubleValue());
                if(b!=null) {
                    if(nouvelleLaisseCrue || laisseCrue.getBorneDebutId()==null){
                        laisseCrue.setBorneDebutId(b.getId());
                    }
                    else if(!laisseCrue.getBorneDebutId().equals(b.getId())){
                        throw new AccessDbImporterException("Inconsistent data.");
                    }
                }
            }

            {
                final boolean bda = row.getBoolean(Columns.AMONT_AVAL_DEBUT.toString());
                if(nouvelleLaisseCrue){
                    laisseCrue.setBorne_debut_aval(bda); 
                } 
//                else if(bda!=desordre.getBorne_debut_aval()){
//                    throw new AccessDbImporterException("Inconsistent data.");
//                }
            }
            
            if (row.getDouble(Columns.DIST_BORNEREF_DEBUT.toString()) != null) {
                final float v = row.getDouble(Columns.DIST_BORNEREF_DEBUT.toString()).floatValue();
                if(nouvelleLaisseCrue){
                    laisseCrue.setBorne_debut_distance(v);
                }
//                else if(v!=desordre.getBorne_debut_distance()) {
//                    throw new AccessDbImporterException("Inconsistent data : "+v+" != "+desordre.getBorne_debut_distance()+" (id="+row.getInt(DesordreColumns.ID_DESORDRE.toString()));
//                }
            }
            
            if (row.getDouble(Columns.ID_BORNEREF_FIN.toString()) != null) {
                final BorneDigue b = bornes.get((int) row.getDouble(Columns.ID_BORNEREF_FIN.toString()).doubleValue());
                if (b!=null) {
                    if(laisseCrue.getBorneFinId()==null){
                        laisseCrue.setBorneFinId(b.getId());
                    }
                    else if(!laisseCrue.getBorneFinId().equals(b.getId())){
                        throw new AccessDbImporterException("Inconsistent data.");
                    }
                }
            }
            
            {
                final boolean bfa = row.getBoolean(Columns.AMONT_AVAL_FIN.toString());
                if(nouvelleLaisseCrue){
                    laisseCrue.setBorne_fin_aval(bfa);
                }
//                else if(bda!=desordre.getBorne_debut_aval()){
//                    throw new AccessDbImporterException("Inconsistent data.");
//                }
            }
            
            if (row.getDouble(Columns.DIST_BORNEREF_FIN.toString()) != null) {
                final float v = row.getDouble(Columns.DIST_BORNEREF_FIN.toString()).floatValue();
                if(nouvelleLaisseCrue){
                    laisseCrue.setBorne_fin_distance(v);
                }
//                else if(v!=desordre.getBorne_fin_distance()) {
//                    throw new AccessDbImporterException("Inconsistent data : "+v+" != "+desordre.getBorne_fin_distance()+" (id="+row.getInt(DesordreColumns.ID_DESORDRE.toString()));
//                }
            }
            
            {
                final String c = cleanNullString(row.getString(Columns.COMMENTAIRE.toString()));
                if (c!=null){
                    if(nouvelleLaisseCrue){
                        laisseCrue.setCommentaire(c);
                    } 
                    else if(!c.equals(laisseCrue.getCommentaire())){
                        throw new AccessDbImporterException("Inconsistent data.");
                    }
                }
            }
            
            
            {
                final Date date = row.getDate(Columns.DATE.toString());
                if (date != null) {
                    if(nouvelleLaisseCrue){
                        laisseCrue.setDateMaj(LocalDateTime.parse(date.toString(), dateTimeFormatter));
                    }
                    else if(!laisseCrue.getDate().equals(LocalDateTime.parse(date.toString(), dateTimeFormatter))){
                        throw new AccessDbImporterException("Inconsistent data.");
                    }
                }
            }
            
            if(row.getInt(Columns.ID_TYPE_REF_HEAU.toString())!=null){
                final RefReferenceHauteur typeRefHauteur = referenceHauteur.get(row.getInt(Columns.ID_TYPE_REF_HEAU.toString()));
                if(typeRefHauteur!=null){
                    if(laisseCrue.getReferenceHauteurId()==null){
                        laisseCrue.setReferenceHauteurId(typeRefHauteur.getId());
                    }
                    else if(!laisseCrue.getReferenceHauteurId().equals(typeRefHauteur.getId())){
                        throw new AccessDbImporterException("Inconsistent data.");
                    }
                }
            }
            
            if (row.getDouble(Columns.HAUTEUR_EAU.toString()) != null) {
                final float h = row.getDouble(Columns.HAUTEUR_EAU.toString()).floatValue();
                if(nouvelleLaisseCrue){
                    laisseCrue.setHauteur(h);
                }
                else if(h!=laisseCrue.getHauteur()) {
                    throw new AccessDbImporterException("Inconsistent data.");
                }
            }
            
            if(row.getInt(Columns.ID_INTERV_OBSERVATEUR.toString())!=null){
                final Contact obs = intervenants.get(row.getInt(Columns.ID_INTERV_OBSERVATEUR.toString()));
                if(obs!=null){
                    if(laisseCrue.getObservateurId()==null){
                        laisseCrue.setObservateurId(obs.getId());
                    }
                    else if(!laisseCrue.getObservateurId().equals(obs.getId())){
                        throw new AccessDbImporterException("Inconsistent data.");
                    }
                }
            }
            
            if(row.getInt(Columns.ID_SOURCE.toString())!=null){
                final RefSource typeSource = typesSource.get(row.getInt(Columns.ID_SOURCE.toString()));
                if(typeSource!=null){
                    if(laisseCrue.getSourceId()==null){
                        laisseCrue.setSourceId(typeSource.getId());
                    }
                    else if(!laisseCrue.getSourceId().equals(typeSource.getId())){
                        throw new AccessDbImporterException("Inconsistent data.");
                    }
                }
            }
            
            {
                final String p = cleanNullString(row.getString(Columns.POSITION.toString()));
                if (p!=null){
                    if(nouvelleLaisseCrue){
                        laisseCrue.setPosition_laisse(p);
                    } 
                    else if(!p.equals(laisseCrue.getPosition_laisse())){
                        throw new AccessDbImporterException("Inconsistent data.");
                    }
                }
            }
            
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                laisseCrue.setDateMaj(LocalDateTime.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            
            if (nouvelleLaisseCrue) {
                
                // Don't set the old ID, but save it into the dedicated map in order to keep the reference.
                structures.put(row.getInt(Columns.ID_LAISSE_CRUE.toString()), laisseCrue);

                // Set the list ByTronconId
                List<LaisseCrue> listByTronconId = structuresByTronconId.get(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
                if (listByTronconId == null) {
                    listByTronconId = new ArrayList<>();
                    structuresByTronconId.put(row.getInt(Columns.ID_TRONCON_GESTION.toString()), listByTronconId);
                }
                listByTronconId.add(laisseCrue);
            }
        }
    }
}
