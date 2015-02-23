function(doc) {

    //var SEARCH_ID='RefProprietaire:2';

    // On parcours l'ensemble des clefs
    var objectKeys = Object.keys(doc);
    for (key in objectKeys) {

        // Si la VALEUR du champ est une chaine de caractère, l'emet pour analyse.
        if (typeof doc[objectKeys[key]] === "string") {
            searchDocumentID(doc, objectKeys[key]);
        }

        // Si la VALEUR du champ est un objet, il faut procéder récursivement en séparant les cas du tableau et de l'objet.
        else if (Array.isArray(doc[objectKeys[key]])) {
            var label;
            if(doc.nom) label = doc.nom;
            else if(doc.libelle) label = doc.libelle;
            parseTab(doc[objectKeys[key]], doc._id, label, doc['@class']);
        }
        else if (typeof doc[objectKeys[key]] === "object") {
            parseObject(doc[objectKeys[key]]);
        }
    }

    /**
     * Émission d'un champ chaine de caractère.
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
        // On parcours les cellules
        for (var i = 0; i < tab.length; i++) {
            // Si la cellule contient une chaîne de caractères
            if (typeof tab[i] === "string") {
                searchTabCellID(docId, docType, label, tab[i]);
            }
            // Si la cellule contient un tableau, on l'explore.
            else if (Array.isArray(tab[i])) {
                parseTab(tab[i], docId, label, docType);
            }
            // si la cellule contient un objet, on le parse.
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

            // Si la VALEUR du champ est une chaine de caractère.
            if (typeof object[objectKeys[key1]] === "string") {
                searchElementID(object, objectKeys[key1]);
            }

            // Si la valeur du champ est un objet, il faut procéder récursivement
            if (typeof object[objectKeys[key1]] === "object") {
                if (Array.isArray(object[objectKeys[key1]])) {
                    
                    var label;
                    if(object.nom) label = object.nom;
                    else if(object.libelle) label = object.libelle;
                    parseTab(object[objectKeys[key1]], object.id, label, object['@class']);
                }
                else {
                    parseObject(object[objectKeys[key1]]);
                }
            }
        }
    }
}