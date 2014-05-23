/*
 * Copyright (C) 2014 AMIS research group, Faculty of Mathematics and Physics, Charles University in Prague, Czech Republic
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cz.cuni.amis.pogamut.ut2004.navigation.evaluator.bot;

import com.google.inject.internal.cglib.core.CollectionUtils;
import cz.cuni.amis.pogamut.base.communication.worldview.event.IWorldEventListener;
import cz.cuni.amis.pogamut.base.communication.worldview.object.IWorldObjectEventListener;
import cz.cuni.amis.pogamut.base.communication.worldview.object.WorldObjectId;
import cz.cuni.amis.pogamut.base.communication.worldview.object.event.WorldObjectEvent;
import cz.cuni.amis.pogamut.base.utils.math.DistanceUtils;
import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.NavigationState;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.astar.UT2004AStar;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.floydwarshall.FloydWarshallMap;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.stuckdetector.UT2004DistanceStuckDetector;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.stuckdetector.UT2004PositionStuckDetector;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.stuckdetector.UT2004TimeStuckDetector;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004Bot;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Move;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.BotKilled;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.ConfigChange;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.GameInfo;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.InitedMessage;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.LocationUpdate;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Self;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.SelfMessage;
import static cz.cuni.amis.pogamut.ut2004.navigation.evaluator.bot.JumpInspectingBotFall.Phase.ToStart;
import cz.cuni.amis.utils.collections.MyCollections;
import cz.cuni.amis.utils.flag.FlagListener;
import java.util.Map;
import java.util.logging.Level;

/**
 *
 * @author Bogo
 */
public class JumpInspectingBotFall extends EvaluatingBot {
    private boolean respawning;
    
    
    

    public enum Phase {

        ToStart, OnStart, Running, Jumping, AfterJump, AtEnd
    }

    private IWorldEventListener<LocationUpdate> listener;

    Phase phase = Phase.ToStart;

    double jumpDistance = 400;

    NavPoint startSpot = null;
    Location endSpot = null;
    int step = 0;
    boolean inEvaluation = false;
    boolean jumpedAlready = false;

    boolean inJump = false;

    Location beforeJumpMax = null;
    Location afterJumpMax = null;

    Location beforeJumpMin = null;
    Location afterJumpMin = null;

//    public BotNavigationParameters getParams() {
//        return (BotNavigationParameters) bot.getParams();
//    }
    @Override
    protected void initializePathFinding(UT2004Bot bot) {
        fwMap = new FloydWarshallMap(bot);
        pathPlanner = NavigationFactory.getPathPlanner(this, "navMesh");
        navigation = NavigationFactory.getNavigation(this, bot, "navigation");

        aStar = new UT2004AStar(bot);
        pathExecutor = navigation.getPathExecutor();

        // add stuck detectors that watch over the path-following, if it (heuristicly) finds out that the bot has stuck somewhere,
        // it reports an appropriate path event and the path executor will stop following the path which in turn allows 
        // us to issue another follow-path command in the right time
        pathExecutor.addStuckDetector(new UT2004TimeStuckDetector(bot, 3000, 100000)); // if the bot does not move for 3 seconds, considered that it is stuck
        pathExecutor.addStuckDetector(new UT2004PositionStuckDetector(bot));           // watch over the position history of the bot, if the bot does not move sufficiently enough, consider that it is stuck
        pathExecutor.addStuckDetector(new UT2004DistanceStuckDetector(bot));           // watch over distances to target

        getBackToNavGraph = navigation.getBackToNavGraph();
        runStraight = navigation.getRunStraight();
    }

    double zOnTheGround = -78.15;
    int jumpCount = 0;
    
    double maxJump = Double.NEGATIVE_INFINITY;
    private double maxJumpDelay;

    boolean turned = false;

    @Override
    public void beforeFirstLogic() {
        super.beforeFirstLogic();
        
    }

    @Override
    public void botInitialized(GameInfo gameInfo, ConfigChange currentConfig, InitedMessage init) {
        super.botInitialized(gameInfo, currentConfig, init);
        //move.setSpeed(0.9);
        bot.getLog().setLevel(Level.WARNING);

        move.getLog().setLevel(Level.WARNING);

        listener = new IWorldEventListener<LocationUpdate>() {
            

            public void notify(LocationUpdate t) {
                //log.warning("Accepted location update.");
                LocationUpdate locationUpdate = t;
                //log.warning("LOCATION UPDATE - L: {0}, V: {1}, R: {2}, SimTime: {3}", new Object[]{locationUpdate.getLoc(), locationUpdate.getVel(), locationUpdate.getRot(), locationUpdate.getSimTime()});

                Phase lastPhase = phase;
                switch (lastPhase) {
                    case Running:
                        if (inJump) {
                            log.warning("LOCATION UPDATE - L: {0}\tV: {1}\tR: {2}\tSimTime: {3}", new Object[]{locationUpdate.getLoc(), locationUpdate.getVel(), locationUpdate.getRot(), locationUpdate.getSimTime()});
                        }
                        if (inJump && !isOnTheGround(locationUpdate.getLoc())) {
                            phase = Phase.Jumping;
                        }
                        break;
                    case Jumping:
                        if(locationUpdate.getLoc().z > maxJump) {
                            maxJump = locationUpdate.getLoc().z;
                            //maxJumpDelay = 0.35 + 0.01 * (jumpCount / 6);
                        }
                        log.warning("LOCATION UPDATE - L: {0}\tV: {1}\tR: {2}\tSimTime: {3}", new Object[]{locationUpdate.getLoc(), locationUpdate.getVel(), locationUpdate.getRot(), locationUpdate.getSimTime()});
                        if (isOnTheGround(locationUpdate.getLoc()) && locationUpdate.getVel().z == 0) {
                            phase = Phase.AfterJump;
                            inJump = false;
                        }
                }

                switch (phase) {
                    case Running:
                        beforeJumpMax = locationUpdate.getLoc();
                        break;
                    case Jumping:
                        if (lastPhase == Phase.Running) {
                            beforeJumpMin = locationUpdate.getLoc();
                        }

                        afterJumpMin = locationUpdate.getLoc();
                        break;
                    case AfterJump:

                        if (lastPhase == Phase.Jumping) {
                            afterJumpMax = locationUpdate.getLoc();
                            //evaluate

                            double jumpLengthMax = afterJumpMax.getDistance2D(beforeJumpMax);
                            double jumpLengthMin = afterJumpMin.getDistance2D(beforeJumpMin);

                            log.warning("JUMP FINISHED: MAX: " + jumpLengthMax + " MIN: " + jumpLengthMin);
                            log.warning("Before MAX loc: " + beforeJumpMax);
                            log.warning("After MAX loc: " + afterJumpMax);
                            log.warning("Before MIN loc: " + beforeJumpMin);
                            log.warning("After MIN loc: " + afterJumpMin);
                        }
                }

            }
        };

        world.addEventListener(LocationUpdate.class, listener);

        navigation.addStrongNavigationListener(new FlagListener<cz.cuni.amis.pogamut.ut2004.agent.navigation.NavigationState>() {

            public void flagChanged(NavigationState changedValue) {
                if (changedValue == NavigationState.TARGET_REACHED) {
                    if (phase == Phase.ToStart && bot.getLocation().getDistance2D(startSpot.getLocation()) < 100) {
                        phase = Phase.OnStart;
                    }
                }
            }
        });

        String firstSpotId = "DM-1on1-Idoma.PathNode25";
        String secondSpotId = "DM-1on1-Idoma.PlayerStart2";

        startSpot = navPoints.getNavPoint(UnrealId.get(firstSpotId));
        endSpot = navPoints.getNavPoint(UnrealId.get(secondSpotId)).getLocation();

    }

    @Override
    public void logic() {
        // mark that another logic iteration has began
        //log.info("--- Logic iteration ---");

        if (bot.getVelocity().size() < 10) {

            switch (phase) {
                case ToStart:
                    body.getAction().respawn(startSpot);
                    phase = Phase.OnStart;
                    break;
                case OnStart:
                    if (info.atLocation(startSpot)) {
                    zOnTheGround = bot.getLocation().z;
                    
                    act.act(new Move(endSpot, null, null, endSpot));
                    //navigation.navigate(endSpot);
                    phase = Phase.Running;
                    }
                    break;
//                case Running: 
//                    //Do nothing
//                    break;
                default:
                    //navigation.navigate(startSpot);
                    phase = Phase.ToStart;
                    break;
            }
        }

        if (phase == Phase.Running && isOnTheGround(bot.getLocation())) {
            if(navPoints.getNavPoint(UnrealId.get("DM-1on1-Idoma.PathNode36")).getLocation().getDistance(bot.getLocation()) < 100) {
                
            move.jump(true, 0.39, 755);
            //move.jump(380);
            log.warning("JUMP INITIATED");
            inJump = true;
            ++jumpCount;
            }
        }

//            NavPoint current = DistanceUtils.getNearest(navPoints.getNavPoints().values(), bot.getLocation());
//
//            if (step == 0) {
//                if (current == null) {
//                    //Do nothing
//                } else if (current.equals(startSpot)) {
//                    inEvaluation = true;
//                    move.turnTo(endSpot);
//                } else {
//                    move.turnTo(startSpot);
//                }
//
//            } else if (step > 3) {
//                if (current == null) {
//                    navigation.navigate(startSpot);
//                } else if (current.equals(startSpot)) {
//                    navigation.navigate(endSpot);
//                } else {
//                    navigation.navigate(startSpot);
//                }
//
//            }
//
//        }
//
//        ++step;
//
//        if (inEvaluation && step > 8 && isOnTheGround() && bot.getVelocity().size() > 400 && !jumpedAlready) {
//            log.warning("Sending JUMP command.");
//            log.log(Level.WARNING, "INFO: Velocity: {0}\n\tPosition: {1}", new Object[]{bot.getVelocity().size(), bot.getLocation()});
//            move.jump();
//            jumpedAlready = true;
//            ++jumpCount;
//        }
        if (jumpCount > 10) {
            log.warning("MAX JUMP: " + maxJump);
            //log.warning("MAX_JUMP_DELAY: " + maxJumpDelay);
            log.info("No more jumps to jump, we are finished...");
            isCompleted = true;
            new Runnable() {
                public void run() {
                    bot.stop();
                }
            }.run();
        }

    }

    @Override
    public void botKilled(BotKilled event) {
        super.botKilled(event); 
        respawning = false;
        log.info("Already respawning...");
    }
    
    

    private boolean isOnTheGround(Location location) {
        return location.z <= zOnTheGround + 1;
    }
}
