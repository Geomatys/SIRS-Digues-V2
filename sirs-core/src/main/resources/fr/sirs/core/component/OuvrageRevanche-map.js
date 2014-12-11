function(doc) {
   if(doc['@class']=='fr.sirs.core.model.TronconDigue') {
     for(i in doc.structures){
        if(doc.structures[i]['@class']=='fr.sirs.core.model.OuvrageRevanche'){
        	var newMap = {};
        	var myMap = doc.structures[i];
        	for (var i in myMap)
        	 newMap[i] = myMap[i];
        	 newMap["documentId"] = doc._id;
        	 emit(doc._id, newMap)            
        }
     }
   }
}
 