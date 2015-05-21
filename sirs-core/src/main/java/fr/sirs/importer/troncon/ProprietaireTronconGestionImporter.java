package fr.sirs.importer.troncon;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.Contact;
import fr.sirs.core.model.Organisme;
import fr.sirs.core.model.ProprieteTroncon;
import fr.sirs.core.model.RefProprietaire;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.BorneDigueImporter;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.GenericImporter;
import fr.sirs.importer.IntervenantImporter;
import fr.sirs.importer.OrganismeImporter;
import fr.sirs.importer.SystemeReperageImporter;
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
class ProprietaireTronconGestionImporter extends GenericImporter {

    private Map<Integer, List<ProprieteTroncon>> proprietairesByTronconId = null;
    
    private final SystemeReperageImporter systemeReperageImporter;
    private final BorneDigueImporter borneDigueImporter;
                    
    private final IntervenantImporter intervenantImporter;
    private final OrganismeImporter organismeImporter;
    private final TypeProprietaireImporter typeProprietaireImporter;

    ProprietaireTronconGestionImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector, 
            final SystemeReperageImporter systemeReperageImporter,
            final BorneDigueImporter borneDigueImporter,
            final IntervenantImporter intervenantImporter,
            final OrganismeImporter organismeImporter) {
        super(accessDatabase, couchDbConnector);
        this.systemeReperageImporter = systemeReperageImporter;
        this.borneDigueImporter = borneDigueImporter;
        this.intervenantImporter = intervenantImporter;
        this.organismeImporter = organismeImporter;
        typeProprietaireImporter = new TypeProprietaireImporter(accessDatabase, 
                couchDbConnector);
    }

    private enum Columns {
        ID_PROPRIETAIRE_TRONCON_GESTION, // Pas dans le nouveau modèle
        ID_TRONCON_GESTION,
        ID_TYPE_PROPRIETAIRE,
        DATE_DEBUT,
        DATE_FIN,
        ID_ORGANISME,
        ID_INTERVENANT,
        PR_DEBUT_CALCULE, // Pas dans le nouveau modèle
        PR_FIN_CALCULE, // Pas dans le nouveau modèle
        X_DEBUT, // Pas dans le nouveau modèle
        X_FIN, // Pas dans le nouveau modèle
        Y_DEBUT, // Pas dans le nouveau modèle
        Y_FIN, // Pas dans le nouveau modèle
        ID_BORNEREF_DEBUT, // Pas dans le nouveau modèle
        ID_BORNEREF_FIN, // Pas dans le nouveau modèle
        ID_SYSTEME_REP, // Pas dans le nouveau modèle
        DIST_BORNEREF_DEBUT, // Pas dans le nouveau modèle
        DIST_BORNEREF_FIN, // Pas dans le nouveau modèle
        AMONT_AVAL_DEBUT, // Pas dans le nouveau modèle
        AMONT_AVAL_FIN, // Pas dans le nouveau modèle
        DATE_DERNIERE_MAJ
    };
    

    /**
     *
     * @return A map containing all ContactTroncon instances accessibles from
     * the internal database <em>TronconGestion</em> identifier.
     * @throws IOException
     * @throws fr.sirs.importer.AccessDbImporterException
     */
    public Map<Integer, List<ProprieteTroncon>> getProprietairesByTronconId() throws IOException, AccessDbImporterException {
        if (proprietairesByTronconId == null) compute();
        return proprietairesByTronconId;
    }

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
        return DbImporter.TableName.PROPRIETAIRE_TRONCON_GESTION.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        proprietairesByTronconId = new HashMap<>();

        final Map<Integer, BorneDigue> bornes = borneDigueImporter.getBorneDigue();
        final Map<Integer, SystemeReperage> systemesReperage = systemeReperageImporter.getSystemeRepLineaire();
        
        final Map<Integer, Contact> intervenants = intervenantImporter.getIntervenants();
        final Map<Integer, Organisme> organismes = organismeImporter.getOrganismes();
        final Map<Integer, RefProprietaire> typesProprietaires = typeProprietaireImporter.getTypeReferences();
        
        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final ProprieteTroncon propriete = new ProprieteTroncon();
            

            if (row.getDate(Columns.DATE_DEBUT.toString()) != null) {
                propriete.setDate_debut(DbImporter.parse(row.getDate(Columns.DATE_DEBUT.toString()), dateTimeFormatter));
            }
            if (row.getDate(Columns.DATE_FIN.toString()) != null) {
                propriete.setDate_fin(DbImporter.parse(row.getDate(Columns.DATE_FIN.toString()), dateTimeFormatter));
            }
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                propriete.setDateMaj(DbImporter.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()), dateTimeFormatter));
            }
            
            if(row.getInt(Columns.ID_TYPE_PROPRIETAIRE.toString())!=null){
                propriete.setTypeProprietaireId(typesProprietaires.get(row.getInt(Columns.ID_TYPE_PROPRIETAIRE.toString())).getId());
            }

             if (row.getDouble(Columns.PR_DEBUT_CALCULE.toString()) != null) {
                propriete.setPR_debut(row.getDouble(Columns.PR_DEBUT_CALCULE.toString()).floatValue());
            }

            if (row.getDouble(Columns.PR_FIN_CALCULE.toString()) != null) {
                propriete.setPR_fin(row.getDouble(Columns.PR_FIN_CALCULE.toString()).floatValue());
            }

            if (row.getInt(Columns.ID_SYSTEME_REP.toString()) != null) {
                propriete.setSystemeRepId(systemesReperage.get(row.getInt(Columns.ID_SYSTEME_REP.toString())).getId());
            }

            if (row.getDouble(Columns.ID_BORNEREF_DEBUT.toString()) != null) {
                propriete.setBorneDebutId(bornes.get((int) row.getDouble(Columns.ID_BORNEREF_DEBUT.toString()).doubleValue()).getId());
            }

            propriete.setBorne_debut_aval(row.getBoolean(Columns.AMONT_AVAL_DEBUT.toString()));

            if (row.getDouble(Columns.DIST_BORNEREF_DEBUT.toString()) != null) {
                propriete.setBorne_debut_distance(row.getDouble(Columns.DIST_BORNEREF_DEBUT.toString()).floatValue());
            }

            if (row.getDouble(Columns.ID_BORNEREF_FIN.toString()) != null) {
                propriete.setBorneFinId(bornes.get((int) row.getDouble(Columns.ID_BORNEREF_FIN.toString()).doubleValue()).getId());
            }

            propriete.setBorne_fin_aval(row.getBoolean(Columns.AMONT_AVAL_FIN.toString()));

            if (row.getDouble(Columns.DIST_BORNEREF_FIN.toString()) != null) {
                propriete.setBorne_fin_distance(row.getDouble(Columns.DIST_BORNEREF_FIN.toString()).floatValue());
            }
            
            GeometryFactory geometryFactory = new GeometryFactory();
            final MathTransform lambertToRGF;
            try {
                lambertToRGF = CRS.findMathTransform(CRS.decode("EPSG:27563"), getOutputCrs(), true);

                try {

                    if (row.getDouble(Columns.X_DEBUT.toString()) != null && row.getDouble(Columns.Y_DEBUT.toString()) != null) {
                        propriete.setPositionDebut((Point) JTS.transform(geometryFactory.createPoint(new Coordinate(
                                row.getDouble(Columns.X_DEBUT.toString()),
                                row.getDouble(Columns.Y_DEBUT.toString()))), lambertToRGF));
                    }
                } catch (MismatchedDimensionException | TransformException ex) {
                    Logger.getLogger(ProprietaireTronconGestionImporter.class.getName()).log(Level.SEVERE, null, ex);
                }

                try {

                    if (row.getDouble(Columns.X_FIN.toString()) != null && row.getDouble(Columns.Y_FIN.toString()) != null) {
                        propriete.setPositionFin((Point) JTS.transform(geometryFactory.createPoint(new Coordinate(
                                row.getDouble(Columns.X_FIN.toString()),
                                row.getDouble(Columns.Y_FIN.toString()))), lambertToRGF));
                    }
                } catch (MismatchedDimensionException | TransformException ex) {
                    Logger.getLogger(ProprietaireTronconGestionImporter.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (FactoryException ex) {
                Logger.getLogger(ProprietaireTronconGestionImporter.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            // Set the references.
            if(row.getInt(Columns.ID_INTERVENANT.toString())!=null){
                final Contact intervenant = intervenants.get(row.getInt(Columns.ID_INTERVENANT.toString()));
                if (intervenant.getId() != null) {
                    propriete.setContactId(intervenant.getId());
                } else {
                    throw new AccessDbImporterException("Le contact " + intervenant + " n'a pas encore d'identifiant CouchDb !");
                }
            }
            else if (row.getInt(Columns.ID_ORGANISME.toString())!=null){
                final Organisme organisme = organismes.get(row.getInt(Columns.ID_ORGANISME.toString()));
                if(organisme.getId()!=null){
                    propriete.setOrganismeId(organisme.getId());
                } else {
                    throw new AccessDbImporterException("L'organisme " + organisme + " n'a pas encore d'identifiant CouchDb !");
                }
            }
            
            propriete.setDesignation(String.valueOf(row.getInt(Columns.ID_PROPRIETAIRE_TRONCON_GESTION.toString())));
            propriete.setValid(true);
            
            // Don't set the old ID, but save it into the dedicated map in order to keep the reference.
            List<ProprieteTroncon> listeGestions = proprietairesByTronconId.get(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
            if(listeGestions == null){
                listeGestions = new ArrayList<>();
            }
            listeGestions.add(propriete);
            proprietairesByTronconId.put(row.getInt(Columns.ID_TRONCON_GESTION.toString()), listeGestions);
        }
    }
}
