package cs5643.particles;

import java.util.LinkedList;
import java.util.List;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
/** A constraint interface as detailed in the Muller paper*/

public abstract class Constraint {

	public int cardinality;
	
	public List<Particle> particles = new LinkedList<Particle>();
	
	public double stiffness;
		
	//Evaluates constraint equation
	public abstract double evaluateConstraint();
	
	//Evaluates gradient with respect to particle i 
	public abstract Vector3d gradient(Particle p); 
	
	// 0 = Equality
	// 1 = Inequality
	public int type; 	
	
}
