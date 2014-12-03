package fr.sirs.importer.objet.prestation;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.Prestation;
import fr.sirs.core.model.RefCote;
import fr.sirs.core.model.RefPosition;
import fr.sirs.core.model.RefPrestation;
import fr.sirs.core.model.RefSource;
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
import fr.sirs.importer.objet.SourceInfoImporter;
import fr.sirs.importer.objet.TypeCoteImporter;
import fr.sirs.importer.objet.TypeFonctionImporter;
import fr.sirs.importer.objet.TypeMateriauImporter;
import fr.sirs.importer.objet.TypeNatureImporter;
import fr.sirs.importer.objet.TypePositionImporter;
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
public class PrestationImporter extends GenericPrestationImporter<Prestation> {
    
    private final TypePrestationImporter typePrestationImporter;
    private final SysEvtPrestationImporter sysEvtPrestationImporter;

    public PrestationImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector, 
            final TronconGestionDigueImporter tronconGestionDigueImporter, 
            final SystemeReperageImporter systemeReperageImporter, 
            final BorneDigueImporter borneDigueImporter, 
            final OrganismeImporter organismeImporter,
            final IntervenantImporter intervenantImporter,
            final ElementStructureImporter structureImporter, 
            final SourceInfoImporter typeSourceImporter,
            final TypePositionImporter typePositionImporter,
            final TypeCoteImporter typeCoteImporter,
            final TypeMateriauImporter typeMateriauImporter, 
            final TypeNatureImporter typeNatureImporter, 
            final TypeFonctionImporter typeFonctionImporter) {
        super(accessDatabase, couchDbConnector, tronconGestionDigueImporter, 
                systemeReperageImporter, borneDigueImporter, organismeImporter, 
                intervenantImporter, typeSourceImporter, typeCoteImporter, 
                typePositionImporter, typeMateriauImporter, typeNatureImporter, 
                typeFonctionImporter);
        this.typePrestationImporter = new TypePrestationImporter(accessDatabase, 
                couchDbConnector);
        this.sysEvtPrestationImporter = new SysEvtPrestationImporter(accessDatabase, 
                couchDbConnector, tronconGestionDigueImporter, 
                systemeReperageImporter, borneDigueImporter, organismeImporter,
                intervenantImporter, structureImporter, typePrestationImporter, 
                typeSourceImporter, typePositionImporter, typeCoteImporter, 
                typeMateriauImporter, typeNatureImporter, typeFonctionImporter);
    }

    private enum Columns {
        ID_PRESTATION,
        ID_TRONCON_GESTION,
        LIBELLE_PRESTATION,
//        ID_MARCHE,
        REALISATION_INTERNE,
        ID_TYPE_PRESTATION,
        COUT_AU_METRE,
        COUT_GLOBAL,
        ID_TYPE_COTE,
        ID_TYPE_POSITION,
//        ID_INTERV_REALISATEUR, // Ne sert à rien : voir la table PRESTATION_INTERVENANT
        DESCRIPTION_PRESTATION,
        DATE_DEBUT_VAL,
        DATE_FIN_VAL,
        ID_SOURCE,
        PR_DEBUT_CALCULE,
        PR_FIN_CALCULE,
        X_DEBUT,
        Y_DEBUT,
        X_FIN,
        Y_FIN,
        ID_BORNEREF_DEBUT,
        ID_BORNEREF_FIN,
        ID_SYSTEME_REP,
        DIST_BORNEREF_DEBUT,
        DIST_BORNEREF_FIN,
        AMONT_AVAL_DEBUT,
        AMONT_AVAL_FIN,
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
        return DbImporter.TableName.PRESTATION.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        
        this.structures = sysEvtPrestationImporter.getById();
        this.structuresByTronconId = sysEvtPrestationImporter.getByTronconId();
        
        final Map<Integer, BorneDigue> bornes = borneDigueImporter.getBorneDigue();
        final Map<Integer, TronconDigue> troncons = tronconGestionDigueImporter.getTronconsDigues();
        final Map<Integer, SystemeReperage> systemesReperage = systemeReperageImporter.getSystemeRepLineaire();
        final Map<Integer, RefPrestation> typesPrestation = typePrestationImporter.getTypes();
        final Map<Integer, RefSource> typesSource = typeSourceImporter.getTypes();
        final Map<Integer, RefPosition> typesPosition = typePositionImporter.getTypes();
        final Map<Integer, RefCote> typesCote = typeCoteImporter.getTypes();
        
        
        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final Prestation prestation;
            final boolean nouvellePrestation;
            if(structures.get(row.getInt(Columns.ID_PRESTATION.toString()))!=null){
                prestation = structures.get(row.getInt(Columns.ID_PRESTATION.toString()));
                nouvellePrestation=false;
            }
            else{
                System.out.println("Nouvelle prestation !!");
                prestation = new Prestation();
                nouvellePrestation=true;
            }
            
            if (row.getInt(Columns.ID_TRONCON_GESTION.toString()) != null) {
                final TronconDigue troncon = troncons.get(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
                if (troncon.getId() != null && prestation.getTroncon()==null) {
                    prestation.setTroncon(troncon.getId());
                } else if(troncon.getId()==null) {
                    throw new AccessDbImporterException("Le tronçon "
                            + troncons.get(row.getInt(Columns.ID_TRONCON_GESTION.toString())) + " n'a pas encore d'identifiant CouchDb !");
                } else if(!prestation.getTroncon().equals(troncon.getId())){
                    throw new AccessDbImporterException("Inconsistent data.");
                }
            }
            
            {
                final String l = cleanNullString(row.getString(Columns.LIBELLE_PRESTATION.toString()));
                if (l!=null){
                    if(nouvellePrestation){
                        prestation.setLibelle(l);
                    } 
                    else if(!l.equals(prestation.getLibelle())){
                        throw new AccessDbImporterException("Inconsistent data.");
                    }
                }
            }
            
            {
                final boolean realisation = row.getBoolean(Columns.REALISATION_INTERNE.toString());
                    if(nouvellePrestation){
                        prestation.setRealisation_interne(realisation);
                    }
            }
            
            if (row.getInt(Columns.ID_TYPE_PRESTATION.toString()) != null) {
                final RefPrestation typePrestation = typesPrestation.get(row.getInt(Columns.ID_TYPE_PRESTATION.toString()).toString());
                if(typePrestation!=null){
                    if(prestation.getTypePrestationId()==null){
                        prestation.setTypePrestationId(typePrestation.getId());
                    }
                }
            }
            
            if (row.getDouble(Columns.COUT_AU_METRE.toString()) != null) {
                prestation.setCout_metre(row.getDouble(Columns.COUT_AU_METRE.toString()).floatValue());
            }
            
            if (row.getDouble(Columns.COUT_GLOBAL.toString()) != null) {
                prestation.setCout_global(row.getDouble(Columns.COUT_GLOBAL.toString()).floatValue());
            }
            
            if(row.getInt(Columns.ID_TYPE_COTE.toString())!=null){
                final RefCote typeCote = typesCote.get(row.getInt(Columns.ID_TYPE_COTE.toString()));
                if(typeCote!=null){
                    if(prestation.getCoteId()==null){
                        prestation.setCoteId(typeCote.getId());
                    }
                }
            }
            
            if(row.getInt(Columns.ID_TYPE_POSITION.toString())!=null){
                final RefPosition typePosition = typesPosition.get(row.getInt(Columns.ID_TYPE_POSITION.toString()));
                if(typePosition!=null){
                    if(prestation.getPosition_structure()==null){
                        prestation.setPosition_structure(typePosition.getId());
                    }
                }
            }
            
            {
                final String c = cleanNullString(row.getString(Columns.DESCRIPTION_PRESTATION.toString()));
                if (c!=null){
                    if(nouvellePrestation){
                        prestation.setCommentaire(c);
                    } 
                    else if(!c.equals(prestation.getCommentaire())){
                        throw new AccessDbImporterException("Inconsistent data.");
                    }
                }
            }
            
            if (row.getDate(Columns.DATE_DEBUT_VAL.toString()) != null) {
                final Date date = row.getDate(Columns.DATE_DEBUT_VAL.toString());
                if(date!=null){
                    final LocalDateTime localDate = LocalDateTime.parse(date.toString(), dateTimeFormatter);
                    if(prestation.getDate_debut()==null){
                        prestation.setDate_debut(localDate);
                    }else if(!prestation.getDate_debut().equals(localDate)){
                        throw new AccessDbImporterException("Inconsistent data.");
                    }
                }
            }
            
            if (row.getDate(Columns.DATE_FIN_VAL.toString()) != null) {
                try {
                    final Date date = row.getDate(Columns.DATE_FIN_VAL.toString());
                    if (date != null) {
//                        System.out.println("prestation Id : "+row.getInt(Columns.ID_PRESTATION.toString()));
                        // Dates de fin de prestation incohérentes sur la prestation 1356 en Isère.
                        final LocalDateTime localDate = LocalDateTime.parse(date.toString(), dateTimeFormatter);
                        if (prestation.getDate_fin() == null) {
                            prestation.setDate_fin(localDate);
                        } 
//                        else if (!prestation.getDate_fin().equals(localDate)) {
//                            throw new AccessDbImporterException("Inconsistent data.");
//                        }
                    }
                } catch (DateTimeParseException e) {
                    System.out.println(e.getMessage());
                }
            }
            
            
            if(row.getInt(Columns.ID_SOURCE.toString())!=null){
                final RefSource typeSource = typesSource.get(row.getInt(Columns.ID_SOURCE.toString()));
                if(typeSource!=null){
                    if(prestation.getSourceId()==null){
                        prestation.setSourceId(typeSource.getId());
                    }
                    else if(!prestation.getSourceId().equals(typeSource.getId())){
                        throw new AccessDbImporterException("Inconsistent data.");
                    }
                }
            }
            
            if (row.getDouble(Columns.PR_DEBUT_CALCULE.toString()) != null) {
                final float v = row.getDouble(Columns.PR_DEBUT_CALCULE.toString()).floatValue();
                if(nouvellePrestation){
                    prestation.setPR_debut(v);
                }
//                else if(v!=desordre.getPR_debut()) {
//                    throw new AccessDbImporterException("Inconsistent data : "+v+" != "+desordre.getPR_debut()+" (id="+row.getInt(DesordreColumns.ID_DESORDRE.toString()));
//                }
            }
            
            if (row.getDouble(Columns.PR_FIN_CALCULE.toString()) != null) {
                final float v = row.getDouble(Columns.PR_FIN_CALCULE.toString()).floatValue();
                if(nouvellePrestation){
                    prestation.setPR_fin(v);
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
                        if(prestation.getPositionDebut()==null){
                            prestation.setPositionDebut(positionDebut);
                        } else if(!prestation.getPositionDebut().equals(positionDebut)){
                            throw new AccessDbImporterException("Inconsistent data.");
                        }
                    }
                } catch (MismatchedDimensionException | TransformException ex) {
                    Logger.getLogger(PrestationImporter.class.getName()).log(Level.SEVERE, null, ex);
                }

                try {

                    if (row.getDouble(Columns.X_FIN.toString()) != null 
                            && row.getDouble(Columns.Y_FIN.toString()) != null) {
                        final Point positionFin = (Point) JTS.transform(geometryFactory.createPoint(new Coordinate(
                                    row.getDouble(Columns.X_FIN.toString()),
                                    row.getDouble(Columns.Y_FIN.toString()))), lambertToRGF);
                        if(prestation.getPositionFin()==null){
                            prestation.setPositionFin(positionFin);
                        } else if(!prestation.getPositionFin().equals(positionFin)){
                            throw new AccessDbImporterException("Inconsistent data.");
                        }
                    }
                } catch (MismatchedDimensionException | TransformException ex) {
                    Logger.getLogger(PrestationImporter.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (FactoryException ex) {
                Logger.getLogger(PrestationImporter.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            if (row.getDouble(Columns.ID_BORNEREF_DEBUT.toString()) != null) {
                final BorneDigue b = bornes.get((int) row.getDouble(Columns.ID_BORNEREF_DEBUT.toString()).doubleValue());
                if(b!=null) {
                    if(nouvellePrestation || prestation.getBorneDebutId()==null){
                        prestation.setBorneDebutId(b.getId());
                    }
                    else if(!prestation.getBorneDebutId().equals(b.getId())){
                        throw new AccessDbImporterException("Inconsistent data.");
                    }
                }
            }
            
            if (row.getDouble(Columns.ID_BORNEREF_FIN.toString()) != null) {
                final BorneDigue b = bornes.get((int) row.getDouble(Columns.ID_BORNEREF_FIN.toString()).doubleValue());
                if (b!=null) {
                    if(prestation.getBorneFinId()==null){
                        prestation.setBorneFinId(b.getId());
                    }
                    else if(!prestation.getBorneFinId().equals(b.getId())){
                        throw new AccessDbImporterException("Inconsistent data.");
                    }
                }
            }
            
            
            
            if(row.getInt(Columns.ID_SYSTEME_REP.toString())!=null){
                final SystemeReperage sr = systemesReperage.get(row.getInt(Columns.ID_SYSTEME_REP.toString()));
                if(sr!=null){
                    if(prestation.getSystemeRepId()==null){
                        prestation.setSystemeRepId(sr.getId());
                    }
                    else if(!prestation.getSystemeRepId().equals(sr.getId())){
                        throw new AccessDbImporterException("Inconsistent data.");
                    }
                }
            }
            
            if (row.getDouble(Columns.DIST_BORNEREF_DEBUT.toString()) != null) {
                final float v = row.getDouble(Columns.DIST_BORNEREF_DEBUT.toString()).floatValue();
                if(nouvellePrestation){
                    prestation.setBorne_debut_distance(v);
                }
//                else if(v!=desordre.getBorne_debut_distance()) {
//                    throw new AccessDbImporterException("Inconsistent data : "+v+" != "+desordre.getBorne_debut_distance()+" (id="+row.getInt(DesordreColumns.ID_DESORDRE.toString()));
//                }
            }
            
            if (row.getDouble(Columns.DIST_BORNEREF_FIN.toString()) != null) {
                final float v = row.getDouble(Columns.DIST_BORNEREF_FIN.toString()).floatValue();
                if(nouvellePrestation){
                    prestation.setBorne_fin_distance(v);
                }
//                else if(v!=desordre.getBorne_fin_distance()) {
//                    throw new AccessDbImporterException("Inconsistent data : "+v+" != "+desordre.getBorne_fin_distance()+" (id="+row.getInt(DesordreColumns.ID_DESORDRE.toString()));
//                }
            }

            {
                final boolean bda = row.getBoolean(Columns.AMONT_AVAL_DEBUT.toString());
                if(nouvellePrestation){
                    prestation.setBorne_debut_aval(bda); 
                } 
//                else if(bda!=desordre.getBorne_debut_aval()){
//                    throw new AccessDbImporterException("Inconsistent data.");
//                }
            }
            
            {
                final boolean bfa = row.getBoolean(Columns.AMONT_AVAL_FIN.toString());
                if(nouvellePrestation){
                    prestation.setBorne_fin_aval(bfa);
                }
//                else if(bda!=desordre.getBorne_debut_aval()){
//                    throw new AccessDbImporterException("Inconsistent data.");
//                }
            }
            
            
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                prestation.setDateMaj(LocalDateTime.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            if (nouvellePrestation) {
                
                // Don't set the old ID, but save it into the dedicated map in order to keep the reference.
                structures.put(row.getInt(Columns.ID_PRESTATION.toString()), prestation);

                // Set the list ByTronconId
                List<Prestation> listByTronconId = structuresByTronconId.get(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
                if (listByTronconId == null) {
                    listByTronconId = new ArrayList<>();
                    structuresByTronconId.put(row.getInt(Columns.ID_TRONCON_GESTION.toString()), listByTronconId);
                }
                listByTronconId.add(prestation);
            }
        }
    }
}
