package lemon.engine.loader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.BufferUtils;

import lemon.engine.entity.TriangularModel;
import lemon.engine.math.Vector2D;
import lemon.engine.math.Vector3D;

public class ObjLoader {
	private List<Vector3D> vertices;
	private List<Vector2D> textureCoords;
	private List<Vector3D> normals;
	private File file;
	private List<Float> data;
	public ObjLoader(File file){
		this.file = file;
		this.vertices = new ArrayList<Vector3D>();
		this.textureCoords = new ArrayList<Vector2D>();
		this.normals = new ArrayList<Vector3D>();
		this.data = new ArrayList<Float>();
	}
	public TriangularModel load(){
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line;
			while((line=reader.readLine())!=null){
				String[] parts = line.trim().split("\\s+");
				if(parts[0].equals("v")){
					processVertex(parts);
				}
				if(parts[0].equals("vt")){
					processTextureCoord(parts);
				}
				if(parts[0].equals("vn")){
					processNormal(parts);
				}
				if(parts[0].equals("f")){
					processFace(parts);
				}
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		FloatBuffer buffer = BufferUtils.createFloatBuffer(data.size());
		for(Float f: data){
			buffer.put(f);
		}
		buffer.flip();
		return new TriangularModel(buffer, data.size()/(3+2+3));
	}
	public void processVertex(String[] parts){
		vertices.add(new Vector3D(Float.parseFloat(parts[1]), Float.parseFloat(parts[2]), Float.parseFloat(parts[3])));
	}
	public void processTextureCoord(String[] parts){
		textureCoords.add(new Vector2D(Float.parseFloat(parts[1]), Float.parseFloat(parts[2])));
	}
	public void processNormal(String[] parts){
		normals.add(new Vector3D(Float.parseFloat(parts[1]), Float.parseFloat(parts[2]), Float.parseFloat(parts[3])));
	}
	public void processFace(String[] parts){
		addData(parts[1]);
		addData(parts[2]);
		addData(parts[3]);
		if(parts.length>4){
			addData(parts[1]);
			addData(parts[3]);
			addData(parts[4]);
		}
	}
	public void addData(String part){
		String[] parts = part.split("/");
		add(vertices.get(Integer.parseInt(parts[0])-1));
		add(textureCoords.get(Integer.parseInt(parts[1])-1));
		add(normals.get(Integer.parseInt(parts[2])-1));
	}
	public void add(Vector2D vector){
		data.add(vector.getX());
		data.add(vector.getY());
	}
	public void add(Vector3D vector){
		data.add(vector.getX());
		data.add(vector.getY());
		data.add(vector.getZ());
	}
}
