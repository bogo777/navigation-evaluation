package cz.cuni.amis.pogamut.ut2004.externalbot;

import cz.cuni.amis.introspection.java.JProp;
import cz.cuni.amis.pogamut.base.agent.navigation.IPathExecutorState;
import cz.cuni.amis.pogamut.base.communication.worldview.event.IWorldEventListener;
import cz.cuni.amis.pogamut.base.utils.guice.AgentScoped;
import cz.cuni.amis.pogamut.base.utils.math.DistanceUtils;
import cz.cuni.amis.pogamut.ut2004.agent.module.utils.TabooSet;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004Bot;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004BotModuleController;
import cz.cuni.amis.pogamut.ut2004.bot.state.impl.BotStateSpawned;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Initialize;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.SendMessage;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.BotKilled;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.ConfigChange;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.GameInfo;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.InitedMessage;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.LocationUpdate;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Self;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.SelfMessage;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Spawn;
import cz.cuni.amis.pogamut.ut2004.navigation.evaluator.bot.EvaluatingBot;
import cz.cuni.amis.pogamut.ut2004.utils.UT2004BotRunner;
import cz.cuni.amis.utils.collections.MyCollections;
import cz.cuni.amis.utils.exception.PogamutException;
import cz.cuni.amis.utils.flag.FlagListener;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

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
public class RespawnBot extends EvaluatingBot {

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

    private final IWorldEventListener<LocationUpdate> locationUpdateMessageListener = new IWorldEventListener<LocationUpdate>() {
        @Override
        public void notify(LocationUpdate event) {
            ++locUpdateCount;
            ++hundredLocUpdates;
            LocationUpdate locationUpdate = event;
            Self self = bot.getSelf();
            if (self == null) {
                return;
            }
            Self newSelf = new SelfMessage(self.getId(), self.getBotId(), self.getName(), self.isVehicle(),
                    locationUpdate.getLoc(), locationUpdate.getVel(), locationUpdate.getRot(), self.getTeam(),
                    self.getWeapon(), self.isShooting(), self.getHealth(), self.getPrimaryAmmo(),
                    self.getSecondaryAmmo(), self.getAdrenaline(), self.getArmor(), self.getSmallArmor(),
                    self.isAltFiring(), self.isCrouched(), self.isWalking(), self.getFloorLocation(),
                    self.getFloorNormal(), self.getCombo(), self.getUDamageTime(), self.getAction(),
                    self.getEmotLeft(), self.getEmotCenter(), self.getEmotRight(), self.getBubble(),
                    self.getAnim());
            //log.fine("LOCATION UPDATE - Updating location from LocationUpdate message...");
            log.log(Level.FINE, "LOCATION UPDATE - L: {0}, V: {1}, R: {2}, SimTime: {3}", new Object[]{locationUpdate.getLoc(), locationUpdate.getVel(), locationUpdate.getRot(), locationUpdate.getSimTime()});
            bot.getWorldView().notifyImmediately(newSelf);
            //selfListener.notify(new WorldObjectFirstEncounteredEvent<Self>(newSelf, event.getSimTime()));

            //eventLocationUpdateMessage();
        }
    };

    /**
     * Initialize all necessary variables here, before the bot actually receives
     * anything from the environment.
     */
    @Override
    public void prepareBot(UT2004Bot bot) {
        bot.getWorldView().addEventListener(LocationUpdate.class, locationUpdateMessageListener);
        taboo = new TabooSet<NavPoint>(bot);
        log.setLevel(Level.WARNING);
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
     * @param config information about configuration
     * @param init information about configuration
     */
    @Override
    public void botInitialized(GameInfo gameInfo, ConfigChange currentConfig, InitedMessage init) {

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
                        reset();
                        break;
                    case STUCK:
                        reset();
                        break;
                    case STOPPED:
                        reset();
                        break;
                    default:
                        throw new AssertionError(changedValue.getState().name());

                }
            }

            private void reset() {
                //start = null;
                //end = null;
            }
        });
    }

    /**
     * Main method that controls the bot - makes decisions what to do next. It
     * is called iteratively by Pogamut engine every time a synchronous batch
     * from the environment is received. This is usually 4 times per second - it
     * is affected by visionTime variable, that can be adjusted in GameBots ini
     * file in UT2004/System folder.
     *
     * @throws cz.cuni.amis.pogamut.base.exceptions.PogamutException
     */
    @Override
    public void logic() throws PogamutException {
        Collection<NavPoint> navPs = navPoints.getNavPoints().values();
        if (start == null) {
            start = DistanceUtils.getNearest(navPs, bot.getLocation());
        }
        if (end == null) {
            end = DistanceUtils.getNearest(navPs, bot.getLocation());
        }

        if (!respawning) {
            if (info.atLocation(end) || failCounter > 3) {
                taboo.add(end);
                log.severe("Teleported distance: " + end.getLocation().getDistance(start.getLocation()));

                start = end;
                end = DistanceUtils.getNearest(taboo.filter(navPs), end);

                respawning = true;
                body.getAction().respawn(end);
            } else {
                log.severe("Failed to teleport distance: " + end.getLocation().getDistance(start.getLocation()));
                ++failCounter;
                body.getAction().respawn(end);
            }

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
        body.getCommunication().sendGlobalTextMessage("I was KILLED!");
        if (!respawning) {
            log.info("Bot killed - respawning started.");
            start = null;
            end = null;
            navigation.stopNavigation();
            respawning = true;
            body.getAction().respawn();
        } else {
            respawning = false;
            log.info("Already respawning...");
        }
    }

    /**
     * This method is called when the bot is started either from IDE or from
     * command line.
     *
     * @param args
     */
    public static void main(String args[]) throws PogamutException {
        // wrapped logic for bots executions, suitable to run single bot in single JVM
        new UT2004BotRunner(RespawnBot.class, "EmptyBot").setMain(true).startAgent();
    }

}
