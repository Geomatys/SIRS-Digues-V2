/**
 * This file is part of SIRS-Digues 2.
 *
 * Copyright (C) 2016, FRANCE-DIGUES,
 * 
 * SIRS-Digues 2 is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * SIRS-Digues 2 is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SIRS-Digues 2. If not, see <http://www.gnu.org/licenses/>
 */
package fr.sirs.core.component;

import fr.sirs.core.model.SQLQuery;
import java.util.List;
import org.ektorp.CouchDbConnector;
import org.ektorp.support.View;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * A repository to store SQL queries of users into database.
 * The SQL queries are used to query the temporary SQL dump of the couchDB repository.
 *
 * @author Alexis Manin (Geomatys)
 */
@Component("fr.sirs.core.component.SQLQueryRepository")
@View(name = "byLibelle", map = "function(doc) {if(doc['@class']=='fr.sirs.core.model.SQLQuery') {emit(doc.libelle, doc._id)}}")
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
