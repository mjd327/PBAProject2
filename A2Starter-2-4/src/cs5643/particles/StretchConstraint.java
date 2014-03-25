package cs5643.particles;

import java.util.List;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

public class StretchConstraint extends Constraint {

	/** Length constraint*/ 
	double l0; 
	
	public StretchConstraint(Particle v0, Particle v1, double restLength){
		type = 0; 
		stiffness = Constants.STRETCH_STIFFNESS; 
		cardinality = 2; 
		l0 = restLength; 
		particles.add(v0);
		particles.add(v1); 
		
	}
	public double evaluateConstraint() {
		//Get two positions of the particles   
		//Get length between them 
		Vector3d diff = new Vector3d(); 
		Particle p1 = particles.get(0);
		Particle p2 = particles.get(1); 
		diff.set(p1.x.x-p2.x.x, p1.x.y-p2.x.y, p1.x.z-p2.x.z);
		//Subtract length of edge  
		return diff.length()-l0;
	}
	
	@Override
	public Vector3d gradient(Particle p) {
		Vector3d grad = new Vector3d();
		Particle p1 = particles.get(0);
		Particle p2 = particles.get(1); 
		grad.set(p1.x.x-p2.x.x, p1.x.y-p2.x.y, p1.x.z-p2.x.z);
		grad.normalize();
		if(p.equals(p1)) 
		{
			return grad;
		}
		grad.scale(-1);
		return grad; 
	}

}
