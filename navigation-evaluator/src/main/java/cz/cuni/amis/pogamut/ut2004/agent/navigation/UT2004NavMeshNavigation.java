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
package cz.cuni.amis.pogamut.ut2004.agent.navigation;

import cz.cuni.amis.pogamut.base.agent.navigation.IPathFuture;
import cz.cuni.amis.pogamut.base.agent.navigation.IPathPlanner;
import cz.cuni.amis.pogamut.base.utils.logging.LogCategory;
import cz.cuni.amis.pogamut.base.utils.math.DistanceUtils;
import cz.cuni.amis.pogamut.base3d.worldview.object.ILocated;
import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import static cz.cuni.amis.pogamut.ut2004.agent.navigation.UT2004Navigation.AT_PLAYER;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.navmesh.NavMesh;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004Bot;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Stop;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.BotKilled;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.EndMessage;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Item;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;
import cz.cuni.amis.utils.flag.Flag;
import cz.cuni.amis.utils.flag.FlagListener;
import static cz.cuni.amis.utils.future.FutureStatus.CANCELED;
import static cz.cuni.amis.utils.future.FutureStatus.COMPUTATION_EXCEPTION;
import static cz.cuni.amis.utils.future.FutureStatus.FUTURE_IS_BEING_COMPUTED;
import static cz.cuni.amis.utils.future.FutureStatus.FUTURE_IS_READY;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Bogo
 */
public class UT2004NavMeshNavigation implements IUT2004Navigation {
    private static final int AT_PLAYER_VERTICAL = 50;
    
    //TODO: Move to runningStraight
    public static final int START_THRESHOLD = 400;

    protected LogCategory log;
    /**
     * UT2004PathExecutor that is used for the navigation.
     */
    protected IUT2004PathExecutor<ILocated> pathExecutor;
    /**
     * NavMesh that is used for path planning.
     */
    protected NavMesh pathPlanner;
    
    /**
     * TODO: More like GetBackToNavMesh
     * UT2004GetBackToNavGraph for returning bot back to the navigation graph.
     */
    protected IUT2004GetBackToNavGraph getBackToNavGraph;
    
    /**
     * Whether navigation is running.
     */
    protected boolean navigating = false;
    /**
     * Last location target.
     */
    protected ILocated lastTarget = null;
    /**
     * Last location target.
     */
    protected Player lastTargetPlayer = null;
    /**
     * Current location target.
     */
    protected ILocated currentTarget = null;
    /**
     * Current target is player (if not null)
     */
    protected Player currentTargetPlayer = null;
    /**
     * Where the bot will continue to.
     */
    protected ILocated continueTo;
    protected IPathFuture<ILocated> continueToPath;
    /**
     * Current path stored in IPathFuture object.
     */
    protected IPathFuture currentPathFuture;
    /**
     * From which distance we should use
     * {@link IUT2004PathExecutor#extendPath(List)}.
     */
    protected double extendPathThreshold;
    /**
     * UT2004Bot reference.
     */
    protected UT2004Bot bot;
    /**
     * UT2004RunStraight is used to run directly to player at some moment.
     */
    protected IUT2004RunStraight runStraight;
    
    
    /**
     * Whether we're using {@link IUT2004Navigation#getBackToNavGraph}.
     */
    protected boolean usingGetBackToNavGraph = false;
    /**
     * We're running straight to the player.
     */
    protected boolean runningStraightToPlayer = false;
    /**
     * Where run-straight failed.
     */
    protected Location runningStraightToPlayerFailedAt = null;
    
    /**
     * State of UT2004Navigation
     */
    protected Flag<NavigationState> state = new Flag<NavigationState>(NavigationState.STOPPED);
    private double PLAYER_DISTANCE_THRESHOLD = 600.0;

    public void addStrongNavigationListener(FlagListener<NavigationState> listener) {
        state.addStrongListener(listener);
    }

    public void removeStrongNavigationListener(FlagListener<NavigationState> listener) {
        state.removeListener(listener);
    }

    public IUT2004PathExecutor<ILocated> getPathExecutor() {
        return pathExecutor;
    }

    /**
     * TODO: Evaluate if we need/want this. Update: Possibly yes, but accustomed
     * to navMesh, e.g. test if we are in any polygon OR near off mesh
     * navpoint...
     *
     * @return
     *
     */
    public IUT2004GetBackToNavGraph getBackToNavGraph() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public IUT2004RunStraight getRunStraight() {
        return runStraight;
    }

    public boolean isNavigating() {
        return navigating;
    }

    public boolean isNavigatingToNavPoint() {
        return navigating && getCurrentTarget() instanceof NavPoint;
    }

    public boolean isNavigatingToItem() {
        return navigating && getCurrentTarget() instanceof Item;
    }

    public boolean isNavigatingToPlayer() {
        return navigating && getCurrentTarget() instanceof Player;
    }

    /**
     * TODO: Remove/accustome this.
     *
     * @return
     *
     */
    public boolean isTryingToGetBackToNav() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean isPathExecuting() {
        return pathExecutor.isExecuting();
    }

    public boolean isRunningStraight() {
        return runStraight.isExecuting();
    }

    public ILocated getFocus() {
        return pathExecutor.getFocus();
    }

    public void setFocus(ILocated located) {
        pathExecutor.setFocus(located);
        //getBackToNavGraph.setFocus(located);
        runStraight.setFocus(located);
    }

    public void stopNavigation() {
        //TODO: reset()
        bot.getAct().act(new Stop());
    }

    public void navigate(ILocated target) {
        if (target == null) {
            if (log != null && log.isLoggable(Level.WARNING)) {
                log.warning("Cannot navigate to NULL target!");
            }
            reset(true, NavigationState.STOPPED);
            return;
        }

        if (target instanceof Player) {
            // USE DIFFERENT METHOD INSTEAD
            navigate((Player) target);
            return;
        }

        if (navigating) {
            if (currentTarget == target || currentTarget.getLocation().equals(target.getLocation())) {
                // just continue with current execution
                return;
            }
            // NEW TARGET!
            // => reset - stops pathExecutor as well, BUT DO NOT STOP getBackOnPath (we will need to do that eventually if needed, or it is not running)
            reset(false, null);
        }

        if (log != null && log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "Start navigating to: {0}", target);
        }

        navigating = true;
        switchState(NavigationState.NAVIGATING);

        currentTarget = target;

        navigate();
    }

    public void navigate(Player player) {
        if (player == null) {
            if (log != null && log.isLoggable(Level.WARNING)) {
                log.warning("Cannot navigate to NULL player!");
            }
            return;
        }

        if (navigating) {
            if (currentTarget == player) {
                // just continue with the execution
                return;
            }
            // NEW TARGET!
            // => reset - stops pathExecutor as well, BUT DO NOT STOP getBackOnPath (we will need to do that eventually if needed, or it is not running)
            reset(false, null);
        }

        if (log != null && log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "Start pursuing: {0}", player);
        }

        navigating = true;
        switchState(NavigationState.NAVIGATING);

        // Current target and currentTarget player should refer to the same object.
        // Current target is used by navigatePlayer to compute new destination and 
        // by this method to see if the target has changed.
        currentTarget = player;
        currentTargetPlayer = player;

        navigate();
    }

    public void navigate(IPathFuture<ILocated> pathHandle) {
        if (pathHandle == null) {
            if (log != null && log.isLoggable(Level.WARNING)) {
                log.warning("Cannot navigate to NULL pathHandle!");
            }
            return;
        }

        if (navigating) {
            if (currentPathFuture == pathHandle) {
                // just continue with the execution
                return;
            }
            // NEW TARGET!
            // => reset - stops pathExecutor as well, BUT DO NOT STOP getBackOnPath (we will need to do that eventually if needed, or it is not running)
            reset(false, null);
        }

        if (log != null && log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "Start running along the path to target: {0}", pathHandle.getPathTo());
        }

        navigating = true;
        switchState(NavigationState.NAVIGATING);

        currentTarget = pathHandle.getPathTo();
        currentPathFuture = pathHandle;

        navigate();
    }

    public void setContinueTo(ILocated continueTo) {
        if (!isNavigating()) {
            log.log(Level.WARNING, "Cannot continueTo({0}) as navigation is not navigating!", continueTo);
            return;
        }
        if (isNavigatingToPlayer()) {
            log.log(Level.WARNING, "Cannot continueTo({0}) as we're navigating to player!", continueTo);
            return;
        }
        this.continueTo = continueTo;

        this.continueToPath = pathPlanner.computePath(currentTarget, continueTo);

        checkExtendPath();
    }

    public ILocated getContinueTo() {
        return continueTo;
    }

    public NavPoint getNearestNavPoint(ILocated location) {
        if (location == null) {
            return null;
        }
        if (location instanceof NavPoint) {
            return (NavPoint) location;
        }
        if (location instanceof Item) {
            if (((Item) location).getNavPoint() != null) {
                return ((Item) location).getNavPoint();
            }
        }
        return DistanceUtils.getNearest(bot.getWorldView().getAll(NavPoint.class).values(), location);
    }

    public List<ILocated> getCurrentPathCopy() {
        List<ILocated> result = new ArrayList();
        if (currentPathFuture != null) {
            result.addAll(currentPathFuture.get());
        }
        return result;
    }

    public List<ILocated> getCurrentPathDirect() {
        if (currentPathFuture != null) {
            return currentPathFuture.get();
        }
        return null;
    }

    @Override
    public ILocated getCurrentTarget() {
        return currentTarget;
    }

    public Player getCurrentTargetPlayer() {
        return currentTargetPlayer;
    }

    public Item getCurrentTargetItem() {
        if (currentTarget instanceof Item) {
            return (Item) currentTarget;
        }
        return null;
    }

    public NavPoint getCurrentTargetNavPoint() {
        if (currentTarget instanceof NavPoint) {
            return (NavPoint) currentTarget;
        }
        return null;
    }

    public ILocated getLastTarget() {
        return lastTarget;
    }

    public Player getLastTargetPlayer() {
        return lastTargetPlayer;
    }

    public Item getLastTargetItem() {
        if (lastTarget instanceof Item) {
            return (Item) lastTarget;
        }
        return null;
    }

    public Flag<NavigationState> getState() {
        return state;
    }

    public double getRemainingDistance() {
        if (!isNavigating()) {
            return 0;
        }
        if (isPathExecuting()) {
            return pathExecutor.getRemainingDistance() + (isNavigatingToPlayer() ? pathExecutor.getPathTo().getLocation().getDistance(currentTargetPlayer.getLocation()) : 0);
        } else {
            // TODO: HOW TO GET TRUE DISTANCE, IF YOU MAY HAVE ASYNC PATH-PLANNER?
            IPathFuture<ILocated> pathFuture = pathPlanner.computePath(bot.getLocation(), isNavigatingToPlayer() ? currentTargetPlayer.getLocation() : currentTarget.getLocation());
            if (pathFuture.isDone()) {
                return getPathDistance(pathFuture.get());
            } else {
                // CANNOT BE COMPUTED DUE TO ASYNC PATH-PLANNER
                return -1;
            }
        }
    }

    public Logger getLog() {
        return log;
    }

    private void checkExtendPath() {
        if (continueTo == null) {
            return;
        }
        if (continueToPath == null) {
            log.severe("continueTo specified, but continueToPath is NULL!");
            return;
        }
        if (isNavigatingToPlayer()) {
            log.warning("continueTo specified, but navigating to Player, INVALID!");
            return;
        }
        if (isPathExecuting()) {
            double remainingDistance = getRemainingDistance();
            if (remainingDistance < extendPathThreshold) {
                if (!continueToPath.isDone()) {
                    log.log(Level.WARNING, "Should extend path, remainingDistance = {0} < {1} = extendPathThreshold, but continueToPath.isDone() == false, cannot extend path!", new Object[]{remainingDistance, extendPathThreshold});
                    return;
                }
                log.log(Level.INFO, "Extending path to continue to {0}", continueTo);

                pathExecutor.extendPath(((List) continueToPath.get()));

                // ADJUST INTERNALS
                currentPathFuture = pathExecutor.getPathFuture();
                lastTarget = currentTarget;
                currentTarget = continueTo;

                continueTo = null;
                continueToPath = null;
            }
        }
    }
    
    protected void switchState(NavigationState newState) {
        state.setFlag(newState);
    }

    private double getPathDistance(List<ILocated> list) {
        if (list == null || list.size() <= 0) {
            return 0;
        }
        double distance = 0;
        ILocated curr = list.get(0);
        for (int i = 1; i < list.size(); ++i) {
            ILocated next = list.get(i);
            distance += curr.getLocation().getDistance(next.getLocation());
            curr = next;
        }
        return distance;
    }
    
    // ======================
    // UTILITY METHODS
    // ======================
    protected void navigate() {
        if (!navigating) {
            return;
        }

        if (log != null && log.isLoggable(Level.FINE)) {
            log.fine("NAVIGATING");
        }
        if (currentTargetPlayer != null) {
            if (log != null && log.isLoggable(Level.FINE)) {
                log.log(Level.FINE, "Pursuing {0}", currentTargetPlayer);
            }
            navigatePlayer();
        } else {
            if (log != null && log.isLoggable(Level.FINE)) {
                log.log(Level.FINE, "Navigating to {0}", currentTarget);
            }
            navigateLocation();
        }
    }
    
    private void navigateLocation() {
        if (isPathExecuting()) {
            // Navigation is driven by Path Executor already...	
            // => check continueTo
            checkExtendPath();
            if (log != null && log.isLoggable(Level.FINE)) {
                log.fine("Path executor running");
            }
            return;
        }

        // PATH EXECUTOR IS NOT RUNNING
        // => we have not started to run along path yet

        // ARE WE ON NAV-GRAPH?
        if (!getBackToNavGraph.isOnNavGraph()) {
            // NO!
            // => get back to navigation graph
            if (log != null && log.isLoggable(Level.FINE)) {
                log.fine("Getting back to navigation graph");
            }
            if (getBackToNavGraph.isExecuting()) {
                // already running
                return;
            }
            if (usingGetBackToNavGraph) {
                // GetBackToNavGraph was already called && stopped && we're still not on nav graph
                // => stuck
                if (log != null && log.isLoggable(Level.WARNING)) {
                    log.warning("UT2004Navigation:stuck(). GetBackToNavGraph was already called && stopped && we're still not on nav graph.");
                }
                stuck();
                return;
            }
            getBackToNavGraph.backToNavGraph();
            // => mark that we're using GetBackToNavGraph
            usingGetBackToNavGraph = true;
            return;
        } else {
            usingGetBackToNavGraph = false;
        }
        // YES!    	
        // ... getBackToNavGraph will auto-terminate itself when we manage to get back to graph

        if (currentPathFuture == null) {

            if (log != null && log.isLoggable(Level.FINE)) {
                log.log(Level.FINE, "Computing path from {0} to {1}", new Object[]{bot.getLocation(), currentTarget});
            }

            currentPathFuture = pathPlanner.computePath(bot.getLocation(), currentTarget);
        }

        switch (currentPathFuture.getStatus()) {
            case FUTURE_IS_READY:
                // ALL OK!
                break;
            case FUTURE_IS_BEING_COMPUTED:
                if (log != null && log.isLoggable(Level.FINE)) {
                    log.fine("Waiting for the path to be computed...");
                }
                return;
            case CANCELED:
                if (log != null && log.isLoggable(Level.WARNING)) {
                    log.warning("Path computation has been canceled.");
                }
                noPath();
                return;
            case COMPUTATION_EXCEPTION:
                if (log != null && log.isLoggable(Level.WARNING)) {
                    log.warning("Path computation has failed with an exception.");
                }
                noPath();
                return;
        }

        // PATH IS READY!
        // => tinker the path
        if (!processPathFuture(currentPathFuture)) {
            noPath();
            return;
        }
        // => let's start running
        pathExecutor.followPath(currentPathFuture);
    }
    
    private void navigatePlayer() {
        double vDistance = bot.getLocation().getDistanceZ(currentTargetPlayer.getLocation());
        double hDistance = bot.getLocation().getDistance2D(currentTargetPlayer.getLocation());

        if (hDistance < AT_PLAYER && vDistance < AT_PLAYER_VERTICAL) {
            // player reached
            if (log != null && log.isLoggable(Level.FINE)) {
                log.fine("Player reached");
            }
            if (pathExecutor.isExecuting()) {
                //Set end of path to current bot location to stop the executor.
                pathExecutor.getPath().set(pathExecutor.getPath().size() - 1, bot.getLocation());
            } else {
                targetReached();
            }
            return;
        }

        if (hDistance < START_THRESHOLD && Math.abs(vDistance) < AT_PLAYER_VERTICAL) {
            // RUN STRAIGHT			
            if (runningStraightToPlayer) {
                if (runStraight.isFailed()) {
                    runningStraightToPlayer = false;
                    runningStraightToPlayerFailedAt = bot.getLocation();
                }
            } else {
                if (runningStraightToPlayerFailedAt == null || // we have not failed previously
                        bot.getLocation().getDistance(runningStraightToPlayerFailedAt) > 500 // or place where we have failed is too distant
                        ) {
                    if (getBackToNavGraph.isExecuting()) {
                        getBackToNavGraph.stop();
                        usingGetBackToNavGraph = false;
                    }
                    if (pathExecutor.isExecuting()) {
                        pathExecutor.stop();
                    }
                    runningStraightToPlayer = true;
                    runningStraightToPlayerFailedAt = null;
                    runStraight.runStraight(currentTargetPlayer);
                }
            }
            if (runningStraightToPlayer) {
                if (log != null && log.isLoggable(Level.FINE)) {
                    log.fine("Running straight to player");
                }
                return;
            }
        } else {
            if (runningStraightToPlayer) {
                runningStraightToPlayer = false;
                runStraight.stop(false);
            }
        }

        if (pathExecutor.isExecuting()) {
            // Navigation is driven by Path Executor already...			
            if (log != null && log.isLoggable(Level.FINE)) {
                log.fine("Path executor running");
            }
            // check distance between point we're navigating to and current player's location
            double distance = currentTarget.getLocation().getDistance(currentTargetPlayer.getLocation());

            if (distance < PLAYER_DISTANCE_THRESHOLD) {
                // PLAYER DID NOT MOVED TOO MUCH FROM ITS ORIGINAL POSITION
                // => continue running using pathExecutor
                return;
            }

            if (log != null && log.isLoggable(Level.FINE)) {
                log.log(Level.FINE, "Player moved {0} from its original location, checking path...", distance);
            }
            // WE NEED TO CHECK ON PATH!	
            //TODO: Implement for NavMesh - nearest INavMeshAtom?
    //        NavPoint newToNavPoint = getNearestNavPoint(currentTargetPlayer);
    //        if (newToNavPoint != toNavPoint) {
                // WE NEED TO ALTER THE PATH!
                if (log != null && log.isLoggable(Level.FINE)) {
                    log.log(Level.FINE, "Replanning path to get to {0}", currentTargetPlayer);
                }
                pathExecutor.stop();
                currentPathFuture = null;
            } else {
                if (log != null && log.isLoggable(Level.FINE)) {
                    log.fine("Path remains the same");
                }
                return;
            }
      //  }
        
        navigateLocation();
    }
    
    protected void noPath() {
        // DAMN ...
        reset(true, NavigationState.PATH_COMPUTATION_FAILED);
    }

    protected void stuck() {
        // DAMN ...
        reset(true, NavigationState.STUCK);
    }

    protected void targetReached() {
        // COOL !!!
        reset(true, NavigationState.TARGET_REACHED);
    }

    protected void reset(boolean stopGetBackToNavGraph, NavigationState resultState) {
        if (currentTarget != null) {
            lastTarget = currentTarget;
            lastTargetPlayer = currentTargetPlayer;
        }

        navigating = false;

        currentTarget = null;
        currentTargetPlayer = null;

        currentPathFuture = null;

        //runningStraightToPlayer = false;
        //runningStraightToPlayerFailedAt = null;

        continueTo = null;
        continueToPath = null;

        pathExecutor.stop();
        runStraight.stop(false);
        if (stopGetBackToNavGraph) {
            getBackToNavGraph.stop();
            usingGetBackToNavGraph = false;
        }


        if (resultState == null) {
            return;
        }
        switchState(resultState);
    }
    
    /**
     * Checks if last path element is in close distance from our desired target
     * and if not, we will add our desired target as the last path element.
     *
     * @param futurePath
     * @return 
     */
    protected boolean processPathFuture(IPathFuture futurePath) {
        List<ILocated> pathList = futurePath.get();

        if (pathList == null) {
            // we failed to compute the path, e.g., path does not exist
            return false;
        }
        if (currentTarget == null) {
            if (pathList.isEmpty()) {
                return false;
            }
            currentTarget = pathList.get(pathList.size() - 1);
        }
        return true;
    }
    
    // ===========
    // CONSTRUCTOR
    // ===========
    /**
     * Here you may specify any custom UT2004Navigation parts.
     *
     * @param bot
     * @param ut2004PathExecutor
     * @param pathPlanner
     * @param getBackOnPath
     * @param runStraight
     */
    public UT2004NavMeshNavigation(UT2004Bot bot, IUT2004PathExecutor ut2004PathExecutor, IPathPlanner<NavPoint> pathPlanner, IUT2004GetBackToNavGraph getBackOnPath, IUT2004RunStraight runStraight) {
        this(bot, ut2004PathExecutor, pathPlanner, getBackOnPath, runStraight, EXTEND_PATH_THRESHOLD);
    }

    /**
     * Here you may specify any custom UT2004Navigation parts.
     *
     * @param bot
     * @param ut2004PathExecutor
     * @param pathPlanner
     * @param getBackOnPath
     * @param runStraight
     */
    public UT2004NavMeshNavigation(UT2004Bot bot, IUT2004PathExecutor ut2004PathExecutor, NavMesh navMesh, IUT2004GetBackToNavGraph getBackOnPath, IUT2004RunStraight runStraight, double extendPathThreshold) {
        this.log = bot.getLogger().getCategory(this.getClass().getSimpleName());
        this.bot = bot;

        this.pathPlanner = pathPlanner;
        this.pathExecutor = ut2004PathExecutor;

        this.getBackToNavGraph = getBackOnPath;
        this.runStraight = runStraight;

        this.extendPathThreshold = extendPathThreshold;

        initListeners();
    }
    
    private void initListeners() {
        this.pathExecutor.getState().addListener(myUT2004PathExecutorStateListener);
        bot.getWorldView().addEventListener(EndMessage.class, endMessageListener);
        bot.getWorldView().addEventListener(BotKilled.class, botKilledMessageListener);
    }
}
