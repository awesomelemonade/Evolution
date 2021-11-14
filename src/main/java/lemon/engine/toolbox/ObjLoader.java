package lemon.engine.toolbox;

import com.google.common.collect.ImmutableMap;
import lemon.engine.control.Loader;
import lemon.engine.draw.IndexedDrawable;
import lemon.engine.math.FloatData;
import lemon.engine.math.MathUtil;
import lemon.engine.math.Vector3D;
import lemon.engine.thread.ThreadManager;
import org.lwjgl.opengl.GL11;

import java.io.BufferedReader;
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
import java.util.logging.Logger;

public class ObjLoader implements Loader {
	private static final Logger logger = Logger.getLogger(ObjLoader.class.getName());
	private static final ImmutableMap<String, BiConsumer<ObjLoader, String[]>> processors;
	static {
		var builder = ImmutableMap.<String, BiConsumer<ObjLoader, String[]>>builder();
		builder.put("#", (objLoader, split) -> {});
		builder.put("o", (objLoader, split) -> {});
		builder.put("g", (objLoader, split) -> {});
		builder.put("s", (objLoader, split) -> {});
		builder.put("mtllib", (objLoader, split) -> {
			try {
				MtlLoader materialLoader = new MtlLoader("/res/" + split[1], mtlLoader -> {
					objLoader.parsedMaterials = mtlLoader.materialMap();
				});
				materialLoader.load();
			} catch (Exception ex) {
				logger.warning("Failed to load " + Arrays.toString(split));
				objLoader.parsedMaterials.clear();
			}
		});
		builder.put("v", (objLoader, split) -> objLoader.parsedVertices.add(Vector3D.ofParsed(split[1], split[2], split[3])));
		builder.put("vn", (objLoader, split) -> objLoader.parsedNormals.add(Vector3D.ofParsed(split[1], split[2], split[3])));
		builder.put("vt", (objLoader, split) -> {}); // ignored
		builder.put("usemtl", (objLoader, split) -> objLoader.currentMaterial = split[1]);
		BiConsumer<ObjLoader, String> vertexAdder = (objLoader, vertexString) -> {
			StringTokenizer tokenizer = new StringTokenizer(vertexString, "/");
			int vertexIndex = Integer.parseInt(tokenizer.nextToken()) - 1;
			int textureCoordIndex = Integer.parseInt(tokenizer.nextToken()) - 1; // unused
			int normalIndex = Integer.parseInt(tokenizer.nextToken()) - 1;
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
				var material = objLoader.parsedMaterials.computeIfAbsent(objLoader.currentMaterial,
						name -> {
							logger.warning("Unknown material: " + name);
							return new Material(name, objLoader.defaultColor);
						});
				var red = MathUtil.saturate(0.1f * material.ambient().r() + 1.4f * material.diffuse().r());
				var green = MathUtil.saturate(0.1f * material.ambient().g() + 1.4f * material.diffuse().g());
				var blue = MathUtil.saturate(0.1f * material.ambient().b() + 1.4f * material.diffuse().b());
				objLoader.modelColors.add(new Color(red, green, blue));
				objLoader.modelNormals.add(objLoader.parsedNormals.get(b));
			}
		};
		builder.put("f", (objLoader, split) -> {
			var numVertices = split.length - 1;
			vertexAdder.accept(objLoader, split[1]);
			vertexAdder.accept(objLoader, split[2]);
			vertexAdder.accept(objLoader, split[3]);
			if (numVertices == 4) {
				vertexAdder.accept(objLoader, split[1]);
				vertexAdder.accept(objLoader, split[3]);
				vertexAdder.accept(objLoader, split[4]);
			} else if (numVertices > 4) {
				throw new IllegalStateException("Unsupported model");
			}
		});
		processors = builder.build();
	}
	private static final BiConsumer<ObjLoader, String[]> UNKNOWN_PROCESSOR = (loader, split) -> {
		logger.warning("Unknown Key: " + Arrays.toString(split));
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
	private final Color defaultColor;
	private Map<String, Material> parsedMaterials = new HashMap<>();
	private String currentMaterial = null;
	private final Disposables disposables = new Disposables(
			parsedVertices::clear, modelVertices::clear,
			parsedNormals::clear, modelNormals::clear,
			modelIndices::clear, modelColors::clear, parsedMaterials::clear, cache::clear
	);

	public ObjLoader(String file, Executor executor, Consumer<ObjLoader> postLoadCallback) {
		this(file, Color.WHITE, executor, postLoadCallback);
	}

	public ObjLoader(String file, Color defaultColor, Executor executor, Consumer<ObjLoader> postLoadCallback) {
		this(file, defaultColor, objLoader -> executor.execute(() -> {
			postLoadCallback.accept(objLoader);
			objLoader.disposables.dispose();
		}));
	}

	private ObjLoader(String file, Color defaultColor, Consumer<ObjLoader> postLoadCallback) {
		this.file = file;
		this.reader = new BufferedReader(new InputStreamReader(
				ObjLoader.class.getResourceAsStream(file)));
		disposables.add(Disposable.ofBufferedReader(reader));
		this.totalLines = Toolbox.getNumLines(file);
		this.postLoadCallback = postLoadCallback;
		this.defaultColor = defaultColor;
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
