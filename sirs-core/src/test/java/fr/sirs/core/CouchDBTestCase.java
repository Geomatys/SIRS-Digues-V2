package fr.sirs.core;

import fr.sirs.core.component.SirsDBInfoRepository;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;


import org.ektorp.CouchDbConnector;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public abstract class CouchDBTestCase {

    protected static CouchDbConnector connector;

    private static SirsDBInfoRepository sirsDBInfoRepository;
    
    static {        
        final ClassPathXmlApplicationContext applicationContextParent = new ClassPathXmlApplicationContext();
        applicationContextParent.refresh();

        final ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext(new String[]{
            "classpath:/spring/test/test-context.xml"}, applicationContextParent);
        
        connector = applicationContext.getBean(CouchDbConnector.class);
        sirsDBInfoRepository = applicationContext.getBean(SirsDBInfoRepository.class);
        
        Optional<SirsDBInfo> init = sirsDBInfoRepository.init();
        if (!init.isPresent())
            sirsDBInfoRepository.create("1.0.1", "EPSG:2154");
    }

    public void test() {
        System.out.println(connector.getAllDocIds());
    }

    protected void dumpAll(List<?> list) {
        if (list != null) {
            for (Object object : list) {
                System.out.println(object);
            }
        }
    }

    protected void log(String message) {
        SirsCore.LOGGER.log(Level.INFO, message);
    }
}
