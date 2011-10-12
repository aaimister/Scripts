/**
 * @author Aaimister
 * @version 1.12 ©2010-2011 Aaimister, No one except Aaimister has the right to
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

@ScriptManifest(authors = { "Aaimister" }, website = "http://e216829d.any.gs", name = "Aaimisters Lumbridge Cooker & Fisher v1.12", keywords = "Cooking & Fishing", version = 1.12, description = ("Fishes and cooks crayfish behind Lumbridge Castle."))
public class AaimistersLumbridgeCookerFisher extends Script implements PaintListener, MessageListener, MouseListener {

	private static interface AM {
	//	final RSTile FishP[] = { new RSTile(3203, 3212), new RSTile(3195, 3218), new RSTile(3189, 3222),
	//							 new RSTile(3186, 3229), new RSTile(3184, 3236), new RSTile(3182, 3242),
	//							 new RSTile(3179, 3249), new RSTile(3174, 3255), new RSTile(3173, 3263) };
		final RSArea AtCastle = new RSArea(new RSTile(3205, 3209), new RSTile(3216, 3227));
	//	final RSArea AtStair = new RSArea(new RSTile(3204, 3208), new RSTile(3207, 3211));
		final RSArea AtStore = new RSArea(new RSTile(3211, 3239), new RSTile(3218, 3243));
	//	final RSArea AtBank = new RSArea(new RSTile(3207, 3217, 2), new RSTile(3210, 3220, 2));
		final RSArea AtFire = new RSArea(new RSTile(3180, 3250), new RSTile(3185, 3256));
		final RSArea AtFish = new RSArea(new RSTile(3163, 3261), new RSTile(3180, 3277));
	//	final RSTile toFire1 = new RSTile(3177, 3263);
	//	final RSTile toFire2 = new RSTile(3182, 3253);
		final RSTile StairT = new RSTile(3207, 3209);
		final RSTile StoreT = new RSTile(3213, 3240);
		final RSTile BankT = new RSTile(3208, 3219);
		final RSTile FishT = new RSTile(3172, 3266);
		final RSTile FireT = new RSTile(3183, 3254);
	}
	
	RSWeb walkWeb;
	
	private final String[] colorstring = { "Black", "Blue", "Brown", "Cyan", "Green", "Lime", "Orange", "Pink", "Purple", "Red", "White", "Yellow" };
	private final String[] methodstring = { "Power Fish", "Normal Drop", "Bank Cooked", "Bank Raw", "Sell Cooked", "Sell Raw" };
	private String url = "http://e216829d.any.gs";
	
	AaimistersGUI g = new AaimistersGUI();
	public final File settingsFile = new File(getCacheDirectory(), "AaimistersLCookFishSettings.txt");
	
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
	
	String formatTime(final int milliseconds) {
		final long t_seconds = milliseconds / 1000;
		final long t_minutes = t_seconds / 60;
		final long t_hours = t_minutes / 60;
		final int seconds = (int) (t_seconds % 60);
		final int minutes = (int) (t_minutes % 60);
		final int hours = (int) (t_hours % 60);
		return (nf.format(hours) + ":" + nf.format(minutes) + ":" + nf.format(seconds));
	}
	
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
	//Color LineColor = new Color(0, 0, 0);
	Color ClickC = new Color(187, 0, 0);
	Color UpRed = new Color(169, 0, 0);
	Color Black = new Color(0, 0, 0);
	Color MainColor = Black;
	Color ThinColor = new Color(0, 0, 0, 70);
	Color BoxColor = Black;
	Color LineColor = White;
	
	final NumberFormat nf = NumberFormat.getInstance();
	
	private String currentStat;
	private String status = "";
	
	boolean currentlyBreaking;
	boolean randomBreaks;
	boolean bankedOpen;
	boolean useBanker;
	boolean antiBanOn;
	boolean clickNext;
	boolean dropcoins;
	boolean powerFish;
	boolean fishPaint = true;
	boolean notChosen = true;
	boolean painting;
	boolean rbanking;
	boolean resting;
	boolean doBreak;
	boolean cooking;
	boolean checked;
	boolean banking;
	boolean fishing;
	boolean updated;
	boolean opened;
	boolean rsell;
	boolean check = true;
	boolean sell;
	boolean rest;
	boolean logTime;
	//Paint Buttons
	boolean xButton;
	boolean StatCO;
	boolean Main = true;
	
	private boolean closed;
	
	int fire = 2732;
	int cage = 13431;
	int fishingpool = 6996;
	int rawShrimp = 13435;
	int cookedShrimp = 13433;
	int burnShrimp = 13437;
	int xpShrimp = 10;
	int shrimpCount;
	int burntCount;
	int shrimpCookCount;
	int fishLevel;
	int cookLevel;
	int maxBetween;
	int minBetween;
	int maxLength;
	int minLength;
	int errorCount;
	int idle;
	int startFishXP;
	int currentFishXP;
	int startCookXP;
	int currentCookXP;
	int xpToFishLvl;
	int timeToFishLvl;
	int xpFishGained;
	int xpFishHour;
	int shrimpHour;
	int shrimpCHour;
	int shrimpToLvl;
	int xpToCookLvl;
	int timeToCookLvl;
	int xpCookGained;
	int xpCookHour;
	int xpGained;
	int xpHour;
	
	
	private enum State { FISH, TOFISH, TOFIRE, TOBANK, TOSTORE, COOK, DROP, BANK, SELL, ERROR };

	private State getState() {
		if (!powerFish) {
			if (inventory.contains(cage) && !inventory.isFull() && !inventory.containsOneOf(cookedShrimp, burnShrimp)) {
				if (AM.AtFish.contains(getMyPlayer().getLocation())) {
					return State.FISH;
				} else {
					status = "Walking to Fish..";
					return State.TOFISH;
				}
			} else if (inventory.containsOneOf(rawShrimp) && inventory.isFull()) {
				if (rsell) {
					if (AM.AtStore.contains(getMyPlayer().getLocation())) {
						status = "Selling...";
						return State.SELL;
					} else {
						status = "Walking to Store...";
						return State.TOSTORE;
					}
				} else if (rbanking) {
					if (game.getPlane() == 2 && calc.distanceTo(AM.BankT) <= 3) {
						status = "Banking...";
						return State.BANK;
					} else {
						status = "Walking to Bank...";
						return State.TOBANK;
					}
				} else {
					if (AM.AtFire.contains(getMyPlayer().getLocation())) {
						return State.COOK;
					} else {
						status = "Walking to Fire..";
						return State.TOFIRE;
					}
				}
			} else if (inventory.containsOneOf(cookedShrimp, burnShrimp) && !inventory.contains(rawShrimp)) {
				if (inventory.isFull()) {
					if (sell) {
						if (AM.AtStore.contains(getMyPlayer().getLocation())) {
							status = "Selling...";
							return State.SELL;
						} else {
							status = "Walking to Store...";
							return State.TOSTORE;
						}
					} else if (banking) {
						if (calc.distanceTo(AM.BankT) <= 3) {
							status = "Banking...";
							return State.BANK;
						} else {
							status = "Walking to Bank...";
							return State.TOBANK;
						}
					} else {
						status = "Dropping..";
						return State.DROP;
					}
				} else {
					status = "Dropping..";
					return State.DROP;
				}
			} else if (inventory.containsOneOf(cookedShrimp, burnShrimp) && inventory.contains(rawShrimp)) {
				if (AM.AtFire.contains(getMyPlayer().getLocation())) {
					return State.COOK;
				} else {
					status = "Walking to Fire..";
					return State.TOFIRE;
				}
			} else {
				return State.ERROR;
			}
		} else {
			if (inventory.contains(cage) && !inventory.isFull()) {
				status = "Fishing..";
				return State.FISH;
			} else if (inventory.isFull()) {
				status = "Dropping..";
				return State.DROP;
			} else {
				return State.ERROR;
			}
		}
	}
	
	public double getVersion() { 
		return 1.12;
	}
	
	public boolean onStart() {
		status = "Starting up..";
		
		try {
			settingsFile.createNewFile();
		} catch (final IOException ignored) {
		}
        
		createAndWaitforGUI();
		if (closed) {
        	log.severe("The GUI window was closed!");
        	return false;
        }
		
		startFishXP = skills.getCurrentExp(10);
		currentFishXP = skills.getExpToNextLevel(10);
		startCookXP = skills.getCurrentExp(7);
		currentCookXP = skills.getExpToNextLevel(7);
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
		log("Thanks for using Aaimister's Lumbridge Cooker & Fisher!");
		if (!powerFish && !rsell && !rbanking) {
			log("In " + formattedTime + " You cooked " + formatter.format(shrimpCookCount) + " unburnt Crayfish & Caught " + formatter.format(shrimpCount) + " Crayfish!");
			log("You Gained: " + cookLevel + " level(s) in Cooking & " + fishLevel + " level(s) in Fishing!");
		} else {
			log("In " + formattedTime + " You Caught " + formatter.format(shrimpCount) + " Crayfish!");
			log("You Gained: " + fishLevel + " level(s) in Fishing!");
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
				if (!powerFish && !rsell && !rbanking) {
					Main = false;
					StatCO = true;
				}
			} else if (StatCO) {
				StatCO = false;
				Main = true;
			}
		}
		//Prev Button
		if (e.getX() >= 25 && e.getX() < 25 + 16 && e.getY() >= 413 && e.getY() < 413 + 14) {
			if (Main) {
				if (!powerFish && !rsell && !rbanking) {
					Main = false;
					StatCO = true;
				}
			} else if (StatCO) {
				StatCO = false;
				Main = true;
			}
		}
	}
	public void mouseReleased(MouseEvent e){
	}

	public void messageReceived(MessageEvent e) {
		if (e.getMessage().contains("You've just advanced a Coo")) {
			cookLevel++;
    	}
		if (e.getMessage().contains("You've just advanced a Fish")) {
			fishLevel++;
    	}
		if (e.getMessage().contains("You successfully cook")) {
			idle = 0;
			shrimpCookCount++;
    	}
		if (e.getMessage().contains("You accidently burn")) {
			idle = 0;
			burntCount++;
    	}
		if (e.getMessage().contains("You catch")) {
			idle = 0;
			shrimpCount++;
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

	private String Location() {
		RSTile locationP = getMyPlayer().getLocation();
		if (AM.AtFire.contains(locationP)) {
			return "Fire Area";
		} else if (AM.AtFish.contains(locationP)) {
			return "Fishing Area";
		} else if (AM.AtStore.contains(locationP)) {
			return "Shop";
		} else if (calc.distanceTo(AM.BankT) <= 3) {
			return "Bank";
		} else if (calc.distanceTo(AM.FireT) > 100) {
			if (!game.isLoggedIn()) {
				return "Login Screen";
			} else {
				return "Unknown";
			}
		} else {
			return "Lumbridge";
		}
	}
	
	private boolean breakingCheck(){
		if (nextBreak <= System.currentTimeMillis()){
			return true;
		}
		return false;
	}
	
	private void clickObj(RSObject x, String y) {
		try {
			if (x.getModel().getPointOnScreen() != null) {
				x.getModel().hover();
				sleep(150, 300);
				x.interact(y);
			}
		} catch (Exception e) {

		}
	}
	
	private boolean stepTo(RSTile tile) {
		RSPath walkPath = walking.getPath(tile.randomize(1, 1));
		try {
			if (walkPath != null) {
	        	if ((!getMyPlayer().isMoving() || calc.distanceTo(walking.getDestination()) < 4) && !AM.AtCastle.contains(getMyPlayer().getLocation())) {
	        		return walkPath.traverse();
	            }
	        }
		} catch (Exception e) {
			
		}
        return false;
	}
	
	private void walkW(RSTile dest) {
		walkWeb = web.getWeb(getMyPlayer().getLocation(), dest);
		try {
			if (walkWeb != null) {
				if (calc.distanceTo(dest) > 4) {
					if (!getMyPlayer().isMoving() || calc.distanceTo(walking.getDestination()) < 4) {
						walkWeb.step();
					}
				}
	        }
		} catch (Exception e) {
			
		}
	}
	
	private void setCamera() {
		if (camera.getPitch() < 10) {
			camera.setPitch(true);
			sleep(1000, 1600);
		}
	}

	private void doRest() {
		if (walking.getEnergy() < random(10, 30)) {
			if (!resting && !cooking && !fishing) {
				status = "Resting..";
				interfaces.getComponent(750, 6).interact("Rest");
				mouse.moveSlightly();
				resting = true;
				sleep(400, 600);
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
				doRest();
			}
		}
	}
	
	@Override
	public int loop() {
		if (breakingCheck() && doBreak) {
			status = "Breaking...";
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
			status = "Breaking..";
			return 3000;
		}
		
		if (startTime == 0 && skills.getCurrentLevel(10) != 0 && skills.getCurrentLevel(7) != 0) {
			startTime = System.currentTimeMillis();
			startFishXP = skills.getCurrentExp(10);
			currentFishXP = skills.getExpToNextLevel(10);
			startCookXP = skills.getCurrentExp(7);
			currentCookXP = skills.getExpToNextLevel(7);
		}
		
		if (logTime) {
			log("Next Break In: " + formatTime((int) nextBreakT) + " For: " + formatTime((int) nextLength) + ".");
			logTime = false;
		}
		
		mouse.setSpeed(random(4, 8));
		setCamera();
		setRun();
		
		if (resting) {
			if (antiBanTime <= System.currentTimeMillis()) {
				check = false;
				doAntiBan();
			}
			if (getMyPlayer().getAnimation() == -1 && !cooking && !fishing) {
				doRest();
				sleep(200, 800);
			}
			return random(100, 250);
		}
		
		switch (getState()) {
		case FISH:
			notChosen = true;
			try {
				RSNPC pool = fishPool();
				if (idle > 2) {
					fishing = false;
					idle = 0;
				}
				if (interfaces.canContinue()) {
					interfaces.clickContinue();
					idle = 0;
				}
				if (calc.tileOnScreen(pool.getLocation()) && AM.AtFish.contains(getMyPlayer().getLocation())) {
					status = "Fishing..";
					if (getMyPlayer().getAnimation()  == -1 && !fishing) {
						pool.interact("Cage");
						fishing = true;
						return random(1000, 1200);
					} else {
						if (getMyPlayer().getAnimation() == -1) {
							idle++;
						}
						if (antiBanTime <= System.currentTimeMillis()) {
							check = true;
							doAntiBan();
						}
						return random(200, 500);
					}
				} else {
					stepTo(pool.getLocation());
					sleep(50);
				}
			} catch (Exception e) {
				
			}
			break;
		case COOK:
			try {
				status = "Cooking..";
				if (idle > 4) {
					cooking = false;
					idle = 0;
				}
				if (interfaces.canContinue()) {
					interfaces.clickContinue();
					idle = 0;
				}
				RSObject cooker = goodFire();
				if (calc.tileOnScreen(cooker.getLocation()) && calc.distanceTo(AM.FireT) <= 4) {
					status = "Cooking..";
					if (inventory.getSelectedItem() == null && !cooking && !interfaces.getComponent(905, 14).isValid()) {
						RSItem shrimp = inventory.getItem(rawShrimp);
						shrimp.interact("Use");
						return random(200, 800);
					}
					if (!interfaces.getComponent(905, 14).isValid()) {
						if (inventory.getSelectedItem() != null && !cooking) {
							cooker.interact("Use");
							return random(1150, 1300);
						}
					} else {
						RSComponent doCook = interfaces.getComponent(905, 14);
						doCook.interact("Cook");
						cooking = true;
						return random(1000, 1200);
					}
					if (cooking) {
						if (antiBanTime <= System.currentTimeMillis()) {
							check = true;
							doAntiBan();
						}
						if (getMyPlayer().getAnimation() == -1) {
							idle++;
						}
					}
				} else {
					stepTo(cooker.getLocation());
					sleep(50);
				}
			} catch (Exception e) {
				
			}
			break;
		case TOFISH:
			idle = 0;
			cooking = false;
			fishing = false;
			if (dropcoins && !interfaces.getComponent(620, 0).isValid()) {
				if (inventory.contains(995)) {
					drop();
				}
			}
			if (game.getPlane() != 0) {
				if (calc.distanceTo(AM.StairT) <= 3) {
					if (game.getPlane() == 2) {
						RSObject st = objects.getNearest(36775);
						if (st != null) {
							if (st.isOnScreen()) {
								clickObj(st, "Climb-down");
								return random(1500, 2000);
							} else {
								if (!getMyPlayer().isMoving() || calc.distanceTo(walking.getDestination()) < 4) {
									walking.walkTileMM(st.getLocation());
								}
								return random(1000, 1200);
							}
						}
					} else if (game.getPlane() == 1) {
						RSObject st2 = objects.getNearest(36774);
						if (st2 != null) {
							if (st2.isOnScreen()) {
								clickObj(st2, "Climb-down");
								return random(1500, 2000);
							} else {
								if (!getMyPlayer().isMoving() || calc.distanceTo(walking.getDestination()) < 4) {
									walking.walkTileMM(st2.getLocation());
								}
								return random(1000, 1200);
							}
						}
					}
				} else {
					stepTo(AM.StairT);
					sleep(50);
				}
			} else {
				if (AM.AtCastle.contains(getMyPlayer().getLocation())) {
					if (!getMyPlayer().isMoving() || calc.distanceTo(walking.getDestination()) < 4) {
						walking.walkTileMM(walking.getClosestTileOnMap(new RSTile(3202, 3216)));
						return 50;
					}
				}
				walkW(AM.FishT);
				sleep(50);
			}
			break;
		case TOFIRE:
			idle = 0;
			fishing = false;
			cooking = false;
			stepTo(AM.FireT);
			sleep(50);
			break;
		case TOSTORE:
			idle = 0;
			fishing = false;
			cooking = false;
			stepTo(AM.StoreT);
			sleep(50);
			break;
		case TOBANK:
			if (game.getPlane() != 2) {
				if (calc.distanceTo(AM.StairT) <= 3) {
					if (game.getPlane() == 0) {
						RSObject st = objects.getNearest(36773);
						if (st != null) {
							if (st.isOnScreen()) {
								clickObj(st, "Climb-up");
								return random(1500, 2000);
							} else {
								walkW(AM.StairT);
								return 50;
							}
						}
					} else if (game.getPlane() == 1) {
						RSObject st2 = objects.getNearest(36774);
						if (st2 != null) {
							if (st2.isOnScreen()) {
								clickObj(st2, "Climb-up");
								return random(1500, 2000);
							} else {
								if (!getMyPlayer().isMoving() || calc.distanceTo(walking.getDestination()) < 4) {
									walking.walkTileMM(st2.getLocation());
								}
								return random(1000, 1200);
							}
						}
					}
				} else {
					if (game.getPlane() == 0 && AM.AtCastle.contains(getMyPlayer().getLocation())) {
						if (!getMyPlayer().isMoving() || calc.distanceTo(walking.getDestination()) < 4) {
							walking.walkTileMM(walking.getClosestTileOnMap(AM.StairT).randomize(1, 1));
							return 50;
						}
					}
					stepTo(AM.StairT);
					sleep(50);
				}
			} else {
				stepTo(AM.BankT);
				sleep(50);
			}
			break;
		case DROP:
			idle = 0;
			drop();
			break;
		case SELL:
			if (shop() != null) {
				RSComponent se = interfaces.getComponent(620, 0);
				if (se.isValid()) {
					if (inventory.contains(rawShrimp)) {
						inventory.getItem(rawShrimp).interact("Sell 50");
						sleep(1000, 1300);
					} 
					if (inventory.contains(cookedShrimp)) {
						inventory.getItem(cookedShrimp).interact("Sell 50");
						sleep(1000, 1300);
					} 
					if (inventory.contains(burnShrimp)) {
						inventory.getItem(burnShrimp).interact("Sell 50");
						sleep(1000, 1300);
					}
				} else {
					shop().interact("Trade");
					return (calc.distanceTo(shop().getLocation()) * 1000);
				}
			}
			break;
		case BANK:
			if (idle > 3) {
				opened = false;
				bankedOpen = false;
				idle = 0;
			}
			if (notChosen) {
				if (random(0, 5) == 0 || random(0, 5) == 2) {
					useBanker = true;
				} else {
					useBanker = false;
				}
				notChosen = false;
			}
			RSObject booth = objects.getNearest(36786);
			RSNPC bankP = banker();
			if (game.getPlane() == 2 && calc.distanceTo(AM.BankT) <= 3 && booth.isOnScreen()) {
				if (!bank.isOpen()) {
					idle++;
					if (!opened) {
						if (useBanker) {
							bankP.interact("Bank Banker");
						} else {
							booth.interact("Use-quickly");
						}
						opened = true;
						return random(200, 500);
					}
				} else {
					opened = false;
					idle++;
					if (!bankedOpen && bank.isOpen()) {
						bank.depositAllExcept(cage);
						sleep(350, 500);
						bankedOpen = true;
						return random(100, 150);
					}
				}
			} else {
				stepTo(AM.BankT);
				sleep(50);
			}
			break;
		case ERROR:
			if (antiBanTime <= System.currentTimeMillis()) {
				drop();
			}
			if (!inventory.contains(cage)) {
				log.warning("No Cage!");
				game.logout(true);
				stopScript();
			}
			break;
		}
		return random(300, 600);
	}
	
	private RSNPC banker() {
		return npcs.getNearest(new Filter<RSNPC>() {
			public boolean accept(RSNPC n) {
				return n.getID() == 494;
			}
		});
	}
	
	private RSNPC fishPool() {
		return npcs.getNearest(new Filter<RSNPC>() {
			public boolean accept(RSNPC g) {
				return g.getID() == fishingpool && AM.AtFish.contains(g.getLocation());
			}
		});
	}
	
	private RSObject goodFire() {
		return objects.getNearest(new Filter<RSObject>() {
			public boolean accept(RSObject g) {
				return g.getID() == fire && AM.AtFire.contains(g.getLocation());
			}
		});
	}
	
	private RSNPC shop() {
		return npcs.getNearest(new Filter<RSNPC>() {
			public boolean accept(RSNPC g) {
				return g.getID() == random(520, 521);
			}
		});
	}
	
	private void drop() {
		try {
			status = "Dropping..";
			mouse.setSpeed(random(4, 8));
			if (dropcoins) {
				inventory.dropAllExcept(true, cage);
			} else {
				inventory.dropAllExcept(true, cage, 995);
			}
			sleep(100, 300);
		} catch (Exception e) {
			
		}
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
	
	public void checkXP() {
		if (!check) {
			return;
		}
		if (fishing) {
			if (game.getCurrentTab() != 2) {
	    		game.openTab(2);
	    		sleep(500, 900);
	    	}
	    	mouse.move(random(677, 729), random(270, 287));
			sleep(2800, 5500);
			game.openTab(4);
			sleep(50, 100);
			mouse.moveRandomly(50, 900);
		}
		if (cooking && !powerFish) {
			if (game.getCurrentTab() != 2) {
	    		game.openTab(2);
	    		sleep(500, 900);
	    	}
	    	mouse.move(random(678, 729), random(299, 317));
			sleep(2800, 5500);
			game.openTab(4);
			sleep(50, 100);
			mouse.moveRandomly(50, 900);
		}
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
	
	 public void drawObjects(final Graphics g) {
	    	// Fire
	    	if (calc.distanceTo(AM.FireT) <= 8) {
	    		RSObject cooker = goodFire();
	        	final RSTile t = cooker.getLocation();
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
	    			g.setColor(ThinColor);
	    			g.fillPolygon(new int[] { py.x, pxy.x, px.x, pn.x }, new int[] {
	        				py.y, pxy.y, px.y, pn.y }, 4);
	        		g.setColor(Black);
	        		g.drawPolygon(new int[] { py.x, pxy.x, px.x, pn.x }, new int[] {
	        				py.y, pxy.y, px.y, pn.y }, 4);
	    		}
			}
	    	
	    	// Pool
	    	if (calc.distanceTo(AM.FishT) <= 8) {
	    		RSNPC pool = fishPool();
	        	final RSTile t = pool.getLocation();
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
	        		g.setColor(ThinColor);
	        		g.fillPolygon(new int[] { py.x, pxy.x, px.x, pn.x }, new int[] {
	        				py.y, pxy.y, px.y, pn.y }, 4);
	        		g.setColor(Black);
	        		g.drawPolygon(new int[] { py.x, pxy.x, px.x, pn.x }, new int[] {
	        				py.y, pxy.y, px.y, pn.y }, 4);
	    		}
	    	}
	    }
	
	 public void main(final Graphics g) {
		   long totalTime = System.currentTimeMillis() - startTime;
	   	   final String formattedTime = formatTime((int) totalTime);
		   g.setColor(LineColor);
		   g.drawString("Time running: " + formattedTime, 63, 390);
		   g.drawString("Location: " + Location(), 63, 404);
		   g.drawString("Status: " + status, 63, 418);
		   g.drawString("Current Fish: Crayfish", 63, 433);
		   g.drawString("Total XP: " + formatter.format((long)xpGained), 63, 447);
		   g.drawString("Total XP/h: " + formatter.format((long)xpHour), 63, 463);
	   }
	   
	   public void drawMouse(final Graphics g) {
		   final Point loc = mouse.getLocation();
			final long mpt = System.currentTimeMillis() - mouse.getPressTime();
			if (mouse.getPressTime() == -1 || mpt >= 1000) {
				g.setColor(ThinColor);
				g.drawLine(0, loc.y, 766, loc.y);
				g.drawLine(loc.x, 0, loc.x, 505);
				g.setColor(MainColor);
				g.drawLine(0, loc.y + 1, 766, loc.y + 1);
				g.drawLine(0, loc.y - 1, 766, loc.y - 1);
				g.drawLine(loc.x + 1, 0, loc.x + 1, 505);
				g.drawLine(loc.x - 1, 0, loc.x - 1, 505);
			}
			if (mpt < 1000) {
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
	
	   private String getGained() {
		   if (Main) {
			   return " (" + fishLevel + ")";
		   } else {
			   return " (" + cookLevel + ")";
		   }
	   }
	   
	   private int getStat() {
		   if (Main) {
			   currentStat = " Fishing";
			   return 10;
		   } else {
			   currentStat = " Cooking";
			   return 7;
		   }
		}
	   
	   private Image getImage(String url) {
			try {
				return ImageIO.read(new URL(url));
			} catch (IOException e) {
				return null;
			}
		}
	   
	private Image Mouse = getImage("http://i88.photobucket.com/albums/k170/aaimister/mousee.png");
	private Image cMouse = getImage("http://i88.photobucket.com/albums/k170/aaimister/cmouse.png");	
	private Image logo = getImage("http://i88.photobucket.com/albums/k170/aaimister/AaimistersLumCookerFisher.png");
	private Image atom = getImage("http://i88.photobucket.com/albums/k170/aaimister/Atomm.png");
		
	public void onRepaint(Graphics g) {
		long totalTime = System.currentTimeMillis() - startTime;
    	
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
    		currentFishXP = skills.getExpToNextLevel(10);
			
			xpToFishLvl = skills.getExpToNextLevel(10);
			xpFishGained = skills.getCurrentExp(10) - startFishXP;
			xpFishHour = ((int) ((3600000.0 / (double) runTime) * xpFishGained));
			if (xpFishHour != 0) {
				timeToFishLvl = (int) (((double) xpToFishLvl / (double) xpFishHour) * 3600000.0);
			}
			shrimpHour = (int) ((3600000.0 / (double) runTime) * shrimpCount);
			
			currentCookXP = skills.getExpToNextLevel(7);
			
			xpToCookLvl = skills.getExpToNextLevel(7);
			xpCookGained = skills.getCurrentExp(7) - startCookXP;
			xpCookHour = ((int) ((3600000.0 / (double) runTime) * xpCookGained));
			if (xpCookHour != 0) {
				timeToCookLvl = (int) (((double) xpToCookLvl / (double) xpCookHour) * 3600000.0);
			}
			shrimpCHour = (int) ((3600000.0 / (double) runTime) * shrimpCookCount);
			
			xpGained = xpFishGained + xpCookGained;
			xpHour = (int) ((3600000.0 / (double) runTime) * xpGained);
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
				main(g);
				//Column 2
				g.drawString("Crayfish Caught: " + formatter.format((long)shrimpCount), 264, 390);
				g.drawString("Crayfish / Hour: " + formatter.format((long)shrimpHour), 264, 404);
				g.drawString("Total XP: " + formatter.format((long)xpFishGained), 264, 418);
				g.drawString("XP / Hour: " + formatter.format((long)xpFishHour), 264, 433);
				g.drawString("XP to Lvl: " + formatter.format((long)xpToFishLvl), 264, 447);
				g.drawString("Level In: " + formatTime(timeToFishLvl), 264, 463);
			}
			if (StatCO) {
				//Column 1
				main(g);
				//Column 2
				g.drawString("Crayfish Cooked: " + formatter.format((long)shrimpCookCount), 264, 390);
				g.drawString("Crayfish / Hour: " + formatter.format((long)shrimpCHour), 264, 404);
				g.drawString("Total XP: " + formatter.format((long)xpCookGained), 264, 418);
				g.drawString("XP / Hour: " + formatter.format((long)xpCookHour), 264, 433);
				g.drawString("XP to Lvl: " + formatter.format((long)xpToCookLvl), 264, 447);
				g.drawString("Level In: " + formatTime(timeToCookLvl), 264, 463);
			}
			//% Bar
			g.setColor(MainColor);
			g.fillRect(4, 318, 512, 20);
			g.setColor(Black);
			g.fillRect(6, 320, 508, 16);
			g.setColor(PercentRed);
			g.fillRect(6, 320, 508, 16);
			g.setColor(PercentGreen);
			g.fillRect(6, 320, skills.getPercentToNextLevel(getStat()) * (508/100), 16);
			g.setColor(White);
			g.setFont(Cam);
			g.drawString("" + skills.getPercentToNextLevel(getStat()) + "% to lvl " + (skills.getCurrentLevel(getStat()) + 1) + currentStat + getGained(), 194, 332);
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
		
		//Objects
		drawObjects(g);
	}
	
	public class AaimistersGUI {

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
         	String med = (String) methBox.getSelectedItem();
         	if (med.contains("Power Fish")) {
         		powerFish = true;
         	} else if (med.contains("Bank Cooked")) {
         		banking = true;
         	} else if (med.contains("Bank Raw")) {
         		rbanking = true;
         	} else if (med.contains("Sell Cooked")) {
         		sell = true;
         	} else if (med.contains("Sell Raw")) {
         		rsell = true;
         	}
         	if (coinBox.isSelected()) {
         		dropcoins = true;
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
             }
             
          // Write settings
 			try {
 				final BufferedWriter out = new BufferedWriter(new FileWriter(settingsFile));
 				out.write((coinBox.isSelected() ? true : false)
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
 						+ (minBreakBox.getValue().toString())
 						+ ":" // 10
 						+ (methBox.getSelectedIndex())
 						// 11
 				);
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
		
		private void initComponents() {
			AaimistersGUI = new JFrame();
			contentPane = new JPanel();
			coinBox = new JCheckBox();
			colorBox = new JComboBox();
			methBox = new JComboBox();
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
			
	        AaimistersGUI.setTitle("Aaimister's Lum. Cooker & Fisher");
	        AaimistersGUI.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	        AaimistersGUI.setBounds(100, 100, 450, 352);
			contentPane = new JPanel();
			contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
			AaimistersGUI.setContentPane(contentPane);
			
			JLabel lblAaimistersLumCooker = new JLabel("Aaimister's Lum. Cooker & Fisher");
			lblAaimistersLumCooker.setHorizontalAlignment(SwingConstants.CENTER);
			lblAaimistersLumCooker.setFont(new Font("Smudger LET", Font.PLAIN, 36));
			
			JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
			
			submit.setText("Start");
			submit.setFont(new Font("Smudger LET", Font.PLAIN, 20));
			submit.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					submitActionPerformed(e);
				}
			});
			GroupLayout gl_contentPane = new GroupLayout(contentPane);
			gl_contentPane.setHorizontalGroup(
				gl_contentPane.createParallelGroup(Alignment.LEADING)
					.addComponent(lblAaimistersLumCooker, GroupLayout.DEFAULT_SIZE, 424, Short.MAX_VALUE)
					.addComponent(tabbedPane, GroupLayout.DEFAULT_SIZE, 424, Short.MAX_VALUE)
					.addGroup(Alignment.TRAILING, gl_contentPane.createSequentialGroup()
						.addGap(91)
						.addComponent(submit, GroupLayout.PREFERRED_SIZE, 244, GroupLayout.PREFERRED_SIZE)
						.addContainerGap(89, Short.MAX_VALUE))
			);
			gl_contentPane.setVerticalGroup(
				gl_contentPane.createParallelGroup(Alignment.LEADING)
					.addGroup(gl_contentPane.createSequentialGroup()
						.addComponent(lblAaimistersLumCooker, GroupLayout.PREFERRED_SIZE, 54, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(tabbedPane, GroupLayout.PREFERRED_SIZE, 213, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(submit, GroupLayout.PREFERRED_SIZE, 27, Short.MAX_VALUE))
			);
			
			JPanel panel = new JPanel();
			tabbedPane.addTab("General", null, panel, null);
			
			restBox.setText("Use Rest");
			restBox.setSelected(true);
			restBox.setFont(new Font("Traditional Arabic", Font.PLAIN, 13));
			
			antibanBox.setText("Use Anti-Ban");
			antibanBox.setSelected(true);
			antibanBox.setFont(new Font("Traditional Arabic", Font.PLAIN, 13));
			
			paintBox.setText("Enable Anti-Aliasing");
			paintBox.setSelected(true);
			paintBox.setFont(new Font("Traditional Arabic", Font.PLAIN, 13));
			
			JLabel lblPaintColor = new JLabel("Paint Color:");
			lblPaintColor.setFont(new Font("Traditional Arabic", Font.PLAIN, 20));
			
			JLabel lblMethod = new JLabel("Method:");
			lblMethod.setFont(new Font("Traditional Arabic", Font.PLAIN, 20));
			
			methBox.setModel(new DefaultComboBoxModel(methodstring));
			
			colorBox.setModel(new DefaultComboBoxModel(colorstring));
			
			coinBox.setText("Drop Coins");
			coinBox.setFont(new Font("Traditional Arabic", Font.PLAIN, 13));
			GroupLayout gl_panel = new GroupLayout(panel);
			gl_panel.setHorizontalGroup(
				gl_panel.createParallelGroup(Alignment.LEADING)
					.addGroup(gl_panel.createSequentialGroup()
						.addGap(20)
						.addGroup(gl_panel.createParallelGroup(Alignment.TRAILING)
							.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
								.addComponent(lblPaintColor)
								.addComponent(lblMethod))
							.addComponent(coinBox))
						.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
							.addGroup(gl_panel.createSequentialGroup()
								.addGap(48)
								.addComponent(restBox)
								.addPreferredGap(ComponentPlacement.RELATED, 61, Short.MAX_VALUE)
								.addComponent(antibanBox)
								.addGap(20))
							.addGroup(gl_panel.createSequentialGroup()
								.addGap(47)
								.addGroup(gl_panel.createParallelGroup(Alignment.TRAILING, false)
									.addComponent(methBox, Alignment.LEADING, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
									.addComponent(colorBox, Alignment.LEADING, 0, 126, Short.MAX_VALUE))
								.addContainerGap(128, Short.MAX_VALUE))))
					.addGroup(Alignment.TRAILING, gl_panel.createSequentialGroup()
						.addContainerGap(149, Short.MAX_VALUE)
						.addComponent(paintBox)
						.addGap(145))
			);
			gl_panel.setVerticalGroup(
				gl_panel.createParallelGroup(Alignment.LEADING)
					.addGroup(gl_panel.createSequentialGroup()
						.addGap(7)
						.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
							.addComponent(restBox)
							.addComponent(antibanBox)
							.addComponent(coinBox))
						.addGap(18)
						.addComponent(paintBox)
						.addGap(17)
						.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
							.addComponent(lblMethod)
							.addComponent(methBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGap(18)
						.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
							.addComponent(lblPaintColor)
							.addComponent(colorBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGap(7))
			);
			panel.setLayout(gl_panel);
			
			JPanel panel_1 = new JPanel();
			tabbedPane.addTab("Breaks", null, panel_1, null);
			
			breakBox.setText("Custom Breaks");
			breakBox.setFont(new Font("Traditional Arabic", Font.PLAIN, 13));
			
			randomBox.setText("Random Breaks");
			randomBox.setFont(new Font("Traditional Arabic", Font.PLAIN, 13));
			
			JLabel lblTimeBetweenBreaks = new JLabel("Time Between Breaks:");
			lblTimeBetweenBreaks.setFont(new Font("Traditional Arabic", Font.PLAIN, 16));
			
			JLabel lblBreakLengths = new JLabel("Break Lengths:");
			lblBreakLengths.setFont(new Font("Traditional Arabic", Font.PLAIN, 16));
			
			minTimeBeBox.setModel(new SpinnerNumberModel(new Integer(111), null, null, new Integer(1)));
			
			JLabel lblMins = new JLabel("mins");
			lblMins.setFont(new Font("Traditional Arabic", Font.PLAIN, 12));
			
			JLabel lblTo = new JLabel("to");
			lblTo.setFont(new Font("Traditional Arabic", Font.PLAIN, 12));
			
			maxTimeBeBox.setModel(new SpinnerNumberModel(new Integer(222), null, null, new Integer(1)));
			
			JLabel label = new JLabel("mins");
			label.setFont(new Font("Traditional Arabic", Font.PLAIN, 12));
			
			JSeparator separator = new JSeparator();
			separator.setOrientation(SwingConstants.VERTICAL);
			separator.setForeground(Color.BLACK);
			
			minBreakBox.setModel(new SpinnerNumberModel(new Integer(15), null, null, new Integer(1)));
			
			JLabel label_1 = new JLabel("mins");
			label_1.setFont(new Font("Traditional Arabic", Font.PLAIN, 12));
			
			JLabel label_2 = new JLabel("to");
			label_2.setFont(new Font("Traditional Arabic", Font.PLAIN, 12));
			
			maxBreakBox.setModel(new SpinnerNumberModel(new Integer(60), null, null, new Integer(1)));
			
			JLabel label_3 = new JLabel("mins");
			label_3.setFont(new Font("Traditional Arabic", Font.PLAIN, 12));
			GroupLayout gl_panel_1 = new GroupLayout(panel_1);
			gl_panel_1.setHorizontalGroup(
				gl_panel_1.createParallelGroup(Alignment.LEADING)
					.addGroup(gl_panel_1.createSequentialGroup()
						.addGroup(gl_panel_1.createParallelGroup(Alignment.LEADING)
							.addGroup(gl_panel_1.createSequentialGroup()
								.addGap(16)
								.addGroup(gl_panel_1.createParallelGroup(Alignment.LEADING)
									.addGroup(gl_panel_1.createParallelGroup(Alignment.LEADING)
										.addComponent(breakBox)
										.addComponent(lblTimeBetweenBreaks))
									.addGroup(gl_panel_1.createSequentialGroup()
										.addGap(10)
										.addGroup(gl_panel_1.createParallelGroup(Alignment.LEADING, false)
											.addComponent(maxTimeBeBox)
											.addComponent(minTimeBeBox, GroupLayout.DEFAULT_SIZE, 54, Short.MAX_VALUE))
										.addPreferredGap(ComponentPlacement.RELATED)
										.addGroup(gl_panel_1.createParallelGroup(Alignment.LEADING)
											.addComponent(lblMins)
											.addComponent(label, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)))))
							.addGroup(gl_panel_1.createSequentialGroup()
								.addGap(53)
								.addComponent(lblTo)))
						.addPreferredGap(ComponentPlacement.RELATED, 53, Short.MAX_VALUE)
						.addGroup(gl_panel_1.createParallelGroup(Alignment.LEADING)
							.addGroup(Alignment.TRAILING, gl_panel_1.createSequentialGroup()
								.addComponent(separator, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGroup(gl_panel_1.createParallelGroup(Alignment.LEADING)
									.addGroup(gl_panel_1.createSequentialGroup()
										.addGap(25)
										.addComponent(lblBreakLengths)
										.addGap(84))
									.addGroup(Alignment.TRAILING, gl_panel_1.createSequentialGroup()
										.addPreferredGap(ComponentPlacement.RELATED, 65, Short.MAX_VALUE)
										.addGroup(gl_panel_1.createParallelGroup(Alignment.LEADING)
											.addGroup(gl_panel_1.createSequentialGroup()
												.addComponent(minBreakBox, GroupLayout.PREFERRED_SIZE, 54, GroupLayout.PREFERRED_SIZE)
												.addGap(5)
												.addComponent(label_1, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE))
											.addGroup(gl_panel_1.createSequentialGroup()
												.addGap(27)
												.addComponent(label_2, GroupLayout.PREFERRED_SIZE, 10, GroupLayout.PREFERRED_SIZE))
											.addGroup(gl_panel_1.createSequentialGroup()
												.addComponent(maxBreakBox, GroupLayout.PREFERRED_SIZE, 54, GroupLayout.PREFERRED_SIZE)
												.addGap(5)
												.addComponent(label_3, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)))
										.addGap(56))))
							.addGroup(Alignment.TRAILING, gl_panel_1.createSequentialGroup()
								.addComponent(randomBox)
								.addGap(23))))
			);
			gl_panel_1.setVerticalGroup(
				gl_panel_1.createParallelGroup(Alignment.LEADING)
					.addGroup(gl_panel_1.createSequentialGroup()
						.addGroup(gl_panel_1.createParallelGroup(Alignment.TRAILING)
							.addGroup(gl_panel_1.createSequentialGroup()
								.addContainerGap()
								.addComponent(separator, GroupLayout.PREFERRED_SIZE, 105, GroupLayout.PREFERRED_SIZE))
							.addGroup(Alignment.LEADING, gl_panel_1.createSequentialGroup()
								.addGap(16)
								.addGroup(gl_panel_1.createParallelGroup(Alignment.BASELINE)
									.addComponent(breakBox)
									.addComponent(randomBox))
								.addPreferredGap(ComponentPlacement.UNRELATED)
								.addGroup(gl_panel_1.createParallelGroup(Alignment.LEADING)
									.addGroup(gl_panel_1.createSequentialGroup()
										.addComponent(lblTimeBetweenBreaks)
										.addPreferredGap(ComponentPlacement.UNRELATED)
										.addGroup(gl_panel_1.createParallelGroup(Alignment.BASELINE)
											.addComponent(minTimeBeBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
											.addComponent(lblMins))
										.addPreferredGap(ComponentPlacement.UNRELATED)
										.addComponent(lblTo)
										.addPreferredGap(ComponentPlacement.UNRELATED)
										.addGroup(gl_panel_1.createParallelGroup(Alignment.BASELINE)
											.addComponent(maxTimeBeBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
											.addComponent(label, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE)))
									.addGroup(gl_panel_1.createSequentialGroup()
										.addComponent(lblBreakLengths)
										.addPreferredGap(ComponentPlacement.UNRELATED)
										.addGroup(gl_panel_1.createParallelGroup(Alignment.LEADING)
											.addComponent(minBreakBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
											.addGroup(gl_panel_1.createSequentialGroup()
												.addGap(2)
												.addComponent(label_1, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE)))
										.addGap(13)
										.addComponent(label_2, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE)
										.addGap(13)
										.addGroup(gl_panel_1.createParallelGroup(Alignment.LEADING)
											.addComponent(maxBreakBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
											.addGroup(gl_panel_1.createSequentialGroup()
												.addGap(2)
												.addComponent(label_3, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE)))))))
						.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
			);
			panel_1.setLayout(gl_panel_1);
			contentPane.setLayout(gl_contentPane);
			// LOAD SAVED SELECTION INFO
			try {
				String filename = getCacheDirectory() + "\\AaimistersLCookFishSettings.txt";
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
		        		coinBox.setSelected(true);
		        	} else {
		        		coinBox.setSelected(false);
		        	}
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
		        		 }
		        	 }
		        	 if (opts[1].equals("true")) {
			        		restBox.setSelected(true);
			        } else {
			        		restBox.setSelected(false);
			        }
		        	colorBox.setSelectedIndex(Integer.parseInt(opts[2]));
		        	methBox.setSelectedIndex(Integer.parseInt(opts[11]));
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
			        }
			        if (opts[6].equals("true")) {
			            randomBox.setSelected(true);
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
		private JComboBox methBox;
		private JCheckBox antibanBox;
		private JCheckBox paintBox;
		private JCheckBox restBox;
		private JCheckBox coinBox;
		private JCheckBox breakBox;
		private JCheckBox randomBox;
		private JSpinner maxTimeBeBox;
		private JSpinner minTimeBeBox;
		private JSpinner maxBreakBox;
		private JSpinner minBreakBox;
		private JButton submit;
	}
}
