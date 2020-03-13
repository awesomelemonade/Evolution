package lemon.engine.toolbox;

import lemon.engine.control.Loader;
import lemon.engine.draw.IndexedDrawable;
import lemon.engine.math.Percentage;
import lemon.engine.math.Vector;
import lemon.engine.math.Vector3D;
import lemon.engine.thread.ThreadManager;
import org.lwjgl.opengl.GL11;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.*;
import java.util.function.BiConsumer;

public class ObjLoader implements Loader {
	private static Map<String, BiConsumer<ObjLoader, StringTokenizer>> processors;
	private static final BiConsumer<ObjLoader, StringTokenizer> UNKNOWN_PROCESSOR = (loader, tokenizer) -> {
		System.out.println("Unknown Key: " + tokenizer.nextToken(""));
	};
	static {
		processors = new HashMap<String, BiConsumer<ObjLoader, StringTokenizer>>();
		processors.put("v", (objLoader, tokenizer) -> {
			Vector3D vertex = new Vector3D(Float.parseFloat(tokenizer.nextToken()),
					Float.parseFloat(tokenizer.nextToken()), Float.parseFloat(tokenizer.nextToken()));
			objLoader.vertices.add(vertex);
		});
		processors.put("vn", (objLoader, tokenizer) -> {
			Vector3D normal = new Vector3D(Float.parseFloat(tokenizer.nextToken()),
					Float.parseFloat(tokenizer.nextToken()), Float.parseFloat(tokenizer.nextToken()));
			objLoader.normals.add(normal);
		});
		processors.put("f", (objLoader, tokenizer) -> {
			for (int i = 0; i < 3; i++) { // triangles
				StringTokenizer tokenizer2 = new StringTokenizer(tokenizer.nextToken(), "/");
				int vertexIndex = Integer.parseInt(tokenizer2.nextToken());
				int textureCoordIndex = Integer.parseInt(tokenizer2.nextToken()); // unused
				int normalIndex = Integer.parseInt(tokenizer2.nextToken());
				if (vertexIndex == normalIndex) {
					objLoader.indices.add(vertexIndex - 1); // offset by 1
				} else {
					if (objLoader.vertices.size() == objLoader.normals.size()) {
						int a = vertexIndex - 1;
						int b = normalIndex - 1;

						if (objLoader.cache.containsKey(a) && objLoader.cache.get(a).containsKey(b)) {
							objLoader.indices.add(objLoader.cache.get(a).get(b));
						} else {
							objLoader.cache.computeIfAbsent(a, key -> new HashMap<Integer, Integer>());
							int index = objLoader.vertices.size();
							objLoader.cache.get(a).put(b, index);
							objLoader.indices.add(index);
							objLoader.vertices.add(objLoader.vertices.get(a));
							objLoader.normals.add(objLoader.normals.get(b));
						}
					} else {
						throw new IllegalStateException(String.format("Unable to add face: %s", tokenizer.toString()));
					}
				}
			}
		});
	}
	private Percentage percentage;
	BufferedReader reader;
	private List<Vector3D> vertices;
	private List<Vector3D> normals;
	private List<Integer> indices;
	private Map<Integer, Map<Integer, Integer>> cache;

	public ObjLoader(String file) {
		this.reader = new BufferedReader(new InputStreamReader(
				ObjLoader.class.getResourceAsStream(file)));
		this.vertices = new ArrayList<Vector3D>();
		this.normals = new ArrayList<Vector3D>();
		this.indices = new ArrayList<Integer>();
		this.cache = new HashMap<Integer, Map<Integer, Integer>>();
		try {
			BufferedReader lineCountReader = new BufferedReader(new InputStreamReader(
					ObjLoader.class.getResourceAsStream(file)));
			int lines = 0;
			while (lineCountReader.readLine() != null) {
				lines++;
			}
			this.percentage = new Percentage(lines);
		} catch (IOException ex) {
			ex.printStackTrace();
			throw new IllegalStateException(ex);
		}
	}
	// Let's just skip the model phase
	public IndexedDrawable toIndexedDrawable() {
		return new IndexedDrawable(new Vector[][] {
				vertices.toArray(Vector3D.EMPTY_ARRAY),
				Color.randomOpaque(vertices.size()),
				normals.toArray(Vector3D.EMPTY_ARRAY)
		}, indices.stream().mapToInt(i -> i).toArray(), GL11.GL_TRIANGLES);
	}
	@Override
	public void load() {
		ThreadManager.INSTANCE.addThread(new Thread(() -> {
			try {
				String line;
				while ((line = reader.readLine()) != null) {
					StringTokenizer tokenizer = new StringTokenizer(line);
					String key = tokenizer.nextToken();
					processors.getOrDefault(key, UNKNOWN_PROCESSOR).accept(this, tokenizer);
					percentage.setPart(percentage.getPart() + 1);
				}
				percentage.setPart(percentage.getWhole());
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		})).start();
	}
	@Override
	public Percentage getPercentage() {
		return percentage;
	}
}
