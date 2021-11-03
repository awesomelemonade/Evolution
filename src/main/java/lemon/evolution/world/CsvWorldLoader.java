package lemon.evolution.world;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import lemon.engine.control.Loader;
import lemon.engine.math.MutableVector3D;
import lemon.engine.thread.ThreadManager;
import lemon.engine.toolbox.Toolbox;
import lemon.evolution.MCMaterial;
import lemon.evolution.destructible.beta.Terrain;
import lemon.evolution.destructible.beta.TerrainChunk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public class CsvWorldLoader implements Loader {
	private final String file;
	private int numLinesRead;
	private final int totalLines;
	private final Terrain terrain;
	private final BiMap<MCMaterial, Integer> blockMapping = HashBiMap.create();
	private final Consumer<CsvWorldLoader> postLoadCallback;

	public CsvWorldLoader(String file, Terrain terrain, Executor executor, Consumer<CsvWorldLoader> postLoadCallback) {
		this(file, terrain, csvLoader -> executor.execute(() -> {
			postLoadCallback.accept(csvLoader);
		}));
	}

	private CsvWorldLoader(String file, Terrain terrain, Consumer<CsvWorldLoader> postLoadCallback) {
		this.file = file;
		this.terrain = terrain;
		this.totalLines = Toolbox.getNumLines(file);
		this.postLoadCallback = postLoadCallback;
	}

	@Override
	public void load() {
		ThreadManager.INSTANCE.addThread(new Thread(() -> {
			try (var reader = new BufferedReader(new InputStreamReader(CsvWorldLoader.class.getResourceAsStream(file)))) {
				var split = reader.readLine().split(",");
				numLinesRead++;
				var sizeX = Integer.parseInt(split[0]) + 1;
				var sizeY = Integer.parseInt(split[1]) + 1;
				var sizeZ = Integer.parseInt(split[2]) + 1;
				var offsetX = -sizeX / 2;
				var offsetZ = -sizeZ / 2;
				var vector = MutableVector3D.ofZero();
				for (int i = 0; i < sizeX; i++) {
					for (int j = 0; j < sizeY; j++) {
						for (int k = 0; k < sizeZ; k++) {
							vector.set(i + offsetX, j, k + offsetZ);
							var materialOrdinal = Integer.parseInt(reader.readLine());
							var material = MCMaterial.values()[materialOrdinal];
							if (!material.isEmpty()) {
								var mapped = blockMapping.computeIfAbsent(material, x -> blockMapping.size());
								terrain.terraform(vector.asImmutable(), 1.4f, 1f, 10f, mapped % TerrainChunk.NUM_TEXTURES);
							}
							numLinesRead++;
						}
					}
				}
				numLinesRead = totalLines;
				postLoadCallback.accept(this);
			} catch (IOException e) {
				e.printStackTrace();
			}
		})).start();
	}

	@Override
	public float getProgress() {
		return ((float) numLinesRead) / ((float) totalLines);
	}

	public BiMap<MCMaterial, Integer> blockMapping() {
		return blockMapping;
	}
}
