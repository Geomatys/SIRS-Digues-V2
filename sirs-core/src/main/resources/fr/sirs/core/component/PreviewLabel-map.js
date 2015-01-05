function(doc) {
    if(doc.libelle) {
        emit(doc._id, {libelle: doc.libelle, type: doc['@class'], objectId: doc._id})
    } 
    else if (doc.nom) {
        emit(doc._id, {libelle: doc.nom, type: doc['@class'], objectId: doc._id})
    }
}