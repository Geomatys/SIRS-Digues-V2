function(doc) {
    if(doc._deleted) {
        emit({_id: doc._id, rev: doc._rev, _deleted: doc._deleted})
    }
}