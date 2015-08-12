package fr.sirs.util.property;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation indiquant, en particulier si un attribut est à usage interne.
 * Cela est utile afin de déterminer, en particulier, s'il doit être affiché.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Internal {
	
}
