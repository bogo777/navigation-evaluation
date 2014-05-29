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

import cz.cuni.amis.pogamut.base.communication.worldview.IWorldView;
import cz.cuni.amis.pogamut.base.communication.worldview.object.IWorldObjectEventListener;
import cz.cuni.amis.pogamut.base.communication.worldview.object.event.WorldObjectUpdatedEvent;
import cz.cuni.amis.pogamut.base.utils.math.DistanceUtils;
import cz.cuni.amis.pogamut.base3d.worldview.object.ILocated;
import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.AgentInfo;
import cz.cuni.amis.pogamut.ut2004.bot.command.AdvancedLocomotion;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004Bot;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Mover;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPointNeighbourLink;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Self;
import cz.cuni.amis.pogamut.ut2004.utils.LinkFlag;
import cz.cuni.amis.pogamut.ut2004.utils.UnrealUtils;
import cz.cuni.amis.utils.NullCheck;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Bogo
 * @param <PATH_ELEMENT>
 */
public class NavMeshNavigator<PATH_ELEMENT extends ILocated> extends AbstractUT2004PathNavigator<PATH_ELEMENT> {

    /**
     * Current navigation destination.
     */
    private Location navigDestination = null;

    /**
     * Current stage of the navigation.
     */
    private Stage navigStage = Stage.COMPLETED;

    /**
     * Current focus of the bot, if null, provide default focus.
     * <p>
     * <p>
     * Filled at the beginning of the
     * {@link LoqueNavigator#navigate(ILocated, int)}.
     */
    private ILocated focus = null;

    /**
     * {@link Self} listener.
     */
    private class SelfListener implements IWorldObjectEventListener<Self, WorldObjectUpdatedEvent<Self>> {

        private IWorldView worldView;

        /**
         * Constructor. Registers itself on the given WorldView object.
         *
         * @param worldView WorldView object to listent to.
         */
        public SelfListener(IWorldView worldView) {
            this.worldView = worldView;
            worldView.addObjectListener(Self.class, WorldObjectUpdatedEvent.class, this);
        }

        @Override
        public void notify(WorldObjectUpdatedEvent<Self> event) {
            self = event.getObject();
        }
    }

    /**
     * {@link Self} listener
     */
    private SelfListener selfListener;

    /*========================================================================*/
    /**
     * Distance, which is considered as close enough for considering the bot to
     * be AT LOCATION/NAV POINT.
     *
     * If greater than 50, navigation will failed on DM-Flux2 when navigating
     * between health vials in one of map corers.
     */
    public static final int CLOSE_ENOUGH = 50;

    /*========================================================================*/
    @Override
    protected void navigate(ILocated focus, int pathElementIndex) {
        if (log != null && log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "Current stage: {0}", navigStage);
        }
        this.focus = focus;
        switch (navigStage = keepNavigating()) {
            case AWAITING_MOVER:
            case RIDING_MOVER:
                setBotWaiting(true);
                break;
            case TELEPORT:
            case NAVIGATING:
            case REACHING:
                setBotWaiting(false);
                break;

            case TIMEOUT:
            case CRASHED:
            case CANCELED:
                if (log != null && log.isLoggable(Level.WARNING)) {
                    log.log(Level.WARNING, "Navigation {0}", navigStage);
                }
                executor.stuck();
                return;

            case COMPLETED:
                executor.targetReached();
                break;
        }
        if (log != null && log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "Next stage: {0}", navigStage);
        }
    }

    @Override
    public void reset() {
        // reinitialize the navigator's values

        navigCurrentLocation = null;
        navigCurrentNode = null;
        navigCurrentLink = null;
        navigDestination = null;
        navigIterator = null;
        navigLastLocation = null;
        navigLastNode = null;
        navigNextLocation = null;
        navigNextNode = null;
        navigNextLocationOffset = 0;
        navigStage = Stage.COMPLETED;
        setBotWaiting(false);

        resetNavigMoverVariables();
    }

    @Override
    public void newPath(List<PATH_ELEMENT> path) {
        // prepare for running along new path
        reset();

        // 1) obtain the destination
        Location dest = path.get(path.size() - 1).getLocation();

        // 2) init the navigation
        initPathNavigation(dest, path);
    }

    @Override
    public void pathExtended(List<PATH_ELEMENT> path, int currentPathIndex) {
        if (path == null || path.size() == 0) {
            throw new RuntimeException("path is null or 0-sized!");
        }
        navigDestination = path.get(path.size() - 1).getLocation();
        navigIterator = path.iterator();

        int newOffset = -currentPathIndex;
        for (int i = 0; i < path.size() && i < currentPathIndex + navigNextLocationOffset && navigIterator.hasNext(); ++i) {
            ++newOffset;
            navigIterator.next();
        }
        log.fine("PATH EXTEND ... curr index " + currentPathIndex + ", old offset " + navigNextLocationOffset + ", new offset " + newOffset + ", path size " + path.size());
        navigNextLocationOffset = newOffset;
    }

    public NavPointNeighbourLink getCurrentLink() {
        return navigCurrentLink;
    }

    /*========================================================================*/
    /**
     * Initializes direct navigation to the specified destination.
     *
     * @param dest Destination of the navigation.
     * @param timeout Maximum timeout of the navigation. Use 0 to auto-timeout.
     */
    protected void initDirectNavigation(Location dest) {
        // calculate destination distance
        int distance = (int) memory.getLocation().getDistance(dest);
        // init the navigation
        if (log != null && log.isLoggable(Level.FINE)) {
            log.fine(
                    "LoqueNavigator.initDirectNavigation(): initializing direct navigation"
                    + ", distance " + distance
            );
        }
        // init direct navigation
        initDirectly(dest);
    }

    /*========================================================================*/
    /**
     * Initializes navigation to the specified destination along specified path.
     *
     * @param dest Destination of the navigation.
     * @param path Navigation path to the destination.
     */
    protected void initPathNavigation(Location dest, List<PATH_ELEMENT> path) {
        // init the navigation
        if (log != null && log.isLoggable(Level.FINE)) {
            log.fine(
                    "LoqueNavigator.initPathNavigation(): initializing path navigation"
                    + ", nodes " + path.size()
            );
        }
        // init path navigation
        if (!initAlongPath(dest, path)) {
            // do it directly then..
            initDirectNavigation(dest);
        }
    }

    /*========================================================================*/
    /**
     * Navigates with the current navigation request.
     *
     * @return Stage of the navigation progress.
     */
    protected Stage keepNavigating() {
        // is there any point in navigating further?
        if (navigStage.terminated) {
            return navigStage;
        }

        if (log != null && log.isLoggable(Level.FINE)) {
            if (navigLastNode != null) {
                log.fine("LoqueNavigator.keepNavigating(): navigating from " + navigLastNode.getId().getStringId() + navigLastNode.getLocation());
            } else if (navigLastLocation != null) {
                log.fine("LoqueNavigator.keepNavigating(): navigating from " + navigLastLocation + " (navpoint is unknown)");
            }
            if (navigCurrentNode != null) {
                log.fine("LoqueNavigator.keepNavigating(): navigating to   " + navigCurrentNode.getId().getStringId() + navigCurrentNode.getLocation());
            } else if (navigCurrentLocation != null) {
                log.fine("LoqueNavigator.keepNavigating(): navigating to       " + navigCurrentLocation + " (navpoint is unknown)");
            }
            if (navigLastLocation != null && navigCurrentLocation != null) {
                log.fine("LoqueNavigator.keepNavigating(): distance in-between " + navigCurrentLocation.getDistance(navigLastLocation));
            }
        }

        // try to navigate
        switch (navigStage) {
            case REACHING:
                navigStage = navigDirectly();
                break;
            default:
                navigStage = navigAlongPath();
                break;
        }

        // return the stage
        if (log != null && log.isLoggable(Level.FINEST)) {
            log.finest("Navigator.keepNavigating(): navigation stage " + navigStage);
        }
        return navigStage;
    }

    /*========================================================================*/
    /**
     * Initializes direct navigation to given destination.
     *
     * @param dest Destination of the navigation.
     * @return Next stage of the navigation progress.
     */
    private Stage initDirectly(Location dest) {
        // setup navigation info
        navigDestination = dest;
        // init runner
        runner.reset();
        // reset navigation stage
        return navigStage = Stage.REACHING;
    }

    /**
     * Tries to navigate the agent directly to the navig destination.
     *
     * @return Next stage of the navigation progress.
     */
    private Stage navigDirectly() {
        // get the distance from the target
        int distance = (int) memory.getLocation().getDistance(navigDestination);

        // are we there yet?
        if (distance <= CLOSE_ENOUGH) {
            if (log != null && log.isLoggable(Level.FINE)) {
                log.fine("LoqueNavigator.navigDirectly(): destination close enough: " + distance);
            }
            return Stage.COMPLETED;
        }

        // run to that location..
        if (!runner.runToLocation(navigLastLocation, navigDestination, null, (focus == null ? navigDestination : focus), null, true)) {
            if (log != null && log.isLoggable(Level.FINE)) {
                log.fine("LoqueNavigator.navigDirectly(): direct navigation failed");
            }
            return Stage.CRASHED;
        }

        // well, we're still running
        if (log != null && log.isLoggable(Level.FINEST)) {
            log.finer("LoqueNavigator.navigDirectly(): traveling directly, distance = " + distance);
        }
        return navigStage;
    }

    /*========================================================================*/
    /**
     * Iterator through navigation path.
     */
    private Iterator<PATH_ELEMENT> navigIterator = null;

    /**
     * How many path elements we have iterated over before selecting the current
     * {@link LoqueNavigator#navigNextLocation}.
     */
    private int navigNextLocationOffset = 0;

    /**
     * Last location in the path (the one the agent already reached).
     */
    private Location navigLastLocation = null;

    /**
     * If {@link LoqueNavigator#navigLastLocation} is a {@link NavPoint} or has
     * NavPoint near by, its instance is written here (null otherwise).
     */
    private NavPoint navigLastNode = null;

    /**
     * Current node in the path (the one the agent is running to).
     */
    private Location navigCurrentLocation = null;

    /**
     * If {@link LoqueNavigator#navigCurrentLocation} is a {@link NavPoint} or
     * has NavPoint near by, its instance is written here (null otherwise).
     */
    private NavPoint navigCurrentNode = null;

    /**
     * If moving between two NavPoints {@link NavPoint} the object
     * {@link NeighbourLink} holding infomation about the link (if any) will be
     * stored here (null otherwise).
     */
    private NavPointNeighbourLink navigCurrentLink = null;

    /**
     * Next node in the path (the one being prepared).
     */
    private Location navigNextLocation = null;

    /**
     * If {@link LoqueNavigator#navigNextLocation} is a {@link NavPoint} or has
     * NavPoint near by, its instance is written here (null otherwise).
     */
    private NavPoint navigNextNode = null;

    /**
     * Returns {@link NavPoint} instance for a given location. If there is no
     * navpoint in the vicinity of {@link LoqueNavigator#CLOSE_ENOUGH} null is
     * returned.
     *
     * @param location
     * @return
     */
    protected NavPoint getNavPoint(ILocated location) {
        if (location instanceof NavPoint) {
            return (NavPoint) location;
        }
        NavPoint np = DistanceUtils.getNearest(main.getWorldView().getAll(NavPoint.class).values(), location);
        if (np.getLocation().getDistance(location.getLocation()) < CLOSE_ENOUGH) {
            return np;
        }
        return null;
    }

    /**
     * Initializes navigation along path.
     *
     * @param dest Destination of the navigation.
     * @param path Path of the navigation.
     * @return True, if the navigation is successfuly initialized.
     */
    private boolean initAlongPath(Location dest, List<PATH_ELEMENT> path) {
        // setup navigation info
        navigDestination = dest;
        navigIterator = path.iterator();
        // reset current node
        navigCurrentLocation = bot.getLocation();
        navigCurrentNode = DistanceUtils.getNearest(bot.getWorldView().getAll(NavPoint.class).values(), bot.getLocation(), 40);
        // prepare next node
        prepareNextNode();
        // reset navigation stage
        navigStage = Stage.NAVIGATING;
        // reset node navigation info
        return switchToNextNode();
    }

    /**
     * Tries to navigate the agent safely along the navigation path.
     *
     * @return Next stage of the navigation progress.
     */
    private Stage navigAlongPath() {
        // get the distance from the destination
        int totalDistance = (int) memory.getLocation().getDistance(navigDestination);

        // are we there yet?
        if (totalDistance <= CLOSE_ENOUGH) {
            log.log(Level.FINEST, "Navigator.navigAlongPath(): destination close enough: {0}", totalDistance);
            return Stage.COMPLETED;
        }

        // navigate
        if (navigStage.mover) {
            log.fine("MOVER");
            return navigThroughMover();
        } else if (navigStage.teleport) {
            log.fine("TELEPORT");
            return navigThroughTeleport();
        } else {
            log.fine("STANDARD");
            return navigToCurrentNode(true); // USE FOCUS, normal navigation
        }
    }

    /*========================================================================*/
    /**
     * Prepares next navigation node in path.
     * <p>
     * <p>
     * If necessary just recalls
     * {@link LoqueNavigator#prepareNextNodeTeleporter()}.
     */
    private void prepareNextNode() {
        if (navigCurrentNode != null && navigCurrentNode.isTeleporter()) {
            // current node is a teleporter! ...
            prepareNextNodeTeleporter();
            return;
        }

        // retreive the next node, if there are any left
        // note: there might be null nodes along the path!
        ILocated located = null;
        navigNextLocation = null;
        navigNextNode = null;
        navigNextLocationOffset = 0;
        while ((located == null) && navigIterator.hasNext()) {
            // get next node in the path
            located = navigIterator.next();
            navigNextLocationOffset += 1;
            if (located == null) {
                continue;
            }
        }

        // did we get the next node?
        if (located == null) {
            navigNextLocationOffset = 0;
            return;
        }

        if (executor.getPathElementIndex() + navigNextLocationOffset >= executor.getPath().size()) {
            navigNextLocationOffset = 0; // WTF?
        }

        // obtain next location
        navigNextLocation = located.getLocation();
        // obtain navpoint instance for a given location
        navigNextNode = getNavPoint(located);
    }

    /**
     * Prepares next node in the path assuming the currently pursued node is a
     * teleporter.
     */
    private void prepareNextNodeTeleporter() {
        // Retrieve the next node, if there are any left
        // note: there might be null nodes along the path!
        ILocated located = null;
        navigNextLocation = null;
        navigNextLocationOffset = 0;
        boolean nextTeleporterFound = false;
        while ((located == null) && navigIterator.hasNext()) {
            // get next node in the path
            located = navigIterator.next();
            navigNextLocationOffset += 1;
            if (located == null) {
                continue;
            }
            navigNextNode = getNavPoint(located);
            if (navigNextNode != null && navigNextNode.isTeleporter()) {
                // next node is 
                if (!nextTeleporterFound) {
                    // ignore first teleporter as it is the other end of the teleporter we're currently trying to enter
                    located = null;
                }
                nextTeleporterFound = true;
            } else {
                break;
            }
        }

        // did we get the next node?
        if (located == null) {
            navigNextLocationOffset = 0;
            return;
        }

        if (executor.getPathElementIndex() + navigNextLocationOffset >= executor.getPath().size()) {
            navigNextLocationOffset = 0; // WTF?
        }

        // obtain next location
        navigNextLocation = located.getLocation();
        // obtain navpoint instance for a given location
        navigNextNode = getNavPoint(located);
    }

    /**
     * Initializes next navigation node in path.
     *
     * @return True, if the navigation node is successfully switched.
     */
    private boolean switchToNextNode() {
        if (log != null && log.isLoggable(Level.FINER)) {
            log.finer("Navigator.switchToNextNode(): switching!");
        }

        // move the current node into last node
        navigLastLocation = navigCurrentLocation;
        navigLastNode = navigCurrentNode;

        // get the next prepared node
        if (null == (navigCurrentLocation = navigNextLocation)) {
            // no nodes left there..
            if (log != null && log.isLoggable(Level.FINER)) {
                log.finer("Navigator.switchToNextNode(): no nodes left");
            }
            navigCurrentNode = null;
            return false;
        }
        // rewrite the navpoint as well
        navigCurrentNode = navigNextNode;

        // store current NavPoint link
        navigCurrentLink = getNavPointsLink(navigLastNode, navigCurrentNode);

        if (navigCurrentLink == null) {
            getNavPointsLink(navigLastNode, navigCurrentNode);
            if (log.isLoggable(Level.INFO)) {
                log.info("No link information...");
            }
        }

        // ensure that the last node is not null
        if (navigLastLocation == null) {
            navigLastLocation = bot.getLocation();
            navigLastNode = navigCurrentNode;
        }

        // get next node distance
        int localDistance = (int) memory.getLocation().getDistance(navigCurrentLocation.getLocation());

        // is this next node a teleporter?
        if (navigCurrentNode != null && navigCurrentNode.isTeleporter()) {
            navigStage = Stage.TeleporterStage();
        } // is this next node a mover?
        else if (navigCurrentNode != null && navigCurrentNode.isLiftCenter()) {
            // setup mover sequence
            navigStage = Stage.FirstMoverStage();
            resetNavigMoverVariables();

            // AREN'T WE ALREADY ON THE LIFT CENTER?
            if (memory.getLocation().getDistance(navigCurrentNode.getLocation()) < CLOSE_ENOUGH) {
                // YES WE ARE!
                navigStage = navigStage.next();
            }
        } // are we still moving on mover? 
        else if (navigStage.mover) {
            navigStage = navigStage.next();
            // init the runner
            runner.reset();
        } else if (navigStage.teleport) {
            navigStage = navigStage.next();
            // init the runner
            runner.reset();
        } // no movers & teleports
        else {
            // init the runner
            runner.reset();
        }

        // switch to next node
        if (log != null && log.isLoggable(Level.FINE)) {
            if (navigCurrentNode != null) {
                log.fine(
                        "LoqueNavigator.switchToNextNode(): switch to next node " + navigCurrentNode.getId().getStringId()
                        + ", distance " + localDistance
                        + ", reachable " + isReachable(navigCurrentNode)
                        + ", mover " + navigStage.mover
                );
                // we do not have extra information about the location we're going to reach
            } else {
                log.log(
                        Level.FINE, "LoqueNavigator.switchToNextNode(): switch to next location {0}, distance {1}, mover {2}", new Object[]{navigCurrentLocation, localDistance, navigStage.mover});
            }

        }

        // tell the executor that we have moved in the path to the next element
        if (executor.getPathElementIndex() < 0) {
            executor.switchToAnotherPathElement(0);
        } else {
            if (navigNextLocationOffset > 0) {
                executor.switchToAnotherPathElement(executor.getPathElementIndex() + navigNextLocationOffset);
            } else {
                executor.switchToAnotherPathElement(executor.getPathElementIndex());
            }
        }
        navigNextLocationOffset = 0;

        prepareNextNode();

        if (localDistance < 20) {
            return switchToNextNode();
        }

        return true;
    }

    protected boolean isReachable(NavPoint node) {
        return true;
//        if (node == null) {
//            return true;
//        }
//        int hDistance = (int) memory.getLocation().getDistance2D(node.getLocation());
//        int vDistance = (int) node.getLocation().getDistanceZ(memory.getLocation());
//        double angle;
//        if (hDistance == 0) {
//            angle = vDistance == 0 ? 0 : (vDistance > 0 ? Math.PI / 2 : -Math.PI / 2);
//        } else {
//            angle = Math.atan(vDistance / hDistance);
//        }
//        return Math.abs(vDistance) < 30 && Math.abs(angle) < Math.PI / 4;
    }

    //
    // NAVIG MOVER VARIABLES
    //
    private int navigMoverRideUpCount;

    private int navigMoverRideDownCount;

    private Boolean navigMoverIsRidingUp;

    private Boolean navigMoverIsRidingDown;

    private void resetNavigMoverVariables() {
        navigMoverIsRidingUp = null;
        navigMoverIsRidingDown = null;
        navigMoverRideUpCount = 0;
        navigMoverRideDownCount = 0;
    }

    private void checkMoverMovement(Mover mover) {
        // ASSUMING THAT MOVER IS ALWAYS ... riding fully UP, riding fully DOWN (or vice versa) and passing all possible exits
        if (mover.getVelocity().z > 0) {
            // mover is riding UP
            if (navigMoverIsRidingUp == null) {
                navigMoverIsRidingUp = true;
                navigMoverIsRidingDown = false;
                navigMoverRideUpCount = 1;
                navigMoverRideDownCount = 0;
            } else if (navigMoverIsRidingDown) {
                navigMoverIsRidingUp = true;
                navigMoverIsRidingDown = false;
                ++navigMoverRideUpCount;
            }
        } else if (mover.getVelocity().z < 0) {
            // mover is riding DOWN
            if (navigMoverIsRidingDown == null) {
                navigMoverIsRidingUp = false;
                navigMoverIsRidingDown = true;
                navigMoverRideUpCount = 0;
                navigMoverRideDownCount = 1;
            } else if (navigMoverIsRidingUp) {
                navigMoverIsRidingUp = false;
                navigMoverIsRidingDown = true;
                ++navigMoverRideDownCount;
            }
        }
    }

    /*========================================================================*/
    /**
     * Gets the link with movement information between two navigation points.
     * Holds information about how we can traverse from the start to the end
     * navigation point.
     *
     * @return NavPointNeighbourLink or null
     */
    private NavPointNeighbourLink getNavPointsLink(NavPoint start, NavPoint end) {
        if (start == null) {
            //if start NavPoint is not provided, we try to find some
            NavPoint tmp = getNavPoint(memory.getLocation());
            if (tmp != null) {
                start = tmp;
            } else {
                return null;
            }
        }
        if (end == null) {
            return null;
        }

        if (end.getIncomingEdges().containsKey(start.getId())) {
            return end.getIncomingEdges().get(start.getId());
        }

        return null;
    }

    /*========================================================================*/
    /**
     * Tries to navigate the agent safely to the current navigation node.
     *
     * @return Next stage of the navigation progress.
     */
    private Stage navigToCurrentNode(boolean useFocus) {
        if (navigCurrentNode != null) {
            // update location of the current place we're reaching ... it might be Mover after all
            navigCurrentLocation = navigCurrentNode.getLocation();
        }
        if (navigNextNode != null) {
            // update location of the next place we're reaching ... it might be Mover after all
            navigNextLocation = navigNextNode.getLocation();
        }

        // get the distance from the current node
        int localDistance = (int) memory.getLocation().getDistance(navigCurrentLocation.getLocation());
        // get the distance from the current node (neglecting jumps)
        int localDistance2D = (int) memory.getLocation().getDistance2D(navigCurrentLocation.getLocation());

        int distanceZ = (int) memory.getLocation().getDistanceZ(navigCurrentLocation);

        // where are we going to run to
        Location firstLocation = navigCurrentLocation.getLocation();
        // where we're going to continue
        Location secondLocation = (navigNextNode != null
                ? (navigNextNode.isLiftCenter() || navigNextNode.isLiftExit()
                ? null // let navigThroughMover() to handle these cases with care!
                : navigNextNode.getLocation())
                : navigNextLocation);
        // and what are we going to look at
        ILocated focus = (this.focus == null || !useFocus
                ? ((navigNextLocation == null) ? firstLocation : navigNextLocation.getLocation())
                : this.focus);

        // run to the current node..
        if (!runner.runToLocation(navigLastLocation, firstLocation, secondLocation, focus, navigCurrentLink, (navigCurrentNode == null ? true : isReachable(navigCurrentNode)))) {
            if (log != null && log.isLoggable(Level.FINE)) {
                log.fine("LoqueNavigator.navigToCurrentNode(): navigation to current node failed");
            }
            return Stage.CRASHED;
        }

        // we're still running
        if (log != null && log.isLoggable(Level.FINEST)) {
            log.finest("LoqueNavigator.navigToCurrentNode(): traveling to current node, distance = " + localDistance);
        }

        //CHANGED: Distance when to switch to next node -> for our movement on the edges with the navMesh, it needs to be pretty accurate...
        //ORIGINAL VALUE: 200
        int testDistance = 40; // default constant suitable for default running 
        if (navigCurrentNode != null && (navigCurrentNode.isLiftCenter() || navigCurrentNode.isLiftExit())) {
            // if we should get to lift exit or the lift center, we must use more accurate constants
            //CHANGED: Original - 150
            testDistance = 30;
        }
        if (navigCurrentLink != null && (navigCurrentLink.getFlags() & LinkFlag.JUMP.get()) != 0) {
            // we need to jump to reach the destination ... do not switch based on localDistance2
            localDistance2D = 10000;
        }
        
        if(navigStage.teleport) {
            testDistance = 10;
        }

        if (navigCurrentLocation != null && navigCurrentLocation.equals(executor.getPath().get(executor.getPath().size() - 1))
                || (!navigIterator.hasNext() && (navigNextLocation == null || navigCurrentLocation == navigNextLocation))) {
            // if we're going to the LAST POINT ... be sure to get to the exact location in order to ensure pick up of the item
            testDistance = 2 * ((int) UnrealUtils.CHARACTER_COLLISION_RADIUS);
        }

        // are we close enough to switch to the next node? (mind the distanceZ particularly!)
        if (distanceZ < 40 && ((localDistance < testDistance) || (localDistance2D < testDistance))) {
            // switch navigation to the next node
            if (!switchToNextNode()) {
                // switch to the direct navigation
                if (log != null && log.isLoggable(Level.FINE)) {
                    log.fine("Navigator.navigToCurrentNode(): switching to direct navigation");
                }
                return initDirectly(navigDestination);
            } else {
                //Send new MOVE command in current iteration.
                return keepNavigating();
            }
        }

        Location botCurrentLocation = memory.getLocation();
        if (navigCurrentLocation != null && navigLastLocation != null && navigNextLocation != null && botCurrentLocation != null && !navigStage.teleport) {

            //Test case for passing navig node without switching target
            Location navigDirection = navigCurrentLocation.sub(navigLastLocation);
            if (navigDirection.getLength() > botCurrentLocation.sub(navigLastLocation).getLength()) {
                //We didn't reach the node yet...
                //Do nothing
            } else {

                Location altDirection = navigNextLocation.sub(botCurrentLocation).getNormalized();
                double angle = Math.acos(navigDirection.getNormalized().dot(altDirection));

                if (angle < Math.PI / 2) {
                    if (botCurrentLocation.getDistance(navigNextLocation) < navigCurrentLocation.getDistance(navigNextLocation)) {
                        log.fine("We are closer to next target than current, switching to next node");
                        // switch navigation to the next node
                        if (!switchToNextNode()) {
                            // switch to the direct navigation
                            if (log != null && log.isLoggable(Level.FINE)) {
                                log.fine("Navigator.navigToCurrentNode(): switching to direct navigation");
                            }
                            return initDirectly(navigDestination);
                        } else {
                            //Send new MOVE command in current iteration.
                            return keepNavigating();
                        }
                    }
                }
            }
        }

        // well, we're still running
        return navigStage;
    }

    /*========================================================================*/
    /**
     * Tries to navigate the agent safely along mover navigation nodes.
     *
     * <h4>Pogamut troubles</h4>
     *
     * Since the engine does not send enough reasonable info about movers and
     * their frames, the agent relies completely and only on the associated
     * navigation points. Fortunatelly, LiftCenter navigation points move with
     * movers.
     *
     * <p>
     * Well, do not get too excited. Pogamut seems to update the position of
     * LiftCenter navpoint from time to time, but it's not frequent enough for
     * correct and precise reactions while leaving lifts.</p>
     *
     * @return Next stage of the navigation progress.
     */
    private Stage navigThroughMover() {
        Stage stage = navigStage;

        if (navigCurrentNode == null) {
            if (log != null && log.isLoggable(Level.WARNING)) {
                log.warning("LoqueNavigator.navigThroughMover(" + stage + "): can't navigate through the mover without the navpoint instance (navigCurrentNode == null)");
            }
            return Stage.CRASHED;
        }

        Mover mover = (Mover) bot.getWorldView().get(navigCurrentNode.getMover());
        if (mover == null) {
            if (log != null && log.isLoggable(Level.WARNING)) {
                log.warning("LoqueNavigator.navigThroughMover(" + stage + "): can't navigate through the mover as current node does not represent a mover (moverId == null): " + navigCurrentNode);
            }
            return Stage.CRASHED;
        }

        // update navigCurrentLocation as the mover might have moved
        navigCurrentLocation = navigCurrentNode.getLocation();

        if (navigNextNode != null) {
            // update navigNextLocation as the mover might have moved
            navigNextLocation = navigNextNode.getLocation();
        }

        log.info("navig-curr: " + navigCurrentNode);
        log.info("navig-next: " + navigNextNode);

        // get horizontal distance from the mover center node ... always POSITIVE
        int hDistance = (int) memory.getLocation().getDistance2D(navigCurrentLocation.getLocation());
        // get vertical distance from the mover center node ... +/- ... negative -> mover is below us, positive -> mover is above us
        int zDistance = (int) navigCurrentLocation.getLocation().getDistanceZ(memory.getLocation());
        // whether mover is riding UP
        boolean moverRidingUp = mover.getVelocity().z > 0;
        // whether mover is riding DOWN
        boolean moverRidingDown = mover.getVelocity().z < 0;
        // whether mover is standing still
        boolean moverStandingStill = Math.abs(mover.getVelocity().z) < Location.DISTANCE_ZERO;

        if (navigStage == Stage.AWAITING_MOVER) {
            // Aren't we under the mover?
            if (zDistance > 0 && moverRidingUp) {
                // we're under the mover and the mover is riding up...
                if (log != null && log.isLoggable(Level.FINER)) {
                    log.finer(
                            "LoqueNavigator.navigThroughMover(" + stage + "): we are UNDER the mover and mover is RIDING UP ... getting back to waiting position"
                            + ", zDistance " + zDistance + ", mover.velocity.z " + mover.getVelocity().z + ", mover " + (moverRidingUp ? "riding UP" : (moverRidingDown ? "riding DOWN" : moverStandingStill ? "standing STILL" : " movement unknown"))
                    );
                }
                // run to the last node, the one we need to be waiting on for the mover to come
                // WE MUST NOT USE FOCUS! Because we need to see the mover TODO: provide turning behavior, i.e., if focus is set, once in a time turn to then direction
                if (!runner.runToLocation(memory.getLocation(), navigLastLocation, null, navigCurrentLocation, null, (navigLastNode == null ? true : isReachable(navigLastNode)))) {
                    if (log != null && log.isLoggable(Level.FINE)) {
                        log.fine("LoqueNavigator.navigThroughMover(" + stage + "): navigation to wait-for-mover node failed");
                    }
                    return Stage.CRASHED;
                }
                return navigStage;
            }

            // wait for the current node to come close in both, vert and horiz
            // the horizontal distance can be quite long.. the agent will hop on
            // TODO: There may be problem when LiftExit is more than 400 ut units far from LiftCenter!
//            if (hDistance > 400) {
//                if (log != null && log.isLoggable(Level.WARNING)) {
//                    log.warning("LoqueNavigator.navigThroughMover(" + stage + "): failed to get onto the mover as its 2D distance is > 400, hDistance " + hDistance + ", unsupported!");
//                }
//                return Stage.CRASHED;
//            }
            if (zDistance > 30 && moverRidingUp) // mover is riding UP and is already above us, we won't make it...
            {
                // run to the last node, the one we need to be waiting on for the mover to come
                if (log != null && log.isLoggable(Level.FINER)) {
                    log.finer(
                            "LoqueNavigator.navigThroughMover(" + stage + "): waiting for the mover to come"
                            + " | zDistance " + zDistance + ", hDistance " + hDistance + ", mover " + (moverRidingUp ? "riding UP" : (moverRidingDown ? "riding DOWN" : moverStandingStill ? "standing STILL" : " movement unknown"))
                            + ", node " + navigCurrentNode.getId().getStringId()
                    );
                }
                // WE MUST NOT USE FOCUS! Because we need to see the mover TODO: provide turning behavior, i.e., if focus is set, once in a time turn to then direction
                if (!runner.runToLocation(memory.getLocation(), navigLastLocation, null, navigCurrentLocation, navigCurrentLink, (navigLastNode == null ? true : isReachable(navigLastNode)))) {
                    if (log != null && log.isLoggable(Level.FINE)) {
                        log.fine("LoqueNavigator.navigThroughMover(" + stage + "): navigation to last node failed");
                    }
                    return Stage.CRASHED;
                }

                return navigStage;
            }

            // MOVER HAS ARRIVED (at least that what we're thinking so...)
            if (log != null && log.isLoggable(Level.FINER)) {
                log.finer(
                        "Navigator.navigThroughMover(" + stage + "): mover arrived"
                        + " | zDistance " + zDistance + ", hDistance " + hDistance + ", mover " + (moverRidingUp ? "riding UP" : (moverRidingDown ? "riding DOWN" : moverStandingStill ? "standing STILL" : " movement unknown"))
                        + ", node " + navigCurrentNode.getId().getStringId()
                );
            }

            // LET'S MOVE TO THE LIFT CENTER (do not use focus)
            return navigToCurrentNode(false);
        } else if (navigStage == Stage.RIDING_MOVER) {
            checkMoverMovement(mover);

            if (navigMoverRideDownCount > 2 || navigMoverRideUpCount > 2) {
                // we're riding up & down without any effect ... failure :(
                if (log != null && log.isLoggable(Level.FINE)) {
                    log.fine("LoqueNavigator.navigThroughMover(" + stage + "): navigation to mover exit node failed, we've rided twice up & down and there was no place suitable to exit the mover in order to get to get to " + navigCurrentNode);
                }
                return Stage.CRASHED;
            }

            if (hDistance > 400) {
                if (log != null && log.isLoggable(Level.WARNING)) {
                    log.warning("LoqueNavigator.navigThroughMover(" + stage + "): navigation to mover exit node failed, the node is too far, hDistance " + hDistance + " > 400, unsupported (wiered navigation graph link)");
                }
                return Stage.CRASHED;
            }

            // wait for the mover to ride us up/down
            if (Math.abs(zDistance) > 50) {
                // run to the last node, the one we're waiting on
                if (log != null && log.isLoggable(Level.FINER)) {
                    log.finer(
                            "LoqueNavigator.navigThroughMover(" + stage + "): riding the mover"
                            + " | zDistance " + zDistance + ", hDistance " + hDistance + ", mover " + (moverRidingUp ? "riding UP" : (moverRidingDown ? "riding DOWN" : moverStandingStill ? "standing STILL" : " movement unknown"))
                            + ", node " + navigCurrentNode.getId().getStringId()
                    );
                }

                // WE MUST NOT USE FOCUS! We have to see the mover. TODO: provide turning behavior, i.e., turn to desired focus once in a time
                if (!runner.runToLocation(memory.getLocation(), navigLastLocation, null, navigCurrentLocation, navigCurrentLink, (navigLastNode == null ? true : isReachable(navigLastNode)))) {
                    if (log != null && log.isLoggable(Level.FINE)) {
                        log.fine("LoqueNavigator.navigThroughMover(" + stage + "): navigation to last node failed");
                    }
                    return Stage.CRASHED;
                }
                // and keep waiting for the mover to go to the correct position

                return navigStage;
            }

            // MOVER HAS ARRIVED TO POSITION FOR EXIT (at least that what we're thinking so...)
            if (log != null && log.isLoggable(Level.FINER)) {
                log.finer(
                        "Navigator.navigThroughMover(" + stage + "): exiting the mover"
                        + " | zDistance " + zDistance + ", hDistance " + hDistance + ", mover " + (moverRidingUp ? "riding UP" : (moverRidingDown ? "riding DOWN" : moverStandingStill ? "standing STILL" : " movement unknown"))
                        + ", node " + navigCurrentNode.getId().getStringId()
                );
            }

            // LET'S MOVE TO THE LIFT EXIT (do not use focus)
            return navigToCurrentNode(false);
        } else {
            if (log != null && log.isLoggable(Level.WARNING)) {
                log.warning("Navigator.navigThroughMover(" + stage + "): invalid stage, neither AWAITING_MOVER nor RIDING MOVER");
            }
            return Stage.CRASHED;
        }

    }

    /*========================================================================*/
    /**
     * Tries to navigate the agent safely to the current navigation node.
     *
     * @return Next stage of the navigation progress.
     */
    private Stage navigThroughTeleport() {
        if (navigCurrentNode != null) {
            // update location of the current place we're reaching
            navigCurrentLocation = navigCurrentNode.getLocation();
        }

        if (navigNextNode != null) {
            // update location of the Next place we're reaching
            navigNextLocation = navigNextNode.getLocation();
        }

        // now we have to compute whether we should switch to another navpoint
        // it has two flavours, we should switch if:
        //			1. we're too near to teleport, we should run into
        //          2. we're at the other end of the teleport, i.e., we've already got through the teleport
        // 1. DISTANCE TO THE TELEPORT
        // get the distance from the current node
        int localDistance1_1 = (int) memory.getLocation().getDistance(navigCurrentLocation.getLocation());
        // get the distance from the current node (neglecting jumps)
        int localDistance1_2 = (int) memory.getLocation().getDistance(
                Location.add(navigCurrentLocation.getLocation(), new Location(0, 0, 100))
        );

        // 2. DISTANCE TO THE OTHER END OF THE TELEPORT
        // ---[[ WARNING ]]--- we're assuming that there is only ONE end of the teleport
        int localDistance2_1 = Integer.MAX_VALUE;
        int localDistance2_2 = Integer.MAX_VALUE;
        for (NavPointNeighbourLink link : navigCurrentNode.getOutgoingEdges().values()) {
            if (link.getToNavPoint().isTeleporter()) {
                localDistance2_1 = (int) memory.getLocation().getDistance(link.getToNavPoint().getLocation());
                localDistance2_2 = (int) memory.getLocation().getDistance(
                        Location.add(link.getToNavPoint().getLocation(), new Location(0, 0, 100))
                );
                break;
            }
        }

        boolean switchedToNextNode = false;
        // are we close enough to switch to the OTHER END of the teleporter?
        if ((localDistance2_1 < 200) || (localDistance2_2 < 200)) {
            // yes we are! we already passed the teleporter, so DO NOT APPEAR DUMB and DO NOT TRY TO RUN BACK 
            // ... better to switch navigation to the next node
            if (!switchToNextNode()) {
                // switch to the direct navigation
                if (log != null && log.isLoggable(Level.FINE)) {
                    log.fine("Navigator.navigToCurrentNode(): switch to direct navigation");
                }
                return initDirectly(navigDestination);
            }
            switchedToNextNode = true;
        }

        // where are we going to run to
        Location firstLocation = navigCurrentLocation.getLocation();
        // where we're going to continue
        Location secondLocation = (navigNextNode != null && !navigNextNode.isLiftCenter() && !navigNextNode.isLiftCenter()
                ? navigNextNode.getLocation()
                : navigNextLocation);
        // and what are we going to look at
        ILocated focus = (this.focus == null
                ? ((navigNextLocation == null) ? firstLocation : navigNextLocation.getLocation())
                : this.focus);

        // run to the current node..
        if (!runner.runToLocation(navigLastLocation, firstLocation, secondLocation, focus, navigCurrentLink, (navigCurrentNode == null ? true : isReachable(navigCurrentNode)))) {
            if (log != null && log.isLoggable(Level.FINE)) {
                log.fine("LoqueNavigator.navigToCurrentNode(): navigation to current node failed");
            }
            return Stage.CRASHED;
        }

        // we're still running
        if (log != null && log.isLoggable(Level.FINEST)) {
            log.finest("LoqueNavigator.navigToCurrentNode(): traveling to current node");
        }

        // now as we've tested the first node ... test the second one
        if (!switchedToNextNode && ((localDistance1_1 < 20) || (localDistance1_2 < 20))) {
            // switch navigation to the next node
            if (!switchToNextNode()) {
                // switch to the direct navigation
                if (log != null && log.isLoggable(Level.FINE)) {
                    log.fine("Navigator.navigToCurrentNode(): switch to direct navigation");
                }
                return initDirectly(navigDestination);
            }
        }

        // well, we're still running
        return navigStage;
    }

    /*========================================================================*/
    /**
     * Enum of types of terminating navigation stages.
     */
    private enum TerminatingStageType {

        /**
         * Terminating with success.
         */
        SUCCESS(false),
        /**
         * Terminating with failure.
         */
        FAILURE(true);

        /**
         * Whether the terminating with failure.
         */
        public boolean failure;

        /**
         * Constructor.
         *
         * @param failure Whether the terminating with failure.
         */
        private TerminatingStageType(boolean failure) {
            this.failure = failure;
        }
    };

    /**
     * Enum of types of mover navigation stages.
     */
    private enum MoverStageType {

        /**
         * Waiting for mover.
         */
        WAITING,
        /**
         * Riding mover.
         */
        RIDING;
    };

    /**
     * Enum of types of mover navigation stages.
     */
    private enum TeleportStageType {

        /**
         * Next navpoint is a teleport
         */
        GOING_THROUGH;
    };

    /**
     * All stages the navigation can come to.
     */
    public enum Stage {

        /**
         * Running directly to the destination.
         */
        REACHING() {
                    protected Stage next() {
                        return this;
                    }
                },
        /**
         * Navigating along the path.
         */
        NAVIGATING() {
                    protected Stage next() {
                        return this;
                    }
                },
        /**
         * Waiting for a mover to arrive.
         */
        AWAITING_MOVER(MoverStageType.WAITING) {
                    protected Stage next() {
                        return RIDING_MOVER;
                    }
                },
        /**
         * Waiting for a mover to ferry.
         */
        RIDING_MOVER(MoverStageType.RIDING) {
                    protected Stage next() {
                        return NAVIGATING;
                    }
                },
        /**
         * Navigation cancelled by outer force.
         */
        CANCELED(TerminatingStageType.FAILURE) {
                    protected Stage next() {
                        return this;
                    }
                },
        /**
         * Navigation timeout reached.
         */
        TIMEOUT(TerminatingStageType.FAILURE) {
                    protected Stage next() {
                        return this;
                    }
                },
        /**
         * Navigation failed because of troublesome obstacles.
         */
        CRASHED(TerminatingStageType.FAILURE) {
                    protected Stage next() {
                        return this;
                    }
                },
        /**
         * Navigation finished successfully.
         */
        COMPLETED(TerminatingStageType.SUCCESS) {
                    protected Stage next() {
                        return this;
                    }
                },
        /**
         * We're going through the teleport.
         */
        TELEPORT(TeleportStageType.GOING_THROUGH) {
                    protected Stage next() {
                        return NAVIGATING;
                    }
                ;
        };
        

        /*====================================================================*/

        /**
         * Running through the mover.
         */
        private boolean mover;
        /**
         * Whether the nagivation is terminated.
         */
        public boolean terminated;
        /**
         * Whether the navigation has failed.
         */
        public boolean failure;
        /**
         * We're going through the teleport.
         */
        public boolean teleport;

        /*====================================================================*/
        /**
         * Constructor: Not finished, not failed
         */
        private Stage() {
            this.mover = false;
            this.teleport = false;
            this.terminated = false;
            this.failure = false;
        }

        private Stage(TeleportStageType type) {
            this.mover = false;
            this.teleport = true;
            this.failure = false;
            this.terminated = false;
        }

        /**
         * Constructor: mover.
         *
         * @param type Type of mover navigation stage.
         */
        private Stage(MoverStageType type) {
            this.mover = true;
            this.teleport = false;
            this.terminated = false;
            this.failure = false;
        }

        /**
         * Constructor: terminating.
         *
         * @param type Type of terminating navigation stage.
         */
        private Stage(TerminatingStageType type) {
            this.mover = false;
            this.teleport = false;
            this.terminated = true;
            this.failure = type.failure;
        }

        /*====================================================================*/
        /**
         * Retreives the next step of navigation sequence the stage belongs to.
         *
         * @return The next step of navigation sequence. Note: Some stages are
         * not part of any logical navigation sequence. In such cases, this
         * method simply returns the same stage.
         */
        protected abstract Stage next();

        /*====================================================================*/
        /**
         * Returns the first step of mover sequence.
         *
         * @return The first step of mover sequence.
         */
        protected static Stage FirstMoverStage() {
            return AWAITING_MOVER;
        }

        /**
         * Returns the first step of the teleporter sequence.
         *
         * @return
         */
        protected static Stage TeleporterStage() {
            return Stage.TELEPORT;
        }
    }

    /*========================================================================*/
    /**
     * Default: Loque Runner.
     */
    private IUT2004PathRunner runner;

    /*========================================================================*/
    /**
     * Agent's main.
     */
    protected UT2004Bot main;
    /**
     * Loque memory.
     */
    protected AgentInfo memory;
    /**
     * Agent's body.
     */
    protected AdvancedLocomotion body;
    /**
     * Agent's log.
     */
    protected Logger log;

//    /*========================================================================*/
//    /**
//     * Constructor.
//     *
//     * @param main Agent's main.
//     * @param memory Loque memory.
//     */
//    public NavMeshNavigator(UT2004Bot bot, AgentInfo info, AdvancedLocomotion move, Logger log) {
//        this(bot, info, move, new NavMeshRunner(bot, info, move, log), log);
//    }
    /**
     * Constructor.
     *
     * @param main Agent's main.
     * @param memory Loque memory.
     */
    public NavMeshNavigator(UT2004Bot bot, AgentInfo info, AdvancedLocomotion move, IUT2004PathRunner runner, Logger log) {
        // setup reference to agent
        this.main = bot;
        this.memory = info;
        this.body = move;
        this.log = log;

        this.selfListener = new SelfListener(bot.getWorldView());

        // save runner object
        this.runner = runner;
        NullCheck.check(this.runner, "runner");
    }

    public Logger getLog() {
        return log;
    }

}
