package fr.sirs.importer.v2.objet.reseau;

import fr.sirs.importer.DbImporter;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class ElementReseauVoieDigueLinker extends AbstractElementReseauLinker {

    public ElementReseauVoieDigueLinker() {
        super(DbImporter.TableName.ELEMENT_RESEAU_VOIE_SUR_DIGUE.name(), "ID_ELEMENT_RESEAU", "ID_ELEMENT_RESEAU_VOIE_SUR_DIGUE");
    }

}
