/**
 * This file is part of SIRS-Digues 2.
 *
 * Copyright (C) 2021, FRANCE-DIGUES,
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

import static fr.sirs.core.component.AbstractTronconDigueRepository.PHOTOS_BY_TRONCON_ID;
import fr.sirs.core.model.Photo;
import fr.sirs.core.model.TronconDigue;
import java.util.List;
import org.ektorp.CouchDbConnector;
import org.ektorp.ViewQuery;
import org.ektorp.support.View;
import org.ektorp.support.Views;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author maximegavens
 */
@Views({
    @View(name = PhotoRepository.PHOTOS_BY_TRONCON_ID, map = "classpath:TronconDigue_photos.js")
})
@Component
public class PhotoRepository extends AbstractSIRSRepository<Photo> {

    public static final String PHOTOS_BY_TRONCON_ID = "photosByTronconId";

    @Autowired
    private PhotoRepository(CouchDbConnector db) {
        super(Photo.class, db);
        initStandardDesignDocument();
    }

    @Override
    public Photo create() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public List<Photo> getPhotosByTronconId(final String tronconId) {
        final ViewQuery viewQuery = createQuery(PHOTOS_BY_TRONCON_ID).includeDocs(false).key(tronconId);
        return db.queryView(viewQuery, Photo.class);
    }

    public List<Photo> getAllTronconPhotos() {
        final ViewQuery viewQuery = createQuery(PHOTOS_BY_TRONCON_ID).includeDocs(false);
        return db.queryView(viewQuery, Photo.class);
    }
}
