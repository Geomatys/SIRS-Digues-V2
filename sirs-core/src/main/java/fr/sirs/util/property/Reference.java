package fr.sirs.util.property;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation permettant de connaître, pour un identifiant ou une liste 
 * d'identifiants, le type d'objet correspondant.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Internal
public @interface Reference  {
    public abstract Class<?> ref();
}
