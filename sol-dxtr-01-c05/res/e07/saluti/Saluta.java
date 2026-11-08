package saluti;

/** Una classe da lanciare: compilare con  javac -d res/e07 res/e07/saluti/Saluta.java */
public class Saluta {
    public static void main(String[] args) {
        if (args.length == 0)
            throw new IllegalArgumentException("nessuno da salutare");
        for (String nome : args)
            System.out.println("ciao, " + nome + " (da " + Saluta.class.getClassLoader().getName() + ")");
    }
}
