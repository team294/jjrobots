
public class __SawdaiMark3_ extends JJRobot {
	// __SawdaiMark3_ robot v. 1.0 for JJRobots, by Don Sawdai
	// This robot drives in a circle around the arena and shoots at
	// whatever it can scan.  It uses linear predictive targeting for shooting.
	
	private int driveAngle = 0;
	private int driveSpeed = 49;	// speed<50, so we can turn without slowing down
	private int scanAngle = 0;
	private int scanRes = 1;

	// Drive parameters
	private static final int fieldSize = 1000;
	private static final int fieldSizeHalf = fieldSize/2;
	private static final int driveRadiusMin = 300;		// Smallest circle to drive around
	private static final int driveRadiusMax = 400;		// Largest circle to drive around
	
	// Shooting parameters
	private static final int cannonRange = 700;			// Max cannon range, in m
	private static final int missleSpeed = 300;			// Missle speed, in m/s
	private static final double velocityAcquireDelay = 1.02;		// Delay to acquire target speed, in s 
	
	// Shooting variables
	private JJVector v1 = new JJVector();				// Temp var 1 
	private JJVector vMeTemp = new JJVector();			// Temp var = my location
	private JJVector vTargetTemp = new JJVector();		// Temp var = target location 
	private JJVector vTargetVelocity = new JJVector();	// Temp var = target velocity 
	private JJVector vTargetSaved = new JJVector();		// Saved copy of target
	private boolean bTargetSavedValid = false;			// Did we find and save a target?
	
	@Override
	void main() {
		// Set initial drive angle and speed
		driveAngle = rand(360);
		drive(driveAngle, driveSpeed);

		// Track main loop iterations per second
//		double t;
//		long i = 0;
//		t = time();

		// Main loop
		while (true) {
			// Don't put any long waits in the loop, so it keeps running repeatedly
			driveRobot();
			scanAndShoot();
			
//			i++;
//			if (time()-t>1) {
//				System.out.println("LPS = " + (i/(time()-t)));
//				t=time();
//				i=0;
//			}
		}
		
	}

	/**
	 * Control the robot driving.
	 * There are no "waits" in this method, so it won't delay the main robot loop.
	 */
	void driveRobot() {
		// Drive in a circle
		
		int x = loc_x() - fieldSizeHalf;
		int y = loc_y() - fieldSizeHalf;
		
		// Get location in polar coords (relative to arena center)
		double r = d_sqrt(x*x + y*y);
		double ang = rad2deg(atan2(y, x));

		// Drive in a circle of radius driveRadiusMin to driveRadiusMax
		if (r>driveRadiusMax) ang+=180; 		// if too far away, then go towards center of field
			else if (r>driveRadiusMin) ang+=100;	// if in the "circle zone", then orbit
												// if too close, then go away from center of field

		// Wrap drive angle within 0-360 degrees
		if (ang>360) ang-=360;
		if (ang<0) ang+=360;

		// Slow down and turn if drive angle is too far off
		if (d_abs(ang-(double)driveAngle)>10) {
			if (speed()<50)	driveAngle = (int)ang;
			drive(driveAngle, 49);
		} else {
			drive(driveAngle, 100);
		}

	}
	
	/**
	 * Look for other robots and shoot at them.
	 * There are no "waits" in this method, so it won't delay the main robot loop.
	 */
	void scanAndShoot() {
		int d;
		double tScan;
		
		d = scan(scanAngle, scanRes);
		tScan = time();			// Save time of scan
		
		if (d==0) {
			// No enemy found, so rotate scanner angle for next time through loop
			scanAngle += scanRes;
		} else {
			// Get my location
			vMeTemp.set(d_loc_x(), d_loc_y());

			// Get enemy location
			v1.set(d * Math.cos(Math.PI * scanAngle / 180.0), d * Math.sin(Math.PI * scanAngle / 180.0));
			v1.plus(vMeTemp, vTargetTemp);
			vTargetTemp.set_t(tScan);
//			System.out.println(vTargetTemp);
			
//			if (bTargetSavedValid) {
//				// We have a saved target.  Clear it if it is not recent
//				bTargetSavedValid = ((tScan - vTargetSaved.t()) <= 10.0*velocityAcquireDelay);
//			}
			
			if (bTargetSavedValid) {
				// We have a valid saved target.
				if ( (tScan - vTargetSaved.t()) >= velocityAcquireDelay ) {
					// We have waited long enough to calculate the target's velocity.
					// Target missile to intercept the target and shoot!

					// Get target velocity
					vTargetTemp.minus(vTargetSaved, v1).velocity(vTargetVelocity);

					// Calculate shot angle and distance leading target movement
					double alpha, sinAlpha;		// target movement angle relative to us
					double theta;				// shot angle relative to scan angle to lead the target's motion
					double range;				// shot distance to lead the target's motion
					
					alpha = 180.0 - (vTargetVelocity.angle() - scanAngle);
					sinAlpha = Math.sin(alpha*Math.PI/180.0);
					theta = Math.asin( sinAlpha * vTargetVelocity.mag() / missleSpeed ) * 180.0/Math.PI;
					range = d * sinAlpha / Math.sin( (alpha+theta)*Math.PI/180.0 );
//					System.out.println("Target speed = " + vTargetVelocity.mag());
					
					// Shoot regardless of range, but make sure shot explodes.
					// Target could get some explosion damage if target is between 700 and 740 m away. 
					if (range>cannonRange) range=cannonRange;  
					cannon((int)(scanAngle+theta), (int)range);

					// Save target location for next shot
					vTargetSaved.set(vTargetTemp);
				}
			} else {
				// Save the target to start tracking it.
				vTargetSaved.set(vTargetTemp);
				bTargetSavedValid = true;
			}
		}
	}
}
