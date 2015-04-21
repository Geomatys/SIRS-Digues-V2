function(doc) {
   if(doc['@class']=='fr.sirs.core.model.TronconDigue') {
     for(i in doc.proprietes){
        if(doc.proprietes[i]['@class']=='fr.sirs.core.model.ProprieteTroncon'){
            var newMap = {};
            var myMap = doc.proprietes[i];
            for (var i in myMap)
             newMap[i] = myMap[i];
             newMap["documentId"] = doc._id;
             emit(doc._id, newMap)    
        }
     }
   }
}
 