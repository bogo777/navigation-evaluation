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

import cz.cuni.amis.pogamut.base.agent.navigation.IPathExecutorState;
import cz.cuni.amis.pogamut.base.agent.navigation.IPathFuture;
import cz.cuni.amis.pogamut.base.agent.navigation.PathExecutorState;
import static cz.cuni.amis.pogamut.base.agent.navigation.PathExecutorState.PATH_COMPUTATION_FAILED;
import static cz.cuni.amis.pogamut.base.agent.navigation.PathExecutorState.STOPPED;
import static cz.cuni.amis.pogamut.base.agent.navigation.PathExecutorState.STUCK;
import static cz.cuni.amis.pogamut.base.agent.navigation.PathExecutorState.TARGET_REACHED;
import cz.cuni.amis.pogamut.base3d.worldview.object.ILocated;
import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.ut2004.agent.module.utils.TabooSet;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.astar.UT2004AStar;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.floydwarshall.FloydWarshallMap;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.stuckdetector.UT2004DistanceStuckDetector;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.stuckdetector.UT2004PositionStuckDetector;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.stuckdetector.UT2004TimeStuckDetector;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004Bot;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004BotModuleController;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Initialize;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.BotKilled;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.ConfigChange;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.GameInfo;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.InitedMessage;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Self;
import cz.cuni.amis.pogamut.ut2004.navigation.evaluator.data.EvaluationResult;
import cz.cuni.amis.pogamut.ut2004.navigation.evaluator.data.PathResult;
import cz.cuni.amis.pogamut.ut2004.utils.UT2004BotRunner;
import cz.cuni.amis.utils.collections.MyCollections;
import cz.cuni.amis.utils.exception.PogamutException;
import cz.cuni.amis.utils.flag.FlagListener;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

/**
 * Bot for evaluating navigations. Initialized with navigation given by parameters and performs evaluation on given map.
 *
 * @author Bogo
 */
public class NavigationEvaluatingBot extends UT2004BotModuleController {

    /**
     * Taboo set is working as "black-list", that is you might add some
     * NavPoints to it for a certain time, marking them as "unavailable".
     */
    protected TabooSet<NavPoint> tabooNavPoints;
    private PathContainer pathContainer;
    private NavPoint target;
    private Path currentPath;
    private NavigationState state;
    private Date startDate;
    private EvaluationResult result;
    //TODO: Move
    int total = 0;
    int processed = 0;


    public NavigationEvaluatingBot() {
    }

    public BotNavigationParameters getParams() {
        return (BotNavigationParameters) bot.getParams();
    }

    @Override
    protected void initializePathFinding(UT2004Bot bot) {
        fwMap = new FloydWarshallMap(bot);
        pathPlanner = NavigationFactory.getPathPlanner(this);
        navigation = NavigationFactory.getNavigation(this, bot);

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

    /**
     * Here we can modify initializing command for our bot.
     *
     * @return
     */
    @Override
    public Initialize getInitializeCommand() {
        return new Initialize().setName("NavigationEvaluatingBot");
    }

    /**
     * The bot is initialized in the environment - a physical representation of
     * the bot is present in the game.
     *
     * @param config information about configuration
     * @param init information about configuration
     */
    @SuppressWarnings("unchecked")
    @Override
    public void botInitialized(GameInfo gameInfo, ConfigChange config, InitedMessage init) {
        // initialize taboo set where we store temporarily unavailable navpoints
        tabooNavPoints = new TabooSet<NavPoint>(bot);

        pathContainer = new PathContainer(world);
        pathContainer.buildRelevant(getParams().getLimit());

        total = pathContainer.size();
        result = new EvaluationResult(total, info.game.getMapName(), log, getParams().getResultPath());

        state = NavigationState.NotMoving;

        //TODO: Figure out what to do with this?
        // auto-removes wrong navigation links between navpoints
        //autoFixer = new UT2004PathAutoFixer(bot, pathExecutor, fwMap, navBuilder);

        // IMPORTANT
        // adds a listener to the path executor for its state changes, it will allow you to 
        // react on stuff like "PATH TARGET REACHED" or "BOT STUCK"
        pathExecutor.getState().addStrongListener(new FlagListener<IPathExecutorState>() {
            @Override
            public void flagChanged(IPathExecutorState changedValue) {
                pathExecutorStateChange(changedValue.getState());
            }
        });


    }

    /**
     * The bot is initialized in the environment - a physical representation of
     * the bot is present in the game.
     *
     * @param config information about configuration
     * @param init information about configuration
     */
    @Override
    public void botFirstSpawn(GameInfo gameInfo, ConfigChange config, InitedMessage init, Self self) {
        // receive logs from the navigation so you can get a grasp on how it is working
        pathExecutor.getLog().setLevel(Level.ALL);
    }

    /**
     * This method is called only once right before actual logic() method is
     * called for the first time.
     */
    @Override
    public void beforeFirstLogic() {
    }

    /**
     * Main method that controls the bot - makes decisions what to do next. It
     * is called iteratively by Pogamut engine every time a synchronous batch
     * from the environment is received. This is usually 4 times per second - it
     * is affected by visionTime variable, that can be adjusted in GameBots ini
     * file in UT2004/System folder.
     */
    @Override
    public void logic() {
        // mark that another logic iteration has began
        log.info("--- Logic iteration ---");

        // maybe we should end? WE should not, we can have valid path in currentPath
//        if (pathContainer.isEmpty()) {
//            wrapUpEvaluation();
//        }

        // get next request
        if (currentPath == null) {
            currentPath = getNextPath(null);
        }

        navigatePath();
    }

    private void navigatePath() {
        log.info(String.format("Navigating path ID = %s", currentPath.getId()));

        if (state == NavigationState.Navigating) {
            boolean reachedTarget = info.atLocation(currentPath.getEnd().getLocation());
            if (reachedTarget) {
                state = NavigationState.AtTheTarget;
                log.info("Reached end of path...");
            } else {
                //Do nothing
                log.info("Navigation in progress...");
                return;
            }
        }
        if (state == NavigationState.OnWayToStart) {
            boolean reachedStart = info.atLocation(currentPath.getStart().getLocation());
            if (reachedStart) {
                state = NavigationState.NotMoving;
                log.info("Reached start of path...");
            } else {
                //Do nothing
                log.info("Navigating to start in progress...");
                return;
            }
        }

        if (state == NavigationState.AtTheTarget) {
            //Write result and find new path -> through NotMoving
            log.info("Successfuly reached end. Hooray!");
            ++processed;
            result.addResult(currentPath, PathResult.ResultType.Completed, (new Date()).getTime() - startDate.getTime());
            log.info(String.format("Completed %d/%d paths...", processed, total));
            currentPath = getNextPath(currentPath.getEnd());

            state = NavigationState.NotMoving;
        }
        if (state == NavigationState.Failed) {
            //Write failed report and find new path -> through NotMoving
            log.info("Navigation failed!");
            result.addResult(currentPath, PathResult.ResultType.Failed, startDate == null ? 0 : (new Date()).getTime() - startDate.getTime());
            currentPath = getNextPath(info.getNearestNavPoint());
            state = NavigationState.NotMoving;
        }

        if (state != NavigationState.NotMoving) {
            //Should not happen
            log.severe("Invalid state!");
            return;
        }

        //Determine where are you - Start(navigate currentPath) X Elsewhere(navigate to start)
        boolean atStart = info.atLocation(currentPath.getStart());
        if (atStart) {
            IPathFuture<ILocated> path = pathPlanner.computePath(currentPath.getStart(), currentPath.getEnd());
            while (path == null || path.get() == null) {
                //Failed to build path
                result.addResult(currentPath, PathResult.ResultType.NotBuilt, 0);
                currentPath = getNextPath(currentPath.getStart());
                path = pathPlanner.computePath(currentPath.getStart(), currentPath.getEnd());
            }
            log.info("Starting navigation on the path.");
            startDate = new Date();
            navigation.navigate(path);
            state = NavigationState.Navigating;
        } else {
//            IPathFuture<ILocated> pathToStart = pathPlanner.computePath(info.getLocation(), currentPath.getStart());
//            if(pathToStart == null || pathToStart.get() == null) {
//                //Failed to build path to start navpoint of current path.
//            }
            log.info("Starting navigation to the start.");
            navigation.navigate(currentPath.getStart());
            state = NavigationState.OnWayToStart;
        }
    }

    private double getPathLength(IPathFuture<ILocated> path) {
        List<ILocated> list = path.get();

        double sum = 0;

        for (int i = 0; i < list.size() - 1; i++) {
            Location l1 = list.get(i).getLocation();
            Location l2 = list.get(i + 1).getLocation();
            double distance = l1.getDistance(l2);
            sum += distance;
        }
        return sum;
    }

    /**
     * Called each time our bot die. Good for reseting all bot state dependent
     * variables.
     *
     * @param event
     */
    @Override
    public void botKilled(BotKilled event) {
        navigation.stopNavigation();
    }

    /**
     * Path executor has changed its state (note that
     * {@link UT2004BotModuleController#getPathExecutor()} is internally used by
     * {@link UT2004BotModuleController#getNavigation()} as well!).
     *
     * @param state
     */
    protected void pathExecutorStateChange(PathExecutorState state) {
        switch (state) {
            case PATH_COMPUTATION_FAILED:
                // if path computation fails to whatever reason, just try another navpoint
                // taboo bad navpoint for 3 minutes
                tabooNavPoints.add(target, 180);
                break;

            case TARGET_REACHED:
                // taboo reached navpoint for 3 minutes
                //tabooNavPoints.add(targetNavPoint, 180);
                break;

            case STUCK:
                // the bot has stuck! ... target nav point is unavailable currently
                tabooNavPoints.add(target, 60);
                this.state = NavigationState.Failed;
                //giveUp();

                break;

            case STOPPED:
                // path execution has stopped
                target = null;
                break;
        }
    }

    /**
     * Randomly picks some navigation point to head to.
     *
     * @return randomly choosed navpoint
     */
    protected NavPoint getRandomNavPoint() {
        log.info("Picking new target navpoint.");

        // choose one feasible navpoint (== not belonging to tabooNavPoints) randomly
        NavPoint chosen = MyCollections.getRandomFiltered(getWorldView().getAll(NavPoint.class).values(), tabooNavPoints);

        if (chosen != null) {
            return chosen;
        }

        log.warning("All navpoints are tabooized at this moment, choosing navpoint randomly!");

        // ok, all navpoints have been visited probably, try to pick one at random
        return MyCollections.getRandom(getWorldView().getAll(NavPoint.class).values());
    }

    public static void main(String args[]) throws PogamutException {
        // wrapped logic for bots executions, suitable to run single bot in single JVM

        // we're forcingly setting logging to aggressive level FINER so you can see (almost) all logs 
        // that describes decision making behind movement of the bot as well as incoming environment events
        new UT2004BotRunner(NavigationEvaluatingBot.class, "NavigationEvaluatingBot").setMain(true).setLogLevel(Level.FINE).startAgent();
    }

//    private void giveUp() {
//        currentRequest.pathInfos.get(this.testType).success = false;
//        navigation.stopNavigation();
//        boolean wasOnWay = false;
//        if (onTheWay) {
//            wasOnWay = true;
//        }
//        onTheWay = false;
//        if (this.testType.equals("mesh")) {
//            this.requestsProcessed++;
//            if (wasOnWay) {
//                updateStats(currentRequest);
//            }
//            currentRequest = null;
//        } else {
//            this.testType = "mesh";
//        }
//    }
    private Path getNextPath(NavPoint end) {
        Path path = end == null ? pathContainer.getPath() : pathContainer.getPath(end);
        if (path == null) {
            path = pathContainer.getPath();
        }
        if (path == null || processed >= getParams().getLimit()) {
            wrapUpEvaluation();
        }
        return path;
    }

    private void wrapUpEvaluation() {
        log.info("No more paths to navigate, we are finished...");
        result.exportAggregate(getParams().getResultUnique());
        result.export(getParams().getResultUnique());
        bot.stop();
    }
}
