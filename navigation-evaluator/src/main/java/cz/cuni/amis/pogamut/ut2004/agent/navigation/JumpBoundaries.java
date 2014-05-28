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
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPointNeighbourLink;

/**
 *
 * @author Bogo
 */
public class JumpBoundaries {

    private boolean jumpable;

    private Location takeOffMin;
    private Location takeOffMax;
    private Location takeoffEdgeDirection;

    private Location landingTarget;

    private Location targetEdgeDirection;
    private NavPointNeighbourLink link;

    public JumpBoundaries(NavPointNeighbourLink link, Location takeOffMin, Location takeOffMax, Location takeoffEdgeDirection, Location target, Location targetEdgeDirection) {
        this.link = link;
        this.takeOffMin = takeOffMin;
        this.takeOffMax = takeOffMax;
        this.takeoffEdgeDirection = takeoffEdgeDirection;
        this.landingTarget = target;
        this.targetEdgeDirection = targetEdgeDirection;
        this.jumpable = true;
    }

    public JumpBoundaries(NavPointNeighbourLink link) {
        this.link = link;
        this.jumpable = false;
    }

    public NavPointNeighbourLink getLink() {
        return link;
    }

    public void setLink(NavPointNeighbourLink link) {
        this.link = link;
    }

    public boolean isJumpable() {
        return jumpable;
    }

    public void setJumpable(boolean jumpable) {
        this.jumpable = jumpable;
    }

    public Location getTakeOffMin() {
        return takeOffMin;
    }

    public void setTakeOffMin(Location takeOffMin) {
        this.takeOffMin = takeOffMin;
    }

    public Location getTakeOffMax() {
        return takeOffMax;
    }

    public void setTakeOffMax(Location takeOffMax) {
        this.takeOffMax = takeOffMax;
    }

    public Location getLandingTarget() {
        return landingTarget;
    }

    public void setLandingTarget(Location landingTarget) {
        this.landingTarget = landingTarget;
    }

    boolean isInBoundaries(Location botLocation) {
        if (!jumpable) {
            return false;
        }

        if (takeOffMin.equals(takeOffMax, 1.0)) {
            //We have only the point, we are out of NavMesh
            return botLocation.getDistance(takeOffMin) < 20;
        }

        //TODO: Improve || inform about passing max boundary
        return botLocation.getDistance(takeOffMax) + botLocation.getDistance(takeOffMin) <= 2 * takeOffMin.getDistance(takeOffMax);
    }

    boolean isPastBoundaries(Location botLocation) {
        if (jumpable) {
            return botLocation.getDistance(landingTarget) < takeOffMax.getDistance(landingTarget);
        } else {
            return false;
        }
    }

    public boolean isJumpUp() {
        return jumpable && landingTarget.z > takeOffMax.z;
    }

    public Location getTargetEdgeDirection() {
        return targetEdgeDirection;
    }

    public Location getTakeoffEdgeDirection() {
        return takeoffEdgeDirection;
    }

}
