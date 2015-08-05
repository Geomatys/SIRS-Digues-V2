package fr.sirs.importer.documentTroncon;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.Convention;
import fr.sirs.core.model.ElementCreator;
import fr.sirs.core.model.ObjetReseau;
import fr.sirs.core.model.PositionConvention;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.BorneDigueImporter;
import static fr.sirs.importer.DbImporter.TableName.ELEMENT_RESEAU_CONVENTION;
import fr.sirs.importer.SystemeReperageImporter;
import fr.sirs.importer.documentTroncon.document.convention.ConventionImporter;
import fr.sirs.importer.objet.reseau.ElementReseauImporter;
import fr.sirs.importer.troncon.TronconGestionDigueImporter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class ElementReseauConventionImporter extends GenericPositionDocumentImporter<PositionConvention> {

    private final ElementReseauImporter elementReseauImporter;
    private final ConventionImporter conventionImporter;
    
    public ElementReseauConventionImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector,
            final TronconGestionDigueImporter tronconGestionDigueImporter,
            final BorneDigueImporter borneDigueImporter,
            final SystemeReperageImporter systemeReperageImporter,
            final ElementReseauImporter elementReseauImporter,
            final ConventionImporter conventionImporter) {
        super(accessDatabase, couchDbConnector, tronconGestionDigueImporter,
                borneDigueImporter, systemeReperageImporter);
        this.elementReseauImporter = elementReseauImporter;
        this.conventionImporter = conventionImporter;
    }

    private enum Columns {
        ID_ELEMENT_RESEAU,
        ID_CONVENTION
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
        return ELEMENT_RESEAU_CONVENTION.toString();
    }

    @Override
    public void compute() throws IOException, AccessDbImporterException {
        
        if(positions == null){
            positions = new HashMap<>();
            int cpt = 0;
            final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
            while (it.hasNext()) {
                final Row row = it.next();
                final PositionConvention position = importRow(row);

                    if (position != null) {
                        // Don't set the old ID, but save it into the dedicated map in order to keep the reference.
                        positions.put(cpt++, position);

                    }
            }
            couchDbConnector.executeBulk(positions.values());
        }
    }
    
    @Override
    public Map<Integer, PositionConvention> getPositions() throws IOException, AccessDbImporterException {
        throw new UnsupportedOperationException("Cannot retrieve unidentified entities");
    }
    
    @Override
    public Map<Integer, List<PositionConvention>> getPositionsByTronconId() throws IOException, AccessDbImporterException {
        throw new UnsupportedOperationException("Cannot retrieve unidentified entities");
    }
    
    @Override
    PositionConvention importRow(Row row) throws IOException, AccessDbImporterException {
        
        final Map<Integer, ObjetReseau> reseaux = elementReseauImporter.getById();
        final Map<Integer, Convention> conventions = conventionImporter.getRelated();
        
            final ObjetReseau reseau = reseaux.get(row.getInt(Columns.ID_ELEMENT_RESEAU.toString()));
            final Convention convention = conventions.get(row.getInt(Columns.ID_CONVENTION.toString()));
            final PositionConvention position;
            if(reseau!=null && convention!=null){
                
                position = ElementCreator.createAnonymValidElement(PositionConvention.class);
                // Par défaut, on initialise la position à l'aide des attributs de l'objet.
                position.setPrDebut(reseau.getPrDebut());
                position.setPrFin(reseau.getPrFin());
                position.setDate_debut(reseau.getDate_debut());
                position.setDate_fin(reseau.getDate_fin());
                position.setSystemeRepId(reseau.getSystemeRepId());
                position.setBorneDebutId(reseau.getBorneDebutId());
                position.setBorneFinId(reseau.getBorneFinId());
                position.setBorne_debut_aval(reseau.getBorne_debut_aval());
                position.setBorne_fin_aval(reseau.getBorne_fin_aval());
                position.setBorne_debut_distance(reseau.getBorne_debut_distance());
                position.setBorne_fin_distance(reseau.getBorne_fin_distance());
                position.setDesignation(convention.getDesignation()+"/"+reseau.getDesignation());
                position.setGeometry(reseau.getGeometry());
                position.setLatitudeMax(reseau.getLatitudeMax());
                position.setLatitudeMin(reseau.getLatitudeMin());
                position.setLongitudeMax(reseau.getLongitudeMax());
                position.setLongitudeMin(reseau.getLongitudeMin());
                position.setLinearId(reseau.getLinearId());
                position.setPositionDebut(reseau.getPositionDebut());
                position.setPositionFin(reseau.getPositionFin());
                
                position.setObjetId(reseau.getId());
                position.setSirsdocument(convention.getId());
            } else {
                position = null;
            }
            return position;
    }
}
