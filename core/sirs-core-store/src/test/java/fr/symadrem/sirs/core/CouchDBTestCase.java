package fr.symadrem.sirs.core;

import java.util.Iterator;
import java.util.List;

import org.ektorp.CouchDbConnector;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/spring/test/test-context.xml")
public abstract class CouchDBTestCase {

    @Autowired
    @Qualifier("symadremChouchDB")
    private CouchDbConnector connector;


    public void test() {
        System.out.println(connector.getAllDocIds());
    }
    
    
    protected void dumpAll(List<?> list) {
    	if(list != null) {
    		for (Object object : list) {
				System.out.println(object);
			}
    	}
    }

}
