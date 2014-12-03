package fr.sirs.importer.objet.desordre;

import fr.sirs.importer.objet.TypePositionImporter;
import fr.sirs.importer.objet.TypeCoteImporter;
import fr.sirs.importer.objet.SourceInfoImporter;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.BorneDigueImporter;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.SystemeReperageImporter;
import fr.sirs.importer.TronconGestionDigueImporter;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.Desordre;
import fr.sirs.core.model.RefCote;
import fr.sirs.core.model.RefPosition;
import fr.sirs.core.model.RefSource;
import fr.sirs.core.model.RefTypeDesordre;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.importer.objet.structure.ElementStructureImporter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
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
public class DesordreImporter extends GenericDesordreImporter {
    
    private final TypeDesordreImporter typeDesordreImporter;
    private final SysEvtDesordreImporter sysEvtDesordreImporter;

    public DesordreImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector, 
            final TronconGestionDigueImporter tronconGestionDigueImporter, 
            final SystemeReperageImporter systemeReperageImporter, 
            final BorneDigueImporter borneDigueImporter, 
            final SourceInfoImporter typeSourceImporter,
            final TypePositionImporter typePositionImporter,
            final TypeCoteImporter typeCoteImporter) {
        super(accessDatabase, couchDbConnector, tronconGestionDigueImporter, 
                systemeReperageImporter, borneDigueImporter, typeSourceImporter, 
                typeCoteImporter, typePositionImporter);
        this.typeDesordreImporter = new TypeDesordreImporter(accessDatabase, 
                couchDbConnector);
        this.sysEvtDesordreImporter = new SysEvtDesordreImporter(accessDatabase, 
                couchDbConnector, tronconGestionDigueImporter, 
                systemeReperageImporter, borneDigueImporter, typeSourceImporter, 
                typePositionImporter, typeCoteImporter, typeDesordreImporter);
    }

    private enum Columns {
        ID_DESORDRE,
        ID_TYPE_DESORDRE,
        ID_TYPE_COTE,
        ID_SOURCE,
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
//        COMMENTAIRE, // Apparemment bsolète voir le champ DESCRIPTION_DESORDRE
        LIEU_DIT_DESORDRE,
        ID_TYPE_POSITION,
//        ID_PRESTATION, // La colonne est vide dans la base de l'Isère. Il s'agit visiblement d'une colonne obsolète remplacée par la table d'association DESORDRE_PRESTATION
//        ID_CRUE, // La colonne est vide dans la base de l'Isère. Il s'agit visiblement d'une colonne obsolète remplacée par la table d'association DESORDRE_EVENEMENT_HYDRAULIQUE
        DESCRIPTION_DESORDRE,
//        DISPARU,
//        DEJA_OBSERVE,
        DATE_DERNIERE_MAJ
    };

    @Override
    public String getTableName() {
        return DbImporter.TableName.DESORDRE.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {

        structures = sysEvtDesordreImporter.getById();
        structuresByTronconId = sysEvtDesordreImporter.getByTronconId();
        
        final Map<Integer, BorneDigue> bornes = borneDigueImporter.getBorneDigue();
        final Map<Integer, TronconDigue> troncons = tronconGestionDigueImporter.getTronconsDigues();
        final Map<Integer, SystemeReperage> systemesReperage = systemeReperageImporter.getSystemeRepLineaire();
        
        final Map<Integer, RefSource> typesSource = typeSourceImporter.getTypes();
        final Map<Integer, RefPosition> typesPosition = typePositionImporter.getTypes();
        final Map<Integer, RefCote> typesCote = typeCoteImporter.getTypes();
        
        final Map<Integer, RefTypeDesordre> typesDesordre = typeDesordreImporter.getTypes();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final Desordre desordre;
            final boolean nouveauDesordre;
            if(structures.get(row.getInt(Columns.ID_DESORDRE.toString()))!=null){
                desordre = structures.get(row.getInt(Columns.ID_DESORDRE.toString()));
                nouveauDesordre=false;
            }
            else{
                System.out.println("Nouveau désordre !!");
                desordre = new Desordre();
                nouveauDesordre=true;
            }
            
            if (row.getDouble(Columns.ID_BORNEREF_DEBUT.toString()) != null) {
                final BorneDigue b = bornes.get((int) row.getDouble(Columns.ID_BORNEREF_DEBUT.toString()).doubleValue());
                if(b!=null) {
                    if(nouveauDesordre || desordre.getBorneDebutId()==null){
                        desordre.setBorneDebutId(b.getId());
                    }
                    else if(!desordre.getBorneDebutId().equals(b.getId())){
                        throw new AccessDbImporterException("Inconsistent data.");
                    }
                }
            }
            
            if (row.getDouble(Columns.DIST_BORNEREF_DEBUT.toString()) != null) {
                final float v = row.getDouble(Columns.DIST_BORNEREF_DEBUT.toString()).floatValue();
                if(nouveauDesordre){
                    desordre.setBorne_debut_distance(v);
                }
//                else if(v!=desordre.getBorne_debut_distance()) {
//                    throw new AccessDbImporterException("Inconsistent data : "+v+" != "+desordre.getBorne_debut_distance()+" (id="+row.getInt(DesordreColumns.ID_DESORDRE.toString()));
//                }
            }
            
            if (row.getDouble(Columns.PR_DEBUT_CALCULE.toString()) != null) {
                final float v = row.getDouble(Columns.PR_DEBUT_CALCULE.toString()).floatValue();
                if(nouveauDesordre){
                    desordre.setPR_debut(v);
                }
//                else if(v!=desordre.getPR_debut()) {
//                    throw new AccessDbImporterException("Inconsistent data : "+v+" != "+desordre.getPR_debut()+" (id="+row.getInt(DesordreColumns.ID_DESORDRE.toString()));
//                }
            }
            
            if (row.getDouble(Columns.ID_BORNEREF_FIN.toString()) != null) {
                final BorneDigue b = bornes.get((int) row.getDouble(Columns.ID_BORNEREF_FIN.toString()).doubleValue());
                if (b!=null) {
                    if(desordre.getBorneFinId()==null){
                        desordre.setBorneFinId(b.getId());
                    }
                    else if(!desordre.getBorneFinId().equals(b.getId())){
                        throw new AccessDbImporterException("Inconsistent data.");
                    }
                }
            }
            
            if (row.getDouble(Columns.DIST_BORNEREF_FIN.toString()) != null) {
                final float v = row.getDouble(Columns.DIST_BORNEREF_FIN.toString()).floatValue();
                if(nouveauDesordre){
                    desordre.setBorne_fin_distance(v);
                }
//                else if(v!=desordre.getBorne_fin_distance()) {
//                    throw new AccessDbImporterException("Inconsistent data : "+v+" != "+desordre.getBorne_fin_distance()+" (id="+row.getInt(DesordreColumns.ID_DESORDRE.toString()));
//                }
            }
            
            if (row.getDouble(Columns.PR_FIN_CALCULE.toString()) != null) {
                final float v = row.getDouble(Columns.PR_FIN_CALCULE.toString()).floatValue();
                if(nouveauDesordre){
                    desordre.setPR_fin(v);
                }
//                else if(v!=desordre.getPR_fin()) {
//                    throw new AccessDbImporterException("Inconsistent data.");
//                }
            }
            
            if(row.getInt(Columns.ID_SYSTEME_REP.toString())!=null){
                final SystemeReperage sr = systemesReperage.get(row.getInt(Columns.ID_SYSTEME_REP.toString()));
                if(sr!=null){
                    if(desordre.getSystemeRepId()==null){
                        desordre.setSystemeRepId(sr.getId());
                    }
                    else if(!desordre.getSystemeRepId().equals(sr.getId())){
                        throw new AccessDbImporterException("Inconsistent data.");
                    }
                }
            }

            {
                final boolean bda = row.getBoolean(Columns.AMONT_AVAL_DEBUT.toString());
                if(nouveauDesordre){
                    desordre.setBorne_debut_aval(bda); 
                } 
//                else if(bda!=desordre.getBorne_debut_aval()){
//                    throw new AccessDbImporterException("Inconsistent data.");
//                }
            }
            
            {
                final boolean bfa = row.getBoolean(Columns.AMONT_AVAL_FIN.toString());
                if(nouveauDesordre){
                    desordre.setBorne_fin_aval(bfa);
                }
//                else if(bda!=desordre.getBorne_debut_aval()){
//                    throw new AccessDbImporterException("Inconsistent data.");
//                }
            }
            
            {
                final String ld = row.getString(Columns.LIEU_DIT_DESORDRE.toString());
                if (ld != null) {
                    if (nouveauDesordre) {
                        desordre.setLieu_dit(ld);
                    } else if (!ld.equals(desordre.getLieu_dit())) {
                        throw new AccessDbImporterException("Inconsistent data.");
                    }
                }
            }
        
            if(row.getInt(Columns.ID_TYPE_DESORDRE.toString())!=null){
                final RefTypeDesordre typeDesordre = typesDesordre.get(row.getInt(Columns.ID_TYPE_DESORDRE.toString()));
                if(typeDesordre!=null){
                    if(desordre.getTypeDesordreId()==null){
                        desordre.setTypeDesordreId(typeDesordre.getId());
                    }
                }
            }
            
            if(row.getInt(Columns.ID_SOURCE.toString())!=null){
                final RefSource typeSource = typesSource.get(row.getInt(Columns.ID_SOURCE.toString()));
                if(typeSource!=null){
                    if(desordre.getSourceId()==null){
                        desordre.setSourceId(typeSource.getId());
                    }
                    else if(!desordre.getSourceId().equals(typeSource.getId())){
                        throw new AccessDbImporterException("Inconsistent data.");
                    }
                }
            }
            
            if(row.getInt(Columns.ID_TYPE_POSITION.toString())!=null){
                final RefPosition typePosition = typesPosition.get(row.getInt(Columns.ID_TYPE_POSITION.toString()));
                if(typePosition!=null){
                    if(desordre.getPosition_structure()==null){
                        desordre.setPosition_structure(typePosition.getId());
                    }
                }
            }
            
            if(row.getInt(Columns.ID_TYPE_COTE.toString())!=null){
                final RefCote typeCote = typesCote.get(row.getInt(Columns.ID_TYPE_COTE.toString()));
                if(typeCote!=null){
                    if(desordre.getCoteId()==null){
                        desordre.setCoteId(typeCote.getId());
                    }
                }
            }
            
            if (row.getInt(Columns.ID_TRONCON_GESTION.toString()) != null) {
                final TronconDigue troncon = troncons.get(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
                if (troncon.getId() != null && desordre.getTroncon()==null) {
                    desordre.setTroncon(troncon.getId());
                } else if(troncon.getId()==null) {
                    throw new AccessDbImporterException("Le tronçon "
                            + troncons.get(row.getInt(Columns.ID_TRONCON_GESTION.toString())) + " n'a pas encore d'identifiant CouchDb !");
                } else if(!desordre.getTroncon().equals(troncon.getId())){
                    throw new AccessDbImporterException("Inconsistent data.");
                }
            }
            
            if (row.getDate(Columns.DATE_DEBUT_VAL.toString()) != null) {
                final Date date = row.getDate(Columns.DATE_DEBUT_VAL.toString());
                if(date!=null){
                    final LocalDateTime localDate = LocalDateTime.parse(date.toString(), dateTimeFormatter);
                    if(desordre.getDate_debut()==null){
                        desordre.setDate_debut(localDate);
                    }else if(!desordre.getDate_debut().equals(localDate)){
                        throw new AccessDbImporterException("Inconsistent data.");
                    }
                }
            }
            
            if (row.getDate(Columns.DATE_FIN_VAL.toString()) != null) {
                try {
                    final Date date = row.getDate(Columns.DATE_FIN_VAL.toString());
                    if (date != null) {
//                        System.out.println("desordre Id : "+row.getInt(DesordreColumns.ID_DESORDRE.toString()));
                        final LocalDateTime localDate = LocalDateTime.parse(date.toString(), dateTimeFormatter);
                        if (desordre.getDate_fin() == null) {
                            desordre.setDate_fin(localDate);
                        } else if (!desordre.getDate_fin().equals(localDate)) {
                            throw new AccessDbImporterException("Inconsistent data.");
                        }
                    }
                } catch (DateTimeParseException e) {
                    System.out.println(e.getMessage());
                }
            }
            
            {
                final String commentaire = row.getString(Columns.DESCRIPTION_DESORDRE.toString());
                if(commentaire!=null){
                    if(desordre.getCommentaire()==null){
                        desordre.setCommentaire(commentaire);
                    } 
//                    else if(!desordre.getCommentaire().equals(commentaire)){ 
// Les commentaires peuvent différer pour les désordres (ex : désordre 325 de l'Isère
//                        System.out.println(row.getInt(Columns.ID_DESORDRE.toString()));
//                        throw new AccessDbImporterException("Inconsistent data.");
//                    }
                }
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
                        if(desordre.getPositionDebut()==null){
                            desordre.setPositionDebut(positionDebut);
                        } else if(!desordre.getPositionDebut().equals(positionDebut)){
                            throw new AccessDbImporterException("Inconsistent data.");
                        }
                    }
                } catch (MismatchedDimensionException | TransformException ex) {
                    Logger.getLogger(DesordreImporter.class.getName()).log(Level.SEVERE, null, ex);
                }

                try {

                    if (row.getDouble(Columns.X_FIN.toString()) != null 
                            && row.getDouble(Columns.Y_FIN.toString()) != null) {
                        final Point positionFin = (Point) JTS.transform(geometryFactory.createPoint(new Coordinate(
                                    row.getDouble(Columns.X_FIN.toString()),
                                    row.getDouble(Columns.Y_FIN.toString()))), lambertToRGF);
                        if(desordre.getPositionFin()==null){
                            desordre.setPositionFin(positionFin);
                        } else if(!desordre.getPositionFin().equals(positionFin)){
                            throw new AccessDbImporterException("Inconsistent data.");
                        }
                    }
                } catch (MismatchedDimensionException | TransformException ex) {
                    Logger.getLogger(DesordreImporter.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (FactoryException ex) {
                Logger.getLogger(DesordreImporter.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                desordre.setDateMaj(LocalDateTime.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            
            
            
            
            if (nouveauDesordre) {
                
                // Don't set the old ID, but save it into the dedicated map in order to keep the reference.
                structures.put(row.getInt(Columns.ID_DESORDRE.toString()), desordre);

                // Set the list ByTronconId
                List<Desordre> listByTronconId = structuresByTronconId.get(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
                if (listByTronconId == null) {
                    listByTronconId = new ArrayList<>();
                    structuresByTronconId.put(row.getInt(Columns.ID_TRONCON_GESTION.toString()), listByTronconId);
                }
                listByTronconId.add(desordre);
            }
        }
    }

    @Override
    protected List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (Columns c : Columns.values()) {
            columns.add(c.toString());
        }
        return columns;
    }
}
