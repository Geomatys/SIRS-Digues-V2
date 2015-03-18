function(doc) {
   if(doc['@class']=='fr.sirs.core.model.TronconDigue') {
     for(d in doc.documentTroncon){
            var newMap = {};
            var myMap = doc.documentTroncon[d];
            for (var i in myMap)
             newMap[i] = myMap[i];
             newMap["documentId"] = doc._id;
             emit(newMap["sirsdocument"], newMap)    
     }
   }
}
 