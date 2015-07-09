package fr.sirs.core.component;

import fr.sirs.core.model.SQLQuery;
import java.util.List;
import org.ektorp.CouchDbConnector;
import org.ektorp.support.View;
import org.ektorp.support.Views;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * A repository to store SQL queries of users into database.
 * The SQL queries are used to query the temporary SQL dump of the couchDB repository.
 *
 * TODO : use cache as other repositories.
 *
 * @author Alexis Manin (Geomatys)
 */
@Component("fr.sirs.core.component.SQLQueryRepository")
@Views({
        @View(name="all", map="function(doc) {if(doc['@class']=='fr.sirs.core.model.SQLQuery') {emit(doc._id, doc._id)}}"),
        @View(name = "byLibelle", map = "function(doc) {if(doc['@class']=='fr.sirs.core.model.SQLQuery') {emit(doc.libelle, doc._id)}}") })
public class SQLQueryRepository  extends AbstractSIRSRepository<SQLQuery>{

    @Autowired
    private SQLQueryRepository (CouchDbConnector db) {
       super(SQLQuery.class, db);
       initStandardDesignDocument();
   }

    @Override
    public Class<SQLQuery> getModelClass() {
        return SQLQuery.class;
    }

    @Override
    public SQLQuery create(){
        return new SQLQuery();
    }

    public List<SQLQuery> getByLibelle(final String name) {
        return this.queryView("byLibelle", name);
    }
}