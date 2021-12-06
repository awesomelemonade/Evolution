package lemon.evolution.audio;

import javax.sound.sampled.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class BackgroundAudio {
    private static boolean initialized = false;
    private static boolean playing = false;
    private static Clip clip;
    public static void init() {
        if (initialized) {
            return;
        }
        try {
            clip = AudioSystem.getClip();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
        initialized = true;
    }

    public static void play(Track track) {
        stop();
        try {
            var url = BackgroundAudio.class.getResource("/res/" + track.filename());
            var audioStream = AudioSystem.getAudioInputStream(url);
            clip.open(audioStream);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            playing = true;
        } catch (LineUnavailableException | IOException | UnsupportedAudioFileException e) {
            e.printStackTrace();
        }
    }
    public static void stop() {
        if (playing) {
            clip.close();
            playing = false;
        }
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
