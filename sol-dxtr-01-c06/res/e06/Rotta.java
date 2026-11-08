import java.util.List;

public class Rotta {
	private List nomi = new java.util.ArrayList();     // avviso: raw type

	int conta(String s) {
		int x = "ciao";                              // errore: tipo
		return s.length();
	}

	class Interna { }
	Runnable r = () -> { };
}
