function(doc) {
   if(doc['@class']=='fr.sirs.core.model.TronconDigue') {
     for(i in doc.documentTroncon){
            var newMap = {};
            var myMap = doc.documentTroncon[i];
            for (var i in myMap)
             newMap[i] = myMap[i];
             newMap["documentId"] = doc._id;
             emit(doc._id, newMap)    
     }
   }
}
 