package fr.sirs.importer.structure.desordre;

import fr.sirs.importer.structure.TypePositionImporter;
import fr.sirs.importer.structure.TypeCoteImporter;
import fr.sirs.importer.structure.TypeSourceImporter;
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
import fr.sirs.core.model.DesordreStructure;
import fr.sirs.core.model.RefCote;
import fr.sirs.core.model.RefPosition;
import fr.sirs.core.model.RefSource;
import fr.sirs.core.model.RefTypeDesordre;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.importer.structure.GenericStructureImporter;
import fr.sirs.importer.structure.StructureImporter;
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
public class DesordreImporter extends GenericStructureImporter {

    private Map<Integer, Desordre> desordres = null;
    private Map<Integer, List<Desordre>> desordresByTronconId = null;
    private final DesordreStructureImporter desordreStructureImporter;
    private final TypeDesordreImporter typeDesordreImporter;
    private final TypeSourceImporter typeSourceImporter;
    private final TypeCoteImporter typeCoteImporter;
    private final TypePositionImporter typePositionImporter;
    private final SubDesordreImporter subDesordreImporter;

    public DesordreImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector, 
            final TronconGestionDigueImporter tronconGestionDigueImporter, 
            final SystemeReperageImporter systemeReperageImporter, 
            final BorneDigueImporter borneDigueImporter, 
            final StructureImporter structureImporter, 
            final TypeDesordreImporter typeDesordreImporter, 
            final TypeSourceImporter typeSourceImporter,
            final TypePositionImporter typePositionImporter,
            final TypeCoteImporter typeCoteImporter) {
        super(accessDatabase, couchDbConnector, tronconGestionDigueImporter, 
                systemeReperageImporter, borneDigueImporter);
        this.desordreStructureImporter = new DesordreStructureImporter(
                accessDatabase, couchDbConnector, structureImporter);
        this.typeDesordreImporter = typeDesordreImporter;
        this.typeSourceImporter = typeSourceImporter;
        this.typePositionImporter = typePositionImporter;
        this.typeCoteImporter = typeCoteImporter;
        this.subDesordreImporter = new SubDesordreImporter(accessDatabase, 
                couchDbConnector, tronconGestionDigueImporter, 
                systemeReperageImporter, borneDigueImporter, 
                structureImporter, typeDesordreImporter, typeSourceImporter, 
                typePositionImporter, typeCoteImporter);
    }

    private enum DesordreColumns {
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
        COMMENTAIRE,
        LIEU_DIT_DESORDRE,
        ID_TYPE_POSITION,
//        ID_PRESTATION,
//        ID_CRUE,
//        DESCRIPTION_DESORDRE,
//        DISPARU,
//        DEJA_OBSERVE,
        DATE_DERNIERE_MAJ
    };

    /**
     *
     * @return A map containing all TronconDigue instances accessibles from the
     * internal database identifier.
     * @throws IOException
     * @throws AccessDbImporterException
     */
    public Map<Integer, Desordre> getDesordres() throws IOException, AccessDbImporterException {
        if (this.desordres == null) {
            compute();
        }
        return desordres;
    }

    /**
     *
     * @return A map containing all TronconDigue instances accessibles from the
     * internal database <em>TronconDigue</em> identifier.
     * @throws IOException
     * @throws AccessDbImporterException
     */
    public Map<Integer, List<Desordre>> getDesordresByTronconId() throws IOException, AccessDbImporterException {
        if (desordresByTronconId == null) {
            compute();
        }
        return desordresByTronconId;
    }

    @Override
    public String getTableName() {
        return DbImporter.TableName.SYS_EVT_DESORDRE.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {

        desordres = subDesordreImporter.getDesordres();
        desordresByTronconId = subDesordreImporter.getDesordresByTronconId();
        
        final Map<Integer, BorneDigue> bornes = borneDigueImporter.getBorneDigue();
        final Map<Integer, TronconDigue> troncons = tronconGestionDigueImporter.getTronconsDigues();
        final Map<Integer, List<DesordreStructure>> desordresStructures = desordreStructureImporter.getDesordresStructuresByDesordreId();
        final Map<Integer, SystemeReperage> systemesReperage = systemeReperageImporter.getSystemeRepLineaire();
        final Map<Integer, RefTypeDesordre> typesDesordre = typeDesordreImporter.getTypeDesordre();
        final Map<Integer, RefSource> typesSource = typeSourceImporter.getTypeSource();
        final Map<Integer, RefPosition> typesPosition = typePositionImporter.getTypePosition();
        final Map<Integer, RefCote> typesCote = typeCoteImporter.getTypeCote();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final Desordre desordre;
            final boolean nouveauDesordre;
            if(desordres.get(row.getInt(DesordreColumns.ID_DESORDRE.toString()))!=null){
                desordre = desordres.get(row.getInt(DesordreColumns.ID_DESORDRE.toString()));
                nouveauDesordre=false;
            }
            else{
                System.out.println("Nouveau désordre !!");
                desordre = new Desordre();
                nouveauDesordre=true;
            }
            
            if (row.getDouble(DesordreColumns.ID_BORNEREF_DEBUT.toString()) != null) {
                final BorneDigue b = bornes.get((int) row.getDouble(DesordreColumns.ID_BORNEREF_DEBUT.toString()).doubleValue());
                if(b!=null) {
                    if(nouveauDesordre || desordre.getBorneDebutId()==null){
                        desordre.setBorneDebutId(b.getId());
                    }
                    else if(!desordre.getBorneDebutId().equals(b.getId())){
                        throw new AccessDbImporterException("Inconsistent data.");
                    }
                }
            }
            if (row.getDouble(DesordreColumns.DIST_BORNEREF_DEBUT.toString()) != null) {
                final float v = row.getDouble(DesordreColumns.DIST_BORNEREF_DEBUT.toString()).floatValue();
                if(nouveauDesordre){
                    desordre.setBorne_debut_distance(v);
                }
                else if(v!=desordre.getBorne_debut_distance()) {
                    throw new AccessDbImporterException("Inconsistent data.");
                }
            }
            if (row.getDouble(DesordreColumns.PR_DEBUT_CALCULE.toString()) != null) {
                final float v = row.getDouble(DesordreColumns.PR_DEBUT_CALCULE.toString()).floatValue();
                if(nouveauDesordre){
                    desordre.setPR_debut(v);
                }
                else if(v!=desordre.getPR_debut()) {
                    throw new AccessDbImporterException("Inconsistent data.");
                }
            }
            
            if (row.getDouble(DesordreColumns.ID_BORNEREF_FIN.toString()) != null) {
                final BorneDigue b = bornes.get((int) row.getDouble(DesordreColumns.ID_BORNEREF_FIN.toString()).doubleValue());
                if (b!=null) {
                    if(desordre.getBorneFinId()==null){
                        desordre.setBorneFinId(b.getId());
                    }
                    else if(!desordre.getBorneFinId().equals(b.getId())){
                        throw new AccessDbImporterException("Inconsistent data.");
                    }
                }
            }
            if (row.getDouble(DesordreColumns.DIST_BORNEREF_FIN.toString()) != null) {
                final float v = row.getDouble(DesordreColumns.DIST_BORNEREF_FIN.toString()).floatValue();
                if(nouveauDesordre){
                    desordre.setBorne_fin_distance(v);
                }
                else if(v!=desordre.getBorne_fin_distance()) {
                    throw new AccessDbImporterException("Inconsistent data.");
                }
            }
            if (row.getDouble(DesordreColumns.PR_FIN_CALCULE.toString()) != null) {
                final float v = row.getDouble(DesordreColumns.PR_FIN_CALCULE.toString()).floatValue();
                if(nouveauDesordre){
                    desordre.setPR_fin(v);
                }
                else if(v!=desordre.getPR_fin()) {
                    throw new AccessDbImporterException("Inconsistent data.");
                }
            }
            
            if(row.getInt(DesordreColumns.ID_SYSTEME_REP.toString())!=null){
                final SystemeReperage sr = systemesReperage.get(row.getInt(DesordreColumns.ID_SYSTEME_REP.toString()));
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
                final boolean bda = row.getBoolean(DesordreColumns.AMONT_AVAL_DEBUT.toString());
                if(nouveauDesordre){
                    desordre.setBorne_debut_aval(bda); 
                } 
                else if(bda!=desordre.getBorne_debut_aval()){
                    throw new AccessDbImporterException("Inconsistent data.");
                }
            }
            
            {
                final boolean bfa = row.getBoolean(DesordreColumns.AMONT_AVAL_FIN.toString());
                if(nouveauDesordre){
                    desordre.setBorne_fin_aval(bfa);
                }
                else if(bfa!=desordre.getBorne_fin_aval()){
                    throw new AccessDbImporterException("Inconsistent data.");
                }
            }
            
            {
                final String ld = row.getString(DesordreColumns.LIEU_DIT_DESORDRE.toString());
                if (ld != null) {
                    if (nouveauDesordre) {
                        desordre.setLieu_dit(ld);
                    } else if (!ld.equals(desordre.getLieu_dit())) {
                        throw new AccessDbImporterException("Inconsistent data.");
                    }
                }
            }
        
            if(row.getInt(DesordreColumns.ID_TYPE_DESORDRE.toString())!=null){
                final RefTypeDesordre typeDesordre = typesDesordre.get(row.getInt(DesordreColumns.ID_TYPE_DESORDRE.toString()));
                if(typeDesordre!=null){
                    if(desordre.getTypeDesordre()==null){
                        desordre.setTypeDesordre(typeDesordre.getId());
                    }
                    else if(!desordre.getTypeDesordre().equals(typeDesordre.getId())){
                        throw new AccessDbImporterException("Inconsistent data.");
                    }
                }
            }
            
            if(row.getInt(DesordreColumns.ID_SOURCE.toString())!=null){
                final RefSource typeSource = typesSource.get(row.getInt(DesordreColumns.ID_SOURCE.toString()));
                if(typeSource!=null){
                    if(desordre.getSource()==null){
                        desordre.setSource(typeSource.getId());
                    }
                    else if(!desordre.getSource().equals(typeSource.getId())){
                        throw new AccessDbImporterException("Inconsistent data.");
                    }
                }
            }
            
            if(row.getInt(DesordreColumns.ID_TYPE_POSITION.toString())!=null){
                final RefPosition typePosition = typesPosition.get(row.getInt(DesordreColumns.ID_TYPE_POSITION.toString()));
                if(typePosition!=null){
                    if(desordre.getPosition_structure()==null){
                        desordre.setPosition_structure(typePosition.getId());
                    }
                    else if(!desordre.getPosition_structure().equals(typePosition.getId())){
                        throw new AccessDbImporterException("Inconsistent data.");
                    }
                }
            }
            
            if(row.getInt(DesordreColumns.ID_TYPE_COTE.toString())!=null){
                final RefCote typeCote = typesCote.get(row.getInt(DesordreColumns.ID_TYPE_COTE.toString()));
                if(typeCote!=null){
                    if(desordre.getCote()==null){
                        desordre.setCote(typeCote.getId());
                    }
                    else if(!desordre.getCote().equals(typeCote.getId())){
                        throw new AccessDbImporterException("Inconsistent data.");
                    }
                }
            }
            
            if (row.getInt(DesordreColumns.ID_TRONCON_GESTION.toString()) != null) {
                final TronconDigue troncon = troncons.get(row.getInt(DesordreColumns.ID_TRONCON_GESTION.toString()));
                if (troncon.getId() != null && desordre.getTroncon()==null) {
                    desordre.setTroncon(troncon.getId());
                } else if(troncon.getId()==null) {
                    throw new AccessDbImporterException("Le tronçon "
                            + troncons.get(row.getInt(DesordreColumns.ID_TRONCON_GESTION.toString())) + " n'a pas encore d'identifiant CouchDb !");
                } else if(!desordre.getTroncon().equals(troncon.getId())){
                    throw new AccessDbImporterException("Inconsistent data.");
                }
            }
            
            if (row.getDate(DesordreColumns.DATE_DEBUT_VAL.toString()) != null) {
                final Date date = row.getDate(DesordreColumns.DATE_DEBUT_VAL.toString());
                if(date!=null){
                    final LocalDateTime localDate = LocalDateTime.parse(date.toString(), dateTimeFormatter);
                    if(desordre.getDate_debut()==null){
                        desordre.setDate_debut(localDate);
                    }else if(!desordre.getDate_debut().equals(localDate)){
                        throw new AccessDbImporterException("Inconsistent data.");
                    }
                }
            }
            
            if (row.getDate(DesordreColumns.DATE_FIN_VAL.toString()) != null) {
                final Date date = row.getDate(DesordreColumns.DATE_FIN_VAL.toString());
                if(date!=null){
                    final LocalDateTime localDate = LocalDateTime.parse(date.toString(), dateTimeFormatter);
                    if(desordre.getDate_fin()==null){
                        desordre.setDate_fin(localDate);
                    }
                    else if(!desordre.getDate_fin().equals(localDate)){
                        throw new AccessDbImporterException("Inconsistent data.");
                    }
                }
            }
            
            {
                final String commentaire = row.getString(DesordreColumns.COMMENTAIRE.toString());
                if(commentaire!=null){
                    if(desordre.getCommentaire()==null){
                        desordre.setCommentaire(commentaire);
                    } else if(!desordre.getCommentaire().equals(commentaire)){
                        throw new AccessDbImporterException("Inconsistent data.");
                    }
                }
            }
            
            GeometryFactory geometryFactory = new GeometryFactory();
            final MathTransform lambertToRGF;
            try {
                lambertToRGF = CRS.findMathTransform(CRS.decode("EPSG:27563"), CRS.decode("EPSG:2154"), true);

                try {

                    if (row.getDouble(DesordreColumns.X_DEBUT.toString()) != null 
                            && row.getDouble(DesordreColumns.Y_DEBUT.toString()) != null) {
                        final Point positionDebut = (Point) JTS.transform(geometryFactory.createPoint(new Coordinate(
                                row.getDouble(DesordreColumns.X_DEBUT.toString()),
                                row.getDouble(DesordreColumns.Y_DEBUT.toString()))), lambertToRGF);
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

                    if (row.getDouble(DesordreColumns.X_FIN.toString()) != null 
                            && row.getDouble(DesordreColumns.Y_FIN.toString()) != null) {
                        final Point positionFin = (Point) JTS.transform(geometryFactory.createPoint(new Coordinate(
                                    row.getDouble(DesordreColumns.X_FIN.toString()),
                                    row.getDouble(DesordreColumns.Y_FIN.toString()))), lambertToRGF);
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
            
            
            if (row.getDate(DesordreColumns.DATE_DERNIERE_MAJ.toString()) != null) {
                desordre.setDateMaj(LocalDateTime.parse(row.getDate(DesordreColumns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            
            
            
            
            if (nouveauDesordre) {
                List<DesordreStructure> structures = desordresStructures.get(row.getInt(DesordreColumns.ID_DESORDRE.toString()));
                if (structures == null) {
                    structures = new ArrayList<>();
                }
                desordre.setDesordreStructure(structures);
                
                // Don't set the old ID, but save it into the dedicated map in order to keep the reference.
                //tronconDigue.setId(String.valueOf(row.getString(TronconDigueColumns.ID.toString())));
                desordres.put(row.getInt(DesordreColumns.ID_DESORDRE.toString()), desordre);

                // Set the list ByTronconId
                List<Desordre> listByTronconId = desordresByTronconId.get(row.getInt(DesordreColumns.ID_TRONCON_GESTION.toString()));
                if (listByTronconId == null) {
                    listByTronconId = new ArrayList<>();
                }
                listByTronconId.add(desordre);
                desordresByTronconId.put(row.getInt(DesordreColumns.ID_TRONCON_GESTION.toString()), listByTronconId);
            }
        }
    }

    @Override
    public List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (DesordreColumns c : DesordreColumns.values()) {
            columns.add(c.toString());
        }
        return columns;
    }
}
