package fr.symadrem.sirs.core;

import java.util.List;

public interface Repository<T> {

    List<T> getAll();
    
    T get(String id);
    
    void add(T t);
    
    void update(T t);
    
    void remove(T t);
    
    Class<T> getModelClass();
    
    
    T create();
    
}
