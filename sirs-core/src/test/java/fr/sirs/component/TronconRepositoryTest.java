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
package fr.sirs.component;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.sirs.core.CouchDBTestCase;
import fr.sirs.core.SirsViewIterator;
import fr.sirs.core.component.TronconDigueRepository;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.Objet;
import fr.sirs.core.model.TronconDigue;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.sirs.core.TronconUtils;
import org.geotoolkit.util.collection.CloseableIterator;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class TronconRepositoryTest extends CouchDBTestCase {

    @Autowired
    private TronconDigueRepository tronconRepository;

    /**
     * Test of getAll method, of class TronconDigueRepository.
     */
    @Test
    public void testGetAll() {
        System.out.println("getAll");
        for (TronconDigue troncon : tronconRepository.getAllStreaming()) {
            System.out.println(troncon);
            for (Objet struct : TronconUtils.getObjetList(troncon)) {
                System.out.println("DocuumentId: " + struct.getDocumentId());

            }
            TronconDigue copy = troncon.copy();
            System.out.println(copy.getCommentaire());
            tronconRepository.add(copy);
        }

    }

    /**
     * Test of getAll method, of class TronconDigueRepository.
     */
    @Test
    public void testGetAllAsStream() {
        System.out.println("getAllAsStream");
        try (CloseableIterator<TronconDigue> allAsStream = tronconRepository
                .getAllStreaming().iterator()) {
            while (allAsStream.hasNext()) {
                TronconDigue troncon = allAsStream.next();
                System.out.println(troncon);
                for (Objet struct : TronconUtils.getObjetList(troncon)) {
                    System.out
                            .println("DocuumentId: " + struct.getDocumentId());

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Test of getAll method, of class TronconDigueRepository.
     */
    @Test
    public void testGetAllLightAsStream() {
        System.out.println("getAllAsStream");
        try (SirsViewIterator<TronconDigue> allAsStream = tronconRepository
                .getAllLightIterator()) {
            while (allAsStream.hasNext()) {
                TronconDigue troncon = allAsStream.next();
                System.out.println(troncon);
                for (Objet struct : TronconUtils.getObjetList(troncon)) {
                    System.out
                            .println("DocuumentId: " + struct.getDocumentId());

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    @Test
//    public void listAllFondations() {
//        final TronconDigueRepository tronconRepository = new TronconDigueRepository(
//                couchDbConnector);
//        List<Fondation> all = tronconRepository.getAllFondations();
//        dumpAllStructure(all);
//
//    }

//    @Test
//    public void listAllFondationsAsStream() {
//        final TronconDigueRepository tronconRepository = new TronconDigueRepository(
//                couchDbConnector);
//        try (StreamingViewResult all = tronconRepository
//                .getAllFondationsIterator()) {
//            System.out.println(all.getTotalRows());
//            if (all.getTotalRows() == 0)
//                return;
//            Iterator<Row> iterator = all.iterator();
//            while (iterator.hasNext()) {
//                Row next = iterator.next();
//                JsonNode docAsNode = next.getValueAsNode();
//                JsonNode jsonNode = docAsNode.get("@class");
//                if (jsonNode == null)
//                    continue;
//                String json = jsonNode.asText();
//                Optional<Class<?>> asClass = DocHelper.asClass(json);
//                toElement(next.getValue(), asClass.get()).ifPresent(
//                        el -> System.out.println(el));
//
//            }
//        }
//    }

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Optional<Element> toElement(String str, Class<?> clazz) {
        try {
            return Optional.of((Element) objectMapper.reader(clazz).readValue(
                    str));
        } catch (IOException e) {
            return Optional.empty();
        }

    }

//    @Test
//    public void listAllCretes() {
//        final TronconDigueRepository tronconRepository = new TronconDigueRepository(
//                couchDbConnector);
//        List<Crete> all = tronconRepository.getAllCretes();
//        dumpAllStructure(all);
//
//    }
//
//    @Test
//    public void listAllPiedDigue() {
//        final TronconDigueRepository tronconRepository = new TronconDigueRepository(
//                couchDbConnector);
//        List<PiedDigue> all = tronconRepository.getAllPiedDigues();
//        dumpAllStructure(all);
//
//    }

    private void dumpAllStructure(List<? extends Objet> allFondations) {
        for (Objet fondation : allFondations) {
            System.out.println(fondation.getId() + " / "
                    + fondation.getDocumentId());
        }
    }
}
