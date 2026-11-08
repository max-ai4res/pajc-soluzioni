package it.unibs.pajc.redolfi.e04;

import java.io.ByteArrayInputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

/**
 * I suoni come Singleton "alla Java": un enum, una costante per suono, la risorsa
 * caricata una volta nel costruttore della costante, accessibile da ovunque con
 * SuoniGlobali.TOC.suona(). Corretto e comodo; il capitolo 6 dice che cosa costa.
 */
public enum SuoniGlobali {

    TOC(440, 0.08), POP(1200, 0.12);

    private final Clip clip;

    SuoniGlobali(double frequenza, double durata) {
        try {
            clip = AudioSystem.getClip();
            clip.open(sintetizza(frequenza, durata));
        } catch (Exception e) {
            throw new IllegalStateException("audio non disponibile", e);
        }
    }

    public void suona(double volume) {
        FloatControl gain =
                (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        double dB = 20 * Math.log10(Math.max(volume, 1e-3));
        gain.setValue((float) Math.max(gain.getMinimum(), dB));
        clip.setFramePosition(0);
        clip.start();
    }

    static AudioInputStream sintetizza(double frequenza, double durata) {
        float freq = 44100;
        int n = (int) (freq * durata);
        byte[] dati = new byte[2 * n];
        for (int i = 0; i < n; i++) {
            double t = i / freq;
            double onda = Math.sin(2 * Math.PI * frequenza * t) * Math.exp(-t * 30);
            short v = (short) (Short.MAX_VALUE * onda);
            dati[2 * i] = (byte) v;
            dati[2 * i + 1] = (byte) (v >> 8);
        }
        return new AudioInputStream(new ByteArrayInputStream(dati), new AudioFormat(freq, 16, 1, true, false), n);
    }
}
