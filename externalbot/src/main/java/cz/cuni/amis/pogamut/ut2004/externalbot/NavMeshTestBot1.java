package cz.cuni.amis.pogamut.ut2004.externalbot;

import cz.cuni.amis.introspection.java.JProp;
import cz.cuni.amis.pogamut.base.agent.navigation.IPathExecutorState;
import cz.cuni.amis.pogamut.base.agent.navigation.IPathFuture;
import cz.cuni.amis.pogamut.base.agent.navigation.IPathPlanner;
import cz.cuni.amis.pogamut.base.agent.utils.runner.impl.AgentRunner;
import cz.cuni.amis.pogamut.base.communication.worldview.event.IWorldEventListener;
import cz.cuni.amis.pogamut.base.utils.guice.AgentScoped;
import cz.cuni.amis.pogamut.base3d.worldview.object.ILocated;
import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;
import cz.cuni.amis.pogamut.ut2004.agent.module.utils.TabooSet;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.UT2004EdgeChecker;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.astar.UT2004AStar;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.floydwarshall.FloydWarshallMap;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.navmesh.NavMesh;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004Bot;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Initialize;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.BotKilled;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.ConfigChange;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.GameInfo;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.InitedMessage;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.LocationUpdate;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.MapListEnd;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPointNeighbourLink;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Self;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.SelfMessage;
import cz.cuni.amis.pogamut.ut2004.communication.translator.shared.events.MapListObtained;
import cz.cuni.amis.pogamut.ut2004.communication.translator.shared.events.MapPointListObtained;
import cz.cuni.amis.pogamut.ut2004.navigation.evaluator.bot.EvaluatingBot;
import cz.cuni.amis.pogamut.ut2004.navigation.evaluator.bot.NavigationFactory;
import cz.cuni.amis.pogamut.ut2004.utils.UT2004BotRunner;
import cz.cuni.amis.utils.exception.PogamutException;
import cz.cuni.amis.utils.flag.FlagListener;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Pogamut's "Hello world!" example showing few extra things such as
 * introspection and various bot-initializing methods.
 * <p>
 * <p>
 * First, try to run the bot and kill it... than uncomment the line inside
 * {@link EmptyBot#botKilled(BotKilled)} and run the bot again kill it to see
 * the difference.
 *
 * @author Michal Bida aka Knight
 * @author Rudolf Kadlec aka ik
 * @author Jakub Gemrot aka Jimmy
 */
@AgentScoped
public class NavMeshTestBot1 extends EvaluatingBot {
    
    @JProp
    public String stringProp = "Hello bot example";
    @JProp
    public boolean boolProp = true;
    @JProp
    public int intProp = 2;
    @JProp
    public double doubleProp = 1.0;
    
    NavPoint start = null;
    NavPoint end = null;
    int failCounter = 0;
    
    long lastLogic = 0;
    int logicCounter = 0;
    int slowCounter = 0;
    long totalTime = 0;
    int locUpdateCount = 0;
    
    long hundredTime = 0;
    int hundredLocUpdates = 0;
    int hundredSlowCounter = 0;
    
    boolean respawning = false;
    TabooSet<NavPoint> taboo;
    
    private IPathPlanner navMesh;
    
    private Phase phase = Phase.Init;
    
    public enum Phase {

        Init, AtStart, Navigate
    }
    
    private final IWorldEventListener<LocationUpdate> locationUpdateMessageListener = new IWorldEventListener<LocationUpdate>() {
        @Override
        public void notify(LocationUpdate event) {
//            ++locUpdateCount;
//            ++hundredLocUpdates;
//            LocationUpdate locationUpdate = event;
//            Self self = bot.getSelf();
//            if (self == null) {
//                return;
//            }
//            Self newSelf = new SelfMessage(self.getId(), self.getBotId(), self.getName(), self.isVehicle(),
//                    locationUpdate.getLoc(), locationUpdate.getVel(), locationUpdate.getRot(), self.getTeam(),
//                    self.getWeapon(), self.isShooting(), self.getHealth(), self.getPrimaryAmmo(),
//                    self.getSecondaryAmmo(), self.getAdrenaline(), self.getArmor(), self.getSmallArmor(),
//                    self.isAltFiring(), self.isCrouched(), self.isWalking(), self.getFloorLocation(),
//                    self.getFloorNormal(), self.getCombo(), self.getUDamageTime(), self.getAction(),
//                    self.getEmotLeft(), self.getEmotCenter(), self.getEmotRight(), self.getBubble(),
//                    self.getAnim());
            //log.fine("LOCATION UPDATE - Updating location from LocationUpdate message...");
            //log.log(Level.FINE, "LOCATION UPDATE - L: {0}, V: {1}, R: {2}, SimTime: {3}", new Object[]{locationUpdate.getLoc(), locationUpdate.getVel(), locationUpdate.getRot(), locationUpdate.getSimTime()});
            //bot.getWorldView().notifyImmediately(newSelf);
            //selfListener.notify(new WorldObjectFirstEncounteredEvent<Self>(newSelf, event.getSimTime()));

            //eventLocationUpdateMessage();
        }
    };
    
    private IWorldEventListener<MapListObtained> mapListEndListener = new IWorldEventListener<MapListObtained>() {
        
        @Override
        public void notify(MapListObtained event) {
            log.severe("MAP LIST OBTAINED received!");
        }
    };
    
    private IWorldEventListener<MapPointListObtained> mapPointListEndListener = new IWorldEventListener<MapPointListObtained>() {
        
        @Override
        public void notify(MapPointListObtained event) {
            log.severe("MAP POINT POINT POINT LIST OBTAINED received!");
        }
    };
    
    private Date startedAt;

    /**
     * Initialize all necessary variables here, before the bot actually receives
     * anything from the environment.
     *
     * @param bot
     */
    @Override
    public void prepareBot(UT2004Bot bot) {
        bot.getWorldView().addEventListener(LocationUpdate.class, locationUpdateMessageListener);
        bot.getWorldView().addEventListener(MapListObtained.class, mapListEndListener);
        bot.getWorldView().addEventListener(MapPointListObtained.class, mapPointListEndListener);
        taboo = new TabooSet<NavPoint>(bot);
        log.setLevel(Level.FINER);
    }

    /**
     * Here we can modify initializing command for our bot, e.g., sets its name
     * or skin.
     *
     * @return instance of {@link Initialize}
     */
    @Override
    public Initialize getInitializeCommand() {
        return new Initialize().setName("EmptyBot").setManualSpawn(false);
    }

    /**
     * Handshake with GameBots2004 is over - bot has information about the map
     * in its world view. Many agent modules are usable since this method is
     * called.
     *
     * @param gameInfo informaton about the game type
     * @param currentConfig
     * @param init information about configuration
     */
    @Override
    public void botInitialized(GameInfo gameInfo, ConfigChange currentConfig, InitedMessage init) {
        
        log.setLevel(Level.FINER);
        bot.getLog().setLevel(Level.FINEST);
        
        pathExecutor.getState().addStrongListener(new FlagListener<IPathExecutorState>() {
            @Override
            public void flagChanged(IPathExecutorState changedValue) {
                switch (changedValue.getState()) {
                    case INSTANTIATED:
                        break;
                    case FOLLOW_PATH_CALLED:
                        break;
                    case PATH_COMPUTED:
                        break;
                    case PATH_COMPUTATION_FAILED:
                        reset();
                        break;
                    case SWITCHED_TO_ANOTHER_PATH_ELEMENT:
                        break;
                    case TARGET_REACHED:
                        phase = Phase.Init;
                        break;
                    case STUCK:
                        reset();
                        break;
                    case STOPPED:
                        break;
                    default:
                        throw new AssertionError(changedValue.getState().name());
                    
                }
            }
            
            private void reset() {
                log.warning("STUCK");
                body.getCommunication().sendGlobalTextMessage("STUCK!");
                navigation.stopNavigation();
                //start = null;
                //end = null;
            }
        });
        
        if (navMeshModule.getNavMeshDraw() != null) {
            navMeshModule.getNavMeshDraw().draw();
        }
    }

    /**
     * Main method that controls the bot - makes decisions what to do next. It
     * is called iteratively by Pogamut engine every time a synchronous batch
     * from the environment is received. This is usually 4 times per second - it
     * is affected by visionTime variable, that can be adjusted in GameBots ini
     * file in UT2004/System folder.
     *
     */
    @Override
    public void logic() throws PogamutException {
//        if(!navigation.isNavigating()) {
//            navigation.navigate(navPoints.getRandomNavPoint());
//        }
        if (start == null) {
            start = navPoints.getNavPoint(UnrealId.get("DM-1on1-Trite.InventorySpot98"));
        }
        if (end == null) {
            end = navPoints.getNavPoint(UnrealId.get("DM-1on1-Trite.InventorySpot100"));
        }

//        NavPointNeighbourLink edge = start.getOutgoingEdges().get(end.getId());
//        boolean b = UT2004EdgeChecker.checkLink(edge);
//        if(b) {
//            return;
//        }
        switch (phase) {
            case Init:
                body.getAction().respawn(start);
                phase = Phase.AtStart;
                break;
            case AtStart:
                if (info.atLocation(start)) {
                    IPathFuture<ILocated> path = navMesh.computePath(start, end);
                    navMeshModule.getNavMeshDraw().drawPath(path, new Location(255, 0, 0));
                    navigation.navigate(path);
                    startedAt = new Date();
                    phase = Phase.Navigate;
                }
                break;
            case Navigate:
                if ((new Date().getTime() - startedAt.getTime()) > 10000) {
                    phase = Phase.Init;
                    navigation.stopNavigation();
                }
                
                break;
            default:
                throw new AssertionError(phase.name());
            
        }
        
    }
    
    @Override
    protected void initializePathFinding(UT2004Bot bot) {
        fwMap = new FloydWarshallMap(bot);
        //pathPlanner = NavigationFactory.getPathPlanner(this, "navMesh");
        navigation = NavigationFactory.getNavigation(this, bot, "acc");
        
        aStar = new UT2004AStar(bot);
        pathExecutor = navigation.getPathExecutor();
        
        getBackToNavGraph = navigation.getBackToNavGraph();
        runStraight = navigation.getRunStraight();
        navMesh = fwMap;
//        if (navMeshModule.getNavMesh() != null) {
//            navMesh = navMeshModule.getNavMesh();
//        } else {
//            navMesh = new NavMesh(world, log);
//        }
    }

    /**
     * Called each time the bot dies. Good for reseting all bot's state
     * dependent variables.
     *
     * @param event
     */
    @Override
    public void botKilled(BotKilled event) {
        // First, try to run the bot and kill it... than uncomment this line and run the bot again
        // kill it and see the difference.

//        if (!respawning) {
//            log.info("Bot killed - respawning started.");
//            start = null;
//            end = null;
//            navigation.stopNavigation();
//            respawning = true;
//            body.getAction().respawn();
//        } else {
        respawning = false;
        log.info("Already respawning...");
        // }
    }

    /**
     * This method is called when the bot is started either from IDE or from
     * command line.
     *
     * @param args
     */
    public static void main(String args[]) throws PogamutException {
        // wrapped logic for bots executions, suitable to run single bot in single JVM
        AgentRunner runner = new UT2004BotRunner(NavMeshTestBot1.class, "NavMeshTestBot").setMain(true);
        //runner.getLog().setLevel(Level.ALL);
        runner.startAgent();
    }
    
}
