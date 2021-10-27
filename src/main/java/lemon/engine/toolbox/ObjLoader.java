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

public class ObjLoader implements Loader {
	private static final ImmutableMap<String, BiConsumer<ObjLoader, String[]>> processors = ImmutableMap.of(

			"mtllib", (objLoader, split) -> {
					MtlLoader materialLoader = new MtlLoader("/res/" + split[1], mtlLoader -> {
					System.out.println("Materials List:");
					for (var m : mtlLoader.getMaterialMap().keySet()) {
						System.out.println(mtlLoader.getMaterialMap().get(m));
					}
					objLoader.parsedMaterials = mtlLoader.getMaterialMap();
					var m = new Material();
					m.setKd(new Color(1f, 0.3f, 0.3f));
					objLoader.parsedMaterials.put("Wood", m);
					});
					materialLoader.load();
				},

			"v", (objLoader, split) -> objLoader.parsedVertices.add(Vector3D.ofParsed(split[1], split[2], split[3])),
			"vn", (objLoader, split) -> objLoader.parsedNormals.add(Vector3D.ofParsed(split[1], split[2], split[3])),
//			"vt", (objLoader, split) -> {}, // ignored
			"usemtl", (objLoader, split) -> {objLoader.currentVertexColor = split[1];},
			"f", (objLoader, split) -> {
				for (int i = 0; i < 3; i++) { // triangles
					StringTokenizer tokenizer2 = new StringTokenizer(split[i + 1], "/");
					int vertexIndex = Integer.parseInt(tokenizer2.nextToken()) - 1;
					int textureCoordIndex = Integer.parseInt(tokenizer2.nextToken()) - 1; // unused
					int normalIndex = Integer.parseInt(tokenizer2.nextToken()) - 1;
					var a = vertexIndex;
					var b = normalIndex;
					if (objLoader.cache.containsKey(a) && objLoader.cache.get(a).containsKey(b)) {
						objLoader.modelIndices.add(objLoader.cache.get(a).get(b));
					} else {
						objLoader.cache.computeIfAbsent(a, key -> new HashMap<>());
						int index = objLoader.modelVertices.size();
						objLoader.cache.get(a).put(b, index);
						objLoader.modelIndices.add(index);
						objLoader.modelVertices.add(objLoader.parsedVertices.get(a));
						System.out.println("Current Color:");
						System.out.println(objLoader.currentVertexColor);
						objLoader.modelColors.add(objLoader.parsedMaterials.getOrDefault(objLoader.currentVertexColor, objLoader.parsedMaterials.get("Wood")).Kd);
						objLoader.modelNormals.add(objLoader.parsedNormals.get(b));
					}
				}
			}
	);
	private static final BiConsumer<ObjLoader, String[]> UNKNOWN_PROCESSOR = (loader, split) -> {
		System.out.println("Unknown Key: " + Arrays.toString(split));
	};

	private int numLinesRead;
	private int totalLines;
	private final String file;
	private final BufferedReader reader;
	private final List<Vector3D> parsedVertices = new ArrayList<>();
	private final List<Vector3D> modelVertices = new ArrayList<>();
	private final List<Vector3D> parsedNormals = new ArrayList<>();
	private final List<Vector3D> modelNormals = new ArrayList<>();
	private final List<Color> modelColors = new ArrayList<>();
	private final List<Integer> modelIndices = new ArrayList<>();
	private final Map<Integer, Map<Integer, Integer>> cache = new HashMap<>();
	private final Consumer<ObjLoader> postLoadCallback;
	private Map<String, Material> parsedMaterials = new HashMap<>() {{
		var m = new Material();
		m.setKd(new Color(1f, 0.3f, 0.3f));
		put("Wood", m);
	}};
	private String currentVertexColor = "Wood";
	private final Disposable disposables = Disposable.of(
			parsedVertices::clear, modelVertices::clear,
			parsedNormals::clear, modelNormals::clear,
			modelIndices::clear, modelColors::clear, parsedMaterials::clear, cache::clear
	);

	public ObjLoader(String file, Executor executor, Consumer<ObjLoader> postLoadCallback) {
		this(file, objLoader -> executor.execute(() -> {
			postLoadCallback.accept(objLoader);
			objLoader.disposables.dispose();
		}));
	}

	private ObjLoader(String file, Consumer<ObjLoader> postLoadCallback) {
		this.file = file;
		this.reader = new BufferedReader(new InputStreamReader(
				ObjLoader.class.getResourceAsStream(file)));
		try {
			BufferedReader lineCountReader = new BufferedReader(new InputStreamReader(
					ObjLoader.class.getResourceAsStream(file)));
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

	// Let's just skip the model phase
	public IndexedDrawable toIndexedDrawable() {
		return new IndexedDrawable(
				modelIndices.stream().mapToInt(i -> i).toArray(),
				new FloatData[][] {
						modelVertices.toArray(Vector3D.EMPTY_ARRAY),
						modelColors.toArray(new Color[0]),
						modelNormals.toArray(Vector3D.EMPTY_ARRAY)
				}, GL11.GL_TRIANGLES);
	}

	@Override
	public void load() {
		ThreadManager.INSTANCE.addThread(new Thread(() -> {
			try {
				String line;
				while ((line = reader.readLine()) != null) {
					String[] split = line.split(" ");
					String key = split[0];
					processors.getOrDefault(key, UNKNOWN_PROCESSOR).accept(this, split);
					numLinesRead++;
				}
				numLinesRead = totalLines;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			postLoadCallback.accept(this);
		})).start();
	}

	@Override
	public float getProgress() {
		return ((float) numLinesRead) / ((float) totalLines);
	}

	@Override
	public String getDescription() {
		return String.format("ObjLoader[file=%s]", file);
	}
}
