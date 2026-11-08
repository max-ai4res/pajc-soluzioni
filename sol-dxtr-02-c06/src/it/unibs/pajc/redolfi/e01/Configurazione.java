package it.unibs.pajc.redolfi.e01;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Seconda versione: una classe normale. Si costruisce dove si vuole (da una risorsa, da
 * valori dati) e si passa a chi la usa; un test ne costruisce una con la gravita' che gli
 * serve.
 */
public record Configurazione(double larghezza, double gravita, double volume) {

    public static Configurazione daRisorsa() {
        try (InputStream in = Configurazione.class.getResourceAsStream("/e01/config.properties")) {
            if (in == null)
                throw new IllegalStateException("manca /e01/config.properties");
            Properties p = new Properties();
            p.load(in);
            return new Configurazione(Double.parseDouble(p.getProperty("larghezza", "8")),
                    Double.parseDouble(p.getProperty("gravita", "9.81")),
                    Double.parseDouble(p.getProperty("volume", "1")));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
