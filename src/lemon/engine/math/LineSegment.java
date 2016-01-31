package lemon.engine.math;

/**
 * Assumed to be a Directed Line Segment
 */
public class LineSegment {
	private Vector endpoint;
	private Vector endpoint2;
	public LineSegment(Vector endpoint, Vector endpoint2){
		this.endpoint = endpoint;
		this.endpoint2 = endpoint2;
	}
	public Vector getEndpoint(){
		return endpoint;
	}
	public Vector getEndpoint2(){
		return endpoint2;
	}
}
