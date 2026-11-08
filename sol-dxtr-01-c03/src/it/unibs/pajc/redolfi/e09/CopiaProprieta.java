package it.unibs.pajc.redolfi.e09;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * Esercizio 3.9: copiare le proprieta' fra oggetti di classi diverse.
 *
 * Idea: due mappe nome -> Method. I getter della sorgente sono i metodi pubblici
 * d'istanza senza parametri che si chiamano getX (o isX se restituiscono boolean), piu',
 * per un record, i metodi che si chiamano come un componente. I setter della
 * destinazione sono i setX con un parametro. Il nome della proprieta' e' il resto del
 * nome con l'iniziale minuscola. Per ogni nome presente in entrambe le mappe, se il
 * tipo del parametro del setter accetta il tipo di ritorno del getter
 * (isAssignableFrom, con i primitivi confrontati per identita'), si copia.
 *
 * Errore tipico: confrontare i tipi con == e perdere le coppie in cui il setter
 * accetta un supertipo (setNome(CharSequence) da getNome() String), oppure includere
 * getClass() fra i getter: ogni oggetto ce l'ha, e nessuna destinazione ha setClass.
 */
public class CopiaProprieta {

    public static int copia(Object sorgente, Object destinazione) {
        Map<String, Method> getter = getter(sorgente.getClass());
        Map<String, Method> setter = setter(destinazione.getClass());
        int copiate = 0;
        for (Map.Entry<String, Method> e : getter.entrySet()) {
            Method set = setter.get(e.getKey());
            if (set == null)
                continue;
            Class<?> da = e.getValue().getReturnType(), a = set.getParameterTypes()[0];
            if (!a.isAssignableFrom(da))
                continue;
            try {
                set.invoke(destinazione, e.getValue().invoke(sorgente));
                copiate++;
            } catch (IllegalAccessException | InvocationTargetException ex) {
                System.out.println("non copio " + e.getKey() + ": " + ex);
            }
        }
        return copiate;
    }

    static Map<String, Method> getter(Class<?> c) {
        Map<String, Method> m = new HashMap<>();
        for (Method k : c.getMethods()) {
            if (k.getParameterCount() != 0 || Modifier.isStatic(k.getModifiers())
                    || k.getDeclaringClass() == Object.class || k.getReturnType() == void.class)
                continue;
            String n = k.getName();
            if (n.startsWith("get") && n.length() > 3)
                m.put(proprieta(n.substring(3)), k);
            else if (n.startsWith("is") && n.length() > 2 && k.getReturnType() == boolean.class)
                m.put(proprieta(n.substring(2)), k);
            else if (c.isRecord() && haComponente(c, n))
                m.put(n, k);
        }
        return m;
    }

    static Map<String, Method> setter(Class<?> c) {
        Map<String, Method> m = new HashMap<>();
        for (Method k : c.getMethods()) {
            String n = k.getName();
            if (k.getParameterCount() == 1 && !Modifier.isStatic(k.getModifiers()) && n.startsWith("set") && n.length() > 3)
                m.put(proprieta(n.substring(3)), k);
        }
        return m;
    }

    private static boolean haComponente(Class<?> record, String nome) {
        for (var rc : record.getRecordComponents())
            if (rc.getName().equals(nome))
                return true;
        return false;
    }

    private static String proprieta(String resto) {
        return Character.toLowerCase(resto.charAt(0)) + resto.substring(1);
    }

    // --- classi di prova

    static class Persona {
        private String nome, cognome; private int anni; private boolean iscritto; private double media;
        Persona(String nome, String cognome, int anni, boolean iscritto, double media) {
            this.nome = nome; this.cognome = cognome; this.anni = anni; this.iscritto = iscritto; this.media = media;
        }
        public String getNome() { return nome; }
        public String getCognome() { return cognome; }
        public int getAnni() { return anni; }
        public boolean isIscritto() { return iscritto; }
        public double getMedia() { return media; }
    }

    static class PersonaDto {
        private CharSequence nome; private String cognome; private int anni; private boolean iscritto; private String email;
        public void setNome(CharSequence nome) { this.nome = nome; }
        public void setCognome(String cognome) { this.cognome = cognome; }
        public void setAnni(int anni) { this.anni = anni; }
        public void setIscritto(boolean iscritto) { this.iscritto = iscritto; }
        public void setEmail(String email) { this.email = email; }
        public void setMedia(String media) { }                 // tipo diverso: non si copia
        @Override public String toString() {
            return "PersonaDto[nome=" + nome + ", cognome=" + cognome + ", anni=" + anni + ", iscritto=" + iscritto + ", email=" + email + "]";
        }
    }

    record Punto(int x, int y, String etichetta) { }

    static class PuntoMutabile {
        int x, y;
        public void setX(int x) { this.x = x; }
        public void setY(int y) { this.y = y; }
        @Override public String toString() { return "(" + x + ", " + y + ")"; }
    }

    public static void main(String[] args) {
        PersonaDto dto = new PersonaDto();
        int n = copia(new Persona("Ada", "Lovelace", 36, true, 29.5), dto);
        System.out.println(n + " proprieta' copiate: " + dto);
        PuntoMutabile p = new PuntoMutabile();
        System.out.println(copia(new Punto(3, 4, "A"), p) + " copiate dal record: " + p);
    }
}
