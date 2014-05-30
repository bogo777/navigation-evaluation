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
import cz.cuni.amis.pogamut.ut2004.agent.navigation.navmesh.OffMeshPoint;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004Bot;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Initialize;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Move;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
public class NavMeshTestBot extends EvaluatingBot {

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
            //CASE: Jump down to left
            //start = navPoints.getNavPoint(UnrealId.get("DM-1on1-Trite.InventorySpot91"));
            //end = navPoints.getNavPoint(UnrealId.get("DM-1on1-Trite.InventorySpot127"));

            //start = navPoints.getNavPoint(UnrealId.get("DM-1on1-Trite.InventorySpot94"));
            //end = navPoints.getNavPoint(UnrealId.get("DM-1on1-Trite.InventorySpot117"));
            //CASE: Jump down
            //start = navPoints.getNavPoint(UnrealId.get("DM-1on1-Trite.InventorySpot81"));
            //end = navPoints.getNavPoint(UnrealId.get("DM-1on1-Trite.InventorySpot102"));
            //start = navPoints.getNavPoint(UnrealId.get("DM-1on1-Trite.InventorySpot101"));
            //end = navPoints.getNavPoint(UnrealId.get("DM-1on1-Trite.InventorySpot68"));
            //CASE: Stop unexpected - high NavPoint
            //start = navPoints.getNavPoint(UnrealId.get("DM-1on1-Trite.InventorySpot119"));
            //end = navPoints.getNavPoint(UnrealId.get("DM-1on1-Trite.PlayerStart5"));
            //Stuck in jump?
            //start = navPoints.getNavPoint(UnrealId.get("DM-1on1-Trite.PlayerStart6"));
            //end = navPoints.getNavPoint(UnrealId.get("DM-1on1-Trite.InventorySpot98"));
            //Jump up to steep platform
            //start = navPoints.getNavPoint(UnrealId.get("DM-1on1-Trite.InventorySpot72"));
            //end = navPoints.getNavPoint(UnrealId.get("DM-1on1-Trite.InventorySpot124"));
            //Lift - too far for AWAITING_MOVER
            //start = navPoints.getNavPoint(UnrealId.get("DM-1on1-Trite.InventorySpot99"));
            //end = navPoints.getNavPoint(UnrealId.get("DM-1on1-Trite.InventorySpot80"));
            //Collision - Jumping detours
            //start = navPoints.getNavPoint(UnrealId.get("DM-1on1-Trite.InventorySpot74"));
            //end = navPoints.getNavPoint(UnrealId.get("DM-1on1-Trite.InventorySpot85"));
            //Jump to low ceiling
            //start = navPoints.getNavPoint(UnrealId.get("DM-1on1-Trite.InventorySpot115"));
            //end = navPoints.getNavPoint(UnrealId.get("DM-1on1-Trite.InventorySpot100"));
            //Jump to low ceiling
            //start = navPoints.getNavPoint(UnrealId.get("DM-1on1-Trite.InventorySpot95"));
            //end = navPoints.getNavPoint(UnrealId.get("DM-1on1-Trite.InventorySpot96"));
            //PathNode2 - Bad path
            //start = navPoints.getNavPoint(UnrealId.get("DM-1on1-Trite.InventorySpot131"));
            //end = navPoints.getNavPoint(UnrealId.get("DM-1on1-Trite.PlayerStart6"));
            //PathNode2
            //start = navPoints.getNavPoint(UnrealId.get("DM-1on1-Trite.InventorySpot71"));
            //end = navPoints.getNavPoint(UnrealId.get("DM-1on1-Trite.InventorySpot93"));
            //Zabradli - internal collision detector
            //start = navPoints.getNavPoint(UnrealId.get("DM-1on1-Mixer.InventorySpot11"));
            //end = navPoints.getNavPoint(UnrealId.get("DM-1on1-Mixer.InventorySpot30"));
            //Double jump up
            //start = navPoints.getNavPoint(UnrealId.get("DM-1on1-Idoma.InventorySpot1"));
            //end = navPoints.getNavPoint(UnrealId.get("DM-1on1-Idoma.InventorySpot14"));
            //Long jump 
            //start = navPoints.getNavPoint(UnrealId.get("DM-1on1-Idoma.InventorySpot0"));
            //end = navPoints.getNavPoint(UnrealId.get("DM-1on1-Idoma.PlayerStart5"));
            //Strange path - Bad placement of NavPoints to polygon(bad floor?!)
            //start = navPoints.getNavPoint(UnrealId.get("DM-1on1-Idoma.InventorySpot11"));
            //end = navPoints.getNavPoint(UnrealId.get("DM-1on1-Idoma.InventorySpot19"));
            //Strange path - Bad placement of NavPoints to polygon(bad floor?!) #2
            //start = navPoints.getNavPoint(UnrealId.get("DM-1on1-Idoma.InventorySpot13"));
            //end = navPoints.getNavPoint(UnrealId.get("DM-1on1-Idoma.InventorySpot28"));
//            int id = navMeshModule.getNavMesh().getPolygonIdByLocation(navPoints.getNavPoint(UnrealId.get("DM-Gestalt.InventorySpot17")).getLocation());
//            //int id = 84;
//            List<OffMeshPoint> offPoints = navMeshModule.getNavMesh().getOffMeshPoinsOnPolygon(id);
//            int[] polygon = navMeshModule.getNavMesh().getPolygon(id);
//            double[] vertex = navMeshModule.getNavMesh().getVertex(polygon[0]);
//            double z = vertex[2];
//            ArrayList neighbours = navMeshModule.getNavMesh().getNeighbourIdsToPolygon(id);
            //Bad path - SOLVED?
            //start = navPoints.getNavPoint(UnrealId.get("DM-Gestalt.InventorySpot22"));
            //end = navPoints.getNavPoint(UnrealId.get("DM-Gestalt.InventorySpot24"));
            //Jump down through lift - NOT SOLVED
            //start = navPoints.getNavPoint(UnrealId.get("DM-Gestalt.InventorySpot16"));
            //end = navPoints.getNavPoint(UnrealId.get("DM-Gestalt.InventorySpot20"));
            //Up movement with rotation - bad mesh
            //start = navPoints.getNavPoint(UnrealId.get("DM-Injector.InventorySpot14"));
            //end = navPoints.getNavPoint(UnrealId.get("DM-Injector.InventorySpot29"));
            //Roughinery - solved
            //start = navPoints.getNavPoint(UnrealId.get("DM-1on1-Roughinery.PathNode60"));
            //end = navPoints.getNavPoint(UnrealId.get("DM-1on1-Roughinery.InventorySpot119"));
            //Roughinery - solved
            //start = navPoints.getNavPoint(UnrealId.get("DM-1on1-Roughinery.InventorySpot105"));
            //end = navPoints.getNavPoint(UnrealId.get("DM-1on1-Roughinery.InventorySpot132"));
            //Roughinery - jump down crooked
            //start = navPoints.getNavPoint(UnrealId.get("DM-1on1-Roughinery.InventorySpot109"));
            //end = navPoints.getNavPoint(UnrealId.get("DM-1on1-Roughinery.InventorySpot103"));
            //Test jumppad corrections - SOLVED
//            start = navPoints.getNavPoint(UnrealId.get("DM-1on1-Crash.InventorySpot19"));
//            end = navPoints.getNavPoint(UnrealId.get("DM-1on1-Crash.InventorySpot13"));
            //Teleports - SOLVED
            //start = navPoints.getNavPoint(UnrealId.get("DM-Inferno.PathNode69"));
            //end = navPoints.getNavPoint(UnrealId.get("DM-Inferno.InventorySpot41"));
            //Serpentine - exact jumping
            //start = navPoints.getNavPoint(UnrealId.get("DM-1on1-Serpentine.InventorySpot35"));
            //end = navPoints.getNavPoint(UnrealId.get("DM-1on1-Serpentine.InventorySpot3"));
            //Serpentine - exact jumping
            //start = navPoints.getNavPoint(UnrealId.get("DM-1on1-Serpentine.InventorySpot16"));
            //end = navPoints.getNavPoint(UnrealId.get("DM-1on1-Serpentine.PlayerStart17"));
            //CASE: TD - Not Built
            //start = navPoints.getNavPoint(UnrealId.get("DM-TrainingDay.InventorySpot42"));
            //end = navPoints.getNavPoint(UnrealId.get("DM-TrainingDay.InventorySpot60"));
            
            
            //Roughinery - unneccessary jump - 2x OUT of MESH edge - hacked
//            start = navPoints.getNavPoint(UnrealId.get("DM-1on1-Roughinery.InventorySpot116"));
//            end = navPoints.getNavPoint(UnrealId.get("DM-1on1-Roughinery.PlayerStart10"));
            
            start = navPoints.getNavPoint(UnrealId.get("DM-1on1-Roughinery.PlayerStart11"));
            end = navPoints.getNavPoint(UnrealId.get("DM-1on1-Roughinery.InventorySpot113"));
            
//            int id = navMeshModule.getNavMesh().getPolygonIdByLocation(start.getLocation());
////            //int id = 84;
//            List<OffMeshPoint> offPoints = navMeshModule.getNavMesh().getOffMeshPoinsOnPolygon(id);
//            int[] polygon = navMeshModule.getNavMesh().getPolygon(id);
//            double[] vertex = navMeshModule.getNavMesh().getVertex(polygon[0]);
//            double z = vertex[2];
//            ArrayList neighbours = navMeshModule.getNavMesh().getNeighbourIdsToPolygon(id);

        }

        switch (phase) {
            case Init:
                body.getAction().respawn(start);
                phase = Phase.AtStart;
                break;
            case AtStart:
                if (info.atLocation(start)) {
                    IPathFuture<ILocated> path = navMesh.computePath(start, end);
                    //navMeshModule.getNavMesh().getPolygonPath(start.getLocation(), end.getLocation());
                    if (path.get() != null) {
                        navMeshModule.getNavMeshDraw().drawPath(path, new Location(255, 0, 0));
                    }
                    navigation.navigate(path);
                    //act.act(new Move(navPoints.getNavPoint(UnrealId.get("DM-1on1-Trite.PathNode39")).getLocation(), end.getLocation(), null, end.getLocation()));
                    startedAt = new Date();
                    phase = Phase.Navigate;
                }
                break;
            case Navigate:
                if ((new Date().getTime() - startedAt.getTime()) > 8000) {
                    phase = Phase.Init;
                    navigation.stopNavigation();
                }
//                else if(bot.getLocation().getDistance(navPoints.getNavPoint(UnrealId.get("DM-1on1-Trite.PathNode36")).getLocation()) < 40) {
//                    log.info("Switching to next node! Distance:" + bot.getLocation().getDistance(navPoints.getNavPoint(UnrealId.get("DM-1on1-Trite.PathNode36")).getLocation()));
//                    act.act(new Move(end.getLocation(), null, null, end.getLocation()));
//                }

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
        navMeshModule.setFwMap(fwMap);

        aStar = new UT2004AStar(bot);
        pathExecutor = navigation.getPathExecutor();

        getBackToNavGraph = navigation.getBackToNavGraph();
        runStraight = navigation.getRunStraight();
        //navMesh = fwMap;
        if (navMeshModule.getNavMesh() != null) {
            navMesh = navMeshModule.getNavMesh();
        } else {
            navMesh = new NavMesh(world, log);
        }
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
        AgentRunner runner = new UT2004BotRunner(NavMeshTestBot.class, "NavMeshTestBot").setMain(true);
        //runner.getLog().setLevel(Level.ALL);
        runner.startAgent();
    }

}
