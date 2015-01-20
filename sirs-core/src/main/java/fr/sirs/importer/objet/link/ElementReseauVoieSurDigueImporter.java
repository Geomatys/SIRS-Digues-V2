package fr.sirs.importer.objet.link;

import fr.sirs.core.SirsCore;
import java.util.logging.Level;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.Objet;
import fr.sirs.core.model.OuvrageFranchissement;
import fr.sirs.core.model.ReseauHydrauliqueFerme;
import fr.sirs.core.model.VoieDigue;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.objet.reseau.ElementReseauImporter;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class ElementReseauVoieSurDigueImporter extends GenericObjetLinker {

    private final ElementReseauImporter reseauImpoter;
    
    public ElementReseauVoieSurDigueImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector,
            final ElementReseauImporter reseauImpoter) {
        super(accessDatabase, couchDbConnector);
        this.reseauImpoter = reseauImpoter;
    }

    private enum Columns {
        ID_ELEMENT_RESEAU,
ID_ELEMENT_RESEAU_VOIE_SUR_DIGUE,
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
        return DbImporter.TableName.ELEMENT_RESEAU_VOIE_SUR_DIGUE.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        
        final Map<Integer, Objet> reseaux = reseauImpoter.getById();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            
            
            final Objet reseauVoieDigue = reseaux.get(row.getInt(Columns.ID_ELEMENT_RESEAU_VOIE_SUR_DIGUE.toString()));
            final Objet reseau =  reseaux.get(row.getInt(Columns.ID_ELEMENT_RESEAU.toString()));
            
            if(reseauVoieDigue instanceof VoieDigue){
                final VoieDigue voieDigue = (VoieDigue) reseauVoieDigue;
                if(voieDigue!=null && reseau!=null){

                    if(reseau instanceof ReseauHydrauliqueFerme){
                        final ReseauHydrauliqueFerme reseauFerme = (ReseauHydrauliqueFerme) reseau;
                        reseauFerme.getReseau_hydro_ciel_ouvert().add(voieDigue.getId());
                
                        associations.add(new AbstractMap.SimpleEntry<>(reseauVoieDigue, reseauFerme));
                    }
                    if(reseau instanceof OuvrageFranchissement){
                        SirsCore.LOGGER.log(Level.FINE, "Supprimé du modèle.");
                    }
                    else {
                        throw new AccessDbImporterException("Bad type");
                    }
                }
//                else if(reseau==null){
//                    SirsCore.LOGGER.log(Level.FINE, reseau+" => "+row.getInt(Columns.ID_ELEMENT_RESEAU.toString()));
//                }
//                else if(voieDigue==null){
//                    SirsCore.LOGGER.log(Level.FINE, reseau+" => "+row.getInt(Columns.ID_ELEMENT_RESEAU_VOIE_SUR_DIGUE.toString()));
//                }
            }
        }
    }
}
