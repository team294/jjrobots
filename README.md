# jjrobots
Java robot virtual arena

To use JJRobots, clone this project from GitHub and import it into Eclipse.  All of the source files are in the "src" directory.  The core simulation/arena files are JJxxxx.java -- do not modify these files.  To build this project, open JJArena.java and Run As either as a Java applet or a Java application, and then maximize the application's window.  There are several sample robots in this project.  To run a robot combat, select two robots from the list in the upper-right of the window (such as Counter and Simple), then click Play.  You can slow down the combat simulation by changing Clock to "/8".  For debugging, check "Scan" to show the scanner direction, check "Trace" to show missle tracks, and check "Track" to show robot movement.

For robot programming details, see http://jrobots.sourceforge.net/jjr_info.shtml
* The Java class that contains the code for your robot must have a name beginning with two underscores and ending with a single underscore (e.g. "\_\__RobotName_\_.java"). 
* The class must be derived from the class JJRobot:  For example, "public class MyRobot extends JJRobot { _all your code_ }"
* You must put in the class a method named void main().  That is the method executed first after the creation of the robot. 
* **Note**:  Each robot runs in a separate thread.  To prevent thread collisions between robots, do not use any method/objects from the Java API!  Only use functions listed in the above hyperlink.
* I suggest starting with the "\_\_Simple\_.java" robot as a basic robot design for new robots.

For tips and tricks, see http://jrobots.sourceforge.net/jjr_tutorials.shtml
