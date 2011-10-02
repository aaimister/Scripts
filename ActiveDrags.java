import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.util.LinkedList;

import javax.swing.JOptionPane;

import org.rsbot.event.events.MessageEvent;
import org.rsbot.event.listeners.MessageListener;
import org.rsbot.event.listeners.PaintListener;
import org.rsbot.script.Script;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.wrappers.RSNPC;
import org.rsbot.script.wrappers.RSTile;
import org.rsbot.script.wrappers.RSWeb;

@ScriptManifest(authors = { "Swipe" }, keywords = "Combat, loot", name = "ActiveDrags", version = 1.0, description = "Kills Drags EAST")
public class ActiveDrags extends Script implements PaintListener, MessageListener {
	 final static int HIDE = 1753;
	final static int FTAB = 8009;
	 final static int BONES = 536;
	 final static int D_WEED = 217;
	 final static RSTile E_BANKL = new RSTile(3186,3439);
	 final static RSTile E_DITCHL = new RSTile(3137,3520);
	 final static int E_DITCHO = 1440;
	 final static RSTile E_DRAGL = new RSTile(3336,3681);
	 final static RSTile VARROCK = new RSTile(3212,3425);
	 public final static int[] dragons = {4679,4680,941};
	 private static final Color MOUSE_COLOR = new Color(139, 69, 19),
	 MOUSE_BORDER_COLOR = new Color(0, 153, 0),
	 MOUSE_CENTER_COLOR = new Color(139, 69, 19);
	 String line[] = new String[5];
	 private boolean pressed = false;
	 long startTime;
	 Point p;
	 Point p2;
	 int paintState=0;//0,1,2
	int thideCount=0;
	 int hideCount=0;
	int tboneCount=0;
	 int boneCount=0;
	 String status="";
	 private final LinkedList<MousePathPoint> mousePath = new LinkedList<MousePathPoint>();
	static enum State {E_DITCH,W_DITCH,FIGHT,TO_WDRAGS,TO_EDRAGS,BANK,TO_WBANK, TO_EBANK, TO_WDITCH,TO_EDITCH,TELE,EAT,WAIT,LOOT}

	private int food=333;
	private int amount=20;
	public boolean onStart(){
		String s = (String)JOptionPane.showInputDialog(null,"Food id, amount. ex (333,10)");
		food=Integer.parseInt(s.split(",")[0]);
		amount =Integer.parseInt(s.split(",")[1]);
		startTime=System.currentTimeMillis();
		return true;
	}
	public State getState(){
		if(players.getMyPlayer().isInCombat()){
			return State.WAIT;
		}
		if(nearArea(E_DRAGL,15)){
			if(groundItems.getNearest(HIDE,BONES,D_WEED)!=null){
				return State.LOOT;
			}
			return State.FIGHT;
		}
		if(nearArea(VARROCK,8)){
			return State.TO_EBANK;
		}
		if(nearArea(E_BANKL,10)){
			if(inventory.contains(BONES)){
			return State.BANK;
			} else{
				return State.TO_EDITCH;
			}
		} 
		if(inventory.getCount(food)<1 || (inventory.getCount(HIDE)+ inventory.getCount(BONES)) >26){
			return State.TELE;
		}
		if(nearArea(E_DITCHL,5)){
			if(getMyPlayer().getLocation().getY()< E_DITCHL.getY()){
				return State.E_DITCH;
			} else{
				return State.TO_EDRAGS;
			}
		}
		return State.WAIT;
	}
	public int getHides(){
		int temp=thideCount;
		thideCount=inventory.getCount(HIDE);
		if(thideCount-temp==0){
			return hideCount;
		} else{
			hideCount+=Math.abs(thideCount-temp);
		}
		return hideCount;
	}
	public int getBones(){
		int temp=tboneCount;
		tboneCount=inventory.getCount(HIDE);
		if(tboneCount-temp==0){
			return boneCount;
		} else{
			boneCount+=Math.abs(tboneCount-temp);
		}
		return boneCount;
	}
	public int PerHour(int i){
		return (int) Math.ceil(i * 3600000D / startTime-System.currentTimeMillis());
	}
	@Override
	public void messageReceived(MessageEvent arg0) {
		// TODO Auto-generated method stub

	}
	
	private int WalkAttempts = 0;

	private void walkTo(final RSTile tile) {
		while(calc.distanceTo(tile)>6){
			RSWeb temp = web.getWeb(getMyPlayer().getLocation(),
					tile.randomize(1, 1));
			temp.step();
		sleep(random(300, 1000));
		if (!getMyPlayer().isMoving()) {
			WalkAttempts++;
		} else {
			WalkAttempts = 0;
		}
		if (WalkAttempts > random(2,5)) {
			log("Having problems walking...");
			WalkAttempts = 0;
		}
		}
	}
private boolean nearArea(RSTile t, int n){
	return calc.distanceTo(t)<=n;
}
private final RenderingHints rh = new RenderingHints(
		RenderingHints.KEY_TEXT_ANTIALIASING,
		RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

	@Override
	public void onRepaint(Graphics g) {
		drawMouse(g);

		((Graphics2D)g).setRenderingHints(rh);
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
		g.drawString(""+line[0], 236, 339);
		g.setColor(new Color(255, 255, 255));
		g.drawString(""+line[0], 233, 336);
		g.setFont(new Font("Arial Black", 0, 11));
		g.setColor(new Color(0, 0, 0, 100));
		g.drawString(""+line[1], 235, 361);
		g.setColor(new Color(255, 255, 255));
		g.drawString(""+line[1], 232, 358);
		g.setFont(new Font("Arial Black", 0, 11));
		g.setColor(new Color(0, 0, 0, 100));
		g.drawString(""+line[2], 235, 383);
		g.setColor(new Color(255, 255, 255));
		g.drawString(""+line[2], 232, 380);
		g.setFont(new Font("Arial Black", 0, 11));
		g.setColor(new Color(0, 0, 0, 100));
		g.drawString(""+line[3], 236, 404);
		g.setColor(new Color(255, 255, 255));
		g.drawString(""+line[3], 233, 401);
		g.setFont(new Font("Arial Black", 0, 11));
		g.setColor(new Color(0, 0, 0, 100));
		g.drawString(""+line[4], 237, 423);
		g.setColor(new Color(255, 255, 255));
		g.drawString(""+line[4], 234, 420);
		g.setFont(new Font("Arial Black", 0, 11));
		g.setColor(new Color(0, 0, 0, 100));
		g.drawString("Status: "+status, 307, 312);
		g.setColor(new Color(255, 255, 255));
		g.drawString("Status: "+status, 304, 309);
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
	@Override
	public int loop() {
		status= getState().toString().toLowerCase();
		switch(paintState){
		case 0:
			line[0]= "ActiveDrags - Profit";
			line[1]="Hides: "+ getHides()+ " - "+ (grandExchange.lookup(HIDE).getGuidePrice()* getHides());
			line[2]="Hides/h: "+ PerHour(getHides())+ " - "+ (grandExchange.lookup(HIDE).getGuidePrice()* PerHour(getHides()));
			line[3]="Bones: "+ getBones() + " - "+ (grandExchange.lookup(BONES).getGuidePrice()* getBones());
			line[4]="Bones/h: "+ PerHour(getBones()) + " - "+ (grandExchange.lookup(BONES).getGuidePrice()* PerHour(getBones()));
			break;
		case 1:
			break;
		case 2:
			break;
		}
		
		switch(getState()){
		case BANK:
			if(bank.isOpen()){
				bank.depositAll();
				bank.withdraw(food, amount);
				sleep(500);
				bank.withdraw(8007, 1);
				bank.close();
			}else{
				bank.open();
			}
		case TO_EBANK:
			walkTo(E_BANKL);
			break;
		case TO_EDITCH:
			walkTo(E_DITCHL);
			break;
		case E_DITCH:
			RSTile l = getMyPlayer().getLocation();
			objects.getNearest(E_DITCHO).doClick();
			sleep(random(700,2000));
			if(getMyPlayer().getLocation().equals(l)){
				log("ditch error! fixing!");
				objects.getNearest(E_DITCHO).doClick();
			}
			break;
		case TO_EDRAGS:
			walkTo(E_DRAGL);
			break;
		case FIGHT:
			attackNearestDragon();
			break;
		case LOOT:
			groundItems.getNearest(HIDE,BONES,D_WEED).interact("take");
			inventory.dropItem(995);
			sleep(random(500,1000));
			break;
		
		case TELE:
			while(combat.getWildernessLevel()>20){
				walking.walkTo(new RSTile(getMyPlayer().getLocation().getX(),getMyPlayer().getLocation().getY()-15));
			}
		inventory.getItem(8007).interact("break");
		sleep(4000);
			break;
		case WAIT:
			if (players.getMyPlayer().getHPPercent() < 41) {
				inventory.getItem(food).interact("Eat");
			}
			if(random(1,30)==3){
				camera.setAngle(camera.getAngle()+random(3,90));
			}
			break;
		}
		return random(100,300);
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
