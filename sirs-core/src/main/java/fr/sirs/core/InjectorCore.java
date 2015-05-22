package fr.sirs.core;

import fr.sirs.core.component.DocumentChangeEmiter;
import fr.sirs.index.ElasticSearchEngine;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 *
 * @author Alexis Manin (Geomatys)
 * @author Olivier Nouguier (Géomatys)
 * @author Samuel Andrés (Géomatys)
 */
public class InjectorCore implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext ac) throws BeansException {
        applicationContext = ac;
    }

    public static void injectDependencies(Object o) {
        applicationContext.getAutowireCapableBeanFactory().autowireBean(o);
    }


    public static <T> T getBean(Class<T> clazz) {
        return applicationContext.getBean(clazz);
        
    }
    
    public static DocumentChangeEmiter getDocumentChangeEmiter(){
        return getBean(DocumentChangeEmiter.class);
    }
    
    public static ElasticSearchEngine getElasticSearchEngine(){
        return getBean(ElasticSearchEngine.class);
    }
    
}
