package lemon.engine.toolbox;

import com.google.common.collect.ImmutableMap;
import lemon.engine.control.Loader;
import lemon.engine.draw.IndexedDrawable;
import lemon.engine.math.FloatData;
import lemon.engine.math.Vector3D;
import lemon.engine.thread.ThreadManager;
import org.lwjgl.opengl.GL11;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class MtlLoader implements Loader {

    private Map<String, Material> materialMap = new HashMap<>();
    private int numLinesRead;
    private int totalLines;
    private final String file;
    private final BufferedReader reader;
    private final Consumer<MtlLoader> postLoadCallback;

    private final Map<Integer, Map<Integer, Integer>> cache = new HashMap<>();


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

    private static final BiConsumer<MtlLoader, String[]> UNKNOWN_PROCESSOR = (loader, split) -> {
        System.out.println("Unknown Key: " + Arrays.toString(split));
    };

    @Override
    public void load() {
        //ThreadManager.INSTANCE.addThread(new Thread(() -> {
            try {
                String line;
                Material m = new Material();
                while ((line = reader.readLine()) != null) {
                    String[] split = line.split(" ", 0);
                    switch (split[0]) {
                        case "newmtl":
                            m = new Material();
//                            if (m.mtlName != null) {
//                                materialMap.put(m.mtlName, new Material(m));
//                                //System.out.println(m);
//                            }
                            m.setMtlName(split[1]);
                            materialMap.put(m.mtlName, m);

                            System.out.println(m.mtlName);
                            break;
                        case "Ns":
                            m.setNs(Float.parseFloat(split[1]));
                            break;
                        case "Ka":
                            m.setKa(new Color(Float.parseFloat(split[1]), Float.parseFloat(split[2]), Float.parseFloat(split[3])));
                            break;
                        case "Kd":
                            m.setKd(new Color(Float.parseFloat(split[1]), Float.parseFloat(split[2]), Float.parseFloat(split[3])));
                            break;
                        case "Ks":
                            m.setKs(new Color(Float.parseFloat(split[1]), Float.parseFloat(split[2]), Float.parseFloat(split[3])));
                            break;
                        case "Ke":
                            m.setKe(new Color(Float.parseFloat(split[1]), Float.parseFloat(split[2]), Float.parseFloat(split[3])));
                            break;
                        case "Ni":
                            m.setNi(Float.parseFloat(split[1]));
                            break;
                        case "d":
                            m.setD(Float.parseFloat(split[1]));
                            break;
                        case "illum":
                            m.setIllum(Integer.parseInt(split[1]));
                            break;
                        case "":
                        case "#":
                            break;
                        default:
                            UNKNOWN_PROCESSOR.accept(this, split);
                    }
                    numLinesRead++;
                }
                numLinesRead = totalLines;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            postLoadCallback.accept(this);
        //})).start();
    }

    public Map<String, Material> getMaterialMap() {
        return materialMap;
    }
    @Override
    public float getProgress() {
        return ((float) numLinesRead) / ((float) totalLines);
    }

    @Override
    public boolean isCompleted() {
        return Loader.super.isCompleted();
    }

    @Override
    public String getDescription() {
        return Loader.super.getDescription();
    }

}
