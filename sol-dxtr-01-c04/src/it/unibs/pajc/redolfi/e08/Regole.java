package it.unibs.pajc.redolfi.e08;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Le tre annotazioni di validazione, raccolte in una classe per comodita'. */
public final class Regole {
    private Regole() { }

    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.FIELD)
    public @interface NonNullo { }

    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.FIELD)
    public @interface Intervallo { double min() default Double.NEGATIVE_INFINITY; double max() default Double.POSITIVE_INFINITY; }

    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.FIELD)
    public @interface Lunghezza { int min() default 0; int max() default Integer.MAX_VALUE; }
}
