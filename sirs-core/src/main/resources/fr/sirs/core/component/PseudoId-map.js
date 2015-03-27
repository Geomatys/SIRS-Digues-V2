function(doc) {

    //var SEARCH_ID='RefProprietaire:2';

    // On parcours l'ensemble des clefs
    var objectKeys = Object.keys(doc);
    for (key in objectKeys) {

        // Si le champ est une chaine de caractère, on regarde si sa valeur correspond à l'ID recherché
        if (objectKeys[key] === "designation") {
            searchDocumentID(doc);
        }

        // Si c'est un objet, il faut procéder récursivement
        else if (Array.isArray(doc[objectKeys[key]])) {
            parseTab(doc, doc[objectKeys[key]]);
        }
        else if (typeof doc[objectKeys[key]] === "object") {
            parseObject(doc, doc[objectKeys[key]]);
        }
    }

    /**
     * Si le contenu de field correspond à l'ID recherché, pour un document.
     */
    function searchDocumentID(object) {
        //if(object[field] === SEARCH_ID)
        var label;
        if(object.nom) label = object.nom;
        else if(object.libelle) label = object.libelle;
        else if(object.login) label = object.login;
        emit(object['@class'], {docId: object._id, docClass: object['@class'], elementId: null, elementClass: null, author: object.author, valid: object.valid, designation: object.designation, label: label});
    }

    /**
     * Si le contenu de field correspond à l'ID recherché, pour un élément non document.
     */
    function searchElementID(docu, object) {
        //if(object[field] === SEARCH_ID)
        var label;
        if(object.nom) label = object.nom;
        else if(object.libelle) label = object.libelle;
        else if(object.login) label = object.login;
        emit(object['@class'], {docId: docu._id, docClass: docu['@class'], elementId: object.id, elementClass: object['@class'], author: object.author, valid: object.valid, designation: object.designation, label: label});
    }

    /**
     * Analyse un tableau, avec l'ID de son élément le plus proche.
     */
    function parseTab(docu, tab) {
        for (var i = 0; i < tab.length; i++) {
            if (Array.isArray(tab[i])) {
                parseTab(docu, tab[i]);
            }
            else if (typeof tab[i] === "object") {
                parseObject(docu, tab[i]);
            }
        }
    }

    /**
     * Analyse un élément.
     */
    function parseObject(docu, object) {

        var objectKeys = Object.keys(object);

        for (key1 in objectKeys) {

            // Si le champ est une chaine de caractère, on regarde si sa valeur correspond à l'ID recherché
            if (objectKeys[key1] === "designation") {
                searchElementID(docu, object);
            }

            // Si c'est un objet, il faut procéder récursivement
            if (typeof object[objectKeys[key1]] === "object") {
                if (Array.isArray(object[objectKeys[key1]])) {
                    
                    var label;
                    if(object.nom) label = object.nom;
                    else if(object.libelle) label = object.libelle;
                    parseTab(docu, object[objectKeys[key1]]);
                }
                else {
                    parseObject(docu, object[objectKeys[key1]]);
                }
            }
        }
    }
}