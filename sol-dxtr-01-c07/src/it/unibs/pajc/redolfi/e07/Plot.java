package it.unibs.pajc.redolfi.e07;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Come una funzione si presenta nel men\`u: il nome e l'intervallo di default. */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface Plot {
    String nome();
    double xMin() default -10;
    double xMax() default 10;
}
