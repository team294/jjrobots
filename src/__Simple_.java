
public class __Simple_ extends JJRobot {
	// __Simple_ robot v. 1.0 for JJRobots, by Don Sawdai
	// This robot randomly drives around the arena and shoots at
	// whatever it can scan.
	
	private int driveAngle = 0;
	private int driveSpeed = 49;	// speed<50, so we can turn without slowing down
	private int scanAngle = 0;
	private int scanRes = 2;

	private final int fieldSize = 1000;
	private final int dangerZone = 50;	// If we get this close to the wall, then change direction
	
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
		// Let's just avoid the walls by changing direction randomly when we get too close.
		// This algorithm only works if driveSpeed < 50 (so that we can turn without slowing down)
		
		int x = loc_x();
		int y = loc_y();
		
		// Are we headed towards a wall?
		if ( (x<=dangerZone && driveAngle>90 && driveAngle<270)							// We are near the left border and traveling left
				|| ((fieldSize-x)<=dangerZone && (driveAngle<90 || driveAngle>270))		// We are near the right border and traveling right
				|| (y<=dangerZone && driveAngle>180)									// We are near the top border and traveling up
				|| ((fieldSize-y)<=dangerZone && driveAngle<180)						// We are near the bottom border and traveling down
				) {
			
			// We are too close to a wall and moving closer, so choose a random new direction
			driveAngle = rand(360);
			drive(driveAngle, driveSpeed);
		}
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
		} else if (d<700) {
			// Found someone within cannon range, so shoot at it
			cannon(scanAngle, d);
		}
	}
}
