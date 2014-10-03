/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sym.digue;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import fr.sym.Session;

/**
 *
 * @author Olivier Nouguier (Géomatys)
 * @author Samuel Andrés (Géomatys)
 */
@Component
public class Injector implements ApplicationContextAware {

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
}
