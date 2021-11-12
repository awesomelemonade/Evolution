package lemon.engine.toolbox;

import com.google.common.collect.ImmutableMap;
import lemon.engine.control.Loader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class MtlLoader implements Loader {
    private static final Logger logger = Logger.getLogger(MtlLoader.class.getName());
    private static final ImmutableMap<String, BiConsumer<MtlLoader, String[]>> processors;
    static {
        var builder = ImmutableMap.<String, BiConsumer<MtlLoader, String[]>>builder();
        builder.put("newmtl", (loader, split) -> {
            loader.flushMaterialToMap();
            loader.builder.setName(split[1]);
        });
        builder.put("Ka", (loader, split) -> loader.builder.setAmbient(new Color(split[1], split[2], split[3])));
        builder.put("Kd", (loader, split) -> loader.builder.setDiffuse(new Color(split[1], split[2], split[3])));
        builder.put("Ks", (loader, split) -> loader.builder.setSpecular(new Color(split[1], split[2], split[3])));
        builder.put("Ke", (loader, split) -> {});
        builder.put("Ns", (loader, split) -> {});
        builder.put("Ni", (loader, split) -> {});
        builder.put("d", (loader, split) -> {});
        builder.put("illum", (loader, split) -> {});
        builder.put("Tf", (loader, split) -> {});
        builder.put("#", (loader, split) -> {});
        processors = builder.build();
    }
    private static final BiConsumer<MtlLoader, String[]> UNKNOWN_PROCESSOR = (loader, split) -> {
        logger.warning("Unknown Key: " + Arrays.toString(split));
    };
    private final Map<String, Material> materialMap = new HashMap<>();
    private int numLinesRead;
    private int totalLines;
    private final String file;
    private final BufferedReader reader;
    private final Consumer<MtlLoader> postLoadCallback;
    private final MaterialBuilder builder = new MaterialBuilder();

    public MtlLoader(String file, Consumer<MtlLoader> postLoadCallback) {
        this.file = file;
        this.reader = new BufferedReader(new InputStreamReader(
                MtlLoader.class.getResourceAsStream(file)));
        try {
            BufferedReader lineCountReader = new BufferedReader(new InputStreamReader(
                    MtlLoader.class.getResourceAsStream(file)));
            int lines = 0;
            while (lineCountReader.readLine() != null) {
                lines++;
            }
            this.totalLines = lines;
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new IllegalStateException(ex);
        }
        this.postLoadCallback = postLoadCallback;
    }

    @Override
    public void load() {
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                String[] split = line.split(" ", 0);
                String key = split[0];
                processors.getOrDefault(key, UNKNOWN_PROCESSOR).accept(this, split);
                numLinesRead++;
            }
            flushMaterialToMap();
            numLinesRead = totalLines;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        postLoadCallback.accept(this);
    }

    private void flushMaterialToMap() {
        builder.build().ifPresent(material -> {
            materialMap.put(material.name(), material);
            builder.reset();
        });
    }

    public Map<String, Material> materialMap() {
        return materialMap;
    }
    @Override
    public float getProgress() {
        return ((float) numLinesRead) / ((float) totalLines);
    }
}
