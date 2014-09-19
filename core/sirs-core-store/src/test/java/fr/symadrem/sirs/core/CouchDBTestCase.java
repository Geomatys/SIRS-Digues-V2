package fr.symadrem.sirs.core;

import static org.junit.Assert.*;

import org.ektorp.CouchDbConnector;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/spring/test/test-context.xml")
public class CouchDBTestCase {

    
    @Autowired
    private CouchDbConnector connector;
    
    @Test
    public void test() {
        System.out.println(connector.getAllDocIds());
    }

}
