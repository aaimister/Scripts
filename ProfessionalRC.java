import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Ellipse2D;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ListIterator;
import java.util.LinkedList;
import java.util.Properties;

import org.rsbot.Configuration;
import org.rsbot.event.events.MessageEvent;
import org.rsbot.event.listeners.MessageListener;
import org.rsbot.event.listeners.PaintListener;

import org.rsbot.script.methods.Game;
import org.rsbot.script.methods.Skills;

import org.rsbot.script.Script;
import org.rsbot.script.ScriptManifest;

import org.rsbot.script.util.Filter;
import org.rsbot.script.wrappers.RSArea;
import org.rsbot.script.wrappers.RSItem;
import org.rsbot.script.wrappers.RSNPC;
import org.rsbot.script.wrappers.RSObject;
import org.rsbot.script.wrappers.RSPlayer;
import org.rsbot.script.wrappers.RSTile;

@ScriptManifest(
	authors = "inspercho",
	name = "professional runecrafting",
	version = 3,
	description = "AIO free to play normal or master/runner runecrafting bot.",
	keywords = {"runecrafting", "runecrafter", "runecraft", "rc", "aio", "master", "runner", "slave"},
	website = "http://www.powerbot.org/community/topic/435261-master-and-slave-rc/")
public class ProfessionalRC extends Script implements PaintListener, MessageListener, MouseListener, KeyListener {
	private static final LinkedList<Strategy> stratagies = new LinkedList<Strategy>();
	private static String status;
	private static ListIterator<Strategy> iterator;
	
	private static final int NORMAL = 0;
	private static final int MASTER = 1;
	private static final int RUNNER = 2;
	
	private final int ESSENCE_ID = 1436;
	private final int ESSENCE_NOTE_ID = 1437;
	private Properties prop = null;
	private InfoDatabase info = null;

	private boolean init = true;
	private boolean init_gui = true;
	private boolean hide_gui = false;
	private boolean cursor = false;
	private boolean location_list = false;
	private boolean[] keylisten = new boolean[2];
	private String[] locations = new String[]{
			"Air Altar",
			"Body Altar",
			"Earth Altar",
			"Fire Altar",
			"Mind Altar",
			"Water Altar (dray)",
			"Water Altar (alkarhid)"
		};
	private int locations_index = 0;
	
	private int requesting = -1;
	private int failCount = 3;
	private boolean changeWorld = false;
	private String loc = Configuration.Paths.getScriptCacheDirectory() + File.separator;

	private int type = -1;
	private static String playerTraded = null;	
	private static String playerServer = null;	
	private static int playerWorld = -1;		
	private boolean useMusician = false;
	private Info location = null;

	private static int altarsClicked = 0;
	private static int craftRune = -1;
	private static int note = -1;
	private static int numberToCraft = -1;


	public boolean onStart() {
		prop = new Properties();	
		
		FileInputStream in = null;
		try {
			in = new FileInputStream(loc + "rc.p");
			prop.load(in);
			playerServer = prop.getProperty("world");
			playerTraded = prop.getProperty("name");
			useMusician = prop.getProperty("musician").equals("true");
			locations_index = Integer.parseInt(prop.getProperty("location"));
			location = getLocation(locations[locations_index]);
		} catch (Exception e) {
			log.severe("No saved settings were found. Will create file on script start.");
		} finally {
			try { in.close(); } catch(Exception e) {}
		}
		
		return true;
	}
	
	@Override
	public int loop() {
		if(init) {
			// if not logged in, wait
			if(!game.isLoggedIn()) { return 500; }
			if(init_gui) { return 500; }
			
			try {
				playerWorld = Integer.parseInt(playerServer);
			} catch (Exception e) {
				log.severe("failed detecting input world");
				stopScript();
			}
			
			if(type != MASTER) {
				FileOutputStream out = null;
				try {
					out = new FileOutputStream(loc + "rc.p");
					prop.setProperty("world", "" + playerServer);
					prop.setProperty("name", playerTraded.replace("\t", "").replace("\n", ""));
					prop.setProperty("musician", "" + useMusician);
					prop.setProperty("location", "" + locations_index);
	
					prop.store(out, null);
				} catch(Exception e) {
					log.severe("Could not store settings! Script will still function as normal however.");
				} finally {
					try { out.close(); } catch(Exception e) {}
				}
			}
			
			info = new InfoDatabase();
			
			switch(type) {
				case NORMAL: 
					numberToCraft = 28;
					log(locations[locations_index] + " runecrafter started.");
					stratagies.add(new ExitAltar());
					stratagies.add(new GoBank());
					stratagies.add(new DoBank());
					stratagies.add(new EnterAltar());
					stratagies.add(new GoAltar());
					stratagies.add(new CraftRunes());
					break;
				case MASTER: 
					numberToCraft = 26;
					log(locations[locations_index] + " master started.");
					note = ESSENCE_NOTE_ID;
					playerTraded = null;
					stratagies.add(new OfferRunes());
					stratagies.add(new ConfirmTrade());
					stratagies.add(new CraftRunes());
					stratagies.add(new DropJunk());
					stratagies.add(new AcceptTrade());
					break;
				case RUNNER:
					numberToCraft = 26;
					log(locations[locations_index] + " runner started for " + playerTraded + ".");
					stratagies.add(new OfferRunes());
					stratagies.add(new ConfirmTrade());
					stratagies.add(new ExitAltar());
					stratagies.add(new GoBank());
					stratagies.add(new DoBank());
					stratagies.add(new EnterAltar());
					stratagies.add(new GoAltar());
					stratagies.add(new FindPlayer());
					stratagies.add(new SendTrade());
					break;
			}
			
			startTime = System.currentTimeMillis();
			startExp = skills.getCurrentExp(Skills.RUNECRAFTING);
			
			init = false;
		}
		
		mouse.setSpeed(random(4, 7));
		
		if (!game.isLoggedIn() || changeWorld) {
			if(!lobby.inLobby()) {
				return loginAndWait();
			}
			if(interfaces.getComponent(910, 11).getText().endsWith(" " + playerWorld)) {
				return loginAndWait();
			} 
			if(lobby.switchWorlds(playerWorld)) {
				return loginAndWait();
			}
			return 1000;
		}
		
		if(game.getPlane() == 1) {
			useGate(info.STAIR_TILE, "Climb-down");
			return 1000;
		}		
		
		// somehow this opened up
		if(interfaces.get(752).getComponent(1).getText().contains("Enter")) {
			keyboard.sendText("", true);
		}
		
		// close any opened dialog box (level up menu, randoms, etc)
		if(interfaces.get(Game.INTERFACE_LEVEL_UP).isValid() ||
			interfaces.canContinue()) {
			interfaces.clickContinue();
			return random(900, 1100)/8;
		}
		
		iterator = stratagies.listIterator();
		while(iterator.hasNext()) {
			Strategy s = (Strategy)iterator.next();
			if(s.isValid()) {
				status = s.getStatus();
				//log(status);
				return s.execute();
			}
		}
		
		status = "waiting.";
		return random(900, 1100)/8;
	}
	
	@Override
	public void messageReceived(MessageEvent e) {
		//if(e.getID() == MessageEvent.MESSAGE_TRADE_REQ);
		if (e.getMessage().contains("wishes to trade with ")) {
			if(type == MASTER) playerTraded = e.getSender();
		}

		String serverString = e.getMessage().toLowerCase();
		if (serverString.contains("power into ") && !serverString.contains(":")) {
			altarsClicked++;
		}
	}
	
	private int startExp = 0;
	private int expGained = 0;
	private int expHour = 0;
	private long startTime = 0;
	private long millis = 0;
	private final RenderingHints antialiasing = new RenderingHints(
			RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

	private final Rectangle normal_button = new Rectangle(18, 381, 133, 19);
	private final Rectangle master_button = new Rectangle(18, 406, 133, 19);
	private final Rectangle runner_button = new Rectangle(18, 431, 133, 19);
	private final Rectangle start_button = new Rectangle(373, 449, 133, 19);

	private final Rectangle crafter_box = new Rectangle(255, 371, 133, 15);
	private final Rectangle server_box = new Rectangle(255, 386, 133, 15);
	private final Rectangle musician_box = new Rectangle(255, 401, 15, 15);
	private final Rectangle location_box = new Rectangle(255, 416, 133, 15);

	private final Rectangle exp_percent = new Rectangle(6, 456, 1, 18);
	
	private final Rectangle[] locations_box = new Rectangle[locations.length];
	
	private final Ellipse2D hide_button = new Ellipse2D.Float(517, 87, 31, 29);
	
	@Override
	public void onRepaint(Graphics render) {
		Graphics2D g = (Graphics2D)render;
		g.setRenderingHints(antialiasing);
		
		// draw oval
		if(hide_gui) {
			g.setColor(new Color(255, 102, 0, 105));
			g.fill(hide_button);
			return;
		}
		g.setColor(new Color(102, 255, 0, 105));
		g.fill(hide_button);
		g.setColor(new Color(204, 204, 0));
		g.draw(hide_button);

		// draw background
		g.setColor(new Color(203, 186, 153));
		g.fillRect(3, 341, 512, 135);
		g.setColor(Color.black);
		g.setStroke(new BasicStroke(5));
		g.drawRect(3, 341, 512, 135);
		g.drawRect(20, 320, 480, 40);
		g.setColor(new Color(203, 186, 153));
		g.fillRect(20, 320, 480, 40);
		
		// put texts
		g.setColor(Color.black);
		g.setFont(new Font("Broadway", 1, 30));
		g.drawString("Professional Runecrafter", 32, 352);
		g.setFont(new Font("BrowalliaUPC", 1, 23));
		g.drawString("GUI", 519, 107);
		
		if(init) {
			// draw seperator
			g.setColor(Color.black);
			g.drawLine(166, 362, 166, 474);
			
			// draw boxes
			g.setColor(Color.black);
			g.setStroke(new BasicStroke(1));
			g.draw(normal_button);
			g.draw(master_button);
			g.draw(runner_button);
			g.draw(start_button);
			g.setColor(new Color(0, 0, 0, 80));
			g.fill(normal_button);
			g.fill(master_button);
			g.fill(runner_button);
			g.fill(start_button);

			g.setFont(new Font("Times", 0, 15));
			g.setColor(Color.black);
			int i = 370;
			Rectangle selected = null;
			switch(type) {
				case NORMAL: // normal
					selected = normal_button;
					
					g.setColor(new Color(0, 0, 0, 80));
					g.fill(musician_box);
					g.fill(location_box);
					
					g.setColor(Color.black);
					g.setStroke(new BasicStroke(1));
					g.draw(musician_box);
					g.draw(location_box);

					g.drawString("", 180, i+=15);
					g.drawString("", 180, i+=15);
					g.drawString("Musician:", 180, i+=15);
					g.drawString("" + (useMusician?"X":""), 258, i);
					
					g.drawString("Location:", 180, i+=15);
					if(location_list) {
						for(int l = locations.length - 1; l >= 0; l--) {
							i -= 15;
							locations_box[l] = new Rectangle(260, i, 133, 15);
							g.setColor(new Color(203, 186, 153));
							g.fill(locations_box[l]);

							g.setColor(Color.black);
							g.draw(locations_box[l]);
							g.drawLine(260, i, 293, i);

							g.setFont(new Font("Times", 0, 13));
							g.drawString("" + locations[l], 263, i+14);
						}
					} else {
						if(location == null) location = new AirInfo();
						g.drawString("" + locations[locations_index].toString(), 256, i);
						g.drawLine(380, i-9, 386, i-9);
						g.drawString("v", 380, i);
					}
					break;
				case MASTER: // master
					selected = master_button;
					
					String loc = detectLocation();
					String ess = detectEssence();
					String inv = detectInventory()?"You must only have essence in your inventory.":"Script will drop random event items.";
					g.drawString("Detected " + loc + " altar.", 180, i+=15);
					g.drawString("Detected " + ess + " noted essence.", 180, i+=15);
					g.drawString(inv, 180, i+=15);
					break;
				case RUNNER: // runner
					selected = runner_button;
					
					g.setColor(new Color(0, 0, 0, 80));
					g.fill(crafter_box);
					g.fill(server_box);
					g.fill(musician_box);
					g.fill(location_box);
					
					g.setColor(Color.black);
					g.setStroke(new BasicStroke(1));
					g.draw(crafter_box);
					g.draw(server_box);
					g.draw(musician_box);
					g.draw(location_box);

					g.drawString("Username:", 180, i+=15);	
					if(playerTraded == null) playerTraded = "";
					g.drawString("" + playerTraded + (keylisten[0]?(cursor?"|":""):""), 256, i);
					
					g.drawString("Server:", 180, i+=15);
					if(playerServer == null) playerServer = "";
					g.drawString("" + playerServer + (keylisten[1]?(cursor?"|":""):""), 256, i);
					
					g.drawString("Musician:", 180, i+=15);
					g.drawString("" + (useMusician?"X":""), 258, i);
					
					g.drawString("Location:", 180, i+=15);
					if(location_list) {
						for(int l = locations.length - 1; l >= 0; l--) {
							i -= 15;
							locations_box[l] = new Rectangle(260, i, 133, 15);
							g.setColor(new Color(203, 186, 153));
							g.fill(locations_box[l]);
							
							g.setColor(Color.black);
							g.draw(locations_box[l]);
							g.drawLine(260, i, 293, i);

							g.setFont(new Font("Times", 0, 13));
							g.drawString("" + locations[l], 263, i+14);
						}
					} else {
						if(location == null) location = new AirInfo();
						g.drawString("" + locations[locations_index].toString(), 256, i);
						g.drawLine(380, i-9, 386, i-9);
						g.drawString("v", 380, i);
					}

					break;
				default:
					g.drawString("Select an option from the left.", 180, i+=15);
			}
			cursor = !cursor;

			if(selected != null) {
				g.setColor(Color.white);
				g.fill(selected);
				g.setColor(Color.black);
				g.draw(selected);
			}
			
			// put texts
			g.setColor(Color.black);
			g.setFont(new Font("BrowalliaUPC", 0, 30));
			g.drawString("normal", 55, 397);
			g.drawString("master", 55, 422);
			g.drawString("runner", 55, 447);
			g.drawString("start", 422, 467);
		} else {
			millis = System.currentTimeMillis() - startTime;
		
			expGained = skills.getCurrentExp(Skills.RUNECRAFTING) - startExp;
			expHour = (int) ((expGained) * 3600000D / millis);

			int i = 380;
			int j = 30;
			g.setFont(new Font("BrowalliaUPC", 0, 23));
			g.setColor(Color.black);
		
			g.drawString("Runtime: " + formatDuration(millis), j, i+=15);
			g.drawString("Status: " + status, j, i+=15);

			switch(type) {
				case NORMAL:
				case MASTER: 
					int pnl = skills.getPercentToNextLevel(Skills.RUNECRAFTING);
					exp_percent.setSize((int)((double)pnl / 100 * 508), 18);
					g.setColor(new Color(20, 175, 20));
					g.fill(exp_percent);
					
					g.setColor(Color.black);
					g.drawLine(8, 456, 510, 456);
					//g.drawString("Altars Clicked (per hour): " + altarsClicked + "(" + altarsHour + ")", j, i+=15);
					g.drawString("Exp Gained (per hour): " + expGained + " (" + expHour + ")", j, i+=15);

					millis = skills.getTimeTillNextLevel(Skills.RUNECRAFTING, startExp, millis);
					g.drawString(pnl + "% until level " + (skills.getCurrentLevel(Skills.RUNECRAFTING)+1) + " (" + formatDuration(millis) + " remaining)", 140, 471);
				break;
				case RUNNER:
					
				break;
			}
		}
		
		// mouse paints
		int s = 4;
		if(mouse.getPressTime() == -1 ||
			System.currentTimeMillis() - mouse.getPressTime() >= 1000) {
				g.fillRect(mouse.getLocation().x-(s/2),mouse.getLocation().y-(s/2),s,s);
		}
	}
	

	public static String formatDuration(long ms) {
		String strReturn = "";
		long lRest;
		long t = 0;
		
		t = ms / 86400000L;
		if(t>0) strReturn += String.valueOf(t) + ":";
		lRest = ms % 86400000L;

		t = lRest / 3600000L;
		strReturn += (t<9?"0":"") + String.valueOf(t) + ":";
		lRest %= 3600000L;

		t = lRest / 60000L;
		strReturn += (t<9?"0":"") + String.valueOf(t) + ":";
		lRest %= 60000L;

		t = lRest / 1000L;
		strReturn += (t<9?"0":"") + String.valueOf(t);
		lRest %= 1000L;

		return strReturn;
	}
	
	private int loginAndWait() {
		changeWorld = false;
		env.enableRandoms();
		//env.enableRandom("Login");
		return random(900, 1100)*2;
	}
	
	private void useGate(RSTile tile, String action) {
		final RSObject g = objects.getTopAt(tile);
	
		if (g != null) {
			if (fixAngle(tile)) {
				if (g.isOnScreen()) {
					g.interact(action);
				}
			}
		}
	}
	
	final private boolean fixAngle(RSTile t) {
		if (calc.tileOnScreen(t))
			return true;
		if (!calc.tileOnScreen(t)) {
			camera.setAngle(camera.getTileAngle(t));
			if (!calc.tileOnScreen(t)) {
				camera.setPitch(camera.getPitch() + random(20, 50));
				if (!calc.tileOnScreen(t))
					walking.walkTo(t);
			}
		}
		return false;
	}

	// return true to continue with script, false to continue resting.
	private boolean checkEnergy() {
		if(!useMusician || walking.getEnergy() > 95) return true;
	
		// are we already resting
		for (int t : info.getRestingAnim()) {
			if(t == getMyPlayer().getAnimation()) return false;
		}

		// if we have 35 run energy, do not rest.
		if(walking.getEnergy() > 35) {
			return true;
		} else {				
			RSNPC musician = npcs.getNearest(info.getMusicianID());
			if (musician != null) {
				// only stop if we're close by, don't want to waste time backtracking
				int t = info.getMusicianID()==30?25:10;
				if (calc.distanceTo(musician) < t) {
					walking.walkTileMM(musician.getLocation());
					musician.interact("Listen-to");
					return false;
				}
			}
		}
		return true;
	}
	
	private RSTile getTileInAreaTowardsTile(RSArea area, RSTile dest) {
		RSTile closest = walking.getClosestTileOnMap(dest);
		RSTile inArea = area.getNearestTile(closest);
		inArea = inArea.randomize(3, 3);
		return area.getNearestTile(inArea);
	}
	
	private void walk(RSTile location) {
		if (!walking.isRunEnabled() && walking.getEnergy() > 20)
			walking.setRun(true);

		if (!getMyPlayer().isMoving()
				|| calc.distanceTo(walking.getDestination()) < 6) {
			location = location.randomize(2, 2);
			RSTile newTile = walking.getClosestTileOnMap(location);
			if (calc.tileOnScreen(newTile)) {
				mouse.move(calc.tileToScreen(newTile));
				menu.click("Walk here");
			} else {
				walking.walkTileMM(newTile);
			}
		}
	}

	private String detectLocation() {
		String loc = null;
		
		// find nearest altar
		RSObject altar = objects.getNearest(
			2478, 2479, 2480, 2481, 2482, 2483
		);
		
		// no altar found nearby... this is bad.
		if(altar == null) {
			log.severe("no altar found! move to an altar!");
			stopScript();
		}
		
		// figure out what type of rune we're making.
		switch(altar.getID()) {
			case 2478: craftRune = 556; loc = "air"; break; // air
			case 2479: craftRune = 555; loc = "mind"; break; // mind??
			case 2480: craftRune = 555; loc = "water"; break; // water
			case 2481: craftRune = 557; loc = "earth"; break; // earth
			case 2482: craftRune = 554; loc = "fire"; break; // fire
			case 2483: craftRune = 554; loc = "body"; break; // body???
		}
		return loc;
	}

	private String detectEssence() {
		return Integer.toString(inventory.getCount(true, ESSENCE_NOTE_ID));
	}
	
	private boolean detectInventory() {
		return inventory.getCount() > 2;
	}
	
	private Info getLocation(String loc) {
		Info l = null;
		if(loc.contains("Air Altar")) {
			l = new AirInfo();
		} else if(loc.contains("Body Altar")) {
			l = new BodyInfo();
		} else if(loc.contains("Earth Altar")) {
			l = new EarthInfo();
		} else if(loc.contains("Fire Altar")) {
			l = new FireInfo();
		} else if(loc.contains("Mind Altar")) {
			l = new MindInfo();
		} else if(loc.contains("Water Altar")) {
			if(loc.contains("dray")) {
				l = new WaterInfo(true);					
			} else if(loc.contains("alkarhid")) {
				l = new WaterInfo(false);
			}
		}
		return l;
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		keylisten[0] = false;
		keylisten[1] = false;
		
		if(location_list) {
			for(int i = 0; i < locations_box.length; i++) {
				if(locations_box[i].contains(e.getPoint())) {
					location = getLocation(locations[i]);
					locations_index = i;
				}
			}
			location_list = false;
			return;
		}
		
		if(start_button.contains(e.getPoint()) && type != -1) {
			init_gui = false;
		} else if (hide_button.contains(e.getPoint())) {
			hide_gui = !hide_gui;
		} else if (crafter_box.contains(e.getPoint())) {
			keylisten[0] = true;
		} else if (server_box.contains(e.getPoint())) {
			keylisten[1] = true;
		} else if (musician_box.contains(e.getPoint())) {
			useMusician = !useMusician;
		} else if (location_box.contains(e.getPoint())) {
			location_list = true;
		} else if (normal_button.contains(e.getPoint())) {
			type = 0;
		} else if (master_button.contains(e.getPoint())) {
			type = 1;
		} else if (runner_button.contains(e.getPoint())) {
			type = 2;
		}
	}
	
	@Override
	public void keyTyped(KeyEvent e) {
		if(keylisten[0]) {
			if(e.getKeyChar() == '\b') {
				playerTraded = playerTraded.substring(0, playerTraded.length() - 1);
			} else {
				playerTraded += e.getKeyChar();
			}
		} else if(keylisten[1]) {
			if(e.getKeyChar() == '\b') {
				playerServer = playerServer.substring(0, playerServer.length() - 1);
			} else if(e.getKeyChar() <= '9' && e.getKeyChar() >= '0') {
				playerServer += e.getKeyChar();
			}
		}
	}
	
	private interface Strategy {
		public boolean isValid();
		public int execute();
		public String getStatus();
	}

	private class ExitAltar implements Strategy {
		public boolean isValid() {
			if(trade.inTrade()) return false;
			return (info.inAltar() && !inventory.contains(info.getEssenceID()));
		}
		
		public int execute() {
			if(calc.distanceTo(info.PORTAL_TILE) > 5) {
				walking.walkTileMM(info.PORTAL_TILE);
			}
			useGate(info.PORTAL_TILE, "Enter");	
			return random(900, 1100)/2;
		}
		
		public String getStatus() {
			return "exiting the altar";
		}
	}
	
	private class DoBank implements Strategy {
		public boolean isValid() {
			return (info.atBank() && inventory.getCount(info.getEssenceID()) != numberToCraft);
		}
		
		public int execute() {
			if (bank.isOpen()) {
				if (inventory.getCount() > 0)
					bank.depositAll();
				bank.withdraw(info.getEssenceID(), numberToCraft);
				return random(900, 1100)/2;
			} else {
				if(bank.open()) failCount = 3;
				else failCount--;
			}
			if(failCount < 1) {
				walking.walkTileMM(info.BANK_AREA.getCentralTile());
				failCount = 3;
			}
			return random(900, 1100)/2;
		}
		
		public String getStatus() {
			return "banking";
		}
	}
	
	private class GoBank implements Strategy {
		public boolean isValid() {
			if(info.inAltar() || info.atBank()) return false;
			return !inventory.contains(info.getEssenceID());
		}
		
		public int execute() {
			if (checkEnergy())
				walk(getTileInAreaTowardsTile(info.getPathArea(), info.BANK_TILE));
			return random(900, 1100);
		}
		
		public String getStatus() {
			return "walking to bank";
		}
	}
	
	private class EnterAltar implements Strategy {
		public boolean isValid() {
			if(!inventory.contains(info.getEssenceID()) || info.inAltar()) return false;
			RSObject altar = objects.getTopAt(info.ALTAR_TILE);
			return (altar != null && calc.distanceTo(info.ALTAR_TILE) < 10);
		}
		
		public int execute() {
			if(calc.distanceTo(info.ALTAR_TILE) > 5) {
				walking.walkTileMM(info.ALTAR_TILE);
			}
			useGate(info.ALTAR_TILE, "Enter");
			return random(900, 1100)/2;
		}
		
		public String getStatus() {
			return "entering the altar";
		}
	}
	
	private class GoAltar implements Strategy {
		public boolean isValid() {
			return (!info.inAltar() && inventory.getCount(info.getEssenceID()) == numberToCraft);
		}
		
		public int execute() {
			if(bank.close()) return random(900, 1100)/4;
			if (checkEnergy()) {
				walk(getTileInAreaTowardsTile(info.getPathArea(),
						info.ALTAR_TILE));
			}
			return random(900, 1100);
		}
		
		public String getStatus() {
			return "walking to altar";
		}
	}
	
	private class CraftRunes implements Strategy {
		RSObject altar; 

		public boolean isValid() {
			// if we have runes to craft, craft them on nearest altar
			return inventory.contains(ESSENCE_ID);
		}

		public int execute() {
			// find nearest altar
			altar = objects.getNearest(
				2478, 2479, 2480, 2481, 2482, 2483
			);
			
			// no altar found nearby... this is bad.
			if(altar == null) return random(900, 1100);
			
			// if we are somewhat far from the altar, move to it before crafting.
			if(calc.distanceTo(altar.getLocation()) > 2) {
				walking.walkTileMM(altar.getLocation());
				return random(900, 1100);
			}
			
			// craft the runes
			if(altar.isOnScreen()) {
				altar.interact("Craft-rune");
				return random(900, 1100);		
			}
			
			return random(900, 1100)/4;
		}

		public String getStatus() {
			// no altar found nearby... this is bad.
			if(objects.getNearest(2478, 2479, 2480, 2481, 2482, 2483) == null)
				return "no altar found! move to an altar!";
			return "crafting runes";
		}
	}
	
	public class FindPlayer implements Strategy {
		RSPlayer crafter = null;
		public boolean isValid() {
			crafter = info.getCrafter();
			return (crafter == null && info.inAltar());
		}

		public int execute() {
			if(calc.distanceTo(info.PORTAL_TILE) > 5) {
				walk(info.PORTAL_TILE);
				return random(1000, 1200);
			}
			if(game.getCurrentWorld() != playerWorld) {
				changeWorld = true;
				env.disableRandoms();
				if(game.logout(true)) 
					return random(1000, 1200)*5;
			}
			
			return random(900, 1100)/4;
		}

		public String getStatus() {
			return "searching for \'" + playerTraded + "\'.";
		}
	}
	
	public class SendTrade implements Strategy {
		RSPlayer crafter = null;
		public boolean isValid() {
			if(trade.inTrade()) return false;
			
			crafter = info.getCrafter();
			return crafter != null && info.inAltar();
		}

		public int execute() {
			if (crafter.isOnScreen()) {
				if(crafter.getAnimation() == 791) requesting = -1;
				if(requesting > 0) { 
					requesting--;
					return random(350, 650);
				}
				trade.tradePlayer(crafter, 1000);
				requesting = 8;
			} else if (calc.distanceTo(crafter) < 5) {
				camera.turnTo(crafter);
			} else {
				walk(crafter.getLocation());
			}
			
			return random(900, 1100)/4;
		}

		public String getStatus() {
			return "found \'" + playerTraded + "\', sending trade.";
		}
	}
	
	public class ConfirmTrade implements Strategy {
		public boolean isValid() {
			return trade.inTrade();
		}

		public int execute() {
			// if the second trade screen is opened
			if(trade.inTradeSecond()) {
				// accept the trade
				trade.acceptTrade();
				
				// null out the last player traded
				if(type == MASTER) playerTraded = null;
				return random(900, 1100)/4;
			}
			
			// if the first trade screen is opened
			if(trade.inTradeMain()) {
				boolean itemOffered = trade.isWealthOffered();
				boolean itemRecived = trade.isWealthReceived();
				if(itemOffered) {
					if(!itemRecived) return random(900, 1100)/8;
					trade.acceptTrade();
				}
			}
			
			return random(900, 1100)/4;
		}

		public String getStatus() {
			return "accepting trade.";
		}
	}
	
	// failsafe to drop extra items (could be dangerous)
	public class DropJunk implements Strategy {
		public boolean isValid() {
			return (type == MASTER && inventory.getCount() > 2);
		}
		
		public int execute() {
			RSItem[] items = inventory.getItems();
			for (RSItem item : items) {
				if(item.getID() != info.getEssenceID() && item.getID() != craftRune) {
					if(!inventory.destroyItem(item.getID())) {
						item.interact("Drop");
					}
				}
			}
			
			return random(900, 1100)/4;
		}
		
		public String getStatus() {
			return "dropping junk items.";
		}
	}
	
	public class AcceptTrade implements Strategy {
		public boolean isValid() {
			if(trade.inTrade()) return false;
			return playerTraded != null;
		}
		
		public int execute() {
			if(playerTraded != null) {
				if(trade.tradePlayer(playerTraded, 1000)) {
					requesting = 0;
					return random(900, 1100)*2;
				}
				
				if(requesting >= 2) {
					requesting = 0;
					playerTraded = null;
				}
				requesting++;
			}
			
			return random(900, 1100)/4;
		}
		
		public String getStatus() {
			return "accepting trade from " + playerTraded;
		}
	}
	
	public class OfferRunes implements Strategy {
		public boolean isValid() {
			// have i offered items?
			if(!trade.inTradeMain()) return false;
			return !trade.isWealthOffered();
		}
		
		public int execute() {
			// open offer item trade interface
			if(type == MASTER) {
				RSItem item = inventory.getItem(note);
				if(item.interact("Offer-X")) {
					sleep(random(1000, 1300));
					keyboard.sendText(String.valueOf(numberToCraft), true);
				}
			} else if(type == RUNNER) {
				trade.offer(info.getEssenceID(), 0);
			}
			return random(900, 1100);
		}
		
		public String getStatus() {
			return "offering items";
		}
	}
	
	public abstract class Info {
		protected RSTile[] PATH_AREA_TILES = null;
		
		protected RSTile BANK_TILE = null;
		protected RSTile ALTAR_TILE = null;
		protected RSTile PORTAL_TILE = null;
		protected RSTile STAIR_TILE = null;
		
		protected RSArea ALTAR_AREA = null;
		protected RSArea BANK_AREA = null;
		
		protected int MUSICIAN_ID = 0;
	}
	
	
	public class AirInfo extends Info {
		public String toString() {
			return "Air";
		}
		
		public AirInfo() { 
			this.PATH_AREA_TILES = new RSTile[]{ 
				new RSTile(3189, 3440),
				new RSTile(3182, 3440), 
				new RSTile(3181, 3431),
				new RSTile(3171, 3431),
				new RSTile(3162, 3424),
				new RSTile(3148, 3422),
				new RSTile(3144, 3414),
				new RSTile(3124, 3406),
				new RSTile(3130, 3400),
				new RSTile(3143, 3410),
				new RSTile(3159, 3419),
				new RSTile(3173, 3425), 
				new RSTile(3184, 3428)
			};
			this.BANK_TILE = new RSTile(3182, 3436);
			this.ALTAR_TILE = new RSTile(3128, 3406);
			this.PORTAL_TILE = new RSTile(2841, 4828);
			this.STAIR_TILE = new RSTile(3256, 3421);
			
			this.ALTAR_AREA = new RSArea(new RSTile(3124, 3400),
					new RSTile(3134, 3408));
			this.BANK_AREA = new RSArea(new RSTile(3182, 3433),
					new RSTile(3189, 3446));
					
			this.MUSICIAN_ID = 8699;	
		}
	}	

	public class BodyInfo extends Info {
		public String toString() {
			return "Body";
		}
		
		public BodyInfo() { 
			this.PATH_AREA_TILES = new RSTile[]{ 
				new RSTile(3085, 3492),
				new RSTile(3079, 3483), 
				new RSTile(3079, 3467),
				new RSTile(3082, 3464),
				new RSTile(3086, 3464),
				new RSTile(3086, 3462),
				new RSTile(3071, 3462),
				new RSTile(3071, 3446),
				new RSTile(3053, 3448),
				new RSTile(3053, 3443),
				new RSTile(3072, 3445),
				new RSTile(3075, 3459), 
				new RSTile(3082, 3461),
				new RSTile(3088, 3462),
				new RSTile(3088, 3464),
				new RSTile(3080, 3467),
				new RSTile(3081, 3474),
				new RSTile(3082, 3483),
				new RSTile(3094, 3488),
				new RSTile(3094, 3493)
			};
			this.BANK_TILE = new RSTile(3095, 3491);
			this.ALTAR_TILE = new RSTile(3054, 3444);
			this.PORTAL_TILE = new RSTile(2521, 4833);
			this.STAIR_TILE = new RSTile(3256, 3421);
			
			this.ALTAR_AREA = new RSArea(new RSTile(3049, 3448),
					new RSTile(3057, 3442));
			this.BANK_AREA = new RSArea(new RSTile(3090, 4500),
					new RSTile(3098, 3488));
					
			this.MUSICIAN_ID = 8699;	
		}
	}

	public class EarthInfo extends Info {
		public String toString() {
			return "Earth";
		}
		
		public EarthInfo() { 
			this.PATH_AREA_TILES = new RSTile[]{
				new RSTile(3251, 3421),
				new RSTile(3253, 3429), 
				new RSTile(3271, 3430), 
				new RSTile(3284, 3434), 
				new RSTile(3284, 3451), 
				new RSTile(3288, 3458), 
				new RSTile(3296, 3469), 
				new RSTile(3303, 3474), 
				new RSTile(3300, 3464), 
				new RSTile(3295, 3457), 
				new RSTile(3291, 3449), 
				new RSTile(3288, 3432), 
				new RSTile(3282, 3427), 
				new RSTile(3265, 3427), 
				new RSTile(3255, 3420)
			};
			this.BANK_TILE = new RSTile(3253, 3420);
			this.ALTAR_TILE = new RSTile(3305, 3474);
			this.PORTAL_TILE = new RSTile(2655, 4829);
			//this.ALTAR = new RSTile(3305, 3473);
			this.STAIR_TILE = new RSTile(3256, 3421);
			
			this.ALTAR_AREA = new RSArea(new RSTile(3297, 3470),
					new RSTile(3302, 3469));
			this.BANK_AREA = new RSArea(new RSTile(3249, 3423),
					new RSTile(3257, 3418));	
						
			this.MUSICIAN_ID = 8700;	
		}
	}
	
	public class FireInfo extends Info {
		public String toString() {
			return "Fire";
		}
		
		public FireInfo() { 
			this.PATH_AREA_TILES = new RSTile[]{ 
				new RSTile(3315, 3254),
				new RSTile(3306, 3240), 
				new RSTile(3300, 3226),
				new RSTile(3294, 3212),
				new RSTile(3162, 3424),
				new RSTile(3288, 3208),
				new RSTile(3284, 3192),
				new RSTile(3280, 3180),
				new RSTile(3271, 3164),
				new RSTile(3271, 3168),
				new RSTile(3278, 3181),
				new RSTile(3281, 3194), 
				new RSTile(3285, 3208),
				new RSTile(3295, 3326),
				new RSTile(3298, 3243),
				new RSTile(3310, 3255)
			};
			this.BANK_TILE = new RSTile(3268, 3168);
			this.ALTAR_TILE = new RSTile(3313, 3254);
			this.PORTAL_TILE = new RSTile(2576, 4846);
			this.STAIR_TILE = new RSTile(3256, 3421);
			
			this.ALTAR_AREA = new RSArea(new RSTile(3310, 3257),
					new RSTile(3316, 3252));
			this.BANK_AREA = new RSArea(new RSTile(3269, 3173),
					new RSTile(3272, 3161));
					
			this.MUSICIAN_ID = 8707;	
		}
	}	
	
	public class MindInfo extends Info {
		public String toString() {
			return "Mind";
		}
		
		public MindInfo() { 
			this.PATH_AREA_TILES = new RSTile[]{ 
				new RSTile(2985, 3514),
				new RSTile(2985, 3494), 
				new RSTile(2978, 3480),
				new RSTile(2975, 3466),
				new RSTile(2983, 3450),
				new RSTile(2989, 3436),
				new RSTile(2989, 3421),
				new RSTile(2973, 3412),
				new RSTile(2969, 3411),
				new RSTile(2965, 3404),
				new RSTile(2964, 3384),
				new RSTile(2954, 3397), 
				new RSTile(2947, 3371),
				new RSTile(2943, 3371),
				new RSTile(2949, 3378),
				new RSTile(2961, 3385),
				new RSTile(2963, 3391),
				new RSTile(2963, 3406),
				new RSTile(2966, 3414),
				new RSTile(2986, 3421),
				new RSTile(2985, 3430),
				new RSTile(2976, 3446),
				new RSTile(2972, 3460),
				new RSTile(2972, 3485),
				new RSTile(2979, 3514)
			};
			this.BANK_TILE = new RSTile(2946, 3367);
			this.ALTAR_TILE = new RSTile(2983,3513);
			this.PORTAL_TILE = new RSTile(2793, 4828);
			this.STAIR_TILE = new RSTile(3256, 3421);
			
			this.ALTAR_AREA = new RSArea(new RSTile(2979, 3516),
					new RSTile(2985, 351));
			this.BANK_AREA = new RSArea(new RSTile(2943, 3373),
					new RSTile(2949, 3367));
					
			this.MUSICIAN_ID = 5442;	
		}
	}
	
	public class WaterInfo extends Info {
		boolean draynor = false;
		
		public String toString() {
			return "Water" + (draynor?" (dray)":" (alkarhid)");
		}

		// boolean true for banking at draynor, otherwise bank at alkarhid
		public WaterInfo(boolean draynor) {
			this.draynor = draynor; 
			
			// bank at draynor or alkarhid
			if(draynor) {
				this.PATH_AREA_TILES = new RSTile[]{
					new RSTile(3089, 3250),
					new RSTile(3103, 3250),
					new RSTile(3109, 3253),
					new RSTile(3109, 3232),
					new RSTile(3121, 3220),
					new RSTile(3138, 3215),
					new RSTile(3152, 3205),
					new RSTile(3160, 3191),
					new RSTile(3168, 3178),
					new RSTile(3182, 3174),
					new RSTile(3188, 3167),
					new RSTile(3179, 3162),
					new RSTile(3163, 3170),
					new RSTile(3149, 3182),
					new RSTile(3149, 3203),
					new RSTile(3131, 3211),
					new RSTile(3112, 3218),
					new RSTile(3101, 3233),
					//new RSTile(3104, 3248),
					new RSTile(3096, 3247),
					new RSTile(3089, 3240)
				};
				this.BANK_TILE = new RSTile(3091, 3244);
				this.BANK_AREA = new RSArea(new RSTile(3092, 3246),
					new RSTile(3097, 3240));
			} else {
				this.PATH_AREA_TILES = new RSTile[]{
					new RSTile(3275, 3166),
					new RSTile(3275, 3176),
					new RSTile(3262, 3175),
					new RSTile(3249, 3174),
					new RSTile(3242, 3169),
					new RSTile(3209, 3162),
					new RSTile(3193, 3162),
					new RSTile(3182, 3167),
					new RSTile(3193, 3160),
					new RSTile(3208, 3160),
					new RSTile(3225, 3160),
					new RSTile(3242, 3168),
					new RSTile(3250, 3168),
					new RSTile(3262, 3173)
				};
				this.BANK_TILE = new RSTile(3268, 3168);
				this.BANK_AREA = new RSArea(new RSTile(3269, 3173),
					new RSTile(3272, 3161));
			}
			this.ALTAR_TILE = new RSTile(3184, 3164);
			this.PORTAL_TILE = new RSTile(3495, 4832);
			this.STAIR_TILE = new RSTile(3256, 3421);
			
			this.ALTAR_AREA = new RSArea(new RSTile(3182, 3168),
					new RSTile(3188, 3162));
				
			this.MUSICIAN_ID = 30;	
		}
	}
	
	private class InfoDatabase {
		Info data = location;
		
		private final RSTile[] PATH_AREA_TILES = data.PATH_AREA_TILES;
		private final RSTile BANK_TILE = data.BANK_TILE;
		private final RSTile ALTAR_TILE = data.ALTAR_TILE;
		private final RSTile PORTAL_TILE = data.PORTAL_TILE;
		private final RSArea BANK_AREA = data.BANK_AREA;
		private final RSTile STAIR_TILE = data.STAIR_TILE;
		private final int MUSICIAN_ID = data.MUSICIAN_ID;	
		
		private final RSArea PATH_AREA = new RSArea(PATH_AREA_TILES);		
		private final int RESTING_ANIMS[] ={ 5713, 5748, 2033, 2716, 12108, 11786 };
		private final int ESSENCE_ID = 1436;
		private Filter<RSPlayer> CRAFTER_FILTER = new Filter<RSPlayer>() {
			public boolean accept(RSPlayer t) {
				if (t != null) {
					if (t.getName() != null) {
						return t.getName().replace('\u00A0', ' ')
								.equalsIgnoreCase(playerTraded.replace('\u00A0', ' '));
					}
				}
				return false;
			}
		};

		private RSArea getPathArea() {
			return this.PATH_AREA;
		}

		private boolean inAltar() {
			return calc.distanceTo(PORTAL_TILE) < 40;
		}
		
		//private boolean atAltar() {
		//	return this.ALTAR_AREA.contains(getMyPlayer().getLocation());
		//}

		private boolean atBank() {
			return BANK_AREA.contains(getMyPlayer().getLocation());
		}

		private int[] getRestingAnim() {
			return this.RESTING_ANIMS;
		}

		private int getMusicianID() {
			return this.MUSICIAN_ID;
		}

		private int getEssenceID() {
			//return this.PURE_ESSENCE_ID;
			return this.ESSENCE_ID;
		}

		private RSPlayer getCrafter() {
			RSPlayer player = players.getNearest(CRAFTER_FILTER);
			return player;
		}
	}
	
	@Override public void mousePressed(MouseEvent e) {}
	@Override public void mouseReleased(MouseEvent e) {}
	@Override public void mouseEntered(MouseEvent e) {}
	@Override public void mouseExited(MouseEvent e) {}
	@Override public void keyPressed(KeyEvent e) {}
	@Override public void keyReleased(KeyEvent e) {}
}