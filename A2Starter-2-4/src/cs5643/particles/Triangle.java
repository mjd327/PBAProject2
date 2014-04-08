package cs5643.particles;

import javax.vecmath.Vector3d;

/**
 * The face of a triangular mesh. See Mesh.java for details.
 *
 * @author Eston Schweickart, February 2014
 */
public class Triangle {

    /** The first vertex of this triangle. */
    public Vertex v0;

    /** The second vertex of this triangle. */
    public Vertex v1;

    /** The third vertex of this triangle. */
    public Vertex v2;

    /**The area of this triangle. */
    public double area; 
    
    /** Constructs a Triangle object from 3 vertices. */
    public Triangle(Vertex v0, Vertex v1, Vertex v2) {
	this.v0 = v0;
	this.v1 = v1;
	this.v2 = v2;
	this.area = calculateArea(); 
    }

    /** Computes the area of the triangle, given the coordinates of its three vertices. */
    public double calculateArea()
    {
    	Vector3d AB = new Vector3d(v1.x.x-v0.x.x,v1.x.y-v0.x.y,v1.x.z-v0.x.z);
    	Vector3d AC = new Vector3d(v2.x.x-v0.x.x,v2.x.y-v0.x.y,v2.x.z-v0.x.z);
    	double lAB = AB.length();
    	double lAC = AC.length();
    	AB.normalize();
    	AC.normalize();
    	//First we need to calculate the angle between AB and AC 
    	double theta = Math.acos(AB.dot(AC));
    	double A = lAC * lAB * Math.sin(theta);
		return A/2.0;
    	
    }
    /** Computes the unit-length normal associated with this triangle. */
    public Vector3d getNormal() {
	Vector3d e0 = new Vector3d();
	Vector3d e1 = new Vector3d();
	e0.sub(v1.x, v0.x);
	e1.sub(v2.x, v1.x);
	Vector3d normal = new Vector3d();
	normal.cross(e1, e0);
	normal.normalize();
	return normal;
    }

}