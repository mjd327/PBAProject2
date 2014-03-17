package cs5643.particles;

import java.util.LinkedList;
import java.util.List;

/** A constraint interface as detailed in the Muller paper*/

public abstract class Constraint {

	public int cardinality;
	
	public List<Particle> particles = new LinkedList<Particle>();
	
	public double stiffness;
	
	public static ParticleSystem PS; 
	
	//Evaluates constraint equation
	public abstract double evaluateConstraint();
	
	// 0 = Equality
	// 1 = Inequality
	public int type; 	
}
