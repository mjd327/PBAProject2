package cs5643.particles;

import java.util.LinkedList;
import java.util.List;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;


public class ParticlePlaneConstraint extends Constraint {

	public List<Point3d> planePoints = new LinkedList<Point3d>(); 
	
	/** If p1,p2,p3 are counter-clockwise then normal is towards the viewer.*/
	public ParticlePlaneConstraint(Particle q, Point3d p1, Point3d p2, Point3d p3){
		type = 1; 
		stiffness = Constants.PARTICLE_PLANE_STIFFNESS;
		kPrime = Math.pow(1-(1-stiffness),(1/Constants.SOLVER_ITERATIONS));
		cardinality = 4;
		particles.add(q);
		planePoints.add(p1);
		planePoints.add(p2);
		planePoints.add(p3);
	}


	@Override
	public double evaluateConstraint()
	{
		Particle q = particles.get(0);
		Point3d p1 = planePoints.get(0);
		Point3d p2 = planePoints.get(1);
		Point3d p3 = planePoints.get(2);
		
		Vector3d temp1 = new Vector3d();
		Vector3d temp2 = new Vector3d();
		
		//Compute the normal vector
		temp1.sub(p2,p1);
		temp2.sub(p3,p1);
		temp1.cross(temp1, temp2);
		temp1.normalize(); 
		
		//Dot product with the normal vector and offset by thickness. 
		temp2.sub(q.p,p1);
		return temp1.dot(temp2) - Constants.h;
	}
	
	/** Computes the gradient of the constraint. Always done with respect to q*/ 
	public Vector3d gradient(Particle p)
	{
		Vector3d temp1 = new Vector3d(0,0,0);
		Vector3d temp2 = new Vector3d(); 
		Particle q = particles.get(0);
		Point3d p1 = planePoints.get(0);
		Point3d p2 = planePoints.get(1);
		Point3d p3 = planePoints.get(2);
		if(!p.equals(q))
		{
			return temp1;
		}
		//Compute the cross product 
		temp1.sub(p2,p1);
		temp2.sub(p3,p1);
		temp1.cross(temp1, temp2);
		temp1.normalize(); 
		return temp1;
	}
	
}
