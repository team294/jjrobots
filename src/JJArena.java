/*
  JJArena.java version 4.00 alfa (2003/10/19)
    Copyright (C) 2001-2003 Leonardo Boselli (boselli@uno.it)

  Portions of this program were written by:
    Moray Goodwin (moray@jyra.com)
    Christo Fogelberg (doubtme@hotmail.com)
    Alan Lund (alan.lund@acm.org)
    Tim Strazny (timstrazny@gmx.de)
    Walter Nistico' (walnist@infinito.it)
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

// Changes by S.G.:
// Main changes from version 3.1 to 4.00
// Mouse interupts do not take CPU power from simulator
// To stop match click stop button first


import java.applet.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;

public final class JJArena extends Applet
implements Runnable, AppletStub
//, AppletContext //sg! alows compilation for 1.4
{
	//fields/static

	static private boolean isApplication;
	static private String codeBase = "file://";

	//fields/volatile/private
	//
	volatile private boolean forcedStop=true;    //sg! forced restart of the app
	volatile private boolean running=false;      //only one app should run at the same time
	volatile private boolean showRanking=false;  //sg! we can restart when showing results
	//fields/private

	private int winner=-1;
	private JJRobots jjRobots;
	private String[] names;
	private String[] jn;
	private int[] id;
	private int[][] wins;
	private int[][] matches;
	private float[][] perc;

	private JJGraph jjGraph = null;
	private String[][] graph;
	private Thread th=null;

	private Frame results = null;
	private TextArea results_ta = null;
	private boolean random = false;
	private int sorted = Integer.MIN_VALUE;
	private double speed = 2;
	private int countMode;
	private int mode;

	private long readTime;

	private Frame combats = null;
	private TextArea combats_ta = null;
	private String combatsHistory="";
	private final String COMBATHEADER = "";

	private TextField startDistance = new TextField("",3);
	boolean singleMode=true;
	boolean doubleMode=false;
	boolean teamMode=false;

	private Checkbox loopCheck;
	private Button   pauseButton;
	private boolean  pause=false;
	private java.awt.List chosenRobot;

	private Choice speedSelector = new Choice();
	private Choice display = new Choice();
	private Choice selection = new Choice();
	private int fps = 60;
	private boolean goOnNoDisplay = true;

	// virtual clock generator
	private static JJClock clock;
	private static int currentPriority = 0;

	static int getClockPriority() {
		if (clock.isAlive())
			return clock.getPriority();
		else
			return currentPriority;
	}
	// switch to choose to run synchronous clock
	private final boolean SYNCH = true;

	//methods/AppletContext

	public AudioClip getAudioClip(URL url) {
		if(isApplication) {
			return null;
		} else {
			return super.getAudioClip(url);
		}
	}

	public Applet getApplet(String name) {
		return null;
	}

	public Enumeration getApplets() {
		return null;
	}

	public void showDocument(URL url) {
	}

	public void showDocument(URL url, String target) {
	}

	public void showStatus(String status) {
		if(isApplication) {
		} else {
			super.showStatus(status);
		}
	}

	//methods/AppletStub

	public boolean isActive() {
		if(isApplication) {
			return true;
		} else {
			return super.isActive();
		}
	}

	public Image getImage(URL url) {
		if(isApplication) {
			//    return null;
			return Toolkit.getDefaultToolkit().getImage(url);
		} else {
			return super.getImage(url);
		}
	}

	public URL getCodeBase() {
		if(isApplication) {
			try {
				return new URL("file:");
			} catch(Exception e) {
				return null;
			}
		} else {
			return super.getCodeBase();
		}
	}

	public URL getDocumentBase() {
		if(isApplication) {
			return getCodeBase();
		} else {
			return super.getDocumentBase();
		}
	}

	public String getParameter(String name) {
		if(isApplication) {
			return null;
		} else {
			return super.getParameter(name);
		}
	}

	public AppletContext getAppletContext() {
		if(isApplication) {
			return null;
		} else {
			return super.getAppletContext();
		}
	}

	public void appletResize(int width, int height) {
	}

	//methods/public

	static public void main(String[] args) {
		isApplication = true;
		JJArena arena = new JJArena();
		arena.setStub((AppletStub)arena);
		Frame f = new Frame(arena.getAppletInfo());
		f.setLayout(new BorderLayout());
		f.add("Center",arena);
		//f.reshape(0,0,600,400);
		f.reshape(0,0,700,500);             // al
		f.show();
		arena.init();
		arena.start();
	}

	public String getAppletInfo() {
		return "JJRobots (c) 2000-2003 L.Boselli - boselli@uno.it";
	}

	public String getStartInfo() {//sg!
		return "JJRobots (c) starting...";
	}

	public String getStopInfo() {//sg!
		return "JJRobots (c) to restart a match press stop button first...";
	}


	public void init() {

		clock = new JJClock();
		clock.setResolution(0.0002);//clock granularity 0.2ms
		if (!SYNCH)
			clock.start();

		setBackground(Color.lightGray);
		setLayout(new BorderLayout());

		readResults();

		chosenRobot = new java.awt.List(jn.length,true);

		for(int ct = 0; ct < jn.length; ct++) {
			String name = jn[ct].substring(2);
			name = name.substring(0,name.indexOf('_'));
			chosenRobot.addItem(name);
		}

		Panel panel = new Panel();
		panel.setLayout(new BorderLayout());
		Panel p = new Panel();
		p.setLayout(new GridLayout(1, 3));
		p.add(new Button("Clear"));

		Button save=new Button("Save");
		save.enable(isApplication);
		p.add(save);

		Button close=new Button("Close");
		close.enable(isApplication);
		p.add(close);
		panel.add("North", p);
		panel.add("Center",chosenRobot);

		Panel panel2 = new Panel();
		panel2.setLayout(new BorderLayout());

		Panel panel3, panel4;

		panel3 = new Panel(new GridLayout(10,1));
		panel3.setBackground(Color.white);
		panel3.add(new Label("Show"));
		panel3.add(new Checkbox("Scan"));
		panel3.add(new Checkbox("Trace"));
		panel3.add(new Checkbox("Track"));
		panel3.add(new Checkbox("Results"));
		panel3.add(new Checkbox("Combats"));
		panel3.add(new Checkbox("Graphs"));
		panel3.add(new Checkbox("Skin"));
		panel3.add(new Checkbox("Debug"));
		panel2.add("West",panel3);

		panel3 = new Panel(new GridLayout(9,1));
		panel3.add(new Label("Options"));
		panel3.add(new Label("Display"));
		panel3.add(new Label("Select"));
		panel3.add(new Label("Clock"));
		panel3.add(new Label("SSDist"));//Single Start Distance

		panel3.add(new Label("Match"));
		panel3.add(new Checkbox("Single",true));
		panel3.add(new Checkbox("Double",false));
		panel3.add(new Checkbox("Team",false));
		panel2.add("Center",panel3);

		panel3 = new Panel(new GridLayout(9,1));

		panel3.add(new Label(""));
		display.addItem("Smooth");
		display.addItem("Fast");
		display.addItem("Fastest");
		display.addItem("None");
		panel3.add(display);

		selection.addItem("Rand");
		selection.addItem("Weight");
		panel3.add(selection);

		speedSelector.addItem("/8");
		speedSelector.addItem("/4");
		speedSelector.addItem("/2");
		speedSelector.addItem("Official");
		speedSelector.addItem("x2");
		speedSelector.addItem("x4");
		speedSelector.addItem("x8");
		speedSelector.select("Official");
		panel3.add(speedSelector);

		panel3.add(startDistance);
		panel3.add(new Label(""));

		panel3.add(pauseButton = new Button("Play"));
		panel3.add(new Button("Stop"));
		panel3.add(loopCheck = new Checkbox("Loop"));
		panel2.add("East",panel3);

		panel.add("South",panel2);

		add("East",panel);

		//sg! -------------------- extremely important ----------------------------------
		try{ // very important!! allow cleanup of old threats
			// old threats MUST finish before proceeding
			Thread.sleep(2000); // the bigger delay the better
		}
		catch (InterruptedException e){}

		//sg! -------------------- extremely important ----------------------------------

		jjRobots = new JJRobots();
		add("Center",jjRobots);

		readCombats();
		readBackground();

		validate();
	}

	private void readResults() {
		try {
			readTime = System.currentTimeMillis();
			Vector lines = new Vector();
			String line;

			URLConnection c = new URL(getCodeBase(),"robots.txt").openConnection();
			c.setUseCaches(false);
			DataInputStream dis = new DataInputStream(c.getInputStream());
			if((line = dis.readLine()) != null) {
				while((line = dis.readLine()) != null) lines.addElement(line);
			}
			int count = lines.size();
			jn = new String[count];
			id = new int[count];
			wins = new int[count][];
			matches = new int[count][];
			perc = new float[count][];
			graph = new String[count][];
			for(int ct = 0; ct < count; ct++) {
				StringTokenizer st = new StringTokenizer((String)lines.elementAt(ct));
				jn[ct] = st.nextToken();
				id[ct] = ct;
				wins[ct] = new int[3];
				matches[ct] = new int[3];
				perc[ct] = new float[3];
				for(int ct1 = 0; ct1 < 3; ct1++) {
					wins[ct][ct1] = Integer.parseInt(st.nextToken());
					matches[ct][ct1] = Integer.parseInt(st.nextToken());
					perc[ct][ct1] = matches[ct][ct1] != 0? wins[ct][ct1]*100f/matches[ct][ct1]: 0;
				}
				if(graph[ct] == null) {
					graph[ct] = new String[3];
					for (int i = 0; i < 3; i++) {
						try { graph[ct][i] = st.nextToken(); } catch(Exception e) { graph[ct][i] = "x"; }
						graph[ct][i] = graph[ct][i].substring(1);
					}
				}
			}
			dis.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private void readCombats() {
		combatsHistory=COMBATHEADER;
		try {
			String line;

			URLConnection c = new URL(getCodeBase(),"combats.txt").openConnection();
			c.setUseCaches(false);
			DataInputStream dis = new DataInputStream(c.getInputStream());

			while ( (line=dis.readLine())!=null)
				combatsHistory.concat(line+getProperty("line.separator", "\n"));
			dis.close();
		} catch(Exception e) {
		}
	}

	private void readBackground() {
		int i;
		try {
			URL backgroundFile = new URL(getCodeBase(),"background.jpg");
			Image background = getImage(backgroundFile);
			jjRobots.setBackground(background);
		} catch(Exception e) {
		}
	}

	private String getResults(int mode) {
		sort(mode);
		String newLine = System.getProperty("line.separator");
		String dos = "000000"+newLine;
		for(int ct = 0; ct < jn.length; ct++) {
			dos += jn[ct];
			for(int ct1 = 0; ct1 < 3; ct1++)
				dos += " "+wins[ct][ct1]+" "+matches[ct][ct1];
			for (int i = 0; i < 3; i++)
				dos += "     X" + graph[ct][i];
			dos += newLine;
		}
		return dos;
	}

	private void saveResults() {
		try {
			DataOutputStream robots = new DataOutputStream(new FileOutputStream("robots.txt"));
			robots.writeBytes(getResults(-1));
			getResults(countMode);
			robots.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private void saveCombats() {
		try {
			DataOutputStream combats = new DataOutputStream(new FileOutputStream("combats.txt"));
			combats.writeBytes(combatsHistory);
			combats.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private void showCombats(boolean state) {
		if (state) {
			if (combats == null) {
				combats = new Frame("JRobots combats (copy and paste into \"combats.txt\")");
				combats.add(combats_ta = new TextArea(20, 40));
				combats_ta.setEditable(false);
				combats_ta.setBackground(Color.white);
				combats.pack();
			}
			updateCombats();
		}
		if (combats != null) {
			if (state) combats.show();
			else combats.hide();
		}
	}

	private void showResults(boolean state) {
		if (state) {
			if (results == null) {
				results = new Frame("JRobots results (copy and paste into \"robots.txt\")");
				results.add(results_ta = new TextArea(chosenRobot.countItems()+2, 40));
				results_ta.setEditable(false);
				results_ta.setBackground(Color.white);
				results.pack();
			}
			updateResults();
		}
		if (results != null) {
			if (state) results.show();
			else results.hide();
		}
	}

	private void updateCombats() {
		if (combats_ta!=null)
			combats_ta.setText(combatsHistory);
	}

	private void updateResults() {
		if (results_ta!=null)
			results_ta.setText(getResults(countMode));
	}

	private void showGraphs(boolean state) {
		if (state && (jjGraph == null)) {
			String[] items = new String[chosenRobot.countItems()];
			for (int i = 0; i < items.length; i++)
				items[i] = chosenRobot.getItem(i);
			jjGraph = new JJGraph(jn, graph, items);
		}
		if (jjGraph != null) {
			if (state)
				jjGraph.show();
			else
				jjGraph.hide();
		}
	}

	public void start() {

		showRanking = false;                // ranking not showed yet
		Thread.yield();                    //

		if(th != null) {
			stop();
			try{ Thread.sleep(1000); } catch(InterruptedException e) {}
		}

		th = new Thread(this, "JRobots");

		if(isApplication)
			JJRobots.isApplication(true);
		th.start();
	}

	private void initArena() {

		if(random) randomize();

		names = new String[countMode == JJRobots.TEAM? 4: 2];
		Random rand = new Random();

		if(selection.getSelectedItem()=="Rand") {

			//BEGIN Random Selection

			int ct = 0;
			String name;

			String[] items={};

			String[] it=chosenRobot.getSelectedItems();
			String[] jnn={};

			if (it.length>names.length){
				for (int i=0; i<it.length; i++)
					it[i]="__"+it[i]+'_';
				jnn=it;
			} else {
				jnn=jn;
				items=it;
			}

			for(; ct < items.length && ct < names.length; ct++) {
				names[ct] = "__"+items[ct]+'_';
			}

			for(; ct < names.length; ct++) {
				boolean notFind;
				do {
					notFind = false;
					name = jnn[(int)(rand.nextFloat()*(jnn.length))];
					for(int ct1 = 0; ct1 < ct; ct1++) {
						if(names[ct1].equals(name)) {
							notFind = true;
							break;
						}
					}
				} while(notFind);
				names[ct] = name;
			}

			//END Random Selection

		} else {

			//BEGIN Weighted Selection

			// Select one robot, with robots with higher winning percentages
			// being chosen more often.

			// The parameter c affects the distribution of the choices:
			//     larger  values ==> more even distribution
			//     smaller values ==> better robots chosen more often
			// Changing c dynamically based on the number of matches done already
			// prevents the first few matches from being unfairly allocated

			// Calculate the total number of matches so far...
			int      matchCount = 0;

			for (int i = 0; i < jn.length; i++)
				matchCount += matches[i][countMode];

			// Account for the number of teams per match
			matchCount /= (countMode == JJRobots.TEAM) ? 4 : 2;

			double[] w1         = new double[jn.length];
			double   floor      = 0.05;
			double   threshhold = jn.length * 5;
			double   c          =   (threshhold * threshhold)
					/
					(matchCount * matchCount + 1);

			for (int i = 0; i < jn.length; i++)
				w1[i] = floor + c + perc[i][countMode] / 100.0;

			// A clever way to honor the user's selections from the list of
			// robots (no matter how many there are) is just to make them
			// substantially more likely to be chosen.
			magnifyUserSelections(w1, 1e24);

			int firstRobot = weightedSelection(w1, rand.nextFloat());

			names[0] = jn[firstRobot];

			// Now select the rest, using weights related to the difference between
			// each robot's winning percentage and that of the first robot chosen.

			// The parameter p affects the distribution of the choices:
			//     larger  values ==> more even distribution
			//     smaller values ==> closer robots chosen more often

			double[] w2 = new double[jn.length];
			double   p  = 1.0;

			for (int i = 0; i < jn.length; i++) {
				double diff = (perc[firstRobot][countMode] - perc[i][countMode]) / 100.0;

				w2[i] = 1.01 - Math.pow(Math.abs(diff), p);  // al
			}

			// Again, honor the users choices
			magnifyUserSelections(w2, 1e24);

			// Prevent the first robot from being chosen again
			w2[firstRobot] = 0.0;

			// Select the remaining needed robots
			for (int i = 1; i < names.length; i++) {
				int nextRobot = weightedSelection(w2, rand.nextFloat());

				names[i]      = jn[nextRobot];

				// Prevent this robot from being chosen again
				w2[nextRobot] = 0.0;
			}

			//END Weighted Selection

		}

		jjRobots.init(mode,names);
	}

	//BEGIN Weighted Selection Support

	private void magnifyUserSelections(double[] weights, double factor)
	{
		String[] items = chosenRobot.getSelectedItems();

		for (int i = 0; i < items.length; i++) {
			String name = "__" + items[i] + '_';

			for (int j = 0; j < weights.length; j++) {

				if (jn[j].equals(name))
					weights[j] *= factor;
			}
		}
	}

	private static int weightedSelection(double[] weights, float fraction)
	{
		// First find the total of all of the weights
		double totalWeight = 0.0;

		for (int i = 0; i < weights.length; i++)
			totalWeight += weights[i];

		double goalWeight = totalWeight * fraction;
		int    index      = 0;

		// Need to be careful here that a weight of 0 is never chosen, thus the
		// use of ">="...
		while (index < weights.length && goalWeight >= weights[index]) {
			goalWeight -= weights[index];
			index      += 1;
		}

		// ... but also need to be careful about fraction = 1, which would run
		// off the end of the array (given the use of ">=" above).
		while (index >= weights.length || (index >= 0 && weights[index] == 0.0))
			index--;

		return index;
	}

	//END Weighted Selection Support

	//}

	public void stop() {
		forcedStop  = true;                              // sg! This flag indicatess that app was restarted
		showRanking = false;

		jjRobots.stop();                                 // stop running robots threads

		System.runFinalization();
		System.gc();                                     // clean up

		if(th != null) {                                 // stop
			if (th.isAlive()) th.stop();
			th = null;
		}
	}

	private void sort(int mode) {
		boolean changed;
		if (sorted == mode) return;
		do {
			changed = false;
			for(int ct = 0; ct < jn.length-1; ct++) {
				if (( mode >= 0 && (perc[ct][mode] < perc[ct+1][mode])) ||
						( mode < 0 && (id[ct] > id[ct+1])))
				{
					int tmp = id[ct];
					id[ct] = id[ct+1];
					id[ct+1] = tmp;
					String temps = jn[ct];
					jn[ct] = jn[ct+1];
					jn[ct+1] = temps;
					float[] tempf = perc[ct];
					perc[ct] = perc[ct+1];
					perc[ct+1] = tempf;
					int[] tempi = wins[ct];
					wins[ct] = wins[ct+1];
					wins[ct+1] = tempi;
					tempi = matches[ct];
					matches[ct] = matches[ct+1];
					matches[ct+1] = tempi;
					String[] temp_s = graph[ct];
					graph[ct] = graph[ct+1];
					graph[ct+1] = temp_s;
					changed = true;
				}
			}
		} while(changed);
		sorted = mode;
	}

	public synchronized void run() {
		if (running) {
			do {
				initArena();
				System.gc();

				jjRobots.start();

				runGame();

				if ((results != null) && results.isVisible()) updateResults();

				if ((combats != null) && combats.isVisible()) updateCombats();

				if ((jjGraph != null) && jjGraph.isVisible()) jjGraph.updateGraph();

				try{ Thread.sleep(2000); } catch(InterruptedException e) {}  // time for cleanup needed

			} while( loopCheck.getState() && running);
		}

		showStatus(getAppletInfo());             // sg!
		showRanking(countMode);
	}

	private void runGameVirtual(int updatePeriod) throws InterruptedException{
		long startVT = clock.currentTimeMillis();
		long currentVT, oldVT = startVT;
		updatePeriod *= 5; //must be (0.001/clock.getResolution())
		// here precomputed for efficiency;
		do {
			clock.tick();
			currentVT = clock.currentTimeMillis();
			winner = jjRobots.step((currentVT-oldVT)*clock.getResolution()*speed);
			Thread.yield();
			oldVT = currentVT;
		} while(
				goOnNoDisplay &&
				((currentVT-startVT) <= updatePeriod) &&
				(winner < 0) &&
				(th != null) &&
				(forcedStop != true)
				);
	}

	private void runGameSmooth(int updatePeriod) throws InterruptedException{
		long startVT = clock.currentTimeMillis();
		long currentVT, oldVT = startVT;
		long startRT = System.currentTimeMillis();
		long currentRT;
		do {
			clock.tick();
			currentVT = clock.currentTimeMillis();
			winner = jjRobots.step((currentVT-oldVT)*clock.getResolution()*speed);
			Thread.yield();
			oldVT = currentVT;
			currentRT = System.currentTimeMillis();
		} while(
				goOnNoDisplay &&
				((currentRT-startRT) <= updatePeriod) &&
				(winner < 0) &&
				(th != null) &&
				(forcedStop != true)
				);
	}


	public void runGame() {
		try {
			winner      = -1;                   // running, no winner, no draw yet
			forcedStop  = false;                // do loop not broken by stop yet
			// can be modified asynchroniclly in action loop
			showRanking = false;                // user can restart the game when it is not running or during
			// showRanking loop
			showStatus(getAppletInfo());

			do{// Display:
				goOnNoDisplay = true;
				if(!pause){
					if (fps==0)// None
						runGameVirtual((int)(1000/speed)); //1 frame per Virtual second
					else if (fps>=60)
						runGameSmooth(1000/fps); //60 fps (real!)
					else
						runGameVirtual(1000/fps);
				}
				jjRobots.drawArena(forcedStop);

				// To give non-JRobot Threads some cpu time
				Thread.yield();
			} while(running && (winner < 0) && (th != null) && (forcedStop==false));

			// forced stop by stop() or start() function
			if(forcedStop) {
				winner = 0;    // forcing draw
			}

			jjRobots.drawArena(forcedStop);

			if (winner>=0){
				if (!loopCheck.getState())
					pauseButton.setLabel("Play");
				combatsHistory+=getCombatInfo(winner);
			}

			if(winner-- > 0) {
				for(int ct = 0; ct < names.length; ct++) {
					for(int ct1 = 0; ct1 < jn.length; ct1++) {
						if(jn[ct1].equals(names[ct])) {
							if(ct == winner) wins[ct1][countMode]++;
							perc[ct1][countMode] =
									wins[ct1][countMode]*100f/(++matches[ct1][countMode]);
							if ((perc[ct1][countMode] < 11) && ((perc[ct1][countMode] > 0)))
								graph[ct1][countMode] += 0;
							graph[ct1][countMode] += String.valueOf((int)(perc[ct1][countMode]-1));
							break;
						}
					}
				}
			}

			jjRobots.stop();
			System.runFinalization();
			System.gc();

			if(sorted == countMode) sorted = Integer.MIN_VALUE;
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private void showRanking(int countMode) {
		forcedStop  = false;
		showRanking = true;
		try {
			Dimension size = size();//getSize();
			int width = size.width;
			int height = size.height;
			Image i = createImage(width,height);
			do {
				sort(countMode);
				String title;
				switch(countMode) {
				default:
				case JJRobots.SINGLE: {
					title = "Single";
					break;
				}
				case JJRobots.DOUBLE: {
					title = "Double";
					break;
				}
				case JJRobots.TEAM: {
					title = "Team";
					break;
				}
				}
				title += " Match Results";
				int startRow = height/2;
				int theRow;
				do {
					theRow = startRow;
					if(!pause){
						Graphics g = i.getGraphics();
						g.setColor(Color.lightGray);
						g.fillRect(0,0,width,height);
						if ((jjRobots != null) && (jjRobots.getImage() != null)) g.drawImage(jjRobots.getImage(),0,0,null);
						g.setColor(Color.red.darker());
						g.drawString(title,74,theRow-1);
						for(int ct = 0; ct < jn.length; ct++) {
							String name = jn[ct].substring(2);
							name = "["+ct+"] "+name.substring(0,name.indexOf('_'));
							g.setColor(Color.darkGray);
							String ratio = ""+wins[ct][countMode]+"/"+matches[ct][countMode];
							String thePerc = ""+((int)perc[ct][countMode])+"%";
							theRow += 18;
							g.drawString(ratio,74,theRow-1);
							g.drawString(thePerc,184,theRow-1);
							g.drawString(name,224,theRow-1);
						}
						startRow--;
						jjRobots.getGraphics().drawImage(i,0,0,null);
					}
					Thread.sleep(50);
				} while(theRow >= height/2 && th != null);
				startRow = height;
				if(countMode++ == JJRobots.TEAM) countMode = 0;
			} while((th != null) && (forcedStop==false));
			i.flush();
		} catch(InterruptedException ie) {/* ignore */}
		showRanking = false;
	}

	public void update(Graphics g) {
		paint(g);
	}

	public /*synchronized*/ boolean action(Event evt, Object what) {
		Object obj = evt.target;

		int dist=-1;
		if (startDistance.getText().trim().length()>0)
			dist = Integer.valueOf(startDistance.getText().trim()).intValue();

		if (jjRobots.setDistancia(dist) < 0)
			startDistance.setText("");

		if(obj instanceof Button) {
			String label = ((Button)obj).getLabel();
			if(label.equals("Save")) {
				saveResults();
				saveCombats();
			} else if(label.equals("Clear")) {
				for (int ct=0 ; ct<matches.length ; ct++)
					for (int ctl=0 ; ctl<matches[ct].length ; ctl++)
					{
						perc[ct][ctl] = wins[ct][ctl] = matches[ct][ctl] = 0;
						graph[ct][ctl] = "";
					}
				updateResults();
				combatsHistory = COMBATHEADER;
				updateCombats();
			} else if(label.equals("Close")) {
				System.exit(1);
			} else if (label.equals("Stop")) {
				running = false;
				showStatus(getAppletInfo());   //sg!
				pauseButton.setLabel("Play");
				pause = false;

			} else	if(  label.equals("Pause") || label.equals("Play") ) {
				pause = label.equals("Pause");
				if( !pause && (running==false) || ((forcedStop==false)&&(showRanking==true)) ){  // restart allowed
					random = true;
					pause = false;
					forcedStop  = true;          //sg!
					showRanking = false;         //sg!
					running = true;
					showStatus(getStartInfo());  //sg!
					start();
				}
				if (pause) pauseButton.setLabel("Play");
				else pauseButton.setLabel("Pause");
			}
			return true;
		} else if(obj instanceof Checkbox) {
			Checkbox cb = (Checkbox)obj;
			String label = cb.getLabel();
			boolean state = cb.getState();
			if(label.equals("Scan")) {
				jjRobots.setShowScans(state);
			} else if(label.equals("Trace")) {
				jjRobots.setShowTraces(state);
			} else if(label.equals("Track")) {
				jjRobots.setShowTracks(state);
			} else if(label.equals("Results")) {
				showResults(state);
			} else if(label.equals("Combats")) {
				showCombats(state);
			} else if(label.equals("Graphs")) {
				showGraphs(state);
			}
			else if(label.equals("Skin")) {
				jjRobots.setSkin(state);
			}
			else if(label.equals("Debug")) {
				jjRobots.setDebug(state);
			}
			else if(label.equals("Single")) {
				if (state==false && !(doubleMode || teamMode)) cb.setState(true);
				singleMode = cb.getState();
			}
			else if(label.equals("Double")) {
				if (state==false && !(singleMode || teamMode)) cb.setState(true);
				doubleMode = cb.getState();
			}
			else if(label.equals("Team")) {
				if (state==false && !(doubleMode || singleMode)) cb.setState(true);
				teamMode = cb.getState();
			}
		}else if(obj instanceof Choice) {
			if (obj.equals(display))
			{
				goOnNoDisplay = false;
				if (display.getSelectedItem() == "Smooth")
					fps = 60; //fps
				else if (display.getSelectedItem() == "Fast")
					fps = 30; //fps
				else if (display.getSelectedItem() == "Fastest")
					fps = 10; //fps
				else if (display.getSelectedItem() == "None")
					fps = 0; //fps
				jjRobots.draw = (fps != 0);
			}
			else if (obj.equals(speedSelector))
			{
				if (speedSelector.getSelectedItem()=="/8")
					speed = 0.125;
				else if (speedSelector.getSelectedItem()=="/4")
					speed = 0.25;
				else if (speedSelector.getSelectedItem()=="/2")
					speed = 0.5;
				else if (speedSelector.getSelectedItem()=="Official")
					speed = 1;
				else if (speedSelector.getSelectedItem()=="x2")
					speed = 2;
				else if (speedSelector.getSelectedItem()=="x4")
					speed = 4;
				else if (speedSelector.getSelectedItem()=="x8")
					speed = 8;
			}
		}
		return false;
	}

	private void randomize() {
		if (! (singleMode||doubleMode||teamMode))
			return;

		while (true){
			int modo= (int) (Math.random()*3);

			if (singleMode&&modo==0){
				mode=JJRobots.N_SINGLE;
				countMode=JJRobots.SINGLE;
				break;
			}
			else if (doubleMode&&modo==1){
				mode=JJRobots.N_DOUBLE;
				countMode=JJRobots.DOUBLE;
				break;
			}
			else if (teamMode&&modo==2){
				mode=JJRobots.N_TEAM;
				countMode=JJRobots.TEAM;
				break;
			}
		}
	}

	String getCombatInfo(int winner) {
		String result = "";
		if (winner-->=0)
		{
			TimeZone.getDefault();
			Date date = new Date();

			String modo = (mode==1?"S":(mode==2?"D":"T")) + (winner<0?"Draw":"");
			for (int i=0; i<names.length; i++)
				for (int j=0; j<chosenRobot.countItems(); j++)
					if (names[i].substring(2, names[i].length()-1).matches(chosenRobot.getItem(j))){
						if (i==winner || winner<0 && i==0)
							result=modo+":"+j+":"+result;
						else
							result+=j+",";
						break;
					}
			if (mode<4) result+=",,";

			result +=
					"\t"
							+(date.getYear()+1900)+"/"+(date.getMonth()+1)+"/"+date.getDate()+" "+date.getHours()+":"+date.getMinutes()+":"+date.getSeconds()+"\t"
							//	+ getProperty("host.number","?")+ "\t"
							+ getProperty("user.timezone","?")+ "\t"
							+ getProperty("user.country", "?")+ "\t"
							+ getIPAddress() + "\t"
							+ getProperty("os.name","NO")+"\t"
							+ getProperty("java.vm.version", "NO") + "\t"
							+ (isApplication ? "P" : "A") + "\t"
							+ (mode==1?"S":(mode==2?"D":"T"))+"\t"
							+ ((winner<0) ? "D" : "W") + "\t"
							+ jjRobots.getLastResult()
							;
		}
		return result + getProperty("line.separator","\n");
	}

	String getProperty(String name, String value) {
		try{
			value = System.getProperty(name,value);
		}catch (Exception e) {
			//        e.printStackTrace();
		}
		return value;
	}

	/** Returns the IP address that the JVM is running in */
	String getIPAddress() {
		String strIPAddress=null;
		URL origin=null;
		String hostName=null;
		String strProtocol=null;
		Socket local=null;
		InetAddress objLocalHost=null;
		try{
			strIPAddress=InetAddress.getLocalHost().getHostAddress();

			origin=this.getCodeBase(); //this is the Applet-class
			hostName=origin.getHost();
			strProtocol=origin.getProtocol();

			if (origin.getPort()!=-1)
				local=new Socket(hostName, origin.getPort());
			else{
				if (strProtocol.equalsIgnoreCase("http"))
					local=new Socket(hostName, 80);
				else if (strProtocol.equalsIgnoreCase("https"))
					local=new Socket(hostName, 443);
			}

			if (local!=null){
				objLocalHost=local.getLocalAddress();
				strIPAddress=objLocalHost.getHostAddress();
			}
		}
		catch (Exception ex){
			ex.printStackTrace();
			strIPAddress="";
		}
		finally{
			try{
				if (local!=null)
					local.close();
			}
			catch (IOException ioe){
				ioe.printStackTrace();
			}
		}
		return strIPAddress;
	}
}
