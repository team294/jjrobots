import java.awt.Graphics;
/*
  JJRobot.java version 4.00 alfa (2003/10/19)
    Copyright (C) 2001-2003 Leonardo Boselli (boselli@uno.it)

  Portions of this program were written by:
    Christo Fogelberg (doubtme@hotmail.com)
    Alan Lund (alan.lund@acm.org)
    Tim Strazny (timstrazny@gmx.de)
    Samuel (a1rex@hotmail.com)
---

  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License
  as published by the Free Software Foundation; either version 2
  of the License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  See the GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the
    Free Software Foundation, Inc.,
    59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

  Please send remarks, questions and bug reports to
    boselli@uno.it
  or write to:
    Leonardo Boselli
    Via Paoletti, 33
    18100 Imperia
    ITALY
 */

abstract public class JJRobot implements Runnable {

	static final private double DEG2RAD = Math.PI/180;
	static final private double RAD2DEG = 180/Math.PI;

	private JJRobots jjRobots;
	private int id;

	JJRobot() {
	}

	final public void run() {
		try {
			main();
		} catch(Exception e) {
			jjRobots.kre(this);   //sg! robot exits due to exception
			e.printStackTrace();
		}
		finally
		{
			jjRobots.krf(this);   //sg! the robot may decide to exit on its own
		}
	}

	abstract void main();

	final void init(int id, JJRobots jjRobots) {
		if(jjRobots.regID(this)) {
			this.id = id;
			this.jjRobots = jjRobots;
		}
	}

	final int getID() {
		return id;
	}

	/**
	 * If there are n robots in the team, this number goes from 0, the first robot created, to n-1, the last one.
	 * This function is available to distinguish between robots in team and double matches. 
	 * @return The identification number of this robot in the team
	 */
	final protected int id() {
		return jjRobots.id(this);
	}

	/**
	 * Returns the number of team mates that started this match:
	 * gives 1 for SINGLE, 2 for DOUBLE, 8 for TEAM.  Note that it doesn't report the number of mates still active (!)
	 * @return
	 */
	final protected int getFriendsCount() {
		return jjRobots.getFriendsCount();
	}

	/**
	 * The elapsed "game-time" seconds since the beginning of the fight 
	 * @return
	 */
	final protected double time() {
		return jjRobots.time();
	}

	/**
	 * Scans for nearest robot in a specific direction.  It scans the battlefield with a resolution from 1 to 20 degrees. 
	 * Scanning the battlefield, the robot receives the distance of the nearest robot (both friend or enemy) or zero if there is no one in that sector.
	 * @param degree is the direction in degrees of the scan (angles start from 3 o'clock and increase clockwise). 
	 * @param resolution is the width of the scan in degrees (scan start degrees-resolution/2 to degrees+resolution/2). Its value must be greater than 0 and lesser than 21 degrees. 
	 * @return the distance of the nearest robot found in the scanned region, or zero if no robot was found.
	 */
	final protected int scan(int degree, int resolution) {
		return jjRobots.scan(this,degree,resolution);
	}

	/**
	 * Shoots the cannon.  The robot can point the cannon all around and can fire all the missiles it wants, but there is a reload time of 1 second. 
	 * Missiles have a range of 700 meters and a speed of 300 m/s. The speed of the missile is independent from the speed of the robot,
	 * so it's always 300 m/s. When a missile explodes, it gives damage points to all the robots nearby. Damage points depend on the distance 
	 * of the robot from the explosion (5 meters = 10 damage points, 20 meters = 5 damage points, 40 meters = 3 damage points).  
	 * If a robot fires a missile within a circle of 5 meters radius, it gives itself 10 damage points, so it's better to fire the missiles far away.
	 * @param degree is the direction in degrees of the shot (angles start from 3 o'clock and increase clockwise)
	 * @param range is the distance where the missile explodes, in meters
	 * @return 1 if the missile was fired, 0 if not (due to reload time)
	 */
	final protected int cannon(int degree, int range) {
		return jjRobots.cannon(this,degree,range);
	}

	/**
	 * Set direction and speed of the robot movement.  
	 * <b>Notes</b>:  Robots can change their direction only if the speed is lower than 50% (= 15 m/s).  Acceleration and deceleration are 5 m/s2.
	 * @param degree is the direction of movement of the robot (angles start from 3 o'clock and increase clockwise).  
	 * @param speed is the speed in percent that the robot must reach: 0% means 0 m/s, 100% means 30 m/s.
	 */
	final protected void drive(int degree, int speed) {
		jjRobots.drive(this,degree,speed);
	}

	/**
	 * Returns the damage points of this robot: 0 to 99 means alive, 100 means dead (the robot will never read this value). 
	 * @return
	 */
	final protected int damage() {
		return jjRobots.damage(this);
	}

	/**
	 * The speed of the robot in percent: 0 means 0 m/s, 100 means 30 m/s.
	 * @return
	 */
	final protected int speed() {
		return jjRobots.speed(this);
	}

	/**
	 * Reads the X location of the robot, in meters
	 * @return the X coordinate of the robot in the battlefield (the origin is in the upper-left corner and X coordinates increase to the right).
	 */
	final protected int loc_x() {
		return i_rnd(jjRobots.loc_x(this));
	}

	/**
	 * Reads the Y location of the robot, in meters
	 * @return the Y coordinate of the robot in the battlefield (the origin is in the upper-left corner and Y coordinates increase to the bottom).
	 */
	final protected int loc_y() {
		return i_rnd(jjRobots.loc_y(this));
	}

	// - Additions by alan.lund@acm.org
	final protected double d_loc_x() {
		return jjRobots.d_loc_x(this);
	}

	final protected double d_loc_y() {
		return jjRobots.d_loc_y(this);
	}

	final protected int cannon(JJVector v) {
		return jjRobots.cannon(this, i_rnd(v.angle()), i_rnd(v.mag()));
	}

	final protected JJVector vscan(int degree, int resolution) {
		int distance = jjRobots.scan(this,degree,resolution);

		if (distance > 0)
			return JJVector.Polar(distance, degree);
		else
			return null;
	}

	final protected void drive(JJVector v) {
		jjRobots.drive(this, i_rnd(v.angle()), i_rnd(100.0 * v.speed() / 30.0));
	}

	final protected double actual_speed() {
		// Returns 0.0 to 30.0, not 0 to 100 like speed()
		return jjRobots.actual_speed(this);
	}

	final protected double heading() {
		return jjRobots.heading(this);
	}

	final protected JJVector velocity() {
		return JJVector.Polar(jjRobots.actual_speed(this),
				rad2deg(jjRobots.heading(this)),
				1.0);
	}

	final protected JJVector location() {
		return new JJVector(jjRobots.d_loc_x(this),
				jjRobots.d_loc_y(this),
				jjRobots.time());
	}
	// - End additions by alan.lund@acm.org

	/**
	 * Return a random integer between 0 and (limit-1), inclusive
	 * @param limit Max value to return (+1)
	 * @return Random integer
	 */
	static protected final int rand(int limit) {
		return (int)(Math.random()*limit);
	}

	static protected final int sqrt(int number) {
		return (int)Math.sqrt(Math.abs(number));
	}

	static final double deg2rad(int degree) {
		int deg = degree%360;
		if(deg > 180) deg -= 360;
		return deg*DEG2RAD;
	}

	static protected final int sin(int degree) {
		return (int)(100000*Math.sin(deg2rad(degree)));
	}

	static protected final int cos(int degree) {
		return (int)(100000*Math.cos(deg2rad(degree)));
	}

	static protected final int tan(int degree) {
		return (int)(100000*Math.tan(deg2rad(degree)));
	}

	static protected final int atan(int value) {
		return (int)(Math.atan(value/100000.0)/DEG2RAD);
	}

	static protected final double d_sqrt(double number) {
		return Math.sqrt(number);
	}

	static protected final double d_sin(double rad) {
		return Math.sin(rad);
	}

	static protected final double d_cos(double rad) {
		return Math.cos(rad);
	}

	static protected final double d_tan(double rad) {
		return Math.tan(rad);
	}

	static protected final double d_atan(double value) {
		return Math.atan(value);
	}

	// Additions Below Here By Christo Fogelberg (doubtme@hotmail.com)
	// Send comments/bugs etc my way :)
	static protected final double d_abs(double value) {
		return Math.abs(value);
	}

	static protected final double d_rnd(double value) {
		return Math.floor(value + 0.5d);
	}

	static protected final int i_rnd(double value) {
		return (int) Math.floor(value + 0.5d);
	}

	static protected final double rad2deg(double value) {
		double deg = value * RAD2DEG;
		while(deg < 0) {
			deg = 360 + deg;
		}
		return deg%360;
	}

	// Additions Below Here By Tim Strazny (timstrazny@gmx.de)

	static protected final double exp(double value) {
		return Math.exp(value);
		// e^value
	}

	static protected final double log(double value) {
		return Math.log(value);
		// ln value
	}

	protected final void finalize() {
	}

	// Additions Below Here By Samuel J. Grabski (frostytower@hotmail.com)
	// enjoy more math functions :)
	static protected final double floor(double value) {
		return Math.floor(value);
	}
	static protected final double ceil(double value) {
		return Math.ceil(value);
	}
	static protected final double atan2(double y, double x) {
		return Math.atan2(y,x);
	}
	static protected final double asin(double x) {
		return Math.asin(x);
	}
	static protected final double acos(double x) {
		return Math.acos(x);
	}
	static protected final double min(double x,double y) {
		return Math.min(x,y);
	}
	static protected final double max(double x,double y) {
		return Math.max(x,y);
	}
	static protected final double round(double x) {
		return Math.round(x);
	}
	// - End additions by Samuel J. Grabski (frostytower@hotmail.com)

	final protected boolean image(int x[], int y[]) {
		return jjRobots.image(this,x,y);
	}

	public void debug(Graphics g, double cte, int offX, int offY)
	{
	}

}
