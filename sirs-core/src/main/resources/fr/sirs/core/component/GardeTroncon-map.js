function(doc) {
   if(doc['@class']=='fr.sirs.core.model.TronconDigue') {
     for(i in doc.gardes){
        if(doc.gardes[i]['@class']=='fr.sirs.core.model.GardeTroncon'){
            var newMap = {};
            var myMap = doc.gardes[i];
            for (var i in myMap)
             newMap[i] = myMap[i];
             newMap["documentId"] = doc._id;
             emit(doc._id, newMap)    
        }
     }
   }
}
 