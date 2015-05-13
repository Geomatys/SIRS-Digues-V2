package fr.sirs.importer.documentTroncon.document;

import com.healthmarketscience.jackcess.Database;
import fr.sirs.importer.IntervenantImporter;
import fr.sirs.importer.OrganismeImporter;
import fr.sirs.importer.SystemeReperageImporter;
import fr.sirs.importer.documentTroncon.PositionDocumentImporter;
import fr.sirs.importer.documentTroncon.TypeDocumentImporter;
import fr.sirs.importer.documentTroncon.document.convention.ConventionImporter;
import fr.sirs.importer.documentTroncon.document.documentAGrandeEchelle.DocumentAGrandeEchelleImporter;
import fr.sirs.importer.documentTroncon.document.journal.JournalArticleImporter;
import fr.sirs.importer.documentTroncon.document.marche.MarcheImporter;
import fr.sirs.importer.documentTroncon.document.profilLong.ProfilEnLongImporter;
import fr.sirs.importer.documentTroncon.document.profilTravers.ProfilEnTraversDescriptionImporter;
import fr.sirs.importer.documentTroncon.document.profilTravers.ProfilEnTraversImporter;
import fr.sirs.importer.documentTroncon.document.rapportEtude.RapportEtudeImporter;
import fr.sirs.importer.evenementHydraulique.EvenementHydrauliqueImporter;
import java.util.ArrayList;
import java.util.List;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class DocumentManager {
    
    private final ConventionImporter conventionImporter;
    private final DocumentAGrandeEchelleImporter documentAGrandeEchelleImporter;
    private final JournalArticleImporter journalArticleImporter;
    private final MarcheImporter marcheImporter;
    private final ProfilEnTraversImporter profilEnTraversImporter;
    private final RapportEtudeImporter rapportEtudeImporter;
    
    private final ProfilEnLongImporter profilEnLongImporter;
    
    private final List<GenericDocumentRelatedImporter> documentRelatedImporters = new ArrayList<>();
    
    public DocumentManager(final Database accessDatabase,
            final CouchDbConnector couchDbConnector,
            final OrganismeImporter organismeImporter,
            final IntervenantImporter intervenantImporter,
            final SystemeReperageImporter systemeReperageImporter,
            final EvenementHydrauliqueImporter evenementHydrauliqueImporter,
            final TypeDocumentImporter typeDocumentImporter){
        
        conventionImporter = new ConventionImporter(accessDatabase, 
                couchDbConnector, intervenantImporter, organismeImporter);
        documentRelatedImporters.add(conventionImporter);
        documentAGrandeEchelleImporter = new DocumentAGrandeEchelleImporter(
                accessDatabase, couchDbConnector, typeDocumentImporter);
        documentRelatedImporters.add(documentAGrandeEchelleImporter);
        journalArticleImporter = new JournalArticleImporter(accessDatabase, 
                couchDbConnector);
        documentRelatedImporters.add(journalArticleImporter);
        marcheImporter = new MarcheImporter(accessDatabase, couchDbConnector, 
                organismeImporter);
        documentRelatedImporters.add(marcheImporter);
        
        final TypeSystemeReleveProfilImporter typeSystemeReleveProfilImporter = 
                new TypeSystemeReleveProfilImporter(accessDatabase, couchDbConnector);
        
        ProfilEnTraversDescriptionImporter profilTraversDescriptionImporter = 
                new ProfilEnTraversDescriptionImporter(
                accessDatabase, couchDbConnector, 
                typeSystemeReleveProfilImporter, organismeImporter, 
                evenementHydrauliqueImporter);
        profilEnTraversImporter = new ProfilEnTraversImporter(accessDatabase, 
                couchDbConnector, profilTraversDescriptionImporter);
        documentRelatedImporters.add(profilEnTraversImporter);
        
        rapportEtudeImporter = new RapportEtudeImporter(accessDatabase, 
                couchDbConnector);
        documentRelatedImporters.add(rapportEtudeImporter);
        
        // OTHER IMPORTERS : NOT DOCUMENT RELATED !!!!!!!
        profilEnLongImporter = new ProfilEnLongImporter(accessDatabase, 
                couchDbConnector, organismeImporter, systemeReperageImporter,
                evenementHydrauliqueImporter, typeSystemeReleveProfilImporter);
    }

    public ConventionImporter getConventionImporter() {
        return conventionImporter;
    }

    public DocumentAGrandeEchelleImporter getDocumentAGrandeEchelleImporter() {
        return documentAGrandeEchelleImporter;
    }

    public JournalArticleImporter getJournalArticleImporter() {
        return journalArticleImporter;
    }

    public MarcheImporter getMarcheImporter() {
        return marcheImporter;
    }

    public ProfilEnTraversImporter getProfilEnTraversImporter() {
        return profilEnTraversImporter;
    }

    public RapportEtudeImporter getRapportEtudeImporter() {
        return rapportEtudeImporter;
    }

    public List<GenericDocumentRelatedImporter> getDocumentRelatedImporters() {
        return documentRelatedImporters;
    }
    
    // OTHER IMPORTERS : NOT DOCUMENT RELATED !!!!!!!
    public ProfilEnLongImporter getProfilEnLongImporter() {
        return profilEnLongImporter;
    }
}
