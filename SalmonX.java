import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import org.rsbot.event.listeners.PaintListener;
import org.rsbot.script.Script;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.util.Timer;
import org.rsbot.script.wrappers.RSGroundItem;
import org.rsbot.script.wrappers.RSTile;
import org.rsbot.script.wrappers.RSWeb;

@ScriptManifest(authors = { "Swipe" }, keywords = { "Money" }, name = "SalmonX", description = "Picks salmon at edge and banks.", version = 1.0)
public class SalmonX extends Script implements PaintListener, MouseListener,
		MouseMotionListener {
	// Salmon
	final int RAW_SALMON = 331;
	// Tile
	final RSTile FISH_TILE = new RSTile(3108, 3433);
	final RSTile FISH2_TILE = new RSTile(3102, 3425);
	final RSTile BANK_TILE = new RSTile(3094, 3492);
	int fishcount = 0;
	int fishworth = 0;
	long start;
	boolean ncamera = true;
	private String status;
	MasterPaint mp;
	int tempcount;
	private boolean moving;

	// \\
	boolean atFish() {
		return calc.distanceTo(FISH_TILE) < 6;
	}

	boolean atFish2() {
		return calc.distanceTo(FISH2_TILE) < 6;
	}

	boolean atBank() {
		return calc.distanceTo(BANK_TILE) < 10;
	}

	void walkToTile(RSTile t) {
		status = "Walking";
		RSWeb w = web.getWeb(t);
		while (calc.distanceTo(t) > 2) {
			w.step();
			sleep(2500);
		}

	}

	@Override
	public void onFinish() {
		log(Color.pink,
				"SalmonX by: Swipe | Ran for: "
						+ Timer.format(mp.start - System.currentTimeMillis()));
		log(Color.pink, "We gained " + fishworth * fishcount);
	}

	// Credits Tutorial post && Ministry
	@SuppressWarnings("deprecation")
	private void antiBan() {
		int b = random(0, 24);
		switch (b) {
		case 1:
			if (random(0, 24) == 5) {
				mouse.moveSlightly();
				sleep(200, 600);
				mouse.moveRandomly(150, 350);
			}
			break;
		case 2:
			if (random(0, 15) == 2) {
				camera.setAngle(random(30, 70));
				sleep(400, 1200);
			}
			break;
		case 4:
			if (random(0, 24) == 6) {
				mouse.moveOffScreen();
				sleep(1200, 2000);
			}
			break;
		case 3:
			if (random(0, 15) == 4) {
				mouse.moveRandomly(900);
				sleep(1400, 2400);
			}
			break;
		case 6:
			if (random(0, 19) == 5) {
				game.openTab(random(random(4, 7), random(3, 5)));
				mouse.sleep(1500);
				sleep(400, 500);
			}
			break;
		default:
			break;
		}
	}

	public boolean onStart() {
		fishworth = grandExchange.lookup(RAW_SALMON).getGuidePrice();
		mp = new MasterPaint(0, 0);
		return true;

	}

	boolean takeItem(RSGroundItem g) {
		if (!getMyPlayer().isMoving()) {
			status = "Getting Salmon";
			mouse.click(g.getPoint(), true);
			// fishcount++;
			tempcount = inventory.getCount(RAW_SALMON);
			return true;
		}
		return false;
	}

	RSTile Migrate() {
		if (atFish()) {
			return FISH2_TILE;
		}
		return FISH_TILE;
	}

	@Override
	public int loop() {
		if (atFish() || atFish2()) {
			status = "At Fish";
			if (ncamera) {
				camera.setCompass('w');
				camera.setPitch(false);
				ncamera = false;
			}
			if (inventory.isFull()) {
				status = "Banking";
				walkToTile(BANK_TILE);
			} else {
				if (groundItems.getAll(RAW_SALMON).length > 0) {
					RSGroundItem sal = groundItems.getNearest(RAW_SALMON);
					// make sure nobody else took it

					if (sal != null) {
						takeItem(sal);

						sleep(200);
					}
				} else {
					walkToTile(Migrate());
				}
				if (random(3, 403) > 350) {
					antiBan();
				}
			}
			// Drop trout
			if (inventory.contains(335))
				inventory.dropAll(335);
		}

		if (atBank()) {
			if (inventory.isFull()) {
				if (bank.isOpen()) {
					bank.depositAll();
					fishcount += tempcount;
					tempcount = 0;
					ncamera = true;
				} else {
					bank.open();
					sleep(150);
				}
			} else {
				walkToTile(FISH_TILE);
				status = "To Fish";
			}
		}
		if (!atBank() && atFish() && atFish2()) {
			status = "Wandered...fixing";
			walkToTile(FISH_TILE);
		}
		return 300;
	}

	@Override
	public void mouseDragged(MouseEvent m) {
		if (moving) {
			mp.xloc = m.getX();
			mp.yloc = m.getY();
		}
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent m) {
		if (mp.moveBox.contains(m.getPoint())) {
			moving = true;
		}
	}

	@Override
	public void mouseReleased(MouseEvent m) {
		if (moving) {
			mp.xloc = m.getX();
			mp.yloc = m.getY();
			moving = false;
		}
	}

	@Override
	public void onRepaint(Graphics arg0) {
		mp.drawPaint(arg0);
		arg0.drawString("A", (int) mouse.getLocation().getX(), (int) mouse
				.getLocation().getY() - 5);
		arg0.drawString("V", (int) mouse.getLocation().getX(), (int) mouse
				.getLocation().getY() + 5);

	}

	class MasterPaint {
		public MasterPaint(int x, int y) {
			xloc = x;
			yloc = y;
			start = System.currentTimeMillis();
		}

		long start;
		int xloc = 0;
		int yloc = 0;
		Rectangle moveBox;

		int dot = 0;
		int i = 0;
		String dots = "";

		String getDot(int d) {
			String tot = "";
			if (dot > 3) {
				dot = 0;
				dots = "";
			}
			for (int j = 0; j < d; j++)
				tot += ".";
			dots = tot;
			return dots;
		}

		public void drawPaint(Graphics g) {
			i++;
			moveBox = new Rectangle(xloc, yloc, 30, 30);
			g.setColor(new Color(30, 30, 30, 195));
			g.fill3DRect(xloc, yloc, 200, 100, true);
			g.setColor(new Color(150, 20, 10, 115));
			g.fillRect(moveBox.x, moveBox.y, moveBox.width, moveBox.height);
			g.setColor(Color.white);
			g.drawString(
					"Time Running: "
							+ Timer.format(System.currentTimeMillis() - start),
					xloc + 10, yloc + 10);
			g.drawString("Status: " + status + getDot(dot), xloc + 10,
					yloc + 25);
			g.drawString("Fish: " + (fishcount + tempcount), xloc + 10,
					yloc + 40);
			g.drawString("Profit: " + (fishcount + tempcount) * fishworth,
					xloc + 10, yloc + 55);
			g.drawString(
					(int) ((fishcount + tempcount) * 3600000D / (System
							.currentTimeMillis() - start)) + "Fish/h",
					xloc + 10, yloc + 70);
			g.drawString(
					(int) (((fishcount + tempcount) * fishworth) * 3600000D / (System
							.currentTimeMillis() - start)) + "Profit/h",
					xloc + 10, yloc + 85);
			if (i % 20 == 0) {
				dot++;
			}
		}
	}
}
