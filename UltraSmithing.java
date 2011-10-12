/**
 * @author BlackWood/Universal
 * @copyright (C) 2011 - BlackWood/Universal
 * 			No one except BlackWood/Universal has the right to modify this script, or use methods that I have specified!
 */

import java.awt.*;
import java.awt.event.*;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JTextField;

import org.rsbot.event.events.MessageEvent;
import org.rsbot.event.listeners.*;
import org.rsbot.script.*;
import org.rsbot.script.methods.Game;
import org.rsbot.script.methods.Skills;
import org.rsbot.script.wrappers.RSComponent;
import org.rsbot.script.wrappers.RSInterface;
import org.rsbot.script.wrappers.RSPath;
import org.rsbot.script.wrappers.RSTile;

import java.io.IOException;
import java.net.URL;

@ScriptManifest(authors = "BlackWood", name = "Ultra Smithing", version = 1.6, description = "Smithing Done Right!")
public class UltraSmithing extends Script implements PaintListener, MessageListener, MouseListener {
	
	Script Script = this;
	String Skill = "Smithing";
	
	SmithingGUI SmithingGUI = new SmithingGUI();
	
	boolean startedScript = false;
	
	long curTime = System.currentTimeMillis();

	boolean isPaintShowing = true;
	boolean canLoop = true;
	
	int StartEXP = 0;
	int EXPGained = 0;
	int EXPMake = 0;
	int Interactions = 0;
	
	long Duration;
	String Role;
	
	boolean singleOred = false;
	boolean doubleOred = false;
	
	int Tin = 438;
    int Copper = 436;
    int Iron = 440;
    int Silver = 442;
    int Coal = 453;
    int Gold = 444;
    int Mith = 447;
    int Addy = 449;
    int Rune = 451;
    
    int BronzeBar = 2349;
	int IronBar = 2351;
	int SilverBar = 2355;
	int SteelBar = 2353;
	int GoldBar = 2357;
	int MithBar = 2359;
	int AddyBar = 2361;
	int RuneBar = 2363;
	
	int Ore1 = -1;
	int Ore2 = -1;
	int Bar = -1;
	
	int Hammer = 2347;
	int AnvilID = 2783;
	int[] FurnaceID = {11666, 26814};
	
	String BuildString = null;	
	int SmithWidget = 300;
	
	Methods Method = null;
	SmeltAreas SmeltArea = null;
	BuildAreas BuildArea = null;
	
	RSTile BankTile = null;
	RSTile WalkTile = null;
	
	int BarsNeeded = 0;
	
	int CBall = 2;
	int CBMould = 4;
	
	int GoldenHammer = 20084;
	
	boolean hasChecked = false;
	boolean usingGoldenHammer = false;
	
	enum SmeltAreas {
		EDGEVILLE, ALKHARID
	}
	
	enum BuildAreas {
		VARROCK
	}
	
	enum Methods {
		SMELT, BUILD, CBALLS
	}
	
	long timeFromMark(long fromMark) {
		return System.currentTimeMillis() - fromMark;
	}

	boolean waitFor(boolean bool, int timeout) {
		long t = System.currentTimeMillis();
		while (timeFromMark(t) < timeout) {
			if (bool == true) {
				return true;
			}
			sleep(10);
		}
		return false;
	}
	
	boolean useAnvil() {
		return (getMyPlayer().isIdle() && (System.currentTimeMillis() - this.curTime > 4000L) && inventory.contains(Bar) && inventory.useItem(inventory.getItem(Bar), objects.getNearest(AnvilID)));
	}
	
	boolean useSingleFurnace() {
		return (getMyPlayer().isIdle() && (System.currentTimeMillis() - this.curTime > 4000L) && inventory.contains(Ore1) && inventory.useItem(inventory.getItem(Ore1), objects.getNearest(FurnaceID)));
	}
	
	boolean useDoubleFurnace() {
		return (getMyPlayer().isIdle() && (System.currentTimeMillis() - this.curTime > 4000L) && inventory.contains(Ore1) && inventory.contains(Ore2) && inventory.useItem(inventory.getItem(Ore1), objects.getNearest(FurnaceID)));
	}
	
	boolean useCBallFurnace() {
		return (getMyPlayer().isIdle() && (System.currentTimeMillis() - this.curTime > 4000L) && inventory.contains(SteelBar) && inventory.useItem(inventory.getItem(SteelBar), objects.getNearest(FurnaceID)));
	}
	
	// You may NOT use this method.
	boolean clickWidget() {
		RSInterface Parent = interfaces.get(300);
		for (RSComponent c : Parent.getComponents()) {
			String Compare = c.getText().replaceAll("<col=ffffff>", "");
			String Bars = Parent.getComponent(c.getIndex() + 1).getText().replaceAll("<col=00ff00>", "").replaceAll("Bar", "").replaceAll("s", "").replaceAll(" ", "");
			if (Compare.toLowerCase().equals(new String(BuildString).toLowerCase()) && c != null && Parent != null) {
				BarsNeeded = Integer.parseInt(Bars);
				c.doClick(false);
				waitFor(menu.isOpen(), 2000);
				if (menu.isOpen()) {
					if (menu.click("Make All")) {
						waitFor(inventory.getCountExcept(Bar) > 0, 2000);
					}
					sleep(1200, 1500);
				}
				return inventory.getCountExcept(Bar) > 0;
			}
		}
		return false;
	}
	
	boolean walkTo(RSTile t) {
		RSPath p = walking.getPath(t);
		while (calc.distanceTo(t) > 3) {
			if (calc.distanceTo(t) < 4) {
				return true;
			}
			if (p.traverse()) {
				waitFor(getMyPlayer().isMoving(), 2500);
			}
		}
		return false;
	}
	
	boolean withdraw(int ID, int Count) {
		if (!bank.isOpen()) {
			return false;
		}
		for (int i = 0; i <= 10; i++) {
			if (bank.getItem(ID) != null) {
				if (bank.withdraw(ID, Count)) {
					waitFor(inventory.getItem(ID) != null, 2000);
					break;
				}
			return true;
			} else {
				if (i >= 10) {
					log.severe("Out of supplies...");
					FinishScript();
					return false;
				}
			}
		}
		return false;
	}
	
	boolean invHasBoth(int ID1, int ID2) {
		return (inventory.contains(ID1) && inventory.contains(ID2));
	}
	
	void singleOreLoop() {
		if (!bank.isOpen()) {
			if (getMyPlayer().getAnimation() != -1) {
				this.curTime = System.currentTimeMillis();
				if (EXPMake == 0) {
					EXPMake = EXPGained;
				}
			}
			if (inventory.contains(Ore1) && calc.distanceTo(WalkTile) > 3) {
				walkTo(WalkTile);
			}
			if (inventory.contains(Ore1) && calc.distanceTo(WalkTile) < 4 && !interfaces.get(905).getComponent(14).isValid()) {
				if (getMyPlayer().isIdle() && (System.currentTimeMillis() - this.curTime > 4000L) && useSingleFurnace()) {
					waitFor(interfaces.get(905).isValid(), 2000);
				}
			}
			if (interfaces.get(905).getComponent(14).isValid()) {
				if (interfaces.get(905).getComponent(14).interact("Make All")) {
					waitFor(inventory.getItem(Bar) != null, 2500);
				}
			}
			if (!inventory.contains(Ore1)) {
				if (BankTile != null) {
					if (calc.distanceTo(BankTile) > 3) {
						if (!getMyPlayer().isMoving()) {
							if (walkTo(BankTile)) {
								waitFor(getMyPlayer().isMoving(), 2500);
							}
						}
					} else {
						if (bank.open()) {
							waitFor(bank.isOpen(), 4000);
						}
					}
				} else {
					if (bank.open()) {
						waitFor(bank.isOpen(), 4000);
					}
				}
			}
		}
		if (bank.isOpen()) {
			if (inventory.contains(Bar) && inventory.getCount() > 0) {
				if (bank.depositAllExcept(Ore1)) {
					waitFor(inventory.getCount() == 0, 1250);
				}
			}
			if (inventory.getCount() == 0) {
				if (withdraw(Ore1, 0)) {
					waitFor(inventory.getItem(Ore1) != null, 2000);
				}
			}
			if (inventory.contains(Ore1) && !inventory.contains(Bar)) {
				if (random(1, 2) == 1) {
					if (bank.close()) {
						waitFor(!bank.isOpen(), 2000);
					}
				} else {
					if (walkTo(WalkTile)) {
						waitFor(getMyPlayer().isMoving(), 2500);
					}
				}
			}
		}
	}
	
	void doubleOreLoop(int Ore1Count) {
		if (!bank.isOpen()) {
			if (getMyPlayer().getAnimation() != -1) {
				this.curTime = System.currentTimeMillis();
				if (EXPMake == 0) {
					EXPMake = EXPGained;
				}
			}
			if (inventory.contains(Ore1) && inventory.contains(Ore2) && calc.distanceTo(WalkTile) > 3) {
				walkTo(WalkTile);
			}
			if (inventory.contains(Ore1) && inventory.contains(Ore2) && calc.distanceTo(WalkTile) < 4 && !interfaces.get(905).getComponent(14).isValid()) {
				if (getMyPlayer().isIdle() && (System.currentTimeMillis() - this.curTime > 4000L) && useDoubleFurnace()) {
					waitFor(interfaces.get(905).isValid(), 2000);
				}
			}
			if (Bar != SteelBar) {
				if (interfaces.get(905).getComponent(14).isValid()) {
					if (interfaces.get(905).getComponent(14).interact("Make All")) {
						waitFor(inventory.getItem(Bar) != null, 2500);
					}
				}
			} else {
				if (interfaces.get(905).getComponent(15).isValid()) {
					if (interfaces.get(905).getComponent(15).interact("Make All")) {
						waitFor(inventory.getItem(Bar) != null, 2500);
					}
				}
			}
			if (!invHasBoth(Ore1, Ore2)) {
				if (BankTile != null) {
					if (calc.distanceTo(BankTile) > 3) {
						if (!getMyPlayer().isMoving()) {
							if (walkTo(BankTile)) {
								waitFor(getMyPlayer().isMoving(), 2500);
							}
						}
					} else {
						if (bank.open()) {
							waitFor(bank.isOpen(), 4000);
						}
					}
				} else {
					if (bank.open()) {
						waitFor(bank.isOpen(), 4000);
					}
				}
			} 
		}
		if (bank.isOpen()) {
			if (inventory.contains(Bar) && inventory.getCount() > 0) {
				if (bank.depositAll()) {
					waitFor(inventory.getCount() == 0, 1250);
				}
			}
			if (inventory.getCount() == 0) {
				if (withdraw(Ore1, Ore1Count)) {
					waitFor(inventory.getItem(Ore1) != null, 2000);
				}
			}
			if (inventory.getCount(Ore1) > Ore1Count) {
				if (bank.depositAll()) {
					waitFor(inventory.getCount() == 0, 2000);
				}
			}
			if (inventory.contains(Ore1) && inventory.getCount(Ore1) <= Ore1Count && !inventory.contains(Ore2)) {
				if (withdraw(Ore2, 0)) {
					waitFor(inventory.getItem(Ore2) != null, 2000);
				}
			}
			if (inventory.contains(Ore1) && inventory.getCount(Ore1) <= Ore1Count && inventory.contains(Ore2) && !inventory.contains(Bar)) {
				if (random(1, 2) == 1) {
					if (bank.close()) {
						waitFor(!bank.isOpen(), 2000);
					}
				} else {
					if (walkTo(WalkTile)) {
						waitFor(getMyPlayer().isMoving(), 2500);
					}
				}
			}
		}
	}
	
	void cBallLoop() {
		if (!bank.isOpen()) {
			if (getMyPlayer().getAnimation() != -1) {
				this.curTime = System.currentTimeMillis();
				if (EXPMake == 0) {
					EXPMake = EXPGained;
				}
			}
			if (inventory.contains(SteelBar) && calc.distanceTo(WalkTile) > 3) {
				walkTo(WalkTile);
			}
			if (inventory.contains(SteelBar) && calc.distanceTo(WalkTile) < 4 && !interfaces.get(905).getComponent(14).isValid()) {
				if (getMyPlayer().isIdle() && (System.currentTimeMillis() - this.curTime > 4000L) && useCBallFurnace()) {
					waitFor(interfaces.get(905).isValid(), 2000);
				}
			}
			if (interfaces.get(905).getComponent(14).isValid()) {
				if (interfaces.get(905).getComponent(14).interact("Make All")) {
					waitFor(inventory.getItem(CBall) != null, 2500);
				}
			}
			if (!inventory.contains(SteelBar)) {
				if (BankTile != null) {
					if (calc.distanceTo(BankTile) > 3) {
						if (!getMyPlayer().isMoving()) {
							if (walkTo(BankTile)) {
								waitFor(getMyPlayer().isMoving(), 2500);
							}
						}
					} else {
						if (bank.open()) {
							waitFor(bank.isOpen(), 4000);
						}
					}
				} else {
					if (bank.open()) {
						waitFor(bank.isOpen(), 4000);
					}
				}
			}
		}
		if (bank.isOpen()) {
			if (inventory.contains(CBall) && inventory.getCount() > 0) {
				if (bank.depositAllExcept(CBMould, SteelBar)) {
					waitFor(inventory.getCountExcept(CBMould, SteelBar) == 0, 1250);
				}
			}
			if (inventory.getCountExcept(CBMould, SteelBar) == 0) {
				if (withdraw(SteelBar, 0)) {
					waitFor(inventory.getItem(SteelBar) != null, 2000);
				}
			}
			if (inventory.contains(SteelBar) && !inventory.contains(CBall)) {
				if (random(1, 2) == 1) {
					if (bank.close()) {
						waitFor(!bank.isOpen(), 2000);
					}
				} else {
					if (walkTo(WalkTile)) {
						waitFor(getMyPlayer().isMoving(), 2500);
					}
				}
			}
		}
	}
	
	@Override
	public boolean onStart() {
		if (!game.isLoggedIn()) {
    		log.severe("You must start this script while logged in!");
    		stopScript();
    		return !Script.isActive();
    	} else {
    		SmithingGUI.setVisible(true);
    		return SmithingGUI.isVisible();
    	}
    }
	
	public void FinishScript() {
		if (isPaintShowing == false) {
			isPaintShowing = true;
		}
		if (isPaintShowing == true) {
			env.saveScreenshot(true);
		}
		if (game.isLoggedIn()) {
			game.logout(false);
		}
		stopScript();
	}

	@SuppressWarnings("deprecation")
	void AntiBans() {
        switch (random(1, 10)) {
	    	case 5: //Turn Screen
	    		if (random(1, 10) == 10 && getMyPlayer().isMoving() && getMyPlayer().getAnimation() == -1 || random(1, 25) == 13 && getMyPlayer().getAnimation() != -1) {
	    			camera.setAngle(random(100, 359));
	    			sleep(500, 1500);
	    			break;
	    		}
	    	break;
	    	case 10: // Move mouse
	    		if (random(1, 10) == 5 && getMyPlayer().getAnimation() != -1) {
	    			mouse.moveRandomly(450);
	    			sleep(500, 1500);
	    			break;
	    		}
	    	break;
        }
        switch (random(1, 100000)) {
            case 58175: // Check EXP + Little break
            	if (!bank.isOpen()) {
            		log("Checking EXP And Taking A Small Break (Anti-Ban)");
            		canLoop = false;
					if (game.getCurrentTab() != Game.TAB_STATS) {
						game.openTab(Game.TAB_STATS, true);
						sleep(1000);
						skills.doHover(Skills.getIndex(Skill));
						sleep(6000, 7500);
					}
					canLoop = true;
					log("Finished Checking EXP And Taking A Small Break");
					break;
            	}
            break;
            case 81645: // Take a break
            	log("Taking a break (Anti-Ban)");
            	canLoop = false;
				if (random(1, 2) == 2) {
					mouse.moveOffScreen();
				}
				sleep(17500, 37500);
				canLoop = true;
				log("Break Finished");
			break;
			default:
				sleep(10);
			break;
        }
    }
	
	class AntiBanThread extends Thread {
		
		public void Start() {
			AntiBans();
		}
		
	}
	
	@Override
	public int loop() {
		try {
			mouse.setSpeed(random(4, 5));
			new AntiBanThread().Start();
			if (interfaces.canContinue()) {
				interfaces.clickContinue();
			}
			if (hasChecked == false && startedScript == true) {
				if (Method == Methods.BUILD) {
					if (equipment.containsOneOf(GoldenHammer)) {
						usingGoldenHammer = true;
						hasChecked = true;
					} else {
						if (inventory.contains(Hammer)) {
							hasChecked = true;
						} else {
							log.severe("You must start with a Golden Hammer equipped or a regular Hammer in your inventory.");
							stopScript();
						}
					}
				}
				if (Method == Methods.CBALLS) {
					if (inventory.contains(CBMould)) {
						hasChecked = true;
					} else {
						log.severe("You must start with a Cannon Ball Mould in your inventory.");
						stopScript();
					}
				}
				if (Method == Methods.SMELT) {
					hasChecked = true;
				}
			}
			if (game.isLoggedIn() && Script.isActive() && startedScript == true) {
			switch (Method) {
				case SMELT:
					if (singleOred == true) {
						singleOreLoop();
					}
					if (doubleOred == true) {
						if (Ore1 == Tin) {
							doubleOreLoop(14);
						}
						if (Ore1 == Iron) {
							doubleOreLoop(9);
						}
						if (Ore1 == Mith) {
							doubleOreLoop(5);
						}
						if (Ore1 == Addy) {
							doubleOreLoop(4);
						}
						if (Ore1 == Rune) {
							doubleOreLoop(3);
						}
					}
				break;
				case BUILD:
					if (!bank.isOpen()) {
						if (getMyPlayer().getAnimation() != -1) {
							this.curTime = System.currentTimeMillis();
							if (EXPMake == 0) {
								EXPMake = EXPGained;
							}
						}
						if (inventory.contains(Bar) && inventory.getCount(Bar) >= BarsNeeded && calc.distanceTo(WalkTile) > 3) {
							walkTo(WalkTile);
						}
						if (inventory.contains(Bar) && inventory.getCount(Bar) >= BarsNeeded && calc.distanceTo(WalkTile) < 4 && !interfaces.get(SmithWidget).isValid()) {
							if (getMyPlayer().isIdle() && (System.currentTimeMillis() - this.curTime > 4000L) && useAnvil()) {
								waitFor(interfaces.get(SmithWidget).isValid(), 2000);
							}
						}
						if (!getMyPlayer().isMoving() && (inventory.contains(Bar) && inventory.getCount(Bar) < BarsNeeded || !inventory.contains(Bar)) && inventory.getCountExcept(Bar, Hammer) > 0) {
							if (bank.open()) {
								waitFor(bank.isOpen() || getMyPlayer().isMoving(), 4000);
							}
						}
						if (interfaces.get(SmithWidget).isValid()) {
							if (clickWidget()) {
								sleep(3000, 5000);
							}
						}
					}
					if (bank.isOpen()) {
						if (inventory.getCountExcept(Bar, Hammer) > 0) {
							if (bank.depositAllExcept(Bar, Hammer)) {
								waitFor(inventory.getCountExcept(Bar, Hammer) < 1, 2500);
							}
						}
						if (inventory.getCountExcept(Bar, Hammer) < 1 && (!inventory.contains(Bar) || inventory.getCount(Bar) < BarsNeeded)) {
							if (withdraw(Bar, 0)) {
								waitFor(inventory.getItem(Bar) != null, 2000);
							}
						}
						if (inventory.getCountExcept(Bar, Hammer) < 1 && inventory.contains(Bar) && inventory.getCount(Bar) >= BarsNeeded) {
							if (random(1, 2) == 1) {
								if (bank.close()) {
									waitFor(!bank.isOpen(), 2000);
								}
							} else {
								if (walkTo(WalkTile)) {
									waitFor(getMyPlayer().isMoving(), 2500);
								}
							}
						}
					}
				break;
				case CBALLS:
					cBallLoop();
				break;
			}
		}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		if (x >= 488 && x < 488 + 16 && y >= 452 && y < 452 + 15) {
			isPaintShowing = !isPaintShowing;
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void messageReceived(MessageEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	String convertDurationToString(long t) {
		t /= 1000;
		StringBuilder s = new StringBuilder();
		s.append(':');
		if (t % 60 < 10)
			s.append('0');
		s.append(t % 60);
		t /= 60;
		s.insert(0, t % 60).insert(0, ':');
		if (t % 60 < 10)
			s.insert(1, '0');
		t /= 60;
		s.insert(0, t % 24);
		if (t % 24 < 10)
			s.insert(0, '0');
		return new String(s);
	}

	private String formatNumber(String str) {
		if (str.length() < 4) {
			return str;
		}
		return formatNumber(str.substring(0, str.length() - 3)) + ","
				+ str.substring(str.length() - 3, str.length());
	}

	private Image getImage(String url) {
		try {
			return ImageIO.read(new URL(url));
		} catch (IOException e) {
			return null;
		}
	}

	private final Color color1 = new Color(184, 155, 127);
	private final Color color2 = new Color(64, 34, 5);
	private final Color color3 = new Color(149, 109, 69);

	private final BasicStroke stroke1 = new BasicStroke(1);

	private final Font font1 = new Font("Arial", 1, 15);

	private final Image img1 = getImage("http://i842.photobucket.com/albums/zz348/monkey123502/ExitButton.png");
	private final Image img2 = getImage("http://dl.dropbox.com/u/22127840/Pictures/Open%20Button.png");

	public void onRepaint(Graphics g1) {
		Graphics2D g = (Graphics2D) g1;
		if (startedScript == true && game.isLoggedIn() && Script.isActive()) {
			if (isPaintShowing == true) {
				if (StartEXP == 0) {
					StartEXP = skills.getCurrentExp(Skills.getIndex(Skill));
				}
				if (EXPMake > 0) {
					Interactions = EXPGained / EXPMake;
				}
				EXPGained = skills.getCurrentExp(Skills.getIndex(Skill)) - StartEXP;
				g.setColor(color1);
				g.fillRect(7, 345, 506, 128);
				g.setColor(color2);
				g.setStroke(stroke1);
				g.drawRect(18, 351, 481, 33);
				g.setColor(color3);
				g.fillRect(21, 354, ((int) (skills.getPercentToNextLevel(Skills.getIndex(Skill)) * 4.75)), 28);
				g.setFont(font1);
				g.setColor(color2);
				g.drawString("Interactions: " + formatNumber(Interactions + "") + " | Interactions/H: " + formatNumber((int) ((Interactions) * 3600000D / (System.currentTimeMillis() - Duration)) + ""), 20, 407);
				g.drawString("EXP Gained: " + formatNumber(EXPGained + "") + " | EXP/H: " + formatNumber((int) ((EXPGained) * 3600000D / (System.currentTimeMillis() - Duration)) + ""), 20, 427);
				g.drawString("Role: " + Role, 20, 445);
				g.drawString("Duration: " + convertDurationToString(System.currentTimeMillis() - Duration), 20, 462);
				g.drawString(skills.getPercentToNextLevel(Skills.getIndex(Skill)) + "% " + "(" + formatNumber(skills.getExpToNextLevel(Skills.getIndex(Skill)) + "") + ") " + "To " + (skills.getCurrentLevel(Skills.getIndex(Skill)) + 1), 200, 375);
				g.drawImage(img1, 488, 452, null);
			}
			if (isPaintShowing == false) {
				g.drawImage(img2, 488, 452, null);
			}
		}
	}
	
	@SuppressWarnings("static-access")
	class SmithingGUI extends JFrame {
		private static final long serialVersionUID = 1L;
		public JFrame Frame = this;
		public Container FrameContentPane = Frame.getContentPane();
		public JComboBox CurrentBox = null;
		public JTextField CurrentInput = null;
		
		public void createSelect(String[] Options, ActionListener event, String ButtonText) {
			if (FrameContentPane.getComponents().length > 0) {
				FrameContentPane.removeAll();
				FrameContentPane.repaint();
			}
			FrameContentPane.setLayout(null);
			JComboBox OptionBox = new JComboBox(Options);
			OptionBox.setFocusable(false);
			OptionBox.setSelectedItem(Options[0]);
			CurrentBox = OptionBox;
			OptionBox.setBounds(new Rectangle(new Point(31, 5), OptionBox.getPreferredSize()));
			FrameContentPane.add(OptionBox);
			JButton Next = new JButton(ButtonText);
			Next.setFocusable(false);
			Next.setBounds(new Rectangle(new Point(32, 30), Next.getPreferredSize()));
			Next.addActionListener(event);
			FrameContentPane.add(Next);
			FrameContentPane.setPreferredSize(new Dimension(120, 60));
            Frame.pack();
            Frame.setLocationRelativeTo(Frame.getOwner());
		}
		
		public SmithingGUI() {
			Frame.setIconImage(getImage("http://dl.dropbox.com/u/23205805/universal.png"));
			final String[] Types = {"Smelting", "Building", "CBalls"};
			final String[] SmeltList = {"Bronze Bar", "Iron Bar", "Silver Bar", "Steel Bar", "Gold Bar", "Mith Bar", "Addy Bar", "Rune Bar"};
			final String[] ElementList = {"Bronze", "Iron", "Steel", "Mith", "Addy", "Rune"};
			final String[] BuildList = {"Dagger", "Hatchet", "Mace", "Medium helm", "Crossbow bolts", "Sword", "Nails", "Scimitar", "Spear", "Hasta", "Arrow tips", "Pickaxe", "Crossbow limbs", "Longsword", "Full helm", "Throwing knife", "Square shield", "Warhammer", "Battleaxe", "Chainbody", "Kiteshield", "Claws", "2-handed sword", "Platelegs", "Plateskirt", "Platebody"};
			final String[] SmeltPlaces = {"Edgeville", "Alkharid"};
			final String[] BuildPlaces = {"Varrock"};
			createSelect(Types, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (CurrentBox.getSelectedItem().equals(Types[0])) {
						Method = Methods.SMELT;
						Role = "Smeling ";
						createSelect(SmeltList, new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent arg0) {
								if (CurrentBox.getSelectedItem().equals(SmeltList[0])) {
									doubleOred = true;
									Ore1 = Tin;
									Ore2 = Copper;
									Bar = BronzeBar;
									Role = Role + "Bronze Bars";
								}
								if (CurrentBox.getSelectedItem().equals(SmeltList[1])) {
									singleOred = true;
									Ore1 = Iron;
									Bar = IronBar;
									Role = Role + "Iron Bars";
								}
								if (CurrentBox.getSelectedItem().equals(SmeltList[2])) {
									singleOred = true;
									Ore1 = Silver;
									Bar = SilverBar;
									Role = Role + "Silver Bars";
								}
								if (CurrentBox.getSelectedItem().equals(SmeltList[3])) {
									doubleOred = true;
									Ore1 = Iron;
									Ore2 = Coal;
									Bar = SteelBar;
									Role = Role + "Steel Bars";
								}
								if (CurrentBox.getSelectedItem().equals(SmeltList[4])) {
									singleOred = true;
									Ore1 = Gold;
									Bar = GoldBar;
									Role = Role + "Gold Bars";
								}
								if (CurrentBox.getSelectedItem().equals(SmeltList[5])) {
									doubleOred = true;
									Ore1 = Mith;
									Ore2 = Coal;
									Bar = MithBar;
									Role = Role + "Mith Bars";
								}
								if (CurrentBox.getSelectedItem().equals(SmeltList[6])) {
									doubleOred = true;
									Ore1 = Addy;
									Ore2 = Coal;
									Bar = AddyBar;
									Role = Role + "Addy Bars";
								}
								if (CurrentBox.getSelectedItem().equals(SmeltList[7])) {
									doubleOred = true;
									Ore1 = Rune;
									Ore2 = Coal;
									Bar = RuneBar;
									Role = Role + "Rune Bars";
								}
								createSelect(SmeltPlaces, new ActionListener() {
									@Override
									public void actionPerformed(ActionEvent e) {
										if (CurrentBox.getSelectedItem().equals(SmeltPlaces[0])) {
											SmeltArea = SmeltAreas.EDGEVILLE;
											WalkTile = new RSTile(3108, 3500);
											Role = Role + " @ Edgeville";
										}
										if (CurrentBox.getSelectedItem().equals(SmeltPlaces[1])) {
											SmeltArea = SmeltAreas.ALKHARID;
											WalkTile = new RSTile(3276, 3186);
											Role = Role + " @ Alkharid";
											BankTile = new RSTile(3269, 3169);
										}
										Duration = System.currentTimeMillis();
										startedScript = true;
										dispose();
									}									
								}, "Start");
								CurrentBox.setBounds(new Rectangle(new Point(27, 5), CurrentBox.getPreferredSize()));
							}							
						}, "Next");
						CurrentBox.setBounds(new Rectangle(new Point(21, 5), CurrentBox.getPreferredSize()));
					}
					if (CurrentBox.getSelectedItem().equals(Types[1])) {
						Method = Method.BUILD;
						createSelect(ElementList, new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								if (CurrentBox.getSelectedItem().equals(ElementList[0])) {
									Bar = BronzeBar;
									Role = "Making Bronze ";
								}
								if (CurrentBox.getSelectedItem().equals(ElementList[1])) {
									Bar = IronBar;
									Role = "Making Iron ";
								}
								if (CurrentBox.getSelectedItem().equals(ElementList[2])) {
									Bar = SteelBar;
									Role = "Making Steel ";
								}
								if (CurrentBox.getSelectedItem().equals(ElementList[3])) {
									Bar = MithBar;
									Role = "Making Mith ";
								}
								if (CurrentBox.getSelectedItem().equals(ElementList[4])) {
									Bar = AddyBar;
									Role = "Making Addy ";
								}
								if (CurrentBox.getSelectedItem().equals(ElementList[5])) {
									Bar = RuneBar;
									Role = "Making Rune ";
								}
								createSelect(BuildList, new ActionListener() {
									@Override
									public void actionPerformed(ActionEvent e) {
										BuildString = CurrentBox.getSelectedItem().toString();
										Role = Role + BuildString;
										if (BuildString != "Crossbow bolts" && BuildString != "Nails") {
											Role = Role + "s";
										}
										createSelect(BuildPlaces, new ActionListener() {
											@Override
											public void actionPerformed(ActionEvent e) {
												if (CurrentBox.getSelectedItem().equals(BuildPlaces[0])) {
													BuildArea = BuildAreas.VARROCK;
													WalkTile = new RSTile(3187, 3425);
													Role = Role + " @ Varrock";
												}
												Duration = System.currentTimeMillis();
												startedScript = true;
												dispose();
											}										
										}, "Start");
									}									
								}, "Next");
								CurrentBox.setBounds(new Rectangle(new Point(10, 5), CurrentBox.getPreferredSize()));
							}						
						}, "Next");
						CurrentBox.setBounds(new Rectangle(new Point(31, 5), CurrentBox.getPreferredSize()));
					}
					if (CurrentBox.getSelectedItem().equals(Types[2])) {
						Method = Methods.CBALLS;
						Role = "Making Cannon Balls";
						Bar = SteelBar;
						createSelect(SmeltPlaces, new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								if (CurrentBox.getSelectedItem().equals(SmeltPlaces[0])) {
									SmeltArea = SmeltAreas.EDGEVILLE;
									WalkTile = new RSTile(3108, 3500);
									Role = Role + " @ Edgeville";
								}
								if (CurrentBox.getSelectedItem().equals(SmeltPlaces[1])) {
									SmeltArea = SmeltAreas.ALKHARID;
									WalkTile = new RSTile(3276, 3186);
									Role = Role + " @ Alkharid";
								}
								Duration = System.currentTimeMillis();
								startedScript = true;
								dispose();
							}									
						}, "Start");
						CurrentBox.setBounds(new Rectangle(new Point(27, 5), CurrentBox.getPreferredSize()));
					}
				}
			}, "Next");
			CurrentBox.setBounds(new Rectangle(new Point(27, 5), CurrentBox.getPreferredSize()));
		}
		
	}
	
}