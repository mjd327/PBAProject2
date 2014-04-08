package cs5643.particles;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

public class BendConstraint extends Constraint
{
	public double phi0; //rest angle in radians
	public double d; 
	public Vector3d n1 = new Vector3d();
	public Vector3d n2 = new Vector3d();
	
	public BendConstraint(Particle p1, Particle p2, Particle p3, Particle p4, double restAngle)
	{
		phi0 = restAngle;
		type = 0;
		cardinality = 4;
		stiffness = Constants.BEND_STIFFNESS;
		kPrime = 1 - Math.pow(1-stiffness,(1.0/Constants.SOLVER_ITERATIONS));
		particles.add(p1);
		particles.add(p2);
		particles.add(p3);
		particles.add(p4);
	}

	@Override
	public double evaluateConstraint()
	{
		//Point3d p1 = new Point3d(0,0,0);
		Vector3d p2 = new Vector3d(particles.get(1).p.x -particles.get(0).p.x,
				particles.get(1).p.y -particles.get(0).p.y, particles.get(1).p.z -particles.get(0).p.z);
		Vector3d p3 = new Vector3d(particles.get(2).p.x -particles.get(0).p.x,
				particles.get(2).p.y -particles.get(0).p.y, particles.get(2).p.z -particles.get(0).p.z);
		Vector3d p4 = new Vector3d(particles.get(3).p.x -particles.get(0).p.x,
				particles.get(3).p.y -particles.get(0).p.y, particles.get(3).p.z -particles.get(0).p.z);
		
		n1.cross(p2,p3);
		n2.cross(p2,p4);
		n1.normalize();
		n2.normalize();
		d = n1.dot(n2);

		//System.out.println("Evaluated Constraint: " + (Math.acos(n1.dot(n2)) - phi0));
		if(Double.isNaN((Math.acos(n1.dot(n2)) - phi0)))
		{
			System.out.println("Constraint evaluatd to NaN");
		}
		return Math.acos(n1.dot(n2)) - phi0;

	}

	@Override
	public Vector3d gradient(Particle p)
	{
		//Point3d p1 = new Point3d(0,0,0);
		Vector3d p2 = new Vector3d(particles.get(1).p.x -particles.get(0).p.x,
				particles.get(1).p.y -particles.get(0).p.y, particles.get(1).p.z -particles.get(0).p.z);
		Vector3d p3 = new Vector3d(particles.get(2).p.x -particles.get(0).p.x,
				particles.get(2).p.y -particles.get(0).p.y, particles.get(2).p.z -particles.get(0).p.z);
		Vector3d p4 = new Vector3d(particles.get(3).p.x -particles.get(0).p.x,
				particles.get(3).p.y -particles.get(0).p.y, particles.get(3).p.z -particles.get(0).p.z);
		
		Vector3d temp1 = new Vector3d();
		Vector3d temp2 = new Vector3d(); 
		
		Vector3d q1 = new Vector3d();
		Vector3d q2 = new Vector3d();
		Vector3d q3 = new Vector3d();
		Vector3d q4 = new Vector3d();
		
		if(p.equals(particles.get(0)) || p.equals(particles.get(1)))
		{
			//if wrt p2
			temp1.cross(p3, n2);
			temp2.cross(n1, p3);
			temp2.scale(d);
			temp1.add(temp2);
			temp2.cross(p2, p3);
			temp1.scale(-1.0/temp2.length());
			q2.set(temp1.x, temp1.y, temp1.z);
			
			temp1.cross(p4, n1);
			temp2.cross(n2, p4);
			temp2.scale(d);
			temp1.add(temp2);
			temp2.cross(p2, p4);
			temp1.scale(-1.0/temp2.length());
			q2.add(temp1);
			
			q2.scale(-1.0/Math.sqrt(1-Math.pow(d,2)));			
		}
		if(p.equals(particles.get(0)) || p.equals(particles.get(2)))
		{
			//if wrt p3
			temp1.cross(p2, n2);
			temp2.cross(n1, p2);
			temp2.scale(d);
			temp1.add(temp2);
			temp2.cross(p2, p3);
			temp1.scale(1.0/temp2.length());
			q3.set(temp1.x, temp1.y, temp1.z);
			
			q3.scale(-1.0/Math.sqrt(1-Math.pow(d,2)));
		}
		if(p.equals(particles.get(0)) || p.equals(particles.get(3)))
		{
			//if wrt p4
			temp1.cross(p2, n1);
			temp2.cross(n2, p2);
			temp2.scale(d);
			temp1.add(temp2);
			temp2.cross(p2, p4);
			temp1.scale(1.0/temp2.length());
			q4.set(temp1.x, temp1.y, temp1.z);
			
			q4.scale(-1.0/Math.sqrt(1-Math.pow(d,2)));
			
		}
		if(p.equals(particles.get(0)))
		{
			//if wrt p1
			q1.set(-1*q2.x, -1*q2.y, -1*q2.z);
			q1.sub(q3);
			q1.sub(q4);
		}


		if(p.equals(particles.get(0))) return q1;
		else if(p.equals(particles.get(1))) return q2;
		else if(p.equals(particles.get(2))) return q3;
		else if(p.equals(particles.get(3))) return q4;
		else return null; //Should never happen!
		
	}
	
	

	/** Helper function for constraint initialization */
	public static double angleBetween(Particle p0, Particle p1, Particle p2, Particle p3)
	{
		Vector3d v2 = new Vector3d(p1.x.x - p0.x.x, p1.x.y - p0.x.y, p1.x.z - p0.x.z);
		Vector3d v3 = new Vector3d(p2.x.x - p0.x.x, p2.x.y - p0.x.y, p2.x.z - p0.x.z);
		Vector3d v4 = new Vector3d(p3.x.x - p0.x.x, p3.x.y - p0.x.y, p3.x.z - p0.x.z);
		
		Vector3d norm1 = new Vector3d();
		Vector3d norm2 = new Vector3d();
		norm1.cross(v2,v3);
		norm2.cross(v2,v4);
		norm1.normalize();
		norm2.normalize();
		if(Double.isNaN(Math.acos(norm1.dot(norm2))))
		{
			System.out.println("initial angle is nan");
		}
		return Math.acos(norm1.dot(norm2));
	}
	
	
	
}
