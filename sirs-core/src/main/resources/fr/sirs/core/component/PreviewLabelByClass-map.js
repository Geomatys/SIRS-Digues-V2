function(doc) {
    if(doc['@class']){
        var label
        if(doc.libelle) label=doc.libelle
        else if (doc.nom) label=doc.nom
        else label=""

        var pseudoId
        if(doc.pseudoId) pseudoId=doc.pseudoId
        else pseudoId=""

        emit(doc['@class'], {libelle: label, type: doc['@class'], objectId: doc._id, pseudoId: pseudoId})
    }
}