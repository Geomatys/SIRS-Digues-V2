package fr.sirs.importer.objet;

import com.healthmarketscience.jackcess.Database;
import fr.sirs.core.model.Objet;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.BorneDigueImporter;
import fr.sirs.importer.IntervenantImporter;
import fr.sirs.importer.OrganismeImporter;
import fr.sirs.importer.SystemeReperageImporter;
import fr.sirs.importer.TronconGestionDigueImporter;
import fr.sirs.importer.evenementHydraulique.EvenementHydrauliqueImporter;
import fr.sirs.importer.objet.desordre.DesordreImporter;
import fr.sirs.importer.objet.geometry.ElementGeometrieImporter;
import fr.sirs.importer.objet.laisseCrue.LaisseCrueImporter;
import fr.sirs.importer.objet.ligneEau.LigneEauImporter;
import fr.sirs.importer.objet.link.DesordreElementReseauImporter;
import fr.sirs.importer.objet.link.DesordreElementStructureImporter;
import fr.sirs.importer.objet.link.ElementReseauAutreOuvrageHydrauImporter;
import fr.sirs.importer.objet.link.ElementReseauConduiteFermeeImporter;
import fr.sirs.importer.objet.link.ElementReseauOuvrageTelNrjImporter;
import fr.sirs.importer.objet.link.ElementReseauReseauEauImporter;
import fr.sirs.importer.objet.link.ElementReseauVoieSurDigueImporter;
import fr.sirs.importer.objet.link.GenericObjetLinker;
import fr.sirs.importer.objet.monteeDesEaux.MonteeDesEauxImporter;
import fr.sirs.importer.objet.prestation.PrestationImporter;
import fr.sirs.importer.objet.reseau.ElementReseauImporter;
import fr.sirs.importer.objet.structure.ElementStructureImporter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class ObjetManager {
    
    private final SourceInfoImporter typeSourceImporter;
    private final TypeCoteImporter typeCoteImporter;
    private final TypePositionImporter typePositionImporter;
    private final TypeMateriauImporter typeMateriauImporter;
    private final TypeNatureImporter typeNatureImporter;
    private final TypeFonctionImporter typeFonctionImporter;
    private final TypeRefHeauImporter typeRefHeauImporter;
    
    private final ElementStructureImporter structureImporter;
    private final DesordreImporter desordreImporter;
    private final ElementGeometrieImporter geometryImporter;
    private final ElementReseauImporter reseauImporter;
    private final PrestationImporter prestationImporter;
    private final LaisseCrueImporter laisseCrueImporter;
    private final LigneEauImporter ligneEauImporter;
    private final MonteeDesEauxImporter monteeDesEauxImporter;
    private final List<GenericObjetImporter> importers = new ArrayList<>();
    
    private final DesordreElementStructureImporter desordreStructureImporter;
    private final DesordreElementReseauImporter desordreElementReseauImporter;
    private final ElementReseauConduiteFermeeImporter reseauConduiteFermeeImporter;
    private final ElementReseauOuvrageTelNrjImporter reseauOuvrageTelecomImporter;
    private final ElementReseauAutreOuvrageHydrauImporter elementReseauAutreOuvrageHydrauImporter;
    private final ElementReseauReseauEauImporter elementReseauReseauEauImporter;
    private final ElementReseauVoieSurDigueImporter elementReseauVoieSurDigueImporter;
    private final List<GenericObjetLinker> linkers = new ArrayList<>();
    
    public ObjetManager(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector,
            final TronconGestionDigueImporter tronconGestionDigueImporter,
            final SystemeReperageImporter systemeReperageImporter,
            final BorneDigueImporter borneDigueImporter, 
            final OrganismeImporter organismeImporter,
            final IntervenantImporter intervenantImporter,
            final EvenementHydrauliqueImporter evenementHydrauliqueImporter
            ){
        
        typeSourceImporter = new SourceInfoImporter(accessDatabase, 
                couchDbConnector);
        typeCoteImporter = new TypeCoteImporter(accessDatabase, 
                couchDbConnector);
        typePositionImporter = new TypePositionImporter(accessDatabase, 
                couchDbConnector);
        typeMateriauImporter = new TypeMateriauImporter(accessDatabase, 
                couchDbConnector);
        typeNatureImporter = new TypeNatureImporter(accessDatabase, 
                couchDbConnector);
        typeFonctionImporter = new TypeFonctionImporter(accessDatabase, 
                couchDbConnector);
        typeRefHeauImporter = new TypeRefHeauImporter(accessDatabase, 
                couchDbConnector);
        
        structureImporter = new ElementStructureImporter(accessDatabase, 
                couchDbConnector, tronconGestionDigueImporter, systemeReperageImporter, 
                borneDigueImporter, organismeImporter, intervenantImporter, 
                typeSourceImporter, typePositionImporter, typeCoteImporter, 
                typeMateriauImporter, typeNatureImporter, typeFonctionImporter);
        importers.add(structureImporter);
        desordreImporter = new DesordreImporter(accessDatabase, 
                couchDbConnector, tronconGestionDigueImporter, systemeReperageImporter, 
                borneDigueImporter, typeSourceImporter, typePositionImporter, 
                typeCoteImporter);
        importers.add(desordreImporter);
        geometryImporter = new ElementGeometrieImporter(accessDatabase, 
                couchDbConnector, tronconGestionDigueImporter, systemeReperageImporter, 
                borneDigueImporter, typeSourceImporter);
        importers.add(geometryImporter);
        reseauImporter = new ElementReseauImporter(accessDatabase, 
                couchDbConnector, tronconGestionDigueImporter, systemeReperageImporter, 
                borneDigueImporter, organismeImporter, intervenantImporter, 
                typeSourceImporter, typePositionImporter, typeCoteImporter, 
                typeMateriauImporter, typeNatureImporter, typeFonctionImporter);
        importers.add(reseauImporter);
        prestationImporter = new PrestationImporter(accessDatabase, 
                couchDbConnector, tronconGestionDigueImporter, 
                systemeReperageImporter, borneDigueImporter,typeSourceImporter, 
                typeCoteImporter, typePositionImporter);
        importers.add(prestationImporter);
        laisseCrueImporter = new LaisseCrueImporter(accessDatabase, 
                couchDbConnector, tronconGestionDigueImporter, 
                systemeReperageImporter, borneDigueImporter, 
                intervenantImporter, evenementHydrauliqueImporter, 
                typeSourceImporter, typeRefHeauImporter);
        importers.add(laisseCrueImporter);
        ligneEauImporter = new LigneEauImporter(accessDatabase, 
                couchDbConnector, tronconGestionDigueImporter, systemeReperageImporter, 
                borneDigueImporter, organismeImporter, intervenantImporter, 
                evenementHydrauliqueImporter, structureImporter, 
                typeSourceImporter, typePositionImporter, typeCoteImporter, 
                typeMateriauImporter, typeNatureImporter, typeFonctionImporter,
                typeRefHeauImporter);
        importers.add(ligneEauImporter);
        monteeDesEauxImporter = new MonteeDesEauxImporter(accessDatabase, 
                couchDbConnector, tronconGestionDigueImporter, systemeReperageImporter, 
                borneDigueImporter, organismeImporter, intervenantImporter, 
                evenementHydrauliqueImporter, structureImporter, 
                typeSourceImporter, typePositionImporter, typeCoteImporter, 
                typeMateriauImporter, typeNatureImporter, typeFonctionImporter,
                typeRefHeauImporter);
        importers.add(monteeDesEauxImporter);
        
        
        desordreStructureImporter = new DesordreElementStructureImporter(
                accessDatabase, couchDbConnector, structureImporter, 
                desordreImporter);
        linkers.add(desordreStructureImporter);
        desordreElementReseauImporter = new DesordreElementReseauImporter(
                accessDatabase, couchDbConnector, reseauImporter, 
                desordreImporter);
        linkers.add(desordreElementReseauImporter);
        reseauConduiteFermeeImporter = new ElementReseauConduiteFermeeImporter(
                accessDatabase, couchDbConnector, reseauImporter);
        linkers.add(reseauConduiteFermeeImporter);
        reseauOuvrageTelecomImporter = new ElementReseauOuvrageTelNrjImporter(
                accessDatabase, couchDbConnector, reseauImporter);
        linkers.add(reseauOuvrageTelecomImporter);
        elementReseauAutreOuvrageHydrauImporter = new ElementReseauAutreOuvrageHydrauImporter(
                accessDatabase, couchDbConnector, reseauImporter);
        linkers.add(elementReseauAutreOuvrageHydrauImporter);
        elementReseauReseauEauImporter = new ElementReseauReseauEauImporter(
                accessDatabase, couchDbConnector, reseauImporter);
        linkers.add(elementReseauReseauEauImporter);
        elementReseauVoieSurDigueImporter = new ElementReseauVoieSurDigueImporter(
                accessDatabase, couchDbConnector, reseauImporter);
        linkers.add(elementReseauVoieSurDigueImporter);
    }
    
    public List<Objet> getByTronconId(final int tronconId) 
            throws IOException, AccessDbImporterException{
        final List<Objet> result = new ArrayList<>();
        for(final GenericObjetImporter goi : importers){
            final Map<Integer, List<Objet>> byTroncon = goi.getByTronconId();
            if(byTroncon.get(tronconId)!=null)
                result.addAll(byTroncon.get(tronconId));
        }
        return result;
    }
    
    public void link() throws IOException, AccessDbImporterException{
        for(final GenericObjetLinker gol : linkers){
            gol.link();
        }
    }
    
    
    public ElementStructureImporter getStructureImporter(){return structureImporter;}
    public DesordreImporter getDesordreImporter(){return desordreImporter;}
    public ElementGeometrieImporter getGeometryImporter(){return geometryImporter;}
}
