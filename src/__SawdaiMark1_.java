
public class __SawdaiMark1_ extends JJRobot {
	// __SawdaiMark1_ robot v. 1.0 for JJRobots, by Don Sawdai
	// This robot drives in a circle around the arena and shoots at
	// whatever it can scan.
	
	private int driveAngle = 0;
	private int driveSpeed = 49;	// speed<50, so we can turn without slowing down
	private int scanAngle = 0;
	private int scanRes = 2;

	// Drive parameters
	private static final int fieldSize = 1000;
	private static final int fieldSizeHalf = fieldSize/2;
	private static final int driveRadiusMin = 300;		// Smallest circle to drive around
	private static final int driveRadiusMax = 400;		// Largest circle to drive around
	
	// Shooting parameters
	private static final int cannonRange = 700;			// Max cannon range
	
	@Override
	void main() {
		// Set initial drive angle and speed
		driveAngle = rand(360);
		drive(driveAngle, driveSpeed);
		
		// Main loop
		while (true) {
			// Don't put any long waits in the loop, so it keeps running repeatedly
			driveRobot();
			scanAndShoot();
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
			else if (r>driveRadiusMin) ang+=90;	// if in the "circle zone", then orbit
												// if too close, then go away from center of field

		// Wrap drive angle within 0-360 degrees
		if (ang>360) ang-=360;
		if (ang<0) ang+=360;

//		if (d_abs(ang-(double)driveAngle)>5) {
//		driveAngle = (int)ang;
//		drive(driveAngle, 10);
//	} else {
//		drive(driveAngle, 100);
//	}


		driveAngle = (int)ang;
		drive(driveAngle, driveSpeed);
	}
	
	/**
	 * Look for other robots and shoot at them.
	 * There are no "waits" in this method, so it won't delay the main robot loop.
	 */
	void scanAndShoot() {
		int d;
		
		d = scan(scanAngle, scanRes);
		if (d==0) {
			// No enemy found, so rotate scanner angle for next time through loop
			scanAngle += scanRes;
		} else if (d<cannonRange) {
			// Found someone within cannon range, so shoot at it
			cannon(scanAngle, d);
		}
	}
}
