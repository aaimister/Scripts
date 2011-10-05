/**
 * @author Aaimister
 * @version 1.44 ©2010-2011 Aaimister, No one except Aaimister has the right to
 *          modify and/or spread this script without the permission of Aaimister.
 *          I'm not held responsible for any damage that may occur to your
 *          property.
 */

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
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
import org.rsbot.script.wrappers.*;

@ScriptManifest(authors = { "Aaimister" }, website = "http://fc4ea3b7.any.gs", name = "Aaimisters Essence Miner v1.44", keywords = "Mining", version = 1.44, description = ("Mines Essence."))
public class AaimistersEssenceMiner extends Script implements PaintListener, MessageListener, MouseListener {

	private static interface AM {

		// Varrock
		//final RSTile toBankV[] = { new RSTile(3258, 3405), new RSTile(3260, 3413), new RSTile(3253, 3420) };
		final RSTile toMineV = new RSTile(3253, 3401);
		final RSArea VCityArea = new RSArea(new RSTile(3234, 3387), new RSTile(3268, 3440));
		final RSArea AtAubury = new RSArea(new RSTile(3250, 3399), new RSTile(3255, 3404));
		final RSArea BankV = new RSArea(new RSTile(3250, 3419), new RSTile(3257, 3423));
		final RSArea GateV = new RSArea(new RSTile(3252, 3398), new RSTile(3254, 3400));
		final RSTile AuburyT = new RSTile(3253, 3401);
		final RSTile BankTV = new RSTile(3253, 3420);
		final RSTile VGateT = new RSTile(3253, 3398);

		// Yanille
		//final RSTile toBankY[] = { new RSTile(3602, 3093), new RSTile(2611, 3092) };
		final RSTile toMineY = new RSTile(2598, 3086);
		final RSArea YCityArea = new RSArea(new RSTile(2583, 3076), new RSTile(2620, 3106));
		final RSArea AtWiz = new RSArea(new RSTile(2585, 3081), new RSTile(2596, 3094));
		final RSArea BankY = new RSArea(new RSTile(2609, 3088), new RSTile(2616, 3097));
		final RSArea GateY = new RSArea(new RSTile(2595, 3086), new RSTile(2598, 3089));
		final RSTile WizT = new RSTile(2595, 3087);
		final RSTile YGateT = new RSTile(2597, 3087);
		final RSTile BankTY = new RSTile (2611, 3093);
	}

	private RSArea Bank;
	private RSArea Gate;
	private RSArea AtPerson;
	private RSArea CityArea;
	private RSTile toMine;
	//private RSTile toBank[];
	private RSTile PersonT;
	private RSTile BankT;
	private RSTile GateT;

	private final String[] colorstring = { "Black", "Blue", "Brown", "Cyan", "Green", "Lime", "Orange", "Pink", "Purple", "Red", "White", "Yellow" };
	private final String[] locationstring = { "Varrock", "Yanille" };

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

	Updater u = new Updater();
	AaimistersGUI g = new AaimistersGUI();
	public final File settingsFile = new File(getCacheDirectory(), "AaimistersEMinerSettings.txt");

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

	private String currentOre = "";
	private String status = "";
	private String url = "http://fc4ea3b7.any.gs";

	boolean currentlyBreaking = false;
	boolean clickedPortal;
	boolean randomBreaks;
	boolean clickedPer;
	boolean bankedOpen;
	boolean antiBanOn;
	boolean notChosen;
	boolean useBooth;
	boolean showPaint = true;
	boolean painting;
	boolean resting;
	boolean checked;
	boolean doBreak;
	boolean opened;
	boolean mining;
	boolean check = true;
	boolean rest;
	boolean logTime;
	boolean noClick;
	boolean stop;
	//Paint Buttons
	boolean xButton;
	boolean Stat;
	boolean Main = true;

	boolean varrock;
	boolean closed;

	int bakers[] = { 2759, 553 };
	int booths = 782;
	int pickaxes[] = { 1265, 1267, 1269, 1273, 1271, 1275, 15259 };
	int markerPlant = 9157;
	int teleport[] = { 13630, 13631, 13629, 13628 };
	int essence = 2491;
	int Aubury = 5913;
	int Wizard = 462;
	int priceEssence;
	int aubCount = 0;
	int obs[] = { 493, 512, 494, 469, 497, 467, 455, 443, 44497, 2491 };
	int dotCount;
	int errorCount;
	int xpEss = 5;
	int currentXP;
	int gainedLvl;
	int timeToLvl;
	int idle = 0;
	int startEXP;
	int essToLvl;
	int xpGained;
	int totalEss;
	int xpToLvl;
	int essHour;
	int GPtotal;
	int GPHour;
	int banker;
	int random;
	int xpHour;
	int bankID;
	int door;
	int ban;
	int iness;
	int maxBetween;
	int minBetween;
	int maxLength;
	int minLength;


	private enum State { TOBANK, MINE, TOMINE, PORTAL, TELE, BANK, ERROR };

	private State getState() {
		if (inventory.isFull()) {
			mining = false;
			if (Bank.contains(getLocation())) {
				return State.BANK;
			} else if (objects.getNearest(obs) != null) {
				return State.PORTAL;
			} else {
				return State.TOBANK;
			}
		} else {
			if (AtPerson.contains(getLocation())) {
				return State.TELE;
			} else if (objects.getNearest(obs) != null) {
				RSObject ess = objects.getNearest(essenceID());
				try {
					if (portal().isOnScreen() && !calc.canReach(new RSTile(ess.getLocation().getX(), ess.getLocation().getY() - 3), true)) {
						log("Getting Unstuck.");
						return State.PORTAL;
					}
				} catch (Exception e) {

				}
				return State.MINE;
			} else if (good()) {
				return State.TOMINE;
			} else {
				return State.ERROR;
			}
		}
	}

	public double getVersion() { 
		return 1.44;
	}

	public boolean onStart() {
		status = "Starting up";

		//log("Dwarfeh showing Aaimister some love <3");

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

		startEXP = skills.getCurrentExp(14);
		currentXP = skills.getExpToNextLevel(14);
		if (doBreak) {
			if (AccountManager.isTakingBreaks(account.getName())) {
				log.severe("Turn Off Bot Breaks!");
				log.severe("Turning off custom breaker...");
				doBreak = false;
			} else {
				breakingNew();
			}
		}

		if (!isMember()) {
			currentOre = "Essence";
			iness = 1436;
			priceEssence = getGuidePrice(iness);
		} else if (isMember()) {
			currentOre = "Pure Essence";
			iness = 7936;
			priceEssence = getGuidePrice(iness);
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
			BufferedReader r = new BufferedReader(new InputStreamReader(new URL("http://aaimister.webs.com/scripts/AaimistersEssMinerVersion.txt").openStream()));
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
		log("Thanks for using Aaimister's Essence Miner!");
		log("In " + formattedTime + " You mined " + formatter.format(totalEss) + " Ore(s) and Gained $" + formatter.format(GPtotal) + "!");
		log("You Gained: " + formatter.format(gainedLvl) + " level(s) in Mining!");
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

	private boolean good() {
		if (!game.isLoggedIn() || game.isLoading() || game.isLoginScreen() || game.isWelcomeScreen()) {
			return false;
		}
		return true;
	}
	
	private boolean breakingCheck(){
		if (nextBreak <= System.currentTimeMillis()){
			return true;
		}
		return false;
	}

	private boolean walkTo(RSTile tile) {
		RSPath walkPath = walking.getPath(tile.randomize(1, 1));
		try {
			if (walkPath != null) {
				if (!getMyPlayer().isMoving() || calc.distanceTo(walking.getDestination()) < 4) {
					return walkPath.traverse();
				}
			}
		} catch (Exception e) {

		}
		return false;
	}

	private void waitForArea(RSArea area) {
		long start = System.currentTimeMillis();
		idle = 0;
		while (!area.contains(getLocation())) {
			if (idle > 60) {
				idle = 0;
				break;
			}
			if (System.currentTimeMillis() >= start + 18000) {
				break;
			}
			if (objects.getNearest(obs) != null) {
				idle++;
			}
			sleep(50);
		}
		clickedPortal = false;
		idle = 0;
	}

	private void waitForObj() {
		long start = System.currentTimeMillis();
		idle = 0;
		while (objects.getNearest(obs) == null) {
			if (idle > 60) {
				idle = 0;
				break;
			}
			if (System.currentTimeMillis() >= start + 18000) {
				break;
			}
			if (AtPerson.contains(getLocation())) {
				idle++;
			}
			sleep(50);
		}
		clickedPer = false;
		idle = 0;
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

	private String Location() {
		if (AtPerson.contains(getLocation())) {
			if (varrock) {
				return "Aubury's";
			} else {
				return "Wizard's";
			}
		} else if (Bank.contains(getLocation())) {
			return "Bank";
		} else if (objects.getNearest(obs) != null) {
			return "Mine";
		} else if (calc.distanceTo(BankT) > 100 && objects.getNearest(obs) == null) {
			if (!game.isLoggedIn()) {
				return "Login Screen";
			} else {
				return "Unknown";
			}
		} else {
			if (varrock) {
				return "Varrock";
			} else {
				return "Yanille";
			}
		}
	}

	private RSTile getLocation() {
		return getMyPlayer().getLocation();
	}

	private void setCamera() {
		if (camera.getPitch() < 10) {
			camera.setPitch(true);
			sleep(1000, 1600);
		}
	}

	private int essenceID() {
		RSObject e[] = objects.getAll();
		for (int i = 0; i < e.length; i++) {
			if (e[i].getName().contains("Essence")) {
				return e[i].getID();
			}
		}
		return 0;
	}

	private int bankerID() {
		RSNPC b[] = npcs.getAll();
		for (int i = 0; i < b.length; i++) {
			if (b[i].getName().contains("Banker")) {
				return b[i].getID();
			}
		}
		return 0;
	}

	private int portalID() {
		RSNPC p[] = npcs.getAll();
		//RSObject p[] = objects.getAll();
		for (int i = 0; i < p.length; i++) {
			if (p[i].getName().contains("Portal")) {
				return p[i].getID();
			}
		}
		return 0;
	}

	private void doRest() {
		if (walking.getEnergy() < random(10, 30) && (calc.distanceTo(PersonT) >= 7) && objects.getNearest(obs) == null) {
			if (!resting && !mining && !bank.isOpen()) {
				status = "Resting";
				interfaces.getComponent(750, 6).interact("Rest");
				mouse.moveSlightly();
				resting = true;
				sleep(1500, 2000);
				return;
			}
		}
		if (resting) {
			if (getMyPlayer().getAnimation() == -1) {
				resting = false;
			}
			if (walking.getEnergy() > random(93, 100)) {
				resting = false;
			}
			if (antiBanTime <= System.currentTimeMillis()) {
				check = false;
				doAntiBan();
			}
		}
	}

	private void setRun() {
		if (!walking.isRunEnabled()) {
			if (walking.getEnergy() >= random(45, 100)) {
				walking.setRun(true);
				sleep(1000, 1600);
			}
		} else {
			if (rest) {
				if ((calc.distanceTo(PersonT) >= 7) || objects.getNearest(obs) == null) {
					doRest();
				}
			}
		}
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

		if (!good()) {
			status = "Logging In / Breaking";
			return 3000;
		}
		
		if (startTime == 0 && skills.getCurrentLevel(14) != 0) {
			startTime = System.currentTimeMillis();
			startEXP = skills.getCurrentExp(14);
			currentXP = skills.getExpToNextLevel(14);
		}

		if (logTime) {
			log("Next Break In: " + formatTime((int) nextBreakT) + " For: " + formatTime((int) nextLength) + ".");
			logTime = false;
		}

		mouse.setSpeed(random(4, 9));
		setCamera();
		setRun();

		if (resting) {
			status = "Resting";
			random = random(0, 7);
			if (antiBanTime <= System.currentTimeMillis()) {
				check = false;
				doAntiBan();
			}
			if (getMyPlayer().getAnimation() == -1 && !mining) {
				doRest();
				sleep(200, 800);
			}
			return random(250, 450);
		}

		switch (getState()) {
		case TOBANK:
			clickedPortal = false;
			status = "Walking to bank";
			try {
				if (!Bank.contains(getLocation())) {
					if (AtPerson.contains(getLocation())) {
						if (checkDoor() || (calc.distanceTo(GateT) > 3 && !varrock)) {
							openDoor();
							return random(300, 500);
						} else {
							if (walkTo(BankT.randomize(2, 2))) {

							} else {
								if (!getMyPlayer().isMoving() || calc.distanceTo(walking.getDestination()) <= 4) {
									walking.walkTileMM(walking.getClosestTileOnMap(BankT.randomize(2, 2)));
								}
							}
							//walking.newTilePath(toBank).randomize(2, 2).traverse();
							return random(300, 600);
						}
					} else {
						if (walkTo(BankT.randomize(2, 2))) {

						} else {
							if (!getMyPlayer().isMoving() || calc.distanceTo(walking.getDestination()) <= 4) {
								walking.walkTileMM(walking.getClosestTileOnMap(BankT.randomize(2, 2)));
							}
						}
						//walking.newTilePath(toBank).randomize(2, 2).traverse();
						return random(300, 600);
					}
				}
			} catch (Exception e) {
				idle++;
			}

			break;
		case MINE:
			clickedPer = false;
			if (idle > 10) {
				mining = false;
				idle = 0;
			}
			try {
				if (objects.getNearest(obs) != null) {
					RSObject ess = objects.getNearest(essenceID());
					if (ess != null) {
						RSTile loc = ess.getArea().getNearestTile(getLocation());
						if (!ess.isOnScreen()) {
							idle++;
							if (calc.distanceTo(loc) > 3 && !mining) {
								status = "Walking to essence";
								if (!getMyPlayer().isMoving() || calc.distanceTo(walking.getDestination()) < 4) {
									walking.walkTileMM(walking.getClosestTileOnMap(loc.randomize(2, 2)));
									return random(150, 300);
								}
							} else if (!mining) {
								camera.turnTo(ess);
								return random(300, 500);
							}
						} else {
							idle++;
							if (calc.distanceTo(loc) > 3) {
								if (!getMyPlayer().isMoving() || calc.distanceTo(walking.getDestination()) < 4) {
									walking.walkTileMM(walking.getClosestTileOnMap(loc.randomize(2, 2)));
									return random(150, 300);
								}
							} else if (!mining) {
								status = "Mining";
								ess.interact("Mine");
								mining = true;
								idle = 0;
								return random(2000, 2700);
							}
						}
					}
				}
				if (getMyPlayer().getAnimation() != -1) {
					idle = 0;
				}
			} catch (Exception e) {
				idle++;
			}

			break;
		case TOMINE:
			bankedOpen = false;
			mining = false;
			notChosen = true;
			opened = false;
			if (varrock) {
				status = "Walking to Aubury";
			} else {
				status = "Walking to Wizard";
			}
			try {
				if (!AtPerson.contains(getLocation())) {
					if (calc.distanceTo(PersonT) <= 6) {
						if (checkDoor()) {
							RSObject closed = objects.getNearest(door);
							if (calc.distanceTo(closed.getLocation()) > 3) {
								walking.walkTileMM(walking.getClosestTileOnMap(closed.getLocation().randomize(1, 1)));
								return random(1500, 1800);
							} else {
								openDoor();
							}
							return random(300, 500);
						} else {
							//walkTo(PersonT.randomize(2, 2));
							//walking.newTilePath(toMine).randomize(2, 2).traverse();
							if (walkTo(toMine)) {

							} else {
								if (!getMyPlayer().isMoving() || calc.distanceTo(walking.getDestination()) <= 4) {
									walking.walkTileMM(walking.getClosestTileOnMap(toMine));
								}
							}
							return random(300, 600);
						}
					} else {
						//walkTo(PersonT.randomize(2, 2));
						//walking.newTilePath(toMine).randomize(2, 2).traverse();
						if (walkTo(toMine)) {

						} else {
							if (!getMyPlayer().isMoving() || calc.distanceTo(walking.getDestination()) <= 4) {
								walking.walkTileMM(walking.getClosestTileOnMap(toMine));
							}
						}
						return random(300, 600);
					}
				}
			} catch (Exception e) {
				idle++;
			}

			break;
		case TELE:
			try {
				if (idle > 15) {
					if (objects.getNearest(obs) == null) {
						RSNPC per = perNPC();
						if (interfaces.getComponent(620, 18).isValid()) {
							close();
						}
						if (per != null) {
							per.interact("Teleport");
							sleep(2000, 3000);
						} else {
							RSNPC plant = plantNPC();
							plant.interact("Teleport");
							sleep(2000, 3000);
						}
					} else {
						return 10;
					}
					idle = 0;
				}
				if (interfaces.getComponent(620, 18).isValid()) {
					close();
					sleep(300);
					clickedPer = false;
					return 350;
				}
				if (!clickedPer) {
					RSNPC per = perNPC();
					if (per != null) {
						idle++;
						if (!clickedPer) {
							per.interact("Teleport");
							clickedPer = true;
						} else {
							waitForObj();
						}
					} else {
						RSNPC plant = plantNPC();
						idle++;
						if (!clickedPer) {
							plant.interact("Teleport");
							clickedPer = true;
						} else {
							waitForObj();
						}
					}
				} else {
					idle++;
					waitForObj();
				}
			} catch (Exception e) {
				idle++;
			}

			break;
		case PORTAL:
			status = "Walking to bank";
			if (idle > 7) {
				clickedPortal = false;
				idle = 0;
			}
			try {
				if (!clickedPortal) {
					if (portal() != null) {
						RSObject p = objects.getNearest(39831);
						RSTile loc = p.getArea().getNearestTile(getLocation());
						if (!portal().isOnScreen()) {
							if (calc.distanceTo(loc) > 3) {
								walking.walkTileMM(walking.getClosestTileOnMap(loc.randomize(2, 2)));
								return random(1200, 1500);
							} else {
								idle++;
								if (calc.distanceTo(loc) > 3) {
									if (!getMyPlayer().isMoving() || calc.distanceTo(walking.getDestination()) < 4) {
										walking.walkTileMM(walking.getClosestTileOnMap(loc.randomize(2, 2)));
										return random(150, 300);
									}
								} else if (!clickedPortal) {
									portal().interact("Enter");
									clickedPortal = true;
									idle = 0;
									return random(200, 500);
								}
							}
						} else {
							idle++;
							if (calc.distanceTo(loc) > 2) {
								if (!getMyPlayer().isMoving() || calc.distanceTo(walking.getDestination()) < 4) {
									walking.walkTileMM(walking.getClosestTileOnMap(loc.randomize(2, 2)));
									return random(150, 300);
								}
							}
							if (!clickedPortal) {
								portal().interact("Enter");
								clickedPortal = true;
								idle = 0;
								waitForArea(CityArea);
							}
						}
					}
				} else {
					idle++;
					waitForArea(CityArea);
				}
			} catch (Exception e) {
				idle++;
			}

			break;
		case BANK:
			status = "Banking";
			if (idle > 7) {
				opened = false;
				notChosen = true;
				bankedOpen = false;
				idle = 0;
			}
			if (notChosen) {
				if (random(0, 5) == 0 || random(0, 5) == 2) {
					useBooth = false;
				} else {
					useBooth = true;
				}
				notChosen = false;
			}
			RSObject booth = objects.getNearest(bankID);
			RSNPC bankP = banker();
			try {
				if (Bank.contains(getLocation())) {
					if (inventory.isFull()) {
						if (!bank.isOpen()) {
							if (!booth.isOnScreen()) {
								if (calc.distanceTo(booth.getLocation()) >= 4) {
									if (!getMyPlayer().isMoving() || calc.distanceTo(walking.getDestination()) < 4) {
										walking.walkTileMM(walking.getClosestTileOnMap(booth.getLocation()));
									}
								} else {
									camera.turnTo(booth);
								}
								return random(300, 600);
							} else {
								idle++;
								if (!opened) {
									if (useBooth) {
										booth.interact("Use-quickly");
									} else {
										bankP.interact("Bank Banker");
									}
									opened = true;
									idle = 0;
									return random(500, 1000);
								}
							}
						} else {
							opened = false;
							if (inventory.containsOneOf(pickaxes)) {
								idle++;
								if (!bankedOpen) {
									bank.depositAllExcept(pickaxes);
									bankedOpen = true;
									return random(100, 150);
								}
							} else {
								idle++;
								if (!bankedOpen) {
									bank.depositAll();
									bankedOpen = true;
									return random(100, 150);
								}
							}
						}
					}
				}
			} catch (Exception e) {
				idle++;
			}

			break;
		case ERROR:

			break;
		}
		return random(300, 600);
	}

	private void close() {
		RSComponent close = interfaces.getComponent(620, 18);
		close.interact("Close");
		sleep(100, 300);
	}

	public void openDoor() {
		RSObject closed = objects.getNearest(door);
		RSTile doorT = closed.getLocation();
		if (Gate.contains(doorT) && calc.distanceTo(GateT) < 3) {
			if (closed != null) {
				if (!closed.isOnScreen()) {
					walking.walkTileMM(doorT);
					sleep(1200, 1500);
				} else {
					closed.interact("Open");
					sleep(1000, 1200);
				}
			}
		} else {
			if (!getMyPlayer().isMoving() || calc.distanceTo(walking.getDestination()) < 4) {
				walking.walkTileMM(walking.getClosestTileOnMap(GateT.randomize(2, 2)));
			}
		}
	}

	public boolean checkDoor() {
		try {
			RSObject closed = objects.getNearest(door);
			if (Gate.contains(closed.getLocation())) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}

	public boolean isMember() {
		return AccountManager.isMember(account.getName());
	}

	public void doAntiBan() {

		if (!antiBanOn) {
			return;
		}

		antiBanRandom = random(15000, 90000);
		antiBanTime = System.currentTimeMillis() + antiBanRandom;

		int action = random(0, 4);

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
			mouse.moveOffScreen();
			sleep(200, 400);
			break;
		case 3:
			checkXP();
			sleep(200, 400);
			break;
		case 4:
			checkPlayer();
			sleep(200, 400);
			break;
		}
	}

	public void checkPlayer() {
		if (!check) {
			return;
		}
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
	public void checkXP() {
		if (!check) {
			return;
		}
		if (game.getCurrentTab() != 2) {
			game.openTab(2);
			sleep(500, 900);
		}
		mouse.move(random(678, 729), random(214, 233));
		sleep(2800, 5500);
		game.openTab(4);
		sleep(50, 100);
		mouse.moveRandomly(50, 900);
	}

	public void rotateCamera() {
		if (!antiBanOn) {
			return;
		}
		final char[] LR = new char[] { KeyEvent.VK_LEFT,
				KeyEvent.VK_RIGHT };
		final char[] UD = new char[] { KeyEvent.VK_DOWN,
				KeyEvent.VK_UP };
		final char[] LRUD = new char[] { KeyEvent.VK_LEFT,
				KeyEvent.VK_RIGHT, KeyEvent.VK_UP,
				KeyEvent.VK_UP };
		final int randomLR = random(0, 2);
		final int randomUD = random(0, 2);
		final int randomAll = random(0, 4);
		if (random(0, 3) == 0) {
			keyboard.pressKey(LR[randomLR]);
			sleepCR(random(2,9));
			keyboard.pressKey(UD[randomUD]);
			sleepCR(random(6,10));
			keyboard.releaseKey(UD[randomUD]);
			sleepCR(random(2,7));
			keyboard.releaseKey(LR[randomLR]);
		} else {
			keyboard.pressKey(LRUD[randomAll]);
			if (randomAll > 1) {
				sleepCR(random(6,11));
			} else {
				sleepCR(random(9,12));
			}
			keyboard.releaseKey(LRUD[randomAll]);
		}
	}

	private boolean sleepCR(int amtOfHalfSecs){
		for (int x = 0; x < (amtOfHalfSecs + 1); x++){
			sleep(random(48,53));
		}
		return true;
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

	private RSNPC perNPC() {
		RSNPC interacting = interactingNPC();
		return interacting != null ? interacting : npcs.getNearest(new Filter<RSNPC>() {
			public boolean accept(RSNPC npc) {
				if (varrock) {
					return npc.getName().equals("Aubury") && AtPerson.contains(npc.getLocation());
				} else {
					return npc.getName().equals("Wizard Distentor") && AtPerson.contains(npc.getLocation());
				}
			}
		});
	}

	private RSNPC banker() {
		return npcs.getNearest(new Filter<RSNPC>() {
			public boolean accept(RSNPC npc) {
				return npc.getID() == bankerID();
			}
		});
	}

	private RSNPC portal() {
		return npcs.getNearest(new Filter<RSNPC>() {
			public boolean accept(RSNPC npc) {
				return npc.getID() == portalID();
			}
		});
	}

	private RSNPC plantNPC() {
		RSNPC interacting = interactingNPC();
		return interacting != null ? interacting : npcs.getNearest(new Filter<RSNPC>() {
			public boolean accept(RSNPC npc) {
				return npc.getID() == markerPlant && AtPerson.contains(npc.getLocation());
			}
		});
	}

	private RSNPC interactingNPC() {
		return npcs.getNearest(new Filter<RSNPC>() {
			public boolean accept(RSNPC n) {
				return n.getInteracting() != null && n.getInteracting().equals(getMyPlayer()) && Bank.contains(n.getLocation());
			}
		});
	}

	public void messageReceived(MessageEvent e) {
		if (e.getMessage().contains("You've just advanced a Min")) {
			gainedLvl++;
		}
		if (e.getMessage().contains("You swing your")) {
			mining = true;
		}
	}

	//Credits Aion
	private String stripFormatting(String str) {
		if (str != null && !str.isEmpty())
			return str.replaceAll("(^[^<]+>|<[^>]+>|<[^>]+$)", "");
		return "";
	}

	// Credits Aion
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

	public void drawObjects(final Graphics g) {
		// Person
		if (perNPC() != null) {
			RSNPC per = perNPC();
			final RSTile t = per.getLocation();
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

		// Portal
		if (objects.getNearest(obs) != null) {
			final RSTile t = portal().getLocation();
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
				Stat = true;
			} else if (!Main) {
				Stat = false;
				Main = true;
			}
		}
		//Prev Button
		if (e.getX() >= 25 && e.getX() < 25 + 16 && e.getY() >= 413 && e.getY() < 413 + 14) {
			if (Main) {
				Main = false;
				Stat = true;
			} else if (!Main) {
				Stat = false;
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
			log("FUCK");
			return null;
		}
	}

	private Image logo = getImage("http://i88.photobucket.com/albums/k170/aaimister/AaimistersEssenceMiner.gif");
	private Image atom = getImage("http://i88.photobucket.com/albums/k170/aaimister/Atomm.png");

	public void onRepaint(Graphics g) {
		if (showPaint) {
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
				currentXP = skills.getExpToNextLevel(14);
				xpToLvl = skills.getExpToNextLevel(14);
				xpGained = skills.getCurrentExp(14) - startEXP;
				xpHour = ((int) ((3600000.0 / (double) runTime) * xpGained));
				if (xpHour != 0) {
					timeToLvl = (int) (((double) xpToLvl / (double) xpHour) * 3600000.0);
				}
				totalEss = (int) (xpGained / xpEss);
				GPtotal = (int) (totalEss * priceEssence);
				essHour = (int) ((3600000.0 / (double) runTime) * totalEss);
				GPHour = (int) ((3600000.0 / (double) runTime) * GPtotal);
				essToLvl = (int) (currentXP / xpEss);
			}

			if (painting) {
				((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				((Graphics2D)g).setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
				((Graphics2D)g).setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			}

			//Person
			//drawObjects(g);

			if (!xButton) {
				//Background
				g.setColor(MainColor);
				g.fillRect(6, 344, 507, 129);
				g.setColor(LineColor);
				g.drawRect(6, 344, 507, 129);
				//Logo
				g.drawImage(logo, 6, 348, null);
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
					//Column 1
					g.setColor(LineColor);
					g.drawString("Time running: " + formattedTime, 63, 390);
					g.drawString("Location: " + Location(), 63, 404);
					g.drawString("Status: " + status + getDots(), 63, 418);
					g.drawString("Current Ore: " + currentOre, 63, 433);
					g.drawString("Total XP: " + formatter.format((long)xpGained), 63, 447);
					g.drawString("Total XP/h: " + formatter.format((long)xpHour), 63, 463);
					//Column 2
					g.drawString("Price of Essence: $" + formatter.format((long)priceEssence), 264, 390);
					g.drawString("Total Money: $" + formatter.format((long)GPtotal), 264, 404);
					g.drawString("Money / Hour: $" + formatter.format((long)GPHour), 264, 418);
					g.drawString("Total Essence: " + formatter.format((long)totalEss), 264, 433);
					g.drawString("Essence / Hour: " + formatter.format((long)essHour), 264, 447);
					g.drawString("Essence to Lvl: " + formatter.format((long)essToLvl), 264, 463);
				}
				if (Stat) {
					//Column 1
					g.setColor(LineColor);
					g.drawString("Time running: " + formattedTime, 63, 390);
					g.drawString("Location: " + Location(), 63, 404);
					g.drawString("Status: " + status + getDots(), 63, 418);
					g.drawString("Current Ore: " + currentOre, 63, 433);
					g.drawString("Total XP: " + formatter.format((long)xpGained), 63, 447);
					g.drawString("Total XP/h: " + formatter.format((long)xpHour), 63, 463);
					//Column 2
					g.drawString("Total Mining XP: " + formatter.format((long)xpGained), 264, 390);
					g.drawString("Mining XP/h: " + formatter.format((long)xpHour), 264, 404);
					g.drawString("Level In: " + formatTime(timeToLvl), 264, 418);
					g.drawString("Mining XP to Lvl: " + formatter.format((long)xpToLvl), 264, 433);
					g.drawString("Current Lvl: " + (skills.getCurrentLevel(14)), 264, 447);
					g.drawString("Gained Lvl(s): " + formatter.format((long)gainedLvl), 264, 463);
				}
				//% Bar
				g.setColor(MainColor);
				g.fillRect(4, 318, 512, 20);
				g.setColor(Black);
				g.fillRect(6, 320, 508, 16);
				g.setColor(PercentRed);
				g.fillRect(6, 320, 508, 16);
				g.setColor(PercentGreen);
				final int Bar = (int) (skills.getPercentToNextLevel(14) * 5.08);
				g.fillRect(6, 320, Bar, 16);
				g.setColor(White);
				g.setFont(Cam);
				g.drawString("" + skills.getPercentToNextLevel(14) + "% to lvl " + (skills.getCurrentLevel(14) + 1) + " Mining", 194, 332);
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

			//Mouse
			drawMouse(g);
		}
	}

	public class AaimistersGUI {
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

		public void submitActionPerformed(ActionEvent e) {
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
			String loc = (String) locationBox.getSelectedItem();
			if (loc.contains("Varrock")) {
				varrock = true;
				door = 24381;
				Bank = AM.BankV;
				AtPerson = AM.AtAubury;
				toMine = AM.toMineV;
				GateT = AM.VGateT;
				//toBank = AM.toBankV;
				PersonT = AM.AuburyT;
				BankT = AM.BankTV;
				bankID = 782;
				Gate = AM.GateV;
				CityArea = AM.VCityArea;
			} else if (loc.contains("Yanille")) {
				varrock = false;
				door = 1600;
				Bank = AM.BankY;
				AtPerson = AM.AtWiz;
				toMine = AM.toMineY;
				GateT = AM.YGateT;
				//toBank = AM.toBankY;
				PersonT = AM.WizT;
				BankT = AM.BankTY;
				bankID = 2213;
				Gate = AM.GateY;
				CityArea = AM.YCityArea;
			}
			if (restBox.isSelected()) {
				rest = true;
			}
			if (paintBox.isSelected()) {
				painting = true;
			}
			if (antibanBox.isSelected()) {
				antiBanOn = true;
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

			// Write settings
			try {
				final BufferedWriter out = new BufferedWriter(new FileWriter(settingsFile));
				out.write((locationBox.getSelectedIndex())
						+ ":" // 0
						+ (restBox.isSelected() ? true : false)
						+ ":" // 1
						+ (colorBox.getSelectedIndex())
						+ ":" // 2
						+ (antibanBox.isSelected() ? true : false)
						+ ":" // 3
						+ (paintBox.isSelected() ? true : false)
						+ ":" // 4
						+ (breakBox.isSelected() ? true : false)
						+ ":" // 5
						+ (randomBox.isSelected() ? true : false)
						+ ":" // 6
						+ (maxTimeBeBox.getValue().toString())
						+ ":" // 7
						+ (minTimeBeBox.getValue().toString())
						+ ":" // 8
						+ (maxBreakBox.getValue().toString())
						+ ":" // 9
						+ (minBreakBox.getValue().toString()));// 10
				out.close();
			} catch (final Exception e1) {
				log.warning("Error saving setting.");
			}
			// End write settings

			AaimistersGUI.dispose();
		}

		private AaimistersGUI() {
			initComponents();
		}

		public void initComponents() {
			AaimistersGUI = new JFrame();
			contentPane = new JPanel();
			colorBox = new JComboBox();
			locationBox = new JComboBox();
			antibanBox = new JCheckBox();
			restBox = new JCheckBox();
			paintBox = new JCheckBox();
			breakBox = new JCheckBox();
			randomBox = new JCheckBox();
			maxTimeBeBox = new JSpinner();
			minTimeBeBox = new JSpinner();
			maxBreakBox = new JSpinner();
			minBreakBox = new JSpinner();
			submit = new JButton();

			// Listeners
			AaimistersGUI.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					closed = true;
				}
			});

			AaimistersGUI.setTitle("Aaimister's Essence Miner");
			AaimistersGUI.setResizable(false);
			AaimistersGUI.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			AaimistersGUI.setBounds(100, 100, 450, 345);
			contentPane = new JPanel();
			contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
			AaimistersGUI.setContentPane(contentPane);

			JLabel lblAaimistersEssenceMiner = new JLabel("Aaimister's Essence Miner");
			lblAaimistersEssenceMiner.setHorizontalAlignment(SwingConstants.CENTER);
			lblAaimistersEssenceMiner.setFont(new Font("Aharoni", Font.PLAIN, 35));

			JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);

			submit.setText("Start");
			submit.setFont(new Font("Aharoni", Font.PLAIN, 14));
			submit.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					submitActionPerformed(e);
				}
			});
			GroupLayout gl_contentPane = new GroupLayout(contentPane);
			gl_contentPane.setHorizontalGroup(
					gl_contentPane.createParallelGroup(Alignment.LEADING)
					.addGroup(gl_contentPane.createSequentialGroup()
							.addGap(1)
							.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
									.addComponent(tabbedPane, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 433, Short.MAX_VALUE)
									.addComponent(lblAaimistersEssenceMiner, GroupLayout.DEFAULT_SIZE, 433, Short.MAX_VALUE)))
									.addGroup(gl_contentPane.createSequentialGroup()
											.addGap(172)
											.addComponent(submit, GroupLayout.PREFERRED_SIZE, 83, GroupLayout.PREFERRED_SIZE)
											.addContainerGap(179, Short.MAX_VALUE))
			);
			gl_contentPane.setVerticalGroup(
					gl_contentPane.createParallelGroup(Alignment.LEADING)
					.addGroup(gl_contentPane.createSequentialGroup()
							.addComponent(lblAaimistersEssenceMiner)
							.addGap(18)
							.addComponent(tabbedPane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(submit, GroupLayout.DEFAULT_SIZE, 23, Short.MAX_VALUE))
			);

			JPanel panel = new JPanel();
			tabbedPane.addTab("General", null, panel, null);

			restBox.setText("Use Rest");
			restBox.setSelected(true);
			restBox.setFont(new Font("Aharoni", Font.PLAIN, 14));

			antibanBox.setText("Anti - Ban");
			antibanBox.setSelected(true);
			antibanBox.setFont(new Font("Aharoni", Font.PLAIN, 14));

			paintBox.setText("Anti - Aliasing");
			paintBox.setSelected(true);
			paintBox.setFont(new Font("Aharoni", Font.PLAIN, 14));

			JLabel lblLocation = new JLabel("Location:");
			lblLocation.setFont(new Font("Aharoni", Font.PLAIN, 20));

			JLabel lblPaintColor = new JLabel("Paint Color:");
			lblPaintColor.setFont(new Font("Aharoni", Font.PLAIN, 20));

			locationBox.setModel(new DefaultComboBoxModel(locationstring));

			colorBox.setModel(new DefaultComboBoxModel(colorstring));
			GroupLayout gl_panel = new GroupLayout(panel);
			gl_panel.setHorizontalGroup(
					gl_panel.createParallelGroup(Alignment.LEADING)
					.addGroup(gl_panel.createSequentialGroup()
							.addGap(13)
							.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
									.addComponent(paintBox)
									.addGroup(gl_panel.createSequentialGroup()
											.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
													.addGroup(gl_panel.createSequentialGroup()
															.addComponent(antibanBox)
															.addPreferredGap(ComponentPlacement.RELATED, 205, Short.MAX_VALUE))
															.addGroup(Alignment.TRAILING, gl_panel.createSequentialGroup()
																	.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
																			.addComponent(lblPaintColor)
																			.addComponent(lblLocation))
																			.addGap(35)))
																			.addGap(2)
																			.addGroup(gl_panel.createParallelGroup(Alignment.LEADING, false)
																					.addComponent(colorBox, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
																					.addComponent(locationBox, 0, 97, Short.MAX_VALUE)))
																					.addComponent(restBox))
																					.addContainerGap())
			);
			gl_panel.setVerticalGroup(
					gl_panel.createParallelGroup(Alignment.LEADING)
					.addGroup(gl_panel.createSequentialGroup()
							.addContainerGap()
							.addComponent(restBox)
							.addGap(11)
							.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
									.addComponent(lblLocation)
									.addComponent(locationBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
									.addGap(15)
									.addComponent(antibanBox)
									.addGap(20)
									.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
											.addComponent(lblPaintColor)
											.addComponent(colorBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
											.addGap(11)
											.addComponent(paintBox)
											.addContainerGap())
			);
			panel.setLayout(gl_panel);

			JPanel panel_1 = new JPanel();
			tabbedPane.addTab("Breaks", null, panel_1, null);

			breakBox.setText("Custom Breaks");
			breakBox.setFont(new Font("Aharoni", Font.PLAIN, 14));
			if (!breakBox.isSelected()) {
				maxTimeBeBox.setEnabled(false);
				minTimeBeBox.setEnabled(false);
				maxBreakBox.setEnabled(false);
				minBreakBox.setEnabled(false);
			}
			breakBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					breakBoxActionPerformed(e);
				}
			});

			randomBox.setText("Random Breaks");
			randomBox.setFont(new Font("Aharoni", Font.PLAIN, 14));
			if (!doBreak) {
				randomBox.setEnabled(false);
			} else {
				randomBox.setEnabled(true);
				maxTimeBeBox.setEnabled(false);
				minTimeBeBox.setEnabled(false);
				maxBreakBox.setEnabled(false);
				minBreakBox.setEnabled(false);
			}
			randomBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					randomBoxActionPerformed(e);
				}
			});

			JLabel lblNewLabel = new JLabel("Time Between Breaks");
			lblNewLabel.setFont(new Font("Aharoni", Font.PLAIN, 15));

			JSeparator separator = new JSeparator();
			separator.setOrientation(SwingConstants.VERTICAL);

			minTimeBeBox.setModel(new SpinnerNumberModel(new Integer(111), new Integer(0), null, new Integer(1)));

			JLabel lblMins = new JLabel("mins");
			lblMins.setFont(new Font("Aharoni", Font.PLAIN, 11));

			JLabel lblTo = new JLabel("to");
			lblTo.setFont(new Font("Aharoni", Font.PLAIN, 11));

			maxTimeBeBox.setModel(new SpinnerNumberModel(new Integer(222), new Integer(0), null, new Integer(1)));

			JLabel label = new JLabel("mins");
			label.setFont(new Font("Aharoni", Font.PLAIN, 11));

			JLabel lblBreakLength = new JLabel("Break Lengths");
			lblBreakLength.setFont(new Font("Aharoni", Font.PLAIN, 15));

			minBreakBox.setModel(new SpinnerNumberModel(new Integer(15), new Integer(0), null, new Integer(1)));

			JLabel label_1 = new JLabel("mins");
			label_1.setFont(new Font("Aharoni", Font.PLAIN, 11));

			JLabel label_2 = new JLabel("to");
			label_2.setFont(new Font("Aharoni", Font.PLAIN, 11));

			maxBreakBox.setModel(new SpinnerNumberModel(new Integer(65), new Integer(0), null, new Integer(1)));

			JLabel label_3 = new JLabel("mins");
			label_3.setFont(new Font("Aharoni", Font.PLAIN, 11));
			GroupLayout gl_panel_1 = new GroupLayout(panel_1);
			gl_panel_1.setHorizontalGroup(
					gl_panel_1.createParallelGroup(Alignment.LEADING)
					.addGroup(gl_panel_1.createSequentialGroup()
							.addContainerGap()
							.addGroup(gl_panel_1.createParallelGroup(Alignment.LEADING)
									.addGroup(gl_panel_1.createSequentialGroup()
											.addComponent(breakBox)
											.addPreferredGap(ComponentPlacement.RELATED, 154, Short.MAX_VALUE)
											.addComponent(randomBox))
											.addGroup(gl_panel_1.createSequentialGroup()
													.addGroup(gl_panel_1.createParallelGroup(Alignment.LEADING)
															.addGroup(gl_panel_1.createSequentialGroup()
																	.addGap(43)
																	.addGroup(gl_panel_1.createParallelGroup(Alignment.LEADING)
																			.addGroup(gl_panel_1.createSequentialGroup()
																					.addComponent(minTimeBeBox, GroupLayout.PREFERRED_SIZE, 56, GroupLayout.PREFERRED_SIZE)
																					.addPreferredGap(ComponentPlacement.RELATED)
																					.addComponent(lblMins))
																					.addGroup(gl_panel_1.createSequentialGroup()
																							.addGap(25)
																							.addComponent(lblTo))
																							.addGroup(gl_panel_1.createSequentialGroup()
																									.addComponent(maxTimeBeBox, GroupLayout.PREFERRED_SIZE, 54, GroupLayout.PREFERRED_SIZE)
																									.addPreferredGap(ComponentPlacement.RELATED)
																									.addComponent(label, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE))))
																									.addGroup(gl_panel_1.createSequentialGroup()
																											.addGap(9)
																											.addComponent(lblNewLabel)))
																											.addGap(33)
																											.addComponent(separator, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
																											.addGroup(gl_panel_1.createParallelGroup(Alignment.LEADING)
																													.addGroup(gl_panel_1.createSequentialGroup()
																															.addGap(47)
																															.addComponent(lblBreakLength))
																															.addGroup(gl_panel_1.createSequentialGroup()
																																	.addGap(95)
																																	.addComponent(label_2, GroupLayout.PREFERRED_SIZE, 11, GroupLayout.PREFERRED_SIZE))
																																	.addGroup(gl_panel_1.createSequentialGroup()
																																			.addGap(70)
																																			.addGroup(gl_panel_1.createParallelGroup(Alignment.TRAILING)
																																					.addComponent(maxBreakBox, GroupLayout.PREFERRED_SIZE, 54, GroupLayout.PREFERRED_SIZE)
																																					.addComponent(minBreakBox, GroupLayout.PREFERRED_SIZE, 56, GroupLayout.PREFERRED_SIZE))
																																					.addPreferredGap(ComponentPlacement.RELATED)
																																					.addGroup(gl_panel_1.createParallelGroup(Alignment.LEADING)
																																							.addComponent(label_3, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)
																																							.addComponent(label_1, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE))))))
																																							.addContainerGap())
			);
			gl_panel_1.setVerticalGroup(
					gl_panel_1.createParallelGroup(Alignment.LEADING)
					.addGroup(gl_panel_1.createSequentialGroup()
							.addGroup(gl_panel_1.createParallelGroup(Alignment.LEADING)
									.addGroup(gl_panel_1.createSequentialGroup()
											.addContainerGap()
											.addGroup(gl_panel_1.createParallelGroup(Alignment.BASELINE)
													.addComponent(breakBox)
													.addComponent(randomBox))
													.addGap(28)
													.addGroup(gl_panel_1.createParallelGroup(Alignment.LEADING)
															.addGroup(gl_panel_1.createSequentialGroup()
																	.addComponent(lblNewLabel)
																	.addGap(18)
																	.addGroup(gl_panel_1.createParallelGroup(Alignment.TRAILING)
																			.addComponent(minTimeBeBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
																			.addComponent(lblMins))
																			.addPreferredGap(ComponentPlacement.UNRELATED)
																			.addGroup(gl_panel_1.createParallelGroup(Alignment.TRAILING)
																					.addGroup(gl_panel_1.createSequentialGroup()
																							.addComponent(lblTo)
																							.addPreferredGap(ComponentPlacement.UNRELATED)
																							.addComponent(maxTimeBeBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
																							.addComponent(label, GroupLayout.PREFERRED_SIZE, 12, GroupLayout.PREFERRED_SIZE)))
																							.addGroup(gl_panel_1.createSequentialGroup()
																									.addComponent(lblBreakLength)
																									.addGap(18)
																									.addGroup(gl_panel_1.createParallelGroup(Alignment.TRAILING)
																											.addComponent(minBreakBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
																											.addComponent(label_1, GroupLayout.PREFERRED_SIZE, 12, GroupLayout.PREFERRED_SIZE))
																											.addPreferredGap(ComponentPlacement.UNRELATED)
																											.addGroup(gl_panel_1.createParallelGroup(Alignment.TRAILING)
																													.addGroup(gl_panel_1.createSequentialGroup()
																															.addComponent(label_2, GroupLayout.PREFERRED_SIZE, 12, GroupLayout.PREFERRED_SIZE)
																															.addPreferredGap(ComponentPlacement.UNRELATED)
																															.addComponent(maxBreakBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
																															.addComponent(label_3, GroupLayout.PREFERRED_SIZE, 12, GroupLayout.PREFERRED_SIZE)))))
																															.addGroup(gl_panel_1.createSequentialGroup()
																																	.addGap(48)
																																	.addComponent(separator, GroupLayout.DEFAULT_SIZE, 127, Short.MAX_VALUE)))
																																	.addContainerGap())
			);
			panel_1.setLayout(gl_panel_1);
			contentPane.setLayout(gl_contentPane);
			// LOAD SAVED SELECTION INFO
			try {
				String filename = getCacheDirectory() + "\\AaimistersEMinerSettings.txt";
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
					if (opts[5].equals("true")) {
						randomBox.setEnabled(true);
						if (opts[6].equals("false")) {
							maxTimeBeBox.setValue(Integer.parseInt(opts[7]));
							minTimeBeBox.setValue(Integer.parseInt(opts[8]));
							maxBreakBox.setValue(Integer.parseInt(opts[9]));
							minBreakBox.setValue(Integer.parseInt(opts[10]));
							maxTimeBeBox.setEnabled(true);
							minTimeBeBox.setEnabled(true);
							maxBreakBox.setEnabled(true);
							minBreakBox.setEnabled(true);
						} else {
							randomBox.setSelected(true);
						}
					}
					if (opts[1].equals("true")) {
						restBox.setSelected(true);
					} else {
						restBox.setSelected(false);
					}
					colorBox.setSelectedIndex(Integer.parseInt(opts[2]));
					locationBox.setSelectedIndex(Integer.parseInt(opts[0]));
					if (opts[3].equals("true")) {
						antibanBox.setSelected(true);
					} else {
						antibanBox.setSelected(false);
					}
					if (opts[4].equals("true")) {
						paintBox.setSelected(true);
					} else {
						paintBox.setSelected(false);
					}
					if (opts[5].equals("true")) {
						breakBox.setSelected(true);
					} else {
						breakBox.setSelected(false);
						randomBox.setEnabled(false);
					}
					if (opts[6].equals("true")) {
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
		private JComboBox colorBox;
		private JComboBox locationBox;
		private JCheckBox antibanBox;
		private JCheckBox paintBox;
		private JCheckBox restBox;
		private JCheckBox breakBox;
		private JCheckBox randomBox;
		private JSpinner maxTimeBeBox;
		private JSpinner minTimeBeBox;
		private JSpinner maxBreakBox;
		private JSpinner minBreakBox;
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