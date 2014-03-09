/*
 * Copyright (C) 2013 AMIS research group, Faculty of Mathematics and Physics, Charles University in Prague, Czech Republic
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

import cz.cuni.amis.pogamut.base.agent.navigation.IPathPlanner;
import cz.cuni.amis.pogamut.base3d.worldview.object.ILocated;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.IUT2004Navigation;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.IUT2004PathExecutor;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.NavMeshNavigator;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.NavMeshRunner;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.UT2004AcceleratedPathExecutor;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.UT2004GetBackToNavGraph;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.UT2004Navigation;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.UT2004PathExecutor;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.UT2004RunStraight;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.loquenavigator.KefikRunner;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.loquenavigator.LoqueNavigator;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.navmesh.NavMesh;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.stuckdetector.MyUT2004DistanceStuckDetector;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.stuckdetector.MyUT2004PositionStuckDetector;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.stuckdetector.UT2004DistanceStuckDetector;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.stuckdetector.UT2004PositionStuckDetector;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.stuckdetector.UT2004TimeStuckDetector;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004Bot;
import java.io.File;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Factory class for creating custom navigations from given parameters.
 * Navigation must be created at bot initialization and this allows for
 * navigation to be specified before execution without modifying bot's source
 * code.
 *
 * @author Bogo
 */
public class NavigationFactory {

    /**
     * Get {@link IPathPlanner} specified in params for given bot .
     *
     * @param bot
     * @return
     *
     */
    public static IPathPlanner getPathPlanner(EvaluatingBot bot, String pathPlannerType) {
        if (pathPlannerType != null) {
            if (pathPlannerType.equals("fwMap")) {
                return bot.getFwMap();
            } else if (pathPlannerType.equals("navMesh")) {
                try {
                    return new NavMesh();
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(NavigationFactory.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(NavigationFactory.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return bot.getFwMap();
    }

    /**
     * Get {@link IUT2004Navigation} specified in params for given bot .
     *
     * @param bot
     * @param utBot
     * @param navigationType
     * @return
     *
     */
    public static IUT2004Navigation getNavigation(EvaluatingBot bot, UT2004Bot utBot, String navigationType) {

        IUT2004PathExecutor pathExecutor;
        if (navigationType.equals("acc")) {

            pathExecutor = new UT2004AcceleratedPathExecutor<ILocated>(
                    utBot, bot.getInfo(), bot.getMove(),
                    //TODO: Switch runners by parameter. For testing new runner.
                    new LoqueNavigator<ILocated>(utBot, bot.getInfo(), bot.getMove(), new KefikRunner(utBot, bot.getInfo(), bot.getMove(), utBot.getLog()), utBot.getLog()),
                    utBot.getLog());
    		pathExecutor.addStuckDetector(new UT2004TimeStuckDetector(utBot, 3000, 100000));
    		// if the bot does not move for 3 seconds, considered that it is stuck
    		pathExecutor.addStuckDetector(new MyUT2004PositionStuckDetector(utBot));
    		// watch over the position history of the bot, if the bot does not move
    		// sufficiently enough, consider that it is stuck
    		pathExecutor.addStuckDetector(new MyUT2004DistanceStuckDetector(utBot));
    		// watch over distances to target

        } else {
            pathExecutor = new UT2004PathExecutor(utBot, bot.getInfo(), bot.getMove(),
                    new LoqueNavigator<ILocated>(utBot, bot.getInfo(), bot.getMove(), new KefikRunner(utBot, bot.getInfo(), bot.getMove(), utBot.getLog()), utBot.getLog()),
                    utBot.getLog());
    		// add stuck detectors that watch over the path-following, if it
    		// (heuristicly) finds out that the bot has stuck somewhere,
    		// it reports an appropriate path event and the path executor will stop
    		// following the path which in turn allows
    		// us to issue another follow-path command in the right time
    		pathExecutor.addStuckDetector(new UT2004TimeStuckDetector(utBot, 3000, 100000));
    		// if the bot does not move for 3 seconds, considered that it is stuck
    		pathExecutor.addStuckDetector(new UT2004PositionStuckDetector(utBot));
    		// watch over the position history of the bot, if the bot does not move
    		// sufficiently enough, consider that it is stuck
    		pathExecutor.addStuckDetector(new UT2004DistanceStuckDetector(utBot));
    		// watch over distances to target
        }
        UT2004GetBackToNavGraph getBackToNavGraph = new UT2004GetBackToNavGraph(utBot, bot.getInfo(), bot.getMove());
        UT2004RunStraight runStraight = new UT2004RunStraight(utBot, bot.getInfo(), bot.getMove());
        return new UT2004Navigation(utBot, pathExecutor, bot.getFwMap(), getBackToNavGraph, runStraight);
    }

    /**
     * Initializes the path container.
     *
     * @param pathContainer
     * @param bot
     */
    static void initializePathContainer(PathContainer pathContainer, NavigationEvaluatingBot bot) {
        BotNavigationParameters params = bot.getParams();
        if (params.isRepeatTask()) {
            File file = new File(params.getRepeatFile());
            pathContainer.buildFromFile(file, true);
        } else if (params.isOnlyRelevantPaths()) {
            pathContainer.buildRelevant(bot.getParams().getLimit());
        } else {
            pathContainer.build(bot.getParams().getLimit());
        }

    }
}
