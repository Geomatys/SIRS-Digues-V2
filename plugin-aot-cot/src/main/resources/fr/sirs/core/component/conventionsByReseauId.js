function(doc) {
    if(doc['@class']=='fr.sirs.core.model.Convention') {
        if(doc.portees) {
            parseTab(doc, doc.portees);
        }
    }
    
    /**
     * Analyse un tableau, avec l'ID de son élément le plus proche.
     */
    function parseTab(docu, tab) {
        for (var i = 0; i < tab.length; i++) {
            parsePortee(docu, tab[i]);
        }
    }

    /**
     * Analyse un élément.
     */
    function parsePortee(docu, object) {
        if(object.objetReseauId){
            emit(object.objetReseauId, docu);
        }
    }

}

// Ancienne version lorsque les objets étaient directement référencés par les conventions
//function(doc) {
//    if(doc['@class']=='fr.sirs.core.model.Convention') {
//
//        var objectKeys = Object.keys(doc);
//        for (key in objectKeys) {
//            // Si on a un attribut de type tableau on parse son contenu.
//            if (Array.isArray(doc[objectKeys[key]])) {
//                parseTab(doc, doc[objectKeys[key]]);
//            }
//            else if (typeof doc[objectKeys[key]] === 'string'){
//                emit(doc[objectKeys[key]], doc);
//            }
//        }
//    }
//    
//    
//
//    /**
//     * Analyse un tableau.
//     */
//    function parseTab(docu, tab) {
//        for (var i = 0; i < tab.length; i++) {
//            if (typeof tab[i] === 'string') {
//                emit(tab[i], docu);
//            }
//        }
//    }
//}