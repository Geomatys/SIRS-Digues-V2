package fr.sirs.importer.documentTroncon;

import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.SirsCore;
import fr.sirs.core.model.AbstractPositionDocument;
import fr.sirs.core.model.ArticleJournal;
import fr.sirs.core.model.DocumentGrandeEchelle;
import fr.sirs.core.model.Marche;
import fr.sirs.core.model.ProfilLong;
import fr.sirs.core.model.ProfilTravers;
import fr.sirs.core.model.RapportEtude;
import fr.sirs.importer.AccessDbImporterException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class PositionDocumentImporter extends GenericPositionDocumentImporter<AbstractPositionDocument> {


    @Override
    protected void compute() throws IOException, AccessDbImporterException {

        positions = new HashMap<>();
        positionsByTronconId = new HashMap<>();

        final Iterator<Row> it = context.inputDb.getTable(getTableName()).iterator();
        while(it.hasNext()){
            final Row row = it.next();
            final AbstractPositionDocument position = importRow(row);

            if(position!=null){
                position.setDesignation(String.valueOf(row.getInt(Columns.ID_DOC.toString())));

                // Don't set the old ID, but save it into the dedicated map in order to keep the reference.
                positions.put(row.getInt(Columns.ID_DOC.toString()), position);

                // Set the list ByTronconId
                List<AbstractPositionDocument> listByTronconId = positionsByTronconId.get(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
                if (listByTronconId == null) {
                    listByTronconId = new ArrayList<>();
                    positionsByTronconId.put(row.getInt(Columns.ID_TRONCON_GESTION.toString()), listByTronconId);
                }
                listByTronconId.add(position);
            }
        }
        context.outputDb.executeBulk(positions.values());
    }

    @Override
    public  importRow(Row row) throws IOException, AccessDbImporterException {

        final Map<Integer, Class> classesDocument = typeDocumentImporter.getClasseDocument();

        final Class classeDocument = classesDocument.get(row.getInt(Columns.ID_TYPE_DOCUMENT.toString()));

        if (classeDocument != null) {

            if (classeDocument.equals(DocumentGrandeEchelle.class)){
                return sysEvtDocumentAGrandeEchelleImporter.importRow(row);
            }
            else if(classeDocument.equals(ArticleJournal.class)){
                return sysEvtJournalImporter.importRow(row);
            }
            else if(classeDocument.equals(Marche.class)){
                return sysEvtMarcheImporter.importRow(row);
            }
            else if(classeDocument.equals(ProfilLong.class)){
                return sysEvtProfilLongImporter.importRow(row);
            }
            else if(classeDocument.equals(ProfilTravers.class)){
                return sysEvtProfilTraversImporter.importRow(row);
            }
            else if(classeDocument.equals(RapportEtude.class)){
                return sysEvtRapportEtudeImporter.importRow(row);
            }
            else {
                SirsCore.LOGGER.log(Level.FINE, "Type de document non pris en charge : ID = " + row.getInt(Columns.ID_TYPE_DOCUMENT.toString()));
                return null;
            }
        } else {
            SirsCore.LOGGER.log(Level.FINE, "Type de document inconnu !");
                return null;
        }
    }
}
