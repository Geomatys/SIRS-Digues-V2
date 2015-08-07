function(doc) {
    if(doc['@class']=='fr.sirs.core.model.PositionConvention') {
        if(doc.objetId) {
            emit(doc.objetId, doc);
        }
    }
}