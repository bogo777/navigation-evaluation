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
import cz.cuni.amis.pogamut.base.communication.worldview.object.IWorldObjectEventListener;
import cz.cuni.amis.pogamut.base.communication.worldview.object.event.WorldObjectEvent;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.astar.UT2004AStar;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.floydwarshall.FloydWarshallMap;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.stuckdetector.UT2004DistanceStuckDetector;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.stuckdetector.UT2004PositionStuckDetector;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.stuckdetector.UT2004TimeStuckDetector;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004Bot;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.ConfigChange;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.GameInfo;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.InitedMessage;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.LocationUpdate;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Self;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.SelfMessage;
import cz.cuni.amis.utils.collections.MyCollections;
import java.util.logging.Level;

/**
 *
 * @author Bogo
 */
public class JumpInspectingBot extends EvaluatingBot {

//    public BotNavigationParameters getParams() {
//        return (BotNavigationParameters) bot.getParams();
//    }

//    @Override
//    protected void initializePathFinding(UT2004Bot bot) {
//        fwMap = new FloydWarshallMap(bot);
//        pathPlanner = NavigationFactory.getPathPlanner(this, getParams().getPathPlanner());
//        navigation = NavigationFactory.getNavigation(this, bot, getParams().getNavigation());
//
//        aStar = new UT2004AStar(bot);
//        pathExecutor = navigation.getPathExecutor();
//
//        // add stuck detectors that watch over the path-following, if it (heuristicly) finds out that the bot has stuck somewhere,
//        // it reports an appropriate path event and the path executor will stop following the path which in turn allows 
//        // us to issue another follow-path command in the right time
//        pathExecutor.addStuckDetector(new UT2004TimeStuckDetector(bot, 3000, 100000)); // if the bot does not move for 3 seconds, considered that it is stuck
//        pathExecutor.addStuckDetector(new UT2004PositionStuckDetector(bot));           // watch over the position history of the bot, if the bot does not move sufficiently enough, consider that it is stuck
//        pathExecutor.addStuckDetector(new UT2004DistanceStuckDetector(bot));           // watch over distances to target
//
//        getBackToNavGraph = navigation.getBackToNavGraph();
//        runStraight = navigation.getRunStraight();
//    }
    
    
    double zOnTheGround = 0;
    int jumpCount = 0;

    @Override
    public void beforeFirstLogic() {
        super.beforeFirstLogic();
        zOnTheGround = bot.getLocation().z;
    }

    @Override
    public void botInitialized(GameInfo gameInfo, ConfigChange currentConfig, InitedMessage init) {
        super.botInitialized(gameInfo, currentConfig, init);
        bot.getLog().setLevel(Level.ALL);

        world.addObjectListener(LocationUpdate.class, new IWorldObjectEventListener<LocationUpdate, WorldObjectEvent<LocationUpdate>>() {
            public void notify(WorldObjectEvent<LocationUpdate> t) {
                log.warning("Accepted location update.");
                LocationUpdate locationUpdate = t.getObject();
                log.warning("LOCATION UPDATE - L: {0}, V: {1}, R: {2}, SimTime: {3}", new Object[]{locationUpdate.getLoc(), locationUpdate.getVel(), locationUpdate.getRot(), locationUpdate.getSimTime()});
//                Self self = bot.getSelf();
//                if (self == null) {
//                    return;
//                }
//                Self newSelf = new SelfMessage(self.getId(), self.getBotId(), self.getName(), self.isVehicle(),
//                        locationUpdate.getLoc(), locationUpdate.getVel(), locationUpdate.getRot(), self.getTeam(),
//                        self.getWeapon(), self.isShooting(), self.getHealth(), self.getPrimaryAmmo(),
//                        self.getSecondaryAmmo(), self.getAdrenaline(), self.getArmor(), self.getSmallArmor(),
//                        self.isAltFiring(), self.isCrouched(), self.isWalking(), self.getFloorLocation(),
//                        self.getFloorNormal(), self.getCombo(), self.getUDamageTime(), self.getAction(),
//                        self.getEmotLeft(), self.getEmotCenter(), self.getEmotRight(), self.getBubble(),
//                        self.getAnim());
                
                //world.notifyImmediately(newSelf);
            }
        });

    }

    @Override
    public void logic() {
        // mark that another logic iteration has began
        log.info("--- Logic iteration ---");

        if(!navigation.isNavigating()) {
            navigation.navigate(MyCollections.getRandom(navPoints.getNavPoints().values()));
            ++jumpCount;
        }
        
//        //Standing jump inpection
//        if (Math.abs(zOnTheGround - bot.getLocation().z) < 10) {
//            log.warning("Sending JUMP command.");
//            move.jump();
//            ++jumpCount;
//        }
        if (jumpCount > 5) {
            log.info("No more jumps to jump, we are finished...");
            isCompleted = true;
            new Runnable() {
                public void run() {
                    bot.stop();
                }
            }.run();
        }

    }
}
