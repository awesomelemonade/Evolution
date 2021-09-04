package lemon.engine.time;

import lemon.engine.model.LineGraph;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class Benchmarker {
	private final Map<String, LineGraph> graphs = new LinkedHashMap<>();

	public void benchmark(Benchmark benchmark) {
		float[] data = benchmark.data();
		int n = 0;
		for (LineGraph graph : graphs.values()) {
			if (n >= data.length) {
				break;
			}
			graph.add(data[n++]);
		}
	}

	public void put(String name, LineGraph graph) {
		graphs.put(name, graph);
	}

	public LineGraph getLineGraph(String name) {
		return graphs.get(name);
	}

	public Set<String> getNames() {
		return graphs.keySet();
	}

	public int getSize() {
		return graphs.size();
	}
}
