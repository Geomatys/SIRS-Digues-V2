package fr.sirs.importer.troncon;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import static fr.sirs.core.LinearReferencingUtilities.buildGeometry;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.Contact;
import static fr.sirs.core.model.ElementCreator.createAnonymValidElement;
import fr.sirs.core.model.Organisme;
import fr.sirs.core.model.ProprieteTroncon;
import fr.sirs.core.model.RefProprietaire;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.BorneDigueImporter;
import fr.sirs.importer.DbImporter;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.IntervenantImporter;
import fr.sirs.importer.OrganismeImporter;
import fr.sirs.importer.SystemeReperageImporter;
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
public class ProprietaireTronconGestionImporter extends GenericPeriodeLocaliseeImporter<ProprieteTroncon> {
                    
    private final IntervenantImporter intervenantImporter;
    private final OrganismeImporter organismeImporter;
    private final TypeProprietaireImporter typeProprietaireImporter;

    public ProprietaireTronconGestionImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector, 
            final TronconGestionDigueImporter tronconGestionDigueImporter,
            final SystemeReperageImporter systemeReperageImporter,
            final BorneDigueImporter borneDigueImporter,
            final IntervenantImporter intervenantImporter,
            final OrganismeImporter organismeImporter) {
        super(accessDatabase, couchDbConnector, tronconGestionDigueImporter, 
                systemeReperageImporter, borneDigueImporter);
        this.intervenantImporter = intervenantImporter;
        this.organismeImporter = organismeImporter;
        typeProprietaireImporter = new TypeProprietaireImporter(accessDatabase, 
                couchDbConnector);
    }

    private enum Columns {
        ID_PROPRIETAIRE_TRONCON_GESTION,
        ID_TRONCON_GESTION,
        ID_TYPE_PROPRIETAIRE,
        DATE_DEBUT,
        DATE_FIN,
        ID_ORGANISME,
        ID_INTERVENANT,
        PR_DEBUT_CALCULE,
        PR_FIN_CALCULE,
        X_DEBUT,
        X_FIN,
        Y_DEBUT,
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
        return PROPRIETAIRE_TRONCON_GESTION.toString();
    }

    @Override
    public void compute() throws IOException, AccessDbImporterException {
        objets = new HashMap<>();

        final Map<Integer, BorneDigue> bornes = borneDigueImporter.getBorneDigue();
        final Map<Integer, SystemeReperage> systemesReperage = systemeReperageImporter.getSystemeRepLineaire();
        
        final Map<Integer, Contact> intervenants = intervenantImporter.getIntervenants();
        final Map<Integer, Organisme> organismes = organismeImporter.getOrganismes();
        final Map<Integer, RefProprietaire> typesProprietaires = typeProprietaireImporter.getTypeReferences();
        
        final Iterator<Row> it = context.inputDb.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final ProprieteTroncon propriete = createAnonymValidElement(ProprieteTroncon.class);
            
            final TronconDigue troncon = tronconGestionDigueImporter.getTronconsDigues().get(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
            propriete.setLinearId(troncon.getId());
            
            propriete.setDesignation(String.valueOf(row.getInt(Columns.ID_PROPRIETAIRE_TRONCON_GESTION.toString())));

            if (row.getDate(Columns.DATE_DEBUT.toString()) != null) {
                propriete.setDate_debut(DbImporter.parseLocalDate(row.getDate(Columns.DATE_DEBUT.toString()), dateTimeFormatter));
            }
            if (row.getDate(Columns.DATE_FIN.toString()) != null) {
                propriete.setDate_fin(DbImporter.parseLocalDate(row.getDate(Columns.DATE_FIN.toString()), dateTimeFormatter));
            }
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                propriete.setDateMaj(DbImporter.parseLocalDate(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()), dateTimeFormatter));
            }
            
            if(row.getInt(Columns.ID_TYPE_PROPRIETAIRE.toString())!=null){
                propriete.setTypeProprietaireId(typesProprietaires.get(row.getInt(Columns.ID_TYPE_PROPRIETAIRE.toString())).getId());
            }

             if (row.getDouble(Columns.PR_DEBUT_CALCULE.toString()) != null) {
                propriete.setPrDebut(row.getDouble(Columns.PR_DEBUT_CALCULE.toString()).floatValue());
            }

            if (row.getDouble(Columns.PR_FIN_CALCULE.toString()) != null) {
                propriete.setPrFin(row.getDouble(Columns.PR_FIN_CALCULE.toString()).floatValue());
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
                lambertToRGF = CRS.findMathTransform(DbImporter.IMPORT_CRS, getOutputCrs(), true);

                try {

                    if (row.getDouble(Columns.X_DEBUT.toString()) != null && row.getDouble(Columns.Y_DEBUT.toString()) != null) {
                        propriete.setPositionDebut((Point) JTS.transform(geometryFactory.createPoint(new Coordinate(
                                row.getDouble(Columns.X_DEBUT.toString()),
                                row.getDouble(Columns.Y_DEBUT.toString()))), lambertToRGF));
                    }
                } catch (MismatchedDimensionException | TransformException ex) {
                    Logger.getLogger(ProprietaireTronconGestionImporter.class.getName()).log(Level.WARNING, null, ex);
                }

                try {

                    if (row.getDouble(Columns.X_FIN.toString()) != null && row.getDouble(Columns.Y_FIN.toString()) != null) {
                        propriete.setPositionFin((Point) JTS.transform(geometryFactory.createPoint(new Coordinate(
                                row.getDouble(Columns.X_FIN.toString()),
                                row.getDouble(Columns.Y_FIN.toString()))), lambertToRGF));
                    }
                } catch (MismatchedDimensionException | TransformException ex) {
                    Logger.getLogger(ProprietaireTronconGestionImporter.class.getName()).log(Level.WARNING, null, ex);
                }
            } catch (FactoryException ex) {
                Logger.getLogger(ProprietaireTronconGestionImporter.class.getName()).log(Level.WARNING, null, ex);
            }
            
            propriete.setGeometry(buildGeometry(troncon.getGeometry(), propriete, tronconGestionDigueImporter.getBorneDigueRepository()));
            
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
            
            objets.put(row.getInt(Columns.ID_TRONCON_GESTION.toString()), propriete);
        }
        context.outputDb.executeBulk(objets.values());
    }
}
