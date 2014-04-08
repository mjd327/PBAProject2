package cs5643.particles;

import java.util.*;

import javax.vecmath.*;
import javax.media.opengl.*;
import com.jogamp.opengl.util.glsl.*;



/**
 * Maintains dynamic lists of Particle and Force objects, and provides
 * access to their state for numerical integration of dynamics.
 * 
 * @author Doug James, January 2007
 * @author Eston Schweickart, February 2014
 */
public class ParticleSystem //implements Serializable
{
    /** Current simulation time. */
    public double time = 0;

    /** List of Particle objects. */
    public ArrayList<Particle>   P = new ArrayList<Particle>();

    /** List of Mesh objects. */
    public ArrayList<Mesh> M = new ArrayList<Mesh>(); 
    
    /** List of Force objects. */
    public ArrayList<Force>      F = new ArrayList<Force>();
    
    /** List of permanent Constraint objects. */
    public ArrayList<Constraint> CPerm = new ArrayList<Constraint>(); 
    
    /** List of temporary Constraint objects. */ 
    public ArrayList<Constraint> CTemp = new ArrayList<Constraint>(); 
    /** 
     * true iff prog has been initialized. This cannot be done in the
     * constructor because it requires a GL2 reference.
     */
    private boolean init = false;

    /** Filename of vertex shader source. */
    public static final String[] VERT_SOURCE = {"vert.glsl"};

    /** Filename of fragment shader source. */
    public static final String[] FRAG_SOURCE = {"frag.glsl"};

    /** Used for various testing purposes. */
    public static boolean test = true; 
    
    /** The shader program used by the particles. */
    ShaderProgram prog;

    /** A temporary vector for use in vector operations*/
    Vector3d temp = new Vector3d(); 

    /** Basic constructor. */
    public ParticleSystem() {}
    
    public Particle selectedParticle = null; //Replaced when ever mouse is pressed. 

    /** 
     * Set up the GLSL program. This requires that the current directory (i.e. the package in which
     * this class resides) has a vertex and fragment shader.
     */
    public synchronized void init(GL2 gl) {
	if (init) return;
	
	prog = new ShaderProgram();
	ShaderCode vert_code = ShaderCode.create(gl, GL2ES2.GL_VERTEX_SHADER, 1, this.getClass(), VERT_SOURCE, false);
	ShaderCode frag_code = ShaderCode.create(gl, GL2ES2.GL_FRAGMENT_SHADER, 1, this.getClass(), FRAG_SOURCE, false);
	if (!prog.add(gl, vert_code, System.err) || !prog.add(gl, frag_code, System.err)) {
	    System.err.println("WARNING: shader did not compile");
	    prog.init(gl); // Initialize empty program
	} else {
	    prog.link(gl, System.err);
	}

	init = true;
    }

    /** Adds a force object (until removed) */
    public synchronized void addForce(Force f) {
	F.add(f);
    }

    /** Useful for removing temporary forces, such as user-interaction
     * spring forces. */
    public synchronized void removeForce(Force f) {
	F.remove(f);
    }

    /** Creates particle and adds it to the particle system. 
     * @param p0 Undeformed/material position. 
     * @return Reference to new Particle.
     */
    public synchronized Particle createParticle(Point3d p0) 
    {
	Particle newP = new Particle(p0);
	P.add(newP);
	return newP;
    }

    /** 
     * Helper-function that computes nearest particle to the specified
     * (deformed) position.
     * @return Nearest particle, or null if no particles. 
     */
    public synchronized Particle getNearestParticle(Point3d x)
    {
	Particle minP      = null;
	double   minDistSq = Double.MAX_VALUE;
	for(Particle particle : P) {
	    double distSq = x.distanceSquared(particle.x);
	    if(distSq < minDistSq) {
		minDistSq = distSq;
		minP = particle;
	    }
	}
	return minP;
    }

    /** Moves all particles to undeformed/materials positions, and
     * sets all velocities to zero. Synchronized to avoid problems
     * with simultaneous calls to advanceTime(). */
    public synchronized void reset()
    {
	for(Particle p : P)  {
	    p.x.set(p.x0);
	    p.p.set(p.x0);
	    p.v.set(0,0,0);
	    p.f.set(0,0,0);
	    p.m = p.m0; 
	    p.w = 1.0/p.m;
	    p.pinHighlight = false;
	    p.dragHighlight = false; 
	    p.pinned = false;
	}
	time = 0;
    }

    /**
     * Incomplete/Debugging implementation of Forward-Euler
     * step. WARNING: Contains buggy debugging forces.
     */
    public synchronized void advanceTime(double dt)
    {
	for(Particle p : P)  p.f.set(0,0,0);

    for(Force force : F) {
      force.applyForce();
	}
	

	/// TIME-STEP: (Forward Euler for now):
	for(Particle p : P) 
	{
		if(p.pinned == false)
		{
	    p.v.scaleAdd((dt*p.w), p.f, p.v); //p.v += dt * p.f;
		}
	}
	dampVelocities();
	for(Particle p : P)
	{
		if (p.pinned == false)
		{
	    p.p.scaleAdd(dt, p.v, p.x); //p.p = p.x + dt * p.v;
		}
	}
	for(Particle p : P)
	{
		//generateCollisionConstraints(xi->pi (?) );
		wallCollisionDetector(p);
		
	}
	for(int i = 0; i < Constants.SOLVER_ITERATIONS; i++)	
	{
		//projectConstraints(C1,...,CM+MColl,p1,...,pn);
		for(Constraint c : CPerm)
		{
			projectConstraint(c);
		}
		for(Constraint c : CTemp)
		{
			projectConstraint(c);
		}
	}
	for(Particle p : P)
	{
		if (p.pinned == false)
		{
		temp.sub(p.p,p.x);
		temp.scale(1.0/dt);
		p.v.set(temp); 
		p.x.set(p.p);
		}
	}
	//velocityUpdate(v1,...,Vn);
	CTemp.clear();
	time += dt;
    }

    /** Projects a given constraint and move particle accordingly. */
    public synchronized void projectConstraint(Constraint c)
    {
    	if((c.type == 0 && Math.abs(c.evaluateConstraint()) > Constants.epsilon) || (c.type == 1 && c.evaluateConstraint() <= -Constants.epsilon)) 
    	{
    		//We need the scaling factor for our delta P calculation.
    		double s = calculateScalingFactor(c);
    		Vector3d gradient = new Vector3d(); 
    		//INEFFICIENT, as we are calculating all of these twice. 
    		for(Particle p : c.particles)
    		{
    			gradient = c.gradient(p); 
    			gradient.scale(s * p.w * c.kPrime);
    			p.p.add(gradient);
    		}
    	}
    	
    }
    
    /** Calculates scaling factor given a constraint*/ 
    public synchronized double calculateScalingFactor(Constraint c)
    {
    	//First we need the sum of all the gradients.
    	double numerator = c.evaluateConstraint();
    	double denominator = 0; 
    	Vector3d gradient = new Vector3d(); 
    	//Add up the inverse mass times gradient magnitude squared
    	for(Particle p : c.particles)
    	{
    		gradient.set(c.gradient(p));
    		denominator += p.w * gradient.lengthSquared();
    	}
    	return -numerator/denominator; 
    }
    
    /** 
     * Checks for collision with the walls. If collision detected, then 
     * a constraint is formed.
     */
    public synchronized void wallCollisionDetector(Particle p)
	{
		//Check each wall of the cube manually, which is simplified because the surface is a unit cube. 
    	Point3d p1; 
    	Point3d p2; 
    	Point3d p3; 
		if(p.p.x < 0)
		{
			p1 = new Point3d(0,0,0);
			p2 = new Point3d(0,1,0);
			p3 = new Point3d(0,0,1);
			CTemp.add(new ParticlePlaneConstraint(p,p1,p2,p3));
		}
		else if (p.p.x > 1)
		{
			p1 = new Point3d(1,0,0);
			p2 = new Point3d(1,1,0);
			p3 = new Point3d(1,0,1);
			CTemp.add(new ParticlePlaneConstraint(p,p1,p3,p2));
		}
		if(p.p.y < 0)
		{
			p1 = new Point3d(0,0,0);
			p2 = new Point3d(1,0,0);
			p3 = new Point3d(0,0,1);
			CTemp.add(new ParticlePlaneConstraint(p,p1,p3,p2));
		}
		else if (p.p.y > 1)
		{
			p1 = new Point3d(0,1,0);
			p2 = new Point3d(1,1,0);
			p3 = new Point3d(0,1,1);
			CTemp.add(new ParticlePlaneConstraint(p,p1,p2,p3));
		}
		if(p.p.z < 0)
		{
			p1 = new Point3d(0,0,0);
			p2 = new Point3d(1,0,0);
			p3 = new Point3d(0,1,0);
			CTemp.add(new ParticlePlaneConstraint(p,p1,p2,p3));
		}
		else if (p.p.z > 1)
		{
			p1 = new Point3d(0,0,1);
			p2 = new Point3d(1,0,1);
			p3 = new Point3d(0,1,1);
			CTemp.add(new ParticlePlaneConstraint(p,p1,p3,p2));
		}
	}
	
    /** Create the initial edge constraints for the triangle.*/ 
    public synchronized void initialConstraints()
    {
    	Vertex p1,p2,p3,p4;
    	
    	for(Mesh m : M)
    	{
    		for(Edge e : m.edges)
    		{	
    			//Add all stretch constraints. 
    			CPerm.add(new StretchConstraint(e.v0,e.v1,e.restLength));
    			
    			//Add bending constraints for non-boundary edges
    			if(e.t0 == null || e.t1 == null)
    				continue;
    			else
    			{
    				p1 = e.v0;
    				p2 = e.v1;

    				if(!(e.t0.v0.equals(e.v0) || e.t0.v0.equals(e.v1))) p3 = e.t0.v0;
    				else if(!(e.t0.v1.equals(e.v0) || e.t0.v1.equals(e.v1))) p3 = e.t0.v1;
    				else if (!(e.t0.v2.equals(e.v0) || e.t0.v2.equals(e.v1))) p3 = e.t0.v2;
    				else
    				{
    					p3 = null;
    					System.out.println("t0; verticies not ordered correctly when calculating bending constraints");
    				}
    				

    				if(!(e.t1.v0.equals(e.v1) || e.t1.v0.equals(e.v0))) p4 = e.t1.v0;
    				else if (!(e.t1.v1.equals(e.v1) || e.t1.v1.equals(e.v0))) p4 = e.t1.v1;
    				else if (!(e.t1.v2.equals(e.v1) || e.t1.v2.equals(e.v0))) p4 = e.t1.v2;
    				else
    				{
    					p4 = null;
    					System.out.println("t1; verticies not ordered correctly when calculating bending constraints");
    				}
    				

    				//CPerm.add(new BendConstraint(p1, p2, p3, p4, BendConstraint.angleBetween(p1,p2,p3,p4)));
    				CPerm.add(new BendConstraint(p1, p2, p3, p4, Math.PI));
    			}
    		}
    		
    		
    	}
    }
    
    /** Calculates center of masses and dampens all the velocities of the particle system as detailed
     * in section 3.5 of the Meuller paper
     */
    public synchronized void dampVelocities()
    {
    	//Calculate position and velocity of center of mass
    	Point3d xCM = new Point3d();  
    	Point3d vCM = new Point3d(); 
    	double sigmaM = 0;  //Can be pre-calculated
    	Point3d tempPoint = new Point3d();
    	Vector3d tempVector = new Vector3d(); 
    	for(Particle p : P)
    	{
    		tempPoint.set(p.x);
    		tempVector.set(p.v);
    		tempPoint.scale(p.m);
    		tempVector.scale(p.m); 
    		xCM.add(tempPoint);
    		vCM.add(tempVector);
    		sigmaM += p.m;
    	}
    	xCM.scale(1/sigmaM);
    	vCM.scale(1/sigmaM);
    	
    	//Find distance of each particle from the center of mass. 
    	Vector3d R = new Vector3d(); 
    	//Calculate L and I
    	Vector3d L = new Vector3d(); 
    	Matrix3d I = new Matrix3d(); 
    	Matrix3d ri = new Matrix3d();
    	for(Particle p : P)
    	{
    		//L
    		R.sub(p.x,xCM);
    		tempVector.set(p.v);
    		tempVector.scale(p.m);
    		tempVector.cross(R, tempVector);
    		L.add(tempVector);
    		
    		//I
    		ri.set(new double[]{0,-R.z,R.y,R.z,0,-R.x,-R.y,R.x,0});
    		ri.mulTransposeRight(ri,ri);
    		ri.mul(p.m,ri);
    		I.add(ri);
    	}
    	
    	//Finally calculate angular velocity and then update velocities. 
    	Vector3d w = new Vector3d(); 
    	I.invert();
    	I.transform(L);
    	w.set(L);
    	Vector3d dV = new Vector3d();
    	for(Particle p : P)
    	{
    		R.sub(p.x,xCM);
    		tempVector.set(w);
    		tempVector.cross(tempVector,R);
    		dV.set(vCM);
    		dV.add(tempVector); 
    		dV.sub(p.v);
    		p.v.scaleAdd(Constants.K_DAMP, dV, p.v);
    		
    	}
    	
    }
    /** Set the mass and inverse mass of the particles based on the density, rho, of the cloth.*/
    public synchronized void initializeParticleMasses()
    {
    	double area = 0; 
    	for(Particle p : P)
    	{
    		area = 0; 
    		for(Triangle t :((Vertex)p).triangles)
    		{
    			area += t.area*(1/3.0);
    		}
    		p.m = Constants.RHO*area;
    		p.m0 = p.m; 
    		p.w = 1.0/p.m;
    	}
    }

    /** Given an X and Y in screen space, project a ray into world space 
     * and turn select closest particle.*/
	public void selectParticle(Point3d nearPoint, Point3d farPoint)
	{
		Point3d origin = new Point3d(nearPoint); //just use directly?
		Vector3d direction = new Vector3d();
		direction.sub(farPoint,nearPoint);
		
		Particle closestP = null; 
		double length = Double.MAX_VALUE; //How far point is from ray. 
		Vector3d vP = new Vector3d(); 
		Vector3d crossProduct = new Vector3d(); 
		for(Particle p : P)
		{
			if(direction.dot(vP) >= 0)
			{
				vP.sub(p.x,origin); //p.p?? Depends on before or after. 
				crossProduct.cross(direction, vP);
				crossProduct.scale(direction.length());
				if (crossProduct.length() < length)
				{
					length = crossProduct.length();
					closestP = p; 
				}
			}
		}
		if (closestP == null)
		{
			System.out.println("No particle in range");
		}
		else 
		{
			selectedParticle = closestP; 
			closestP.selectParticle(); 
		}
	}
	
	public void deselectParticle(boolean flipPin)
	{
		selectedParticle.deselectParticle(flipPin);
	}

	/**Drags the selected particle to the given position. */
	public void dragParticle(Point3d nearPoint, Point3d farPoint)
	{
		Point3d origin = new Point3d(nearPoint); //just use directly?
		Vector3d direction = new Vector3d();
		direction.sub(farPoint,nearPoint);
		Vector3d vP = new Vector3d(); 
		vP.sub(selectedParticle.x,origin); //p.p?? Depends on before or after. 
		
		double pDotR = direction.dot(vP);
		direction.scale(1/direction.lengthSquared());
		direction.scale(pDotR);
		direction.add(origin);
		selectedParticle.x.set(direction); 
		selectedParticle.p.set(direction);
	}
	
    /**
     * Displays Particle and Force objects.
     */
    public synchronized void display(GL2 gl) 
    {
    	for(Force force : F)
    	{
    		force.display(gl);
    	}

    	if(!init) init(gl);

    	prog.useProgram(gl, true);
	
    	for(Particle particle : P)
    	{
    		particle.display(gl);
    	}

    	prog.useProgram(gl, false);
    }
}
