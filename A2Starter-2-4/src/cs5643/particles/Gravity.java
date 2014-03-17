package cs5643.particles;

import javax.media.opengl.GL2;

public class Gravity implements Force {

	private ParticleSystem ps; 
	
	public Gravity(ParticleSystem ps) {
		this.ps = ps; 
	}

	@Override
	public void applyForce() {
		for (Particle p : getParticleSystem().P)
		{
			p.f.y -= p.m * 10.f;
		}
	}

	@Override
	public void display(GL2 gl) {
		
		
	}

	@Override
	public ParticleSystem getParticleSystem() {
		return ps;
	}

}
