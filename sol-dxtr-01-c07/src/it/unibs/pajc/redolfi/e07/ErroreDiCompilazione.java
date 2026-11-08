package it.unibs.pajc.redolfi.e07;

import java.util.List;
import java.util.Locale;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/** Il sorgente non compila: le diagnostiche del compilatore e il sorgente, per mostrarli. */
public class ErroreDiCompilazione extends Exception {

    private final String sorgente;
    private final List<Diagnostic<? extends JavaFileObject>> diagnostiche;

    public ErroreDiCompilazione(String sorgente, List<Diagnostic<? extends JavaFileObject>> diagnostiche) {
        super(diagnostiche.isEmpty() ? "compilazione fallita"
                : "riga " + diagnostiche.get(0).getLineNumber() + ": " + diagnostiche.get(0).getMessage(Locale.ITALIAN));
        this.sorgente = sorgente;
        this.diagnostiche = diagnostiche;
    }

    public String getSorgente() {
        return sorgente;
    }

    public List<Diagnostic<? extends JavaFileObject>> getDiagnostiche() {
        return diagnostiche;
    }
}
