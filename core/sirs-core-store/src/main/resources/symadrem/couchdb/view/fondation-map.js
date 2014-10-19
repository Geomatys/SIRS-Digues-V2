function(doc) {
   if(doc['@class']=='fr.symadrem.sirs.core.model.TronconDigue') {
     for(i in doc.stuctures){
        if(doc.stuctures[i]['@class']=='fr.symadrem.sirs.core.model.Fondation'){
            emit(doc._id, doc.stuctures[i])
        }
     }
   }
}