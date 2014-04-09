package cs5643.particles;

import javax.vecmath.Vector3d;

public class BalloonConstraint extends Constraint
{
	public double kpressure;
	public double v0;
	public Mesh mesh;
	
	
	public BalloonConstraint(int cardinality, Mesh mesh)
	{
		this.mesh = mesh;
		this.cardinality = cardinality;
		type = 0;
		stiffness = 1;
		kPrime = stiffness;
		kpressure = Constants.BALLOON_PRESSURE;
		
		Vector3d p1 = new Vector3d();
		Vector3d p2 = new Vector3d();
		Vector3d p3 = new Vector3d();
		
		for(Vertex v: mesh.vertices) particles.add(v);
		
		
		for(Triangle t: mesh.triangles)
		{
			p1.set(t.v0.x.x, t.v0.x.y, t.v0.x.z);
			p2.set(t.v1.x.x, t.v1.x.y, t.v1.x.z);
			p3.set(t.v2.x.x, t.v2.x.y, t.v2.x.z);
			p1.cross(p1,p2);
			v0 += p1.dot(p3);
		}
		System.out.println("initial volume is : " + v0);
	}
	
	
	@Override
	public double evaluateConstraint()
	{
		Vector3d p1 = new Vector3d();
		Vector3d p2 = new Vector3d();
		Vector3d p3 = new Vector3d();
		double sum = 0;
		
		for(Triangle t: mesh.triangles)
		{
			p1.set(t.v0.p.x, t.v0.p.y, t.v0.p.z);
			p2.set(t.v1.p.x, t.v1.p.y, t.v1.p.z);
			p3.set(t.v2.p.x, t.v2.p.y, t.v2.p.z);
			p1.cross(p1,p2);
			sum += p1.dot(p3);
		}
		
		System.out.println("Evaluated Constraint: " + (sum - (kpressure*v0)));
		return sum - (kpressure*v0);
	}

	@Override
	public Vector3d gradient(Particle p)
	{
		Vector3d grad = new Vector3d();
		Vector3d temp = new Vector3d();
		
		Vector3d p1 = new Vector3d();
		Vector3d p2 = new Vector3d();
		Vector3d p3 = new Vector3d();
		
		for(Triangle t: ((Vertex)p).triangles)
		{
			p1.set(t.v0.p.x, t.v0.p.y, t.v0.p.z);
			p2.set(t.v1.p.x, t.v1.p.y, t.v1.p.z);
			p3.set(t.v2.p.x, t.v2.p.y, t.v2.p.z);
			
			if((p.p.x == p1.x) && (p.p.y == p1.y) && (p.p.z == p1.z))
			{
				temp.cross(p2,p3);
			}
			if((p.p.x == p2.x) && (p.p.y == p2.y) && (p.p.z == p2.z))
			{
				temp.cross(p3,p1);
			}
			if((p.p.x == p3.x) && (p.p.y == p3.y) && (p.p.z == p3.z))
			{
				temp.cross(p1,p2);
			}
			grad.add(grad, temp);
		}
		return grad;
	}

}
