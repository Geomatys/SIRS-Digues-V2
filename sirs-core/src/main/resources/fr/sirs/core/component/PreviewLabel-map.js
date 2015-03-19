function(doc) {
    if(doc['@class']){
        var label
        if(doc.libelle) label=doc.libelle
        else if (doc.nom) label=doc.nom
        else label=""

        var designation
        if(doc.designation) designation=doc.designation
        else designation=""

        emit(doc._id, {libelle: label, type: doc['@class'], objectId: doc._id, designation: designation})
    }
}