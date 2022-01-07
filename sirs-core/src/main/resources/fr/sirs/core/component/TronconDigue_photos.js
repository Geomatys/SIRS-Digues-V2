function(doc) {
  if(doc['@class']=='fr.sirs.core.model.TronconDigue') {
    if (doc['photos']) {
      for (const photo of doc['photos']) {
          emit(doc._id, photo);
      }
    }
  }
}
