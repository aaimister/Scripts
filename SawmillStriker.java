import java.io.*;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.*;

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

@ScriptManifest(authors={"Strikeskids"}, name="SawmillStriker", description="Uses the sawmill to level woodcutting", version=1.23)

public class SawmillStriker extends Script implements PaintListener, MessageListener, MouseListener {
    
    long startMillis=-1;
    long addToStart=-1;
    long lastAnim = 0;
    boolean paintOpen=true;
    
    int jobBoard=46296;
    int logPile=46297;
    int logHopper=46304; 
    int plankHopper=46309;
    int workbench=46300;
    int cart=46303;
    int overseer=8904;
    
    int last = 123;
    String threadLoc = "http://adf.ly/2VRY1";
    RSTile logPileLoc= new RSTile(3317,3495);
    RSTile logHopperLoc = new RSTile(3323,3501);
    RSTile plankHopperLoc = new RSTile(3324,3495);
    RSTile workbenchLoc = new RSTile(3323,3491);
    RSTile jobBoardLoc = new RSTile(3315,3493);
    RSTile cartLoc = new RSTile(3318,3492);
    RSTile overseerLoc = new RSTile(3316,3491);
    
    RSArea sawmillArea = new RSArea(new RSTile[]{new RSTile(3315,3488), new RSTile(3315,3498), new RSTile(3319,3503), new RSTile(3326,3503), new RSTile(3326,3488)});
    RSArea houseArea = new RSArea(new RSTile(3312,3490), new RSTile(3314,3494));
    
    int jobInterface = 766;
    int[] jobNeededComponent = {102,93,83,73,63,53};
    int[] jobCurrentComponent = {100,91,81,71,61,51};
    int makeInterface = 902;
    int[] makeComponent = {36,30,24,19};
    int makeCloseComponent = 54;
    
    int cartInterface = 903;
    int[] cartComponent = {76,68,58,50,42,34};
    int cartCloseComponent = 18;
    
    int depositCartInterface = 771;
    
    String getJobAction = "Take-job(large)";
    String takeLogsAction = "Take-logs(many)";
    String putLogsAction = "Load-logs(many)";
    String takePlanksAction = "Take-plank(many)";
    String workAction = "Cut";
    String useCartAction = "Inspect";
    String finishJobAction = "Finish";
    
    int log=1511;
    int plank=15291;
    int[] plankId = {15292,15293,15294,15295,15296,15297};
    int[] numNeeded = new int[6];
    int[] numOnCart = new int[6];
    boolean jobOpen=true;
    
    boolean planksAvailable=true;
    boolean cuttingUntilFull=false;
    int val=-1;
    
    int logsPicked=0;
    int planksPicked=0;
    int[] planksCut=new int[4];
    int[] planksRecieved = new int[4];
    int[] xpForPlanks=new int[4];
    long[] cuttingFor=new long[4];
    int cuttingPlank=-1;
    int startingPlanks;
    int xpBeforePlanks;
    int planksBeforeCutting;
    long startedCuttingTime;
    
    boolean finishAction = false;
    
    String status="Starting up...";
    
    RSTile hoverTile;
    
    boolean justMoved;
    boolean hasFinished;
    
    boolean showTime=true;
    int laps = 0;
    
    int startXp;
    int skillId=skills.WOODCUTTING;
    
    private final Color color1 = new Color(202, 184, 150);
    private final Color color2 = new Color(0, 0, 0);
    private final Color color3 = new Color(255, 0, 51);
    private final Color color4 = new Color(0, 255, 0);
    private final Color color5 = new Color(0, 0, 255);
    
    private final BasicStroke stroke1 = new BasicStroke(1);
    
    private final Font font1 = new Font("Courier New", 0, 12);
    private final Font font2 = new Font("Arial", 0, 18);
    private final Font font3 = new Font("Courier New", 1, 12);
    
    private final Color color6 = new Color(32, 28, 22);
    private final Color color7 = new Color(255, 205, 0);
    
    enum TaskEnum {
        WAIT_FOR_ANIMATION,
        NOTHING,
        GET_JOB,
        TAKE_LOGS,
        PUT_LOGS,
        TAKE_PLANKS,
        USE_TABLE,
        CRAFT_PLANKS,
        USE_CART,
        DEPOSIT_PLANKS,
        WITHDRAW_PLANKS,
        FINISH_JOB,
        GET_TO_AREA,
        DROP_PLANKS,
        PICK_PLANKS,
        CLOSE_CART,
        LOGIN,
        LOBBY
    };
    
    
    public boolean onStart() {
        if (getVersion()!=thisVersion()) {
            update();
            return false;
        }
        /*GUI g = new GUI();
        g.show();
        while (!hasClicked&&g.getFrame().isVisible()) sleep(100);
        g.hide();
        g.setFrame(null);*/
        startMillis = System.currentTimeMillis()-1;
        addToStart = startMillis;
        if (!env.disableRandom("Improved Login")) {
            env.disableRandoms();
            log("Disable all randoms");
        }
        if (game.isLoggedIn()) {
            camera.setAngle(90);
            camera.setPitch(true);
        }
        startXp = skills.getCurrentExp(skillId);
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
            BufferedReader r = new BufferedReader(new InputStreamReader(new URL("http://strikeskids.com/ScriptVersions/SawmillStrikerVersion.txt").openStream()));
            int v = Integer.parseInt(r.readLine());
            r.close();
            return v;
        }
        catch (Exception ex) {
            log("Cannot read version file");
        }
        return 0;
    }
    
    private int thisVersion() {
        return last;
    }
    
    public void onFinish() {
        paintOpen = true;
        log("Status at end was: "+status);
    }
    
    public int loop() {
        if (startMillis!=-1) startMillis += System.currentTimeMillis()-addToStart;
        addToStart = System.currentTimeMillis();
        int value = looper();
        if (value==-1) value=500;
        else addToStart = System.currentTimeMillis()+value;
        return value;
    }
    
    private int looper() {
        if (interfaces.get(901).isValid()) {
            interfaces.getComponent(901,18).doClick();
        }
        refreshVars();
        TaskEnum task = getTask();
        if (task!=TaskEnum.WAIT_FOR_ANIMATION) {
            cuttingUntilFull = false;
            finishAction = false;
        }
        if (game.isLoggedIn()) {
            camera.setAngle(90);
            camera.setPitch(true);
        }
        switch (task) {
            case GET_TO_AREA:
                if (houseArea.contains(getMyPlayer().getLocation())) {
                    RSObject door = objects.getNearest(46306);
                    if (door!=null) {
                        tiles.interact(door.getLocation(),-.1,.5,0,"Open");
                    }
                    else {
                        walking.walkTileMM(logPileLoc);
                    }
                }
                else {
                    RSObject door = objects.getNearest(46307);
                    if (door!=null) {
                        if (door.isOnScreen()) {
                            tiles.interact(door.getLocation(),.9,.5,0,"Open");
                        }
                        else {
                            walking.walkTileMM(door.getLocation());
                        }
                    }
                }
                break;
            case WAIT_FOR_ANIMATION:
                if (cuttingUntilFull) {
                    RSObject w = objects.getNearest(workbench);
                    if (isTypeDone(val)) {
                        cuttingUntilFull = false;
                        if (sufficientPlanks()) {
                            mouse.hop(calc.tileToMinimap(cartLoc));
                            mouse.click(true);
                        }
                        else if (inventory.getCount(plank)!=0) {
                            if (w!=null&&w.isOnScreen()) {
                                tiles.interact(w.getLocation(),workAction);
                                hoverTile = null;
                            }
                            else {
                                walking.walkTileMM(workbenchLoc);
                                hoverTile = workbenchLoc;
                            }
                        }
                        return 700;
                    }
                }
                antiBan();
                if (hoverTile!=null&&(tiles.getTileUnderMouse()==null||!tiles.getTileUnderMouse().equals(hoverTile))&&calc.tileOnScreen(hoverTile)&&!justMoved) {
                    tiles.doHover(hoverTile);
                }
                return 300;
            case GET_JOB:
                logm("Get new job");
                hasFinished = false;
                RSObject o = objects.getNearest(jobBoard);
                if (o!=null&&o.isOnScreen()) {
                    //tiles.interact(o.getLocation(),0,.5,1,getJobAction);
                    o.interact(getJobAction);
                    hoverTile = cartLoc;
                }
                else {
                    walking.walkTileMM(jobBoardLoc);
                    hoverTile = null;
                }
                antiBan();
                return 700;
            case TAKE_LOGS:
                logm("Take the logs");
                o = objects.getNearest(logPile);
                if (o!=null&&o.isOnScreen()) {
                    o.interact(takeLogsAction);
                    hoverTile = logHopperLoc;
                }
                else {
                    walking.walkTileMM(logPileLoc);
                    hoverTile = null;
                }
                antiBan();
                return 1300;
            case PUT_LOGS:
                logm("Split the logs");
                planksAvailable = true;
                o = objects.getNearest(logHopper);
                if (o!=null&&o.isOnScreen()) {
                    tiles.interact(logHopperLoc,.5,.5,1,putLogsAction);
                    hoverTile = plankHopperLoc;

                }
                else {
                    walking.walkTileMM(logHopperLoc);
                    hoverTile = null;
                }
                antiBan();
                return 1300;
            case TAKE_PLANKS:
                logm("Take the planks");
                o = objects.getNearest(plankHopper);
                if (o!=null&&o.isOnScreen()) {
                    tiles.interact(o.getLocation(),.5,.7,0,takePlanksAction);
                }
                else {
                    walking.walkTileMM(plankHopperLoc);
                }
                hoverTile = null;
                antiBan();
                return 1300;
            case USE_TABLE:
                logm("Start crafting");
                o = objects.getNearest(workbench);
                if (o!=null&&o.isOnScreen()) {
                    o.interact(workAction);
                }
                else {
                    walking.walkTileMM(workbenchLoc);
                }
                hoverTile = null;
                return 1300;
            case USE_CART:
                logm("Inspect the cart");
                o = objects.getNearest(cart);
                if (o!=null&&calc.tileOnScreen(cartLoc)) {
                    o.interact(useCartAction);
                }
                else {
                    walking.walkTileMM(cartLoc);
                }
                hoverTile = null;
                return 1300;
            case FINISH_JOB:
                logm("Finish the job");
                updatePlankVals();
                if (!hasFinished)  {
                    laps++;
                    showTime = true;
                }
                if (interfaces.get(228).isValid()) {
                    interfaces.getComponent(228,2).interact("Continue");
                    return 700;
                }
                RSNPC z = npcs.getNearest(overseer);
                if (z!=null&&z.isOnScreen()) {
                    z.interact(finishJobAction);
                }
                else {
                    walking.walkTileMM(overseerLoc);
                }
                antiBan();
                hoverTile = null;
                hasFinished = true;
                return 700;
            case WITHDRAW_PLANKS:
                logm("Take the planks");
                withdrawFromCart(0,getNumExtra(0));
                withdrawFromCart(1,getNumExtra(0));
                withdrawFromCart(2,getNumExtra(2));
                withdrawFromCart(3,getNumExtra(1));
                withdrawFromCart(4,getNumExtra(1));
                withdrawFromCart(5,getNumExtra(3));
                interfaces.getComponent(cartInterface,cartCloseComponent).interact("Close");
                antiBan();
                hoverTile = null;
                return 700;
            case DEPOSIT_PLANKS:
                logm("Put the planks");
                for (int i=0;i<plankId.length;i++) {
                    for (int k=0;k<28;k++) {
                        if (interfaces.getComponent(771,0)==null||interfaces.getComponent(771,0).getComponent(k)==null) {
                            log("Interfaces are null");
                            continue;
                        }
                        if (interfaces.getComponent(771,0).getComponent(k).getComponentID()==plankId[i]) {
                            if (inventory.getCount(interfaces.getComponent(771,0).getComponent(k).getComponentID())==1) {
                                interfaces.getComponent(771,0).getComponent(k).doClick();
                            }
                            else {
                                if (interfaces.getComponent(771,0)!=null&&interfaces.getComponent(771,0).getComponent(k)!=null) {
                                    interfaces.getComponent(771,0).getComponent(k).interact("Store-All");
                                }
                                else {
                                    log("Interfaces are null");
                                }
                            }
                            break;
                        }
                    }
                }
                sleep(700);
                logm("Close the interface");
                interfaces.getComponent(cartInterface,cartCloseComponent).doClick();
                antiBan();
                hoverTile = null;
                return 700;
            case CRAFT_PLANKS:
                logm("Craft the planks");
                updatePlankVals();
                startingPlanks = inventory.getCount(plank);
                xpBeforePlanks = skills.getCurrentExp(skillId);
                long startedCuttingTime = System.currentTimeMillis()-startMillis;
                if (numNeeded[5]>numOnCart[5]) {
                    cuttingPlank = 3;
                    planksBeforeCutting = numOnCart[5];
                    craftPlanks(numNeeded[5]-numOnCart[5],3);
                }
                else if (numNeeded[4]>numOnCart[4]||numNeeded[3]>numOnCart[3]) {
                    cuttingPlank = 1;
                    planksBeforeCutting = numOnCart[4]+numOnCart[3];
                    craftPlanks(4,3,1);
                }
                else if (numNeeded[2]>numOnCart[2]) {
                    cuttingPlank = 2;
                    planksBeforeCutting = numOnCart[2];
                    craftPlanks((numNeeded[2]-numOnCart[2])/2+1,2);
                }
                else if (numNeeded[0]>numOnCart[0]||numNeeded[1]>numOnCart[1]) {
                    cuttingPlank = 0;
                    planksBeforeCutting = numOnCart[0]+numOnCart[1];
                    craftPlanks(0,1,0);
                }
                else {
                    cuttingPlank = -1;
                    interfaces.getComponent(makeInterface,makeCloseComponent).interact("Close");
                }
                antiBan();
                hoverTile = workbenchLoc;
                break;
            case DROP_PLANKS:
                logm("Dropping extra planks");
                if (inventory.contains(plankId[4])) {
                    for (int i=0;i<28;i++) {
                        if (!inventory.contains(plankId[4])) break;
                        if (inventory.getItemAt(i)!=null&&inventory.getItemAt(i).getID()==plankId[4]) inventory.getItemAt(i).interact("Drop");
                    }
                    return 700;
                }
                for (int i=0;i<28;i++) {
                    if (!needToDrop()) break;
                    if (inventory.getItemAt(i)!=null&&inventory.getItemAt(i).getID()==plank) inventory.getItemAt(i).interact("Drop");
                }
                hoverTile = null;
                return 100;
            case PICK_PLANKS:
                logm("Picking up planks");
                hoverTile = null;
                RSGroundItem g = groundItems.getNearest(plank);
                if (!sawmillArea.contains(g.getLocation())) return 100;
                if (g.isOnScreen()) {
                    g.interact("Take Plank");
                    return 500;
                }
                else {
                    walking.walkTileMM(g.getLocation());
                    return 700;
                }
            case CLOSE_CART:
                interfaces.getComponent(cartInterface,cartCloseComponent).interact("Close");
                return 700;
            case LOBBY:
                logm("Login");
                if (!interfaces.get(906).isValid()) return -1;
                interfaces.getComponent(906,171).doClick();
                return -1;
            case LOGIN:
                logm("Login");
                if (!interfaces.get(596).isValid()) return -1;
                RSComponent userComp = interfaces.getComponent(596,73);
                String userNameVal = userComp.getText();
                if (userNameVal.compareTo(account.getName())!=0) {
                    userComp.doClick();
                    sleep(300);
                    while (userComp.getText().length()>0) {
                        keyboard.sendText("\b",false);
                        sleep(100);
                    }
                    sleep(300);
                    keyboard.sendText(account.getName(),false);
                    return -1;
                }
                RSComponent passComp = interfaces.getComponent(596,79);
                int passLength = passComp.getText().length();
                if (passLength!=account.getPassword().length()) {
                    passComp.doClick();
                    sleep(300);
                    while (passComp.getText().length()>0) {
                        keyboard.sendText("\b",false);
                        sleep(100);
                    }
                    sleep(300);
                    keyboard.sendText(account.getPassword(),false);
                    return -1;
                }
                RSComponent loginComp = interfaces.getComponent(596,60);
                loginComp.doClick();
                return -1;
        }
        return 1500;
    }
    
    private TaskEnum getTask() {
        if (!game.isLoggedIn()) {
            if (interfaces.get(906).isValid()) return TaskEnum.LOBBY;
            return TaskEnum.NOTHING;
            //return TaskEnum.LOGIN;
        }
        if (((getMyPlayer().getAnimation()!=-1||getMyPlayer().isMoving()||System.currentTimeMillis()-lastAnim<1000)&&!finishAction)) return TaskEnum.WAIT_FOR_ANIMATION;
        if (!sawmillArea.contains(getMyPlayer().getLocation())) return TaskEnum.GET_TO_AREA;
        if (!jobOpen) return TaskEnum.GET_JOB;
        if (isWorkbenchOpen()) return TaskEnum.CRAFT_PLANKS;
        if (readyToFinishJob()||(sufficientPlanks()&&getExtraPlanks()==0)) {
            if (isCartOpen()) return TaskEnum.CLOSE_CART;
            return TaskEnum.FINISH_JOB;
        }
        if (sufficientPlanks()) {
            //Sufficient planks to finish job
            if (isCartOpen()) return TaskEnum.WITHDRAW_PLANKS;
            else if (needToDrop()) return TaskEnum.DROP_PLANKS;
            else return TaskEnum.USE_CART;
        }
        if (inventory.containsOneOf(plankId)) {
            //There are still planks
            if (!inventory.contains(plankId[4])) {
                if (isCartOpen()) return TaskEnum.DEPOSIT_PLANKS;
                else return TaskEnum.USE_CART;
            }
            else {
                return TaskEnum.DROP_PLANKS;
            }
        }
        if (isCartOpen()) return TaskEnum.CLOSE_CART;
        if (groundItems.getNearest(plank)!=null&&sawmillArea.contains(groundItems.getNearest(plank).getLocation())) return TaskEnum.PICK_PLANKS;
        if (inventory.contains(log)) {
            if (calc.distanceTo(logPileLoc)<calc.distanceTo(logHopperLoc)) {
                //Nearer to log pile
                if (inventory.isFull()) return TaskEnum.PUT_LOGS;
                else return TaskEnum.TAKE_LOGS;
            }
            else {
                //Nearer to hopper
                return TaskEnum.PUT_LOGS;
            }
        }
        if (inventory.contains(plank)) {
            if (inventory.isFull()&&planksAvailable&&!finishAction) planksLeft();
            return TaskEnum.USE_TABLE;
        }
        if (planksLeft()) {
            return TaskEnum.NOTHING;
        }
        else {
            return TaskEnum.TAKE_LOGS;
        }
        //return TaskEnum.NOTHING;
    }
                            
                    
    
    private boolean needToDrop() {
        if (28-inventory.getCount()>=getExtraPlanks()) return false;
        return true;
    }
    
    private boolean canTakeMore() {
        if (inventory.isFull()) return false;
        int sum=0;
        for (int i=0;i<6;i++) {
            if (i!=4&&i!=3) sum += numOnCart[i]-numNeeded[i];
        }
        return sum>0;
    }
    
    private int getExtraPlanks() {
        int ret=0;
        for (int i=0;i<4;i++) {
            ret += getNumExtra(i);
        }
        return ret;
    }

    private int getNumExtra(int id) {
        int a = 1000, b = 1000;
        switch (id) {
            case 0:
                a = numOnCart[0]-numNeeded[0];
                b = numOnCart[1]-numNeeded[1];
                break;
            case 1:
                a = numOnCart[3]-numNeeded[3];
                b = numOnCart[4]-numNeeded[4];
                break;
            case 2:
                a = numOnCart[2]-numNeeded[2];
                break;
            case 3:
                a = numOnCart[5]-numNeeded[5];
                break;
        }
        return (a>b)?b:a;
    }
                            
    private boolean isTypeDone(int t) {
        switch (t) {
            case 0:
                return (numNeeded[0]<=numOnCart[0])&&(numNeeded[1]<=numOnCart[1]);
            case 1:
                return (numNeeded[3]<=numOnCart[3])&&(numNeeded[4]<=numOnCart[4]);
            case 2:
                return (numNeeded[2]<=numOnCart[2]);
            case 3:
                return (numNeeded[5]<=numOnCart[5]);
        }
        return false;
    }
    
    
    private void refreshPlankVals() {
        if (cuttingPlank!=-1) {
            int cachedPlankInventCount = getCachedCount(plank);
            planksCut[cuttingPlank] += startingPlanks-cachedPlankInventCount;
            xpForPlanks[cuttingPlank] += skills.getCurrentExp(skillId)-xpBeforePlanks;
            cuttingFor[cuttingPlank] += System.currentTimeMillis()-(startedCuttingTime+startMillis);
            switch (cuttingPlank) {
                case 0:
                    planksRecieved[0] += (numOnCart[0]+numOnCart[1])-planksBeforeCutting;
                    planksBeforeCutting = (numOnCart[0]+numOnCart[1]);
                    break;
                case 1:
                    planksRecieved[1] += (numOnCart[3]+numOnCart[4])-planksBeforeCutting;
                    planksBeforeCutting = (numOnCart[3]+numOnCart[4]);
                    break;
                case 2:
                    planksRecieved[2] += (numOnCart[2])-planksBeforeCutting;
                    planksBeforeCutting = (numOnCart[2]);
                    break;
                case 3:
                    planksRecieved[3] += (numOnCart[5])-planksBeforeCutting;
                    planksBeforeCutting = (numOnCart[5]);
                    break;
            }
            startedCuttingTime = System.currentTimeMillis()-startMillis;
            startingPlanks = cachedPlankInventCount;
            xpBeforePlanks = skills.getCurrentExp(skillId);
        }
    }
    
    private int getCachedCount(int item) {
        RSItem[] invent = inventory.getItems(true);
        int ret = 0;
        for (int i=0;i<28;i++) {
            if (invent[i]!=null&&invent[i].getID()==item) ret++;
        }
        return ret;
    }
    
    private void updatePlankVals() {
        if (cuttingPlank!=-1) {
            planksCut[cuttingPlank] += startingPlanks-getCachedCount(plank);
            xpForPlanks[cuttingPlank] += skills.getCurrentExp(skillId)-xpBeforePlanks;
            cuttingFor[cuttingPlank] += System.currentTimeMillis()-(startedCuttingTime+startMillis);
            switch (cuttingPlank) {
                case 0:
                    planksRecieved[0] += (numOnCart[0]+numOnCart[1])-planksBeforeCutting;
                    break;
                case 1:
                    planksRecieved[1] += (numOnCart[3]+numOnCart[4])-planksBeforeCutting;
                    break;
                case 2:
                    planksRecieved[2] += (numOnCart[2])-planksBeforeCutting;
                    break;
                case 3:
                    planksRecieved[3] += (numOnCart[5])-planksBeforeCutting;
                    break;
            }
            startingPlanks = 0;
            xpBeforePlanks = 0;
            planksBeforeCutting = 0;
            cuttingPlank = -1;
            startedCuttingTime = 0;
        }
    }
    
    private void craftPlanks(int num, int in) {
        RSComponent inComp = interfaces.getComponent(makeInterface,makeComponent[in]);
        cuttingUntilFull = true;
        val = in;
        inComp.interact("Make All");
    }
    
    private void withdrawFromCart(int id, int num) {
        RSComponent c = interfaces.getComponent(cartInterface, cartComponent[id]);
        if (num<=0) return;
        if (num<5) {
            for (int i=0;i<num;i++) {
                c.interact("Withdraw 1");
            }
        }
        else if (num==5) {
            c.interact("Withdraw 5");
        }
        else if (num==10) {
            c.interact("Withdraw 10");
        }
        else {
            String before = interfaces.getComponent(752,5).getText();
            c.interact("Withdraw X");
            sleep(2000);
            keyboard.sendText(num+"",true);
            if (interfaces.getComponent(752,5).getText().compareTo("")==0) {
                sleep(2000);
                keyboard.sendText(num+"",true);
            }
        }
    }
    
    private void logm(String s) {
        status = s;
    }
    
    private void craftPlanks(int p1, int p2, int in) {
        int am1,am2;
        am1 = numNeeded[p1]-numOnCart[p1];
        am2 = numNeeded[p2]-numOnCart[p2];
        if (am1>am2) craftPlanks(am1,in);
        else craftPlanks(am2,in);
    }
    
    private boolean planksLeft() {
        updatePlankVals();
        if (!planksAvailable) return false;
        RSObject o = objects.getTopAt(plankHopperLoc);
        if (o==null) return false;
        if (!o.isOnScreen()) {
            walking.walkTileMM(plankHopperLoc);
            planksAvailable = true;
            return true;
        }
        planksAvailable = o.interact(takePlanksAction);
        if (planksAvailable) {
            hoverTile = workbenchLoc;
            logm("Take planks");
        }
        return planksAvailable;
    }
    
    private String arrayToString(String[] in) {
        if (in.length==0) return "No elements";
        String ret = "";
        for (int i=0;i<in.length;i++) {
            ret += in[i]+",";
        }
        return ret.substring(0,ret.length()-2);
    }
                    
    private boolean isWorkbenchOpen() {
        return interfaces.get(makeInterface).isValid();
    }

    private boolean isCartOpen() {
        return interfaces.getComponent(depositCartInterface,0).isValid();
    }
    
    private void refreshVars() {
        if (interfaces.get(jobInterface).isValid()) {
            jobOpen = true;
            String cur;
            for (int i=0;i<plankId.length;i++) {
                numOnCart[i] = Integer.parseInt(interfaces.getComponent(jobInterface,jobCurrentComponent[i]).getText());
                numNeeded[i] = Integer.parseInt(interfaces.getComponent(jobInterface,jobNeededComponent[i]).getText());
            }
        }
        else {
            jobOpen = false;
        }
    }
    
    private boolean sufficientPlanks() {
        if (!jobOpen) return false;
        for (int i=0;i<plankId.length;i++) {
            if (numNeeded[i]>numOnCart[i]) return false;
        }
        return true;
    }
    
    private boolean readyToFinishJob() {
        if (!jobOpen) return false;
        for (int i=0;i<plankId.length;i++) {
            if (numNeeded[i]!=numOnCart[i]) return false;
        }
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
        String msg = e.getMessage().toLowerCase();
        if (msg.contains("is full")||msg.contains("out of planks")||msg.contains("no logs to put")) {
            finishAction = true;
            logm("Finished action");
        }
    }
    
    class GUI {
        JFrame frame;
        GUI() {
            frame = new JFrame("Sawmill Striker");
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
        OptionPanel() {
            setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
        }
    }
    
    class AboutPanel extends JPanel {
        AboutPanel() {
            setLayout(new BorderLayout());
            JTextArea jtf = new JTextArea("Created by Strikeskids\nTo use you must have 70 agility\nIt is recommended that you bring 2 food in case of emergencies\nSet quick prayers to protect item and protect melee\nHave antipoison, falador teleports, super pots (if using), prayer pots (if using), and food (if using) in one bank tab\nIf praying at monastery you must be wearing an ardy task cape\nHave all food and potions used, vials, and falador teleports in one bank tab. Have that bank tab selected",5,120);
            JScrollPane jsp = new JScrollPane(jtf);
            jtf.setEditable(false);
            add(jsp,BorderLayout.CENTER);
        }
    }
    
    private void paintStrings(String[] in, Graphics2D g, int x, int y, int offset) {
        for (int i=0;i<in.length;i++) {
            g.drawString(in[i], x, y+offset*i);
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
    
    private boolean arePlanksCut(int loc) {
        switch (loc) {
            case 0:
                return planksCut[0]>0;
            case 1:
                return planksCut[0]>0;
            case 2:
                return planksCut[2]>0;
            case 3:
                return planksCut[1]>0;
            case 4:
                return planksCut[1]>0;
            case 5:
                return planksCut[3]>0;
        }
        return false;
    }
    
    private int numPlanksCut(int loc) {
        switch (loc) {
            case 0:
                return planksCut[0];
            case 1:
                return planksCut[0];
            case 2:
                return planksCut[2];
            case 3:
                return planksCut[1];
            case 4:
                return planksCut[1];
            case 5:
                return planksCut[3];
        }
        return 0;
    }
    
    private int numPlanksLeft(int loc) {
        return numNeeded[loc]-numOnCart[loc];
    }
    
    private String getPlankRatio(int loc) {
        if (loc==-1) return formatDouble((double)sumArray(planksRecieved)/(double)sumArray(planksCut),2);
        return formatDouble((double)planksRecieved[loc]/(double)planksCut[loc],2);
    }
    
    private String getPlankXpRatio(int loc) {
        if (loc==-1) return formatDouble((double)sumArray(xpForPlanks)/(double)sumArray(planksCut),2);
        return formatDouble((double)xpForPlanks[loc]/(double)planksCut[loc],2);
    }
    
    private long getTimeCuttingFor(int loc) {
        switch (loc) {
            case 0:
                return cuttingFor[0];
            case 1:
                return cuttingFor[0];
            case 2:
                return cuttingFor[2];
            case 3:
                return cuttingFor[1];
            case 4:
                return cuttingFor[1];
            case 5:
                return cuttingFor[3];
        }
        return 10000000;
    }
    
    public int sumArray(int[] in) {
        int ret=0;
        for (int i=0;i<in.length;i++) ret+=in[i];
        return ret;
    }

    public void onRepaint(Graphics g1) {
        if (getMyPlayer().getAnimation()!=-1) lastAnim = System.currentTimeMillis();
        refreshVars();
        Graphics2D g = (Graphics2D)g1;
        if (paintOpen) {
            long ranFor = System.currentTimeMillis()-startMillis;
            g.setColor(color1);
            g.fillRect(7, 345, 489, 113);
            g.setFont(font1);
            g.setColor(color2);
            String[] parts = new String[8];
            parts[0] = "Status: "+status;
            parts[1] = "Time running: "+org.rsbot.script.util.Timer.format(ranFor);
            parts[2] = "Laps completed: "+laps+((showTime)?" ("+formatDouble((double)laps*1000*60*60/(double)ranFor,2)+")":"");
            parts[3] = "";
            refreshPlankVals();
            String[] plankNames = {"Short/Long","Tooth/Groove","Diagonal","Curved"};
            for (int i=0;i<4;i++) {
                if (planksCut[i]>0) parts[i+3] = plankNames[i]+": "+planksRecieved[i]+" ["+planksCut[i]+"] ("+getPlankRatio(i)+") "+xpForPlanks[i]+" ("+getPlankXpRatio(i)+")";
                else parts[i+3] = plankNames[i]+":";
            }
            int cutSum = sumArray(planksCut);
            if (cutSum>0) parts[7] = "Total: "+sumArray(planksRecieved)+" ["+cutSum+"] ("+getPlankRatio(-1)+") "+sumArray(xpForPlanks)+" ("+getPlankXpRatio(-1)+")";
            else parts[7] = "Total:";
            paintStrings(parts,g,8,356,14);
            //g.drawString("Middle Point", 254, 403);
            g.setFont(font2);
            g.drawString("Sawmill Striker", 376, 364);
            //Draw skill
            String text = interfaces.getComponent(137,55).getText();
            String[] t = text.split("<col=0000ff>");
            long gained = skills.getCurrentExp(skillId)-startXp;
            if (t.length<2&&gained>0) {
                int curLvl = skills.getRealLevel(skillId);
                int gainedLvl = curLvl-skills.getLevelAt(startXp);
                g.setColor(color3);
                g.fillRect(7, 459, 505, 14);
                g.setColor(color4);
                g.fillRect(7, 459, (int)(skills.getPercentToNextLevel(skillId)*505/100), 14);
                g.setFont(font1);
                g.setColor(color2);
                if (showTime) g.drawString("Woodcutting "+curLvl+" ("+gainedLvl+"): "+gained+" ("+((long)(gained*1000*60*60/ranFor))+") TTL: "+org.rsbot.script.util.Timer.format(skills.getTimeTillNextLevel(skillId,startXp,ranFor)), 9, 470);
                else g.drawString("Woodcutting "+curLvl+" ("+gainedLvl+"): "+gained, 9, 470);
            }
            else {
                g.setFont(font1);
                g.setColor(color1);
                g.fillRect(7,459,505,14);
                g.setColor(color5);
                g.drawString((t.length<2)?"":t[1], 9, 470);
            }
            g.setColor(color2);
            g.drawRect(7,458,505,0);
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
    
    private final Font font4 = new Font("GB18030 Bitmap", 0, 11);
    
    private void drawPlankTimer(Graphics2D g) {
        if (showTime) {
            int[] yLoc = {54,71,88,105,122,140};
            g.setColor(color6);
            g.setFont(font4);
            for (int i=0;i<6;i++) {
                if (arePlanksCut(i)&&numPlanksLeft(i)>0) {
                    g.setColor(color6);
                    g.fillRoundRect(93,yLoc[i],45,12,5,16);
                    g.setColor(color7);
                    long timeLeft=0;
                    if (numPlanksLeft(i)>0) {
                        int numCut = numPlanksCut(i);
                        if (i!=2&&i!=5) numCut /= 2;
                        timeLeft = numPlanksLeft(i)*getTimeCuttingFor(i)/numCut;
                    }
                    g.drawString(org.rsbot.script.util.Timer.format(timeLeft),97,yLoc[i]+10);
                }
            }
        }
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
    
    private void antiBan() {
        //doAntiban();
    }
    
    private void doAntiban() {
        if (random(1,7)==4) {
            //Going to do antiban
            switch(random(0,2)) {
                case 0:
                    if (random(0,7)==2) {
                        game.openTab(game.TAB_STATS);
                        sleep(1000);
                        skills.doHover(skills.INTERFACE_WOODCUTTING);
                        sleep(1000);
                        game.openTab(game.TAB_INVENTORY);
                    }
                    justMoved=true;
                    break;
                case 1:
                    mouse.moveRandomly(100);
                    justMoved=true;
                    break;
            }
        }
        if (random(1,5)==3) justMoved = false;
    }

}