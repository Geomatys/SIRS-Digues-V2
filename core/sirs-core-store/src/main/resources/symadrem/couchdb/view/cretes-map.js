function(doc) {
   if(doc['@class']=='fr.symadrem.sirs.core.model.TronconDigue') {
     for(i in doc.stuctures){
        if(doc.stuctures[i]['@class']=='fr.symadrem.sirs.core.model.Crete'){
        	var newMap = {};
        	var myMap = doc.stuctures[i];
        	for (var i in myMap)
        	 newMap[i] = myMap[i];
        	 newMap["documentId"] = doc._id;
        	 emit(doc._id, newMap)            
        }
     }
   }
}