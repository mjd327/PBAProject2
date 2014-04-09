package cs5643.particles;

/**
 * Default constants. Add your own as necessary.
 * 
 * @author Doug James, January 2007
 * @author Eston Schweickart, February 2014
 */
public interface Constants
{
    /** Mass of a particle. */
    public static final double PARTICLE_MASS = 1.0;

    /** Camera rotation speed constants. */
    public static final double CAM_SIN_THETA = Math.sin(0.2);
    public static final double CAM_COS_THETA = Math.cos(0.2);

    /**Number of iterations for solver. */
    public static final int SOLVER_ITERATIONS = 5; 
    
    /** Stiffness constant for stretch constraint. */
    public static final double STRETCH_STIFFNESS = .6; 
    
    /**Stiffness constant for bend constraint. */
    public static final double BEND_STIFFNESS = .1;
    
    /** Stiffness constant for particle plane constraint. */ 
    public static final double PARTICLE_PLANE_STIFFNESS	= 1;
    
    /** */
    public static final double BALLOON_PRESSURE = 1;

    /** Cloth density. */
    public static final double RHO = 10; 
    /** Cloth thickness. */ 
    public static final double h = 0; 
    
    /** Damping constant. */
    public static final double K_DAMP = .05; 
    
    public static final double epsilon = 10e-6;

}
