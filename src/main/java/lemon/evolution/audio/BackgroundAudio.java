package lemon.evolution.audio;

import lemon.engine.toolbox.Lazy;
import lemon.evolution.cmd.EvolutionOptions;

import javax.sound.sampled.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class BackgroundAudio {
    private static final Lazy<Clip> clip = new Lazy<>(() -> {
        try {
            return AudioSystem.getClip();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    });
    private static boolean playing = false;

    public static void play(Track track) {
        stop();
        if (EvolutionOptions.ENABLE_MUSIC) {
            try {
                var url = BackgroundAudio.class.getResource("/res/" + track.filename());
                var audioStream = AudioSystem.getAudioInputStream(url);
                var clip = clip();
                clip.open(audioStream);
                clip.loop(Clip.LOOP_CONTINUOUSLY);
                playing = true;
            } catch (LineUnavailableException | IOException | UnsupportedAudioFileException e) {
                e.printStackTrace();
            }
        }
    }

    public static void stop() {
        if (playing) {
            clip().close();
            playing = false;
        }
    }

    private static Clip clip() {
        return clip.get();
    }

    public enum Track {
        MENU("music2.wav"), COMBAT("music.wav");

        private final String filename;

        private Track(String filename) {
            this.filename = filename;
        }

        public String filename() {
            return filename;
        }
    }
}
