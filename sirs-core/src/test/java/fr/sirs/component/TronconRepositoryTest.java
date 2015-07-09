/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.component;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.sirs.core.CouchDBTestCase;
import fr.sirs.core.JacksonIterator;
import fr.sirs.core.component.TronconDigueRepository;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.Objet;
import fr.sirs.core.model.TronconDigue;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.sirs.core.TronconUtils;

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
        for (TronconDigue troncon : tronconRepository.getAll()) {
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
        try (JacksonIterator<TronconDigue> allAsStream = tronconRepository
                .getAllIterator()) {
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
        try (JacksonIterator<TronconDigue> allAsStream = tronconRepository
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
