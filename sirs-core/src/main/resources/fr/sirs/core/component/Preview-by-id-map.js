function(doc) {
    // On transmet le document, puis on va parcourir l'ensemble de ses clefs pour trouver les élements contenus.
    emitDocument(doc);
    var objectKeys = Object.keys(doc);
    for (key in objectKeys) {
        // Si on a un attribut de type tableau ou objet, on recherche un element ou une collection d'élements à l'interieur.
        if (Array.isArray(doc[objectKeys[key]])) {
            parseTab(doc, doc[objectKeys[key]]);
        }
        else if (typeof doc[objectKeys[key]] === "object") {
            parseObject(doc, doc[objectKeys[key]]);
        }
    }

    /**
     * Si le contenu de field correspond à l'ID recherché, pour un document.
     */
    function emitDocument(object) {
        var label;
        if(object.nom) label = object.nom;
        else if(object.libelle) label = object.libelle;
        else if(object.login) label = object.login;
        emit(object._id, {docId: object._id, docClass: object['@class'], elementId: object._id, elementClass: object['@class'], author: object.author, valid: object.valid, designation: object.designation, libelle: label});
    }

    /**
     * Si le contenu de field correspond à l'ID recherché, pour un élément non document.
     */
    function emitInnerElement(docu, object) {
        //if(object[field] === SEARCH_ID)
        var label;
        if(object.nom) label = object.nom;
        else if(object.libelle) label = object.libelle;
        else if(object.login) label = object.login;
        emit(object.id, {docId: docu._id, docClass: docu['@class'], elementId: object.id, elementClass: object['@class'], author: object.author, valid: object.valid, designation: object.designation, libelle: label});
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
        // On transmet le document, puis on va parcourir l'ensemble de ses clefs pour trouver les élements contenus.
        emitInnerElement(docu, object);
        var objectKeys = Object.keys(object);
        for (key1 in objectKeys) {
            // Si c'est un objet, il faut procéder récursivement
            if (Array.isArray(object[objectKeys[key1]])) {
                parseTab(docu, object[objectKeys[key1]]);
            }
            else if (typeof object[objectKeys[key1]] === "object") {
                parseObject(docu, object[objectKeys[key1]]);
            }
        }
    }
}