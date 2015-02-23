function(doc) {

    // On parcours l'ensemble des clefs
    var objectKeys = Object.keys(doc);
    for (key in objectKeys) {

        // Si le NOM du champ est valid, le document est bien un élément validable et on l'analyse en conséquence.
        if (objectKeys[key] === "valid") {
            searchDocumentID(doc);
        }

        // Si la VALEUR du champ est un objet, il faut procéder récursivement en séparant les cas du tableau et de l'objet.
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
    function searchDocumentID(docu) {
        var label;
        if(docu.nom) label = docu.nom;
        else if(docu.libelle) label = docu.libelle;
        emit(docu.valid, {docId: docu._id, docClass: docu['@class'], elementId: null, elementClass: null, author: docu.author, valid: docu.valid, pseudoId: docu.pseudoId, label: label});
    }

    /**
     * Si le contenu de field correspond à l'ID recherché, pour un élément non document.
     */
    function searchElementID(docu, object) {
        var label;
        if(object.nom) label = object.nom;
        else if(object.libelle) label = object.libelle;
        emit(object.valid, {docId: docu._id, docClass: docu['@class'], elementId: object.id, elementClass: object['@class'], author: object.author, valid: object.valid, pseudoId: object.pseudoId, label: label});
    }

    /**
     * Analyse un tableau, avec l'ID de son élément le plus proche.
     */
    function parseTab(docu, tab) {
        // Pour chacun des éléments, on regarde s'il s'agit d'un tableau ou d'un objet.
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

        // On récupère les noms des champs.
        var objectKeys = Object.keys(object);

        for (key1 in objectKeys) {

            // Si le NOM du champ est valid, l'objet est validable et on l'explore en conséquence.
            if (objectKeys[key1] === "valid") {
                searchElementID(docu, object);
            }

            // Si la VALEUR du champ est un objet, il faut procéder récursivement
            if (typeof object[objectKeys[key1]] === "object") {
                if (Array.isArray(object[objectKeys[key1]])) {
                    
//                    var label;
//                    if(object.nom) label = object.nom;
//                    else if(object.libelle) label = object.libelle;
                    parseTab(docu, object[objectKeys[key1]]);
                }
                else {
                    parseObject(docu, object[objectKeys[key1]]);
                }
            }
        }
    }
}