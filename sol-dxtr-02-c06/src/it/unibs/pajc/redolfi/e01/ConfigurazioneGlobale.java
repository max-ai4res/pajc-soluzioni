package it.unibs.pajc.redolfi.e01;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/** Prima versione: un enum singleton. Raggiungibile da ovunque, letto al primo accesso, non sostituibile. */
public enum ConfigurazioneGlobale {

    ISTANZA;

    private final Properties p = new Properties();

    ConfigurazioneGlobale() {
        try (InputStream in = ConfigurazioneGlobale.class.getResourceAsStream("/e01/config.properties")) {
            if (in == null)
                throw new IllegalStateException("manca /e01/config.properties");
            p.load(in);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public double larghezza() { return Double.parseDouble(p.getProperty("larghezza", "8")); }
    public double gravita()   { return Double.parseDouble(p.getProperty("gravita", "9.81")); }
    public double volume()    { return Double.parseDouble(p.getProperty("volume", "1")); }
}
