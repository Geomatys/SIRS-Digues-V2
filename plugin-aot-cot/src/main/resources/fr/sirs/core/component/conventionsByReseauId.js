function(doc) {
    if(doc['@class']=='fr.sirs.core.model.Convention') {
//        emit(doc._id, doc._id)

        var objectKeys = Object.keys(doc);
        for (key in objectKeys) {
            // Si on a un attribut de type tableau on parse son contenu.
            if (Array.isArray(doc[objectKeys[key]])) {
                parseTab(doc, doc[objectKeys[key]]);
            }
            else if (typeof doc[objectKeys[key]] === 'string'){
                emit(doc[objectKeys[key]], doc);
            }
        }
    }
    
    

    /**
     * Analyse un tableau.
     */
    function parseTab(docu, tab) {
        for (var i = 0; i < tab.length; i++) {
            if (typeof tab[i] === 'string') {
                emit(tab[i], docu);
            }
        }
    }
}