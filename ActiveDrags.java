import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.util.LinkedList;

import javax.swing.JOptionPane;

import org.Constants.MISC;
import org.rsbot.event.events.MessageEvent;
import org.rsbot.event.listeners.MessageListener;
import org.rsbot.event.listeners.PaintListener;
import org.rsbot.script.Script;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.wrappers.RSNPC;
import org.rsbot.script.wrappers.RSTile;
import org.rsbot.script.wrappers.RSTilePath;
import org.rsbot.script.wrappers.RSWeb;

@ScriptManifest(authors = { "Swipe" }, keywords = "Combat, loot", name = "ActiveDrags", version = 1.01, description = "Kills Green Drags EAST varrock teletab")
public class ActiveDrags extends Script implements PaintListener,
MessageListener {
	final static int HIDE = 1753;
	final static int FTAB = 8009;
	final static int BONES = 536;
	final static int D_WEED = 217;
	// East
	final static RSTile E_BANKL = new RSTile(3186, 3439);
	final static RSTile E_DITCHL = new RSTile(3137, 3520);
	final static int E_DITCHO = 1440;
	final static RSTile E_DRAGL = new RSTile(3336, 3681);
	final static RSTile VARROCK = new RSTile(3212, 3425);
	// West
	public final static RSTile W_BANKLOC = new RSTile(2945, 3370);
	public final static RSTile W_DITCHLOC = new RSTile(2947, 3522);
	public final static RSTile W_FALALOC = new RSTile(2965, 3377);
	public final static RSTile W_DRAGLOC = new RSTile(2978, 3616);
	public static final RSTile[] W_DITCHPATH = { new RSTile(2944, 3369),
		new RSTile(2945, 3376), new RSTile(2949, 3379),
		new RSTile(2953, 3382), new RSTile(2958, 3382),
		new RSTile(2963, 3384), new RSTile(2964, 3389),
		new RSTile(2964, 3394), new RSTile(2963, 3399),
		new RSTile(2962, 3404), new RSTile(2961, 3409),
		new RSTile(2959, 3414), new RSTile(2956, 3418),
		new RSTile(2952, 3421), new RSTile(2949, 3425),
		new RSTile(2949, 3430), new RSTile(2948, 3435),
		new RSTile(2946, 3440), new RSTile(2945, 3445),
		new RSTile(2945, 3450), new RSTile(2944, 3455),
		new RSTile(2944, 3460), new RSTile(2945, 3465),
		new RSTile(2945, 3470), new RSTile(2945, 3475),
		new RSTile(2944, 3480), new RSTile(2941, 3485),
		new RSTile(2940, 3490), new RSTile(2939, 3495),
		new RSTile(2937, 3500), new RSTile(2939, 3505),
		new RSTile(2942, 3509), new RSTile(2943, 3514),
		new RSTile(2944, 3519) };
	public static final RSTile[] W_DRAGPATH = { new RSTile(2946, 3525),
		new RSTile(2951, 3525), new RSTile(2956, 3526),
		new RSTile(2961, 3526), new RSTile(2966, 3528),
		new RSTile(2969, 3532), new RSTile(2972, 3536),
		new RSTile(2973, 3541), new RSTile(2974, 3546),
		new RSTile(2976, 3551), new RSTile(2977, 3556),
		new RSTile(2978, 3561), new RSTile(2979, 3566),
		new RSTile(2980, 3571), new RSTile(2980, 3576),
		new RSTile(2981, 3581), new RSTile(2981, 3586),
		new RSTile(2982, 3591), new RSTile(2982, 3596),
		new RSTile(2982, 3601), new RSTile(2981, 3606),
		new RSTile(2979, 3611), new RSTile(2977, 3616) };
	//
	public final static int[] dragons = { 4679, 4680, 941 };
	private static final Color MOUSE_COLOR = new Color(139, 69, 19),
	MOUSE_BORDER_COLOR = new Color(0, 153, 0),
	MOUSE_CENTER_COLOR = new Color(139, 69, 19);
	String line[] = new String[5];
	long startTime;
	Point p;
	Point p2;
	int foodArray[] = { 333, 329, 361, 379, 373, 385, 15266 };
	int SETLOC = 0; // Green East, Green West, Green Chaos, ...
	int paintState = 0;// 0,1,2
	int thideCount = 0;
	int hideCount = 0;
	int tboneCount = 0;
	int boneCount = 0;
	MyGUI m;
	String status = "";
	private final LinkedList<MousePathPoint> mousePath = new LinkedList<MousePathPoint>();

	// Green Drags
	static enum State {
		E_DITCH, W_DITCH, FIGHT, TO_WDRAGS, TO_EDRAGS, BANK, TO_WBANK, TO_EBANK, TO_WDITCH, TO_EDITCH, TELE, EAT, WAIT, LOOT
	}

	private int food = 333;
	private int amount = 10;

	public boolean onStart() {
		// String s =
		// (String)JOptionPane.showInputDialog(null,"Food id, amount. ex (333,10)");
		startTime = System.currentTimeMillis();
		hidep = grandExchange.lookup(HIDE).getGuidePrice();
		bonep = grandExchange.lookup(BONES).getGuidePrice();
		m = new MyGUI();
		m.setVisible(true);
		while (m.isVisible()) {
			sleep(100);
		}
		return true;
	}

	public State getState() {
		/*
		 * Green Dragons States
		 */
		if (players.getMyPlayer().isInCombat()) {
			return State.WAIT;
		}
		if (nearArea(E_DRAGL, 15) || nearArea(W_DRAGLOC, 15)) {
			if (groundItems.getNearest(HIDE, BONES, D_WEED) != null) {
				return State.LOOT;
			}
			return State.FIGHT;
		}

		if (nearArea(VARROCK, 8)) {
			return State.TO_EBANK;
		}
		if (nearArea(W_FALALOC, 8)) {
			return State.TO_WBANK;
		}
		if (nearArea(E_BANKL, 10)) {
			if (inventory.contains(BONES)) {
				return State.BANK;
			} else {
				return State.TO_EDITCH;
			}
		}
		if (nearArea(W_BANKLOC, 10)) {
			if (inventory.contains(BONES)) {
				return State.BANK;
			} else {
				return State.TO_WDITCH;
			}
		}
		if (inventory.getCount(food) < 1
				|| (inventory.getCount(HIDE) + inventory.getCount(BONES)) > 26) {
			return State.TELE;
		}

		if (nearArea(E_DITCHL, 5)) {
			if (getMyPlayer().getLocation().getY() < E_DITCHL.getY()) {
				return State.E_DITCH;
			} else {
				return State.TO_EDRAGS;
			}
		}
		if (nearArea(W_DITCHLOC, 5)) {
			if (getMyPlayer().getLocation().getY() < W_DITCHLOC.getY()) {
				return State.W_DITCH;
			} else {
				return State.TO_WDRAGS;
			}
		}
		/*
		 * End Green Drag States
		 */
		return State.WAIT;
	}

	public int getHides() {
		int temp = thideCount;
		thideCount = inventory.getCount(HIDE);
		if (thideCount - temp == 0) {
			return hideCount;
		} else {
			hideCount += Math.abs(thideCount - temp);
		}
		return hideCount;
	}

	public int getBones() {
		int temp = tboneCount;
		tboneCount = inventory.getCount(HIDE);
		if (tboneCount - temp == 0) {
			return boneCount;
		} else {
			boneCount += Math.abs(tboneCount - temp);
		}
		return boneCount;
	}

	public int PerHour(int i) {
		return (int) Math.ceil(i * 3600000D
				/ Math.abs(startTime - System.currentTimeMillis()));
	}

	@Override
	public void messageReceived(MessageEvent arg0) {
		// TODO Auto-generated method stub

	}

	private int WalkAttempts = 0;

	private void walkTo(final RSTile tile) {
		while (calc.distanceTo(tile) > 6) {
			RSWeb temp = web.getWeb(getMyPlayer().getLocation(),
					tile.randomize(1, 1));
			temp.step();
			sleep(random(300, 1000));
			if (!getMyPlayer().isMoving()) {
				WalkAttempts++;
			} else {
				WalkAttempts = 0;
			}
			if (WalkAttempts > random(2, 5)) {
				log("Having problems walking...");
				WalkAttempts = 0;
			}
		}
	}

	private boolean nearArea(RSTile t, int n) {
		return calc.distanceTo(t) <= n;
	}

	private final RenderingHints rh = new RenderingHints(
			RenderingHints.KEY_TEXT_ANTIALIASING,
			RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

	@Override
	public void onRepaint(Graphics g) {
		drawMouse(g);

		((Graphics2D) g).setRenderingHints(rh);
		g.setColor(new Color(153, 153, 153, 213));
		g.fillRect(74, 300, 576, 164);
		g.setColor(new Color(0, 102, 0));
		g.fillRect(99, 325, 77, 29);
		g.setColor(new Color(0, 102, 0));
		g.fillRect(98, 361, 77, 28);
		g.setColor(new Color(0, 102, 0));
		g.fillRect(101, 398, 76, 29);
		g.setColor(new Color(204, 0, 51));
		g.fillRect(628, 303, 20, 17);
		g.setColor(new Color(153, 153, 0));
		g.fillRect(604, 303, 22, 17);
		g.setColor(new Color(51, 51, 51, 152));
		g.fillRoundRect(185, 313, 413, 132, 4, 4);
		g.setFont(new Font("Arial Black", 0, 11));
		g.setColor(new Color(0, 0, 0, 100));
		g.drawString("" + line[0], 236, 339);
		g.setColor(new Color(255, 255, 255));
		g.drawString("" + line[0], 233, 336);
		g.setFont(new Font("Arial Black", 0, 11));
		g.setColor(new Color(0, 0, 0, 100));
		g.drawString("" + line[1], 235, 361);
		g.setColor(new Color(255, 255, 255));
		g.drawString("" + line[1], 232, 358);
		g.setFont(new Font("Arial Black", 0, 11));
		g.setColor(new Color(0, 0, 0, 100));
		g.drawString("" + line[2], 235, 383);
		g.setColor(new Color(255, 255, 255));
		g.drawString("" + line[2], 232, 380);
		g.setFont(new Font("Arial Black", 0, 11));
		g.setColor(new Color(0, 0, 0, 100));
		g.drawString("" + line[3], 236, 404);
		g.setColor(new Color(255, 255, 255));
		g.drawString("" + line[3], 233, 401);
		g.setFont(new Font("Arial Black", 0, 11));
		g.setColor(new Color(0, 0, 0, 100));
		g.drawString("" + line[4], 237, 423);
		g.setColor(new Color(255, 255, 255));
		g.drawString("" + line[4], 234, 420);
		g.setFont(new Font("Arial Black", 0, 11));
		g.setColor(new Color(0, 0, 0, 100));
		g.drawString("Status: " + status, 307, 312);
		g.setColor(new Color(255, 255, 255));
		g.drawString("Status: " + status, 304, 309);
	}

	public void attackNearestDragon() {
		if (players.getMyPlayer().getHPPercent() < 41) {
			inventory.getItem(food).interact("Eat");
		}
		if (!players.getMyPlayer().isInCombat()) {
			RSNPC Dragon[] = ctx.npcs.getAll(dragons);
			for (RSNPC d : Dragon) {
				if (d != null) {
					if (!d.isInCombat()) {
						if (d.isOnScreen()) {
							d.interact("Attack");
							ctx.env.sleep(300);
						} else {
							ctx.camera.turnTo(d);
						}
					} else {
						if (ctx.env.random(0, 6) == 3) {
							ctx.camera.setAngle(ctx.env.random(1, 359));
						}
						ctx.env.sleep(2000);
					}
				}
			}
		}
	}

	int hidep;
	int bonep;

	@Override
	public int loop() {
		//Paint
		status = getState().toString().toLowerCase();
		switch (paintState) {
		case 0:
			line[0] = "ActiveDrags - Profit";
			line[1] = "Hides: " + getHides() + " - " + (hidep * getHides());
			line[2] = "Hides/h: " + PerHour(getHides()) + " - "
			+ (hidep * PerHour(getHides()));
			line[3] = "Bones: " + getBones() + " - " + (bonep * getBones());
			line[4] = "Bones/h: " + PerHour(getBones()) + " - "
			+ (bonep * PerHour(getBones()));
			break;
		case 1:
			break;
		case 2:
			break;
		}

		switch (getState()) {
		/*
		 * Green Drag Loop
		 */
		case BANK:
			if (bank.isOpen()) {
				bank.depositAll();
				bank.withdraw(food, amount);
				sleep(500);
				bank.withdraw(getTabForLoc(), 1);
				bank.close();
			} else {
				bank.open();
			}
		case TO_EBANK:
			walkTo(E_BANKL);
			break;
		case TO_WBANK:
			walkTo(E_BANKL);
			break;
		case TO_EDITCH:
			walkTo(E_DITCHL);
			break;
		case TO_WDITCH:
			RSTilePath myPath = ctx.walking.newTilePath(W_DITCHPATH);
			while (ctx.calc.distanceTo(W_DITCHLOC) > 5) {
				myPath.traverse();
				ctx.env.sleep(500);
			}
			break;
		case E_DITCH:
			RSTile l = getMyPlayer().getLocation();
			objects.getNearest(E_DITCHO).doClick();
			sleep(random(700, 2000));
			if (getMyPlayer().getLocation().equals(l)) {
				log("ditch error! fixing!");
				objects.getNearest(E_DITCHO).doClick();
			}
			break;
		case W_DITCH:
			RSTile a = getMyPlayer().getLocation();
			mouse.click(ctx.calc.tileToScreen(W_DITCHLOC), true);
			sleep(random(1700, 2000));
			if (getMyPlayer().getLocation().equals(a)) {
				log("ditch error! fixing!");
				mouse.click(ctx.calc.tileToScreen(W_DITCHLOC), true);
			}
			break;
		case TO_EDRAGS:
			walkTo(E_DRAGL);
			break;
		case TO_WDRAGS:
			RSTilePath MyPath = walking.newTilePath(W_DRAGPATH);
			while(calc.distanceTo(W_DRAGLOC) < 10){
				myPath.traverse();
				ctx.env.sleep(500);
			}
			break;
		case FIGHT:
			attackNearestDragon();

			break;
		case LOOT:
			if (inventory.isFull() && inventory.contains(food)) {
				inventory.getItem(food).interact("Eat");
			}
			groundItems.getNearest(HIDE, BONES, D_WEED).interact("take");
			sleep(random(500, 1000));
			break;

		case TELE:
			while (combat.getWildernessLevel() > 20) {
				walking.walkTo(new RSTile(getMyPlayer().getLocation().getX(),
						getMyPlayer().getLocation().getY() - 15));
			}
			inventory.getItem(getTabForLoc()).interact("break");
			sleep(4000);
			break;
		case WAIT:
			if (players.getMyPlayer().getHPPercent() < 41) {
				inventory.getItem(food).interact("Eat");
			}
			inventory.dropAllExcept(HIDE, BONES, D_WEED, 8007, food);
			if (random(1, 30) == 3) {
				camera.setAngle(camera.getAngle() + random(3, 90));
			}
			break;
		}
		/*
		 * End Green Drag Loop
		 */
		return random(100, 300);
	}

	private int getTabForLoc() {
		if (SETLOC == 0) {
			return 8007;
		}
		if (SETLOC == 1) {
			return 8009;
		}
		return -1;
	}

	private void drawMouse(Graphics g1) {
		((Graphics2D) g1).setRenderingHints(new RenderingHints(
				RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON));
		Point p = mouse.getLocation();
		Graphics2D spinG = (Graphics2D) g1.create();
		Graphics2D spinGRev = (Graphics2D) g1.create();
		Graphics2D spinG2 = (Graphics2D) g1.create();
		spinG.setColor(MOUSE_BORDER_COLOR);
		spinGRev.setColor(MOUSE_COLOR);
		spinG.rotate(System.currentTimeMillis() % 2000d / 2000d * (360d) * 2
				* Math.PI / 180.0, p.x, p.y);
		spinGRev.rotate(System.currentTimeMillis() % 2000d / 2000d * (-360d)
				* 2 * Math.PI / 180.0, p.x, p.y);
		final int outerSize = 20;
		final int innerSize = 12;
		spinG.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_ROUND,
				BasicStroke.JOIN_ROUND));
		spinGRev.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_ROUND,
				BasicStroke.JOIN_ROUND));
		spinG.drawArc(p.x - (outerSize / 2), p.y - (outerSize / 2), outerSize,
				outerSize, 100, 75);
		spinG.drawArc(p.x - (outerSize / 2), p.y - (outerSize / 2), outerSize,
				outerSize, -100, 75);
		spinGRev.drawArc(p.x - (innerSize / 2), p.y - (innerSize / 2),
				innerSize, innerSize, 100, 75);
		spinGRev.drawArc(p.x - (innerSize / 2), p.y - (innerSize / 2),
				innerSize, innerSize, -100, 75);
		g1.setColor(MOUSE_CENTER_COLOR);
		g1.fillOval(p.x, p.y, 2, 2);
		spinG2.setColor(MOUSE_CENTER_COLOR);
		spinG2.rotate(System.currentTimeMillis() % 2000d / 2000d * 360d
				* Math.PI / 180.0, p.x, p.y);
		spinG2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_ROUND,
				BasicStroke.JOIN_ROUND));
		spinG2.drawLine(p.x - 5, p.y, p.x + 5, p.y);
		spinG2.drawLine(p.x, p.y - 5, p.x, p.y + 5);
	}

	public class MyGUI extends javax.swing.JFrame {

		/** Creates new form MyGUI */
		public MyGUI() {
			initComponents();
		}

		/**
		 * This method is called from within the constructor to initialize the
		 * form. WARNING: Do NOT modify this code. The content of this method is
		 * always regenerated by the Form Editor.
		 */
		@SuppressWarnings("unchecked")
		// <editor-fold defaultstate="collapsed" desc="Generated Code">
		private void initComponents() {

			jTabbedPane1 = new javax.swing.JTabbedPane();
			jPanel1 = new javax.swing.JPanel();
			jLabel1 = new javax.swing.JLabel();
			jComboBox1 = new javax.swing.JComboBox();
			jLabel7 = new javax.swing.JLabel();
			jComboBox7 = new javax.swing.JComboBox();
			jLabel8 = new javax.swing.JLabel();
			jComboBox8 = new javax.swing.JComboBox();
			jCheckBox1 = new javax.swing.JCheckBox();
			jScrollPane1 = new javax.swing.JScrollPane();
			jList1 = new javax.swing.JList();
			jLabel9 = new javax.swing.JLabel();
			jButton1 = new javax.swing.JButton();
			jScrollPane2 = new javax.swing.JScrollPane();
			jList2 = new javax.swing.JList();
			jLabel10 = new javax.swing.JLabel();
			jLabel11 = new javax.swing.JLabel();
			jButton2 = new javax.swing.JButton();
			jLabel12 = new javax.swing.JLabel();
			jCheckBox2 = new javax.swing.JCheckBox();
			jButton3 = new javax.swing.JButton();
			jCheckBox3 = new javax.swing.JCheckBox();
			jLabel38 = new javax.swing.JLabel();
			jScrollPane13 = new javax.swing.JScrollPane();
			jTextPane1 = new javax.swing.JTextPane();
			jPanel2 = new javax.swing.JPanel();
			jButton4 = new javax.swing.JButton();
			jButton5 = new javax.swing.JButton();
			jScrollPane3 = new javax.swing.JScrollPane();
			jList3 = new javax.swing.JList();
			jButton6 = new javax.swing.JButton();
			jScrollPane4 = new javax.swing.JScrollPane();
			jList4 = new javax.swing.JList();
			jLabel13 = new javax.swing.JLabel();
			jComboBox9 = new javax.swing.JComboBox();
			jLabel14 = new javax.swing.JLabel();
			jComboBox2 = new javax.swing.JComboBox();
			jLabel2 = new javax.swing.JLabel();
			jComboBox10 = new javax.swing.JComboBox();
			jLabel15 = new javax.swing.JLabel();
			jCheckBox4 = new javax.swing.JCheckBox();
			jLabel16 = new javax.swing.JLabel();
			jCheckBox5 = new javax.swing.JCheckBox();
			jCheckBox6 = new javax.swing.JCheckBox();
			jLabel37 = new javax.swing.JLabel();
			jPanel3 = new javax.swing.JPanel();
			jButton7 = new javax.swing.JButton();
			jButton8 = new javax.swing.JButton();
			jScrollPane5 = new javax.swing.JScrollPane();
			jList5 = new javax.swing.JList();
			jButton9 = new javax.swing.JButton();
			jScrollPane6 = new javax.swing.JScrollPane();
			jList6 = new javax.swing.JList();
			jLabel17 = new javax.swing.JLabel();
			jComboBox11 = new javax.swing.JComboBox();
			jLabel18 = new javax.swing.JLabel();
			jComboBox3 = new javax.swing.JComboBox();
			jLabel3 = new javax.swing.JLabel();
			jComboBox12 = new javax.swing.JComboBox();
			jLabel19 = new javax.swing.JLabel();
			jCheckBox7 = new javax.swing.JCheckBox();
			jLabel20 = new javax.swing.JLabel();
			jCheckBox8 = new javax.swing.JCheckBox();
			jCheckBox9 = new javax.swing.JCheckBox();
			jLabel36 = new javax.swing.JLabel();
			jPanel4 = new javax.swing.JPanel();
			jButton10 = new javax.swing.JButton();
			jButton11 = new javax.swing.JButton();
			jScrollPane7 = new javax.swing.JScrollPane();
			jList7 = new javax.swing.JList();
			jButton12 = new javax.swing.JButton();
			jScrollPane8 = new javax.swing.JScrollPane();
			jList8 = new javax.swing.JList();
			jLabel21 = new javax.swing.JLabel();
			jComboBox13 = new javax.swing.JComboBox();
			jLabel22 = new javax.swing.JLabel();
			jComboBox4 = new javax.swing.JComboBox();
			jLabel4 = new javax.swing.JLabel();
			jComboBox14 = new javax.swing.JComboBox();
			jLabel23 = new javax.swing.JLabel();
			jCheckBox10 = new javax.swing.JCheckBox();
			jLabel24 = new javax.swing.JLabel();
			jCheckBox11 = new javax.swing.JCheckBox();
			jCheckBox12 = new javax.swing.JCheckBox();
			jLabel35 = new javax.swing.JLabel();
			jPanel5 = new javax.swing.JPanel();
			jButton13 = new javax.swing.JButton();
			jButton14 = new javax.swing.JButton();
			jScrollPane9 = new javax.swing.JScrollPane();
			jList9 = new javax.swing.JList();
			jButton15 = new javax.swing.JButton();
			jScrollPane10 = new javax.swing.JScrollPane();
			jList10 = new javax.swing.JList();
			jLabel25 = new javax.swing.JLabel();
			jComboBox15 = new javax.swing.JComboBox();
			jLabel26 = new javax.swing.JLabel();
			jComboBox5 = new javax.swing.JComboBox();
			jLabel5 = new javax.swing.JLabel();
			jComboBox16 = new javax.swing.JComboBox();
			jLabel27 = new javax.swing.JLabel();
			jCheckBox13 = new javax.swing.JCheckBox();
			jLabel28 = new javax.swing.JLabel();
			jCheckBox14 = new javax.swing.JCheckBox();
			jCheckBox15 = new javax.swing.JCheckBox();
			jLabel34 = new javax.swing.JLabel();
			jPanel6 = new javax.swing.JPanel();
			jButton16 = new javax.swing.JButton();
			jButton17 = new javax.swing.JButton();
			jScrollPane11 = new javax.swing.JScrollPane();
			jList11 = new javax.swing.JList();
			jButton18 = new javax.swing.JButton();
			jScrollPane12 = new javax.swing.JScrollPane();
			jList12 = new javax.swing.JList();
			jLabel29 = new javax.swing.JLabel();
			jComboBox17 = new javax.swing.JComboBox();
			jLabel30 = new javax.swing.JLabel();
			jComboBox6 = new javax.swing.JComboBox();
			jLabel6 = new javax.swing.JLabel();
			jComboBox18 = new javax.swing.JComboBox();
			jLabel31 = new javax.swing.JLabel();
			jCheckBox16 = new javax.swing.JCheckBox();
			jLabel32 = new javax.swing.JLabel();
			jCheckBox17 = new javax.swing.JCheckBox();
			jCheckBox18 = new javax.swing.JCheckBox();
			jLabel33 = new javax.swing.JLabel();

			setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

			jLabel1.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
			jLabel1.setText("Food");

			jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(
					new String[] { "Trout", "Salmon", "Tuna", "Lobster",
							"SwordFish", "Shark", "CaveFish", " " }));

			jLabel7.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
			jLabel7.setText("Location");

			jComboBox7.setModel(new javax.swing.DefaultComboBoxModel(
					new String[] { "East", "West", "Chaos Tunnels", " " }));

			jLabel8.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
			jLabel8.setText("Familiar");

			jComboBox8.setModel(new javax.swing.DefaultComboBoxModel(
					new String[] { "None", "Snail", "Terrorbird", "Tortise",
					"Yak" }));

			jCheckBox1.setText("Cannon");

			jList1.setModel(new javax.swing.AbstractListModel() {
				String[] strings = { "Dragon Bones", "Dragon Hide" };

				public int getSize() {
					return strings.length;
				}

				public Object getElementAt(int i) {
					return strings[i];
				}
			});
			jScrollPane1.setViewportView(jList1);

			jLabel9.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
			jLabel9.setText("Looting");

			jButton1.setText("Add");
			jButton1.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					jButton1ActionPerformed(evt);
				}
			});

			jList2.setModel(new javax.swing.AbstractListModel() {
				String[] strings = { "Grimy Rannar", "Rune Dagger", "Law Rune",
				" " };

				public int getSize() {
					return strings.length;
				}

				public Object getElementAt(int i) {
					return strings[i];
				}
			});
			jScrollPane2.setViewportView(jList2);

			jLabel10.setText("->");

			jLabel11.setText("<-");

			jButton2.setText("Remove");
			jButton2.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					jButton2ActionPerformed(evt);
				}
			});

			jLabel12.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
			jLabel12.setForeground(new java.awt.Color(102, 0, 0));
			jLabel12.setText("Settings");

			jCheckBox2.setText("Use Glory to Bank");

			jButton3.setLabel("Start Green Drags");
			jButton3.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					jButton3ActionPerformed(evt);
				}
			});

			jCheckBox3.setSelected(true);
			jCheckBox3.setText("Tele in combat");

			jLabel38.setText("Amount:");

			jScrollPane13.setViewportView(jTextPane1);

			javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(
					jPanel1);
			jPanel1.setLayout(jPanel1Layout);
			jPanel1Layout
			.setHorizontalGroup(jPanel1Layout
					.createParallelGroup(
							javax.swing.GroupLayout.Alignment.LEADING)
							.addGroup(
									jPanel1Layout
									.createSequentialGroup()
									.addGroup(
											jPanel1Layout
											.createParallelGroup(
													javax.swing.GroupLayout.Alignment.LEADING)
													.addGroup(
															jPanel1Layout
															.createSequentialGroup()
															.addGap(27,
																	27,
																	27)
																	.addGroup(
																			jPanel1Layout
																			.createParallelGroup(
																					javax.swing.GroupLayout.Alignment.LEADING)
																					.addComponent(
																							jScrollPane2,
																							javax.swing.GroupLayout.Alignment.TRAILING,
																							javax.swing.GroupLayout.DEFAULT_SIZE,
																							70,
																							Short.MAX_VALUE)
																							.addGroup(
																									jPanel1Layout
																									.createSequentialGroup()
																									.addComponent(
																											jLabel38,
																											javax.swing.GroupLayout.DEFAULT_SIZE,
																											70,
																											Short.MAX_VALUE)
																											.addPreferredGap(
																													javax.swing.LayoutStyle.ComponentPlacement.RELATED))
																													.addGroup(
																															jPanel1Layout
																															.createSequentialGroup()
																															.addGap(10,
																																	10,
																																	10)
																																	.addComponent(
																																			jButton1,
																																			javax.swing.GroupLayout.PREFERRED_SIZE,
																																			60,
																																			javax.swing.GroupLayout.PREFERRED_SIZE)
																																			.addPreferredGap(
																																					javax.swing.LayoutStyle.ComponentPlacement.RELATED))))
																																					.addGroup(
																																							jPanel1Layout
																																							.createSequentialGroup()
																																							.addGap(22,
																																									22,
																																									22)
																																									.addComponent(
																																											jLabel1,
																																											javax.swing.GroupLayout.DEFAULT_SIZE,
																																											75,
																																											Short.MAX_VALUE)
																																											.addPreferredGap(
																																													javax.swing.LayoutStyle.ComponentPlacement.RELATED))
																																													.addGroup(
																																															jPanel1Layout
																																															.createSequentialGroup()
																																															.addContainerGap()
																																															.addComponent(
																																																	jComboBox1,
																																																	javax.swing.GroupLayout.PREFERRED_SIZE,
																																																	javax.swing.GroupLayout.DEFAULT_SIZE,
																																																	javax.swing.GroupLayout.PREFERRED_SIZE)
																																																	.addPreferredGap(
																																																			javax.swing.LayoutStyle.ComponentPlacement.RELATED))
																																																			.addGroup(
																																																					jPanel1Layout
																																																					.createSequentialGroup()
																																																					.addContainerGap()
																																																					.addComponent(
																																																							jScrollPane13,
																																																							javax.swing.GroupLayout.PREFERRED_SIZE,
																																																							87,
																																																							javax.swing.GroupLayout.PREFERRED_SIZE)
																																																							.addPreferredGap(
																																																									javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
																																																									.addGroup(
																																																											jPanel1Layout
																																																											.createParallelGroup(
																																																													javax.swing.GroupLayout.Alignment.LEADING)
																																																													.addGroup(
																																																															jPanel1Layout
																																																															.createSequentialGroup()
																																																															.addGroup(
																																																																	jPanel1Layout
																																																																	.createParallelGroup(
																																																																			javax.swing.GroupLayout.Alignment.LEADING)
																																																																			.addGroup(
																																																																					jPanel1Layout
																																																																					.createSequentialGroup()
																																																																					.addGap(3,
																																																																							3,
																																																																							3)
																																																																							.addComponent(
																																																																									jLabel9,
																																																																									javax.swing.GroupLayout.PREFERRED_SIZE,
																																																																									69,
																																																																									javax.swing.GroupLayout.PREFERRED_SIZE))
																																																																									.addGroup(
																																																																											jPanel1Layout
																																																																											.createSequentialGroup()
																																																																											.addGap(32,
																																																																													32,
																																																																													32)
																																																																													.addGroup(
																																																																															jPanel1Layout
																																																																															.createParallelGroup(
																																																																																	javax.swing.GroupLayout.Alignment.LEADING)
																																																																																	.addComponent(
																																																																																			jLabel11)
																																																																																			.addComponent(
																																																																																					jLabel10))
																																																																																					.addPreferredGap(
																																																																																							javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																																																																																							.addComponent(
																																																																																									jScrollPane1,
																																																																																									javax.swing.GroupLayout.PREFERRED_SIZE,
																																																																																									120,
																																																																																									javax.swing.GroupLayout.PREFERRED_SIZE))
																																																																																									.addGroup(
																																																																																											jPanel1Layout
																																																																																											.createSequentialGroup()
																																																																																											.addGap(77,
																																																																																													77,
																																																																																													77)
																																																																																													.addComponent(
																																																																																															jButton2)))
																																																																																															.addPreferredGap(
																																																																																																	javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																																																																																																	.addGroup(
																																																																																																			jPanel1Layout
																																																																																																			.createParallelGroup(
																																																																																																					javax.swing.GroupLayout.Alignment.LEADING)
																																																																																																					.addGroup(
																																																																																																							jPanel1Layout
																																																																																																							.createParallelGroup(
																																																																																																									javax.swing.GroupLayout.Alignment.LEADING)
																																																																																																									.addGroup(
																																																																																																											jPanel1Layout
																																																																																																											.createSequentialGroup()
																																																																																																											.addGap(104,
																																																																																																													104,
																																																																																																													104)
																																																																																																													.addGroup(
																																																																																																															jPanel1Layout
																																																																																																															.createParallelGroup(
																																																																																																																	javax.swing.GroupLayout.Alignment.LEADING)
																																																																																																																	.addComponent(
																																																																																																																			jLabel12)
																																																																																																																			.addComponent(
																																																																																																																					jCheckBox1)
																																																																																																																					.addComponent(
																																																																																																																							jCheckBox2)
																																																																																																																							.addComponent(
																																																																																																																									jCheckBox3))
																																																																																																																									.addContainerGap(
																																																																																																																											31,
																																																																																																																											Short.MAX_VALUE))
																																																																																																																											.addGroup(
																																																																																																																													javax.swing.GroupLayout.Alignment.TRAILING,
																																																																																																																													jPanel1Layout
																																																																																																																													.createSequentialGroup()
																																																																																																																													.addGap(101,
																																																																																																																															101,
																																																																																																																															101)
																																																																																																																															.addGroup(
																																																																																																																																	jPanel1Layout
																																																																																																																																	.createParallelGroup(
																																																																																																																																			javax.swing.GroupLayout.Alignment.TRAILING)
																																																																																																																																			.addComponent(
																																																																																																																																					jComboBox8,
																																																																																																																																					javax.swing.GroupLayout.PREFERRED_SIZE,
																																																																																																																																					javax.swing.GroupLayout.DEFAULT_SIZE,
																																																																																																																																					javax.swing.GroupLayout.PREFERRED_SIZE)
																																																																																																																																					.addComponent(
																																																																																																																																							jLabel8))
																																																																																																																																							.addGap(72,
																																																																																																																																									72,
																																																																																																																																									72)))
																																																																																																																																									.addGroup(
																																																																																																																																											javax.swing.GroupLayout.Alignment.TRAILING,
																																																																																																																																											jPanel1Layout
																																																																																																																																											.createSequentialGroup()
																																																																																																																																											.addComponent(
																																																																																																																																													jButton3)
																																																																																																																																													.addGap(50,
																																																																																																																																															50,
																																																																																																																																															50))))
																																																																																																																																															.addGroup(
																																																																																																																																																	jPanel1Layout
																																																																																																																																																	.createSequentialGroup()
																																																																																																																																																	.addGroup(
																																																																																																																																																			jPanel1Layout
																																																																																																																																																			.createParallelGroup(
																																																																																																																																																					javax.swing.GroupLayout.Alignment.LEADING)
																																																																																																																																																					.addGroup(
																																																																																																																																																							jPanel1Layout
																																																																																																																																																							.createSequentialGroup()
																																																																																																																																																							.addGap(103,
																																																																																																																																																									103,
																																																																																																																																																									103)
																																																																																																																																																									.addComponent(
																																																																																																																																																											jComboBox7,
																																																																																																																																																											javax.swing.GroupLayout.PREFERRED_SIZE,
																																																																																																																																																											javax.swing.GroupLayout.DEFAULT_SIZE,
																																																																																																																																																											javax.swing.GroupLayout.PREFERRED_SIZE))
																																																																																																																																																											.addGroup(
																																																																																																																																																													jPanel1Layout
																																																																																																																																																													.createSequentialGroup()
																																																																																																																																																													.addGap(114,
																																																																																																																																																															114,
																																																																																																																																																															114)
																																																																																																																																																															.addComponent(
																																																																																																																																																																	jLabel7)))
																																																																																																																																																																	.addContainerGap(
																																																																																																																																																																			216,
																																																																																																																																																																			Short.MAX_VALUE)))));
			jPanel1Layout
			.setVerticalGroup(jPanel1Layout
					.createParallelGroup(
							javax.swing.GroupLayout.Alignment.LEADING)
							.addGroup(
									jPanel1Layout
									.createSequentialGroup()
									.addGroup(
											jPanel1Layout
											.createParallelGroup(
													javax.swing.GroupLayout.Alignment.LEADING)
													.addGroup(
															jPanel1Layout
															.createSequentialGroup()
															.addGap(33,
																	33,
																	33)
																	.addGroup(
																			jPanel1Layout
																			.createParallelGroup(
																					javax.swing.GroupLayout.Alignment.LEADING)
																					.addGroup(
																							jPanel1Layout
																							.createSequentialGroup()
																							.addComponent(
																									jLabel1)
																									.addPreferredGap(
																											javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																											.addComponent(
																													jComboBox1,
																													javax.swing.GroupLayout.PREFERRED_SIZE,
																													javax.swing.GroupLayout.DEFAULT_SIZE,
																													javax.swing.GroupLayout.PREFERRED_SIZE))
																													.addGroup(
																															jPanel1Layout
																															.createSequentialGroup()
																															.addComponent(
																																	jLabel7)
																																	.addPreferredGap(
																																			javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																																			.addComponent(
																																					jComboBox7,
																																					javax.swing.GroupLayout.PREFERRED_SIZE,
																																					javax.swing.GroupLayout.DEFAULT_SIZE,
																																					javax.swing.GroupLayout.PREFERRED_SIZE)))
																																					.addPreferredGap(
																																							javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																																							.addComponent(
																																									jLabel38)
																																									.addGap(1,
																																											1,
																																											1)
																																											.addComponent(
																																													jScrollPane13,
																																													javax.swing.GroupLayout.PREFERRED_SIZE,
																																													javax.swing.GroupLayout.DEFAULT_SIZE,
																																													javax.swing.GroupLayout.PREFERRED_SIZE)
																																													.addGap(56,
																																															56,
																																															56)
																																															.addComponent(
																																																	jLabel9))
																																																	.addGroup(
																																																			jPanel1Layout
																																																			.createSequentialGroup()
																																																			.addGap(42,
																																																					42,
																																																					42)
																																																					.addComponent(
																																																							jLabel12)
																																																							.addPreferredGap(
																																																									javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																																																									.addComponent(
																																																											jCheckBox1)
																																																											.addPreferredGap(
																																																													javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																																																													.addComponent(
																																																															jCheckBox2)
																																																															.addPreferredGap(
																																																																	javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																																																																	.addComponent(
																																																																			jCheckBox3)
																																																																			.addGap(30,
																																																																					30,
																																																																					30)
																																																																					.addComponent(
																																																																							jLabel8)))
																																																																							.addGroup(
																																																																									jPanel1Layout
																																																																									.createParallelGroup(
																																																																											javax.swing.GroupLayout.Alignment.LEADING)
																																																																											.addGroup(
																																																																													jPanel1Layout
																																																																													.createSequentialGroup()
																																																																													.addPreferredGap(
																																																																															javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																																																																															.addGroup(
																																																																																	jPanel1Layout
																																																																																	.createParallelGroup(
																																																																																			javax.swing.GroupLayout.Alignment.TRAILING)
																																																																																			.addComponent(
																																																																																					jScrollPane2,
																																																																																					javax.swing.GroupLayout.PREFERRED_SIZE,
																																																																																					javax.swing.GroupLayout.DEFAULT_SIZE,
																																																																																					javax.swing.GroupLayout.PREFERRED_SIZE)
																																																																																					.addComponent(
																																																																																							jScrollPane1))
																																																																																							.addPreferredGap(
																																																																																									javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																																																																																									.addGroup(
																																																																																											jPanel1Layout
																																																																																											.createParallelGroup(
																																																																																													javax.swing.GroupLayout.Alignment.LEADING)
																																																																																													.addComponent(
																																																																																															jButton2)
																																																																																															.addComponent(
																																																																																																	jButton1)))
																																																																																																	.addGroup(
																																																																																																			jPanel1Layout
																																																																																																			.createSequentialGroup()
																																																																																																			.addGap(4,
																																																																																																					4,
																																																																																																					4)
																																																																																																					.addComponent(
																																																																																																							jComboBox8,
																																																																																																							javax.swing.GroupLayout.PREFERRED_SIZE,
																																																																																																							javax.swing.GroupLayout.DEFAULT_SIZE,
																																																																																																							javax.swing.GroupLayout.PREFERRED_SIZE)
																																																																																																							.addGap(26,
																																																																																																									26,
																																																																																																									26)
																																																																																																									.addComponent(
																																																																																																											jLabel10)
																																																																																																											.addGap(34,
																																																																																																													34,
																																																																																																													34)
																																																																																																													.addComponent(
																																																																																																															jLabel11)
																																																																																																															.addGap(13,
																																																																																																																	13,
																																																																																																																	13)
																																																																																																																	.addComponent(
																																																																																																																			jButton3,
																																																																																																																			javax.swing.GroupLayout.PREFERRED_SIZE,
																																																																																																																			35,
																																																																																																																			javax.swing.GroupLayout.PREFERRED_SIZE)))
																																																																																																																			.addContainerGap(49,
																																																																																																																					Short.MAX_VALUE)));

			jTabbedPane1.addTab("Green", jPanel1);

			jButton4.setText("Start Blue Drags");
			jButton4.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					jButton4ActionPerformed(evt);
				}
			});

			jButton5.setText("Remove");
			jButton5.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					jButton5ActionPerformed(evt);
				}
			});

			jList3.setModel(new javax.swing.AbstractListModel() {
				String[] strings = { "Dragon Bones", "Dragon Hide" };

				public int getSize() {
					return strings.length;
				}

				public Object getElementAt(int i) {
					return strings[i];
				}
			});
			jScrollPane3.setViewportView(jList3);

			jButton6.setText("Add");
			jButton6.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					jButton6ActionPerformed(evt);
				}
			});

			jList4.setModel(new javax.swing.AbstractListModel() {
				String[] strings = { "Grimy Rannar", "Rune Dagger", "Law Rune",
				" " };

				public int getSize() {
					return strings.length;
				}

				public Object getElementAt(int i) {
					return strings[i];
				}
			});
			jScrollPane4.setViewportView(jList4);

			jLabel13.setFont(new java.awt.Font("Tahoma", 0, 18));
			jLabel13.setText("Looting");

			jComboBox9.setModel(new javax.swing.DefaultComboBoxModel(
					new String[] { "None", "Snail", "Terrorbird", "Tortise",
					"Yak" }));

			jLabel14.setFont(new java.awt.Font("Tahoma", 0, 18));
			jLabel14.setText("Familiar");

			jComboBox2.setModel(new javax.swing.DefaultComboBoxModel(
					new String[] { "Trout", "Salmon", "Tuna", "Lobster",
							"SwordFish", "Shark", "CaveFish", " " }));

			jLabel2.setFont(new java.awt.Font("Tahoma", 0, 18));
			jLabel2.setText("Food");

			jComboBox10.setModel(new javax.swing.DefaultComboBoxModel(
					new String[] { "East", "West", "Chaos Tunnels", " " }));

			jLabel15.setFont(new java.awt.Font("Tahoma", 0, 18));
			jLabel15.setText("Location");

			jCheckBox4.setText("Cannon");

			jLabel16.setFont(new java.awt.Font("Tahoma", 0, 18));
			jLabel16.setForeground(new java.awt.Color(102, 0, 0));
			jLabel16.setText("Settings");

			jCheckBox5.setText("Use Glory to Bank");

			jCheckBox6.setSelected(true);
			jCheckBox6.setText("Tele in combat");

			jLabel37.setFont(new java.awt.Font("Tahoma", 0, 18));
			jLabel37.setForeground(new java.awt.Color(255, 0, 0));
			jLabel37.setText("Unsupported!");

			javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(
					jPanel2);
			jPanel2.setLayout(jPanel2Layout);
			jPanel2Layout
			.setHorizontalGroup(jPanel2Layout
					.createParallelGroup(
							javax.swing.GroupLayout.Alignment.LEADING)
							.addGroup(
									javax.swing.GroupLayout.Alignment.TRAILING,
									jPanel2Layout
									.createSequentialGroup()
									.addContainerGap(335,
											Short.MAX_VALUE)
											.addComponent(jLabel37)
											.addGap(69, 69, 69))
											.addGroup(
													jPanel2Layout
													.createParallelGroup(
															javax.swing.GroupLayout.Alignment.LEADING)
															.addGroup(
																	jPanel2Layout
																	.createSequentialGroup()
																	.addGap(53, 53, 53)
																	.addGroup(
																			jPanel2Layout
																			.createParallelGroup(
																					javax.swing.GroupLayout.Alignment.LEADING)
																					.addGroup(
																							jPanel2Layout
																							.createSequentialGroup()
																							.addGroup(
																									jPanel2Layout
																									.createParallelGroup(
																											javax.swing.GroupLayout.Alignment.LEADING)
																											.addGroup(
																													jPanel2Layout
																													.createParallelGroup(
																															javax.swing.GroupLayout.Alignment.LEADING,
																															false)
																															.addGroup(
																																	jPanel2Layout
																																	.createSequentialGroup()
																																	.addGap(11,
																																			11,
																																			11)
																																			.addComponent(
																																					jComboBox2,
																																					javax.swing.GroupLayout.PREFERRED_SIZE,
																																					javax.swing.GroupLayout.DEFAULT_SIZE,
																																					javax.swing.GroupLayout.PREFERRED_SIZE))
																																					.addGroup(
																																							jPanel2Layout
																																							.createSequentialGroup()
																																							.addGap(29,
																																									29,
																																									29)
																																									.addComponent(
																																											jLabel2,
																																											javax.swing.GroupLayout.DEFAULT_SIZE,
																																											javax.swing.GroupLayout.DEFAULT_SIZE,
																																											Short.MAX_VALUE)))
																																											.addGroup(
																																													jPanel2Layout
																																													.createSequentialGroup()
																																													.addGap(10,
																																															10,
																																															10)
																																															.addComponent(
																																																	jButton6,
																																																	javax.swing.GroupLayout.PREFERRED_SIZE,
																																																	60,
																																																	javax.swing.GroupLayout.PREFERRED_SIZE))
																																																	.addComponent(
																																																			jScrollPane4,
																																																			javax.swing.GroupLayout.PREFERRED_SIZE,
																																																			94,
																																																			javax.swing.GroupLayout.PREFERRED_SIZE))
																																																			.addGroup(
																																																					jPanel2Layout
																																																					.createParallelGroup(
																																																							javax.swing.GroupLayout.Alignment.LEADING)
																																																							.addGroup(
																																																									jPanel2Layout
																																																									.createSequentialGroup()
																																																									.addGap(36,
																																																											36,
																																																											36)
																																																											.addGroup(
																																																													jPanel2Layout
																																																													.createParallelGroup(
																																																															javax.swing.GroupLayout.Alignment.LEADING)
																																																															.addGroup(
																																																																	jPanel2Layout
																																																																	.createSequentialGroup()
																																																																	.addComponent(
																																																																			jScrollPane3,
																																																																			javax.swing.GroupLayout.PREFERRED_SIZE,
																																																																			120,
																																																																			javax.swing.GroupLayout.PREFERRED_SIZE)
																																																																			.addGap(49,
																																																																					49,
																																																																					49)
																																																																					.addComponent(
																																																																							jButton4))
																																																																							.addGroup(
																																																																									jPanel2Layout
																																																																									.createSequentialGroup()
																																																																									.addGroup(
																																																																											jPanel2Layout
																																																																											.createParallelGroup(
																																																																													javax.swing.GroupLayout.Alignment.TRAILING,
																																																																													false)
																																																																													.addComponent(
																																																																															jLabel15,
																																																																															javax.swing.GroupLayout.Alignment.LEADING,
																																																																															javax.swing.GroupLayout.DEFAULT_SIZE,
																																																																															javax.swing.GroupLayout.DEFAULT_SIZE,
																																																																															Short.MAX_VALUE)
																																																																															.addComponent(
																																																																																	jComboBox10,
																																																																																	javax.swing.GroupLayout.Alignment.LEADING,
																																																																																	javax.swing.GroupLayout.PREFERRED_SIZE,
																																																																																	javax.swing.GroupLayout.DEFAULT_SIZE,
																																																																																	javax.swing.GroupLayout.PREFERRED_SIZE))
																																																																																	.addGap(51,
																																																																																			51,
																																																																																			51)
																																																																																			.addGroup(
																																																																																					jPanel2Layout
																																																																																					.createParallelGroup(
																																																																																							javax.swing.GroupLayout.Alignment.LEADING)
																																																																																							.addComponent(
																																																																																									jLabel16)
																																																																																									.addComponent(
																																																																																											jCheckBox4)
																																																																																											.addComponent(
																																																																																													jCheckBox5)
																																																																																													.addComponent(
																																																																																															jCheckBox6)))))
																																																																																															.addGroup(
																																																																																																	jPanel2Layout
																																																																																																	.createSequentialGroup()
																																																																																																	.addGap(60,
																																																																																																			60,
																																																																																																			60)
																																																																																																			.addComponent(
																																																																																																					jButton5))))
																																																																																																					.addGroup(
																																																																																																							jPanel2Layout
																																																																																																							.createSequentialGroup()
																																																																																																							.addGap(86,
																																																																																																									86,
																																																																																																									86)
																																																																																																									.addGroup(
																																																																																																											jPanel2Layout
																																																																																																											.createParallelGroup(
																																																																																																													javax.swing.GroupLayout.Alignment.LEADING)
																																																																																																													.addComponent(
																																																																																																															jLabel14)
																																																																																																															.addComponent(
																																																																																																																	jLabel13)
																																																																																																																	.addComponent(
																																																																																																																			jComboBox9,
																																																																																																																			javax.swing.GroupLayout.PREFERRED_SIZE,
																																																																																																																			javax.swing.GroupLayout.DEFAULT_SIZE,
																																																																																																																			javax.swing.GroupLayout.PREFERRED_SIZE))))
																																																																																																																			.addContainerGap(
																																																																																																																					48,
																																																																																																																					Short.MAX_VALUE))));
			jPanel2Layout
			.setVerticalGroup(jPanel2Layout
					.createParallelGroup(
							javax.swing.GroupLayout.Alignment.LEADING)
							.addGroup(
									javax.swing.GroupLayout.Alignment.TRAILING,
									jPanel2Layout
									.createSequentialGroup()
									.addContainerGap(287,
											Short.MAX_VALUE)
											.addComponent(jLabel37)
											.addGap(122, 122, 122))
											.addGroup(
													jPanel2Layout
													.createParallelGroup(
															javax.swing.GroupLayout.Alignment.LEADING)
															.addGroup(
																	jPanel2Layout
																	.createSequentialGroup()
																	.addGap(25, 25, 25)
																	.addGroup(
																			jPanel2Layout
																			.createParallelGroup(
																					javax.swing.GroupLayout.Alignment.LEADING)
																					.addGroup(
																							jPanel2Layout
																							.createSequentialGroup()
																							.addGroup(
																									jPanel2Layout
																									.createParallelGroup(
																											javax.swing.GroupLayout.Alignment.LEADING)
																											.addGroup(
																													jPanel2Layout
																													.createSequentialGroup()
																													.addComponent(
																															jLabel2)
																															.addPreferredGap(
																																	javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																																	.addComponent(
																																			jComboBox2,
																																			javax.swing.GroupLayout.PREFERRED_SIZE,
																																			javax.swing.GroupLayout.DEFAULT_SIZE,
																																			javax.swing.GroupLayout.PREFERRED_SIZE)
																																			.addGap(18,
																																					18,
																																					18)
																																					.addComponent(
																																							jLabel14))
																																							.addGroup(
																																									jPanel2Layout
																																									.createSequentialGroup()
																																									.addComponent(
																																											jLabel15)
																																											.addPreferredGap(
																																													javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																																													.addComponent(
																																															jComboBox10,
																																															javax.swing.GroupLayout.PREFERRED_SIZE,
																																															javax.swing.GroupLayout.DEFAULT_SIZE,
																																															javax.swing.GroupLayout.PREFERRED_SIZE)))
																																															.addPreferredGap(
																																																	javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																																																	.addComponent(
																																																			jComboBox9,
																																																			javax.swing.GroupLayout.PREFERRED_SIZE,
																																																			javax.swing.GroupLayout.DEFAULT_SIZE,
																																																			javax.swing.GroupLayout.PREFERRED_SIZE)
																																																			.addGap(18,
																																																					18,
																																																					18)
																																																					.addComponent(
																																																							jLabel13)
																																																							.addPreferredGap(
																																																									javax.swing.LayoutStyle.ComponentPlacement.RELATED,
																																																									18,
																																																									Short.MAX_VALUE)
																																																									.addGroup(
																																																											jPanel2Layout
																																																											.createParallelGroup(
																																																													javax.swing.GroupLayout.Alignment.TRAILING,
																																																													false)
																																																													.addComponent(
																																																															jScrollPane4,
																																																															javax.swing.GroupLayout.Alignment.LEADING)
																																																															.addComponent(
																																																																	jScrollPane3,
																																																																	javax.swing.GroupLayout.Alignment.LEADING,
																																																																	javax.swing.GroupLayout.DEFAULT_SIZE,
																																																																	169,
																																																																	Short.MAX_VALUE))
																																																																	.addPreferredGap(
																																																																			javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																																																																			.addGroup(
																																																																					jPanel2Layout
																																																																					.createParallelGroup(
																																																																							javax.swing.GroupLayout.Alignment.BASELINE)
																																																																							.addComponent(
																																																																									jButton6)
																																																																									.addComponent(
																																																																											jButton5)))
																																																																											.addGroup(
																																																																													javax.swing.GroupLayout.Alignment.TRAILING,
																																																																													jPanel2Layout
																																																																													.createSequentialGroup()
																																																																													.addPreferredGap(
																																																																															javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																																																																															.addComponent(
																																																																																	jLabel16)
																																																																																	.addGap(18,
																																																																																			18,
																																																																																			18)
																																																																																			.addComponent(
																																																																																					jCheckBox4)
																																																																																					.addGap(2,
																																																																																							2,
																																																																																							2)
																																																																																							.addComponent(
																																																																																									jCheckBox5)
																																																																																									.addPreferredGap(
																																																																																											javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																																																																																											.addComponent(
																																																																																													jCheckBox6)
																																																																																													.addGap(180,
																																																																																															180,
																																																																																															180)
																																																																																															.addComponent(
																																																																																																	jButton4)
																																																																																																	.addGap(66,
																																																																																																			66,
																																																																																																			66)))
																																																																																																			.addGap(26, 26, 26))));

			jTabbedPane1.addTab("Blue", jPanel2);

			jButton7.setText("Start Red Drags");
			jButton7.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					jButton7ActionPerformed(evt);
				}
			});

			jButton8.setText("Remove");
			jButton8.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					jButton8ActionPerformed(evt);
				}
			});

			jList5.setModel(new javax.swing.AbstractListModel() {
				String[] strings = { "Dragon Bones", "Dragon Hide" };

				public int getSize() {
					return strings.length;
				}

				public Object getElementAt(int i) {
					return strings[i];
				}
			});
			jScrollPane5.setViewportView(jList5);

			jButton9.setText("Add");
			jButton9.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					jButton9ActionPerformed(evt);
				}
			});

			jList6.setModel(new javax.swing.AbstractListModel() {
				String[] strings = { "Grimy Rannar", "Rune Dagger", "Law Rune",
				" " };

				public int getSize() {
					return strings.length;
				}

				public Object getElementAt(int i) {
					return strings[i];
				}
			});
			jScrollPane6.setViewportView(jList6);

			jLabel17.setFont(new java.awt.Font("Tahoma", 0, 18));
			jLabel17.setText("Looting");

			jComboBox11.setModel(new javax.swing.DefaultComboBoxModel(
					new String[] { "None", "Snail", "Terrorbird", "Tortise",
					"Yak" }));

			jLabel18.setFont(new java.awt.Font("Tahoma", 0, 18));
			jLabel18.setText("Familiar");

			jComboBox3.setModel(new javax.swing.DefaultComboBoxModel(
					new String[] { "Trout", "Salmon", "Tuna", "Lobster",
							"SwordFish", "Shark", "CaveFish", " " }));

			jLabel3.setFont(new java.awt.Font("Tahoma", 0, 18));
			jLabel3.setText("Food");

			jComboBox12.setModel(new javax.swing.DefaultComboBoxModel(
					new String[] { "East", "West", "Chaos Tunnels", " " }));

			jLabel19.setFont(new java.awt.Font("Tahoma", 0, 18));
			jLabel19.setText("Location");

			jCheckBox7.setText("Cannon");

			jLabel20.setFont(new java.awt.Font("Tahoma", 0, 18));
			jLabel20.setForeground(new java.awt.Color(102, 0, 0));
			jLabel20.setText("Settings");

			jCheckBox8.setText("Use Glory to Bank");

			jCheckBox9.setSelected(true);
			jCheckBox9.setText("Tele in combat");

			jLabel36.setFont(new java.awt.Font("Tahoma", 0, 18));
			jLabel36.setForeground(new java.awt.Color(255, 0, 0));
			jLabel36.setText("Unsupported!");

			javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(
					jPanel3);
			jPanel3.setLayout(jPanel3Layout);
			jPanel3Layout
			.setHorizontalGroup(jPanel3Layout
					.createParallelGroup(
							javax.swing.GroupLayout.Alignment.LEADING)
							.addGroup(
									javax.swing.GroupLayout.Alignment.TRAILING,
									jPanel3Layout
									.createSequentialGroup()
									.addContainerGap(335,
											Short.MAX_VALUE)
											.addComponent(jLabel36)
											.addGap(69, 69, 69))
											.addGroup(
													jPanel3Layout
													.createParallelGroup(
															javax.swing.GroupLayout.Alignment.LEADING)
															.addGroup(
																	jPanel3Layout
																	.createSequentialGroup()
																	.addGap(53, 53, 53)
																	.addGroup(
																			jPanel3Layout
																			.createParallelGroup(
																					javax.swing.GroupLayout.Alignment.LEADING)
																					.addGroup(
																							jPanel3Layout
																							.createSequentialGroup()
																							.addGroup(
																									jPanel3Layout
																									.createParallelGroup(
																											javax.swing.GroupLayout.Alignment.LEADING)
																											.addGroup(
																													jPanel3Layout
																													.createParallelGroup(
																															javax.swing.GroupLayout.Alignment.LEADING,
																															false)
																															.addGroup(
																																	jPanel3Layout
																																	.createSequentialGroup()
																																	.addGap(11,
																																			11,
																																			11)
																																			.addComponent(
																																					jComboBox3,
																																					javax.swing.GroupLayout.PREFERRED_SIZE,
																																					javax.swing.GroupLayout.DEFAULT_SIZE,
																																					javax.swing.GroupLayout.PREFERRED_SIZE))
																																					.addGroup(
																																							jPanel3Layout
																																							.createSequentialGroup()
																																							.addGap(29,
																																									29,
																																									29)
																																									.addComponent(
																																											jLabel3,
																																											javax.swing.GroupLayout.DEFAULT_SIZE,
																																											javax.swing.GroupLayout.DEFAULT_SIZE,
																																											Short.MAX_VALUE)))
																																											.addGroup(
																																													jPanel3Layout
																																													.createSequentialGroup()
																																													.addGap(10,
																																															10,
																																															10)
																																															.addComponent(
																																																	jButton9,
																																																	javax.swing.GroupLayout.PREFERRED_SIZE,
																																																	60,
																																																	javax.swing.GroupLayout.PREFERRED_SIZE))
																																																	.addComponent(
																																																			jScrollPane6,
																																																			javax.swing.GroupLayout.PREFERRED_SIZE,
																																																			94,
																																																			javax.swing.GroupLayout.PREFERRED_SIZE))
																																																			.addGroup(
																																																					jPanel3Layout
																																																					.createParallelGroup(
																																																							javax.swing.GroupLayout.Alignment.LEADING)
																																																							.addGroup(
																																																									jPanel3Layout
																																																									.createSequentialGroup()
																																																									.addGap(36,
																																																											36,
																																																											36)
																																																											.addGroup(
																																																													jPanel3Layout
																																																													.createParallelGroup(
																																																															javax.swing.GroupLayout.Alignment.LEADING)
																																																															.addGroup(
																																																																	jPanel3Layout
																																																																	.createSequentialGroup()
																																																																	.addComponent(
																																																																			jScrollPane5,
																																																																			javax.swing.GroupLayout.PREFERRED_SIZE,
																																																																			120,
																																																																			javax.swing.GroupLayout.PREFERRED_SIZE)
																																																																			.addGap(49,
																																																																					49,
																																																																					49)
																																																																					.addComponent(
																																																																							jButton7))
																																																																							.addGroup(
																																																																									jPanel3Layout
																																																																									.createSequentialGroup()
																																																																									.addGroup(
																																																																											jPanel3Layout
																																																																											.createParallelGroup(
																																																																													javax.swing.GroupLayout.Alignment.TRAILING,
																																																																													false)
																																																																													.addComponent(
																																																																															jLabel19,
																																																																															javax.swing.GroupLayout.Alignment.LEADING,
																																																																															javax.swing.GroupLayout.DEFAULT_SIZE,
																																																																															javax.swing.GroupLayout.DEFAULT_SIZE,
																																																																															Short.MAX_VALUE)
																																																																															.addComponent(
																																																																																	jComboBox12,
																																																																																	javax.swing.GroupLayout.Alignment.LEADING,
																																																																																	javax.swing.GroupLayout.PREFERRED_SIZE,
																																																																																	javax.swing.GroupLayout.DEFAULT_SIZE,
																																																																																	javax.swing.GroupLayout.PREFERRED_SIZE))
																																																																																	.addGap(51,
																																																																																			51,
																																																																																			51)
																																																																																			.addGroup(
																																																																																					jPanel3Layout
																																																																																					.createParallelGroup(
																																																																																							javax.swing.GroupLayout.Alignment.LEADING)
																																																																																							.addComponent(
																																																																																									jLabel20)
																																																																																									.addComponent(
																																																																																											jCheckBox7)
																																																																																											.addComponent(
																																																																																													jCheckBox8)
																																																																																													.addComponent(
																																																																																															jCheckBox9)))))
																																																																																															.addGroup(
																																																																																																	jPanel3Layout
																																																																																																	.createSequentialGroup()
																																																																																																	.addGap(60,
																																																																																																			60,
																																																																																																			60)
																																																																																																			.addComponent(
																																																																																																					jButton8))))
																																																																																																					.addGroup(
																																																																																																							jPanel3Layout
																																																																																																							.createSequentialGroup()
																																																																																																							.addGap(86,
																																																																																																									86,
																																																																																																									86)
																																																																																																									.addGroup(
																																																																																																											jPanel3Layout
																																																																																																											.createParallelGroup(
																																																																																																													javax.swing.GroupLayout.Alignment.LEADING)
																																																																																																													.addComponent(
																																																																																																															jLabel18)
																																																																																																															.addComponent(
																																																																																																																	jLabel17)
																																																																																																																	.addComponent(
																																																																																																																			jComboBox11,
																																																																																																																			javax.swing.GroupLayout.PREFERRED_SIZE,
																																																																																																																			javax.swing.GroupLayout.DEFAULT_SIZE,
																																																																																																																			javax.swing.GroupLayout.PREFERRED_SIZE))))
																																																																																																																			.addContainerGap(
																																																																																																																					50,
																																																																																																																					Short.MAX_VALUE))));
			jPanel3Layout
			.setVerticalGroup(jPanel3Layout
					.createParallelGroup(
							javax.swing.GroupLayout.Alignment.LEADING)
							.addGroup(
									javax.swing.GroupLayout.Alignment.TRAILING,
									jPanel3Layout
									.createSequentialGroup()
									.addContainerGap(273,
											Short.MAX_VALUE)
											.addComponent(jLabel36)
											.addGap(136, 136, 136))
											.addGroup(
													jPanel3Layout
													.createParallelGroup(
															javax.swing.GroupLayout.Alignment.LEADING)
															.addGroup(
																	jPanel3Layout
																	.createSequentialGroup()
																	.addGap(25, 25, 25)
																	.addGroup(
																			jPanel3Layout
																			.createParallelGroup(
																					javax.swing.GroupLayout.Alignment.LEADING)
																					.addGroup(
																							jPanel3Layout
																							.createSequentialGroup()
																							.addGroup(
																									jPanel3Layout
																									.createParallelGroup(
																											javax.swing.GroupLayout.Alignment.LEADING)
																											.addGroup(
																													jPanel3Layout
																													.createSequentialGroup()
																													.addComponent(
																															jLabel3)
																															.addPreferredGap(
																																	javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																																	.addComponent(
																																			jComboBox3,
																																			javax.swing.GroupLayout.PREFERRED_SIZE,
																																			javax.swing.GroupLayout.DEFAULT_SIZE,
																																			javax.swing.GroupLayout.PREFERRED_SIZE)
																																			.addGap(18,
																																					18,
																																					18)
																																					.addComponent(
																																							jLabel18))
																																							.addGroup(
																																									jPanel3Layout
																																									.createSequentialGroup()
																																									.addComponent(
																																											jLabel19)
																																											.addPreferredGap(
																																													javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																																													.addComponent(
																																															jComboBox12,
																																															javax.swing.GroupLayout.PREFERRED_SIZE,
																																															javax.swing.GroupLayout.DEFAULT_SIZE,
																																															javax.swing.GroupLayout.PREFERRED_SIZE)))
																																															.addPreferredGap(
																																																	javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																																																	.addComponent(
																																																			jComboBox11,
																																																			javax.swing.GroupLayout.PREFERRED_SIZE,
																																																			javax.swing.GroupLayout.DEFAULT_SIZE,
																																																			javax.swing.GroupLayout.PREFERRED_SIZE)
																																																			.addGap(18,
																																																					18,
																																																					18)
																																																					.addComponent(
																																																							jLabel17)
																																																							.addPreferredGap(
																																																									javax.swing.LayoutStyle.ComponentPlacement.RELATED,
																																																									18,
																																																									Short.MAX_VALUE)
																																																									.addGroup(
																																																											jPanel3Layout
																																																											.createParallelGroup(
																																																													javax.swing.GroupLayout.Alignment.TRAILING,
																																																													false)
																																																													.addComponent(
																																																															jScrollPane6,
																																																															javax.swing.GroupLayout.Alignment.LEADING)
																																																															.addComponent(
																																																																	jScrollPane5,
																																																																	javax.swing.GroupLayout.Alignment.LEADING,
																																																																	javax.swing.GroupLayout.DEFAULT_SIZE,
																																																																	169,
																																																																	Short.MAX_VALUE))
																																																																	.addPreferredGap(
																																																																			javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																																																																			.addGroup(
																																																																					jPanel3Layout
																																																																					.createParallelGroup(
																																																																							javax.swing.GroupLayout.Alignment.BASELINE)
																																																																							.addComponent(
																																																																									jButton9)
																																																																									.addComponent(
																																																																											jButton8)))
																																																																											.addGroup(
																																																																													javax.swing.GroupLayout.Alignment.TRAILING,
																																																																													jPanel3Layout
																																																																													.createSequentialGroup()
																																																																													.addPreferredGap(
																																																																															javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																																																																															.addComponent(
																																																																																	jLabel20)
																																																																																	.addGap(18,
																																																																																			18,
																																																																																			18)
																																																																																			.addComponent(
																																																																																					jCheckBox7)
																																																																																					.addGap(2,
																																																																																							2,
																																																																																							2)
																																																																																							.addComponent(
																																																																																									jCheckBox8)
																																																																																									.addPreferredGap(
																																																																																											javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																																																																																											.addComponent(
																																																																																													jCheckBox9)
																																																																																													.addGap(180,
																																																																																															180,
																																																																																															180)
																																																																																															.addComponent(
																																																																																																	jButton7)
																																																																																																	.addGap(66,
																																																																																																			66,
																																																																																																			66)))
																																																																																																			.addGap(26, 26, 26))));

			jTabbedPane1.addTab("Red", jPanel3);

			jButton10.setText("Start Black Drags");
			jButton10.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					jButton10ActionPerformed(evt);
				}
			});

			jButton11.setText("Remove");
			jButton11.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					jButton11ActionPerformed(evt);
				}
			});

			jList7.setModel(new javax.swing.AbstractListModel() {
				String[] strings = { "Dragon Bones", "Dragon Hide" };

				public int getSize() {
					return strings.length;
				}

				public Object getElementAt(int i) {
					return strings[i];
				}
			});
			jScrollPane7.setViewportView(jList7);

			jButton12.setText("Add");
			jButton12.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					jButton12ActionPerformed(evt);
				}
			});

			jList8.setModel(new javax.swing.AbstractListModel() {
				String[] strings = { "Grimy Rannar", "Rune Dagger", "Law Rune",
				" " };

				public int getSize() {
					return strings.length;
				}

				public Object getElementAt(int i) {
					return strings[i];
				}
			});
			jScrollPane8.setViewportView(jList8);

			jLabel21.setFont(new java.awt.Font("Tahoma", 0, 18));
			jLabel21.setText("Looting");

			jComboBox13.setModel(new javax.swing.DefaultComboBoxModel(
					new String[] { "None", "Snail", "Terrorbird", "Tortise",
					"Yak" }));

			jLabel22.setFont(new java.awt.Font("Tahoma", 0, 18));
			jLabel22.setText("Familiar");

			jComboBox4.setModel(new javax.swing.DefaultComboBoxModel(
					new String[] { "Trout", "Salmon", "Tuna", "Lobster",
							"SwordFish", "Shark", "CaveFish", " " }));

			jLabel4.setFont(new java.awt.Font("Tahoma", 0, 18));
			jLabel4.setText("Food");

			jComboBox14.setModel(new javax.swing.DefaultComboBoxModel(
					new String[] { "East", "West", "Chaos Tunnels", " " }));

			jLabel23.setFont(new java.awt.Font("Tahoma", 0, 18));
			jLabel23.setText("Location");

			jCheckBox10.setText("Cannon");

			jLabel24.setFont(new java.awt.Font("Tahoma", 0, 18));
			jLabel24.setForeground(new java.awt.Color(102, 0, 0));
			jLabel24.setText("Settings");

			jCheckBox11.setText("Use Glory to Bank");

			jCheckBox12.setSelected(true);
			jCheckBox12.setText("Tele in combat");

			jLabel35.setFont(new java.awt.Font("Tahoma", 0, 18));
			jLabel35.setForeground(new java.awt.Color(255, 0, 0));
			jLabel35.setText("Unsupported!");

			javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(
					jPanel4);
			jPanel4.setLayout(jPanel4Layout);
			jPanel4Layout
			.setHorizontalGroup(jPanel4Layout
					.createParallelGroup(
							javax.swing.GroupLayout.Alignment.LEADING)
							.addGroup(
									javax.swing.GroupLayout.Alignment.TRAILING,
									jPanel4Layout
									.createSequentialGroup()
									.addContainerGap(341,
											Short.MAX_VALUE)
											.addComponent(jLabel35)
											.addGap(63, 63, 63))
											.addGroup(
													jPanel4Layout
													.createParallelGroup(
															javax.swing.GroupLayout.Alignment.LEADING)
															.addGroup(
																	jPanel4Layout
																	.createSequentialGroup()
																	.addGap(53, 53, 53)
																	.addGroup(
																			jPanel4Layout
																			.createParallelGroup(
																					javax.swing.GroupLayout.Alignment.LEADING)
																					.addGroup(
																							jPanel4Layout
																							.createSequentialGroup()
																							.addGroup(
																									jPanel4Layout
																									.createParallelGroup(
																											javax.swing.GroupLayout.Alignment.LEADING)
																											.addGroup(
																													jPanel4Layout
																													.createParallelGroup(
																															javax.swing.GroupLayout.Alignment.LEADING,
																															false)
																															.addGroup(
																																	jPanel4Layout
																																	.createSequentialGroup()
																																	.addGap(11,
																																			11,
																																			11)
																																			.addComponent(
																																					jComboBox4,
																																					javax.swing.GroupLayout.PREFERRED_SIZE,
																																					javax.swing.GroupLayout.DEFAULT_SIZE,
																																					javax.swing.GroupLayout.PREFERRED_SIZE))
																																					.addGroup(
																																							jPanel4Layout
																																							.createSequentialGroup()
																																							.addGap(29,
																																									29,
																																									29)
																																									.addComponent(
																																											jLabel4,
																																											javax.swing.GroupLayout.DEFAULT_SIZE,
																																											javax.swing.GroupLayout.DEFAULT_SIZE,
																																											Short.MAX_VALUE)))
																																											.addGroup(
																																													jPanel4Layout
																																													.createSequentialGroup()
																																													.addGap(10,
																																															10,
																																															10)
																																															.addComponent(
																																																	jButton12,
																																																	javax.swing.GroupLayout.PREFERRED_SIZE,
																																																	60,
																																																	javax.swing.GroupLayout.PREFERRED_SIZE))
																																																	.addComponent(
																																																			jScrollPane8,
																																																			javax.swing.GroupLayout.PREFERRED_SIZE,
																																																			94,
																																																			javax.swing.GroupLayout.PREFERRED_SIZE))
																																																			.addGroup(
																																																					jPanel4Layout
																																																					.createParallelGroup(
																																																							javax.swing.GroupLayout.Alignment.LEADING)
																																																							.addGroup(
																																																									jPanel4Layout
																																																									.createSequentialGroup()
																																																									.addGap(36,
																																																											36,
																																																											36)
																																																											.addGroup(
																																																													jPanel4Layout
																																																													.createParallelGroup(
																																																															javax.swing.GroupLayout.Alignment.LEADING)
																																																															.addGroup(
																																																																	jPanel4Layout
																																																																	.createSequentialGroup()
																																																																	.addComponent(
																																																																			jScrollPane7,
																																																																			javax.swing.GroupLayout.PREFERRED_SIZE,
																																																																			120,
																																																																			javax.swing.GroupLayout.PREFERRED_SIZE)
																																																																			.addGap(49,
																																																																					49,
																																																																					49)
																																																																					.addComponent(
																																																																							jButton10))
																																																																							.addGroup(
																																																																									jPanel4Layout
																																																																									.createSequentialGroup()
																																																																									.addGroup(
																																																																											jPanel4Layout
																																																																											.createParallelGroup(
																																																																													javax.swing.GroupLayout.Alignment.TRAILING,
																																																																													false)
																																																																													.addComponent(
																																																																															jLabel23,
																																																																															javax.swing.GroupLayout.Alignment.LEADING,
																																																																															javax.swing.GroupLayout.DEFAULT_SIZE,
																																																																															javax.swing.GroupLayout.DEFAULT_SIZE,
																																																																															Short.MAX_VALUE)
																																																																															.addComponent(
																																																																																	jComboBox14,
																																																																																	javax.swing.GroupLayout.Alignment.LEADING,
																																																																																	javax.swing.GroupLayout.PREFERRED_SIZE,
																																																																																	javax.swing.GroupLayout.DEFAULT_SIZE,
																																																																																	javax.swing.GroupLayout.PREFERRED_SIZE))
																																																																																	.addGap(51,
																																																																																			51,
																																																																																			51)
																																																																																			.addGroup(
																																																																																					jPanel4Layout
																																																																																					.createParallelGroup(
																																																																																							javax.swing.GroupLayout.Alignment.LEADING)
																																																																																							.addComponent(
																																																																																									jLabel24)
																																																																																									.addComponent(
																																																																																											jCheckBox10)
																																																																																											.addComponent(
																																																																																													jCheckBox11)
																																																																																													.addComponent(
																																																																																															jCheckBox12)))))
																																																																																															.addGroup(
																																																																																																	jPanel4Layout
																																																																																																	.createSequentialGroup()
																																																																																																	.addGap(60,
																																																																																																			60,
																																																																																																			60)
																																																																																																			.addComponent(
																																																																																																					jButton11))))
																																																																																																					.addGroup(
																																																																																																							jPanel4Layout
																																																																																																							.createSequentialGroup()
																																																																																																							.addGap(86,
																																																																																																									86,
																																																																																																									86)
																																																																																																									.addGroup(
																																																																																																											jPanel4Layout
																																																																																																											.createParallelGroup(
																																																																																																													javax.swing.GroupLayout.Alignment.LEADING)
																																																																																																													.addComponent(
																																																																																																															jLabel22)
																																																																																																															.addComponent(
																																																																																																																	jLabel21)
																																																																																																																	.addComponent(
																																																																																																																			jComboBox13,
																																																																																																																			javax.swing.GroupLayout.PREFERRED_SIZE,
																																																																																																																			javax.swing.GroupLayout.DEFAULT_SIZE,
																																																																																																																			javax.swing.GroupLayout.PREFERRED_SIZE))))
																																																																																																																			.addContainerGap(
																																																																																																																					44,
																																																																																																																					Short.MAX_VALUE))));
			jPanel4Layout
			.setVerticalGroup(jPanel4Layout
					.createParallelGroup(
							javax.swing.GroupLayout.Alignment.LEADING)
							.addGroup(
									javax.swing.GroupLayout.Alignment.TRAILING,
									jPanel4Layout
									.createSequentialGroup()
									.addContainerGap(274,
											Short.MAX_VALUE)
											.addComponent(jLabel35)
											.addGap(135, 135, 135))
											.addGroup(
													jPanel4Layout
													.createParallelGroup(
															javax.swing.GroupLayout.Alignment.LEADING)
															.addGroup(
																	jPanel4Layout
																	.createSequentialGroup()
																	.addGap(25, 25, 25)
																	.addGroup(
																			jPanel4Layout
																			.createParallelGroup(
																					javax.swing.GroupLayout.Alignment.LEADING)
																					.addGroup(
																							jPanel4Layout
																							.createSequentialGroup()
																							.addGroup(
																									jPanel4Layout
																									.createParallelGroup(
																											javax.swing.GroupLayout.Alignment.LEADING)
																											.addGroup(
																													jPanel4Layout
																													.createSequentialGroup()
																													.addComponent(
																															jLabel4)
																															.addPreferredGap(
																																	javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																																	.addComponent(
																																			jComboBox4,
																																			javax.swing.GroupLayout.PREFERRED_SIZE,
																																			javax.swing.GroupLayout.DEFAULT_SIZE,
																																			javax.swing.GroupLayout.PREFERRED_SIZE)
																																			.addGap(18,
																																					18,
																																					18)
																																					.addComponent(
																																							jLabel22))
																																							.addGroup(
																																									jPanel4Layout
																																									.createSequentialGroup()
																																									.addComponent(
																																											jLabel23)
																																											.addPreferredGap(
																																													javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																																													.addComponent(
																																															jComboBox14,
																																															javax.swing.GroupLayout.PREFERRED_SIZE,
																																															javax.swing.GroupLayout.DEFAULT_SIZE,
																																															javax.swing.GroupLayout.PREFERRED_SIZE)))
																																															.addPreferredGap(
																																																	javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																																																	.addComponent(
																																																			jComboBox13,
																																																			javax.swing.GroupLayout.PREFERRED_SIZE,
																																																			javax.swing.GroupLayout.DEFAULT_SIZE,
																																																			javax.swing.GroupLayout.PREFERRED_SIZE)
																																																			.addGap(18,
																																																					18,
																																																					18)
																																																					.addComponent(
																																																							jLabel21)
																																																							.addPreferredGap(
																																																									javax.swing.LayoutStyle.ComponentPlacement.RELATED,
																																																									18,
																																																									Short.MAX_VALUE)
																																																									.addGroup(
																																																											jPanel4Layout
																																																											.createParallelGroup(
																																																													javax.swing.GroupLayout.Alignment.TRAILING,
																																																													false)
																																																													.addComponent(
																																																															jScrollPane8,
																																																															javax.swing.GroupLayout.Alignment.LEADING)
																																																															.addComponent(
																																																																	jScrollPane7,
																																																																	javax.swing.GroupLayout.Alignment.LEADING,
																																																																	javax.swing.GroupLayout.DEFAULT_SIZE,
																																																																	169,
																																																																	Short.MAX_VALUE))
																																																																	.addPreferredGap(
																																																																			javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																																																																			.addGroup(
																																																																					jPanel4Layout
																																																																					.createParallelGroup(
																																																																							javax.swing.GroupLayout.Alignment.BASELINE)
																																																																							.addComponent(
																																																																									jButton12)
																																																																									.addComponent(
																																																																											jButton11)))
																																																																											.addGroup(
																																																																													javax.swing.GroupLayout.Alignment.TRAILING,
																																																																													jPanel4Layout
																																																																													.createSequentialGroup()
																																																																													.addPreferredGap(
																																																																															javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																																																																															.addComponent(
																																																																																	jLabel24)
																																																																																	.addGap(18,
																																																																																			18,
																																																																																			18)
																																																																																			.addComponent(
																																																																																					jCheckBox10)
																																																																																					.addGap(2,
																																																																																							2,
																																																																																							2)
																																																																																							.addComponent(
																																																																																									jCheckBox11)
																																																																																									.addPreferredGap(
																																																																																											javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																																																																																											.addComponent(
																																																																																													jCheckBox12)
																																																																																													.addGap(180,
																																																																																															180,
																																																																																															180)
																																																																																															.addComponent(
																																																																																																	jButton10)
																																																																																																	.addGap(66,
																																																																																																			66,
																																																																																																			66)))
																																																																																																			.addGap(26, 26, 26))));

			jTabbedPane1.addTab("Black", jPanel4);

			jButton13.setText("Start Bronze Drags");
			jButton13.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					jButton13ActionPerformed(evt);
				}
			});

			jButton14.setText("Remove");
			jButton14.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					jButton14ActionPerformed(evt);
				}
			});

			jList9.setModel(new javax.swing.AbstractListModel() {
				String[] strings = { "Dragon Bones", "Dragon Hide" };

				public int getSize() {
					return strings.length;
				}

				public Object getElementAt(int i) {
					return strings[i];
				}
			});
			jScrollPane9.setViewportView(jList9);

			jButton15.setText("Add");
			jButton15.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					jButton15ActionPerformed(evt);
				}
			});

			jList10.setModel(new javax.swing.AbstractListModel() {
				String[] strings = { "Grimy Rannar", "Rune Dagger", "Law Rune",
				" " };

				public int getSize() {
					return strings.length;
				}

				public Object getElementAt(int i) {
					return strings[i];
				}
			});
			jScrollPane10.setViewportView(jList10);

			jLabel25.setFont(new java.awt.Font("Tahoma", 0, 18));
			jLabel25.setText("Looting");

			jComboBox15.setModel(new javax.swing.DefaultComboBoxModel(
					new String[] { "None", "Snail", "Terrorbird", "Tortise",
					"Yak" }));

			jLabel26.setFont(new java.awt.Font("Tahoma", 0, 18));
			jLabel26.setText("Familiar");

			jComboBox5.setModel(new javax.swing.DefaultComboBoxModel(
					new String[] { "Trout", "Salmon", "Tuna", "Lobster",
							"SwordFish", "Shark", "CaveFish", " " }));

			jLabel5.setFont(new java.awt.Font("Tahoma", 0, 18));
			jLabel5.setText("Food");

			jComboBox16.setModel(new javax.swing.DefaultComboBoxModel(
					new String[] { "East", "West", "Chaos Tunnels", " " }));

			jLabel27.setFont(new java.awt.Font("Tahoma", 0, 18));
			jLabel27.setText("Location");

			jCheckBox13.setText("Cannon");

			jLabel28.setFont(new java.awt.Font("Tahoma", 0, 18));
			jLabel28.setForeground(new java.awt.Color(102, 0, 0));
			jLabel28.setText("Settings");

			jCheckBox14.setText("Use Glory to Bank");

			jCheckBox15.setSelected(true);
			jCheckBox15.setText("Tele in combat");

			jLabel34.setFont(new java.awt.Font("Tahoma", 0, 18));
			jLabel34.setForeground(new java.awt.Color(255, 0, 0));
			jLabel34.setText("Unsupported!");

			javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(
					jPanel5);
			jPanel5.setLayout(jPanel5Layout);
			jPanel5Layout
			.setHorizontalGroup(jPanel5Layout
					.createParallelGroup(
							javax.swing.GroupLayout.Alignment.LEADING)
							.addGroup(
									javax.swing.GroupLayout.Alignment.TRAILING,
									jPanel5Layout
									.createSequentialGroup()
									.addContainerGap(352,
											Short.MAX_VALUE)
											.addComponent(jLabel34)
											.addGap(52, 52, 52))
											.addGroup(
													jPanel5Layout
													.createParallelGroup(
															javax.swing.GroupLayout.Alignment.LEADING)
															.addGroup(
																	jPanel5Layout
																	.createSequentialGroup()
																	.addGap(53, 53, 53)
																	.addGroup(
																			jPanel5Layout
																			.createParallelGroup(
																					javax.swing.GroupLayout.Alignment.LEADING)
																					.addGroup(
																							jPanel5Layout
																							.createSequentialGroup()
																							.addGroup(
																									jPanel5Layout
																									.createParallelGroup(
																											javax.swing.GroupLayout.Alignment.LEADING)
																											.addGroup(
																													jPanel5Layout
																													.createParallelGroup(
																															javax.swing.GroupLayout.Alignment.LEADING,
																															false)
																															.addGroup(
																																	jPanel5Layout
																																	.createSequentialGroup()
																																	.addGap(11,
																																			11,
																																			11)
																																			.addComponent(
																																					jComboBox5,
																																					javax.swing.GroupLayout.PREFERRED_SIZE,
																																					javax.swing.GroupLayout.DEFAULT_SIZE,
																																					javax.swing.GroupLayout.PREFERRED_SIZE))
																																					.addGroup(
																																							jPanel5Layout
																																							.createSequentialGroup()
																																							.addGap(29,
																																									29,
																																									29)
																																									.addComponent(
																																											jLabel5,
																																											javax.swing.GroupLayout.DEFAULT_SIZE,
																																											javax.swing.GroupLayout.DEFAULT_SIZE,
																																											Short.MAX_VALUE)))
																																											.addGroup(
																																													jPanel5Layout
																																													.createSequentialGroup()
																																													.addGap(10,
																																															10,
																																															10)
																																															.addComponent(
																																																	jButton15,
																																																	javax.swing.GroupLayout.PREFERRED_SIZE,
																																																	60,
																																																	javax.swing.GroupLayout.PREFERRED_SIZE))
																																																	.addComponent(
																																																			jScrollPane10,
																																																			javax.swing.GroupLayout.PREFERRED_SIZE,
																																																			94,
																																																			javax.swing.GroupLayout.PREFERRED_SIZE))
																																																			.addGroup(
																																																					jPanel5Layout
																																																					.createParallelGroup(
																																																							javax.swing.GroupLayout.Alignment.LEADING)
																																																							.addGroup(
																																																									jPanel5Layout
																																																									.createSequentialGroup()
																																																									.addGap(36,
																																																											36,
																																																											36)
																																																											.addGroup(
																																																													jPanel5Layout
																																																													.createParallelGroup(
																																																															javax.swing.GroupLayout.Alignment.LEADING)
																																																															.addGroup(
																																																																	jPanel5Layout
																																																																	.createSequentialGroup()
																																																																	.addComponent(
																																																																			jScrollPane9,
																																																																			javax.swing.GroupLayout.PREFERRED_SIZE,
																																																																			120,
																																																																			javax.swing.GroupLayout.PREFERRED_SIZE)
																																																																			.addGap(49,
																																																																					49,
																																																																					49)
																																																																					.addComponent(
																																																																							jButton13))
																																																																							.addGroup(
																																																																									jPanel5Layout
																																																																									.createSequentialGroup()
																																																																									.addGroup(
																																																																											jPanel5Layout
																																																																											.createParallelGroup(
																																																																													javax.swing.GroupLayout.Alignment.TRAILING,
																																																																													false)
																																																																													.addComponent(
																																																																															jLabel27,
																																																																															javax.swing.GroupLayout.Alignment.LEADING,
																																																																															javax.swing.GroupLayout.DEFAULT_SIZE,
																																																																															javax.swing.GroupLayout.DEFAULT_SIZE,
																																																																															Short.MAX_VALUE)
																																																																															.addComponent(
																																																																																	jComboBox16,
																																																																																	javax.swing.GroupLayout.Alignment.LEADING,
																																																																																	javax.swing.GroupLayout.PREFERRED_SIZE,
																																																																																	javax.swing.GroupLayout.DEFAULT_SIZE,
																																																																																	javax.swing.GroupLayout.PREFERRED_SIZE))
																																																																																	.addGap(51,
																																																																																			51,
																																																																																			51)
																																																																																			.addGroup(
																																																																																					jPanel5Layout
																																																																																					.createParallelGroup(
																																																																																							javax.swing.GroupLayout.Alignment.LEADING)
																																																																																							.addComponent(
																																																																																									jLabel28)
																																																																																									.addComponent(
																																																																																											jCheckBox13)
																																																																																											.addComponent(
																																																																																													jCheckBox14)
																																																																																													.addComponent(
																																																																																															jCheckBox15)))))
																																																																																															.addGroup(
																																																																																																	jPanel5Layout
																																																																																																	.createSequentialGroup()
																																																																																																	.addGap(60,
																																																																																																			60,
																																																																																																			60)
																																																																																																			.addComponent(
																																																																																																					jButton14))))
																																																																																																					.addGroup(
																																																																																																							jPanel5Layout
																																																																																																							.createSequentialGroup()
																																																																																																							.addGap(86,
																																																																																																									86,
																																																																																																									86)
																																																																																																									.addGroup(
																																																																																																											jPanel5Layout
																																																																																																											.createParallelGroup(
																																																																																																													javax.swing.GroupLayout.Alignment.LEADING)
																																																																																																													.addComponent(
																																																																																																															jLabel26)
																																																																																																															.addComponent(
																																																																																																																	jLabel25)
																																																																																																																	.addComponent(
																																																																																																																			jComboBox15,
																																																																																																																			javax.swing.GroupLayout.PREFERRED_SIZE,
																																																																																																																			javax.swing.GroupLayout.DEFAULT_SIZE,
																																																																																																																			javax.swing.GroupLayout.PREFERRED_SIZE))))
																																																																																																																			.addContainerGap(
																																																																																																																					36,
																																																																																																																					Short.MAX_VALUE))));
			jPanel5Layout
			.setVerticalGroup(jPanel5Layout
					.createParallelGroup(
							javax.swing.GroupLayout.Alignment.LEADING)
							.addGroup(
									javax.swing.GroupLayout.Alignment.TRAILING,
									jPanel5Layout
									.createSequentialGroup()
									.addContainerGap(269,
											Short.MAX_VALUE)
											.addComponent(jLabel34)
											.addGap(140, 140, 140))
											.addGroup(
													jPanel5Layout
													.createParallelGroup(
															javax.swing.GroupLayout.Alignment.LEADING)
															.addGroup(
																	jPanel5Layout
																	.createSequentialGroup()
																	.addGap(25, 25, 25)
																	.addGroup(
																			jPanel5Layout
																			.createParallelGroup(
																					javax.swing.GroupLayout.Alignment.LEADING)
																					.addGroup(
																							jPanel5Layout
																							.createSequentialGroup()
																							.addGroup(
																									jPanel5Layout
																									.createParallelGroup(
																											javax.swing.GroupLayout.Alignment.LEADING)
																											.addGroup(
																													jPanel5Layout
																													.createSequentialGroup()
																													.addComponent(
																															jLabel5)
																															.addPreferredGap(
																																	javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																																	.addComponent(
																																			jComboBox5,
																																			javax.swing.GroupLayout.PREFERRED_SIZE,
																																			javax.swing.GroupLayout.DEFAULT_SIZE,
																																			javax.swing.GroupLayout.PREFERRED_SIZE)
																																			.addGap(18,
																																					18,
																																					18)
																																					.addComponent(
																																							jLabel26))
																																							.addGroup(
																																									jPanel5Layout
																																									.createSequentialGroup()
																																									.addComponent(
																																											jLabel27)
																																											.addPreferredGap(
																																													javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																																													.addComponent(
																																															jComboBox16,
																																															javax.swing.GroupLayout.PREFERRED_SIZE,
																																															javax.swing.GroupLayout.DEFAULT_SIZE,
																																															javax.swing.GroupLayout.PREFERRED_SIZE)))
																																															.addPreferredGap(
																																																	javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																																																	.addComponent(
																																																			jComboBox15,
																																																			javax.swing.GroupLayout.PREFERRED_SIZE,
																																																			javax.swing.GroupLayout.DEFAULT_SIZE,
																																																			javax.swing.GroupLayout.PREFERRED_SIZE)
																																																			.addGap(18,
																																																					18,
																																																					18)
																																																					.addComponent(
																																																							jLabel25)
																																																							.addPreferredGap(
																																																									javax.swing.LayoutStyle.ComponentPlacement.RELATED,
																																																									18,
																																																									Short.MAX_VALUE)
																																																									.addGroup(
																																																											jPanel5Layout
																																																											.createParallelGroup(
																																																													javax.swing.GroupLayout.Alignment.TRAILING,
																																																													false)
																																																													.addComponent(
																																																															jScrollPane10,
																																																															javax.swing.GroupLayout.Alignment.LEADING)
																																																															.addComponent(
																																																																	jScrollPane9,
																																																																	javax.swing.GroupLayout.Alignment.LEADING,
																																																																	javax.swing.GroupLayout.DEFAULT_SIZE,
																																																																	169,
																																																																	Short.MAX_VALUE))
																																																																	.addPreferredGap(
																																																																			javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																																																																			.addGroup(
																																																																					jPanel5Layout
																																																																					.createParallelGroup(
																																																																							javax.swing.GroupLayout.Alignment.BASELINE)
																																																																							.addComponent(
																																																																									jButton15)
																																																																									.addComponent(
																																																																											jButton14)))
																																																																											.addGroup(
																																																																													javax.swing.GroupLayout.Alignment.TRAILING,
																																																																													jPanel5Layout
																																																																													.createSequentialGroup()
																																																																													.addPreferredGap(
																																																																															javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																																																																															.addComponent(
																																																																																	jLabel28)
																																																																																	.addGap(18,
																																																																																			18,
																																																																																			18)
																																																																																			.addComponent(
																																																																																					jCheckBox13)
																																																																																					.addGap(2,
																																																																																							2,
																																																																																							2)
																																																																																							.addComponent(
																																																																																									jCheckBox14)
																																																																																									.addPreferredGap(
																																																																																											javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																																																																																											.addComponent(
																																																																																													jCheckBox15)
																																																																																													.addGap(180,
																																																																																															180,
																																																																																															180)
																																																																																															.addComponent(
																																																																																																	jButton13)
																																																																																																	.addGap(66,
																																																																																																			66,
																																																																																																			66)))
																																																																																																			.addGap(26, 26, 26))));

			jTabbedPane1.addTab("Metal", jPanel5);

			jButton16.setText("Start Frost Drags");
			jButton16.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					jButton16ActionPerformed(evt);
				}
			});

			jButton17.setText("Remove");
			jButton17.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					jButton17ActionPerformed(evt);
				}
			});

			jList11.setModel(new javax.swing.AbstractListModel() {
				String[] strings = { "Dragon Bones", "Dragon Hide" };

				public int getSize() {
					return strings.length;
				}

				public Object getElementAt(int i) {
					return strings[i];
				}
			});
			jScrollPane11.setViewportView(jList11);

			jButton18.setText("Add");
			jButton18.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					jButton18ActionPerformed(evt);
				}
			});

			jList12.setModel(new javax.swing.AbstractListModel() {
				String[] strings = { "Grimy Rannar", "Rune Dagger", "Law Rune",
				" " };

				public int getSize() {
					return strings.length;
				}

				public Object getElementAt(int i) {
					return strings[i];
				}
			});
			jScrollPane12.setViewportView(jList12);

			jLabel29.setFont(new java.awt.Font("Tahoma", 0, 18));
			jLabel29.setText("Looting");

			jComboBox17.setModel(new javax.swing.DefaultComboBoxModel(
					new String[] { "None", "Snail", "Terrorbird", "Tortise",
					"Yak" }));

			jLabel30.setFont(new java.awt.Font("Tahoma", 0, 18));
			jLabel30.setText("Familiar");

			jComboBox6.setModel(new javax.swing.DefaultComboBoxModel(
					new String[] { "Trout", "Salmon", "Tuna", "Lobster",
							"SwordFish", "Shark", "CaveFish", " " }));

			jLabel6.setFont(new java.awt.Font("Tahoma", 0, 18));
			jLabel6.setText("Food");

			jComboBox18.setModel(new javax.swing.DefaultComboBoxModel(
					new String[] { "East", "West", "Chaos Tunnels", " " }));

			jLabel31.setFont(new java.awt.Font("Tahoma", 0, 18));
			jLabel31.setText("Location");

			jCheckBox16.setText("Cannon");

			jLabel32.setFont(new java.awt.Font("Tahoma", 0, 18));
			jLabel32.setForeground(new java.awt.Color(102, 0, 0));
			jLabel32.setText("Settings");

			jCheckBox17.setText("Use Glory to Bank");

			jCheckBox18.setSelected(true);
			jCheckBox18.setText("Tele in combat");

			jLabel33.setFont(new java.awt.Font("Tahoma", 0, 18));
			jLabel33.setForeground(new java.awt.Color(255, 0, 0));
			jLabel33.setText("Unsupported!");

			javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(
					jPanel6);
			jPanel6.setLayout(jPanel6Layout);
			jPanel6Layout
			.setHorizontalGroup(jPanel6Layout
					.createParallelGroup(
							javax.swing.GroupLayout.Alignment.LEADING)
							.addGroup(
									javax.swing.GroupLayout.Alignment.TRAILING,
									jPanel6Layout
									.createSequentialGroup()
									.addContainerGap(341,
											Short.MAX_VALUE)
											.addComponent(jLabel33)
											.addGap(63, 63, 63))
											.addGroup(
													jPanel6Layout
													.createParallelGroup(
															javax.swing.GroupLayout.Alignment.LEADING)
															.addGroup(
																	jPanel6Layout
																	.createSequentialGroup()
																	.addGap(53, 53, 53)
																	.addGroup(
																			jPanel6Layout
																			.createParallelGroup(
																					javax.swing.GroupLayout.Alignment.LEADING)
																					.addGroup(
																							jPanel6Layout
																							.createSequentialGroup()
																							.addGroup(
																									jPanel6Layout
																									.createParallelGroup(
																											javax.swing.GroupLayout.Alignment.LEADING)
																											.addGroup(
																													jPanel6Layout
																													.createParallelGroup(
																															javax.swing.GroupLayout.Alignment.LEADING,
																															false)
																															.addGroup(
																																	jPanel6Layout
																																	.createSequentialGroup()
																																	.addGap(11,
																																			11,
																																			11)
																																			.addComponent(
																																					jComboBox6,
																																					javax.swing.GroupLayout.PREFERRED_SIZE,
																																					javax.swing.GroupLayout.DEFAULT_SIZE,
																																					javax.swing.GroupLayout.PREFERRED_SIZE))
																																					.addGroup(
																																							jPanel6Layout
																																							.createSequentialGroup()
																																							.addGap(29,
																																									29,
																																									29)
																																									.addComponent(
																																											jLabel6,
																																											javax.swing.GroupLayout.DEFAULT_SIZE,
																																											javax.swing.GroupLayout.DEFAULT_SIZE,
																																											Short.MAX_VALUE)))
																																											.addGroup(
																																													jPanel6Layout
																																													.createSequentialGroup()
																																													.addGap(10,
																																															10,
																																															10)
																																															.addComponent(
																																																	jButton18,
																																																	javax.swing.GroupLayout.PREFERRED_SIZE,
																																																	60,
																																																	javax.swing.GroupLayout.PREFERRED_SIZE))
																																																	.addComponent(
																																																			jScrollPane12,
																																																			javax.swing.GroupLayout.PREFERRED_SIZE,
																																																			94,
																																																			javax.swing.GroupLayout.PREFERRED_SIZE))
																																																			.addGroup(
																																																					jPanel6Layout
																																																					.createParallelGroup(
																																																							javax.swing.GroupLayout.Alignment.LEADING)
																																																							.addGroup(
																																																									jPanel6Layout
																																																									.createSequentialGroup()
																																																									.addGap(36,
																																																											36,
																																																											36)
																																																											.addGroup(
																																																													jPanel6Layout
																																																													.createParallelGroup(
																																																															javax.swing.GroupLayout.Alignment.LEADING)
																																																															.addGroup(
																																																																	jPanel6Layout
																																																																	.createSequentialGroup()
																																																																	.addComponent(
																																																																			jScrollPane11,
																																																																			javax.swing.GroupLayout.PREFERRED_SIZE,
																																																																			120,
																																																																			javax.swing.GroupLayout.PREFERRED_SIZE)
																																																																			.addGap(49,
																																																																					49,
																																																																					49)
																																																																					.addComponent(
																																																																							jButton16))
																																																																							.addGroup(
																																																																									jPanel6Layout
																																																																									.createSequentialGroup()
																																																																									.addGroup(
																																																																											jPanel6Layout
																																																																											.createParallelGroup(
																																																																													javax.swing.GroupLayout.Alignment.TRAILING,
																																																																													false)
																																																																													.addComponent(
																																																																															jLabel31,
																																																																															javax.swing.GroupLayout.Alignment.LEADING,
																																																																															javax.swing.GroupLayout.DEFAULT_SIZE,
																																																																															javax.swing.GroupLayout.DEFAULT_SIZE,
																																																																															Short.MAX_VALUE)
																																																																															.addComponent(
																																																																																	jComboBox18,
																																																																																	javax.swing.GroupLayout.Alignment.LEADING,
																																																																																	javax.swing.GroupLayout.PREFERRED_SIZE,
																																																																																	javax.swing.GroupLayout.DEFAULT_SIZE,
																																																																																	javax.swing.GroupLayout.PREFERRED_SIZE))
																																																																																	.addGap(51,
																																																																																			51,
																																																																																			51)
																																																																																			.addGroup(
																																																																																					jPanel6Layout
																																																																																					.createParallelGroup(
																																																																																							javax.swing.GroupLayout.Alignment.LEADING)
																																																																																							.addComponent(
																																																																																									jLabel32)
																																																																																									.addComponent(
																																																																																											jCheckBox16)
																																																																																											.addComponent(
																																																																																													jCheckBox17)
																																																																																													.addComponent(
																																																																																															jCheckBox18)))))
																																																																																															.addGroup(
																																																																																																	jPanel6Layout
																																																																																																	.createSequentialGroup()
																																																																																																	.addGap(60,
																																																																																																			60,
																																																																																																			60)
																																																																																																			.addComponent(
																																																																																																					jButton17))))
																																																																																																					.addGroup(
																																																																																																							jPanel6Layout
																																																																																																							.createSequentialGroup()
																																																																																																							.addGap(86,
																																																																																																									86,
																																																																																																									86)
																																																																																																									.addGroup(
																																																																																																											jPanel6Layout
																																																																																																											.createParallelGroup(
																																																																																																													javax.swing.GroupLayout.Alignment.LEADING)
																																																																																																													.addComponent(
																																																																																																															jLabel30)
																																																																																																															.addComponent(
																																																																																																																	jLabel29)
																																																																																																																	.addComponent(
																																																																																																																			jComboBox17,
																																																																																																																			javax.swing.GroupLayout.PREFERRED_SIZE,
																																																																																																																			javax.swing.GroupLayout.DEFAULT_SIZE,
																																																																																																																			javax.swing.GroupLayout.PREFERRED_SIZE))))
																																																																																																																			.addContainerGap(
																																																																																																																					44,
																																																																																																																					Short.MAX_VALUE))));
			jPanel6Layout
			.setVerticalGroup(jPanel6Layout
					.createParallelGroup(
							javax.swing.GroupLayout.Alignment.LEADING)
							.addGroup(
									javax.swing.GroupLayout.Alignment.TRAILING,
									jPanel6Layout
									.createSequentialGroup()
									.addContainerGap(273,
											Short.MAX_VALUE)
											.addComponent(jLabel33)
											.addGap(136, 136, 136))
											.addGroup(
													jPanel6Layout
													.createParallelGroup(
															javax.swing.GroupLayout.Alignment.LEADING)
															.addGroup(
																	jPanel6Layout
																	.createSequentialGroup()
																	.addGap(25, 25, 25)
																	.addGroup(
																			jPanel6Layout
																			.createParallelGroup(
																					javax.swing.GroupLayout.Alignment.LEADING)
																					.addGroup(
																							jPanel6Layout
																							.createSequentialGroup()
																							.addGroup(
																									jPanel6Layout
																									.createParallelGroup(
																											javax.swing.GroupLayout.Alignment.LEADING)
																											.addGroup(
																													jPanel6Layout
																													.createSequentialGroup()
																													.addComponent(
																															jLabel6)
																															.addPreferredGap(
																																	javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																																	.addComponent(
																																			jComboBox6,
																																			javax.swing.GroupLayout.PREFERRED_SIZE,
																																			javax.swing.GroupLayout.DEFAULT_SIZE,
																																			javax.swing.GroupLayout.PREFERRED_SIZE)
																																			.addGap(18,
																																					18,
																																					18)
																																					.addComponent(
																																							jLabel30))
																																							.addGroup(
																																									jPanel6Layout
																																									.createSequentialGroup()
																																									.addComponent(
																																											jLabel31)
																																											.addPreferredGap(
																																													javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																																													.addComponent(
																																															jComboBox18,
																																															javax.swing.GroupLayout.PREFERRED_SIZE,
																																															javax.swing.GroupLayout.DEFAULT_SIZE,
																																															javax.swing.GroupLayout.PREFERRED_SIZE)))
																																															.addPreferredGap(
																																																	javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																																																	.addComponent(
																																																			jComboBox17,
																																																			javax.swing.GroupLayout.PREFERRED_SIZE,
																																																			javax.swing.GroupLayout.DEFAULT_SIZE,
																																																			javax.swing.GroupLayout.PREFERRED_SIZE)
																																																			.addGap(18,
																																																					18,
																																																					18)
																																																					.addComponent(
																																																							jLabel29)
																																																							.addPreferredGap(
																																																									javax.swing.LayoutStyle.ComponentPlacement.RELATED,
																																																									18,
																																																									Short.MAX_VALUE)
																																																									.addGroup(
																																																											jPanel6Layout
																																																											.createParallelGroup(
																																																													javax.swing.GroupLayout.Alignment.TRAILING,
																																																													false)
																																																													.addComponent(
																																																															jScrollPane12,
																																																															javax.swing.GroupLayout.Alignment.LEADING)
																																																															.addComponent(
																																																																	jScrollPane11,
																																																																	javax.swing.GroupLayout.Alignment.LEADING,
																																																																	javax.swing.GroupLayout.DEFAULT_SIZE,
																																																																	169,
																																																																	Short.MAX_VALUE))
																																																																	.addPreferredGap(
																																																																			javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																																																																			.addGroup(
																																																																					jPanel6Layout
																																																																					.createParallelGroup(
																																																																							javax.swing.GroupLayout.Alignment.BASELINE)
																																																																							.addComponent(
																																																																									jButton18)
																																																																									.addComponent(
																																																																											jButton17)))
																																																																											.addGroup(
																																																																													javax.swing.GroupLayout.Alignment.TRAILING,
																																																																													jPanel6Layout
																																																																													.createSequentialGroup()
																																																																													.addPreferredGap(
																																																																															javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																																																																															.addComponent(
																																																																																	jLabel32)
																																																																																	.addGap(18,
																																																																																			18,
																																																																																			18)
																																																																																			.addComponent(
																																																																																					jCheckBox16)
																																																																																					.addGap(2,
																																																																																							2,
																																																																																							2)
																																																																																							.addComponent(
																																																																																									jCheckBox17)
																																																																																									.addPreferredGap(
																																																																																											javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																																																																																											.addComponent(
																																																																																													jCheckBox18)
																																																																																													.addGap(180,
																																																																																															180,
																																																																																															180)
																																																																																															.addComponent(
																																																																																																	jButton16)
																																																																																																	.addGap(66,
																																																																																																			66,
																																																																																																			66)))
																																																																																																			.addGap(26, 26, 26))));

			jTabbedPane1.addTab("Frost", jPanel6);

			javax.swing.GroupLayout layout = new javax.swing.GroupLayout(
					getContentPane());
			getContentPane().setLayout(layout);
			layout.setHorizontalGroup(layout.createParallelGroup(
					javax.swing.GroupLayout.Alignment.LEADING).addComponent(
							jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 516,
							Short.MAX_VALUE));
			layout.setVerticalGroup(layout.createParallelGroup(
					javax.swing.GroupLayout.Alignment.LEADING).addComponent(
							jTabbedPane1, javax.swing.GroupLayout.Alignment.TRAILING,
							javax.swing.GroupLayout.DEFAULT_SIZE, 459, Short.MAX_VALUE));

			pack();
		}// </editor-fold>

		private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
			// add
		}

		private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {
			// remove
		}

		private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {
			if (jComboBox7.getSelectedItem().toString() == "Chaos Tunnels") {
				SETLOC = 2;
			}
			if (jComboBox7.getSelectedItem().toString() == "East") {
				SETLOC = 1;
			}
			if (jComboBox7.getSelectedItem().toString() == "West") {
				SETLOC = 0;
			}
			food = foodArray[jComboBox1.getSelectedIndex()];
			amount = Integer.parseInt(jTextPane1.getText());
			this.dispose();
		}

		private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {
			// TODO add your handling code here:
		}

		private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {
			// TODO add your handling code here:
		}

		private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {
			// TODO add your handling code here:
		}

		private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {
			// TODO add your handling code here:
		}

		private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {
			// TODO add your handling code here:
		}

		private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {
			// TODO add your handling code here:
		}

		private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) {
			// TODO add your handling code here:
		}

		private void jButton11ActionPerformed(java.awt.event.ActionEvent evt) {
			// TODO add your handling code here:
		}

		private void jButton12ActionPerformed(java.awt.event.ActionEvent evt) {
			// TODO add your handling code here:
		}

		private void jButton13ActionPerformed(java.awt.event.ActionEvent evt) {
			// TODO add your handling code here:
		}

		private void jButton14ActionPerformed(java.awt.event.ActionEvent evt) {
			// TODO add your handling code here:
		}

		private void jButton15ActionPerformed(java.awt.event.ActionEvent evt) {
			// TODO add your handling code here:
		}

		private void jButton16ActionPerformed(java.awt.event.ActionEvent evt) {
			// TODO add your handling code here:
		}

		private void jButton17ActionPerformed(java.awt.event.ActionEvent evt) {
			// TODO add your handling code here:
		}

		private void jButton18ActionPerformed(java.awt.event.ActionEvent evt) {
			// TODO add your handling code here:
		}

		/**
		 * @param args
		 *            the command line arguments
		 */

		// Variables declaration - do not modify
		private javax.swing.JButton jButton1;
		private javax.swing.JButton jButton10;
		private javax.swing.JButton jButton11;
		private javax.swing.JButton jButton12;
		private javax.swing.JButton jButton13;
		private javax.swing.JButton jButton14;
		private javax.swing.JButton jButton15;
		private javax.swing.JButton jButton16;
		private javax.swing.JButton jButton17;
		private javax.swing.JButton jButton18;
		private javax.swing.JButton jButton2;
		private javax.swing.JButton jButton3;
		private javax.swing.JButton jButton4;
		private javax.swing.JButton jButton5;
		private javax.swing.JButton jButton6;
		private javax.swing.JButton jButton7;
		private javax.swing.JButton jButton8;
		private javax.swing.JButton jButton9;
		private javax.swing.JCheckBox jCheckBox1;
		private javax.swing.JCheckBox jCheckBox10;
		private javax.swing.JCheckBox jCheckBox11;
		private javax.swing.JCheckBox jCheckBox12;
		private javax.swing.JCheckBox jCheckBox13;
		private javax.swing.JCheckBox jCheckBox14;
		private javax.swing.JCheckBox jCheckBox15;
		private javax.swing.JCheckBox jCheckBox16;
		private javax.swing.JCheckBox jCheckBox17;
		private javax.swing.JCheckBox jCheckBox18;
		private javax.swing.JCheckBox jCheckBox2;
		private javax.swing.JCheckBox jCheckBox3;
		private javax.swing.JCheckBox jCheckBox4;
		private javax.swing.JCheckBox jCheckBox5;
		private javax.swing.JCheckBox jCheckBox6;
		private javax.swing.JCheckBox jCheckBox7;
		private javax.swing.JCheckBox jCheckBox8;
		private javax.swing.JCheckBox jCheckBox9;
		private javax.swing.JComboBox jComboBox1;
		private javax.swing.JComboBox jComboBox10;
		private javax.swing.JComboBox jComboBox11;
		private javax.swing.JComboBox jComboBox12;
		private javax.swing.JComboBox jComboBox13;
		private javax.swing.JComboBox jComboBox14;
		private javax.swing.JComboBox jComboBox15;
		private javax.swing.JComboBox jComboBox16;
		private javax.swing.JComboBox jComboBox17;
		private javax.swing.JComboBox jComboBox18;
		private javax.swing.JComboBox jComboBox2;
		private javax.swing.JComboBox jComboBox3;
		private javax.swing.JComboBox jComboBox4;
		private javax.swing.JComboBox jComboBox5;
		private javax.swing.JComboBox jComboBox6;
		private javax.swing.JComboBox jComboBox7;
		private javax.swing.JComboBox jComboBox8;
		private javax.swing.JComboBox jComboBox9;
		private javax.swing.JLabel jLabel1;
		private javax.swing.JLabel jLabel10;
		private javax.swing.JLabel jLabel11;
		private javax.swing.JLabel jLabel12;
		private javax.swing.JLabel jLabel13;
		private javax.swing.JLabel jLabel14;
		private javax.swing.JLabel jLabel15;
		private javax.swing.JLabel jLabel16;
		private javax.swing.JLabel jLabel17;
		private javax.swing.JLabel jLabel18;
		private javax.swing.JLabel jLabel19;
		private javax.swing.JLabel jLabel2;
		private javax.swing.JLabel jLabel20;
		private javax.swing.JLabel jLabel21;
		private javax.swing.JLabel jLabel22;
		private javax.swing.JLabel jLabel23;
		private javax.swing.JLabel jLabel24;
		private javax.swing.JLabel jLabel25;
		private javax.swing.JLabel jLabel26;
		private javax.swing.JLabel jLabel27;
		private javax.swing.JLabel jLabel28;
		private javax.swing.JLabel jLabel29;
		private javax.swing.JLabel jLabel3;
		private javax.swing.JLabel jLabel30;
		private javax.swing.JLabel jLabel31;
		private javax.swing.JLabel jLabel32;
		private javax.swing.JLabel jLabel33;
		private javax.swing.JLabel jLabel34;
		private javax.swing.JLabel jLabel35;
		private javax.swing.JLabel jLabel36;
		private javax.swing.JLabel jLabel37;
		private javax.swing.JLabel jLabel38;
		private javax.swing.JLabel jLabel4;
		private javax.swing.JLabel jLabel5;
		private javax.swing.JLabel jLabel6;
		private javax.swing.JLabel jLabel7;
		private javax.swing.JLabel jLabel8;
		private javax.swing.JLabel jLabel9;
		private javax.swing.JList jList1;
		private javax.swing.JList jList10;
		private javax.swing.JList jList11;
		private javax.swing.JList jList12;
		private javax.swing.JList jList2;
		private javax.swing.JList jList3;
		private javax.swing.JList jList4;
		private javax.swing.JList jList5;
		private javax.swing.JList jList6;
		private javax.swing.JList jList7;
		private javax.swing.JList jList8;
		private javax.swing.JList jList9;
		private javax.swing.JPanel jPanel1;
		private javax.swing.JPanel jPanel2;
		private javax.swing.JPanel jPanel3;
		private javax.swing.JPanel jPanel4;
		private javax.swing.JPanel jPanel5;
		private javax.swing.JPanel jPanel6;
		private javax.swing.JScrollPane jScrollPane1;
		private javax.swing.JScrollPane jScrollPane10;
		private javax.swing.JScrollPane jScrollPane11;
		private javax.swing.JScrollPane jScrollPane12;
		private javax.swing.JScrollPane jScrollPane13;
		private javax.swing.JScrollPane jScrollPane2;
		private javax.swing.JScrollPane jScrollPane3;
		private javax.swing.JScrollPane jScrollPane4;
		private javax.swing.JScrollPane jScrollPane5;
		private javax.swing.JScrollPane jScrollPane6;
		private javax.swing.JScrollPane jScrollPane7;
		private javax.swing.JScrollPane jScrollPane8;
		private javax.swing.JScrollPane jScrollPane9;
		private javax.swing.JTabbedPane jTabbedPane1;
		private javax.swing.JTextPane jTextPane1;
		// End of variables declaration
	}

	@SuppressWarnings("serial")
	private class MousePathPoint extends Point { // credits to Enfilade
		private int toColor(double d) {
			return Math.min(255, Math.max(0, (int) d));
		}

		private long finishTime;
		private double lastingTime;

		public MousePathPoint(int x, int y, int lastingTime) {
			super(x, y);
			this.lastingTime = lastingTime;
			finishTime = System.currentTimeMillis() + lastingTime;
		}

		public boolean isUp() {
			return System.currentTimeMillis() > finishTime;
		}

		public Color getColor() {
			return new Color(
					0,
					153,
					0,
					toColor(256 * ((finishTime - System.currentTimeMillis()) / lastingTime)));
		}
	}
}
