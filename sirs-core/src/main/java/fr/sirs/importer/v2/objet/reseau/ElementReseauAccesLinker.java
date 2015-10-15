package fr.sirs.importer.v2.objet.reseau;


import static fr.sirs.importer.DbImporter.TableName.ELEMENT_RESEAU_POINT_ACCES;
import org.springframework.stereotype.Component;

/**
 *
 * Create links between object using an MS-Access join table.
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class ElementReseauAccesLinker extends AbstractElementReseauLinker {

    public ElementReseauAccesLinker() {
        super(ELEMENT_RESEAU_POINT_ACCES.name(), "ID_ELEMENT_RESEAU", "ID_ELEMENT_RESEAU_POINT_ACCES");
    }
}
