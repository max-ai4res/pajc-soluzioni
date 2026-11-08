package it.unibs.pajc.redolfi.e06;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Il contenitore che il compilatore usa quando @Autore e' ripetuta. */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface Autori {
    Autore[] value();
}
