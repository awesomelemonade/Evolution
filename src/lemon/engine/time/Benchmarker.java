package lemon.engine.time;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import lemon.engine.entity.LineGraph;

public class Benchmarker {
	private Map<String, LineGraph> graphs;
	public Benchmarker(){
		graphs = new LinkedHashMap<String, LineGraph>();
	}
	public void benchmark(Benchmark benchmark){
		int n = 0;
		for(LineGraph graph: graphs.values()){
			graph.add(benchmark.getData()[n++]);
		}
	}
	public void put(String name, LineGraph graph){
		graphs.put(name, graph);
	}
	public LineGraph getLineGraph(String name){
		return graphs.get(name);
	}
	public Set<String> getNames(){
		return graphs.keySet();
	}
	public int getSize(){
		return graphs.size();
	}
}
