package lemon.engine.font;

import lemon.engine.toolbox.Lazy;

import java.nio.file.Paths;

public class CommonFonts {
    private static final Lazy<Font> FREE_SANS = new Lazy<>(() -> new Font(Paths.get("/res/fonts/FreeSans.fnt")));
    public static Font freeSans() {
        return FREE_SANS.get();
    }
}
