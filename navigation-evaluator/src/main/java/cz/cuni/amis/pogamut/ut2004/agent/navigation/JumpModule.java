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

import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.navmesh.NavMesh;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.navmesh.NavMeshConstants;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.navmesh.NavMeshPolygon;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPointNeighbourLink;
import cz.cuni.amis.pogamut.ut2004.utils.LinkFlag;
import cz.cuni.amis.pogamut.ut2004.utils.UnrealUtils;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.vecmath.Vector2d;
import math.geom2d.Point2D;
import math.geom2d.Vector2D;
import math.geom2d.line.Line2D;

/**
 *
 * @author Bogo
 */
public class JumpModule {

    //
    // JUMP EQUATION - SINGLE JUMP
    // z = -475 * t^2 + power * t
    //
    // t = Unreal time
    //
    //
    // JUMP EQUATION - DOUBLE JUMP
    // z = -475 * t^2 + power * t + koef * (t - delay)
    //
    // koef = (power - 340) * 1.066 + 2 
    //
    // t = Unreal time
    //
    public static final double MAX_DOUBLE_JUMP_POWER = 755;
    public static final double MAX_SINGLE_JUMP_POWER = 340;

    private NavMesh navMesh;

    private Logger log;

    public static final double MAX_JUMP_HEIGHT = 130;
    private static final double MAX_SINGLE_JUMP_HEIGHT = 60;
    private static final double NAVMESH_Z_COORD_CORRECTION = 20;
    private static final double SPEED_BOOST_DELAY = 0.100;
    private static final double BOT_RADIUS = 70;
    private static final double JUMP_PEEK_TIME = 0.39;

    public JumpModule(NavMesh mesh, Logger log) {
        this.navMesh = mesh;
        this.log = log;
    }

    /**
     * Computes jump boundaries for given link, with using maximal available
     * information about environment. Stores the boundaries in the "Jump" object
     * for later use.
     *
     * @param jumpLink Link on which the jump should occur.
     * @return True if jump boundaries were successfully computed, false
     * otherwise.
     */
    public JumpBoundaries computeJumpBoundaries(NavPointNeighbourLink jumpLink) {
        if (jumpLink == null) {
            return new JumpBoundaries(jumpLink);
        }

        NavPoint startNavPoint = jumpLink.getFromNavPoint();
        NavPoint endNavPoint = jumpLink.getToNavPoint();
        Location startLocation = startNavPoint.getLocation();
        Location endLocation = endNavPoint.getLocation();

        //Get edge of the mesh + offset
        Location linkDirection3d = endLocation.sub(startLocation);
        Vector2d linkDirection = new Vector2d(linkDirection3d.x, linkDirection3d.y);

        //double startDistanceFromEdge = navMesh.getDistanceFromEdge(startLocation, linkDirection);
        BorderPoint startBorder = getBorderPoint(startLocation, endLocation);
        Location startBorderPoint = startBorder.getPoint();

        //Get landing edge of the mesh + offset
        Vector2d negatedLinkDirection = new Vector2d(linkDirection);
        negatedLinkDirection.negate();

        //double endDistanceFromEdge = navMesh.getDistanceFromEdge(endLocation, negatedLinkDirection);
        BorderPoint endBorderPoint = getBorderPoint(endLocation, startLocation);
        if (!endBorderPoint.getPoint().equals(endNavPoint.getLocation(), 1.0)) {
            //NavMesh Z coordinate correction
            endBorderPoint.setPoint(endBorderPoint.getPoint().addZ(NAVMESH_Z_COORD_CORRECTION));
        }

        //Check capability to jump between borders
        boolean borderToBorder = isJumpable(startBorderPoint, endBorderPoint.getPoint(), UnrealUtils.MAX_VELOCITY);
        if (!borderToBorder) {
            //We can't jump between the nearest possible points. Uh-oh.
            return new JumpBoundaries(jumpLink);
        }

        //Find take-off booundary
        Location testBoundary = startLocation;
        Location currentBoundary = startBorderPoint;
        boolean boundaryFound = false;
        double distanceToSearch = 0;
        do {
            boolean isJumpable = isJumpable(testBoundary, endBorderPoint.getPoint(), UnrealUtils.MAX_VELOCITY);
            if (isJumpable && distanceToSearch < BOUNDARY_THRESHOLD) {
                currentBoundary = testBoundary;
                boundaryFound = true;
            } else if (isJumpable) {
                //Move the test location far from the border
                currentBoundary = testBoundary;
                distanceToSearch /= 2;
                testBoundary = getNavMeshPoint(testBoundary, startLocation, distanceToSearch);

            } else if (distanceToSearch < BOUNDARY_THRESHOLD && distanceToSearch > 0) {
                //Take the last successfull point
                boundaryFound = true;
            } else {
                //Move the test location nearer to the border
                if (distanceToSearch == 0) {
                    distanceToSearch = testBoundary.getDistance2D(startBorderPoint);
                }
                distanceToSearch /= 2;
                testBoundary = getNavMeshPoint(testBoundary, startBorderPoint, distanceToSearch);
            }

        } while (!boundaryFound);

        return new JumpBoundaries(jumpLink, currentBoundary, startBorderPoint, startBorder.getDirection(), endBorderPoint.getPoint(), endBorderPoint.getDirection());
    }
    private static final int BOUNDARY_THRESHOLD = 50;

    /**
     * Border point lays on the nav mesh edge.
     *
     * @param start
     * @param end
     * @return
     */
    public BorderPoint getBorderPoint(Location start, Location end) {

        // get a 2D projection of ray
        Line2D ray = new Line2D(start.x, start.y, end.x, end.y);
        // get the current polygon
        int pId = navMesh.getPolygonIdByLocation(start);
        if (pId < 0) {
            return new BorderPoint(start, null);
        }

        // how to find end of navmesh?
        // 1. start on the polygon of starting location
        // 2. find where the line crosses its border
        // 3. while there is another polyogn behind, repeat
        // 4. return the last cross (its distance from location)
        int currentPolygonId = pId;
        int nextPolygonId = -1;

        // find the first cross
        Point2D cross = null;
        int v1 = -1, v2 = -1;
        double[] vertex1 = null;
        double[] vertex2 = null;
        int[] polygon = navMesh.getPolygon(currentPolygonId);
        for (int i = 0; i < polygon.length; i++) {
            v1 = polygon[i];
            v2 = polygon[((i == polygon.length - 1) ? 0 : i + 1)];
            vertex1 = navMesh.getVertex(v1);
            vertex2 = navMesh.getVertex(v2);
            Line2D edge = new Line2D(vertex1[0], vertex1[1], vertex2[0], vertex2[1]);
            cross = ray.getIntersection(edge);
            if (cross != null) {
                if (((cross.x <= Math.max(edge.p1.x, edge.p2.x) && cross.x >= Math.min(edge.p1.x, edge.p2.x)) || Math.abs(cross.x - edge.p1.x) < 0.0001)
                        && ((cross.x <= Math.max(ray.p1.x, ray.p2.x) && cross.x >= Math.min(ray.p1.x, ray.p2.x)) || Math.abs(cross.x - ray.p1.x) < 0.0001)) {
                    // is a cross!
                    break;
                } else {
                    // it's not a cross
                    cross = null;
                }
            }
        }
        // now we have the cross.
        // if it too far from location, we return 0;
        if (cross == null) {
            return new BorderPoint(start, null);
        }

        // is there another polygon behind?
        nextPolygonId = navMesh.getNeighbourPolygon(currentPolygonId, v1, v2);

        Location vertex1Loc = new Location(vertex1);
        Location vertex2Loc = new Location(vertex2);
        Location crossLocation = new Location(cross.x, cross.y);

        double koef = vertex1Loc.getDistance2D(crossLocation) / vertex1Loc.getDistance2D(vertex2Loc);

        Location border = vertex1Loc.interpolate(vertex2Loc, koef);

        if (nextPolygonId == -1) {
            // there is no polygon. we return distance of cross

            return new BorderPoint(border.addZ(NavMeshConstants.liftPolygonLocation), vertex2Loc.sub(vertex1Loc));

        } else {
            // if there is another polygon, we return recursively border in that direction
            // move a little so it is in the neighbour polygon
            Vector2D directionVector = new Vector2D(end.x - start.x, end.y - start.y);
            directionVector.normalize();

            return getBorderPoint(border.addXYZ(directionVector.getX(), directionVector.getY(), NavMeshConstants.liftPolygonLocation), end);
        }
    }

    /**
     * Decides whether jump is needed for following given link.
     *
     * @param link Link to analyze.
     * @return True if jump is needed for successful following of given link.
     */
    public boolean needsJump(NavPointNeighbourLink link) {
        if (link == null) {
            //It is NavMesh link, no jump is needed
            return false;
        } else {
            //Off mesh link
            if ((link.getFlags() & LinkFlag.JUMP.get()) != 0) {
                //Jump flag present
                return true;
            }
            if (link.isForceDoubleJump()) {
                //Flag for double jump is present
                return true;
            }
            if (link.getNeededJump() != null) {
                //Jump information is present
                return true;
            }

            //TODO: Analyze further
            //TODO: Check slope
            //TODO: Raycasts?
        }

        return false;
    }

    public Double computeJump(Location start, JumpBoundaries boundaries, double velocity, double jumpAngleCos) {
        if (log.isLoggable(Level.FINER)) {
            log.log(Level.FINER, "Computing jump. Start: {0} End: {1} Velocity: {2}", new Object[]{start, boundaries.getLandingTarget(), velocity});
        }

        double distance2d = getDistance2D(start, boundaries.getLandingTarget(), jumpAngleCos);

        double targetZ = boundaries.getLandingTarget().z - start.z;
        if (log.isLoggable(Level.FINER)) {
            log.log(Level.FINER, "Computing jump. Target Z: {0}", targetZ);
        }

        Double force = computeJump(targetZ, distance2d, velocity, jumpAngleCos);

        if (force == Double.NaN || force < 0) {
            return force;
        }

        Location collisionLocation = getCollisionLocation(boundaries);
        if (collisionLocation == null) {
            return force;
        } else {
            double collisionDistance = getDistance2D(start, collisionLocation, jumpAngleCos);
            double timeToPassDistance = getTimeToPassDistance(collisionDistance, velocity);

            double collidingTime = JUMP_PEEK_TIME * (force <= MAX_SINGLE_JUMP_POWER ? 1 : 2) - timeToPassDistance;
            if (collidingTime > 0) {
                //Collision will occur...
                double collisionCoef = 1 + (collidingTime / JUMP_PEEK_TIME);
                force = Math.min(MAX_DOUBLE_JUMP_POWER, force * collisionCoef);
                log.log(Level.FINER, "Possible jump collision detected, adjusting power. NEW POWER: {0}", force);
            }

            return force;
        }

    }

    public Double computeJump(double targetZ, double distance2d, double velocity, double jumpAngleCos) {

        if (!isJumpable(distance2d, velocity, targetZ)) {
            //We are not able to jump there
            log.finer("We are not able to jump there!");
            return Double.NaN;
        }

        double timeToPassDistance = getTimeToPassDistance(distance2d, velocity);
        if (log.isLoggable(Level.FINER)) {
            log.log(Level.FINER, "Computing jump. Time to pass the distance: {0}", timeToPassDistance);
        }

        double power;

        if (isSingleJumpable(distance2d, velocity, targetZ)) {
            log.log(Level.FINER, "Computing jump. Single jump should suffice.");
            //TODO: Check - COrrection -> Jump Delay
            power = getSingleJumpPower(targetZ, timeToPassDistance - 0.055);
        } else {
            log.log(Level.FINER, "Computing jump. Double jump will be needed.");
            power = getDoubleJumpPower(targetZ, timeToPassDistance - 0.055, UnrealUtils.FULL_DOUBLEJUMP_DELAY);
        }

        //Solved in jump computign -  ||
        if ((power < 340 && timeToPassDistance < 0.39) || (power > 340 && timeToPassDistance < 0.78)) {
            double resultZ = getZDiffForJump(power, timeToPassDistance - 0.055, power > MAX_SINGLE_JUMP_POWER, 0.39);
            log.log(Level.FINER, "Computing jump Z for checking. Result: {0}", resultZ);
            double lastZ = -1000;
            while (resultZ < targetZ && resultZ > lastZ && power <= MAX_DOUBLE_JUMP_POWER) {
                log.log(Level.FINER, "Computing jump. Checking power. Wanted Z: {0}, Computed Z: {1}, increasing power by 50", new Object[]{targetZ, resultZ});
                power += 50;
                lastZ = resultZ;
                resultZ = getZDiffForJump(power, timeToPassDistance - 0.055, power > MAX_SINGLE_JUMP_POWER, 0.39);
            }
            power = Math.min(power, MAX_DOUBLE_JUMP_POWER);
            log.log(Level.FINER, "Computing jump. Final power: {0}", power);
        }

        return power;
    }

    private double getDistance2D(Location start, Location end, double jumpAngleCos) {
        //TODO: Add take-off delay
        //TODO: Added angle distance correction
        double distance2d = start.getDistance2D(end);
        if (log.isLoggable(Level.FINER)) {
            log.log(Level.FINER, "Computing jump. Distance2D: {0} AngleCos: {1}", new Object[]{distance2d, jumpAngleCos});
        }
        if (jumpAngleCos > 0) {
            distance2d = start.getDistance2D(end) / jumpAngleCos;
            if (log.isLoggable(Level.FINER)) {
                log.log(Level.FINER, "Computing jump. Distance2D after angle correction: {0}", distance2d);
            }
        } else {
            if (log.isLoggable(Level.FINER)) {
                log.log(Level.FINER, "Computing jump. Jump angle is > 90, no angle correction: {0}", distance2d);
            }
        }
        return distance2d;
    }

    public boolean isJumpable(Location start, Location end, double velocity) {
        if (start == null || end == null) {
            return false;
        }

        if (end.z - start.z > MAX_JUMP_HEIGHT) {
            //We cannot jump higher than MAX_JUMP_HEIGHT.
            return false;
        }

        double distance2d = start.getDistance2D(end);

        return isJumpable(distance2d, velocity, end.z - start.z);
    }

    private boolean isJumpable(double distance2d, double velocity, double zDiff) {
        double timeToPassDistance = getTimeToPassDistance(distance2d, velocity);

        double computedZDiff;
        if (timeToPassDistance < 2 * JUMP_PEEK_TIME) {
            computedZDiff = MAX_JUMP_HEIGHT;
        } else {
            computedZDiff = getZDiffForJump(MAX_DOUBLE_JUMP_POWER, timeToPassDistance, true, 0.39);
        }

        return computedZDiff >= zDiff;
    }

    private double getTimeToPassDistance(double distance2d, double velocity) {
        return distance2d / velocity;
        //TODO: Inspect - strange behaviour
//        if (distance2d < velocity * SPEED_BOOST_DELAY) {
//            return distance2d / velocity;
//        } else {
//            return SPEED_BOOST_DELAY + (distance2d - velocity * SPEED_BOOST_DELAY) / (velocity * JUMP_SPEED_BOOST);
//        }
    }

    private static final double JUMP_SPEED_BOOST = 1.08959;

    //
    // JUMP EQUATION - SINGLE JUMP
    // z = -475 * t^2 + power * t
    //
    // t = Unreal time
    //
    //
    // JUMP EQUATION - DOUBLE JUMP
    // z = -475 * t^2 + power * t + koef * (t - delay)
    //
    // koef = (power - 340) * 1.066 + 2 
    //
    // t = Unreal time
    //
    private double getZDiffForJump(double power, double deltaTime, boolean isDoubleJump, double delay) {
        double z = 0;

        //Equation for single jump
        z += -475 * (deltaTime * deltaTime) + Math.min(power, MAX_SINGLE_JUMP_POWER) * deltaTime;
        if (isDoubleJump) {
            //Double jump specific
            z += ((power - MAX_SINGLE_JUMP_POWER) * 1.066 + 2) * (deltaTime - delay);
        }
        return z;
    }

    public double getSingleJumpPower(double targetZ, double time) {
        double power = (targetZ + 475 * time * time) / time;

        if (power > MAX_SINGLE_JUMP_POWER) {
            return -1;
        }

        return power;
    }

    public double getDoubleJumpPower(double targetZ, double time, double delay) {
        double power;

        if (time < delay) {
            if (log.isLoggable(Level.FINER)) {
                log.log(Level.FINER, "Computing double jump power. Time is lower than delay, using collisin equation.");
            }
            power = 3.1362 * (targetZ + 20) + 287;
        } else {

            //power = (targetZ + 475 * time * time + 360.5 * (time - delay)) / (2.066 * time - 1.066 * delay);
            power = (targetZ + 475 * time * time + 20 * time - 140) / (1.066 * (time - delay));
            if (log.isLoggable(Level.FINER)) {
                log.log(Level.FINER, "Computing double jump power. targetZ: {0}, Time: {1}, Delay: {2}, POWER: {3}", new Object[]{targetZ, time, delay, power});
            }
        }

        if (power > MAX_DOUBLE_JUMP_POWER) {
            return -1;
        }

        return power;
    }

    private Location getNavMeshPoint(Location from, Location direction, double distance) {
        //TODO: Implement properly
        return from.interpolate(direction, distance / from.getDistance2D(direction));
    }

    private boolean isSingleJumpable(double distance2d, double velocity, double zDiff) {
        if (zDiff > MAX_SINGLE_JUMP_HEIGHT) {
            //We cannot jump higher than MAX_JUMP_HEIGHT.
            return false;
        }

        double timeToPassDistance = getTimeToPassDistance(distance2d, velocity);

        double computedZDiff = getZDiffForJump(MAX_SINGLE_JUMP_POWER, timeToPassDistance, false, 0);

        return computedZDiff >= zDiff;
    }

    public Location getCollisionLocation(JumpBoundaries boundaries) {

        if (!boundaries.isJumpUp()) {
            return null;
        }
        if (boundaries.getTargetEdgeDirection() == null) {
            return null;
        }

        Location edgeDirection = boundaries.getTargetEdgeDirection().setZ(0).getNormalized();
        Location computedDirection = boundaries.getLandingTarget().sub(boundaries.getTakeOffMax()).setZ(0).getNormalized();
        Location verticalDirection = new Location(edgeDirection.y, -edgeDirection.x);

        double angleCos = verticalDirection.dot(computedDirection);

        double correctionDistance = (2 * BOT_RADIUS) / angleCos;

        double koef = correctionDistance / boundaries.getLandingTarget().getDistance2D(boundaries.getTakeOffMax());

        Location collisionLocation;
        if (koef > 1.0) {
            collisionLocation = boundaries.getTakeOffMax();
        } else {
            collisionLocation = boundaries.getLandingTarget().interpolate(boundaries.getTakeOffMax(), koef);
        }

        return collisionLocation;
    }

    public Location getNearestMeshDirection(Location location, Location direction) {

        //TODO: Not good enough - centre of polygon not suitable for big polygons
        NavMeshPolygon poly = navMesh.getNearestPolygon(location);

        Line2D ray = new Line2D(location.x, location.y, location.x + direction.x * 10000, location.y + direction.y * 10000);

        int[] polygon = navMesh.getPolygon(poly.getPolygonId());
        for (int i = 0; i < polygon.length; i++) {
            int v1 = polygon[i];
            int v2 = polygon[((i == polygon.length - 1) ? 0 : i + 1)];
            double[] vertex1 = navMesh.getVertex(v1);
            double[] vertex2 = navMesh.getVertex(v2);
            Line2D edge = new Line2D(vertex1[0], vertex1[1], vertex2[0], vertex2[1]);
            Point2D cross = ray.getIntersection(edge);
            if (cross != null) {
                if (((cross.x <= Math.max(edge.p1.x, edge.p2.x) && cross.x >= Math.min(edge.p1.x, edge.p2.x)) || Math.abs(cross.x - edge.p1.x) < 0.0001)
                        && ((cross.x <= Math.max(ray.p1.x, ray.p2.x) && cross.x >= Math.min(ray.p1.x, ray.p2.x)) || Math.abs(cross.x - ray.p1.x) < 0.0001)) {
                    // is a cross!
                    Location vertex1Loc = new Location(vertex1);
                    Location vertex2Loc = new Location(vertex2);

                    return vertex2Loc.sub(vertex1Loc);
                } else {
                    // it's not a cross
                    cross = null;
                }
            }
        }
        return null;
    }

}
