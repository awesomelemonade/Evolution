package lemon.evolution.world;

import lemon.engine.control.Loader;
import lemon.engine.math.MutableVector3D;
import lemon.engine.thread.ThreadManager;
import lemon.engine.toolbox.Disposable;
import lemon.engine.toolbox.Disposables;
import lemon.engine.toolbox.Toolbox;
import lemon.evolution.MCMaterial;
import lemon.evolution.destructible.beta.Terrain;
import lemon.evolution.destructible.beta.TerrainChunk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

public class CsvWorldLoader implements Loader {
	private final String file;
	private int numLinesRead;
	private final int totalLines;
	private final BufferedReader reader;
	private final Terrain terrain;
	private final Disposables disposables = new Disposables();

	public CsvWorldLoader(String file, Terrain terrain) {
		this.file = file;
		this.reader = new BufferedReader(new InputStreamReader(
				CsvWorldLoader.class.getResourceAsStream(file)));
		disposables.add(Disposable.ofBufferedReader(reader));
		this.terrain = terrain;
		this.totalLines = Toolbox.getNumLines(file);
	}

	@Override
	public void load() {
		ThreadManager.INSTANCE.addThread(new Thread(() -> {
			try {
				var mapping = new HashMap<Integer, Integer>();
				var split = reader.readLine().split(",");
				numLinesRead++;
				var sizeX = Integer.parseInt(split[0]);
				var sizeY = Integer.parseInt(split[1]);
				var sizeZ = Integer.parseInt(split[2]);
				var offsetX = -sizeX / 2;
				var offsetZ = -sizeZ / 2;
				var vector = MutableVector3D.ofZero();
				for (int i = 0; i < sizeX; i++) {
					for (int j = 0; j < sizeY; j++) {
						for (int k = 0; k < sizeZ; k++) {
							vector.set(i + offsetX, j, k + offsetZ);
							var material = Integer.parseInt(reader.readLine());
							if (material != 0 && material != 1193 && material != 150 && material != 185) {
								if (!mapping.containsKey(material)) {
									System.out.println(material + " - " + MCMaterial.values()[material]);
								}
								var mapped = mapping.computeIfAbsent(material, x -> mapping.size());
								terrain.terraform(vector.asImmutable(), 1f, 1f, 100f, mapped % TerrainChunk.NUM_TEXTURES);
							}
							numLinesRead++;
						}
					}
				}
				numLinesRead = totalLines;
			} catch (IOException e) {
				e.printStackTrace();
			}
		})).start();
	}

	@Override
	public float getProgress() {
		return ((float) numLinesRead) / ((float) totalLines);
	}
}
