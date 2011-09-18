import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import org.rsbot.event.events.MessageEvent;
import org.rsbot.event.listeners.MessageListener;
import org.rsbot.event.listeners.PaintListener;
import org.rsbot.gui.AccountManager;
import org.rsbot.script.Script;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.methods.Bank;
import org.rsbot.script.methods.GrandExchange;
import org.rsbot.script.methods.Skills;
import org.rsbot.script.util.Timer;
import org.rsbot.script.wrappers.RSArea;
import org.rsbot.script.wrappers.RSGroundItem;
import org.rsbot.script.wrappers.RSInterface;
import org.rsbot.script.wrappers.RSItem;
import org.rsbot.script.wrappers.RSNPC;
import org.rsbot.script.wrappers.RSObject;
import org.rsbot.script.wrappers.RSPath;
import org.rsbot.script.wrappers.RSTile;
import org.rsbot.script.wrappers.RSWeb;

@ScriptManifest(website = "http://goo.gl/WEQX6", authors = { "hlunnb" }, keywords = { "Woodcutting, Firemaking" }, name = "Dynamic Woodcutter", version = 1.65, description = "Independently trains Woodcutting and Firemaking from a low level.")
public class DynamicWoodcutter extends Script implements PaintListener, MouseListener, MessageListener {

	final RSArea adviserHouse = new RSArea(new RSTile(3229, 3236), new RSTile(3232, 3241));
	final RSArea bobsArea = new RSArea(new RSTile(3227, 3201), new RSTile(3233, 3205));
	final RSTile bobsTile = new RSTile(3230, 3203);
	final RSTile treeTile = new RSTile(3172, 3224);
	final RSTile oakTreeTile = new RSTile(3101, 3286);
	final RSTile oakTreeTile2 = new RSTile(3115, 3245);
	final RSTile oakTreeTile3 = new RSTile(3082, 3298);
	final RSTile willowTreeTile = new RSTile(2971, 3195); // Rimm
	final RSTile willowTreeTile2 = new RSTile(3165, 3272); // Lumb
	final RSTile willowTreeTile3 = new RSTile(3060, 3254); // Port Sarim
	final RSTile yewTreeTile = new RSTile(3166, 3234); // Lumb
	final RSTile yewTreeTile2 = new RSTile(2930, 3229); // Rimm
	final RSTile yewTreeTile3 = new RSTile(3048, 3270); // Port Sarim
	final RSArea generalStoreArea = new RSArea(new RSTile(3210, 3238), new RSTile(3219, 3246));
	final RSTile generalStoreTile = new RSTile(3215, 3243);
	final RSArea generalStoreArea2 = new RSArea(new RSTile(2946, 3211), new RSTile(2950, 3218));
	final RSTile generalStoreTile2 = new RSTile(2948, 3215);
	final RSArea draynorBankArea = new RSArea(new RSTile(3092, 3240), new RSTile(3097, 3246));
	final RSTile draynorBankTile = new RSTile(3093, 3244);
	final RSTile[] toGe = { new RSTile(3095, 3261), new RSTile(3094, 3266), new RSTile(3093, 3272), new RSTile(3095, 3278),
	        new RSTile(3095, 3283), new RSTile(3095, 3288), new RSTile(3089, 3291), new RSTile(3089, 3294),
	        new RSTile(3095, 3300), new RSTile(3098, 3304), new RSTile(3098, 3309), new RSTile(3102, 3311),
	        new RSTile(3108, 3311), new RSTile(3114, 3312), new RSTile(3118, 3315), new RSTile(3123, 3317),
	        new RSTile(3126, 3319), new RSTile(3131, 3324), new RSTile(3135, 3329), new RSTile(3140, 3333),
	        new RSTile(3144, 3333), new RSTile(3146, 3337), new RSTile(3146, 3342), new RSTile(3145, 3347),
	        new RSTile(3143, 3351), new RSTile(3141, 3356), new RSTile(3143, 3362), new RSTile(3140, 3367),
	        new RSTile(3139, 3372), new RSTile(3136, 3377) };
	final RSTile[] toGe2 = { new RSTile(3008, 3273), new RSTile(3008, 3274), new RSTile(3008, 3277), new RSTile(3008, 3280),
	        new RSTile(3008, 3284), new RSTile(3007, 3287), new RSTile(3007, 3291), new RSTile(3007, 3295),
	        new RSTile(3006, 3298), new RSTile(3006, 3301), new RSTile(3006, 3305), new RSTile(3010, 3310),
	        new RSTile(3010, 3312), new RSTile(3015, 3317), new RSTile(3018, 3317), new RSTile(3021, 3320),
	        new RSTile(3025, 3320), new RSTile(3029, 3320), new RSTile(3032, 3320), new RSTile(3035, 3319),
	        new RSTile(3038, 3320), new RSTile(3043, 3322), new RSTile(3046, 3322), new RSTile(3049, 3322),
	        new RSTile(3053, 3323), new RSTile(3058, 3321), new RSTile(3061, 3321), new RSTile(3066, 3325),
	        new RSTile(3067, 3330), new RSTile(3070, 3333), new RSTile(3072, 3337), new RSTile(3072, 3340),
	        new RSTile(3072, 3344), new RSTile(3072, 3348), new RSTile(3072, 3350), new RSTile(3072, 3354),
	        new RSTile(3072, 3357), new RSTile(3072, 3358), new RSTile(3072, 3363), new RSTile(3072, 3368),
	        new RSTile(3073, 3373), new RSTile(3073, 3377) };
	final RSTile[] willowsToBank = { new RSTile(2968, 3198), new RSTile(2972, 3199), new RSTile(2975, 3202),
	        new RSTile(2978, 3205), new RSTile(2982, 3206), new RSTile(2985, 3209), new RSTile(2989, 3211),
	        new RSTile(2992, 3214), new RSTile(2995, 3217), new RSTile(2998, 3220), new RSTile(3000, 3224),
	        new RSTile(3002, 3228), new RSTile(3003, 3232), new RSTile(3004, 3236), new RSTile(3006, 3240),
	        new RSTile(3010, 3242), new RSTile(3014, 3242), new RSTile(3018, 3242), new RSTile(3022, 3243),
	        new RSTile(3026, 3242), new RSTile(3030, 3242), new RSTile(3034, 3244), new RSTile(3036, 3248),
	        new RSTile(3039, 3251), new RSTile(3041, 3255), new RSTile(3044, 3259), new RSTile(3046, 3263),
	        new RSTile(3049, 3266), new RSTile(3053, 3268), new RSTile(3057, 3270), new RSTile(3060, 3273),
	        new RSTile(3064, 3274), new RSTile(3068, 3276), new RSTile(3072, 3275), new RSTile(3075, 3272),
	        new RSTile(3076, 3268), new RSTile(3076, 3264), new RSTile(3077, 3260), new RSTile(3079, 3256),
	        new RSTile(3082, 3253), new RSTile(3086, 3251), new RSTile(3089, 3248), new RSTile(3093, 3247),
	        new RSTile(3094, 3243) };
	RSPath willowsToBankPath;
	RSPath bankToWillowsPath;
	RSPath pathToGE;
	RSPath pathToGE2;
	final RSTile GETile = new RSTile(3153, 3480);
	final RSArea grandExchangeArea = new RSArea(new RSTile(3144, 3473), new RSTile(3155, 3483));
	final int bronzeHatchetID = 1351;
	final int ironHatchetID = 1349;
	final int steelHatchetID = 1353;
	final int blackHatchetID = 1361;
	final int mithrilHatchetID = 1355;
	final int adamantHatchetID = 1357;
	final int runeHatchetID = 1359;
	final int tinderboxID = 590;
	final int regularLogID = 1511;
	final int oakLogID = 1521;
	final int willowLogID = 1519;
	final int yewLogID = 1515;
	final int notedOakLogs = 1522;
	final int[] logID = { regularLogID, oakLogID, willowLogID, yewLogID };
	final int[] dontDropIDs = { regularLogID, oakLogID, willowLogID, yewLogID, 995, bronzeHatchetID, ironHatchetID,
	        steelHatchetID, blackHatchetID, mithrilHatchetID, adamantHatchetID, runeHatchetID, notedOakLogs, 13439,
	        tinderboxID, 14664 }; // Random event lamp 14664, Starter lamp 13439
	final int[] dontDepositIDs = { bronzeHatchetID, ironHatchetID, steelHatchetID, blackHatchetID, mithrilHatchetID,
	        adamantHatchetID, runeHatchetID, 995, 14664, tinderboxID };
	
	final RSTile[] logStart = { new RSTile(3199, 3243), new RSTile(3199, 3244), new RSTile(3199, 3245),
	        new RSTile(3199, 3246), new RSTile(3196, 3237), new RSTile(3202, 3236) };
	final RSTile[] oakStart = { new RSTile(3093, 3288), new RSTile(3093, 3290), new RSTile(3093, 3289),
	        new RSTile(3106, 3273), new RSTile(3118, 3263) };
	final RSTile[] willowStartRimm = { new RSTile(2968, 3194), new RSTile(2968, 3193), new RSTile(2968, 3192),
	        new RSTile(2968, 3199), new RSTile(2968, 3200) };
	final RSTile[] willowStartLumb = { new RSTile(3199, 3243), new RSTile(3199, 3244), new RSTile(3199, 3245),
	        new RSTile(3199, 3246), new RSTile(3183, 3275), new RSTile(3183, 3276) };
	final RSTile[] willowStartPort = { new RSTile(3061, 3253), new RSTile(3061, 3254), new RSTile(3068, 3274),
	        new RSTile(3068, 3275), new RSTile(3068, 3276) };
	RSTile[] start;

	final int[] treeID = { 38782, 38783, 38784, 38785, 38786, 38787, 38788, 38760 };
	final int[] oakID = { 38731, 38732 };
	final int[] willowID = { 38616, 38627, 38616 };
	final int[] yewID = { 38755 };

	int runeHatchetPrice = -1;
	int run = random(5, 30);
	int totalCash = 0;
	int bankCash = 0;
	int oakCash = 0;
	int oakPrice = -1;
	int trips = 0;
	int maxTrips = random(5, 10);

	boolean checkBank = false;
	boolean useBank = false;
	boolean needTutoring = false;
	String status = "";
	String antiBan = "";
	boolean collectFromSlots1 = false;
	boolean collectFromSlots2 = false;
	boolean done = false;
	boolean offeredOaks = false;
	boolean sawLamp = false;

	boolean burnLogs = false;
	boolean trainFM = false;
	RSItem dontClick = null;
	RSTile dest = null;

	boolean end = false;
	boolean sentOffer = false;
	boolean showPaint = false;
	boolean showNotice = false;
	boolean checkedGE = false;
	long startTime;
	long timer;
	long timer2;
	long clickTimer;
	int levelsGained = 0;
	int levelsGained2 = 0;
	int initialXP = -1;
	int initialXP2 = -1;
	boolean wasLoggedOut = false;

	private double scriptVersion = DynamicWoodcutter.class.getAnnotation(ScriptManifest.class).version();
	private double currVer;

	final Color color1 = new Color(0, 255, 100); // highlight
	final Color color2 = new Color(51, 255, 51, 130); // back
	final Color color3 = new Color(51, 255, 51, 200);
	final Color color4 = new Color(255, 51, 51, 150); // red
	final Color color5 = new Color(255, 255, 255, 69); // White
	final Color color6 = new Color(0, 0, 0); // Black
	final BasicStroke stroke1 = new BasicStroke(1);
	final Font font1 = new Font("CordiaUPC", 0, 24);
	final Font font2 = new Font("Arial", 0, 9);
	final Font font3 = new Font("Arial", 0, 12);
	int r = 0;
	int b = 0;
	int g = 0;
	final Color MOUSE_BORDER_COLOR = new Color(225, 200, 25);
	final Color MOUSE_COLOR = new Color(132, 198, 99);
	String dots = "";
	final Timer t = new Timer(200);
	
	GUI frame;
	boolean guiWait = true;
	int fails = 0;

	enum State {
		BUYHATCHET, WC, TREE, OAK, WILLOWRIMM, WILLOWLUMB, WILLOWPORT, BUYRUNEHATCHET, CHECKBANK, GETUTOR, LAMPS, BURN, BUYTINDERBOX, YEW, error
	}

	@Override
	public boolean onStart() {
		mouse.setSpeed(5);
		currVer = getCurrentVersion();
		if (currVer > scriptVersion) {
			int n;
			int o = 1;
			n = JOptionPane.showConfirmDialog(null, "A new version of this script is available!\n"
			        + "Would you like to download new version?", "Update available", JOptionPane.YES_NO_OPTION);
			System.out.println(n); // yes 0, no 1.
			if (n == 0) {
				o = JOptionPane.showConfirmDialog(null, "You will now be sent to powerbot.org\n"
				        + "to download the latest version.", "Update available", JOptionPane.OK_CANCEL_OPTION);
			}
			if (o == 0)
				sendToURL("http://goo.gl/WEQX6");

		} else {
			log(Color.GREEN, "The script is up to date!");
		}

		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					frame = new GUI();
					frame.setVisible(true);
				}
			});
		} catch (Throwable e) {}
		return true;
	}

	@Override
	public int loop() {
		int check = checkStuff();
		if (check != 69)
			return check;
		switch (getState()) {
			case BUYTINDERBOX:
				if (inventory.isFull()) {
					RSItem[] inv = inventory.getItems();
					RSItem log;
					for (RSItem r : inv) {
						if (r.getID() == 1519 || r.getID() == 1521 || r.getID() == 1511 || r.getID() == 1515) {
							log = r;
							inventory.dropItem(log);
							break;
						}
					}
				}
				if (!generalStoreArea.contains(myLocation())) {
					webWalk(generalStoreTile);
					return random(500, 1000);
				}
				if (generalStoreArea.contains(myLocation())) {
					if (interfaces.get(620).isValid()) {
						interfaces.get(620).getComponent(26).getComponent(0).interact("Take 1");
						return 1000;
					} else {
						RSNPC shopKeeper = npcs.getNearest(521, 520);
						if (shopKeeper != null) {
							if (shopKeeper.isOnScreen()) {
								shopKeeper.interact("Trade Shop");
								chill();
							} else {
								camera.turnTo(shopKeeper);
							}
							return 1000;
						}
					}
				}
				break;
			case BURN:
				if (getMyPlayer().isInCombat() || (inventory.getCount(1521, 1511, 1519, 1515) == 0 && isIdle())) {
					burnLogs = false;
				}
				if (dest != null)
					return walkDest();
				if (!isSelected()) {
					if (clickLog()) {
						return random(50, 100);
					}
					return 0;
				}
				if (isIdle()) {
					if (isTileFree(myLocation())) {
						if (inventory.getCount(1521, 1511, 1519, 1515) == 1) {
							burnLog();
							return random(1600, 1700);
						}
						burnLog();
					} else if (inventory.getCount(1521, 1511, 1519, 1515) < 4) {
						burnLogs = false;
					} else {
						findNewTile(start, false);
					}
					return 0;
				}
				break;
			case GETUTOR:
				if (interfaces.canContinue()) {
					if (interfaces.get(241).getComponent(4).containsText("Good luck")) {
						needTutoring = false;
					}
					interfaces.clickContinue();
					return random(1000, 2000);
				}
				if (interfaces.get(230).isValid()) {
					interfaces.get(230).getComponent(2).doClick();
					return random(500, 1000);
				}
				RSNPC tutor = npcs.getNearest("Grand Exchange Tutor");
				if (tutor.isOnScreen()) {
					tutor.interact("Talk-to");
					chill();
					return 500;
				} else {
					walking.walkTileMM(tutor.getLocation());
					return random(500, 1500);
				}
			case CHECKBANK:
				return useDraynorBank();
			case BUYRUNEHATCHET:
				if (!grandExchangeArea.contains(myLocation()))
					return walkToGE();
				if ((inventory.contains(995) && inventory.getItem(995).getStackSize() < runeHatchetPrice * 1.1 && !sentOffer)
				        || (!inventory.contains(995) && !sentOffer)) {
					if (!offeredOaks) {
						if (!bank.isOpen() && !inventory.contains(1522)) {
							RSNPC i = npcs.getNearest(BANKERS);
							mouse.move(i.getPoint());
							sleep(100, 200);
							mouse.click(false);
							if (menu.clickIndex(menu.getIndex("Bank") + 1)) {
								return random(1500, 2000);
							}
						} else if (bank.isOpen()) {
							if (bank.getItem(runeHatchetID) != null && !inventory.contains(runeHatchetID)) {
								bank.withdraw(runeHatchetID, 1);
								return 2000;
							}
							if (interfaces.getComponent(762, Bank.INTERFACE_BANK_BUTTON_NOTE).getTextureID() == 1431) {
								interfaces.getComponent(762, Bank.INTERFACE_BANK_BUTTON_NOTE).interact(
								    "Switch to note withdrawal mode");
							} else {
								if (bank.getItem(995) != null) {
									bank.withdraw(995, 30000);
								}
								if (bank.getItem(oakLogID) != null) {
									bank.withdraw(oakLogID, 0);
								}
								bank.close();
							}
							return random(1200, 1500);
						}
					}
				}
				if (bankCollectIsOpen()) {
					bankCollectClose();
				}
				if (!isOpen()) {
					open();
				}
				if (isOpen()) {
					int t = getEmptySlot();
					if (t > 0) {
						if (inventory.contains(1522) && getAllEmptySlots() > 0 && !offeredOaks) {
							sell("Oak logs", t, inventory.getItem(1522).getStackSize(), (int) (oakPrice * 0.8));
						}
						if (inventory.contains(995) && !sentOffer) {
							if (inventory.getItem(995).getStackSize() >= runeHatchetPrice * 1.1) {
								buy("Rune hatchet", t, 1, (int) (runeHatchetPrice * 1.1));
							}
						}
						for (int i = 1; i <= getTotalSlots();) {
							mouse.moveSlightly();
							sleep(random(700, 1000));
							if (isAnOfferCompleted()) {
								log("Offer completed");
								if (isOpen()) {
									close();
								}
								if (!bankCollectIsOpen()) {
									bankCollectOpen();
								}
								if (bankCollectIsOpen()) {
									bankCollectAll();
								}
							}
							i++;
						}
					} else {
						for (int i = 1; i <= getTotalSlots();) {
							mouse.moveSlightly();
							sleep(random(700, 1000));
							if (isAnOfferCompleted()) {
								log("Offer completed");
								if (isOpen()) {
									close();
								}
								if (!bankCollectIsOpen()) {
									bankCollectOpen();
								}
								if (bankCollectIsOpen()) {
									bankCollectAll();
								}
							}
							i++;
						}
					}
				}
				return 200;
			case BUYHATCHET:
				if (adviserHouse.contains(myLocation())) {
					RSObject door1 = objects.getTopAt(new RSTile(3228, 3240));
					if (door1 != null) {
						if (door1.getID() == 45476) {
							if (door1.isOnScreen()) {
								door1.interact("Open");
							} else {
								walking.walkTileMM(door1.getLocation());
							}
							chill();
							return 0;
						}
					}
					walking.walkTileMM(bobsTile);
				}
				if (bobsArea.contains(myLocation())) {
					if (interfaces.get(620).isValid()) {
						if (!inventory.contains(bronzeHatchetID)) {
							interfaces.get(620).getComponent(26).getComponent(4).interact("Take 1");
							return 1000;
						}
						if (!inventory.contains(steelHatchetID)) {
							store.getItem(steelHatchetID).interact("Buy 1");
							return 1000;
						}
					} else {
						RSNPC bob = npcs.getNearest(519);
						if (bob != null) {
							if (bob.isOnScreen()) {
								bob.interact("Trade Bob");
							} else {
								camera.turnTo(bob);
							}
							return 1000;
						}
					}
				}
				if (!bobsArea.contains(myLocation())) {
					RSObject door = objects.getTopAt(new RSTile(3234, 3203, 0));
					if (door != null) {
						if (door.getID() == 45476) {
							if (door.isOnScreen()) {
								door.interact("Open");
							} else {
								walking.walkTileMM(door.getLocation());
							}
							chill();
							return 0;
						}
					}
					webWalk(bobsTile);
					return random(500, 1000);
				}
				break;
			case TREE:
				if (store.isOpen()) {
					if ("Bob's Brilliant Axes".contains(interfaces.get(620).getComponent(20).getText())) {
						store.close();
						return 1000;
					}
					if (inventory.contains("Logs")) {
						inventory.getItem("Logs").interact("Sell 50");
						return 1500;
					}
				}
				if (inventory.isFull()) {
					if (trainFM) {
						start = logStart;
						burnLogs = true;
						findNewTile(start, false);
						return 0;
					}
					if (useBank) {
						checkBank = true;
					}
					if (!generalStoreArea.contains(myLocation())) {
						webWalk(generalStoreTile);
						return random(500, 1000);
					}
					RSNPC shopKeeper = npcs.getNearest(521, 520);
					if (shopKeeper != null) {
						if (shopKeeper.isOnScreen()) {
							shopKeeper.interact("Trade Shop");
							chill();
						} else {
							camera.turnTo(shopKeeper);
						}
						return 0;
					}
				}
				int h = handleCoins();
				if (h != -1) {
					return h;
				}
				return chopTree(35, 25, treeTile, "Tree", treeID);
			case OAK:
				if (inventory.isFull()) {
					if (trainFM) {
						start = oakStart;
						burnLogs = true;
						findNewTile(start, false);
						return 0;
					}
					checkBank = true;
					return 0;
				}
				if (oakLocation == 0) {
					return chopTree(20, 10, oakTreeTile, "Oak", oakID);
				} else if (oakLocation == 1) {
					return chopTree(30, 20, oakTreeTile2, "Oak", oakID);
				} else if (oakLocation == 2) {
					return chopTree(15, 6, oakTreeTile3, "Oak", oakID);
				}
				break;
			case WILLOWLUMB:
				if (inventory.isFull()) {
					if (store.isOpen()) {
						return useStore();
					}
					if (trainFM) {
						start = willowStartLumb;
						burnLogs = true;
						findNewTile(start, false);
						return 0;
					}
					if (useBank) {
						checkBank = true;
						return 0;
					}
					if (!generalStoreArea.contains(myLocation())) {
						webWalk(generalStoreTile);
						return random(500, 1000);
					}
					RSNPC shopKeeper = npcs.getNearest(521, 520);
					if (shopKeeper != null) {
						if (shopKeeper.isOnScreen()) {
							shopKeeper.interact("Trade Shop");
							chill();
							return 0;
						} else {
							camera.turnTo(shopKeeper);
						}
					}
				}
				return chopTree(30, 20, willowTreeTile2, "Willow", willowID);
			case WILLOWRIMM:
				if (inventory.isFull()) {
					if (store.isOpen()) {
						return useStore();
					}
					if (trainFM) {
						start = willowStartRimm;
						burnLogs = true;
						findNewTile(start, false);
						return 0;
					}
					if (useBank) {
						checkBank = true;
						useBank = false;
						return 0;
					}
					if (!generalStoreArea2.contains(myLocation())) {
						webWalk(generalStoreTile2);
						return random(500, 1000);
					}
					RSNPC shopKeeper = npcs.getNearest(530, 531);
					if (shopKeeper != null) {
						if (shopKeeper.isOnScreen()) {
							shopKeeper.interact("Trade Shop");
							chill();
						} else {
							camera.turnTo(shopKeeper);
						}
					}
					return 0;
				}
				if (calc.distanceTo(draynorBankTile) < 40 && inventory.getCount(dontDropIDs) > 10) {
					checkBank = true;
					return 0;
				}
				if (calc.distanceTo(willowTreeTile) > 20) {
					bankToWillowsPath = walking.newTilePath(willowsToBank).reverse();
					if (bankToWillowsPath.isValid()) {
						bankToWillowsPath.traverse();
						return random(500, 2700);
					}
					webWalk(willowTreeTile);
					return random(500, 2000);
				}
				return chopTree(20, 10, willowTreeTile, "Willow", willowID);
			case WILLOWPORT:
				if (inventory.isFull()) {
					if (trainFM) {
						start = willowStartPort;
						burnLogs = true;
						findNewTile(start, true);
						return 0;
					}
					checkBank = true;
					return 0;
				}
				return chopTree(25, 15, willowTreeTile3, "Willow", willowID);
			case YEW:
				useBank = true; // Always bank Yew logs.
				if (inventory.isFull()) {
					checkBank = true;
					return 0;
				}
				if (yewLocation == 0) {
					return chopTree(45, 35, yewTreeTile, "Yew", yewID);
				} else if (yewLocation == 1) {
					return chopTree(25, 15, yewTreeTile2, "Yew", yewID);
				} else if (yewLocation == 2) {
					return chopTree(25, 15, yewTreeTile3, "Yew", yewID);
				}
				break;
			case LAMPS:
				RSObject downLadder = objects.getNearest(37683);
				RSObject door = objects.getTopAt(new RSTile(3228, 3240));
				RSNPC sirVant = npcs.getNearest(7942);
				RSItem lamp = inventory.getItem(13439);
				if (sawLamp && lamp == null) {
					RSObject upLadder = objects.getNearest(37684);
					if (upLadder != null) {
						if (upLadder.isOnScreen()) {
							upLadder.interact("Climb-up");
						} else {
							walking.walkTileMM(upLadder.getLocation());
						}
						chill();
						return 1000;
					}
				}
				if (interfaces.get(1139).isValid()) {
					interfaces.get(1139).getComponent(17).interact("Select");
					sleep(50, 200);
					interfaces.get(1139).getComponent(2).interact("Confirm");
					return 1000;
				}
				if (lamp != null) {
					sawLamp = true;
					lamp.interact("Rub");
					return 1500;
				}
				if (interfaces.get(243).isValid()) {
					if (interfaces.get(243).getComponent(4).containsText("much for the offer")) {
						sawLamp = true;
						return 0;
					}
				}
				if (interfaces.get(228).isValid()) {
					if (interfaces.get(228).getComponent(2).containsText("I could kill the")) {
						interfaces.get(228).getComponent(2).doClick();
						return 1000;
					}
				}
				if (interfaces.canContinue()) {
					interfaces.clickContinue();
					return random(800, 1900);
				}
				if (sirVant != null) {
					if (calc.distanceTo(sirVant) < 10) {
						if (sirVant.isOnScreen()) {
							sirVant.interact("Talk-to");
						} else {
							walking.walkTileMM(sirVant.getLocation());
						}
						chill();
						return 1000;
					}
				}
				if (!adviserHouse.contains(myLocation())) {
					if (door != null) {
						if (door.getID() == 45476) { // Closed
							if (door.isOnScreen()) {
								door.interact("Open");
								chill();
								return 1000;
							} else {
								walking.walkTileMM(door.getLocation());
								return random(500, 1700);
							}
						}
					}
					webWalk(new RSTile(3230, 3240, 0));
					return random(500, 1000);
				}
				if (adviserHouse.contains(myLocation())) {
					if (downLadder != null) {
						if (downLadder.isOnScreen()) {
							downLadder.interact("Climb-down");
						} else {
							walking.walkTileOnScreen(downLadder.getLocation());
						}
						chill();
						return 1000;
					}
				}
				break;
		} // end of switch
		return random(300, 500);
	}

	private int walkToGE() {
		pathToGE = walking.newTilePath(toGe);
		pathToGE2 = walking.newTilePath(toGe2);
		if (calc.distanceTo(pathToGE.getEnd()) > 25 && pathToGE.isValid()) {
			pathToGE.traverse();
			return random(500, 2700);
		}
		if (calc.distanceTo(pathToGE2.getEnd()) > 25 && pathToGE2.isValid()) {
			pathToGE2.traverse();
			return random(500, 2700);
		}
		webWalk(GETile);
		return random(300, 1500);
	}

	private int useStore() {
		for (int i : logID) {
			if (inventory.contains(i)) {
				if (inventory.getItem(i).interact("Sell 50"))
					return random(1000, 1500);
			}
		}
		return 0;
	}

	private int checkStuff() {
		if (!checkedGE) {
			try {
				oakPrice = grandExchange.lookup("Oak logs").getGuidePrice();
				runeHatchetPrice = grandExchange.lookup("Rune hatchet").getGuidePrice();
			} catch (Exception e) {
				oakPrice = 30; // Set lower.
				runeHatchetPrice = 10000; // Set higher.
				log.severe("Could not get Grand Exchange prices, script may not function properly.");
			}
			if (oakPrice != -1 && runeHatchetPrice != -1) {
				log("Rune hatchet: " + runeHatchetPrice + ", " + "Oak price: " + oakPrice);
				checkedGE = true;
			}
		}
		if (initialXP == -1) {
			if (game.getClientState() != 11) {
				wasLoggedOut = true;
				return 500;
			}
			if (game.getClientState() == 11) {
				if (wasLoggedOut)
					sleep(3000);
				status = "";
				initialXP = skills.getCurrentExp(Skills.WOODCUTTING);
				initialXP2 = skills.getCurrentExp(Skills.FIREMAKING);
				startTime = System.currentTimeMillis();
				showPaint = true;
				return 0;
			}
		}
		if (guiWait) {
			status = "Waiting" + dots;
			startTime = System.currentTimeMillis();
			manageDots();
			return 100;
		}
		if (end) {
			if (game.logout(false)) {
				stopScript(true);
			}
			return 1000;
		}
		totalCash = inventory.getItem(995) == null ? bankCash + oakCash : inventory.getItem(995).getStackSize()
		        + bankCash + oakCash;
		RSItem[] invent = inventory.getItems();
		for (RSItem r : invent) {
			if (r.hasAction("Eat")) {
				r.interact("Eat");
				sleep(700);
			}
			if (!r.hasAction("Drop")) {
				continue;
			}
			for (int h : dontDropIDs) {
				if (r.getName().contains("hatchet")) {
					continue;
				}
				if (r.getID() == h) {
					break;
				}
				// If all of dontDepositIDs have not been checked.
				if (h != dontDropIDs[dontDropIDs.length - 1]) {
					continue;
				}
				// If all of dontDepositIDs have been checked.
				r.interact("Drop");
			}
		}
		if (interfaces.get(149).isValid()) { // TODO Unnecessary now?
			log("Reading and closing level 10 solicitation message.");
			sleep(random(1000, 2000)); // "Read" it.
			if (!interfaces.get(149).getComponent(230).interact("Select")) {
				return 500;
			}
		}
		if (wcLvl() >= 6 && inventory.contains(bronzeHatchetID) && inventory.contains(steelHatchetID)) {
			if (store.isOpen())
				if (store.close())
					return 1000;
			inventory.getItem(bronzeHatchetID).interact("Drop Bronze hatchet");
			return 1000;
		}
		if (((wcLvl() >= 41 && inventory.contains(runeHatchetID))
		        || (wcLvl() >= 31 && inventory.contains(adamantHatchetID)) || (wcLvl() >= 21 && inventory
		    .contains(mithrilHatchetID)))
		        && (inventory.contains(bronzeHatchetID) || inventory.contains(steelHatchetID))) {
			if (store.isOpen())
				if (store.close())
					return 1000;
			inventory.getItem(bronzeHatchetID).interact("Drop Bronze hatchet");
			inventory.getItem(steelHatchetID).interact("Drop Steel hatchet");
		}
		if (!walking.isRunEnabled() && run < walking.getEnergy()) {
			if (!interfaces.getComponent(walking.INTERFACE_RUN_ORB, 0).interact("Turn run mode on")) {
				mouse.moveRandomly(200);
			}
			run = random(5, 30);
		}
		if (myLocation().getZ() > 0) {
			log("How did you get up there?");
			RSObject[] allObjects = objects.getAll();
			for (RSObject o : allObjects) {
				if (o.hasAction("Climb-down")) {
					if (!o.isOnScreen()) {
						walking.walkTileMM(o.getLocation());
						chill();
					}
					if (o.interact("Climb-down")) {
						log("Climbing down...");
						chill();
						return 1000;
					}
				}
			}
		}
		return 69;
	}

	private void manageDots() {
		if (!t.isRunning()) {
			t.reset();
			if (dots.length() >= 3) {
				dots = "";
			} else {
				dots = dots + ".";
			}
		}
    }

	@Override
	public void mouseEntered(MouseEvent arg0) {
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
	}

	@Override
	public void onFinish() {
		log(Color.BLUE, "Thanks for using my script, please leave your feedback on my thread.");
		frame.dispose();
	}

	public void webWalk(RSTile dest) {
		RSWeb walkWeb = null;
		try {
			walkWeb = web.getWeb(dest);
		} catch (SecurityException e) {
			log("SecurityException.");
		}
		if (bobsArea.contains(myLocation())) {
			RSObject door = objects.getTopAt(new RSTile(3234, 3203, 0));
			if (door != null) {
				if (door.getID() == 45476) {
					if (door.isOnScreen()) {
						door.interact("Open");
					} else {
						walking.walkTileMM(door.getLocation());
					}
					chill();
				}
			}
		}
		if (adviserHouse.contains(myLocation())) {
			RSObject door1 = objects.getTopAt(new RSTile(3228, 3240));
			if (door1 != null) {
				if (door1.getID() == 45476) {
					if (door1.isOnScreen()) {
						door1.interact("Open");
					} else {
						walking.walkTileMM(door1.getLocation());
					}
					chill();
				}
			}
		}
		if (walkWeb != null) {
			if (!getMyPlayer().isMoving() || calc.distanceTo(walking.getDestination()) < 5) {
				walkWeb.step();
			}
		} else if (objects.getNearest(45476) != null && calc.distanceTo(objects.getNearest(45476)) < 7) {
			log("Possibly trapped. Opening door");
			RSObject door = objects.getNearest(45476);
			if (door.isOnScreen()) {
				if (door.interact("Open")) {
					chill();
					walking.walkTileMM(dest);
				}
			} else {
				walking.walkTileMM(door.getLocation());
			}
			chill();
		} else {
			log("Web is null, using backup walking method");
			walking.walkTileMM(dest);
			chill();
		}
	}

	public RSTile myLocation() {
		return getMyPlayer().getLocation();
	}

	public int wcLvl() {
		return skills.getCurrentLevel(Skills.WOODCUTTING);
	}

	public int fmLvl() {
		return skills.getCurrentLevel(Skills.FIREMAKING);
	}

	public void chill() {
		sleep(1000);
		while (getMyPlayer().isMoving()) {
			sleep(100);
			if (store.isOpen())
				break;
		}
	}

	public int useDraynorBank() {
		if (!draynorBankArea.contains(myLocation())) {
			willowsToBankPath = walking.newTilePath(willowsToBank);
			if (willowsToBankPath.isValid()) {
				willowsToBankPath.traverse();
				return random(500, 2700);
			}
			webWalk(draynorBankTile);
			return random(500, 1000);
		}
		if (draynorBankArea.contains(myLocation())) {
			if (!bank.isOpen()) {
				bank.open();
				chill();
				return 1000;
			} else {
				if (bank.depositAll()) {
					sleep(random(1500, 2000));
				} else {
					return 0;
				}
				RSItem[] bankItems = bank.getItems();
				for (RSItem r : bankItems) {
					String name = r.getName();
					if (name.contains("Bronze hatchet")) {
						hasBronzeHatchet = true;
					}
					if (name.contains("Iron hatchet")) {
						hasIronHatchet = true;
					}
					if (name.contains("Steel hatchet")) {
						hasSteelHatchet = true;
					}
					if (name.contains("Black hatchet")) {
						hasBlackHatchet = true;
					}
					if (name.contains("Mithril hatchet")) {
						hasMithrilHatchet = true;
					}
					if (name.contains("Adamant hatchet")) {
						hasAdamantHatchet = true;
					}
					if (name.contains("Rune hatchet")) {
						hasRuneHatchet = true;
					}
				}
				bestHatchetAvailable = bestHatchetAvailable();
				if (bank.getItem("Coins") != null) {
					bankCash = bank.getItem("Coins").getStackSize();
				} else {
					bankCash = 0;
				}
				if (bank.getItem("Oak logs") != null) {
					oakCash = bank.getItem("Oak logs").getStackSize() * oakPrice;
				}
				if (bestHatchetAvailable != -1 && bank.getItem(bestHatchetAvailable) != null
				        && !inventory.contains(bestHatchetAvailable)) {
					bank.withdraw(bestHatchetAvailable, 1);
					fails = 0;
					sleep(random(1500, 2000));
				} else if (inventory.contains(bestHatchetAvailable)) {
					sleep(random(500, 1000));
				} else if (fails > 3) {
					log("No hatchet available for your current level");
					end = true;
					return 0;
				} else {
					log("Bank fail count: " + ++fails);
					return random(1000, 2000);
				}
				if (trainFM && !inventory.contains(tinderboxID)) {
					bank.withdraw(tinderboxID, 1);
					sleep(random(1500, 2000));
				}
				if ((inventory.contains(bestHatchetAvailable) && bestHatchetAvailable != -1)
				        && (!trainFM || (trainFM && inventory.contains(tinderboxID)))) {
					checkBank = false;
					checkedBank = true;
					bank.close();
				}
			}
		}
		return 0;
	}

	/**
	 * @author hlunnb
	 * @param farDist The distance away from the centre where web walking is used to get back.
	 * @param closeDist The distance away from the centre where it will click the centre and wait until it walks back.
	 * @param t The centre tile.
	 * @param treeName The name of the tree. "Willow"
	 * @param treeID An array tree IDs
	 * @return A number representing the amount of time to sleep based on the action completed. Should return this value
	 *         in loop.
	 */
	public int chopTree(int farDist, int closeDist, RSTile t, String treeName, int[] treeID) {
		if (isAnimated()) {
			antiBan();
		}
		if (calc.distanceTo(t) > farDist) {
			webWalk(t);
			return random(800, 1500);
		}
		if (calc.distanceTo(t) > closeDist) {
			walking.walkTileMM(t);
			chill();
		}
		if (calc.distanceTo(t) <= closeDist) {
			RSObject tree = getNearestTree(treeID, t, closeDist);
			if (tree != null) {
				if ((treeID != oakID && treeID != yewID && calc.distanceTo(tree) > 1)
				        || ((treeID == oakID || treeID == yewID) && calc.distanceTo(tree) > 2)) {
					if (tree.isOnScreen()) {
						if (tree.interact("Chop down " + treeName)) {
							chill();
							return 0;
						} else {
							moveCamera(tree);
							return 500;
						}
					} else {
						walking.walkTileMM(tree.getLocation());
						return random(800, 1500);
					}
				}
				if (!isAnimated()) {
					tree = getNearestTree(treeID, t, closeDist);
					if (tree.isOnScreen()) {
						if (tree.interact("Chop down " + treeName)) {
							chill();
							return 0;
						} else {
							moveCamera(tree);
							return 500;
						}
					} else {
						walking.walkTileMM(tree.getLocation());
						return random(800, 1500);
					}
				}
			}
		}
		return 0;
	}

	/**
	 * Returns the <tt>RSObject</tt> that is nearest and a within certain distance away a centre tile.
	 * 
	 * @param treeID The array of tree ids.
	 * @param t The centre tile.
	 * @param closeDist The maximum distance from the centre tile.
	 * @return An <tt>RSObject</tt> representing the nearest tree within the approved distance or null if a tree cannot
	 *         be found.
	 */
	private RSObject getNearestTree(int[] treeID, RSTile t, int closeDist) {
		RSObject tree = null;
		RSObject tree2;
		int dist = 100000000;
		for (int i : treeID) { // Nearest tree with one of treeIDs within cl
			tree2 = objects.getNearest(i);
			if (tree2 == null) {
				continue;
			}
			if (calc.distanceBetween(t, tree2.getLocation()) >= closeDist) {
				continue;
			}
			if (calc.distanceTo(tree2.getLocation()) < dist) {
				dist = calc.distanceTo(tree2.getLocation());
				tree = objects.getNearest(i);
			}
		}
		return tree;
	}

	public void burnLog() {
		if (isSelected()) {
			if (timer2 < System.currentTimeMillis()) {
				if (clickTin()) {
					timer2 = System.currentTimeMillis() + random(1600, 1700);
				}
			} else {
				hoverTin();
			}
		}
	}

	public void hoverTin() {
		RSItem tin = inventory.getItem(tinderboxID);
		tin.getComponent().doHover();
	}

	public boolean clickTin() {
		RSItem tin = inventory.getItem(tinderboxID);
		if (tin != null) {
			if (tin.interact("Use")) {
				return true;
			}
		}
		return false;
	}

	public boolean clickLog() {
		RSItem[] logs = inventory.getItems(false);
		RSItem log = null;
		for (RSItem l : logs) {
			if (l == null) {
				continue;
			}
			if (dontClick != null) {
				if (l.getComponent().equals(dontClick.getComponent())) {
					continue;
				}
			}
			if (!canBurn(l)) {
				inventory.dropItem(l);
				sleep(random(500, 750));
				continue;
			}
			if (l.getID() == 1519 || l.getID() == 1511 || l.getID() == 1521 || l.getID() == 1515) {
				log = l;
				break;
			}
		}
		if (log != null) {
			if (log.interact("Use")) {
				if (inventory.getCount(1519, 1511, 1521, 1515) > 1) {
					dontClick = log;
				}
				return true;
			}
		}
		return false;
	}

	private boolean canBurn(RSItem l) {
		if (l.getID() == 1515) {// oak-1521, tree-1511, will-1519, yew-1515
			return fmLvl() >= 60 ? true : false;
		}
		if (l.getID() == 1519) {
			return fmLvl() >= 30 ? true : false;
		}
		if (l.getID() == 1521) {
			return fmLvl() >= 15 ? true : false;
		}
		if (l.getID() == 1511) {
			return fmLvl() >= 1 ? true : false;
		}
		return true; // If the item doesn't have a log ID.
	}

	/**
	 * 
	 * @return True, if the player isn't moving and has no animation.
	 */
	public boolean isIdle() {
		return !isAnimated() && !getMyPlayer().isMoving();
	}

	public boolean isSelected() {
		RSItem inv = inventory.getSelectedItem();
		if (inv != null) {
			int id = inv.getID();
			return id == 1519 || id == 1511 || id == 1521 || id == 1515;
		}
		return false;
	}

	public boolean isTileFree(RSTile t) { // TODO Can you Firemake on any object?
		RSObject[] objs = objects.getAllAt(t);
		if (objs.length == 0) {
			return true;
		}
		for (RSObject r : objs) {
			if (r.getID() == 2732) {
				break;
			}
			if (r == objs[objs.length - 1]) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param r An array of starting tiles
	 * @param p Use priority method
	 */
	public void findNewTile(RSTile[] r, boolean p) {
		if (!p) {
			int dist = 1000000000;
			for (RSTile t : start) { // Closest method
				if (isTileFree(t)) {
					if (calc.distanceTo(t) < dist) {
						dest = t;
						dist = calc.distanceTo(t);
					}
				}
			}
		}
		if (p) {
			for (RSTile t : start) { // List priority method
				if (isTileFree(t)) {
					dest = t;
					break;
				}
			}
		}
		if (dest == null) {
			log("Cannot find starting tile");
		}
	}

	public int walkDest() {
		if (myLocation().equals(dest) && !getMyPlayer().isMoving()) {
			dest = null;
			return 0;
		}
		if (calc.tileOnScreen(dest) && menu.getIndex("Walk here") != -1) {
			sleep(500);
			tiles.interact(dest, "Walk here");
		} else {
			webWalk(dest);
		}
		return random(700, 2400);
	}

	public int take(RSGroundItem g) {
		if (calc.tileOnScreen(g.getLocation())) {
			if (g.interact("Take " + g.getItem().getName())) {
				chill();
			} else {
				antiBan();
			}
		} else if (!getMyPlayer().isMoving()) {
			walking.walkTileMM(g.getLocation());
			antiBan();
			return random(500, 2000);
		}
		return 0;
	}

	public int handleCoins() {
		if (bestHatchetAvailable == -1 || bestHatchetAvailable == bronzeHatchetID) {
			RSGroundItem coins = groundItems.getNearest("Coins");
			if ((coins != null && calc.distanceTo(coins.getLocation()) < 20)
			        && ((inventory.getItem("Coins") == null) || (inventory.getItem("Coins") != null && inventory
			            .getItem("Coins").getStackSize() < 200))) {
				return take(coins);
			}
		}
		return -1;
	}

	/**
	 * @return The best hatchet available at the players current level.
	 */
	public int bestHatchetAvailable() {
		int wcLvl = wcLvl();
		if (hasRuneHatchet || inventory.contains(runeHatchetID)) {
			if (wcLvl >= 41) {
				return runeHatchetID;
			}
		}
		if (hasAdamantHatchet || inventory.contains(adamantHatchetID)) {
			if (wcLvl >= 31) {
				return adamantHatchetID;
			}
		}
		if (hasMithrilHatchet || inventory.contains(mithrilHatchetID)) {
			if (wcLvl >= 21) {
				return mithrilHatchetID;
			}
		}
		if (hasBlackHatchet || inventory.contains(blackHatchetID)) {
			if (wcLvl >= 6) {
				return blackHatchetID;
			}
		}
		if (hasSteelHatchet || inventory.contains(steelHatchetID)) {
			if (wcLvl >= 6) {
				return steelHatchetID;
			}
		}
		if (hasIronHatchet || inventory.contains(ironHatchetID)) {
			return ironHatchetID;
		}
		if (hasBronzeHatchet || inventory.contains(bronzeHatchetID)) {
			return bronzeHatchetID;
		}
		return -1;
	}

	public boolean isAnimated() {
		return getMyPlayer().getAnimation() != -1;
	}

	public double getCurrentVersion() {
		try {
			BufferedReader r = new BufferedReader(new InputStreamReader(new URL(
			    "http://pastebin.com/raw.php?i=SePHdUFV").openStream()));
			double d = Double.parseDouble(r.readLine());
			r.close();
			return d;
		} catch (Exception e) {
			log.warning("Error checking for latest version.");
		}
		return scriptVersion;
	}

	public void sendToURL(String url) {
		try {
			java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
		} catch (Exception e) {
			System.out.println("Error:" + e.getLocalizedMessage());
		}
	}

	public void mousePressed(MouseEvent e) {
		Rectangle area = new Rectangle(482, 319, 34, 19);
		Rectangle area2 = new Rectangle(482, 300, 34, 19);
		Point a = e.getPoint();
		if (area.contains(a)) {
			showPaint = !showPaint;
		}
		if (area2.contains(a)) {
			useBank = !useBank;
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		Rectangle area = new Rectangle(482, 281, 34, 19);
		Point a = e.getPoint();
		if (area.contains(a))
			frame.setVisible(true);
	}

	public void onRepaint(Graphics g1) {
		Graphics2D g = (Graphics2D) g1;
		long millis = System.currentTimeMillis() - startTime;
		long hours = millis / (1000 * 60 * 60);
		millis -= hours * (1000 * 60 * 60);
		long minutes = millis / (1000 * 60);
		millis -= minutes * (1000 * 60);
		long seconds = millis / 1000;

		drawMouse(g);

		if (showPaint) {
			g.setColor(color2);
			g.fillRect(340, 242, 176, 96); // Green back
			g.setColor(color6);
			g.setStroke(stroke1);
			g.drawRect(340, 242, 176, 96);

			g.setColor(color4); // Red bar
			g.fillRect(340, 221, 176, 18);
			g.setColor(color6);
			g.drawRect(340, 221, 176, 18);

			g.setColor(color3); // Green bar
			g.fillRect(342, 223, skills.getPercentToNextLevel(Skills.WOODCUTTING), 15);

			g.setColor(color5); // White bar
			g.fillRect(341, 230, 175, 9);

			g.setFont(font3);
			g.setColor(color6);
			g.drawString(Integer.toString(wcLvl()) + " WC", 420, 235);

			if (trainFM) {
				g.setColor(color2);
				g.fillRect(340, 186, 176, 32); // Green back
				g.setColor(color6);
				g.setStroke(stroke1);
				g.drawRect(340, 186, 176, 32);

				g.setColor(color4); // Red bar
				g.fillRect(340, 165, 176, 18);
				g.setColor(color6);
				g.drawRect(340, 165, 176, 18);

				g.setColor(color3); // Green bar
				g.fillRect(342, 167, skills.getPercentToNextLevel(Skills.FIREMAKING), 15);

				g.setColor(color5); // White bar
				g.fillRect(341, 174, 175, 9);

				g.setFont(font3);
				g.setColor(color6);
				g.drawString(Integer.toString(fmLvl()) + " FM", 420, 179);

				g.drawString(levelsGained2 + " levels, " + (skills.getCurrentExp(Skills.FIREMAKING) - initialXP2)
				        + " xp", 345, 200);
				g.drawString((double) Math.round((skills.getCurrentExp(Skills.FIREMAKING) - initialXP2) * 3600D
				        / (System.currentTimeMillis() - startTime) * 10)
				        / 10
				        + "k xp/hr  "
				        + (double) Math.round((skills.getCurrentExp(Skills.FIREMAKING)
				                + skills.getCurrentExp(Skills.WOODCUTTING) - initialXP - initialXP2)
				                * 3600D / (System.currentTimeMillis() - startTime) * 10) / 10 + "k total xp/hr", 345,
				    215);
			}

			g.setFont(font1);
			g.setColor(color6);
			g.drawString("Dynamic Woodcutter", 346, 260);

			g.setFont(font3); // General text
			g.drawString("Status: " + status, 345, 275);
			if (totalCash > 1000000) {
				g.drawString("Available wealth: " + Integer.toString(totalCash / 1000000) + "m", 345, 305);
			} else if (totalCash > 1000) {
				g.drawString("Available wealth: " + Integer.toString(totalCash / 1000) + "k", 345, 305);
			} else {
				g.drawString("Available wealth: " + Integer.toString(totalCash), 345, 305);
			}
			g.drawString(levelsGained + " levels" + ", " + (skills.getCurrentExp(Skills.WOODCUTTING) - initialXP)
			        + " xp", 345, 320);
			g.drawString((double) Math.round((skills.getCurrentExp(Skills.WOODCUTTING) - initialXP) * 3600D
			        / (System.currentTimeMillis() - startTime) * 10)
			        / 10 + "k xp/hr", 345, 335);
			if (antiBan.length() > 0) {
				g.setFont(font2);
				g.setColor(color5);
				g.fillRect(315, 350, 200, g.getFontMetrics().stringWidth("Antiban: " + antiBan) + 2);
				g.setColor(color6);
				g.drawRect(315, 350, 200, g.getFontMetrics().stringWidth("Antiban: " + antiBan) + 2);
				g.drawString("Antiban: " + antiBan, 317, 360);
			}
			g.setFont(font3);
			g.drawString(hours + ":" + minutes + ":" + seconds, 345, 290);

			g.setColor(color6); // Show/Hide button
			g.drawRect(482, 319, 34, 19);

			if (!useBank) {
				g.setColor(color6);
				g.drawRect(482, 300, 34, 19);
			} else {
				g.setColor(color1); // Check bank button
				g.fillRect(482, 300, 34, 19); // high
				g.setColor(color6);
				g.drawRect(482, 300, 34, 19);
			}

			if (!frame.isVisible()) {
				g.setColor(color6);
				g.drawRect(482, 281, 34, 19);

				g.setFont(font2);
				g.setColor(color6);
				g.drawString("GUI", 490, 294);
			}

			g.setFont(font2);
			g.setColor(color6);
			g.drawString("Hide", 490, 332);
			g.drawString("Bank", 490, 313);

		} else {
			g.setColor(color2); // Show/Hide button
			g.fillRect(482, 319, 34, 19);
			g.setColor(color6);
			g.drawRect(482, 319, 34, 19);

			g.setFont(font2);
			g.setColor(color6);
			g.drawString("Show", 490, 332);

			if (!useBank) {
				g.setColor(color2); // Check bank button
				g.fillRect(482, 300, 34, 19);
				g.setColor(color6);
				g.drawRect(482, 300, 34, 19);
			} else {
				g.setColor(color1); // Check bank button
				g.fillRect(482, 300, 34, 19); // high
				g.setColor(color6);
				g.drawRect(482, 300, 34, 19);
			}

			g.setFont(font2);
			g.setColor(color6);
			g.drawString("Bank", 490, 313);

			if (!frame.isVisible()) {
				g.setColor(color2);
				g.fillRect(482, 281, 34, 19);
				g.setColor(color6);
				g.drawRect(482, 281, 34, 19);

				g.setFont(font2);
				g.setColor(color6);
				g.drawString("GUI", 490, 294);
			}
		}
	}

	private void drawMouse(Graphics2D g) {
		((Graphics2D) g).setRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING,
		    RenderingHints.VALUE_ANTIALIAS_ON));
		final long mpt = System.currentTimeMillis() - mouse.getPressTime();

		Point p = mouse.getLocation();
		Graphics2D spinG = (Graphics2D) g.create();
		Graphics2D spinGRev = (Graphics2D) g.create();
		Graphics2D spinG2 = (Graphics2D) g.create();
		spinG.setColor(MOUSE_BORDER_COLOR);
		spinGRev.setColor(MOUSE_COLOR);
		spinG.rotate(System.currentTimeMillis() % 2000d / 2000d * (360d) * 2 * Math.PI / 180.0, p.x, p.y);
		spinGRev.rotate(System.currentTimeMillis() % 2000d / 2000d * (-360d) * 2 * Math.PI / 180.0, p.x, p.y);
		final int outerSize = 20;
		final int innerSize = 12;
		spinG.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		spinGRev.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		spinG.drawArc(p.x - (outerSize / 2), p.y - (outerSize / 2), outerSize, outerSize, 100, 75);
		spinG.drawArc(p.x - (outerSize / 2), p.y - (outerSize / 2), outerSize, outerSize, -100, 75);
		spinGRev.drawArc(p.x - (innerSize / 2), p.y - (innerSize / 2), innerSize, innerSize, 100, 75);
		spinGRev.drawArc(p.x - (innerSize / 2), p.y - (innerSize / 2), innerSize, innerSize, -100, 75);
		if (mpt < 100 || mouse.isPressed()) {
			clickTimer = System.currentTimeMillis();
			r = 255;
			b = 255;
			this.g = 255;
		} else {
			double fadeTime = 1000d;
			double timeDiff = fadeTime - (System.currentTimeMillis() - clickTimer);
			if (r > 0) {
				r = (int) (timeDiff * 255d / fadeTime);
			}
			if (b > 0) {
				b = (int) (timeDiff * 255d / fadeTime);
			}
			if (this.g > 0) {
				this.g = (int) (timeDiff * 255d / fadeTime);
			}
			if (r <= 0) {
				r = 0;
			}
			if (b <= 0) {
				b = 0;
			}
			if (this.g <= 0) {
				this.g = 0;
			}
		}
		spinG2.setColor(new Color(r, b, this.g)); // Mouse centre color.
		spinG2.rotate(System.currentTimeMillis() % 2000d / 2000d * 360d * Math.PI / 180.0, p.x, p.y);
		spinG2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		spinG2.drawLine(p.x - 5, p.y, p.x + 5, p.y);
		spinG2.drawLine(p.x, p.y - 5, p.x, p.y + 5);
	}

	int stopAtWC = 100;
	int stopAtFM = 100;
	int after60WC = 0; // 0 cut willows, 1 stop script, 2 cut yews.
	int afterXFM = 0; // 0 continue wcing and stop fming, 1 stop script
	int oakLocation = 0;
	int willowLocation = 0;
	int treeLocation = 0;
	int yewLocation = 0;

	boolean useAvailableHatchets = false;
	boolean checkedBank = false;
	boolean hasBronzeHatchet = false;
	boolean hasIronHatchet = false;
	boolean hasSteelHatchet = false;
	boolean hasBlackHatchet = false;
	boolean hasMithrilHatchet = false;
	boolean hasAdamantHatchet = false;
	boolean hasRuneHatchet = false;
	int bestHatchetAvailable = -1;

	@SuppressWarnings("serial")
	public class GUI extends JFrame {
		public GUI() {
			initComponents();
		}

		public void setVariables() {
			trainFM = checkBox1.isSelected();
			stopAtWC = Integer.parseInt(textField1.getText());
			stopAtFM = Integer.parseInt(textField2.getText());
			afterXFM = comboBox5.getSelectedIndex(); // Train wc, stop script
			after60WC = comboBox1.getSelectedIndex();
			useAvailableHatchets = radioButton2.isSelected();

			oakLocation = comboBox7.getSelectedIndex();
			// North East Draynor, EastDraynor, North Draynor
			willowLocation = comboBox8.getSelectedIndex();
			// Lumbridge, Rimmington
			treeLocation = comboBox6.getSelectedIndex();
			// Lumbridge
			yewLocation = comboBox10.getSelectedIndex();
			// Lumbridge, Rimmington, Falador
		}

		private void checkBox1ActionPerformed(ActionEvent e) {
			if (checkBox1.isSelected()) {
				label8.setEnabled(true);
				label9.setEnabled(true);
				textField2.setEnabled(true);
				comboBox5.setEnabled(true);
			} else {
				label8.setEnabled(false);
				label9.setEnabled(false);
				textField2.setEnabled(false);
				comboBox5.setEnabled(false);
			}
		}

		private void textField1KeyReleased(KeyEvent e) {
			if (textField1.getText().length() > 0) {
				if (Integer.parseInt(textField1.getText()) > 60) {
					label4.setEnabled(true);
					comboBox1.setEnabled(true);
				} else {
					label4.setEnabled(false);
					comboBox1.setEnabled(false);
				}
			}
		}

		public void button1ActionPerformed(ActionEvent e) { // Start button
			setVisible(false);
			guiWait = false;
			setVariables();
		}

		public void button2ActionPerformed(ActionEvent e) { // Thread button
			sendToURL("http://goo.gl/WEQX6");
		}

		public void radioButton1ActionPerformed(ActionEvent e) {
			if (radioButton1.isSelected()) {
				radioButton2.setSelected(false);
			} else {
				radioButton1.setSelected(true);
			}
		}

		public void radioButton2ActionPerformed(ActionEvent e) {
			if (radioButton2.isSelected()) {
				radioButton1.setSelected(false);
			} else {
				radioButton2.setSelected(true);
			}
		}

		private void initComponents() {
			button3 = new JButton();
			panel3 = new JPanel();
			tabbedPane4 = new JTabbedPane();
			panel5 = new JPanel();
			label2 = new JLabel();
			textField1 = new JTextField();
			label3 = new JLabel();
			label4 = new JLabel();
			comboBox1 = new JComboBox();
			checkBox1 = new JCheckBox();
			label8 = new JLabel();
			textField2 = new JTextField();
			label9 = new JLabel();
			comboBox5 = new JComboBox();
			radioButton1 = new JRadioButton();
			radioButton2 = new JRadioButton();
			panel1 = new JPanel();
			label12 = new JLabel();
			label13 = new JLabel();
			label14 = new JLabel();
			label15 = new JLabel();
			comboBox6 = new JComboBox();
			comboBox7 = new JComboBox();
			comboBox8 = new JComboBox();
			comboBox10 = new JComboBox();
			panel2 = new JPanel();
			label10 = new JLabel();
			button2 = new JButton();
			label11 = new JLabel();
			label16 = new JLabel();
			label17 = new JLabel();
			label18 = new JLabel();
			scrollPane1 = new JScrollPane();
			textArea1 = new JTextArea();
			label1 = new JLabel();
			button1 = new JButton();

			// ======== this ========
			setTitle("Dynamic Woodcutter Options");
			setBackground(Color.darkGray);
			setForeground(Color.black);
			Container contentPane = getContentPane();

			// ---- button3 ----
			button3.setEnabled(false);
			button3.setVisible(false);
			button3.setBackground(Color.darkGray);

			// ======== panel3 ========
			{
				panel3.setBackground(Color.darkGray);

				// ======== tabbedPane4 ========
				{
					tabbedPane4.setBackground(Color.darkGray);

					// ======== panel5 ========
					{
						panel5.setBackground(Color.darkGray);

						// ---- label2 ----
						label2.setText("Stop at");
						label2.setBackground(Color.darkGray);

						// ---- textField1 ----
						textField1.setText("100");
						textField1.setBackground(Color.darkGray);
						textField1.addKeyListener(new KeyAdapter() {
							@Override
							public void keyReleased(KeyEvent e) {
								textField1KeyReleased(e);
							}
						});

						// ---- label3 ----
						label3.setText("Woodcutting");
						label3.setBackground(Color.darkGray);

						// ---- label4 ----
						label4.setText("After 60 Woodcutting");
						label4.setBackground(Color.darkGray);

						// ---- comboBox1 ----
						comboBox1.setModel(new DefaultComboBoxModel(new String[] { "Cut Willows", "Stop script",
						        "Cut Yews" }));
						comboBox1.setBackground(Color.darkGray);

						// ---- checkBox1 ----
						checkBox1.setText("Train Firemaking?");
						checkBox1.setBackground(Color.darkGray);
						checkBox1.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								checkBox1ActionPerformed(e);
							}
						});

						// ---- label8 ----
						label8.setText("After");
						label8.setEnabled(false);
						label8.setBackground(Color.darkGray);

						// ---- textField2 ----
						textField2.setText("100");
						textField2.setEnabled(false);
						textField2.setBackground(Color.darkGray);

						// ---- label9 ----
						label9.setText("Firemaking");
						label9.setEnabled(false);
						label9.setBackground(Color.darkGray);

						// ---- comboBox5 ----
						comboBox5.setModel(new DefaultComboBoxModel(new String[] { "Train WC", "Stop script" }));
						comboBox5.setEnabled(false);
						comboBox5.setBackground(Color.darkGray);

						// ---- radioButton1 ----
						radioButton1.setText("Obtain hatchets independently");
						radioButton1.setEnabled(true);
						radioButton1.setSelected(true);
						radioButton1.setBackground(Color.darkGray);
						radioButton1.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								radioButton1ActionPerformed(e);
							}
						});

						// ---- radioButton2 ----
						radioButton2.setText("Use available hatchets");
						radioButton2.setEnabled(true);
						radioButton2.setBackground(Color.darkGray);
						radioButton2.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								radioButton2ActionPerformed(e);
							}
						});

						GroupLayout panel5Layout = new GroupLayout(panel5);
						panel5.setLayout(panel5Layout);
						panel5Layout.setHorizontalGroup(panel5Layout.createParallelGroup().addGroup(
						    panel5Layout.createSequentialGroup().addContainerGap().addGroup(
						        panel5Layout.createParallelGroup().addGroup(
						            panel5Layout.createSequentialGroup().addComponent(radioButton2,
						                GroupLayout.PREFERRED_SIZE, 177, GroupLayout.PREFERRED_SIZE).addContainerGap())
						            .addGroup(
						                panel5Layout.createParallelGroup().addGroup(
						                    panel5Layout.createSequentialGroup().addComponent(checkBox1)
						                        .addContainerGap()).addGroup(
						                    panel5Layout.createParallelGroup().addGroup(
						                        panel5Layout.createSequentialGroup().addGroup(
						                            panel5Layout.createParallelGroup().addGroup(
						                                panel5Layout.createSequentialGroup().addComponent(label2)
						                                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
						                                    .addComponent(textField1, GroupLayout.DEFAULT_SIZE, 41,
						                                        Short.MAX_VALUE).addPreferredGap(
						                                        LayoutStyle.ComponentPlacement.RELATED).addComponent(
						                                        label3).addGap(43, 43, 43)).addGroup(
						                                panel5Layout.createSequentialGroup().addComponent(label4)
						                                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
						                                    .addComponent(comboBox1, GroupLayout.PREFERRED_SIZE,
						                                        GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
						                            .addGap(95, 95, 95)).addGroup(
						                        panel5Layout.createSequentialGroup().addComponent(label8).addGap(5, 5,
						                            5).addComponent(textField2, GroupLayout.PREFERRED_SIZE,
						                            GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addGap(6, 6,
						                            6).addComponent(label9).addGap(9, 9, 9).addComponent(comboBox5,
						                            GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
						                            GroupLayout.PREFERRED_SIZE).addContainerGap(76, Short.MAX_VALUE))))
						            .addGroup(
						                panel5Layout.createSequentialGroup().addComponent(radioButton1,
						                    GroupLayout.PREFERRED_SIZE, 216, GroupLayout.PREFERRED_SIZE)
						                    .addContainerGap()))));
						panel5Layout.setVerticalGroup(panel5Layout.createParallelGroup().addGroup(
						    panel5Layout.createSequentialGroup().addGap(6, 6, 6).addComponent(radioButton1).addGap(5,
						        5, 5).addComponent(radioButton2).addPreferredGap(
						        LayoutStyle.ComponentPlacement.UNRELATED).addGroup(
						        panel5Layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(
						            textField1).addComponent(label2).addComponent(label3)).addPreferredGap(
						        LayoutStyle.ComponentPlacement.UNRELATED).addGroup(
						        panel5Layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(label4)
						            .addComponent(comboBox1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
						                GroupLayout.PREFERRED_SIZE)).addGap(11, 11, 11).addComponent(checkBox1).addGap(
						        18, 18, 18).addGroup(
						        panel5Layout.createParallelGroup().addGroup(
						            panel5Layout.createSequentialGroup().addGap(5, 5, 5).addComponent(label8))
						            .addComponent(textField2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
						                GroupLayout.PREFERRED_SIZE).addGroup(
						                panel5Layout.createSequentialGroup().addGap(5, 5, 5).addComponent(label9))
						            .addComponent(comboBox5, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
						                GroupLayout.PREFERRED_SIZE)).addGap(56, 56, 56)));
					}
					tabbedPane4.addTab("Training", panel5);

					// ======== panel1 ========
					{
						panel1.setBackground(Color.darkGray);

						// ---- label12 ----
						label12.setText("Tree:");

						// ---- label13 ----
						label13.setText("Oak:");

						// ---- label14 ----
						label14.setText("Willow:");

						// ---- label15 ----
						label15.setText("Yew:");

						// ---- comboBox6 ----
						comboBox6.setModel(new DefaultComboBoxModel(new String[] { "Lumbridge" }));

						// ---- comboBox7 ----
						comboBox7.setModel(new DefaultComboBoxModel(new String[] { "North East Draynor",
						        "East Draynor", "North Draynor" }));

						// ---- comboBox8 ----
						comboBox8.setModel(new DefaultComboBoxModel(new String[] { "Lumbridge", "Rimmington",
						        "Port Sarim" }));

						// ---- comboBox10 ----
						comboBox10.setModel(new DefaultComboBoxModel(new String[] { "Lumbridge", "Rimmington",
						        "Port Sarim" }));

						GroupLayout panel1Layout = new GroupLayout(panel1);
						panel1.setLayout(panel1Layout);
						panel1Layout.setHorizontalGroup(panel1Layout.createParallelGroup().addGroup(
						    panel1Layout.createSequentialGroup().addContainerGap().addGroup(
						        panel1Layout.createParallelGroup().addComponent(label12).addComponent(label13)
						            .addComponent(label14).addComponent(label15)).addGap(63, 63, 63).addGroup(
						        panel1Layout.createParallelGroup().addComponent(comboBox8, GroupLayout.PREFERRED_SIZE,
						            GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(comboBox7,
						            GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						            .addComponent(comboBox6, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
						                GroupLayout.PREFERRED_SIZE).addComponent(comboBox10,
						                GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
						                GroupLayout.PREFERRED_SIZE)).addContainerGap(57, Short.MAX_VALUE)));
						panel1Layout.setVerticalGroup(panel1Layout.createParallelGroup().addGroup(
						    panel1Layout.createSequentialGroup().addGap(23, 23, 23).addGroup(
						        panel1Layout.createParallelGroup(GroupLayout.Alignment.TRAILING).addComponent(label12)
						            .addComponent(comboBox6, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
						                GroupLayout.PREFERRED_SIZE)).addGap(18, 18, 18).addGroup(
						        panel1Layout.createParallelGroup(GroupLayout.Alignment.TRAILING).addComponent(label13)
						            .addComponent(comboBox7, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
						                GroupLayout.PREFERRED_SIZE)).addGap(18, 18, 18).addGroup(
						        panel1Layout.createParallelGroup(GroupLayout.Alignment.TRAILING).addComponent(label14)
						            .addComponent(comboBox8, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
						                GroupLayout.PREFERRED_SIZE)).addGap(18, 18, 18).addGroup(
						        panel1Layout.createParallelGroup(GroupLayout.Alignment.TRAILING).addComponent(label15)
						            .addComponent(comboBox10, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
						                GroupLayout.PREFERRED_SIZE)).addContainerGap(86, Short.MAX_VALUE)));
					}
					tabbedPane4.addTab("Locations", panel1);

					// ======== panel2 ========
					{
						panel2.setBackground(Color.darkGray);

						// ---- label10 ----
						label10.setText("Dynamic Woodcutter");
						label10.setFont(label10.getFont().deriveFont(label10.getFont().getSize() + 8f));
						label10.setBackground(Color.darkGray);

						// ---- button2 ----
						button2.setText("Go to thread");
						button2.setBackground(Color.darkGray);
						button2.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								button2ActionPerformed(e);
							}
						});

						// ---- label11 ----
						label11.setText("Current version:");
						label11.setFont(label11.getFont().deriveFont(label11.getFont().getSize() + 1f));
						label11.setBackground(Color.darkGray);

						// ---- label16 ----
						label16.setText("Latest version:");
						label16.setFont(label16.getFont().deriveFont(label16.getFont().getSize() + 1f));
						label16.setBackground(Color.darkGray);

						// ---- label17 ----
						label17.setFont(label17.getFont().deriveFont(label17.getFont().getSize() + 1f));
						label17.setBackground(Color.darkGray);
						label17.setText(Double.toString(scriptVersion));

						// ---- label18 ----
						label18.setFont(label18.getFont().deriveFont(label18.getFont().getSize() + 1f));
						label18.setBackground(Color.darkGray);
						label18.setText(Double.toString(currVer));

						// ======== scrollPane1 ========
						{
							scrollPane1.setBackground(Color.darkGray);

							// ---- textArea1 ----
							textArea1.setWrapStyleWord(true);
							textArea1.setLineWrap(true);
							textArea1
							    .setText("Thanks for using Dynamic Woodcutter please leave feedback on my thread. ~hlunnb");
							textArea1.setEditable(false);
							textArea1.setBackground(Color.darkGray);
							textArea1.setFont(textArea1.getFont().deriveFont(textArea1.getFont().getSize() + 5f));
							scrollPane1.setViewportView(textArea1);
						}

						GroupLayout panel2Layout = new GroupLayout(panel2);
						panel2.setLayout(panel2Layout);
						panel2Layout.setHorizontalGroup(panel2Layout.createParallelGroup().addGroup(
						    panel2Layout.createSequentialGroup().addContainerGap().addGroup(
						        panel2Layout.createParallelGroup().addGroup(
						            panel2Layout.createSequentialGroup().addComponent(scrollPane1,
						                GroupLayout.DEFAULT_SIZE, 262, Short.MAX_VALUE).addContainerGap()).addGroup(
						            panel2Layout.createSequentialGroup().addComponent(label10).addContainerGap(97,
						                Short.MAX_VALUE)).addGroup(GroupLayout.Alignment.TRAILING,
						            panel2Layout.createSequentialGroup().addComponent(button2).addGap(101, 101, 101))
						            .addGroup(
						                panel2Layout.createSequentialGroup().addComponent(label11).addPreferredGap(
						                    LayoutStyle.ComponentPlacement.RELATED).addComponent(label17)
						                    .addContainerGap(179, Short.MAX_VALUE)).addGroup(
						                panel2Layout.createSequentialGroup().addComponent(label16).addPreferredGap(
						                    LayoutStyle.ComponentPlacement.RELATED).addComponent(label18)
						                    .addContainerGap(179, Short.MAX_VALUE)))));
						panel2Layout.linkSize(SwingConstants.HORIZONTAL, new Component[] { label11, label16 });
						panel2Layout.setVerticalGroup(panel2Layout.createParallelGroup().addGroup(
						    panel2Layout.createSequentialGroup().addContainerGap().addComponent(label10)
						        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED).addComponent(scrollPane1,
						            GroupLayout.PREFERRED_SIZE, 78, GroupLayout.PREFERRED_SIZE).addGap(25, 25, 25)
						        .addGroup(
						            panel2Layout.createParallelGroup(GroupLayout.Alignment.TRAILING).addComponent(
						                label11).addComponent(label17)).addGap(18, 18, 18).addGroup(
						            panel2Layout.createParallelGroup(GroupLayout.Alignment.TRAILING).addComponent(
						                label16).addComponent(label18)).addPreferredGap(
						            LayoutStyle.ComponentPlacement.RELATED, 13, Short.MAX_VALUE).addComponent(button2)
						        .addContainerGap()));
					}
					tabbedPane4.addTab("About", panel2);

				}

				// ---- label1 ----
				label1.setText("<html><img src=\"http://i.imgur.com/9MfV2.png\"></img> ");
				label1.setBackground(Color.darkGray);

				// ---- button1 ----
				button1.setText("Start");
				button1.setBackground(Color.darkGray);
				button1.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						button1ActionPerformed(e);
					}
				});

				GroupLayout panel3Layout = new GroupLayout(panel3);
				panel3.setLayout(panel3Layout);
				panel3Layout.setHorizontalGroup(panel3Layout.createParallelGroup().addGroup(
				    panel3Layout.createSequentialGroup().addContainerGap().addGroup(
				        panel3Layout.createParallelGroup(GroupLayout.Alignment.TRAILING, false).addComponent(
				            tabbedPane4, GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE).addGroup(
				            GroupLayout.Alignment.LEADING,
				            panel3Layout.createSequentialGroup().addComponent(label1, GroupLayout.PREFERRED_SIZE,
				                GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addGap(4, 4, 4).addComponent(
				                button1))).addContainerGap(12, Short.MAX_VALUE)));
				panel3Layout.setVerticalGroup(panel3Layout.createParallelGroup().addGroup(
				    panel3Layout.createSequentialGroup().addContainerGap().addComponent(tabbedPane4,
				        GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addGap(4, 4,
				        4).addGroup(
				        panel3Layout.createParallelGroup().addComponent(label1, GroupLayout.PREFERRED_SIZE,
				            GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addGroup(
				            panel3Layout.createSequentialGroup().addGap(30, 30, 30).addComponent(button1)))
				        .addContainerGap(15, Short.MAX_VALUE)));
			}

			GroupLayout contentPaneLayout = new GroupLayout(contentPane);
			contentPane.setLayout(contentPaneLayout);
			contentPaneLayout.setHorizontalGroup(contentPaneLayout.createParallelGroup().addGroup(
			    contentPaneLayout.createSequentialGroup().addGap(240, 240, 240).addComponent(button3).addContainerGap(
			        69, Short.MAX_VALUE)).addComponent(panel3, GroupLayout.Alignment.TRAILING,
			    GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
			contentPaneLayout.setVerticalGroup(contentPaneLayout.createParallelGroup().addGroup(
			    contentPaneLayout.createSequentialGroup().addComponent(panel3, GroupLayout.PREFERRED_SIZE,
			        GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addPreferredGap(
			        LayoutStyle.ComponentPlacement.RELATED).addComponent(button3).addContainerGap(
			        GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
			pack();
			setLocationRelativeTo(getOwner());
		}

		private JButton button3;
		private JPanel panel3;
		private JTabbedPane tabbedPane4;
		private JPanel panel5;
		private JLabel label2;
		private JTextField textField1;
		private JLabel label3;
		private JLabel label4;
		private JComboBox comboBox1;
		private JCheckBox checkBox1;
		private JLabel label8;
		private JTextField textField2;
		private JLabel label9;
		private JComboBox comboBox5;
		private JRadioButton radioButton1;
		private JRadioButton radioButton2;
		private JPanel panel1;
		private JLabel label12;
		private JLabel label13;
		private JLabel label14;
		private JLabel label15;
		private JComboBox comboBox6;
		private JComboBox comboBox7;
		private JComboBox comboBox8;
		private JComboBox comboBox10;
		private JPanel panel2;
		private JLabel label10;
		private JButton button2;
		private JLabel label11;
		private JLabel label16;
		private JLabel label17;
		private JLabel label18;
		private JScrollPane scrollPane1;
		private JTextArea textArea1;
		private JLabel label1;
		private JButton button1;

	} // End of GUI

	public final int CLERKS = 2593; // public final int[] CLERKS = new int[] { 2241, 2240, 2593, 1419 };
	public final int BANKERS = 3416; // public final int[] BANKERS = new int[] { 3293, 3416, 2718, 3418 };
	public final int GE_INTERFACE = 105;
	public final int GE_CLOSE = 14;
	public final int SEARCH = 389;
	public final int COLLECT_INTERFACE = 109;
	public int SLOT = 0;

	private final Pattern PATTERN = Pattern.compile("(?i)<td><img src=\".+obj_sprite\\.gif\\?id=(\\d+)\" alt=\"(.+)\"");

	private final String HOST = "http://services.runescape.com";
	private final String GET = "/m=itemdb_rs/viewitem.ws?obj=";

	/**
	 * Buys items from the Grand Exchange if it's open
	 * 
	 * @param itemName item to buy
	 * @param slotNumber slot number to buy from (1-5)
	 * @param quantity amount to buy
	 * @param price Price to buy from. 0 will leave it as default price
	 * @return <tt>true</tt> if bought successfully; otherwise <tt>false</tt>
	 */
	public boolean buy(String itemName, int slotNumber, int quantity, int price) {
		SLOT = slotNumber;
		itemName = itemName.substring(0, 1).toUpperCase() + itemName.substring(1).toLowerCase();
		String Sep[] = itemName.split(" ");
		String searchName = null;
		for (int i = 0; i < Sep.length;) {
			if (!Sep[i].contains("(")) {
				if (searchName == null) {
					searchName = Sep[i];
				} else {
					searchName += " " + Sep[i];
				}
			}
			i++;
		}
		if (slotNumber == 0 || slotNumber > 5) {
			return false;
		}
		if (isOpen()) {
			GEBuyMethods t = new GEBuy(slotNumber);
			int buyClick = t.getBuyClick();
			if (!isSearching()) {
				interfaces.getComponent(GE_INTERFACE, buyClick).doClick();
				sleep(random(1500, 2000));
			}
			if (isSearching() && !hasSearched(searchName)) {
				keyboard.sendText(searchName, true);
				sleep(random(1000, 1500));
			}
			if (isSearching() && hasSearched(searchName)) {
				boolean foundItem = false;
				if (findItem() && !foundItem) {
					boolean done = false;
					int index = 0;
					if (!done) {
						for (int i = 0; interfaces.getComponent(389, 4).getComponent(i) != null;) {
							if (interfaces.getComponent(389, 4).getComponent(i).getText().equals(itemName)) {
								index = i;
							}
							i++;
							if (interfaces.getComponent(389, 4).getComponent(i + 1) == null) {
								done = true;
							}
						}
					}
					if (done && index == 0) {
						return false;
					}
					if (done && index > 0) {
						if (!interfaces.getComponent(389, 4).getComponent(index).isValid()
						        && interfaces.getComponent(389, 8).getComponent(5).isValid()) {
							interfaces.getComponent(389, 8).getComponent(5).doClick();
							sleep(random(200, 500));
						}
						if (interfaces.getComponent(389, 4).getComponent(index).isValid()) {
							interfaces.getComponent(389, 4).getComponent(index).doClick();
							sleep(random(700, 900));
							foundItem = true;
						}
					}
				}
				if (foundItem) {
					boolean changeQuantity = true;
					boolean changePrice;
					changePrice = price > 1;
					int times = 0;
					while (changeQuantity) {
						if (times >= 3) {
							close();
							return false;
						}
						if (isOpen()) {
							if (interfaces.getComponent(GE_INTERFACE, 148).getText() != null
							        && interfaces.getComponent(GE_INTERFACE, 148).getText().contains(
							            "" + formatNumb(quantity))) {
								changeQuantity = false;
								break;
							}
							if (interfaces.getComponent(GE_INTERFACE, 168).getText() != null
							        && interfaces.getComponent(GE_INTERFACE, 168).getText().contains("...")) {
								interfaces.getComponent(GE_INTERFACE, 168).doClick();
								sleep(random(1000, 1200));
							}
							if (interfaces.getComponent(752, 4).getText().contains("you wish to purchase")) {
								keyboard.sendText("" + quantity, true);
							}
							sleep(random(1000, 2000));
							if (interfaces.getComponent(GE_INTERFACE, 148).getText() != null
							        && interfaces.getComponent(GE_INTERFACE, 148).getText().contains(
							            "" + formatNumb(quantity))) {
								changeQuantity = false;
								sleep(random(1000, 1200));
							}
						} else {
							return false;
						}
						times++;
					}
					times = 0;
					while (changePrice && !changeQuantity) {
						if (times >= 3) {
							close();
							return false;
						}
						if (isOpen()) {
							if (interfaces.getComponent(GE_INTERFACE, 153).getText() != null
							        && interfaces.getComponent(GE_INTERFACE, 153).getText().contains(
							            "" + formatNumb(price))) {
								changePrice = false;
								break;
							}
							if (interfaces.getComponent(GE_INTERFACE, 177).getText() != null
							        && interfaces.getComponent(GE_INTERFACE, 177).getText().contains("...")) {
								interfaces.getComponent(GE_INTERFACE, 177).doClick();
								sleep(random(1000, 1200));
							}
							if (interfaces.getComponent(752, 4).getText().contains("you wish to buy")) {
								keyboard.sendText("" + price, true);
							}
							sleep(random(1000, 2000));
							if (interfaces.getComponent(GE_INTERFACE, 153).getText() != null
							        && interfaces.getComponent(GE_INTERFACE, 153).getText().contains(
							            "" + formatNumb(price))) {
								changePrice = false;
								sleep(random(1000, 1200));
							}
						} else {
							return false;
						}
						times++;
					}
					if (!changePrice && !changeQuantity) {
						if (interfaces.getComponent(GE_INTERFACE, 187).getText() != null) {
							interfaces.getComponent(GE_INTERFACE, 187).doClick();
						}
						close();
						sentOffer = true;
						return true;
					}
				}
			}
			close();
			return false;
		}
		return false;
	}

	/**
	 * Sells items from the Grand Exchange if it's open
	 * 
	 * @param itemName item to sell
	 * @param slotNumber slot number to sell from (1-5)
	 * @param quantity amount to sell
	 * @param price Price to sell from. 0 will leave it as default price
	 * @return <tt>true</tt> if sold successfully; otherwise <tt>false</tt>
	 */
	public boolean sell(String itemName, int slotNumber, int quantity, int price) {
		SLOT = slotNumber;
		if (slotNumber == 0 || slotNumber > 5) {
			return false;
		}
		if (!inventory.contains(itemName)) {
			return false;
		}
		if (isOpen()) {
			GEBuyMethods t = new GEBuy(slotNumber);
			int sellClick = t.getSellClick();
			boolean offerItem = false;
			boolean offeredItem = false;
			if (!isSelling()) {
				interfaces.getComponent(GE_INTERFACE, sellClick).doClick();
				sleep(random(700, 900));
				offerItem = true;
			}
			if (!isSelling() && offerItem) {
				inventory.getItem(itemName).doClick(true);
				sleep(random(500, 700));
				offeredItem = true;
			}
			if (isSelling()) {
				close();
				return false;
			}
			if (!isSelling() && offeredItem) {
				boolean changeQuantity;
				changeQuantity = quantity > 1;
				boolean changePrice;
				changePrice = price > 1;
				int times = 0;
				while (changeQuantity) {
					if (times == 3) {
						close();
						return false;
					}
					if (isOpen()) {
						if (interfaces.getComponent(GE_INTERFACE, 148).getText() != null
						        && interfaces.getComponent(GE_INTERFACE, 148).getText().contains(
						            "" + formatNumb(quantity))) {
							changeQuantity = false;
							break;
						}
						if (interfaces.getComponent(GE_INTERFACE, 168).getText() != null
						        && interfaces.getComponent(GE_INTERFACE, 168).getText().contains("...")) {
							interfaces.getComponent(GE_INTERFACE, 168).doClick();
							sleep(random(700, 900));
						}
						if (interfaces.getComponent(752, 4).getText().contains("amount you wish to")) {
							keyboard.sendTextInstant("" + quantity, true);
						}
						sleep(random(1000, 2000));
						if (interfaces.getComponent(GE_INTERFACE, 148).getText() != null
						        && interfaces.getComponent(GE_INTERFACE, 148).getText().contains(
						            "" + formatNumb(quantity))) {
							changeQuantity = false;
						}
					} else {
						return false;
					}
					times++;
				}
				times = 0;
				while (changePrice && !changeQuantity) {
					if (times == 3) {
						close();
						return false;
					}
					if (isOpen()) {
						if (interfaces.getComponent(GE_INTERFACE, 177).getText() != null
						        && interfaces.getComponent(GE_INTERFACE, 168).getText().contains("...")) {
							interfaces.getComponent(GE_INTERFACE, 177).doClick();
							sleep(random(700, 900));
						}
						if (interfaces.getComponent(752, 4).getText().contains("you wish to sell")) {
							keyboard.sendTextInstant("" + price, true);
						}
						sleep(random(1000, 2000));
						if (interfaces.getComponent(GE_INTERFACE, 153).getText() != null
						        && interfaces.getComponent(GE_INTERFACE, 153).getText()
						            .contains("" + formatNumb(price))) {
							changePrice = false;
						}
					} else {
						return false;
					}
					times++;
				}
				if (!changeQuantity && !changePrice) {
					if (interfaces.getComponent(GE_INTERFACE, 187).getText() != null) {
						interfaces.getComponent(GE_INTERFACE, 187).doClick();
					}
				}
				close();
				offeredOaks = true;
				return true;
			}
			close();
			return false;
		}
		return false;
	}

	/**
	 * @return <tt>true</tt> if selling; otherwise <tt>false</tt>
	 */
	private boolean isSelling() {
		return interfaces.getComponent(GE_INTERFACE, 142).isValid()
		        && !interfaces.getComponent(GE_INTERFACE, 142).getText().equals("Choose an item to exchange");
	}

	/**
	 * Determines membership
	 * 
	 * @return <tt>true</tt> if members is selected for the account; otherwise <tt>false</tt>
	 */
	public boolean isMember() {
		return AccountManager.isMember(account.getName());
	}

	/**
	 * Sets the number format as the same as GrandExchange's
	 * 
	 * @param money GrandExchange's money
	 * @return number to match GrandExchange's
	 */
	private static String formatNumb(long money) {
		return new DecimalFormat("###,###,###,###,###,###").format(money);
	}

	/**
	 * Gets the total slots there are for the person
	 * 
	 * @return number of slots if account is member
	 */
	public int getTotalSlots() {
		return isMember() ? 6 : 2;
	}

	/**
	 * Checks to see if the GE slot is empty
	 * 
	 * @param slot gets the correct interface
	 * @return <tt>true</tt> if empty; otherwise <tt>false</tt>
	 */
	public boolean isSlotEmpty(int slot) {
		GEBuyMethods check2 = new GEBuy(slot);
		int check = check2.getInterface();
		if (isOpen()) {
			if (interfaces.getComponent(GE_INTERFACE, check).getComponent(10).getText().equals("Empty")) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Will determine the total amount of empty slots
	 * 
	 * @return total amount of empty slots
	 */
	public int getAllEmptySlots() {
		if (isOpen()) {
			int total = 0;
			for (int i = 1; i <= getTotalSlots();) {
				SLOT = i;
				GEBuyMethods check2 = new GEBuy(i);
				int check = check2.getInterface();
				if (interfaces.getComponent(GE_INTERFACE, check).getComponent(10).getText().equals("Empty")) {
					total++;
				}
				if (i == getTotalSlots()) {
					return total;
				}
				i++;
			}
		}
		return 0;
	}

	/**
	 * Checks for nearest empty slot
	 * 
	 * @return an empty spot in the GE if there is one. If not, 0
	 */
	public int getEmptySlot() {
		if (isOpen()) {
			for (int i = 1; i <= getTotalSlots();) {
				SLOT = i;
				GEBuyMethods check2 = new GEBuy(i);
				int check = check2.getInterface();
				if (interfaces.getComponent(GE_INTERFACE, check).getComponent(10).getText().equals("Empty")) {
					return i;
				}
				i++;
			}
			return 0;
		}
		return 0;
	}

	/**
	 * Determines if an offer has completed or not
	 * 
	 * @return <tt>true</tt> if an offer is completed; otherwise <tt>false</tt>
	 */
	public boolean isAnOfferCompleted() {
		GEBuyMethods check2 = new GEBuy();
		return check2.isAnOfferCompleted();
	}

	/**
	 * Determines if there is an item by the name
	 * 
	 * @return <tt>true</tt> if an item was found; otherwise <tt>false</tt>
	 */
	public boolean findItem() {
		return interfaces.getComponent(SEARCH, 4).getComponent(0) != null
		        && interfaces.getComponent(SEARCH, 4).getComponent(0).getText() != null
		        && !interfaces.getComponent(SEARCH, 4).getComponent(0).getText().equals("No matching items found.");
	}

	/**
	 * Determines if the person has searched or not
	 * 
	 * @param itemName is defined by buy/sell item
	 * @return <tt>true</tt> if they have searched; otherwise <tt>false</tt>
	 */
	public boolean hasSearched(String itemName) {
		if (!Character.isUpperCase(itemName.charAt(0))) {
			Character.toUpperCase(itemName.charAt(0));
		}
		return interfaces.getComponent(GE_INTERFACE, 142).getText().contains(itemName)
		        || interfaces.getComponent(SEARCH, 4).getComponent(1) != null
		        && interfaces.getComponent(SEARCH, 9).getText().contains(itemName);
	}

	/**
	 * Determines if the player is searching
	 * 
	 * @return <tt>true</tt> if interface is valid; otherwise <tt>false</tt>
	 */
	public boolean isSearching() {
		return interfaces.getComponent(GE_INTERFACE, 134).isValid();
	}

	/**
	 * Checks whether the GE is open
	 * 
	 * @return <tt>true</tt> if the GE interface is valid; otherwise <tt>false</tt>
	 */
	public boolean isOpen() {
		return getInterface().isValid();
	}

	/**
	 * Gets the bank's interface.
	 * 
	 * @return <tt>true</tt> if interface is valid
	 */
	public RSInterface getInterface() {
		return interfaces.get(GE_INTERFACE);
	}

	/**
	 * Gets the general interface for the slot
	 * 
	 * @param slot determines which one to take from
	 * @return interface for the slot
	 */
	public int bankGetInterface(int slot) {
		BankCollectMethods collect = new BankCollect(slot);
		return collect.getBankInterface();
	}

	/**
	 * Gets the left interface for the slot
	 * 
	 * @param slot determines which one to take from
	 * @return left interface for the slot
	 */
	public int bankGetLeftInterface(int slot) {
		BankCollectMethods collect = new BankCollect(slot);
		return collect.getBankLeftCollect();
	}

	/**
	 * Gets the right interface for the slot
	 * 
	 * @param slot determines which one to take from
	 * @return right interface for the slot
	 */
	public int bankGetRightInterface(int slot) {
		BankCollectMethods collect = new BankCollect(slot);
		return collect.getBankRightCollect();
	}

	/**
	 * Collects everything from the interface
	 * 
	 * @return <tt>true</tt> if collected all successfully; otherwise <tt>false</tt>
	 */
	public boolean bankCollectAll() {
		BankCollectMethods collect = new BankCollect();
		return collect.bankCollectAll();
	}

	/**
	 * Opens collection interface
	 * 
	 * @return <tt>true</tt> if opened successfully; otherwise <tt>false</tt>
	 */
	public boolean bankCollectOpen() {
		BankCollectMethods collect = new BankCollect();
		return collect.bankOpen();
	}

	/**
	 * Closes collection interface
	 * 
	 * @return <tt>true</tt> if closed successfully; otherwise <tt>false</tt>
	 */
	public boolean bankCollectClose() {
		BankCollectMethods collect = new BankCollect();
		return collect.bankClose();
	}

	/**
	 * Checks collection interface
	 * 
	 * @return <tt>true</tt> if opened; otherwise <tt>false</tt>
	 */
	public boolean bankCollectIsOpen() {
		BankCollectMethods collect = new BankCollect();
		return collect.bankIsOpen();
	}

	/**
	 * Closes the GE.
	 * 
	 * @return <tt>true</tt> if the GE is no longer open; otherwise <tt>false</tt>
	 */
	public boolean close() {
		if (isOpen()) {
			interfaces.getComponent(GE_INTERFACE, GE_CLOSE).doClick();
			sleep(random(1500, 2000));
			return !isOpen();
		}
		return !isOpen();
	}

	/**
	 * Opens the GrandExchange
	 * 
	 * @return <tt>true</tt> if open; otherwise <tt>false</tt>
	 */
	public boolean open() {
		if (!isOpen()) {
			RSNPC i = npcs.getNearest(CLERKS);
			if (!i.isValid()) {
				return false;
			}
			mouse.move(i.getPoint());
			sleep(100, 200);
			mouse.click(false);
			// i.interact("Exchange ")
			if (menu.clickIndex(menu.getIndex("Exchange") + 1)) {
				if (calc.distanceTo(i) > 1) {
					long time = System.currentTimeMillis();
					int max = random(2000, 4000);
					while ((System.currentTimeMillis() - time) < max) {
						if (players.getMyPlayer().isMoving()) {
							do {
								sleep(random(5, 15));
							}
							while (players.getMyPlayer().isMoving() || !i.isOnScreen());
							break;
						}
						sleep(random(5, 15));
					}
				}
				for (int j = 0; j < 10 && !isOpen(); j++) {
					sleep(random(100, 200));
				}
				// Ensures that the widget becomes valid
				sleep(random(700, 900));
				return isOpen();
			} else {
				return false;
			}
		}
		return true;
	}

	/**
	 * Gets the name of the given item ID. Should not be used.
	 * 
	 * @param itemID The item ID to look for.
	 * @return The name of the given item ID or an empty String if unavailable.
	 * @see GrandExchange#lookup(int)
	 */
	public String getItemName(final int itemID) {
		final GEItem geItem = lookup(itemID);
		if (geItem != null) {
			return geItem.getName();
		}
		return "";
	}

	/**
	 * Gets the ID of the given item name. Should not be used.
	 * 
	 * @param itemName The name of the item to look for.
	 * @return The ID of the given item name or -1 if unavailable.
	 * @see GrandExchange#lookup(java.lang.String)
	 */
	public int getItemID(final String itemName) {
		final GEItem geItem = lookup(itemName);
		if (geItem != null) {
			return geItem.getID();
		}
		return -1;
	}

	/**
	 * Collects data for a given item ID from the Grand Exchange website.
	 * 
	 * @param itemID The item ID.
	 * @return An instance of GrandExchange.GEItem; <code>null</code> if unable to fetch data.
	 */
	public GEItem lookup(final int itemID) {
		try {

			final URL url = new URL(HOST + GET + itemID);
			final BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
			String input;
			boolean exists = false;
			int i = 0;
			final double[] values = new double[4];
			String name = "", examine = "";
			while ((input = br.readLine()) != null) {
				if (input.contains("<div class=\"brown_box main_ge_page") && !exists) {
					if (!input.contains("vertically_spaced")) {
						return null;
					}
					exists = true;
					br.readLine();
					br.readLine();
					name = br.readLine();
				} else if (input.contains("<img id=\"item_image\" src=\"")) {
					examine = br.readLine();
				} else if (input.matches("(?i).+ (price|days):</b> .+")) {
					values[i] = parse(input);
					i++;
				} else if (input.matches("<div id=\"legend\">")) {
					break;
				}
			}
			return new GEItem(name, examine, itemID, values);
		} catch (final IOException ignore) {}
		return null;
	}

	/**
	 * Collects data for a given item name from the Grand Exchange website.
	 * 
	 * @param itemName The name of the item.
	 * @return An instance of GrandExchange.GEItem; <code>null</code> if unable to fetch data.
	 */
	public GEItem lookup(final String itemName) {
		try {
			final URL url = new URL(HOST + "/m=itemdb_rs/results.ws?query=" + itemName + "&price=all&members=");
			final BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
			String input;
			while ((input = br.readLine()) != null) {
				if (input.contains("<div id=\"search_results_text\">")) {
					input = br.readLine();
					if (input.contains("Your search for")) {
						return null;
					}
				} else if (input.startsWith("<td><img src=")) {
					final Matcher matcher = PATTERN.matcher(input);
					if (matcher.find()) {
						if (matcher.group(2).contains(itemName)) {
							return lookup(Integer.parseInt(matcher.group(1)));
						}
					}
				}
			}
		} catch (final IOException ignored) {}
		return null;
	}

	private double parse(String str) {
		if (str != null && !str.isEmpty()) {
			str = stripFormatting(str);
			str = str.substring(str.indexOf(58) + 2, str.length());
			str = str.replace(",", "");
			str = str.trim();
			if (!str.endsWith("%")) {
				if (!str.endsWith("k") && !str.endsWith("m") && !str.endsWith("b")) {
					return Double.parseDouble(str);
				}
				return Double.parseDouble(str.substring(0, str.length() - 1))
				        * (str.endsWith("b") ? 1000000000 : str.endsWith("m") ? 1000000 : 1000);
			}
			final int k = str.startsWith("+") ? 1 : -1;
			str = str.substring(1);
			return Double.parseDouble(str.substring(0, str.length() - 1)) * k;
		}
		return -1D;
	}

	private String stripFormatting(final String str) {
		if (str != null && !str.isEmpty()) {
			return str.replaceAll("(^[^<]+>|<[^>]+>|<[^>]+$)", "");
		}
		return "";
	}

	/**
	 * Provides access to GEItem Information.
	 */
	public class GEItem {
		private final String name;
		private final String examine;

		private final int id;

		private final int guidePrice;

		private final double change30;
		private final double change90;
		private final double change180;

		GEItem(final String name, final String examine, final int id, final double[] values) {
			this.name = name;
			this.examine = examine;
			this.id = id;
			guidePrice = (int) values[0];
			change30 = values[1];
			change90 = values[2];
			change180 = values[3];
		}

		/**
		 * Gets the change in price for the last 30 days of this item.
		 * 
		 * @return The change in price for the last 30 days of this item.
		 */
		public double getChange30Days() {
			return change30;
		}

		/**
		 * Gets the change in price for the last 90 days of this item.
		 * 
		 * @return The change in price for the last 90 days of this item.
		 */
		public double getChange90Days() {
			return change90;
		}

		/**
		 * Gets the change in price for the last 180 days of this item.
		 * 
		 * @return The change in price for the last 180 days of this item.
		 */
		public double getChange180Days() {
			return change180;
		}

		/**
		 * Gets the ID of this item.
		 * 
		 * @return The ID of this item.
		 */
		public int getID() {
			return id;
		}

		/**
		 * Gets the market price of this item.
		 * 
		 * @return The market price of this item.
		 */
		public int getGuidePrice() {
			return guidePrice;
		}

		/**
		 * Gets the name of this item.
		 * 
		 * @return The name of this item.
		 */
		public String getName() {
			return name;
		}

		/**
		 * Gets the description of this item.
		 * 
		 * @return The description of this item.
		 */
		public String getDescription() {
			return examine;
		}
	}

	private interface BankCollectMethods {
		public int getBankInterface();

		public int getBankLeftCollect();

		public int getBankRightCollect();

		public boolean bankCollectAll();

		public boolean bankIsOpen();

		public boolean bankOpen();

		public boolean bankClose();
	}

	private class BankCollect implements BankCollectMethods {
		private int Interface = 0;
		private int leftCollect = 0;
		private int rightCollect = 0;
		private final int COLLECT_CLOSE = 14;

		public BankCollect(int slot) {
			SLOT = slot;
			switch (slot) {
				case 1:
					Interface = 19;
					leftCollect = 0;
					rightCollect = 2;
					break;

				case 2:
					Interface = 23;
					leftCollect = 0;
					rightCollect = 2;
					break;

				case 3:
					Interface = 27;
					leftCollect = 0;
					rightCollect = 2;
					break;

				case 4:
					Interface = 32;
					leftCollect = 0;
					rightCollect = 2;
					break;

				case 5:
					Interface = 37;
					leftCollect = 0;
					rightCollect = 2;
					break;

				case 6:
					Interface = 42;
					leftCollect = 0;
					rightCollect = 2;
					break;
			}
		}

		public BankCollect() {

		}

		public int getBankInterface() {
			return this.Interface;
		}

		public int getBankLeftCollect() {
			return this.leftCollect;
		}

		public int getBankRightCollect() {
			return this.rightCollect;
		}

		public boolean bankIsOpen() {
			return interfaces.get(COLLECT_INTERFACE).isValid();
		}

		public boolean bankOpen() {
			if (!bankIsOpen()) {
				RSNPC i = npcs.getNearest(BANKERS);
				if (!i.isValid()) {
					return false;
				}
				mouse.move(i.getPoint());
				sleep(100, 200);
				mouse.click(false);
				if (menu.clickIndex(menu.getIndex("Collect"))) {
					if (calc.distanceTo(i) > 1) {
						long time = System.currentTimeMillis();
						int max = random(2000, 4000);
						while ((System.currentTimeMillis() - time) < max) {
							if (players.getMyPlayer().isMoving()) {
								do {
									sleep(random(5, 15));
								}
								while (players.getMyPlayer().isMoving() || !i.isOnScreen());
								break;
							}
							sleep(random(5, 15));
						}
					}
					for (int j = 0; j < 10 && !bankIsOpen(); j++) {
						sleep(random(100, 200));
					}
					// Ensures that the widget becomes valid
					sleep(random(700, 900));
					return bankIsOpen();
				} else {
					return false;
				}
			}
			return true;
		}

		public boolean bankCollectAll() {
			if (bankIsOpen()) {
				int boxToCollect;
				boxToCollect = getTotalSlots();
				for (int i = 1; i <= boxToCollect;) {
					BankCollectMethods k = new BankCollect(i);
					int inter = k.getBankInterface();
					int left = k.getBankLeftCollect();
					int right = k.getBankRightCollect();
					if (interfaces.getComponent(COLLECT_INTERFACE, inter).getComponent(left).getActions() != null
					        && interfaces.getComponent(COLLECT_INTERFACE, inter).getComponent(left).getActions().length >= 1) {
						interfaces.getComponent(COLLECT_INTERFACE, inter).getComponent(left).doClick();
						sleep(random(300, 500));
					}
					if (interfaces.getComponent(COLLECT_INTERFACE, inter).getComponent(right).getActions() != null
					        && interfaces.getComponent(COLLECT_INTERFACE, inter).getComponent(right).getActions().length >= 1) {
						interfaces.getComponent(COLLECT_INTERFACE, inter).getComponent(right).doClick();
						sleep(random(300, 500));
					}
					i++;
				}
				bankClose();
				return true;
			}
			return false;
		}

		public boolean bankClose() {
			if (bankIsOpen()) {
				interfaces.getComponent(COLLECT_INTERFACE, COLLECT_CLOSE).doClick();
				sleep(random(700, 900));
			}
			return true;
		}

	}

	private interface GEBuyMethods {
		public int getInterface();

		public int getBuyClick();

		public int getSellClick();

		public boolean isAnOfferCompleted();
	}

	private class GEBuy implements GEBuyMethods {
		private int Interface = 0;
		private int buyClick = 0;
		private int sellClick = 0;
		private int completeWidth = 124;
		private int height = 13;
		private int COMPLETION_BAR_INTERFACE = 13;

		public GEBuy(int slot) {
			switch (slot) {
				case 1:
					this.Interface = 19;
					this.buyClick = 30;
					this.sellClick = 29;
					break;

				case 2:
					this.Interface = 35;
					this.buyClick = 46;
					this.sellClick = 45;
					break;

				case 3:
					this.Interface = 51;
					this.buyClick = 62;
					this.sellClick = 61;
					break;

				case 4:
					this.Interface = 70;
					this.buyClick = 81;
					this.sellClick = 80;
					break;

				case 5:
					this.Interface = 89;
					this.buyClick = 100;
					this.sellClick = 99;
					break;

				case 6:
					this.Interface = 108;
					this.buyClick = 119;
					this.sellClick = 118;
					break;
			}
		}

		public GEBuy() {

		}

		public int getInterface() {
			return this.Interface;
		}

		public int getBuyClick() {
			return this.buyClick;
		}

		public int getSellClick() {
			return this.sellClick;
		}

		public boolean isAnOfferCompleted() {
			if (grandExchange.isOpen()) {
				int boxToCollect = getTotalSlots();
				for (int i = 1; i <= boxToCollect;) {
					GEBuyMethods k = new GEBuy(i);
					int inter = k.getInterface();
					if (interfaces.getComponent(GE_INTERFACE, inter).getComponent(COMPLETION_BAR_INTERFACE) != null
					        && interfaces.getComponent(GE_INTERFACE, inter).getComponent(COMPLETION_BAR_INTERFACE)
					            .getHeight() == height
					        && interfaces.getComponent(GE_INTERFACE, inter).getComponent(COMPLETION_BAR_INTERFACE)
					            .getWidth() == completeWidth) {
						return true;
					}
					i++;
				}
			}
			return false;
		}
	}

	/**
	 * Checks whether or not your inventory contains the provided item name.
	 * 
	 * @param name The item(s) you wish to evaluate.
	 * @return <tt>true</tt> if your inventory contains an item with the name provided; otherwise <tt>false</tt>.
	 */
	public boolean contains(final String name) {
		return getItem(name) != null;
	}

	/**
	 * Gets the first item in the inventory containing any of the provided names.
	 * 
	 * @param names The names of the item to find.
	 * @return The first <tt>RSItem</tt> for the given name(s); otherwise null.
	 */
	public RSItem getItem(final String... names) {
		for (final RSItem item : inventory.getItems()) {
			String name = item.getName();
			if (name != null) {
				name = name.toLowerCase();
				for (final String n : names) {
					if (n != null && name.contains(n.toLowerCase())) {
						return item;
					}
				}
			}
		}
		return null;
	}

	private void moveCamera(RSObject t) {
		switch (random(0, 2)) {
			case 0:
				switch (random(0, 2)) {
					case 0:
						antiBan = "moveCamera Randomly";
						camera.moveRandomly(random(500, 1000));
						break;
					case 1:
						antiBan = "moveCamera setPitch";
						camera.setPitch(100);
						break;
				}
			case 1:
				antiBan = "moveCamera turnTo";
				camera.turnTo(t);
				break;
		}
		antiBan = "";
	}

	private void antiBan() {
		int num = random(0, 100);
		int j = 10;
		int k = 20;
		if (num < k)
			j = 0;
		if (num >= k && num < k + 5) // 40-45
			j = 1;
		k += 5;
		if (num >= k && num < k + 5) // 45-50
			j = 2;
		k += 5;
		if (num >= k && num < k + 5) // 50-55
			j = 3;
		k += 5;
		if (num >= k && num < k + 5) // 55-60
			j = 4;
		k += 5;
		if (num >= k && num < k + 5) // 60-65
			j = 5;
		k += 5;
		if (num >= k && num < k + 5) // 65-70
			j = 6;
		k += 5;
		if (num >= k && num < k + 15) // 70-85
			j = 7;
		k += 15;
		if (num >= k && num < k + 15) // 85-100
			j = 8;
		if (timer < System.currentTimeMillis()) {
			switch (j) {
				case 0:
					antiBan = "0 Advanced Camera";
					advancedCameraMovement();
					break;
				case 1:
					antiBan = "1 mouse.moveOffScreen";
					mouse.moveOffScreen();
					sleep(random(1000, 1500));
					break;
				case 2:
					antiBan = "2 mouse.moveRandomly and sleep";
					mouse.moveRandomly(200, 600);
					sleep(random(300, 500));
					break;
				case 3:
					antiBan = "3 mouse.moveRandomly";
					mouse.moveRandomly(random(100, 500));
					break;
				case 4:
					antiBan = "4 mouse.moveSlightly";
					mouse.moveSlightly();
					sleep(random(300, 500));
					break;
				case 5:
					antiBan = "5 skills.doHover INTERFACE";
					skills.doHover(Skills.INTERFACE_WOODCUTTING);
					sleep(random(500, 2500));
					break;
				case 6:
					antiBan = "6 mouse.move";
					mouse.move(random(527, 200), random(744, 464));
					break;
				case 7:
					antiBan = "7 camera.moveRandomly";
					for (int i = 0; i <= random(0, 3); i++) { // (0, n) n is number of repeats
						camera.moveRandomly(random(300, 800));
					}
					break;
				case 8:
					antiBan = "8 camera.setPitch";
					camera.setPitch(100);
					break;
				default:
					break;
			}
			timer = System.currentTimeMillis() + random(2000, 10000);
		}
		antiBan = "";
	}

	private void advancedCameraMovement() {
		int random1 = random(300, 600);
		int random2 = random(300, 600);
		if (random(0, 2) == 0) {
			keyboard.pressKey((char) KeyEvent.VK_RIGHT);
		} else {
			keyboard.pressKey((char) KeyEvent.VK_LEFT);
		}
		sleep(random(200, 500));
		if (random(0, 2) == 0) {
			keyboard.pressKey((char) KeyEvent.VK_UP);
		} else {
			keyboard.pressKey((char) KeyEvent.VK_DOWN);
		}
		if (random(0, 2) == 0) {
			sleep(random1);
			keyboard.releaseKey((char) KeyEvent.VK_RIGHT);
			keyboard.releaseKey((char) KeyEvent.VK_LEFT);
			sleep(random(200, 500));
			keyboard.releaseKey((char) KeyEvent.VK_UP);
			keyboard.releaseKey((char) KeyEvent.VK_DOWN);
		} else {
			sleep(random2);
			keyboard.releaseKey((char) KeyEvent.VK_UP);
			keyboard.releaseKey((char) KeyEvent.VK_DOWN);
			sleep(random(200, 500));
			keyboard.releaseKey((char) KeyEvent.VK_RIGHT);
			keyboard.releaseKey((char) KeyEvent.VK_LEFT);
		}
	}

	@Override
	public void messageReceived(MessageEvent e) {
		String m = e.getMessage();
		if (m.contains("just advanced a Firemaking")) {
			levelsGained2++;
		}
		if (m.contains("just advanced a Woodcutting")) {
			levelsGained++;
		}
		if (m.contains("just advanced 2 Wood")) {
			levelsGained += 2;
		}
		if (m.contains("just advanced 3 Wood")) {
			levelsGained += 3;
		}
		if (m.contains("just advanced 4 Wood")) {
			levelsGained += 4;
		}
		if (m.contains("can't light a fire")) {
			findNewTile(start, false);
		}
		if (m.contains("the ladder has been completely destroyed")) {
			sawLamp = true;
		}
//		if (m.contains("Ramsey, is that you?")) {
//			keyboard.sendText("Shh.", true);
//		}
	}

	public State getState() {
		if ((wcLvl() >= stopAtWC || after60WC == 1) || (fmLvl() >= stopAtFM && afterXFM == 1)) {
			status = "Ending";
			end = true;
		}
		if (fmLvl() >= stopAtFM && afterXFM == 0) {
			trainFM = false;
		}
		if (interfaces.get(244).containsText("may I ask you to speak") || needTutoring) {
			status = "GE Tutor";
			needTutoring = true;
			return State.GETUTOR;
		}
		if (checkBank) {
			status = "Banking";
			return State.CHECKBANK;
		}
		if (runeHatchetPrice * 1.2 < totalCash && !inventory.contains(runeHatchetID) && wcLvl() >= 41
		        && !useAvailableHatchets) {
			status = "Rune hatchet";
			return State.BUYRUNEHATCHET;
		}
		if (skills.getCurrentExp(Skills.WOODCUTTING) < 500 && !sawLamp) {
			status = "Lamps";
			return State.LAMPS;
		}
		if (inventory.contains(13439)) {
			status = "Lamps";
			return State.LAMPS;
		}
		if (npcs.getNearest(7942) != null) {
			if (calc.distanceTo(npcs.getNearest(7942)) < 10) {
				status = "Lamps";
				return State.LAMPS;
			}
		}
		if (trainFM && !inventory.contains("Tinderbox")) {
			status = "Tinderbox";
			return State.BUYTINDERBOX;
		}
		if (!useAvailableHatchets) {
			bestHatchetAvailable = bestHatchetAvailable();
			if ((bestHatchetAvailable == bronzeHatchetID || bestHatchetAvailable == -1)
			        && !inventory.contains(bronzeHatchetID)) {
				status = "Buying hatchet";
				return State.BUYHATCHET;
			}
			if (inventory.contains(bronzeHatchetID)) {
				if (!inventory.contains(steelHatchetID)) {
					if (inventory.contains("Coins")) {
						if (inventory.getItem("Coins").getStackSize() >= 200) {
							status = "Buying hatchet";
							return State.BUYHATCHET;
						}
					}
				}
			}
			if (!inventory.contains(bestHatchetAvailable)) {
				status = "Upgrading Hatchet";
				return State.CHECKBANK;
			}
		}
		if (burnLogs) {
			status = "Burning logs";
			return State.BURN;
		}
		if (useAvailableHatchets) {
			if (!checkedBank) {
				status = "Hatchet count";
				return State.CHECKBANK;
			}
			bestHatchetAvailable = bestHatchetAvailable();
			RSItem r = inventory.getItem(bestHatchetAvailable);
			if (bestHatchetAvailable != -1) {
				if (r == null) {
					status = "Upgrading Hatchet";
					return State.CHECKBANK;
				}
			}
			if (checkedBank && bestHatchetAvailable == -1) {
				log("No hatchet available in your bank or inventory for your level.");
				end = true;
			}
		}
		if (wcLvl() >= 60 && after60WC == 2) {
			status = "Yews";
			return State.YEW;
		}
		if (trainFM && fmLvl() < stopAtFM) {
			if (fmLvl() >= 30) {
				if (wcLvl() >= 30) {
					if (willowLocation == 0) {
						status = "Burn Willows";
						return State.WILLOWLUMB;
					}
					if (willowLocation == 1) {
						status = "Burn Willows";
						return State.WILLOWRIMM;
					}
					if (willowLocation == 2) {
						status = "Burn Willows";
						return State.WILLOWPORT;
					}
				}
			}
			if (fmLvl() >= 15) {
				if (wcLvl() < 15) {
					status = "Burn Trees";
					return State.TREE;
				}
				if (wcLvl() >= 15) {
					status = "Burn Oaks";
					return State.OAK;
				}
			}
			if (fmLvl() < 15) {
				status = "Burn Trees";
				return State.TREE;
			}
		}
		if (wcLvl() >= 30) {
			if (willowLocation == 0) {
				status = "Willows";
				return State.WILLOWLUMB;
			}
			if (willowLocation == 1) {
				status = "Willows";
				return State.WILLOWRIMM;
			}
			if (willowLocation == 2) {
				status = "Willows";
				return State.WILLOWPORT;
			}
		}
		if (wcLvl() < 15) {
			status = "Trees";
			return State.TREE;
		}
		if (wcLvl() >= 15 && wcLvl() < 30) {
			status = "Oaks";
			return State.OAK;
		}
		status = "Error.";
		return State.error;
	}
}