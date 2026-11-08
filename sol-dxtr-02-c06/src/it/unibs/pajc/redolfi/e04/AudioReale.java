package it.unibs.pajc.redolfi.e04;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

/** L'audio vero: due clip caricati una volta, nel costruttore. Si costruisce nel main e si passa. */
public class AudioReale implements Audio {

    private final Clip toc, pop;

    public AudioReale() throws Exception {
        toc = AudioSystem.getClip();
        toc.open(SuoniGlobali.sintetizza(440, 0.08));
        pop = AudioSystem.getClip();
        pop.open(SuoniGlobali.sintetizza(1200, 0.12));
    }

    private static void suona(Clip clip, double volume) {
        FloatControl gain =
                (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        double dB = 20 * Math.log10(Math.max(volume, 1e-3));
        gain.setValue((float) Math.max(gain.getMinimum(), dB));
        clip.setFramePosition(0);
        clip.start();
    }

    @Override
    public void toc(double volume) { suona(toc, volume); }

    @Override
    public void pop() { suona(pop, 0.8); }
}
