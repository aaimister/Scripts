
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.*;
import java.io.*;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.*;
import java.awt.*;

import org.rsbot.script.*;
import org.rsbot.script.util.*;
import org.rsbot.script.wrappers.*;
import org.rsbot.script.methods.*;
import org.rsbot.event.listeners.*;
import org.rsbot.event.events.*;

@ScriptManifest(authors={"Strikeskids"}, name="HellhoundStriker", description="Kills hellhounds at the taverly dungeon", version=2.05)

public class HellhoundStriker extends Script implements PaintListener, MessageListener, MouseListener {
    
    private final Color color1 = new Color(202, 184, 150);
    private final Color color2 = new Color(0, 0, 0);
    private final Color[] charmColors = {new Color(110, 92, 61),new Color(81, 93, 71),new Color(92, 32, 28),new Color(41, 91, 93)};
    private final Color color5 = new Color(255, 0, 51);
    private final Color color7 = new Color(0, 255, 0);
    
    private final BasicStroke stroke1 = new BasicStroke(1);
    
    private final Font font1 = new Font("Courier New", 0, 12);
    private final Font font2 = new Font("Arial", 0, 18);
    private final Font font3 = new Font("Courier New", 1, 12);
    
    int last = 205;
    String threadLoc = "http://adf.ly/2VRFC";
    String[] loot={"ancient","Clue","charm"};
    String[] junk={"Pie dish","Vial","bones"};
    //Tiles and Areas
    RSTile faladorWall=new RSTile(2935,3355);
    RSTile wallStep=new RSTile(2939,3361);
    RSTile bankStep=new RSTile(2957,3381);
    RSTile bankStep2=new RSTile(2950,3376);
    RSTile ladderLoc=new RSTile(2884,3398);
    RSTile pipeLoc=new RSTile(2886,9799);
    RSTile houndLoc=new RSTile(2869,9829);
    RSArea houndArea= new RSArea(2846,9818,2874,9854);
    RSArea bankArea=new RSArea(2942,3367,2950,3374);
    RSTile bankLoc=new RSTile(2946,3368);
    RSTile door1Loc=new RSTile(2978,3346);
    RSTile door2Loc=new RSTile(2983,3348);
	RSTile door3Loc=new RSTile(2983,3348,1);
	RSTile stairsLoc=new RSTile(2984,3336,1);
	RSTile altarLoc=new RSTile(2983,3344,2);
    RSTile altar2Loc=new RSTile(2608,3209);
    RSTile failSafeLoc = new RSTile(2880,9817);
    int[] startXp=new int[4];
    int[] skillOrder={skills.ATTACK,skills.STRENGTH,skills.DEFENSE,skills.CONSTITUTION};
    String[] skillNames={"Att","Str","Def","Cst"};
    boolean paintOpen=true;

    int houndsKilled = 0;
    
    long startMillis;
    long addToStart;
    
    int wallId=11844;
    int ladderId=55404;
    int pipeId=9293;
	int doorId=11714;
	int ladder2Id=11727;
	int stairsId=11734;
	int altarId=35400;
    int altar2Id=409;
    int extraSleep;
    
    int faladorTele=8009;
    int food=379;
    int[] prayPot={2434,143,141,139};
    int[] superStr={2440,161,159,157};
    int[] superAtt={2436,149,147,145};
    int[] superDef={2442,167,165,163};
    int[] antiPoison={179,177,175,2446,5949,5947,5945,5943,5958,5956,5954,5952,185,183,181,2448};
    int[] agilPotion={3038,3036,3034,3032};
    int[] summerPie = {7220,7218};
    int pie=7220;
    
    int numFood=3;
    int numPray;
    int numPots=2;
    int numDef=0;
    int numPie=0;
    RSWeb walkWeb=null;
    RSTile dest = null;
    boolean isWalking=false;
    boolean areAggressive=true;
    int aggresiveCheck=0;
    boolean hasStarted=false; 
    boolean hasClicked=false;
    boolean walkingToAltar=false;
    boolean walkingToHounds=false;
    boolean hasPoison;
    boolean waitedForLoot=false;
    boolean useAgil = false;
    boolean hasAgil= false;
    
    int[] charmInBank={-1,-1,-1,-1};
    int[] charmId={12158,12159,12160,12163};
    String[] charmNames={"Gold","Green","Crimson","Blue"};
    boolean hasCountedCharms;
    int effigyInBank;
    boolean clueScroll=false;
    boolean isTaking=false;
    boolean stopWithClue=false;
    boolean teleOnClue=false;
    boolean prayingAtMonastery=false;
    boolean useMonastery=true;
    boolean takenScreenshot=false;
    boolean useAltar=true;
    boolean stopOnLevel;
    int stopSkill;
    int levelToStop;
    String status;
    boolean usePot;
    boolean usePie;
    
    boolean useSpec=false;
    int specWep;
    int normWep=-1;
    boolean doingSpec;
    boolean canDoSpec = true;
    boolean useSameWeap=false;
    
    RSNPC curHound=null;
    
    enum TaskEnum {
        WAIT_FOR_ANIMATION,
        WALKING,
        WALK_TO_ALTAR,
        WALK_TO_BANK,
        OPEN_BANK,
        BANK,
        WALK_TO_WALL,
        CLIMB_WALL,
        WALK_TO_LADDER,
        CLIMB_LADDER,
        WALK_TO_PIPE,
        CRAWL_PIPE,
        WALK_TO_HOUNDS,
        FIGHT,
        TELE,
        LOGIN,
        FAILSAFE,
        MONASTERY,
        NOTHING,
        EQUIP_NORM
    };
    
    
    public boolean onStart() {
        logm("Starting up...");
        if (getVersion()>thisVersion()) {
            update();
            return false;
        }
        GUI g = new GUI();
        g.show();
        while (!hasClicked&&g.getFrame().isVisible()) sleep(100);
        g.hide();
        g.setFrame(null);
        log("Food "+food+": "+numFood+" Pray: "+numPray+" Supers: "+numPots+" Def: "+numDef);
        log("Using agility potion: "+useAgil+" Using spec: "+useSpec+" Weap: "+((useSpec)?((useSameWeap)?"This":specWep):"None"));
        startMillis = System.currentTimeMillis()-1;
        addToStart = startMillis;
        camera.setAngle(0);
        camera.setPitch(true);
        logm("Started");
        return true;
    }
                                                    
    public void update() {
        int ret = JOptionPane.showConfirmDialog(null,"Would you like to go the thread?","Script Outdated",JOptionPane.YES_NO_OPTION);
        if (ret==JOptionPane.YES_OPTION) {
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
                
                if (!desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
                    log("Can't open thread.");
                    return;
                }
                
                try {
                    java.net.URI uri = new java.net.URI(threadLoc);
                    desktop.browse(uri);
                } catch (Exception e) {
                    
                }
            }
        }
        else {
            
        }
    }
    
    public int getVersion() {
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(new URL("http://strikeskids.com/ScriptVersions/HellhoundStrikerVersion.txt").openStream()));
            int v = Integer.parseInt(r.readLine());
            r.close();
            return v;
        }
        catch (Exception ex) {
            log.severe("Unable to read version");
            return 0;
        }
    }
    
    private int thisVersion() {
        return last;
    }
    
    public void onFinish() {
        paintOpen = true;
        walkWeb = null;
        dest = null;
        isWalking = false;
        log("Status at end was: "+status);
    }
    
    public int loop() {
        startMillis += System.currentTimeMillis()-addToStart;
        sleep(extraSleep);
        extraSleep = 0;
        addToStart = System.currentTimeMillis();
        int value = looper();
        if (value==-1) value=500;
        else addToStart = System.currentTimeMillis()+value;
        return value;
    }
    
    private int looper() {
        if (!hasStarted) {
            stopScript();
            return 0;
        }
        RSObject temp;
        if (combat.isPoisoned()&&inventory.containsOneOf(antiPoison)) {
            interfaces.getComponent(748,2).interact("Use cure");
            sleep(500);
        }
        if (houndArea.contains(getMyPlayer().getLocation())) {
            if (!prayer.isQuickPrayerOn()) prayer.setQuickPrayer(true);
        }
        if (combat.getLifePoints()<500) {
            if (inventory.contains(food)) {
                inventory.getItem(food).interact("Eat");
            }
            else if (combat.getLifePoints()<300) {
                if (game.getCurrentTab()!=game.TAB_INVENTORY) {
                    game.openTab(game.TAB_INVENTORY);
                    sleep(700);
                }
                if (inventory.getItem(faladorTele)!=null) inventory.getItem(faladorTele).interact("Break");
            }
        }
        if (startXp[0]==0) {
            for (int i=0;i<startXp.length;i++) {
                startXp[i] = skills.getCurrentExp(skillOrder[i]);
            }
        }
        if (charmInBank[0]==-1) {
            for (int i=0;i<charmInBank.length;i++) {
                if (game.getCurrentTab()==game.TAB_INVENTORY&&inventory.contains(charmId[i])) {
                    charmInBank[i] = -inventory.getCount(true,charmId[i]);
                }
                else {
                    charmInBank[i]=0;
                }
            }
        }
        if (useSpec&&normWep==-1) {
            if (game.getCurrentTab()!=game.TAB_EQUIPMENT) {
                game.openTab(game.TAB_EQUIPMENT);
                sleep(700);
            }
            normWep = equipment.getItem(equipment.WEAPON).getID();
            game.openTab(game.TAB_INVENTORY);
            sleep(700);
        }
        if (game.isLoggedIn()) {            
            camera.setAngle(0);
            camera.setPitch(true);
        }
        TaskEnum task=getTask();
        switch (task) {
            case WAIT_FOR_ANIMATION:
                return 100;
            case FAILSAFE:
                logm("Failsafe");
                isWalking = false;
                inventory.getItem(faladorTele).interact("Break");
                return 1000;
            case WALKING:
                if (walkWeb==null&&dest==null) {
                    isWalking = false;
                    return 100;
                }
                if ((walkingToHounds&&houndArea.contains(getMyPlayer().getLocation()))||(walkWeb!=null&&calc.distanceTo(walkWeb.getEnd())<4)||(dest!=null&&calc.distanceTo(dest)<4)) {
                    isWalking = false;
                    walkWeb = null;
                    dest = null;
                    return 100;
                }
                if ((calc.distanceTo(walking.getDestination())<4||walking.getDestination()==null)&&walkWeb!=null) {
                    walkWeb.step();
                    return 600;
                }
                break;
            case WALK_TO_BANK:
                logm("Walk to bank");
                prayer.setQuickPrayer(false);
                int distToBank = calc.distanceTo(bankLoc);
                int distToStep = calc.distanceTo(bankStep);
                if (calc.tileOnMap(bankLoc)) newWeb(bankLoc);
                else if (calc.tileOnMap(bankStep2)&&distToBank>distToStep) newWeb(bankStep2);
                else if (distToBank>distToStep) newWeb(bankStep);
                else newWeb(bankLoc);
                return 100;
            case WALK_TO_ALTAR:
                logm("Recharge prayer");
                prayer.setQuickPrayer(false);
                if (useMonastery) {
                    if (game.getCurrentTab()!=game.TAB_EQUIPMENT) {
                        game.openTab(game.TAB_EQUIPMENT);
                        sleep(700);
                    }
                    equipment.getItem(equipment.CAPE).interact("Monastery");
                    prayingAtMonastery = true;
                    return 700;
                }
                walkingToAltar=true;
                if (combat.getPrayerPoints()==skills.getRealLevel(skills.PRAYER)*10&&getMyPlayer().getLocation().getZ()==0) {
                    walkingToAltar=false;
                    return 100;
                }
                if (getMyPlayer().getLocation().getZ()==0) {
                    //On ground floor
                    RSObject door = objects.getNearest(doorId);
                    if (door!=null) {
                        RSTile doorTempLoc = door.getLocation();
                        if (doorTempLoc.equals(door1Loc)) {
                            if (door.isOnScreen()) {
                                tiles.interact(doorTempLoc,.5,1,1,"Open");
                                sleep(700);
                                walking.walkTileMM(door2Loc);
                            }
                            else {
                                walking.walkTileMM(doorTempLoc);
                            }
                            return 700;
                        }
                        else if (doorTempLoc.equals(door2Loc)) {
                            if (door.isOnScreen()) {
                                tiles.interact(doorTempLoc,.5,1,1,"Open");
                            }
                            else {
                                walking.walkTileMM(doorTempLoc);
                            }
                            return 700;
                        }
                    }
                    RSObject ladder = objects.getNearest(ladder2Id);
                    if (ladder!=null&&calc.distanceBetween(ladder.getLocation(),door2Loc)<4) {
                        if (ladder.isOnScreen()) {
                            ladder.interact("Climb");
                        }
                        else {
                            walking.walkTileMM(ladder.getLocation());
                        }
                        return 700;
                    }

                    if (calc.tileOnMap(door1Loc)) {
                        walking.walkTileMM(door1Loc);
                        return 700;
                    }
                    else {
                        walkWeb = web.getWeb(door1Loc);
                        isWalking = true;
                        return 100;
                    }
                }
                else if (getMyPlayer().getLocation().getZ()==1) {
                    //On first floor
                    RSObject door = objects.getNearest(doorId);
                    if (door!=null&&door.getLocation().equals(door3Loc)) {
                        tiles.interact(door.getLocation(),.5,1,1,"Open");
                        return 700;
                    }
                    RSObject stairs = objects.getNearest(stairsId);
                    if (stairs!=null&&stairs.isOnScreen()) {
                        stairs.interact("Climb");
                        return 700;
                    }
                    walking.walkTileMM(stairsLoc);
                }
                else {
                    //On second floor
                    if (combat.getPrayerPoints()==skills.getRealLevel(skills.PRAYER)*10) {
                        inventory.getItem(faladorTele).interact("Break");
                        return 700;
                    }
                    //Pray
                    RSObject alt = objects.getNearest(altarId);
                    if (alt==null) {
                        log.severe("Error in climbing. Restarting");
                        inventory.getItem(faladorTele).interact("Break");
                        return 700;
                    }
                    if (alt.isOnScreen()) {
                        //Pray at the altar
                        alt.interact("Pray");
                    }
                    else {
                        walking.walkTileMM(altarLoc);
                    }
                    return 700;
                }
            case MONASTERY:
                logm("Praying at monastery");
                prayingAtMonastery = true;
                if (combat.getPrayerPoints()==skills.getRealLevel(skills.PRAYER)*10) {
                    if (calc.distanceTo(altar2Loc)>calc.distanceTo(bankLoc)) {
                        prayingAtMonastery=false;
                        return 100;
                    }
                    else {
                        logm("Tele back to falador");
                        if (game.getCurrentTab()!=game.TAB_INVENTORY) {
                            game.openTab(game.TAB_INVENTORY);
                            sleep(700);
                        }
                        inventory.getItem(faladorTele).interact("Break");
                        return 700;
                    }
                }
                RSObject altar = objects.getNearest(altar2Id);
                if (altar!=null&&altar.isOnScreen()) {
                    altar.interact("Pray");
                }
                else {
                    walking.walkTileMM(altar2Loc);
                }
                return 700;
            case WALK_TO_HOUNDS:
                walkingToHounds = true;
                logm("Walk to hounds");
                newWeb(houndLoc);
                return 100;
            case WALK_TO_LADDER:
                logm("Walk to ladder");
                hasCountedCharms = false;
                newWeb(ladderLoc);
                return 100;
            case WALK_TO_PIPE:
                logm("Walk to pipe");
                newWeb(pipeLoc);
                return 100;
            case WALK_TO_WALL:
                logm("Walk to wall");
                if (calc.tileOnMap(faladorWall)) newWeb(faladorWall);
                else newWeb(wallStep);
                return 100;
            case OPEN_BANK:
                logm("Open the bank");
                RSObject bankBooth=objects.getNearest(bank.BANK_BOOTHS);
                if (bankBooth!=null) {
                    if (bankBooth.isOnScreen()) {
                        bankBooth.interact("Use-quick");
                    }
                    else {
                        walking.walkTileMM(bankBooth.getLocation());
                    }
                }
                else {
                    RSNPC banker = npcs.getNearest(bank.BANKERS);
                    if (banker!=null) {
                        if (banker.isOnScreen()) {
                            banker.interact("Bank");
                        }
                        else {
                            walking.walkTileMM(banker.getLocation());
                        }
                    }
                    else {
                        return 100;
                    }
                }
                return 1700;
            case BANK:
                logm("Bank");
                if (!bank.isOpen()) return 100;
                if (!hasCountedCharms) {
                    if (inventory.contains(12158)) charmInBank[0] += inventory.getCount(true,12158);
                    if (inventory.contains(12159)) charmInBank[1] += inventory.getCount(true,12159);
                    if (inventory.contains(12160)) charmInBank[2] += inventory.getCount(true,12160);
                    if (inventory.contains(12163)) charmInBank[3] += inventory.getCount(true,12163);
                    if (inventory.getItem("Starved ancient effigy")!=null) {
                        effigyInBank++;
                    }
                    hasCountedCharms = true;
                }
                if (inventory.getItem(loot)!=null) {
                    bank.depositAll();
                    return 700;
                }
                if (isInventReady()&&combat.getLifePoints()>=500) {
                    bank.close();
                    return 700;
                }
                fixInventCount(food,numFood);
                fixInventCount(prayPot[0],numPray);
                fixInventCount(superAtt[0],numPots);
                fixInventCount(superStr[0],numPots);
                fixInventCount(superDef[0],numDef);
                fixInventCount(faladorTele,5);
                fixInventCount(pie,numPie);
                if (useSpec&&!useSameWeap) {
                    fixInventCount(specWep,1);
                }
                if (!inventory.containsOneOf(antiPoison)) {
                    for (int i=0;i<antiPoison.length;i++) {
                        if (bank.getCount(antiPoison[i])>0) {
                            bank.withdraw(antiPoison[i],1);
                            hasPoison = true;
                            break;
                        }
                        hasPoison = false;
                    }
                }
                if (usePie) {
                    if (!inventory.containsOneOf(summerPie)) {
                        for (int i=0;i<summerPie.length;i++) {
                            if (bank.getCount(summerPie[i])>0) {
                                bank.withdraw(summerPie[i],1);
                                hasAgil = true;
                                break;
                            }
                            hasAgil = false;
                        }
                    }
                }
                else if (usePot) {
                    if (!inventory.containsOneOf(agilPotion)) {
                        for (int i=0;i<agilPotion.length;i++) {
                            if (bank.getCount(agilPotion[i])>0) {
                                bank.withdraw(agilPotion[i],1);
                                hasAgil = true;
                                break;
                            }
                            hasAgil = false;
                        }
                    }
                }
                if (useAgil&&!hasAgil) {
                    log.severe("Out of agility boosts");
                    stopScript();
                    return 0;
                }
                if (combat.getLifePoints()<500) {
                    if (inventory.getItem(food)!=null) {
                        inventory.getItem(food).interact("Eat");
                        return 1400;
                    }
                }
                return 700;
            case CLIMB_WALL:
                logm("Climb the wall");
                temp = objects.getNearest(wallId);
                if (temp!=null&&temp.isOnScreen()) {
                    tiles.interact(faladorWall,.9,.5,0,"Climb");
                }
                return 2400;
            case CLIMB_LADDER:
                logm("Climb the ladder");
                temp = objects.getNearest(ladderId);
                if (temp!=null&&temp.isOnScreen()) {
                    temp.interact("Climb");
                }
                return 1800;
            case CRAWL_PIPE:
                logm("Crawl through the pipe");
                if (useAgil&&skills.getCurrentLevel(skills.AGILITY)<70) {
                    if (usePot) inventory.getItem(agilPotion).interact("Drink");
                    if (usePie) inventory.getItem(summerPie).interact("Eat");
                    return 1600;
                }
                temp = objects.getNearest(pipeId);
                if (temp!=null&&temp.isOnScreen()) {
                    temp.interact("Squeeze");
                }
                return 700;
             case TELE:
                logm("Teleport");
                prayer.setQuickPrayer(false);
                if (game.getCurrentTab()!=game.TAB_INVENTORY) {
                    game.openTab(game.TAB_INVENTORY);
                    sleep(700);
                }
                if (inventory.getItem(faladorTele)!=null) {
                    inventory.getItem(faladorTele).interact("Break");
                }
                return 700;
            case FIGHT:
                walkingToHounds = false;
                logm("Fight the hounds");
                if (!combat.isAutoRetaliateEnabled()) combat.setAutoRetaliate(true);
                if (!prayer.isQuickPrayerOn()&&getMyPlayer().isInCombat()) prayer.setQuickPrayer(true);
                if (loot()) return 1500;
                if (doingSpec) reSpec();
                int fightVal = fightHounds();
                if (fightVal==2) return 2400;
                if (fightVal==1) return 700;
                waitedForLoot = false;
                checkFight();
                antiBan();
                return 700;
            case EQUIP_NORM:
                if (game.getCurrentTab()!=game.TAB_INVENTORY) {
                    game.openTab(game.TAB_INVENTORY);
                    sleep(700);
                }
                inventory.getItem(normWep).interact("W");
                return 700;
            case LOGIN:
                logm("Login");
                if (!interfaces.get(906).isValid()) return -1;
                interfaces.getComponent(906,171).doClick();
                return -1;
        }
        return 1500;
    }
    
    private void antiBan() {
        if (random(1,7)==4) {
            //Going to do antiban
            switch(random(0,3)) {
                case 0:
                    if (random(0,7)==2) {
                        game.openTab(game.TAB_STATS);
                        sleep(1000);
                        skills.doHover(skills.INTERFACE_STRENGTH);
                        sleep(1000);
                        game.openTab(game.TAB_INVENTORY);
                    }
                    break;
                case 1:
                    mouse.moveRandomly(100);
                    break;
                case 2:
                    if (random(0,15)==2) {
                        RSNPC r = npcs.getNearest("Hellhound");
                        if (r!=null&&r.isOnScreen()) {
                            mouse.moveSlightly();
                            mouse.move(r.getPoint());
                            sleep(random(0,30));
                            mouse.click(false);
                        }
                        mouse.moveRandomly(700);
                    }
                    break;
            }
        }
    }

    
    private boolean loot() {
        isTaking = false;
        RSGroundItem pickup = groundItems.getNearest(new Filter<RSGroundItem>() {
            public boolean accept(RSGroundItem rgi) {
                if (rgi==null) return false;
                RSItem i = rgi.getItem();
                if (!arrayContainsContains(loot,i.getName())) return false;
                return true;
            }
        });
        if  (pickup!=null) {
            isTaking = true;
            int pickupId = pickup.getItem().getID();
            if (!inventory.contains(pickupId)) {
                if (inventory.isFull()) {
                    if (dropJunk()) {
                        
                    }
                    else if (inventory.contains(food)) {
                        inventory.getItem(food).interact("Eat");
                    }
                    else {
                        drinkPrayPot();
                        return true;
                    }
                }
            }
            if (pickup.isOnScreen()) pickup.interact("Take "+pickup.getItem().getName());
            else {
                walking.walkTileMM(pickup.getLocation());
            }
            return true;
        }
        return false;
    }
    
    private void reSpec() {
        if (combat.getSpecialBarEnergy()<25||!canDoSpec) {
            doingSpec = false;
            canDoSpec = true;
            if (game.getCurrentTab()!=game.TAB_INVENTORY) {
                game.openTab(game.TAB_INVENTORY);
            }
            if (!useSameWeap&&inventory.contains(normWep)) {
                inventory.getItem(normWep).interact("W");
            }
        }
        if (!combat.isSpecialEnabled()) {
            if (game.getCurrentTab()!=game.TAB_ATTACK) {
                game.openTab(game.TAB_ATTACK);
            }
            combat.setSpecialAttack(true);
        }
    }
    private boolean doSpec() {
        if (inventory.contains(specWep)||useSameWeap) {
            doingSpec = true;
            if (!useSameWeap) {
                inventory.getItem(specWep).interact("W");
            }
            game.openTab(game.TAB_ATTACK);
            sleep(700);
            if (!combat.isSpecialEnabled()) {
                combat.setSpecialAttack(true); 
            }
            sleep(700);
            return true;
        }
        else {
            return false;
        }
    }
    
    private int fightHounds() {
        RSNPC hound = (RSNPC) getMyPlayer().getInteracting();
        if (hound==null||hound.isDead()) {
            if (hound!=null&&curHound!=null&&hound.isDead()) {
                curHound = null;
                houndsKilled++;
            }
            //Killed hound
            RSNPC newHound = npcs.getNearest(new Filter<RSNPC>(){
                public boolean accept(RSNPC in) {
                    if (in==null) return false;
                    if (!in.getName().contains("hound")) return false;
                    if (in.isDead()) return false;
                    if (in.getInteracting()!=null&&!in.getInteracting().equals(getMyPlayer())) return false;
                    if (houndOnOppositeSide(in)) return false;
                    return true;
                }
            });
            if (newHound!=null) {
                if (combat.getSpecialBarEnergy()==100&&useSpec&&!doingSpec) {
                    if (doSpec()) return 1;
                }
                if (getLengthToHound(newHound)<6||waitedForLoot) {
                    if (newHound.isOnScreen()) {
                        newHound.interact("Attack "+newHound.getName());
                        if (!prayer.isQuickPrayerOn()) prayer.setQuickPrayer(true);
                        curHound = newHound;
                    }
                    else {
                        walking.walkTileMM(newHound.getLocation());
                    }
                }
                else  {
                    logm("Waiting for loot");
                    waitedForLoot = true;
                    return 2;
                }
            }
            return 1;
        }
        return 0;
    }
    
    private boolean houndOnOppositeSide(RSNPC h) {
        RSArea lowRidge = new RSArea(new RSTile[]{new RSTile(2859,9849),new RSTile(2864,9842),new RSTile(2863,9833),new RSTile(2851,9833),new RSTile(2851,9849)});
        RSArea highRidge = new RSArea(new RSTile[]{new RSTile(2873,9818),new RSTile(2873,9849), new RSTile(2862,9849),new RSTile(2865,9844),new RSTile(2865,9831), new RSTile(2865,9818)});
        RSTile hLoc = h.getLocation();
        RSTile myLoc = getMyPlayer().getLocation();

        return (highRidge.contains(myLoc)&&lowRidge.contains(hLoc)||highRidge.contains(hLoc)&&lowRidge.contains(myLoc));
    }
    
    private int getLengthToHound(RSNPC h) {
        RSArea lowRidge = new RSArea(new RSTile[]{new RSTile(2859,9849),new RSTile(2864,9842),new RSTile(2863,9833),new RSTile(2851,9833),new RSTile(2851,9849)});
        RSArea highRidge = new RSArea(new RSTile[]{new RSTile(2873,9818),new RSTile(2873,9849), new RSTile(2862,9849),new RSTile(2865,9844),new RSTile(2865,9831), new RSTile(2865,9818)});
        RSTile hLoc = h.getLocation();
        RSTile myLoc = getMyPlayer().getLocation();
        
        
        int ret = calc.distanceTo(hLoc);
        if (houndOnOppositeSide(h)) {
            int yLoc = (myLoc.getY()<hLoc.getY())?myLoc.getY():hLoc.getY();
            int endRidge = 9849;
            ret += Math.abs(endRidge-yLoc)*2;
        }
        return ret;
    }
    
    private void checkFight() {
        if (inventory.getItem("Clue scroll (hard)")!=null) {
            clueScroll = true;
        }
        if (numPray>0&&combat.getPrayerPoints()<200) {
            drinkPrayPot();
        }
        if (numPots>0&&skills.getRealLevel(skills.ATTACK)+5>=skills.getCurrentLevel(skills.ATTACK)) {
            drinkAtt();
        }
        if (numPots>0&&skills.getRealLevel(skills.STRENGTH)+5>=skills.getCurrentLevel(skills.STRENGTH)) {
            drinkStr();
        }
        if (numDef>0&&skills.getRealLevel(skills.DEFENSE)>=skills.getCurrentLevel(skills.DEFENSE)) {
            drinkDef();
        }
    }
    
    private boolean dropJunk() {
        if (inventory.contains("Bones")) {
            inventory.getItem("Bones").interact("Bury");
            sleep(1600);
            return true;
        }
        if (inventory.contains("Vial")) {
            inventory.getItem("Vial").interact("Drop");
            return true;
        }
        if (inventory.contains("Pie dish")) {
            inventory.getItem("Pie dish").interact("Drop");
            return true;
        }
        return false;
    }
    
    private void logm(String s) {
        status = s;
    }
    
    private void newWeb(RSTile end) {
        if (calc.tileOnMap(end)) {
            walking.walkTileMM(end);
            dest = end;
        }
        else  {
            walkWeb = web.getWeb(end);
        }
        isWalking = true;
    }

    private void drinkPrayPot() {
        for (int i=prayPot.length-1;i>=0;i--) {
            if (inventory.contains(prayPot[i])) {
                inventory.getItem(prayPot[i]).interact("Drink");
                return;
            }
        }
    }
    
    private void drinkAtt() {
        for (int i=superAtt.length-1;i>=0;i--) {
            if (inventory.contains(superAtt[i])) {
                inventory.getItem(superAtt[i]).interact("Drink");
                sleep(1700);
                break;
            }
        }
    }
    
    private void drinkStr() {
        
        for (int i=superStr.length-1;i>=0;i--) {
            if (inventory.contains(superStr[i])) {
                inventory.getItem(superStr[i]).interact("Drink");
                sleep(1700);
                return;
            }
        }
    }
    
    private void drinkDef() {
        for (int i=superDef.length-1;i>=0;i--) {
            if (inventory.contains(superDef[i])) {
                inventory.getItem(superDef[i]).interact("Drink");
            }
        }
    }
    
    private void fixInventCount(int itemId, int qty) {
        if (!bank.isOpen()) return;
        int dif = qty-inventory.getCount(true,itemId);
        if (dif==0) {
            return;
        }
        else if (dif>0) {
            if (bank.getCount(itemId)<dif) {
                log.severe("Out of item: "+itemId);
                stopScript();
                return;
            }
            if (dif<4) {
                for (int i=0;i<dif;i++) {
                    bank.withdraw(itemId,1);
                    sleep(100);
                }
            }
            else {
                bank.withdraw(itemId,dif);
                sleep(300);
            }
        }
        else {
            dif = -dif;
            if (dif<4) {
                for (int i=0;i<dif;i++) {
                    bank.deposit(itemId,1);
                    sleep(100);
                }
            }
            else {
                bank.deposit(itemId,dif);
            }
        }
        return;
    }
    
    private TaskEnum getTask() {
        if (game.isLoginScreen()) return TaskEnum.NOTHING;
        if (interfaces.get(906).isValid()) return TaskEnum.LOGIN;
        if ((getMyPlayer().getInteracting()==null&&getMyPlayer().getAnimation()!=-1)||(getMyPlayer().isMoving()&&!isWalking)) return TaskEnum.WAIT_FOR_ANIMATION;
        if (getMyPlayer().getLocation().getX()>=failSafeLoc.getX()&&getMyPlayer().getLocation().getY()>=failSafeLoc.getY()) return TaskEnum.FAILSAFE;
        int pipeDist = calc.distanceTo(pipeLoc);
        int bankDist = calc.distanceTo(bankLoc);
        if (combat.getLifePoints()<200&&pipeDist>bankDist) {
            logm("Low life points...");
            return TaskEnum.TELE;
        }
        if (isWalking) return TaskEnum.WALKING;
        if (useAltar) {
            if (prayingAtMonastery||calc.distanceTo(bankLoc)>calc.distanceTo(altar2Loc)) return TaskEnum.MONASTERY;
            if (walkingToAltar) return TaskEnum.WALK_TO_ALTAR;
        }
        if (isTaking) return TaskEnum.FIGHT;
        if (bankArea.contains(getMyPlayer().getLocation())) {
            if (stopWithClue&&clueScroll) {
                stopScript();
                return TaskEnum.NOTHING;
            }
            if (stopOnLevel&&skills.getRealLevel(stopSkill)>=levelToStop) {
                stopScript();
                return TaskEnum.NOTHING;
            }
            //In bank area
            if (bank.isOpen()) {
                //Do banking
                return TaskEnum.BANK;
            }
            if (inventory.contains(normWep)&&useSpec&&!useSameWeap) {
                return TaskEnum.EQUIP_NORM;
            }
            if (isInventReady()) {
                //Walk to the wall
                return TaskEnum.WALK_TO_WALL;
            }
            //Open the bank
            return TaskEnum.OPEN_BANK;
        }
        RSObject temp;
        //log("Pipe dist: "+pipeDist+" Bank dist: "+bankDist);
        if (pipeDist>bankDist) {
            if (stopWithClue&&clueScroll&&!takenScreenshot) {
                env.saveScreenshot(false);
                takenScreenshot = true;
            }
            if (stopOnLevel&&skills.getRealLevel(stopSkill)>=levelToStop&&!takenScreenshot) {
                env.saveScreenshot(false);
                takenScreenshot = true;
            }
            //log("On surface");
            if (getMyPlayer().getLocation().getX()<=faladorWall.getX()) {
                //log("Past Wall");
                temp = objects.getNearest(ladderId);
                if (temp!=null&&temp.isOnScreen()) {
                    //Climb the ladder into the dungeon
                    return TaskEnum.CLIMB_LADDER;
                }
                //Walk to the ladder
                return TaskEnum.WALK_TO_LADDER;
            }
            else {
                //log("Not past wall so chekc if invent is ready to go");
                if (useAltar) {
                    if (numPray>0&&combat.getPrayerPoints()<skills.getRealLevel(skills.PRAYER)*10) {
                        return TaskEnum.WALK_TO_ALTAR;
                    }
                }
                if (isInventReady()) {
                    //log("Invent is ready");
                    temp = objects.getNearest(wallId);
                    if (temp!=null&&temp.isOnScreen()) {
                        //Climb the falador wall
                        return TaskEnum.CLIMB_WALL;
                    }
                    //Walk to the wall
                    return TaskEnum.WALK_TO_WALL;
                }
                else {
                    //Inventory not ready so walk to the bank
                    return TaskEnum.WALK_TO_BANK;
                }
            }
        }
        //log("Not on surface");
        temp = objects.getNearest(pipeId);
        if (temp!=null&&temp.isOnScreen()) {
            int tempDist = calc.distanceTo(temp.getLocation());
            if (calc.distanceTo(pipeLoc)<((tempDist<5)?tempDist:5)) {
                //Crawl through the pipe
                return TaskEnum.CRAWL_PIPE;
            }
            else {
                //Already on the other side so
                return TaskEnum.WALK_TO_HOUNDS;
            }
        }
        //Now there is no pipe, and we are not on the surface so
        if (houndArea.contains(getMyPlayer().getLocation())) {
            if (stopWithClue&&clueScroll) {
                log("Got a clue scroll");
                return TaskEnum.TELE;
            }
            if (stopOnLevel&&skills.getRealLevel(stopSkill)>=levelToStop) {
                log("Finished levelling...");
                return TaskEnum.TELE;
            }
            if (combat.getPrayerPoints()<50&&inventory.getCount(prayPot)==0&&numPray>0) {
                return TaskEnum.TELE;
            }
            if (combat.getLifePoints()<200&&!inventory.contains(food)) {
                return TaskEnum.TELE;
            }
            return TaskEnum.FIGHT;
        }
        else {
            return TaskEnum.WALK_TO_HOUNDS;
        }
    }
    
    private boolean isInventReady() {
        if (inventory.getCount(food)!=numFood) return false;
        if (inventory.getCount(prayPot)!=numPray) return false;
        if (inventory.getCount(superStr)!=numPots) return false;
        if (inventory.getCount(superAtt)!=numPots) return false;
        if (inventory.getCount(superDef)!=numDef) return false;
        if (inventory.getCount(true,faladorTele)<5) return false;
        if (useAgil&&!inventory.containsOneOf(agilPotion)) return false;
        if (useSpec&&!useSameWeap&&!inventory.contains(specWep)) return false;
        return true;
    }
    
    private boolean arrayContains(int[] a, int b) {
        for (int i=0;i<a.length;i++) {
            if (a[i]==b) return true;
        }
        return false;
    }
    
    private int inArrayAtLocation(int[] a, int b) {
        for (int i=0;i<a.length;i++) {
            if (a[i]==b) return i;
        }
        return -1;
    }
    
    private boolean arrayContains(String[] a, String b) {
        for (int i=0;i<a.length;i++) {
            if (a[i].equalsIgnoreCase(b)) return true;
        }
        return false;
    }
    
    private int inArrayAtLocation(String[] a, String b) {
        for (int i=0;i<a.length;i++) {
            if (a[i].equalsIgnoreCase(b)) return i;
        }
        return -1;
    }
                                                            
    private boolean arrayContainsContains(String[] a, String b) {
        if (b==null) return false;
        for (int i=0;i<a.length;i++) {
            if (a[i]!=null&&(a[i].contains(b)||b.contains(a[i]))) return true;
        }
        return false;
    }
                                                    

    
    public void messageReceived(MessageEvent e) {
        if (e.getMessage().contains("I'm already under attack")) {
            extraSleep = 2400;
        }
        if (e.getMessage().contains("You don't have enough power left")) {
            canDoSpec = false;
        }
    }
    
    class GUI {
        JFrame frame;
        GUI() {
            frame = new JFrame("Hellhound Striker");
            JTabbedPane tabs = new JTabbedPane();
            tabs.add("Options",new OptionPanel());
            tabs.add("Instructions",new AboutPanel());
            frame.setContentPane(tabs);
            frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            frame.setSize(409,450);
            //frame.setResizable(false);
        }
        
        void show() {
            frame.setVisible(true);
        }
        
        void hide() {
            frame.setVisible(false);
        }
        
        JFrame getFrame() {
            return frame;
        }
        
        void setFrame(JFrame f) {
            frame = f;
        }
    }
    
    class OptionPanel extends JPanel {
        JLabel[] labels ={new JLabel("Prayer: 06"),new JLabel("Food: 03"),new JLabel("Food Id:"),new JLabel("Super att/str: 02"),new JLabel("Super def: 00"),new JLabel("Mouse speed: 04"), new JLabel("Pray location:"), new JLabel("For pipe:"), new JLabel("Special Attack"), new JLabel("Stopping:"),new JLabel("Stop on level:")};
        String[] titles = {"Prayer","Food","Food id","Super att/str","Super def","Mouse speed"};
        int[] startValues = {6,3,379,2,0,4,-1,-1,-1,-1,-1};
        int[] maximums = {14,11,-1,6,8,10,-1,-1,-1,-1,-1};
        JTextField fi,lvlStop,specWepId;
        JSlider[] sliders = {null,null,null,null,null};
        JComboBox stop, mona, agil, lvl, spec;
        OptionPanel() {
            setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            fi  = new JTextField("379",4);
            
            c.gridx = 0;
            c.gridy = 0;
            c.anchor = GridBagConstraints.WEST;
            for (int i=0,j=0;i<labels.length;i++) {
                c.gridy = i;
                if (labels[i]!=null) {
                    add(labels[i],c);
                }
            }
            
            c.gridwidth = 2;
            c.gridx=1;
            c.fill = 2;
            c.anchor = GridBagConstraints.EAST;
            for (int i=0,j=0;i<maximums.length;i++) {
                System.out.println("I: "+i+" J:"+j);
                if (maximums[i]!=-1) {
                    
                    sliders[j] = new JSlider(0,maximums[i],startValues[i]);
                    sliders[j].setMinorTickSpacing(1);
                    sliders[j].setMajorTickSpacing(3);
                    sliders[j].setSnapToTicks(true);
                    sliders[j].addChangeListener(new ChangeListener() {
                        public void stateChanged(ChangeEvent e) {
                            numPots = sliders[2].getValue();
                            numPray = sliders[0].getValue();
                            numFood = sliders[1].getValue();
                            food = Integer.parseInt(fi.getText());
                            mouse.setSpeed(sliders[4].getValue());
                            numDef = sliders[3].getValue();
                            useAgil = (agil.getSelectedIndex()!=0);
                            useSpec = (spec.getSelectedIndex()!=0);
                            useSameWeap = (spec.getSelectedIndex()==2);
                            int inventSpace = 21;
                            if (useAgil) inventSpace--;
                            if (useSpec&&!useSameWeap) inventSpace--;
                            if (numPots>inventSpace/2||numPots<0) {
                                numPots = 2;
                            }
                            inventSpace -= numPots*2;
                            if (numDef>inventSpace||numDef<0) {
                                numDef = 0;
                            }
                            inventSpace -= numDef;
                            if (numFood>inventSpace||numFood<0) {
                                numFood = (inventSpace>=2)?2:inventSpace;
                            }
                            inventSpace -= numFood;
                            if (numPray>inventSpace||numPray<0) {
                                numPray = inventSpace;
                            }
                            inventSpace -= numPray;
                            updateLabels(inventSpace);
                        }
                    });
                    c.gridy = i;
                    add(sliders[j],c);
                    j++;
                }
            }
            
            c.fill = 2;
            c.gridy = 9;
            c.gridx = 1;
            c.anchor = GridBagConstraints.EAST;
            String[] stopStrings = {"Never","On receiving clue","On banking with clue"};
            stop = new JComboBox(stopStrings);
            add(stop,c);
            
            c.gridy = 6;
            String[] prayStrings = {"Monastery","Falador altar","Don't use"};
            mona = new JComboBox(prayStrings);
            mona.setSelectedIndex(2);
            add(mona,c);
            
            c.gridy = 7;
            String[] agilStrings = {"No boosts","Use potion","Use pie"};
            agil = new JComboBox(agilStrings);
            agil.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    numPots = sliders[2].getValue();
                    numPray = sliders[0].getValue();
                    numFood = sliders[1].getValue();
                    food = Integer.parseInt(fi.getText());
                    mouse.setSpeed(sliders[4].getValue());
                    numDef = sliders[3].getValue();
                    useAgil = !(agil.getSelectedIndex()==0);
                    useSpec = (spec.getSelectedIndex()!=0);
                    useSameWeap = (spec.getSelectedIndex()==2);
                    int inventSpace = 21;
                    if (useAgil) inventSpace--;
                    if (useSpec&&!useSameWeap) inventSpace--;
                    if (numPots>inventSpace/2||numPots<0) {
                        numPots = 2;
                    }
                    inventSpace -= numPots*2;
                    if (numDef>inventSpace||numDef<0) {
                        numDef = 0;
                    }
                    inventSpace -= numDef;
                    if (numFood>inventSpace||numFood<0) {
                        numFood = (inventSpace>=2)?2:inventSpace;
                    }
                    inventSpace -= numFood;
                    if (numPray>inventSpace||numPray<0) {
                        numPray = inventSpace;
                    }
                    inventSpace -= numPray;
                    updateLabels(inventSpace);
                }
            });
            add(agil,c);
            
            c.gridy = 10;
            c.gridwidth = 1;
            String[] lvlStrings = {"Attack","Strength","Defence","Constitution","Don't stop"};
            lvl = new JComboBox(lvlStrings);
            lvl.setSelectedIndex(4);
            add(lvl,c);
            
            c.gridx = 2;
            lvlStop = new JTextField("99",2);
            add(lvlStop,c);
            
            
            c.gridy = 8;
            c.gridx = 1;
            String[] specStrings = {"Don't use","Use another weapon","Use this weapon"};
            spec = new JComboBox(specStrings);
            spec.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    numPots = sliders[2].getValue();
                    numPray = sliders[0].getValue();
                    numFood = sliders[1].getValue();
                    food = Integer.parseInt(fi.getText());
                    mouse.setSpeed(sliders[4].getValue());
                    numDef = sliders[3].getValue();
                    useAgil = (agil.getSelectedIndex()!=0);
                    useSpec = (spec.getSelectedIndex()!=0);
                    useSameWeap = (spec.getSelectedIndex()==2);
                    int inventSpace = 21;
                    if (useAgil) inventSpace--;
                    if (useSpec&&!useSameWeap) inventSpace--;
                    if (numPots>inventSpace/2||numPots<0) {
                        numPots = 2;
                    }
                    inventSpace -= numPots*2;
                    if (numDef>inventSpace||numDef<0) {
                        numDef = 0;
                    }
                    inventSpace -= numDef;
                    if (numFood>inventSpace||numFood<0) {
                        numFood = (inventSpace>=2)?2:inventSpace;
                    }
                    inventSpace -= numFood;
                    if (numPray>inventSpace||numPray<0) {
                        numPray = inventSpace;
                    }
                    inventSpace -= numPray;
                    updateLabels(inventSpace);
                }
            });
            add(spec,c);
            
            c.gridx = 2;
            specWepId = new JTextField("Id",4);
            add(specWepId,c);
            
            c.fill = 2;
            c.gridy = 11;
            c.gridx = 0;
            c.gridwidth = 3;
            c.anchor = GridBagConstraints.CENTER;
            JButton b1 = new JButton("Start");
            b1.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    numPots = sliders[2].getValue();
                    numPray = sliders[0].getValue();
                    numFood = sliders[1].getValue();
                    food = Integer.parseInt(fi.getText());
                    mouse.setSpeed(sliders[4].getValue());
                    numDef = sliders[3].getValue();
                    useAgil = (agil.getSelectedIndex()!=0);
                    usePot = (agil.getSelectedIndex()==1);
                    usePie = (agil.getSelectedIndex()==2);
                    useSpec = (spec.getSelectedIndex()!=0);
                    useSameWeap = (spec.getSelectedIndex()==2);
                    int inventSpace = 21;
                    if (useAgil) inventSpace--;
                    if (useSpec&&!useSameWeap) inventSpace--;
                    if (numPots>inventSpace/2||numPots<0) {
                        numPots = 2;
                    }
                    inventSpace -= numPots*2;
                    if (numDef>inventSpace||numDef<0) {
                        numDef = 0;
                    }
                    inventSpace -= numDef;
                    if (numFood>inventSpace||numFood<0) {
                        numFood = (inventSpace>=2)?2:inventSpace;
                    }
                    inventSpace -= numFood;
                    if (numPray>inventSpace||numPray<0) {
                        numPray = inventSpace;
                    }
                    inventSpace -= numPray;
                    hasClicked = true;
                    hasStarted = true;
                    stopWithClue = (stop.getSelectedIndex()!=0);
                    teleOnClue = (stop.getSelectedIndex()==1);
                    useMonastery = (mona.getSelectedIndex()==0);
                    useAltar = (mona.getSelectedIndex()!=2);
                    stopOnLevel = (lvl.getSelectedIndex()<4);
                    if (!useSameWeap&&useSpec) {
                        specWep = Integer.parseInt(specWepId.getText());
                    }
                    if (stopOnLevel) {
                        levelToStop = Integer.parseInt(lvlStop.getText());
                        stopSkill = lvl.getSelectedIndex();
                        if (stopSkill>3) {
                            stopOnLevel=false;
                        }
                        else {
                            stopSkill = skillOrder[stopSkill];
                        }
                    }
                }
            });
            add(b1,c);
            
            /*c.gridwidth = 1;
            c.gridy=6;
            c.gridx=1;
            c.anchor = GridBagConstraints.EAST;
            JButton b = new JButton("Start: Stop with clue");
            b.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    numPots = sliders[2].getValue();
                    numPray = sliders[0].getValue();
                    numFood = sliders[1].getValue();
                    food = Integer.parseInt(fi.getText());
                    mouse.setSpeed(sliders[4].getValue());
                    numDef = sliders[3].getValue();
                    int inventSpace = 21;
                    if (numPots>inventSpace/2||numPots<0) {
                        numPots = 2;
                    }
                    inventSpace -= numPots*2;
                    if (numDef>inventSpace||numDef<0) {
                        numDef = 0;
                    }
                    inventSpace -= numDef;
                    if (numFood>inventSpace||numFood<0) {
                        numFood = (inventSpace>=2)?2:inventSpace;
                    }
                    inventSpace -= numFood;
                    if (numPray>inventSpace||numPray<0) {
                        numPray = inventSpace;
                    }
                    inventSpace -= numPray;
                    hasClicked = true;
                    hasStarted = true;
                    stopWithClue = true;
                }
            });
            add(b,c);
            System.out.println("B gridx: "+c.gridx);*/

            c.gridx = 1;
            c.gridy = 2;
            c.gridwidth = 2;
            c.anchor = GridBagConstraints.EAST;
            fi = new JTextField("379",4);
            add(fi,c);
            
        }
        
        private void updateLabels(int inventSpace) {
            for (int i=0,j=0;i<maximums.length;i++) {
                if (maximums[i]!=-1) {
                    if (j!=4) {
                        maximums[i] = (inventSpace+sliders[j].getValue()*((j==2)?2:1))/((j==2)?2:1);
                        sliders[j].setMaximum(maximums[i]);
                        if (sliders[j].getValue()>=sliders[j].getMaximum()) sliders[j].setValue(sliders[j].getMaximum());
                    }
                    
                    labels[i].setText(titles[i]+": "+((sliders[j].getValue()<10)?"0":"")+(sliders[j].getValue()));
                    j++;
                }
            }
        }
    }
    
    class AboutPanel extends JPanel {
        AboutPanel() {
            setLayout(new BorderLayout());
            JTextArea jtf = new JTextArea("Created by Strikeskids\nTo use you must have 65 agility\nIt is recommended that you bring 2 food in case of emergencies\nSet quick prayers to protect item and protect melee\nHave antipoison, falador teleports, super pots (if using), prayer pots (if using), and food (if using) in one bank tab\nIf praying at monastery you must be wearing an ardy task cape\nHave all food and potions used, vials, and falador teleports in one bank tab. Have that bank tab selected",5,120);
            JScrollPane jsp = new JScrollPane(jtf);
            jtf.setEditable(false);
            add(jsp,BorderLayout.CENTER);
        }
    }
    
    private String formatDouble(double d, int places) {
        if (places<=0) {
            return ((int) d)+"";
        }
        int i = (int) d;
        double f = d-(double)i;
        f *= Math.pow(10,places);
        int e = (int) f;
        return i+"."+addPlaces(e,places);
    }
    
    private String addPlaces(int in, int len) {
        String ret = in+"";
        while (ret.length()<len) {
            ret = "0"+ret;
        }
        return ret;
    }

    public void onRepaint(Graphics g1) {
        Graphics2D g = (Graphics2D)g1;
        if (paintOpen) {
            long ranFor = System.currentTimeMillis()-startMillis;
            //Draw background
            
            g.setColor(color1);
            g.fillRect(7, 345, 489, 113);
            g.fillRect(7,459,505,14);
            g.setColor(color2);
            g.drawRect(7,458,505,0);
            String text = interfaces.getComponent(137,55).getText();
            String[] t = text.split("<col=0000ff>");
            g.setFont(font1);
            if (t.length>=2) {
                g.setColor(Color.BLUE);
                g.drawString((t.length<2)?"":t[1], 9, 470);
            }/*
            else {
                g.setColor(color2);
                g.drawString("Hounds killed: "+houndsKilled+" ("+formatDouble((double)houndsKilled*1000*60*60/(double)ranFor,2)+")",9,470);
            }*/
            g.setColor(color2);
            g.setFont(font2);
            g.drawString("Hellhound Striker", 356, 364);
            //Draw info
            g.setFont(font1);
            //Status
            g.drawString("Status: "+status, 8, 356);
            //Time
            g.drawString("Time Running: "+org.rsbot.script.util.Timer.format(ranFor), 8, 370);
            //Counters
            int count;
            String finish;
            for (int i=0;i<4;i++) {
                g.setColor(charmColors[i]);
                count = 0;
                if (game.getCurrentTab()==game.TAB_INVENTORY&&inventory.contains(charmId[i])) {
                    count = inventory.getCount(true,charmId[i]);
                }
                count += charmInBank[i];
                finish = charmNames[i];
                finish += " charms: ";
                finish += count;
                finish += " (";
                finish += formatDouble((double)count*1000*60*60/(double)ranFor,2);
                finish += ")";
                g.drawString(finish,8,384+14*i);
            }
            g.setColor(color2);
            count = 0;
            if (game.getCurrentTab()==game.TAB_INVENTORY&&inventory.contains("Starved ancient effigy")) {
                count = 1;
            }
            count += effigyInBank;
            g.drawString("Effigies: "+count, 8, 440);
            g.drawString("Clue scroll: "+((clueScroll)?"Yes":"Not yet"), 8, 454);
            //Draw skills
            g.setFont(font1);
            long gained;
            int curLvl;
            int gainedLvl;
            for (int i=0;i<skillOrder.length;i++) {
                gained = skills.getCurrentExp(skillOrder[i])-startXp[i];
                if (gained>0) {
                    g.setColor(color5);
                    g.fillRect(188, 368+i*18, 306, 13);
                    g.setColor(color7);
                    g.fillRect(188, 368+i*18, (int)(skills.getPercentToNextLevel(skillOrder[i])*306/100), 13);
                    g.setColor(color2);
                    curLvl = skills.getRealLevel(skillOrder[i]);
                    gainedLvl = curLvl-skills.getLevelAt(startXp[i]);
                    g.drawString(skillNames[i]+" "+curLvl+" ("+gainedLvl+"): "+gained+" ("+(long)(gained*1000*60*60/ranFor)+") TTL: "+org.rsbot.script.util.Timer.format(skills.getTimeTillNextLevel(skillOrder[i],startXp[i],ranFor)), 190, 378+i*18);
                }
            }
        }
        //Closing part
        g.setColor(color1);
        g.fillRect(477,438,17,18);
        g.setColor(color2);
        g.setFont(font3);
        g.drawString((paintOpen)?"X":"O", 482, 451);
        g.setStroke(stroke1);
        g.drawRect(476, 437, 18, 19);
    }
    
    public void mouseClicked(MouseEvent e) {
        
    }
    public void mouseEntered(MouseEvent e) {
        
    }
    public void mouseExited(MouseEvent e) {
        
    }
    public void mousePressed(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        if (x>476&&x<476+18&&y>437&&y<437+19) {
            paintOpen = !paintOpen;
        }
    }
    public void mouseReleased(MouseEvent e) {
        
    }

}