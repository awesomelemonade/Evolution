package lemon.evolution.audio;

import javax.sound.sampled.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class BackgroundAudio {
    private static final Map<Track, AudioInputStream> audioStreams = new HashMap<>();
    private static boolean initialized = false;
    private static boolean playing = false;
    private static Clip clip;
    public static void init() {
        if (initialized) {
            return;
        }
        try {
            clip = AudioSystem.getClip();
            for (var track : Track.values()) {
                var url = BackgroundAudio.class.getResource("/res/" + track.filename());
                audioStreams.put(track, AudioSystem.getAudioInputStream(url));
            }
        } catch (LineUnavailableException | UnsupportedAudioFileException | IOException e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
        initialized = true;
    }

    public static void play(Track track) {
        stop();
        try {
            clip.open(audioStreams.get(track));
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            playing = true;
        } catch (LineUnavailableException | IOException e) {
            e.printStackTrace();
        }
    }
    public static void stop() {
        if (playing) {
            clip.close();
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
