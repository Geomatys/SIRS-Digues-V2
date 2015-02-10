function(doc) {

    //var SEARCH_ID='RefProprietaire:2';

    // On parcours l'ensemble des clefs
    var objectKeys = Object.keys(doc);
    for (key in objectKeys) {

        // Si le champ est une chaine de caractère, on regarde si sa valeur correspond à l'ID recherché
        if (typeof doc[objectKeys[key]] === "string") {
            searchDocumentID(doc, objectKeys[key]);
        }

        // Si c'est un objet, il faut procéder récursivement
        else if (Array.isArray(doc[objectKeys[key]])) {
            parseTab(doc[objectKeys[key]], doc._id);
        }
        else if (typeof doc[objectKeys[key]] === "object") {
            parseObject(doc[objectKeys[key]]);
        }
    }

    /**
     * Si le contenu de field correspond à l'ID recherché, pour un document.
     */
    function searchDocumentID(object, field) {
        //if(object[field] === SEARCH_ID)
        var label;
        if(object.nom) label = object.nom;
        else if(object.libelle) label = object.libelle;
        emit(object[field], {property: field, type: object['@class'], objectId: object._id, label: label});
    }

    /**
     * Si le contenu de field correspond à l'ID recherché, pour un élément non document.
     */
    function searchElementID(object, field) {
        //if(object[field] === SEARCH_ID)
        var label;
        if(object.nom) label = object.nom;
        else if(object.libelle) label = object.libelle;
        emit(object[field], {property: field, type: object['@class'], objectId: object.id, label: label});
    }

    function searchTabCellID(docId, docType, label, tabCell) {
        //if(tabCell === SEARCH_ID)
        emit(tabCell, {property: tabCell, type: docType, objectId: docId, label: label});
    }

    /**
     * Analyse un tableau, avec l'ID de son élément le plus proche.
     */
    function parseTab(tab, docId, label, docType) {
        for (var i = 0; i < tab.length; i++) {
            if (typeof tab[i] === "string") {
                searchTabCellID(docId, docType, label, tab[i]);
            }
            else if (Array.isArray(tab[i])) {
                parseTab(tab[i], docId, label, docType);
            }
            else if (typeof tab[i] === "object") {

                parseObject(tab[i]);
            }
        }
    }

    /**
     * Analyse un élément.
     */
    function parseObject(object) {

        var objectKeys = Object.keys(object);

        for (key1 in objectKeys) {

            // Si le champ est une chaine de caractère, on regarde si sa valeur correspond à l'ID recherché
            if (typeof object[objectKeys[key1]] === "string") {
                searchElementID(object, objectKeys[key1]);
            }

            // Si c'est un objet, il faut procéder récursivement
            if (typeof object[objectKeys[key1]] === "object") {
                if (Array.isArray(object[objectKeys[key1]])) {
                    
                    var label;
                    if(object.nom) label = object.nom;
                    else if(object.libelle) label = object.libelle;
                    parseTab(object[objectKeys[key1]], object.id, label, object['@class']);
                }
                else {
                    parseObject(objectKeys[key1]);
                }
            }
        }
    }
}