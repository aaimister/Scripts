/**
 * @author Aaimister
 * @version 1.32 ©2010-2011 Aaimister, No one except Aaimister has the right to
 *          modify and/or spread this script without the permission of Aaimister.
 *          I'm not held responsible for any damage that may occur to your
 *          property.
 */

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import org.rsbot.event.events.MessageEvent;
import org.rsbot.event.listeners.MessageListener;
import org.rsbot.event.listeners.PaintListener;
import org.rsbot.gui.AccountManager;
import org.rsbot.script.Script;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.util.Filter;
import org.rsbot.script.wrappers.RSArea;
import org.rsbot.script.wrappers.RSGroundItem;
import org.rsbot.script.wrappers.RSItem;
import org.rsbot.script.wrappers.RSNPC;
import org.rsbot.script.wrappers.RSPlayer;
import org.rsbot.script.wrappers.RSTile;

@ScriptManifest(authors = { "Aaimister" }, website = "http://922d1ef9.any.gs", name = "Aaimisters Chicken Killer v1.32", keywords = "Combat", version = 1.32, description = ("Kills chickens."))
public class AaimistersChickenKiller extends Script implements MessageListener, PaintListener, MouseListener {

	private RSTile InPen;

	private RSArea Pen;
	private RSArea ChickenPenF = new RSArea(new RSTile(3014, 3282), new RSTile(
			3020, 3297));
	private RSArea ChickenPenELum = new RSArea(new RSTile(3225, 3295),
			new RSTile(3236, 3301));
	private RSArea ChickenPenNLum = new RSArea(new RSTile(3184, 3276), new RSTile(3192, 3279));
	private RSArea ChickenPenFout = new RSArea(new RSTile(3026, 3282), new RSTile(3037, 3291));
	private RSArea Champion = new RSArea(new RSTile(3195, 3351), new RSTile(3199, 3360));
	
	//Falador Porch Bad Rooms / Doors
	//Room1
	private RSArea badRoom1 = new RSArea(new RSTile(3030, 3291), new RSTile(3040, 3296));
	//Door for Room 1 (id = 8695)
	private RSArea room1Door = new RSArea(new RSTile(3033, 3289), new RSTile(3035, 3292));
	private RSTile room1DoorT = new RSTile(3034, 3291);
	//Room2
	private RSArea badRoom2 = new RSArea(new RSTile(3022, 3286), new RSTile(3025, 3290));
	//Door for Room 2 (id = 8695)
	private RSArea room2Door = new RSArea(new RSTile(3025, 3286), new RSTile(3027, 3288));
	private RSTile room2DoorT = new RSTile(3025, 3287);
	
	//Chicken Pen East Lumbridge
	private RSArea gateAreaE = new RSArea(new RSTile(3235, 3293), new RSTile(3239, 3298));
	private RSTile gateT = new RSTile(3238, 3294);
	//Gate ID = 45206
	
	//Chicken Pen North Lumbridge
	private RSArea gateAreaN = new RSArea(new RSTile(3187, 3278), new RSTile(3191, 3281));
	private RSTile gateTileN = new RSTile(3189, 3280);
	//Gate ID = 45206
	//Room 1
	private RSArea lumroom1 = new RSArea(new RSTile(3188, 3271), new RSTile(3192, 3275));
	private RSArea lumdoor1 = new RSArea(new RSTile(3188, 3274), new RSTile(3190, 3277));
	private RSTile lumdoorT = new RSTile(3190, 3274);
	//Door ID = 45476
	
	//Chicken Pen Falador Pen
	private RSArea fBadRoom = new RSArea(new RSTile(3021, 3291), new RSTile(3025, 3296));
	private RSArea fDoorArea = new RSArea(new RSTile(3019, 3291), new RSTile(3022, 3294));
	private RSTile fDoorTile = new RSTile(3022, 3292);
	//Door ID = 8695
	
	private final String[] locationstring = { "Falador Pen", "Falador Porch", "Lumbridge E. Pen", "Lumbridge N. Pen", "Champion's Guild" };
	private final String[] statstring = { "Attack", "Strength", "Defence", "Range", "Magic" };
	private final String[] colorstring = { "Black", "Blue", "Brown", "Cyan", "Green", "Lime", "Orange", "Pink", "Purple", "Red", "White", "Yellow" };
	private String url = "http://922d1ef9.any.gs";
	
	private long nextBreak = System.currentTimeMillis();
	private long nextLength = 60000;
	private long totalBreakTime;
	private long antiBanRandom = random(15000, 90000);
	private long antiBanTime = System.currentTimeMillis() + antiBanRandom;
	private long lastBreakTime;
	private long nextBreakT;
	private long startTime;
	private long runTime;
	private long now;
	
	private ArrayList<Integer> loot = new ArrayList<Integer>();
	private ArrayList<Integer> drop = new ArrayList<Integer>();
	
	Updater u = new Updater();
	AaimistersGUI g = new AaimistersGUI();
	public final File settingsFile = new File(getCacheDirectory(), "AaimistersCKillerSettings.txt");
	
	NumberFormat formatter = new DecimalFormat("#,###,###");
	
	Font Cam10 = new Font("Cambria Math", Font.BOLD, 10);
	Font Cam = new Font("Cambria Math", Font.BOLD, 12);
	
	Color PercentGreen = new Color(0, 163, 4, 150);
	Color PercentRed = new Color(163, 4, 0, 150);
	Color White150 = new Color(255, 255, 255, 150);
	Color White90 = new Color(255, 255, 255, 90);
	Color White = new Color(255, 255, 255);
	Color Background = new Color(219, 200, 167);
	Color UpGreen = new Color(0, 169, 0);
	Color LineColor = new Color(0, 0, 0);
	Color ClickC = new Color(187, 0, 0);
	Color UpRed = new Color(169, 0, 0);
	Color Black = new Color(0, 0, 0);
	Color MainColor;
	Color ThinColor;
	Color BoxColor;
	
	final NumberFormat nf = NumberFormat.getInstance();

	boolean currentlyBreaking;
	boolean randomBreaks;
	boolean waitForLoot;
	boolean useSetting;
	boolean antiBanOn;
	boolean showPaint = true;
	boolean painting;
	boolean doBreak;
	boolean checked;
	boolean checkAmmo;
	boolean outofAmmo;
	boolean clicked;
	boolean logTime;
	boolean closed;
	boolean wait;
	boolean skip;
	boolean stop;
	
	//Paint Buttons
	boolean xButton;
	boolean StatC;
	boolean StatP;
	boolean StatM;
	boolean StatI;
	boolean Main = true;

	private String currentChic = "Chic Lvl 1";
	private String currentStat = "";
	private String status = "";
	private String penType = "";
	
	int dropp[] = { 1925, 1944, 2138 };
	int Chickens[] = { 1017, 41 };
	int runEnergy = (random(40, 75));
	int all[] = { 882, 314, 526 };
	int arrow = 882;
	int feathers = 314;
	int bones = 526;
	int stopLevel = 99;
	int random;
	int pickItem;
	int stat;
	int barStat;
	int priceFeather;
	int totalGP;
	int featherHour;
	int gpHour;
	int totalFeather;
	int errorCount;
	int checkFea;
	int i;
	int dotCount;
	int chicToLvl;
	int chicHour;
	int xpGained;
	int xpHour;
	int buriedBones;
	int totalChic;
	int xpChic;
	int maxBetween;
	int minBetween;
	int maxLength;
	int minLength;
	//Chosen Stat
	int sxpHour;
	int scurrentXP;
	int stimeToLvl;
	int sgainedLvl;
	int sstartXP;
	int sxpGained;
	int sxpToLvl;
	//Constution
	int cxpHour;
	int ccurrentXP;
	int ctimeToLvl;
	int cgainedLvl;
	int cstartXP;
	int cxpGained;
	int cxpToLvl;
	//Prayer
	int pxpHour;
	int pcurrentXP;
	int ptimeToLvl;
	int pgainedLvl;
	int pstartXP;
	int pxpGained;
	int pxpToLvl;

	private enum State { BACKTOCHICK, ATTACK };

	private State getState() {
		if (!atPen()) {
			return State.BACKTOCHICK;
		} else {
			return State.ATTACK;
		}
	}

	public double getVersion() { 
		return 1.32;
	}
	
	@Override
	public boolean onStart() {
		status = "Starting up";
		
		//CheckfoUpdate
		if (getUpdate() > getVersion()) {
			update();
			if (closed || stop) {
	        	log.severe("The GUI window was closed!");
	        	return false;
	        }
		}
		
        try {
			settingsFile.createNewFile();
		} catch (final IOException ignored) {
		}
        
		createAndWaitforGUI();
		if (closed) {
        	log.severe("The GUI window was closed!");
        	return false;
        }
		
		if (doBreak) {
			if (AccountManager.isTakingBreaks(account.getName())) {
					log.severe("Turn Off Bot Breaks!");
					log.severe("Turning off custom breaker...");
					doBreak = false;
			} else {
				breakingNew();
			}
		}
        
		return true;
	}
	
	private void update() {
        if (SwingUtilities.isEventDispatchThread()) {
            u.Updater.setVisible(true);
        } else {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                    	u.Updater.setVisible(true);
                    }
                });
            } catch (InvocationTargetException ite) {
            } catch (InterruptedException ie) {
            }
        }
        sleep(100);
        while (u.Updater.isVisible()) {
            sleep(100);
        }
    }
	
	private void createAndWaitforGUI() {
        if (SwingUtilities.isEventDispatchThread()) {
            g.AaimistersGUI.setVisible(true);
        } else {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                    	g.AaimistersGUI.setVisible(true);
                    }
                });
            } catch (InvocationTargetException ite) {
            } catch (InterruptedException ie) {
            }
        }
        sleep(100);
        while (g.AaimistersGUI.isVisible()) {
            sleep(100);
        }
    }
	
	public double getUpdate() {
	    try {
	        BufferedReader r = new BufferedReader(new InputStreamReader(new URL("http://aaimister.webs.com/scripts/AaimistersChickenVersion.txt").openStream()));
	        double d = Double.parseDouble(r.readLine());
	        r.close();
	       return d;
	    } catch(Exception e) {
	        log("Could not check for update, sorry. =/");
	    }
	    return getVersion();
	}
	
	public void openThread(){
		if (java.awt.Desktop.isDesktopSupported()) {
			java.awt.Desktop desktop = java.awt.Desktop.getDesktop();

			if (!desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
				log("Can't open thread. Something is conflicting.");
				return;
			}

			try {

				java.net.URI uri = new java.net.URI(url);
				desktop.browse(uri);
			} catch (Exception e) {

			}
		}
	}
	
	public void onFinish() {
		runTime = (System.currentTimeMillis() - startTime) - totalBreakTime;
		long totalTime = System.currentTimeMillis() - startTime;
    	final String formattedTime = formatTime((int) totalTime);
		log("Thanks for using Aaimister's Chicken Killer!");
		if (loot.contains(bones)) {
			log("In " + formattedTime + " You killed " + formatter.format(totalChic) + " chickens, gained " + formatter.format(sgainedLvl) + " Level(s), and buried " + formatter.format(buriedBones) + " bones!");
		} else {
			log("In " + formattedTime + " You killed " + formatter.format(totalChic) + " chickens, and gained " + formatter.format(sgainedLvl) + " Level(s)!");
		}
	}
	
	private String getDots() {
		if (dotCount <= 15) {
			dotCount++;
			return ".";
		} else if (dotCount >= 15 && dotCount <= 25) {
			dotCount++;
			return "..";
		} else if (dotCount >= 25 && dotCount <= 35) {
			dotCount++;
			return "...";
		} else if (dotCount >= 35 && dotCount <= 45) {
			dotCount++;
			return "";
		} else {
			dotCount = 0;
			return ".";
		}
	}
	
	private String getLocation() {
		if (atPen()) {
			return penType;
		} else if (calc.distanceTo(InPen) > 100) {
			if (!game.isLoggedIn()) {
				return "Login Screen";
			} else {
				return "Unknown";
			}
		} else {
			return "Close to Pen";
		}
	}
	
	private void breakingNew(){
		if (randomBreaks){
			long varTime = random(3660000, 10800000);
			nextBreak = System.currentTimeMillis() + varTime;
			nextBreakT = varTime;
			long varLength = random(900000, 3600000);
			nextLength = varLength;
		} else {
			int diff = random(0, 5) * 1000 * 60;
			long varTime = random((minBetween * 1000 * 60) + diff, (maxBetween * 1000 * 60) - diff);
			nextBreak = System.currentTimeMillis() + varTime;
			nextBreakT = varTime;
			int diff2 = random(0, 5) * 1000 * 60;
			long varLength = random((minLength * 1000 * 60) + diff2, (maxLength * 1000 * 60) - diff2);
			nextLength = varLength;
		}
		logTime = true;
	}

	private boolean breakingCheck(){
		if (nextBreak <= System.currentTimeMillis()){
			return true;
		}
		return false;
	}
	
	private boolean atPen() {
		return Pen.contains(getMyPlayer().getLocation());
	}

	private boolean idle() {
		if (getMyPlayer().getAnimation() == -1) {
			return true;
		}
		return false;
	}
	
	public boolean checkLog() {
		if (getMyPlayer().isOnScreen() && game.isLoggedIn() && calc.distanceTo(InPen) < 20) {
			return true;
		}
		return false;
	}
	
	public boolean dyingChic() {
		for (RSNPC i : npcs.getAll()) {
			if (i.getAnimation() == 5389 && calc.distanceTo(i.getLocation()) < 2 && getMyPlayer().isInCombat()) {
				return true;
			}
		}
		return false;
	}
	
	public void bury(boolean leftToRight, int... items) {
		while (inventory.getCount(items) != 0) {
			if (!leftToRight) {
				for (int c = 0; c < 4; c++) {
					for (int r = 0; r < 7; r++) {
						boolean found = false;
						for (int i = 0; i < items.length && !found; ++i) {
							found = items[i] == inventory.getItems()[c + r * 4]
									.getID();
						}
						if (!found) {
							buryItem(c, r);
						}
					}
				}
			} else {
				for (int r = 0; r < 7; r++) {
					for (int c = 0; c < 4; c++) {
						boolean found = true;
						for (int i = 0; i < items.length && found; ++i) {
							found = items[i] == inventory.getItems()[c + r * 4]
									.getID();
						}
						if (found) {
							buryItem(c, r);
						}
					}
				}
			}
			sleep(random(500, 800));
		}
	}

	public void buryItem(int col, int row) {
		if (interfaces.get(210).getComponent(2).getText()
				.equals("Click here to continue")) {
			sleep(random(800, 1300));
			if (interfaces.get(210).getComponent(2).getText()
					.equals("Click here to continue")) {
				interfaces.get(210).getComponent(2).doClick();
				sleep(random(150, 200));
			}
		}
		if (game.getCurrentTab() != 4
				&& !interfaces.get(762).isValid()
				&& !interfaces.get(620).isValid()) {
			game.openTab(4);
		}
		if (col < 0 || col > 3 || row < 0 || row > 6)
			return;
		if (inventory.getItems()[col + row * 4].getID() == -1)
			return;
		Point p;
		p = mouse.getLocation();
		if (p.x < 563 + col * 42 || p.x >= 563 + col * 42 + 32
				|| p.y < 213 + row * 36 || p.y >= 213 + row * 36 + 32) {
			mouse.hop(inventory.getInterface().getComponents()[row * 4 + col]
					.getCenter(), 10, 10);
		}
		mouse.click(false);
		sleep(100, 200);
		menu.click("Bury");
		sleep(25, 50);
	}

	private boolean checkDrop() {
		RSItem all[] = inventory.getItems();
		for (int i = 0; i < all.length; i++) {
			for (int z = 0; z < drop.size(); z++) {
				if (all[i].getID() == drop.get(z)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private void doDrop() {
		RSItem all[] = inventory.getItems();
		for (int i = 0; i < all.length; i++) {
			for (int z = 0; z < drop.size(); z++) {
				if (all[i].getID() == drop.get(z)) {
					inventory.getItem(all[i].getID()).interact("Drop");
					sleep(200, 500);
				}
			}
		}
	}
	
	private void openDoor(int id, RSTile tile) {
		if (objects.getNearest(id).isOnScreen()) {
			objects.getNearest(id).interact("Open");
			sleep(500, 1250);
		} else if (!getMyPlayer().isMoving() || calc.distanceTo(walking.getDestination()) < 4) {
			walking.walkTileMM(walking.getClosestTileOnMap(tile.randomize(1, 1)));
			sleep(100, 300);
		}
	}
	
	private void checkDoors() {
		if (Pen == ChickenPenFout) {
			if (badRoom1.contains(getMyPlayer().getLocation())) {
				if (room1Door.contains(objects.getNearest(8695).getLocation())) {
					openDoor(8695, room1DoorT);
				}
			} else if (badRoom2.contains(getMyPlayer().getLocation())) {
				if (room2Door.contains(objects.getNearest(8695).getLocation())) {
					openDoor(8695, room2DoorT);
				}
			}
		} else if (Pen == ChickenPenNLum) {
			if (lumroom1.contains(getMyPlayer().getLocation())) {
				if (lumdoor1.contains(objects.getNearest(45476).getLocation())) {
					openDoor(45476, lumdoorT);
				}
			} else if (!Pen.contains(getMyPlayer().getLocation())) {
				if (gateAreaN.contains(objects.getNearest(45206).getLocation())) {
					openDoor(45206, gateTileN);
				}
			}
		} else if (Pen == ChickenPenELum) {
			if (!Pen.contains(getMyPlayer().getLocation())) {
				if (gateAreaE.contains(objects.getNearest(45206).getLocation())) {
					openDoor(45206, gateT);
				}
			}
		} else if (Pen == ChickenPenF) {
			if (fBadRoom.contains(getMyPlayer().getLocation())) {
				if (fDoorArea.contains(objects.getNearest(8695).getLocation())) {
					openDoor(8695, fDoorTile);
				}
			}
		}
	}
	
	private void checkLootDoor(RSTile loott) {
		if (Pen == ChickenPenFout) {
			if (badRoom1.contains(loott) || badRoom2.contains(loott)) {
				if (badRoom1.contains(loott)) {
					if (room1Door.contains(objects.getNearest(8695).getLocation())) {
						openDoor(8695, room1DoorT);
					}
				} else if (badRoom2.contains(feather().getLocation())) {
					if (room2Door.contains(objects.getNearest(8695).getLocation())) {
						openDoor(8695, room2DoorT);
					}
				}
			}
		} else if (Pen == ChickenPenNLum) {
			if (lumroom1.contains(loott)) {
				if (lumdoor1.contains(objects.getNearest(45476).getLocation())) {
					openDoor(45476, lumdoorT);
				}
			}
		} else if (Pen == ChickenPenF) {
			if (fBadRoom.contains(loott)) {
				if (fDoorArea.contains(objects.getNearest(8695).getLocation())) {
					openDoor(8695, fDoorTile);
				}
			}
		}
	}
	
	private void setCamera() {
		if (camera.getPitch() < 10) {
			camera.setPitch(true);
			sleep(1000, 1600);
		}
	}

	private void setRun() {
		if (!walking.isRunEnabled()) {
			if (walking.getEnergy() >= 50) {
				walking.setRun(true);
				sleep(1000, 1600);
			}
		}
	}
	
	private RSGroundItem arrow() {
		return groundItems.getNearest(new Filter<RSGroundItem>() {
			public boolean accept(RSGroundItem g) {
				return g.getItem().getID() == arrow && Pen.contains(g.getLocation());
			}
		});
	}
	
	private RSGroundItem feather() {
		return groundItems.getNearest(new Filter<RSGroundItem>() {
			public boolean accept(RSGroundItem g) {
				return g.getItem().getID() == feathers && Pen.contains(g.getLocation());
			}
		});
	}
	
	private RSGroundItem bones() {
		return groundItems.getNearest(new Filter<RSGroundItem>() {
			public boolean accept(RSGroundItem g) {
				return g.getItem().getID() == bones && Pen.contains(g.getLocation());
			}
		});
	}
	
	private RSNPC newNPC() {
		RSNPC interacting = interactingNPC();
		return interacting != null ? interacting : npcs.getNearest(new Filter<RSNPC>() {
			public boolean accept(RSNPC npc) {
				return npc.getName().equals("Chicken") && npc.getHPPercent() > 0 && !npc.isInCombat() && Pen.contains(npc.getLocation());
			}
		});
	}
	
	private RSNPC interactingNPC() {
		return npcs.getNearest(new Filter<RSNPC>() {
			public boolean accept(RSNPC n) {
				return n.getInteracting() != null && n.getInteracting().equals(players.getMyPlayer()) && Pen.contains(n.getLocation());
			}
		});
	}
	
	private RSPlayer playerNear() {
		RSPlayer me = myPlayer();
		return me != null ? me : players.getNearest(new Filter<RSPlayer>() {
			public boolean accept(RSPlayer p) {
				return !p.isMoving() && p.isOnScreen();
			}
		});
	}
	 
	private RSPlayer myPlayer() {
		final String myName = players.getMyPlayer().getName();
		 return players.getNearest(new Filter<RSPlayer>() {
			 public boolean accept(RSPlayer p) {
				 return p.getName() == myName;
			 }
		 });
	}
	
	@Override
	public int loop() {
		if (breakingCheck() && doBreak) {
			status = "Breaking";
			long endTime = System.currentTimeMillis() + nextLength;
			totalBreakTime += (nextLength + 5000);
    		lastBreakTime = (totalBreakTime - (nextLength + 5000));
			currentlyBreaking = true;
			while (game.isLoggedIn()) {
				game.logout(false);
				sleep(50);
			}
			log("Taking a break for " + formatTime((int) nextLength));
			while (System.currentTimeMillis() < endTime && currentlyBreaking == true){
				sleep(1000);
			}
			currentlyBreaking = false;
			while (!game.isLoggedIn()) {
				try {
					breakingNew();
					game.login();
				} catch (Exception e) {
					return 10;
				}
				sleep(50);
			}
			return 10;
		}
		
		if (!game.isLoggedIn()) {
			status = "Logging In / Breaking";
			return 3000;
		}
		
		if (startTime == 0 && skills.getCurrentLevel(stat) != 0) {
			startTime = System.currentTimeMillis();
			sstartXP = skills.getCurrentExp(stat);
			scurrentXP = skills.getExpToNextLevel(stat);
			cstartXP = skills.getCurrentExp(3);
			ccurrentXP = skills.getExpToNextLevel(3);
			pstartXP = skills.getCurrentExp(5);
			pcurrentXP = skills.getExpToNextLevel(5);
			if (loot.contains(feathers)) {
		    	priceFeather = getGuidePrice(feathers);
		    	log("Price of feathers: " + priceFeather);
		    }
		}
		
		if (i < 1) {
 			checkFea = inventory.getCount(true, 314);
 			i++;
 		}
		
		if (logTime) {
			log("Next Break In: " + formatTime((int) nextBreakT) + " For: " + formatTime((int) nextLength) + ".");
			logTime = false;
		}
		
		setCamera();
		setRun();
		mouse.setSpeed(random(4, 9));
		
		if (skills.getCurrentLevel(stat) >= stopLevel) {
			log.severe("You have reached level " + stopLevel + "! Stopping Script...");
			game.logout(false);
			stopScript();
		}
		
		switch (getState()) {
		case ATTACK:
			checkDoors();
			if (atPen()) {
				if (checkAmmo) {
					checkAmmo = false;
					RSItem wield = inventory.getItem(arrow);
					if (game.getCurrentTab() != 4) {
						game.openTab(4);
					} else {
						if (inventory.contains(arrow)) {
							wield.interact("Wield");
							return random(800, 1000);
						} else {
							outofAmmo = true;
						}
					}
				}
				if (players.getMyPlayer().getInteracting() != null) {
					if (interfaces.canContinue()) {
						interfaces.clickContinue();
					}
					if (loot.contains(feathers) || loot.contains(bones) || loot.contains(arrow)) {
						wait = true;
						skip = false;
					}
					return random(200, 555);
				}
				if ((dyingChic() || waitForLoot) && wait && !skip) {
					sleep(2750, 3000);
				}
				try {
					RSNPC chic = newNPC();
					if (chic != null && !wait) {
						status = "Killing chickens";
						if (players.getMyPlayer().isMoving()) {
							return random(400, 600);
						}
						checkLootDoor(chic.getLocation());
						if (chic.isOnScreen()) {
							chic.interact("Attack " + chic.getName());
							return random(1000, 1600);
						} else if (!chic.isOnScreen()) {
							RSTile chicT = walking.getClosestTileOnMap(chic.getLocation()).randomize(-1, 1);
							if (Pen.contains(chicT)) {
								walking.walkTileMM(chicT);
							} else {
								return 100;
							}
							return random(1100, 1500);
						}
					} else {
						if (checkDrop()) {
							status = "Dropping junk";
							doDrop();
							return random(350, 600);
						} else if (inventory.isFull()) {
							while (inventory.contains(bones)) {
								if (loot.contains(bones)) {
									status = "Burying bones";
									bury(true, bones);
								} else {
									status = "Dropping bones";
									inventory.getItem(bones).interact("Drop");
								}
								sleep(600, 1350);
							}
						} else {
							if (Pen.contains(getMyPlayer().getLocation()) && !loot.isEmpty()) {
								skip = true;
								while (feather() != null && loot.contains(feathers) && checkLog()) {
									status = "Looting feathers";
									checkLootDoor(feather().getLocation());
									if (feather().isOnScreen()) {
										feather().interact("Take " + feather().getItem().getName());
										sleep(800, 1200);
										totalFeather = inventory.getCount(true, 314) - checkFea;
										return 10;
									} else {
										walking.walkTileMM(feather().getLocation().randomize(1, 1));
										return random(1000, 1350);
									}
								} 
								while (arrow() != null && loot.contains(arrow) && checkLog()) {
									status = "Picking up arrows";
									checkLootDoor(arrow().getLocation());
									if (arrow().isOnScreen()) {
										arrow().interact("Take " + arrow().getItem().getName());
										return random(800, 1200);
									} else {
										walking.walkTileMM(arrow().getLocation().randomize(1, 1));
										return random(1000, 1350);
									}
								} 
								while (bones() != null && loot.contains(bones) && checkLog()) {
									status = "Looting bones";
									checkLootDoor(bones().getLocation());
									if (bones().isOnScreen()) {
										bones().interact("Take " + bones().getItem().getName());
										if (newNPC() != null) {
											wait = false;
										}
										return random(800, 1200);
									} else {
										walking.walkTileMM(bones().getLocation().randomize(1, 1));
										return random(1000, 1350);
									}
								}
								wait = false;
							} else {
								wait = false;
							}
						}
					}
				} catch (Exception e) {
					
				}
				if (antiBanTime <= System.currentTimeMillis()) {
					doAntiBan();
					return random(1000, 1600);
				}
			}
			break;
		case BACKTOCHICK:
			checkDoors();
			status = "Finding pen";
			if (!atPen()) {
				if (!idle() || getMyPlayer().isInCombat()) {
					return random(1000, 1500);
				}
				if (idle() && !getMyPlayer().isInCombat()) {
					RSTile Next = walking.getClosestTileOnMap(InPen).randomize(-2, 2);
					walking.walkTileMM(Next);
					sleep(1000, 1500);
				} else {
					return random(1000, 1600);
				}
			}
			break;
		}
		return (random(650, 1050));
	}

	//Credits Aion
	private int getGuidePrice(int itemID) {
        try {
            URL url = new URL(
                    "http://services.runescape.com/m=itemdb_rs/viewitem.ws?obj="
                            + itemID);
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    url.openStream()));
            String line = null;

            while ((line = br.readLine()) != null) {
                if (line.contains("<b>Current guide price:</b>")) {
                    line = line.replace("<b>Current guide price:</b>", "");
                    return (int) parse(line);
                }
            }
        } catch (IOException e) {
        }
        return -1;
    }
	
	
	//Credits Aion
    private double parse(String str) {
        if (str != null && !str.isEmpty()) {
            str = stripFormatting(str);
            str = str.substring(str.indexOf(58) + 2, str.length());
            str = str.replace(",", "");
            if (!str.endsWith("%")) {
                if (!str.endsWith("k") && !str.endsWith("m")) {
                    return Double.parseDouble(str);
                }
                return Double.parseDouble(str.substring(0, str.length() - 1))
                        * (str.endsWith("m") ? 1000000 : 1000);
            }
            int k = str.startsWith("+") ? 1 : -1;
            str = str.substring(1);
            return Double.parseDouble(str.substring(0, str.length() - 1)) * k;
        }
        return -1D;
    }

    public void doAntiBan() {

	    if (antiBanOn == false) {
	          return;
	    }

	    antiBanRandom = random(15000, 90000);
	    antiBanTime = System.currentTimeMillis() + antiBanRandom;
	    
	    int action = random(0, 6);

	    switch (action) {
	    case 0:
	    	rotateCamera();
	    	sleep(200, 400);
	      break;
	    case 1:
	    	mouse.moveRandomly(100, 900);
	    	sleep(200, 400);
	    	break;
	    case 2:
	    	checkPlayer();
	    	sleep(200, 400);
	    	break;
	    case 3:
	    	rotateCamera();
	    	sleep(200, 400);
	    	break;
	    case 4:
	    	checkEXP();
	    	sleep(200, 400);
	    	break;
	    case 5:
	    	mouse.moveOffScreen();
	    	sleep(200, 400);
	    	break;
	    case 6:
	    	randomTile();
	    	sleep(200, 400);
	    	break;
	    }
    }
    
    public void randomTile() {
    	RSTile random = InPen.randomize(-5, 5);
    	if (Pen.contains(random)) {
    		if (idle()) {
    			walking.walkTileMM(walking.getClosestTileOnMap(random));
    			sleep(1200, 1500);
    		}
    	} else {
    		mouse.moveRandomly(900);
    	}
    }
    
    public void checkPlayer() {
    	RSPlayer near = playerNear();
    	if (near != null) {
    		if (!getMyPlayer().isMoving()) {
    			if (near.getScreenLocation() != null) {
    				if (mouse.getLocation() != near.getScreenLocation()) {
            			mouse.move(near.getScreenLocation());
            			sleep(300, 550);
            		}
    				mouse.click(false);
    				sleep(300, 500);
    				if (menu.contains("Follow")) {
    					Point menuu = menu.getLocation();
        				int Mx = menuu.x;
        				int My = menuu.y;
        				int x = Mx + random(3, 120);
        				int y = My + random(3, 98);
        				mouse.move(x, y);
                		sleep(2320, 3520);
                		mouse.moveRandomly(100, 900);
                		sleep(50);
                		if (menu.isOpen()) {
                			mouse.moveRandomly(100, 900);
                			sleep(50);
                		}
                		if (menu.isOpen()) {
                			mouse.moveRandomly(100, 900);
                			sleep(50);
                		}
    				} else {
    					mouse.moveRandomly(100, 900);
    				}
    			}
    		} else {
    			return;
    		}
    	} else {
    		mouse.moveRandomly(100, 900);
    	}
	}
    
    @SuppressWarnings("deprecation")
	public void checkEXP() {
    	if (game.getCurrentTab() != 2) {
    		game.openTab(2);
    		sleep(500, 900);
    	}
    	if (stat == 0) {
    		mouse.move(random(551, 604), random(214, 233));
    		sleep(3000, 5500);
    		game.openTab(4);
    		sleep(50, 100);
    		mouse.moveRandomly(50, 900);
    	} else if (stat == 1) {
    		mouse.move(random(551, 604), random(269, 289));
    		sleep(3000, 5500);
    		game.openTab(4);
    		sleep(50, 100);
    		mouse.moveRandomly(50, 900);
    	} else if (stat == 2) {
    		mouse.move(random(551, 604), random(241, 261));
    		sleep(3000, 5500);
    		game.openTab(4);
    		sleep(50, 100);
    		mouse.moveRandomly(50, 900);
    	} else if (stat == 4) {
    		mouse.move(random(551, 604), random(298, 316));
    		sleep(3000, 5500);
    		game.openTab(4);
    		sleep(50, 100);
    		mouse.moveRandomly(50, 900);
    	} else if (stat == 6) {
    		mouse.move(random(553, 604), random(353, 370));
    		sleep(3000, 5500);
    		game.openTab(4);
    		sleep(50, 100);
    		mouse.moveRandomly(50, 900);
    	}
    }
    
    public void rotateCamera() {
        final char[] LR = new char[] { KeyEvent.VK_LEFT,
                KeyEvent.VK_RIGHT };
        final char[] DU = new char[] { KeyEvent.VK_DOWN,
                KeyEvent.VK_UP };

        int x = random(0, 2);
        int key = 0;        

        keyboard.pressKey(LR[x]);
        if (getMyPlayer().getAnimation() != -1) {
            keyboard.pressKey(DU[1]);
            key = 0;
        }
        sleep(500, 1000);
        keyboard.releaseKey(DU[key]);
        keyboard.releaseKey(LR[x]);
    }
    
    private String stripFormatting(String str) {
        if (str != null && !str.isEmpty())
            return str.replaceAll("(^[^<]+>|<[^>]+>|<[^>]+$)", "");
        return "";
    }
	
	@Override
	public void messageReceived(MessageEvent e) {
		if (e.getMessage().contains("You bury the bones")) {
			clicked = false;
			buriedBones++;
		}
		if (stat == 0) {
			if (e.getMessage().contains("You've just advanced an Attack")) {
				sgainedLvl++;
			}
		} else if (stat == 1) {
			if (e.getMessage().contains("You've just advanced a Defence")) {
				sgainedLvl++;
			}
		} else if (stat == 2) {
			if (e.getMessage().contains("You've just advanced a Strength")) {
				sgainedLvl++;
			}
		} else if (stat == 4) {
			if (e.getMessage().contains("You've just advanced a Ranged")) {
				sgainedLvl++;
			}
			if (e.getMessage().contains("There is no ammo")) {
				checkAmmo = true;
				if (outofAmmo) {
					log("Out of ammo. =/");
					stopScript();
				}
			}
		} else if (stat == 6) {
			if (e.getMessage().contains("You've just advanced a Ma")) {
				sgainedLvl++;
			}
		}
		if (e.getMessage().contains("You've just advanced a Prayer")) {
			pgainedLvl++;
		}
		if (e.getMessage().contains("You've just advanced a Con")) {
			cgainedLvl++;
		}
	}

	public void drawMe(final Graphics g) {
		if (getMyPlayer().isOnScreen()) {
        	final RSTile t = getMyPlayer().getLocation();
        	final RSTile tx = new RSTile (t.getX() + 1, t.getY());
        	final RSTile ty = new RSTile (t.getX(), t.getY() + 1);
        	final RSTile txy = new RSTile (t.getX() + 1, t.getY() + 1);
    		calc.tileToScreen(t);
    		calc.tileToScreen(tx);
    		calc.tileToScreen(ty);
    		calc.tileToScreen(txy);
    		final Point pn = calc.tileToScreen(t, 0, 0, 0);
    		final Point px = calc.tileToScreen(tx, 0, 0, 0);
    		final Point py = calc.tileToScreen(ty, 0, 0, 0);
    		final Point pxy = calc.tileToScreen(txy, 0, 0, 0);
    		if (calc.pointOnScreen(pn) && calc.pointOnScreen(px) && calc.pointOnScreen(py) && calc.pointOnScreen(pxy)) {
    			g.setColor(Black);
        		g.drawPolygon(new int[] { py.x, pxy.x, px.x, pn.x }, new int[] {
        				py.y, pxy.y, px.y, pn.y }, 4);
        		g.setColor(ThinColor);
        		g.fillPolygon(new int[] { py.x, pxy.x, px.x, pn.x }, new int[] {
        				py.y, pxy.y, px.y, pn.y }, 4);
    		}
		}
	}
	
	public void drawMouse(final Graphics g) {
		final Point loc = mouse.getLocation();
		final long mpt = System.currentTimeMillis() - mouse.getPressTime();
		if (mouse.getPressTime() == -1 || mpt >= 500) {
			g.setColor(ThinColor);
			g.drawLine(0, loc.y, 766, loc.y);
			g.drawLine(loc.x, 0, loc.x, 505);
			g.setColor(MainColor);
			g.drawLine(0, loc.y + 1, 766, loc.y + 1);
			g.drawLine(0, loc.y - 1, 766, loc.y - 1);
			g.drawLine(loc.x + 1, 0, loc.x + 1, 505);
			g.drawLine(loc.x - 1, 0, loc.x - 1, 505);
		}
		if (mpt < 500) {
			g.setColor(ClickC);
			g.drawLine(0, loc.y, 766, loc.y);
			g.drawLine(loc.x, 0, loc.x, 505);
			g.setColor(MainColor);
			g.drawLine(0, loc.y + 1, 766, loc.y + 1);
			g.drawLine(0, loc.y - 1, 766, loc.y - 1);
			g.drawLine(loc.x + 1, 0, loc.x + 1, 505);
			g.drawLine(loc.x - 1, 0, loc.x - 1, 505);
		}
	}

	public void mouseClicked(MouseEvent e){
	}
	public void mouseEntered(MouseEvent e){
	}
	public void mouseExited(MouseEvent e){
	}
	public void mousePressed(MouseEvent e){
		//X Button
		if (e.getX() >= 497 && e.getX() < 497 + 16 && e.getY() >= 344 && e.getY() < 344 + 16) {
			if (!xButton) {
				xButton = true;
			} else {
				xButton = false;
			}
		}
		//Next Button
		if (e.getX() >= 478 && e.getX() < 478 + 16 && e.getY() >= 413 && e.getY() < 413 + 14) {
			if (Main) {
				Main = false;
				StatC = true;
			} else if (StatC) {
				StatC = false;
				if (loot.contains(bones)) {
					StatP = true;
				} else if (loot.contains(feathers)) {
					StatM = true;
				} else {
					StatI = true;
				}
			} else if (StatP) {
				StatP = false;
				if (loot.contains(feathers)) {
					StatM = true;
				} else {
					StatI = true;
				}
			} else if (StatM) {
				StatM = false;
				StatI  = true;
			} else if (StatI) {
				StatI = false;
				Main = true;
			}
		}
		//Prev Button
		if (e.getX() >= 25 && e.getX() < 25 + 16 && e.getY() >= 413 && e.getY() < 413 + 14) {
			if (Main) {
				Main = false;
				StatI = true;
			} else if (StatI) {
				StatI = false;
				if (loot.contains(feathers)) {
					StatM = true;
				} else if (loot.contains(bones)) {
					StatP = true;
				} else {
					StatC = true;
				}
			} else if (StatM) {
				StatM = false;
				if (loot.contains(bones)) {
					StatP = true;
				} else {
					StatC = true;
				}
			} else if (StatP) {
				StatP = false;
				StatC  = true;
			} else if (StatC) {
				StatC = false;
				Main = true;
			}
		}
	}
	public void mouseReleased(MouseEvent e){
	}
	
	String formatTime(final int milliseconds) {
		final long t_seconds = milliseconds / 1000;
		final long t_minutes = t_seconds / 60;
		final long t_hours = t_minutes / 60;
		final int seconds = (int) (t_seconds % 60);
		final int minutes = (int) (t_minutes % 60);
		final int hours = (int) (t_hours % 60);
		return (nf.format(hours) + ":" + nf.format(minutes) + ":" + nf.format(seconds));
	}
	
	private Image getImage(String url) {
		try {
			return ImageIO.read(new URL(url));
		} catch (IOException e) {
			return null;
		}
	}
	
	private Image logo = getImage("http://i88.photobucket.com/albums/k170/aaimister/AaimistersChickenKiller.gif");
	private Image atom = getImage("http://i88.photobucket.com/albums/k170/aaimister/Atomm.png");
	
	public void getStat() {
    	if (stat == 0) {
    		currentStat = "Attack";
    	} else if (stat == 2) {
    		currentStat = "Strength";
    	} else if (stat == 1) {
    		currentStat = "Defense";
    	} else if (stat == 4) {
    		currentStat = "Range";
    	} else if (stat == 6) {
    		currentStat = "Magic";
    	}
    }
	
	public void onRepaint(Graphics g) {
		long totalTime = System.currentTimeMillis() - startTime;
    	final String formattedTime = formatTime((int) totalTime);
    	
    	if (!currentlyBreaking) {
    		runTime = (System.currentTimeMillis() - startTime) - totalBreakTime;
    		now = (totalTime);
    		checked = false;
    	} else {
    		if (!game.isLoggedIn()) {
    			if (!checked) {
    				runTime = (now - lastBreakTime);
            		checked = true;
    			}
    		}
    	}
		
		if (startTime != 0) {
			scurrentXP = skills.getExpToNextLevel(stat);
			ccurrentXP = skills.getExpToNextLevel(3);
			pcurrentXP = skills.getExpToNextLevel(5);

			xpChic = (int) (30 * 0.4);
			sxpToLvl = skills.getExpToNextLevel(stat);
			sxpGained = skills.getCurrentExp(stat) - sstartXP;
			sxpHour = ((int) ((3600000.0 / (double) runTime) * sxpGained));
			cxpToLvl = skills.getExpToNextLevel(3);
			cxpGained = skills.getCurrentExp(3) - cstartXP;
			cxpHour = ((int) ((3600000.0 / (double) runTime) * cxpGained));
			pxpToLvl = skills.getExpToNextLevel(5);
			pxpGained = skills.getCurrentExp(5) - pstartXP;
			pxpHour = ((int) ((3600000.0 / (double) runTime) * pxpGained));
			if (sxpHour != 0) {
				stimeToLvl = (int) (((double) sxpToLvl / (double) sxpHour) * 3600000.0);
				ctimeToLvl = (int) (((double) cxpToLvl / (double) cxpHour) * 3600000.0);
				ptimeToLvl = (int) (((double) pxpToLvl / (double) pxpHour) * 3600000.0);
			}
			totalChic = (int) (sxpGained / (double) xpChic);
			chicHour = (int) ((3600000.0 / (double) runTime) * totalChic);
			chicToLvl = (int) (scurrentXP / (double) xpChic);
			xpGained = (int) sxpGained + cxpGained + pxpGained;
			xpHour = (int) ((3600000.0 / (double) runTime) * xpGained);
			totalGP = (int) (totalFeather * priceFeather);
			featherHour = (int) ((3600000.0 / (double) runTime) * totalFeather);
			gpHour = (int) (featherHour * priceFeather);
		}
		
		if (painting) {
			((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			((Graphics2D)g).setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
			((Graphics2D)g).setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		}
		
		if (!xButton) {
			//Background
			g.setColor(MainColor);
			g.fillRect(6, 344, 507, 129);
			g.setColor(LineColor);
			g.drawRect(6, 344, 507, 129);
			//Logo
			g.drawImage(logo, 6, 349, null);
			g.drawImage(atom, 40, 358, null);
			g.setColor(LineColor);
			g.setFont(Cam10);
			g.drawString("By Aaimister © " + getVersion(), 379, 369);
			//Next Button
			g.setColor(BoxColor);
			g.fillRect(478, 413, 16, 14);
			g.setColor(LineColor);
			g.setFont(Cam);
			g.drawString(">", 481, 424);
			g.drawRect(478, 413, 16, 14);
				//Shadow
				g.setColor(White90);
				g.fillRect(478, 413, 16, 7);
			//Prev Button
				g.setColor(BoxColor);
				g.fillRect(25, 413, 16, 14);
				g.setColor(LineColor);
				g.setFont(Cam);
				g.drawString("<", 28, 424);
				g.drawRect(25, 413, 16, 14);
					//Shadow
					g.setColor(White90);
					g.fillRect(25, 413, 16, 7);
			//Main Box
			g.setColor(BoxColor);
			g.fillRect(59, 374, 401, 95);
			g.setColor(White90);
			g.fillRect(59, 374, 401, 46);
			//Text
			if (Main) {
				barStat = stat;
				getStat();
				//Column 1
				g.setColor(LineColor);
				g.drawString("Time running: " + formattedTime, 63, 390);
				g.drawString("Location: " + getLocation(), 63, 404);
				g.drawString("Status: " + status + getDots(), 63, 418);
				g.drawString("Training: " + currentStat, 63, 433);
				g.drawString("Total XP: " + formatter.format((long)xpGained), 63, 447);
				g.drawString("Total XP/h: " + formatter.format((long)xpHour), 63, 463);
				//Column 2
				g.drawString(currentStat + " Total XP: " + formatter.format((long)sxpGained), 264, 390);
				g.drawString(currentStat + " XP/h: " + formatter.format((long)(sxpHour)), 264, 404);
				g.drawString(currentStat + " XP to Lvl: " + formatter.format((long)(scurrentXP)), 264, 418);
				g.drawString("Lvl in: " + formatTime(stimeToLvl), 264, 433);
				g.drawString("Current Lvl: " + (skills.getCurrentLevel(stat)), 264, 447);
				g.drawString("Gained Lvl(s): " + sgainedLvl, 264, 463);
			}
			if (StatC) {
				barStat = 3;
				currentStat = "Cons.";
				//Column 1
				g.setColor(LineColor);
				g.drawString("Time running: " + formattedTime, 63, 390);
				g.drawString("Location: " + getLocation(), 63, 404);
				g.drawString("Status: " + status + getDots(), 63, 418);
				g.drawString("Training: " + currentStat, 63, 433);
				g.drawString("Total XP: " + formatter.format((long)xpGained), 63, 447);
				g.drawString("Total XP/h: " + formatter.format((long)xpHour), 63, 463);
				//Column 2
				g.drawString("Cons. Total XP: " + formatter.format((long)cxpGained), 264, 390);
				g.drawString("Cons. XP/h: " + formatter.format((long)(cxpHour)), 264, 404);
				g.drawString("Cons. XP to Lvl: " + formatter.format((long)(ccurrentXP)), 264, 418);
				g.drawString("Lvl in: " + formatTime(ctimeToLvl), 264, 433);
				g.drawString("Current Lvl: " + (skills.getCurrentLevel(3)), 264, 447);
				g.drawString("Gained Lvl(s): " + cgainedLvl, 264, 463);
			}
			if (StatP) {
				barStat = 5;
				currentStat = "Prayer";
				//Column 1
				g.setColor(LineColor);
				g.drawString("Time running: " + formattedTime, 63, 390);
				g.drawString("Location: " + getLocation(), 63, 404);
				g.drawString("Status: " + status + getDots(), 63, 418);
				g.drawString("Training: " + currentStat, 63, 433);
				g.drawString("Total XP: " + formatter.format((long)xpGained), 63, 447);
				g.drawString("Total XP/h: " + formatter.format((long)xpHour), 63, 463);
				//Column 2
				g.drawString("Prayer Total XP: " + formatter.format((long)pxpGained), 264, 390);
				g.drawString("Prayer XP/h: " + formatter.format((long)(pxpHour)), 264, 404);
				g.drawString("Prayer XP to Lvl: " + formatter.format((long)(pcurrentXP)), 264, 418);
				g.drawString("Lvl in: " + formatTime(ptimeToLvl), 264, 433);
				g.drawString("Current Lvl: " + (skills.getCurrentLevel(5)), 264, 447);
				g.drawString("Gained Lvl(s): " + pgainedLvl, 264, 463);
			}
			if (StatM) {
				barStat = stat;
				getStat();
				//Column 1
				g.setColor(LineColor);
				g.drawString("Time running: " + formattedTime, 63, 390);
				g.drawString("Location: " + getLocation(), 63, 404);
				g.drawString("Status: " + status + getDots(), 63, 418);
				g.drawString("Training: " + currentStat, 63, 433);
				g.drawString("Total XP: " + formatter.format((long)xpGained), 63, 447);
				g.drawString("Total XP/h: " + formatter.format((long)xpHour), 63, 463);
				//Column 2
				g.drawString("Total Money: $" + formatter.format((long)totalGP), 264, 390);
				g.drawString("Money / Hour: $" + formatter.format((long)(gpHour)), 264, 404);
				g.drawString("Price of Feathers: $" + formatter.format((long)(priceFeather)), 264, 418);
				g.drawString("Total Feather(s): " + formatter.format((long)(totalFeather)), 264, 433);
				g.drawString("Feather(s) / Hour: " + formatter.format((long)(featherHour)), 264, 447);
				g.drawString("Feater Type: Plain", 264, 463);
			}
			if (StatI) {
				barStat = stat;
				getStat();
				//Column 1
				g.setColor(LineColor);
				g.drawString("Time running: " + formattedTime, 63, 390);
				g.drawString("Location: " + getLocation(), 63, 404);
				g.drawString("Status: " + status + getDots(), 63, 418);
				g.drawString("Training: " + currentStat, 63, 433);
				g.drawString("Total XP: " + formatter.format((long)xpGained), 63, 447);
				g.drawString("Total XP/h: " + formatter.format((long)xpHour), 63, 463);
				//Column 2
				g.drawString("Chic(s) Killed: " + formatter.format((long)(totalChic)), 264, 390);
				g.drawString("Chic(s) / Hour: " + formatter.format((long)(chicHour)), 264, 404);
				g.drawString("Chic Killing: " + currentChic, 264, 418);
				g.drawString("Chic(s) to Lvl: " + formatter.format((long)(chicToLvl)), 264, 433);
				g.drawString("XP / Chic: " + xpChic, 264, 447);
				g.drawString("Kill Chics: Yes!", 264, 463);
			}
			//% Bar
			g.setColor(MainColor);
			g.fillRect(4, 318, 512, 20);
			g.setColor(Black);
			g.fillRect(6, 320, 508, 16);
			g.setColor(PercentRed);
			g.fillRect(6, 320, 508, 16);
			g.setColor(PercentGreen);
			final int Bar = (int) (skills.getPercentToNextLevel(barStat) * 5.08);
			g.fillRect(6, 320, Bar, 16);
			g.setColor(White);
			g.setFont(Cam);
			g.drawString("" + skills.getPercentToNextLevel(barStat) + "% to lvl " + (skills.getCurrentLevel(barStat) + 1) + " " + currentStat, 194, 332);
				//Shadow
				g.setColor(White90);
				g.fillRect(4, 318, 512, 10);
			//X
			g.setColor(LineColor);
			g.setFont(Cam);
			g.drawString("X", 501, 357);
			//Main Box Shadow
			g.setColor(LineColor);
			g.drawRect(59, 374, 401, 95);
			g.drawLine(260, 380, 260, 465);
		} else {
			//X Button
			g.setColor(MainColor);
			g.fillRect(497, 344, 16, 16);
			g.setColor(LineColor);
			g.drawRect(497, 344, 16, 16);
				//X
				g.setColor(LineColor);
				g.setFont(Cam);
				g.drawString("O", 501, 357);
				//Shadow
				g.setColor(White90);
				g.fillRect(497, 344, 17, 8);
		}
		
		//Player
		drawMe(g);
		
		//Mouse
		drawMouse(g);
	}
	
	public class AaimistersGUI {
		private AaimistersGUI() {
			initComponents();
		}
		
		private void breakBoxActionPerformed(ActionEvent e) {
 			doBreak = breakBox.isSelected();
 			randomBreaks = randomBox.isSelected();
 			if (!doBreak) {
 				randomBox.setEnabled(false);
 				randomBox.setSelected(false);
 				maxTimeBeBox.setEnabled(false);
 				minTimeBeBox.setEnabled(false);
 				maxBreakBox.setEnabled(false);
 				minBreakBox.setEnabled(false);
 			} else {
 				randomBox.setEnabled(true);
 				if (!randomBreaks) {
 					maxTimeBeBox.setEnabled(true);
 	 				minTimeBeBox.setEnabled(true);
 	 				maxBreakBox.setEnabled(true);
 	 				minBreakBox.setEnabled(true);
 				}
 			}
 		}
 		
 		private void randomBoxActionPerformed(ActionEvent e) {
 			doBreak = breakBox.isSelected();
 			randomBreaks = randomBox.isSelected();
 			if (randomBreaks == true) {
 				maxTimeBeBox.setEnabled(false);
 				minTimeBeBox.setEnabled(false);
 				maxBreakBox.setEnabled(false);
 				minBreakBox.setEnabled(false);
 			} else {
 				if (doBreak) {
 					maxTimeBeBox.setEnabled(true);
 	 				minTimeBeBox.setEnabled(true);
 	 				maxBreakBox.setEnabled(true);
 	 				minBreakBox.setEnabled(true);
 				}
 			}
 		}
		
		private void submitActionPerformed(ActionEvent e) {
			String color = (String) colorBox.getSelectedItem();
          	if (color.contains("Blue")) {
          		MainColor = new Color(0, 0, 100);
          		ThinColor = new Color(0, 0, 100, 70);
          		LineColor = new Color(255, 255, 255);
          		BoxColor = MainColor;
          	} else if (color.contains("Black")) {
          		MainColor = new Color(0, 0, 0);
          		ThinColor = new Color(0, 0, 0, 70);
          		LineColor = new Color(255, 255, 255);
          		BoxColor = MainColor;
          	} else if (color.contains("Brown")) {
          		MainColor = new Color(92, 51, 23);
          		ThinColor = new Color(92, 51, 23, 70);
          		BoxColor = MainColor;
          	} else if (color.contains("Cyan")) {
          		MainColor = new Color(0, 255, 255);
          		ThinColor = new Color(0, 255, 255, 70);
          		BoxColor = MainColor;
          		LineColor = new Color(0, 0, 0);
          	} else if (color.contains("Green")) {
          		MainColor = new Color(0, 100, 0);
          		ThinColor = new Color(0, 100, 0, 70);
          		BoxColor = MainColor;
          	} else if (color.contains("Lime")) {
          		MainColor = new Color(0, 220, 0);
          		ThinColor = new Color(0, 220, 0, 70);
          		BoxColor = MainColor;
          		LineColor = new Color(0, 0, 0);
          	} else if (color.contains("Orange")) {
          		MainColor = new Color(255, 127, 0);
          		ThinColor = new Color(255, 127, 0, 70);
          		BoxColor = MainColor;
          		LineColor = new Color(0, 0, 0);
          	} else if (color.contains("Pink")) {
          		MainColor = new Color(238, 18, 137);
          		ThinColor = new Color(238, 18, 137, 70);
          		BoxColor = MainColor;
          		LineColor = new Color(0, 0, 0);
          	} else if (color.contains("Purple")) {
          		MainColor = new Color(104, 34, 139);
          		ThinColor = new Color(104, 34, 139, 70);
          		BoxColor = MainColor;
          	} else if (color.contains("Red")) {
          		MainColor = new Color(100, 0, 0);
          		ThinColor = new Color(100, 0, 0, 70);
          		ClickC = Black;
          		BoxColor = MainColor;
          	} else if (color.contains("White")) {
          		MainColor = new Color(255, 255, 255);
          		ThinColor = new Color(255, 255, 255, 70);
          		LineColor = new Color(0, 0, 0);
          		BoxColor = new Color(140, 140, 140);
          		LineColor = new Color(0, 0, 0);
          	} else if (color.contains("Yellow")) {
          		MainColor = new Color(238, 201, 0);
          		ThinColor = new Color(238, 201, 0, 70);
          		BoxColor = MainColor;
          		LineColor = new Color(0, 0, 0);
          	}
        	String chosen = locationBox.getSelectedItem().toString();
            if (chosen.contains("Falador Pen")) {
            	InPen = new RSTile(3017, 3290);
    			Pen = ChickenPenF;
    			penType = "Falador Pen";
            } else if (chosen.contains("Falador Porch")) {
            	InPen = new RSTile(3032, 3286);
    			Pen = ChickenPenFout;
    			penType = "Falador Porch";
            } else if (chosen.contains("Lumbridge E. Pen")) {
            	InPen = new RSTile(3231, 3297);
            	Pen = ChickenPenELum;
            	penType = "NorthEast Lum.";
            } else if (chosen.contains("Lumbridge N. Pen")) {
            	InPen = new RSTile(3188, 3277);
            	Pen = ChickenPenNLum;
            	penType = "North Lum.";
            } else if (chosen.contains("Champion's Guild")) {
            	InPen = new RSTile(3197, 3355);
    			Pen = Champion;
    			penType = "Champion's Guild";
            }
            String stats = statBox.getSelectedItem().toString();
            if (stats.contains("Attack")) {
            	stat = 0;
            } else if (stats.contains("Strength")) {
            	stat = 2;
            } else if (stats.contains("Defence")) {
                stat = 1;
            } else if (stats.contains("Range")) {
            	stat = 4;
            } else if (stats.contains("Magic")) {
            	stat = 6;
            }
            for (int i = 0; i < dropp.length; i++) {
            	drop.add(dropp[i]);
            }
            if (waitBox.isSelected()) {
            	waitForLoot = true;
            }
            if (arrowBox.isSelected()) {
            	loot.add(arrow);
            }
            if (featherBox.isSelected()) {
            	loot.add(feathers);
            }
            if (paintBox.isSelected()) {
            	painting = true;
            }
            if (antibanBox.isSelected()) {
            	antiBanOn = true;
            }
            if (buryBox.isSelected()) {
            	loot.add(bones);
            } else {
            	drop.add(bones);
            }
            if (breakBox.isSelected()) {
            	doBreak = true;
            	if (randomBox.isSelected()) {
            		randomBreaks = true;
            	}
            	maxBetween = Integer.parseInt(maxTimeBeBox.getValue().toString());
            	minBetween = Integer.parseInt(minTimeBeBox.getValue().toString());
            	maxLength = Integer.parseInt(maxBreakBox.getValue().toString());
            	minLength = Integer.parseInt(minBreakBox.getValue().toString());
            	if (minBetween < 1) {
            		minBetween = 1;
            	}
            	if (minLength < 1) {
            		minLength = 1;
            	}
            	if (maxBetween > 5000) {
            		maxBetween = 5000;
            	} else if (maxBetween < 6) {
            		maxBetween = 6;
            	}
            	if (maxLength > 5000) {
            		maxLength = 5000;
            	} else if (maxLength < 5) {
            		maxLength = 5;
            	}
            }
            
            stopLevel = Integer.parseInt(levelBox.getValue().toString());
            if (stopLevel < 1) {
            	stopLevel = 1;
            } else if (stopLevel >= 99) {
            	stopLevel = 99;
            }
            
            // Write settings
			try {
				final BufferedWriter out = new BufferedWriter(new FileWriter(settingsFile));
				out.write((buryBox.isSelected() ? true : false)
						+ ":" // 0
						+ (locationBox.getSelectedIndex())
						+ ":" // 1
						+ (statBox.getSelectedIndex())
						+ ":" // 2
						+ (colorBox.getSelectedIndex())
						+ ":" // 3
						+ (antibanBox.isSelected() ? true : false)
						+ ":" // 4
						+ (waitBox.isSelected() ? true : false)
						+ ":" // 5
						+ (featherBox.isSelected() ? true : false)
						+ ":" // 6
						+ (arrowBox.isSelected() ? true : false)
						+ ":" // 7
						+ (paintBox.isSelected() ? true : false)
						+ ":" // 8
						+ (breakBox.isSelected() ? true : false)
						+ ":" // 9
						+ (randomBox.isSelected() ? true : false)
						+ ":" // 10
						+ (maxTimeBeBox.getValue().toString())
						+ ":" // 11
						+ (minTimeBeBox.getValue().toString())
						+ ":" // 12
						+ (maxBreakBox.getValue().toString())
						+ ":" // 13
						+ (minBreakBox.getValue().toString())
						+ ":" // 14
						+ (levelBox.getValue().toString())); //15
				out.close();
			} catch (final Exception e1) {
				log.warning("Error saving setting.");
			}
			// End write settings

            AaimistersGUI.dispose();
        }
		
		private void initComponents() {
			AaimistersGUI = new JFrame();
			contentPane = new JPanel();
			buryBox = new JCheckBox();
			locationBox = new JComboBox();
			statBox = new JComboBox();
			colorBox = new JComboBox();
			antibanBox = new JCheckBox();
			waitBox = new JCheckBox();
			featherBox = new JCheckBox();
			arrowBox = new JCheckBox();
			paintBox = new JCheckBox();
			breakBox = new JCheckBox();
			randomBox = new JCheckBox();
			maxTimeBeBox = new JSpinner();
			minTimeBeBox = new JSpinner();
			maxBreakBox = new JSpinner();
			minBreakBox = new JSpinner();
			levelBox = new JSpinner();
			submit = new JButton();
			
			// Listeners
	        AaimistersGUI.addWindowListener(new WindowAdapter() {
	            public void windowClosing(WindowEvent e) {
	                closed = true;
	            }
	        });
			
	        AaimistersGUI.setTitle("Aaimister's Chicken Killer");
	        AaimistersGUI.setResizable(false);
	        AaimistersGUI.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	        AaimistersGUI.setBounds(100, 100, 330, 485);
			contentPane = new JPanel();
			contentPane.setBackground(Color.BLACK);
			contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
			AaimistersGUI.setContentPane(contentPane);
			
			submit.setText("Start");
			submit.setForeground(SystemColor.textHighlight);
			submit.setBackground(Color.BLACK);
			submit.setFont(new Font("Segoe Print", Font.PLAIN, 16));
			submit.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					submitActionPerformed(e);
				}
			});
			
			JLabel lblAaimistersChickenKiller = new JLabel("Aaimister's Chicken Killer");
			lblAaimistersChickenKiller.setForeground(SystemColor.textHighlight);
			lblAaimistersChickenKiller.setBackground(Color.BLACK);
			lblAaimistersChickenKiller.setHorizontalAlignment(SwingConstants.CENTER);
			lblAaimistersChickenKiller.setFont(new Font("Segoe Print", Font.PLAIN, 22));
			
			JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
			tabbedPane.setBackground(Color.BLACK);
			GroupLayout gl_contentPane = new GroupLayout(contentPane);
			gl_contentPane.setHorizontalGroup(
				gl_contentPane.createParallelGroup(Alignment.LEADING)
					.addComponent(lblAaimistersChickenKiller, GroupLayout.DEFAULT_SIZE, 314, Short.MAX_VALUE)
					.addComponent(tabbedPane, GroupLayout.DEFAULT_SIZE, 314, Short.MAX_VALUE)
					.addGroup(gl_contentPane.createSequentialGroup()
						.addGap(70)
						.addComponent(submit, GroupLayout.PREFERRED_SIZE, 166, GroupLayout.PREFERRED_SIZE)
						.addContainerGap(78, Short.MAX_VALUE))
			);
			gl_contentPane.setVerticalGroup(
				gl_contentPane.createParallelGroup(Alignment.LEADING)
					.addGroup(gl_contentPane.createSequentialGroup()
						.addComponent(lblAaimistersChickenKiller)
						.addGap(18)
						.addComponent(tabbedPane, GroupLayout.PREFERRED_SIZE, 352, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(submit, GroupLayout.PREFERRED_SIZE, 28, GroupLayout.PREFERRED_SIZE)
						.addGap(79))
			);
			
			JPanel panel = new JPanel();
			panel.setBackground(Color.BLACK);
			tabbedPane.addTab("General", null, panel, null);
			
			paintBox.setText("Anti - Aliasing");
			paintBox.setFont(new Font("Segoe Print", Font.PLAIN, 12));
			paintBox.setBackground(Color.BLACK);
			paintBox.setForeground(SystemColor.textHighlight);
			paintBox.setSelected(true);
			
			antibanBox.setText("Anti - Ban");
			antibanBox.setFont(new Font("Segoe Print", Font.PLAIN, 12));
			antibanBox.setSelected(true);
			antibanBox.setForeground(SystemColor.textHighlight);
			antibanBox.setBackground(Color.BLACK);
			
			waitBox.setText("Wait For Loot");
			waitBox.setFont(new Font("Segoe Print", Font.PLAIN, 12));
			waitBox.setForeground(SystemColor.textHighlight);
			waitBox.setBackground(Color.BLACK);
			waitBox.setSelected(true);
			
			buryBox.setText("Bury Bones");
			buryBox.setFont(new Font("Segoe Print", Font.PLAIN, 12));
			buryBox.setBackground(Color.BLACK);
			buryBox.setForeground(SystemColor.textHighlight);
			
			featherBox.setText("Loot Feathers");
			featherBox.setFont(new Font("Segoe Print", Font.PLAIN, 12));
			featherBox.setForeground(SystemColor.textHighlight);
			featherBox.setSelected(true);
			featherBox.setBackground(Color.BLACK);
			
			arrowBox.setText("Loot Arrows");
			arrowBox.setFont(new Font("Segoe Print", Font.PLAIN, 12));
			arrowBox.setForeground(SystemColor.textHighlight);
			arrowBox.setBackground(Color.BLACK);
			
			JLabel lblStat = new JLabel("Stat:");
			lblStat.setBackground(Color.BLACK);
			lblStat.setForeground(SystemColor.textHighlight);
			lblStat.setFont(new Font("Segoe Print", Font.PLAIN, 16));
			
			JLabel lblLocation = new JLabel("Location:");
			lblLocation.setForeground(SystemColor.textHighlight);
			lblLocation.setFont(new Font("Segoe Print", Font.PLAIN, 16));
			lblLocation.setBackground(Color.BLACK);
			
			JLabel lblPaintColor = new JLabel("Paint Color:");
			lblPaintColor.setBackground(Color.BLACK);
			lblPaintColor.setForeground(SystemColor.textHighlight);
			lblPaintColor.setFont(new Font("Segoe Print", Font.PLAIN, 16));
			
			JLabel lblStopAtLevel = new JLabel("Stop At Level:");
			lblStopAtLevel.setForeground(SystemColor.textHighlight);
			lblStopAtLevel.setFont(new Font("Segoe Print", Font.PLAIN, 16));
			lblStopAtLevel.setBackground(Color.BLACK);
			
			statBox.setModel(new DefaultComboBoxModel(statstring));
			statBox.setForeground(SystemColor.textHighlight);
			statBox.setBackground(Color.BLACK);
			statBox.setForeground(SystemColor.textHighlight);
			
			locationBox.setModel(new DefaultComboBoxModel(locationstring));
			locationBox.setForeground(SystemColor.textHighlight);
			locationBox.setBackground(Color.BLACK);
			locationBox.setForeground(SystemColor.textHighlight);
			
			colorBox.setModel(new DefaultComboBoxModel(colorstring));
			colorBox.setForeground(SystemColor.textHighlight);
			colorBox.setBackground(Color.BLACK);
			colorBox.setForeground(SystemColor.textHighlight);
			
			levelBox.setForeground(SystemColor.textHighlight);
			levelBox.setFont(new Font("Segoe Print", Font.PLAIN, 11));
			levelBox.setBackground(Color.BLACK);
			levelBox.setModel(new SpinnerNumberModel(99, 1, 99, 1));
			GroupLayout gl_panel = new GroupLayout(panel);
			gl_panel.setHorizontalGroup(
				gl_panel.createParallelGroup(Alignment.LEADING)
					.addGroup(gl_panel.createSequentialGroup()
						.addContainerGap()
						.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
							.addComponent(paintBox)
							.addComponent(waitBox)
							.addGroup(gl_panel.createSequentialGroup()
								.addGap(15)
								.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
									.addComponent(lblStat)
									.addComponent(lblLocation)
									.addComponent(lblPaintColor)
									.addComponent(lblStopAtLevel)))
							.addComponent(featherBox))
						.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
							.addGroup(gl_panel.createSequentialGroup()
								.addPreferredGap(ComponentPlacement.RELATED, 60, Short.MAX_VALUE)
								.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
									.addComponent(buryBox)
									.addComponent(antibanBox)
									.addComponent(arrowBox))
								.addGap(17))
							.addGroup(gl_panel.createSequentialGroup()
								.addGap(32)
								.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
									.addComponent(locationBox, GroupLayout.PREFERRED_SIZE, 109, GroupLayout.PREFERRED_SIZE)
									.addComponent(statBox, GroupLayout.PREFERRED_SIZE, 109, GroupLayout.PREFERRED_SIZE)
									.addComponent(colorBox, GroupLayout.PREFERRED_SIZE, 109, GroupLayout.PREFERRED_SIZE)
									.addComponent(levelBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
								.addGap(31))))
			);
			gl_panel.setVerticalGroup(
				gl_panel.createParallelGroup(Alignment.LEADING)
					.addGroup(gl_panel.createSequentialGroup()
						.addContainerGap()
						.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
							.addComponent(paintBox)
							.addComponent(antibanBox))
						.addGap(18)
						.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
							.addComponent(waitBox)
							.addComponent(buryBox))
						.addGap(18)
						.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
							.addComponent(arrowBox)
							.addComponent(featherBox))
						.addGap(18)
						.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
							.addGroup(gl_panel.createSequentialGroup()
								.addComponent(lblStat)
								.addPreferredGap(ComponentPlacement.UNRELATED)
								.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
									.addComponent(lblLocation)
									.addComponent(locationBox, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(ComponentPlacement.UNRELATED)
								.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
									.addComponent(lblPaintColor)
									.addComponent(colorBox, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(ComponentPlacement.UNRELATED)
								.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
									.addComponent(lblStopAtLevel)
									.addComponent(levelBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
							.addComponent(statBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addContainerGap(7, Short.MAX_VALUE))
			);
			panel.setLayout(gl_panel);
			
			JPanel panel_1 = new JPanel();
			panel_1.setBackground(Color.BLACK);
			tabbedPane.addTab("Breaks", null, panel_1, null);
			
			breakBox.setText("Custom Breaks");
			breakBox.setForeground(SystemColor.textHighlight);
			breakBox.setBackground(Color.BLACK);
			breakBox.setFont(new Font("Segoe Print", Font.PLAIN, 12));
			breakBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					breakBoxActionPerformed(e);
				}
			});
			
			randomBox.setText("Random Breaks");
			randomBox.setForeground(SystemColor.textHighlight);
			randomBox.setBackground(Color.BLACK);
			randomBox.setFont(new Font("Segoe Print", Font.PLAIN, 12));
			if (!doBreak) {
				randomBox.setEnabled(false);
			} else {
				randomBox.setEnabled(true);
			}
			randomBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					randomBoxActionPerformed(e);
				}
			});
			
			JLabel lblTime = new JLabel("Time Between Breaks");
			lblTime.setFont(new Font("Segoe Print", Font.PLAIN, 15));
			lblTime.setBackground(Color.BLACK);
			lblTime.setForeground(SystemColor.textHighlight);
			
			JLabel lblBreakLengths = new JLabel("Break Lengths");
			lblBreakLengths.setForeground(SystemColor.textHighlight);
			lblBreakLengths.setBackground(Color.BLACK);
			lblBreakLengths.setFont(new Font("Segoe Print", Font.PLAIN, 15));
			
			minTimeBeBox.setFont(new Font("Segoe Script", Font.PLAIN, 11));
			minTimeBeBox.setBackground(Color.BLACK);
			minTimeBeBox.setForeground(SystemColor.textHighlight);
			minTimeBeBox.setModel(new SpinnerNumberModel(new Integer(111), new Integer(0), null, new Integer(1)));
			
			JLabel lblMins = new JLabel("mins");
			lblMins.setFont(new Font("Segoe Print", Font.PLAIN, 11));
			lblMins.setBackground(Color.BLACK);
			lblMins.setForeground(SystemColor.textHighlight);
			
			JLabel lblTo = new JLabel("to");
			lblTo.setForeground(SystemColor.textHighlight);
			lblTo.setBackground(Color.BLACK);
			lblTo.setFont(new Font("Segoe Print", Font.PLAIN, 11));
			
			maxTimeBeBox.setFont(new Font("Segoe Script", Font.PLAIN, 11));
			maxTimeBeBox.setModel(new SpinnerNumberModel(new Integer(222), new Integer(0), null, new Integer(1)));
			maxTimeBeBox.setForeground(SystemColor.textHighlight);
			maxTimeBeBox.setBackground(Color.BLACK);
			
			JLabel label = new JLabel("mins");
			label.setForeground(SystemColor.textHighlight);
			label.setFont(new Font("Segoe Print", Font.PLAIN, 11));
			label.setBackground(Color.BLACK);
			
			JSeparator separator = new JSeparator();
			
			minBreakBox.setModel(new SpinnerNumberModel(new Integer(15), new Integer(0), null, new Integer(1)));
			minBreakBox.setForeground(SystemColor.textHighlight);
			minBreakBox.setFont(new Font("Segoe Script", Font.PLAIN, 11));
			minBreakBox.setBackground(Color.BLACK);
			
			JLabel label_1 = new JLabel("mins");
			label_1.setForeground(SystemColor.textHighlight);
			label_1.setFont(new Font("Segoe Print", Font.PLAIN, 11));
			label_1.setBackground(Color.BLACK);
			
			JLabel label_2 = new JLabel("to");
			label_2.setForeground(SystemColor.textHighlight);
			label_2.setFont(new Font("Segoe Print", Font.PLAIN, 11));
			label_2.setBackground(Color.BLACK);
			
			JLabel label_3 = new JLabel("mins");
			label_3.setForeground(SystemColor.textHighlight);
			label_3.setFont(new Font("Segoe Print", Font.PLAIN, 11));
			label_3.setBackground(Color.BLACK);
			
			maxBreakBox.setModel(new SpinnerNumberModel(new Integer(65), new Integer(0), null, new Integer(1)));
			maxBreakBox.setForeground(SystemColor.textHighlight);
			maxBreakBox.setFont(new Font("Segoe Script", Font.PLAIN, 11));
			maxBreakBox.setBackground(Color.BLACK);
			GroupLayout gl_panel_1 = new GroupLayout(panel_1);
			gl_panel_1.setHorizontalGroup(
				gl_panel_1.createParallelGroup(Alignment.LEADING)
					.addGroup(gl_panel_1.createSequentialGroup()
						.addContainerGap()
						.addGroup(gl_panel_1.createParallelGroup(Alignment.LEADING)
							.addComponent(separator, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 285, Short.MAX_VALUE)
							.addGroup(gl_panel_1.createSequentialGroup()
								.addComponent(breakBox)
								.addPreferredGap(ComponentPlacement.RELATED, 49, Short.MAX_VALUE)
								.addComponent(randomBox))
							.addGroup(gl_panel_1.createSequentialGroup()
								.addGroup(gl_panel_1.createParallelGroup(Alignment.LEADING)
									.addGroup(gl_panel_1.createSequentialGroup()
										.addComponent(lblTime)
										.addPreferredGap(ComponentPlacement.RELATED, 33, Short.MAX_VALUE))
									.addGroup(gl_panel_1.createSequentialGroup()
										.addComponent(minTimeBeBox, GroupLayout.PREFERRED_SIZE, 56, GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(lblMins)
										.addPreferredGap(ComponentPlacement.RELATED, 51, Short.MAX_VALUE)
										.addComponent(lblTo)
										.addGap(49)))
								.addComponent(maxTimeBeBox, GroupLayout.PREFERRED_SIZE, 56, GroupLayout.PREFERRED_SIZE)
								.addGap(5)
								.addComponent(label, GroupLayout.PREFERRED_SIZE, 26, GroupLayout.PREFERRED_SIZE))
							.addComponent(lblBreakLengths)
							.addGroup(gl_panel_1.createSequentialGroup()
								.addComponent(minBreakBox, GroupLayout.PREFERRED_SIZE, 56, GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(label_1, GroupLayout.PREFERRED_SIZE, 26, GroupLayout.PREFERRED_SIZE)
								.addGap(52)
								.addComponent(label_2, GroupLayout.PREFERRED_SIZE, 11, GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(ComponentPlacement.RELATED, 48, Short.MAX_VALUE)
								.addComponent(maxBreakBox, GroupLayout.PREFERRED_SIZE, 56, GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(label_3, GroupLayout.PREFERRED_SIZE, 26, GroupLayout.PREFERRED_SIZE)))
						.addContainerGap())
			);
			gl_panel_1.setVerticalGroup(
				gl_panel_1.createParallelGroup(Alignment.LEADING)
					.addGroup(gl_panel_1.createSequentialGroup()
						.addContainerGap()
						.addGroup(gl_panel_1.createParallelGroup(Alignment.BASELINE)
							.addComponent(breakBox)
							.addComponent(randomBox))
						.addGap(18)
						.addComponent(lblTime)
						.addGap(18)
						.addGroup(gl_panel_1.createParallelGroup(Alignment.LEADING)
							.addGroup(gl_panel_1.createParallelGroup(Alignment.BASELINE)
								.addComponent(minTimeBeBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(lblMins))
							.addGroup(gl_panel_1.createParallelGroup(Alignment.BASELINE)
								.addComponent(maxTimeBeBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(lblTo))
							.addComponent(label, GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(ComponentPlacement.RELATED, 35, Short.MAX_VALUE)
						.addComponent(separator, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGap(28)
						.addComponent(lblBreakLengths)
						.addGap(18)
						.addGroup(gl_panel_1.createParallelGroup(Alignment.BASELINE)
							.addComponent(minBreakBox, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)
							.addComponent(label_1, GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE)
							.addComponent(label_2, GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE)
							.addComponent(label_3, GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE)
							.addComponent(maxBreakBox, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE))
						.addGap(61))
			);
			panel_1.setLayout(gl_panel_1);
			contentPane.setLayout(gl_contentPane);
			// LOAD SAVED SELECTION INFO
			try {
				String filename = getCacheDirectory() + "\\AaimistersCKillerSettings.txt";
		        Scanner in = new Scanner(new BufferedReader(new FileReader(filename)));
		        String line;
		        String[] opts = {};
		        while (in.hasNext()) {
		        	line = in.next();
		        	if (line.contains(":")) {
		        		opts = line.split(":");
		        	}
		        }
		        in.close();
		        if (opts.length > 1) {
		        	 if (opts[0].equals("true")) {
				        	buryBox.setSelected(true);
				        } else {
				        	buryBox.setSelected(false);
				        }
		        	 if (opts[9].equals("true")) {
		        		 randomBox.setEnabled(true);
		        		 if (opts[10].equals("false")) {
		        			 maxTimeBeBox.setValue(Integer.parseInt(opts[11]));
					         minTimeBeBox.setValue(Integer.parseInt(opts[12]));
					         maxBreakBox.setValue(Integer.parseInt(opts[13]));
					         minBreakBox.setValue(Integer.parseInt(opts[14]));
					         maxTimeBeBox.setEnabled(true);
					         minTimeBeBox.setEnabled(true);
					         maxBreakBox.setEnabled(true);
					         minBreakBox.setEnabled(true);
		        		 }
		        	 }
		        	levelBox.setValue(Integer.parseInt(opts[15]));
		        	locationBox.setSelectedIndex(Integer.parseInt(opts[1]));
			        statBox.setSelectedIndex(Integer.parseInt(opts[2]));
			        colorBox.setSelectedIndex(Integer.parseInt(opts[3]));
			        if (opts[4].equals("true")) {
			            antibanBox.setSelected(true);
			        } else {
			            antibanBox.setSelected(false);
			        }
			        if (opts[5].equals("true")) {
			            waitBox.setSelected(true);
			        } else {
			            waitBox.setSelected(false);
			        }
			        if (opts[6].equals("true")) {
			            featherBox.setSelected(true);
			        } else {
			            featherBox.setSelected(false);
			        }
			        if (opts[7].equals("true")) {
			            arrowBox.setSelected(true);
			        } else {
			            arrowBox.setSelected(false);
			        }
			        if (opts[8].equals("true")) {
			            paintBox.setSelected(true);
			        } else {
			            paintBox.setSelected(false);
			        }
			        if (opts[9].equals("true")) {
			            breakBox.setSelected(true);
			        } else {
			            breakBox.setSelected(false);
			            randomBox.setEnabled(false);
			        }
			        if (opts[10].equals("true")) {
			            randomBox.setSelected(true);
			            randomBox.setEnabled(true);
			        } else {
			            randomBox.setSelected(false);
			        }
		        }
			} catch (final Exception e2) {
				//e2.printStackTrace();
				log.warning("Error loading settings.  If this is first time running script, ignore.");
			}
		      // END LOAD SAVED SELECTION INFO
		}
		
		private JFrame AaimistersGUI;
		private JPanel contentPane;
		private JCheckBox buryBox;
		private JComboBox locationBox;
		private JComboBox statBox;
		private JComboBox colorBox;
		private JCheckBox antibanBox;
		private JCheckBox waitBox;
		private JCheckBox featherBox;
		private JCheckBox arrowBox;
		private JCheckBox paintBox;
		private JCheckBox breakBox;
		private JCheckBox randomBox;
		private JSpinner maxTimeBeBox;
		private JSpinner minTimeBeBox;
		private JSpinner maxBreakBox;
		private JSpinner minBreakBox;
		private JSpinner levelBox;
		private JButton submit;
	}
	public class Updater {
		private Updater() {
			initComponents();
		}
		
		private void threadActionPerformed(ActionEvent e) {
			openThread();
			Updater.dispose();
 			stop = true;
 		}
		
		private void noActionPerformed(ActionEvent e) {
			Updater.dispose();
 			stop = true;
 		}
		
		private void initComponents() {
			Updater = new JFrame();
			contentPane = new JPanel();
			thread = new JButton();
			no = new JButton();
			
			// Listeners
	        Updater.addWindowListener(new WindowAdapter() {
	            public void windowClosing(WindowEvent e) {
	                closed = true;
	            }
	        });
			
	        Updater.setTitle("Aaimister's Updater");
	        Updater.setResizable(false);
	        Updater.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	        Updater.setBounds(100, 100, 420, 123);
			contentPane = new JPanel();
			contentPane.setBackground(new Color(0, 0, 0));
			contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
			Updater.setContentPane(contentPane);
			
			thread.setText("Visit Thread");
			thread.setFont(new Font("Rod", Font.PLAIN, 12));
			thread.setForeground(new Color(255, 255, 0));
			thread.setBackground(new Color(0, 0, 0));
			thread.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					threadActionPerformed(e);
				}
			});
			
			JLabel lblUpdateAvail = new JLabel("Update Available!  Please Visit The Thread!");
			lblUpdateAvail.setFont(new Font("Rod", Font.PLAIN, 15));
			lblUpdateAvail.setHorizontalAlignment(SwingConstants.CENTER);
			lblUpdateAvail.setForeground(Color.YELLOW);
			
			no.setText("No Thanks");
			no.setForeground(Color.YELLOW);
			no.setFont(new Font("Rod", Font.PLAIN, 12));
			no.setBackground(Color.BLACK);
			no.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					noActionPerformed(e);
				}
			});
			GroupLayout gl_contentPane = new GroupLayout(contentPane);
			gl_contentPane.setHorizontalGroup(
				gl_contentPane.createParallelGroup(Alignment.TRAILING)
					.addGroup(gl_contentPane.createSequentialGroup()
						.addGap(22)
						.addComponent(thread, GroupLayout.PREFERRED_SIZE, 147, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED, 67, Short.MAX_VALUE)
						.addComponent(no, GroupLayout.PREFERRED_SIZE, 147, GroupLayout.PREFERRED_SIZE)
						.addGap(32))
					.addGroup(Alignment.LEADING, gl_contentPane.createSequentialGroup()
						.addGap(5)
						.addComponent(lblUpdateAvail, GroupLayout.DEFAULT_SIZE, 399, Short.MAX_VALUE))
			);
			gl_contentPane.setVerticalGroup(
				gl_contentPane.createParallelGroup(Alignment.LEADING)
					.addGroup(Alignment.TRAILING, gl_contentPane.createSequentialGroup()
						.addContainerGap()
						.addComponent(lblUpdateAvail)
						.addPreferredGap(ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
						.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
							.addComponent(thread, GroupLayout.PREFERRED_SIZE, 28, GroupLayout.PREFERRED_SIZE)
							.addComponent(no, GroupLayout.PREFERRED_SIZE, 28, GroupLayout.PREFERRED_SIZE))
						.addContainerGap())
			);
			contentPane.setLayout(gl_contentPane);
		}
		private JFrame Updater;
		private JPanel contentPane;
		private JButton thread;
		private JButton no;
	}
}