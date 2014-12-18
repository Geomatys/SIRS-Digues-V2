function(doc) {
  if(doc['@class']=='fr.sirs.core.model.TronconDigue') {
    var viewDocument = {};
    for (var k in doc){
      if("structures" != k && "borneIds" != k)		   
        viewDocument[k] = doc[k];
    }
    emit(doc._id, viewDocument)                 
   }
}
 