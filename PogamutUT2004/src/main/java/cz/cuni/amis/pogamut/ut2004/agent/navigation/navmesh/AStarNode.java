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
package cz.cuni.amis.pogamut.ut2004.agent.navigation.navmesh;

import cz.cuni.amis.pogamut.base3d.worldview.object.ILocated;
import java.util.ArrayList;
import java.util.List;

/**
 * class used in algorithm A* in pathfinding in NavMesh
 * @author Jakub
 */
public class AStarNode {
    private INavMeshAtom atom;
    private AStarNode from;
    private List<AStarNode> followers;
    private double distanceFromStart;
    private double estimatedDistanceToTarget;
    private double estimatedTotalDistance;
    
    public AStarNode(AStarNode from, INavMeshAtom atom, NavMesh mesh, INavMeshAtom start, INavMeshAtom target) {
        
        this.from = from;
        this.atom = atom;
        followers = new ArrayList<AStarNode>();
        
        // count distance from start
        if(from == null) {
            distanceFromStart = 0;
        }
        else {
            distanceFromStart = from.getDistanceFromStart() + mesh.getDistance(from.atom, atom);
        }
        
        // count distance to end
        estimatedDistanceToTarget = mesh.getDistance(atom, target);
        
        // count total distance
        estimatedTotalDistance = distanceFromStart + estimatedDistanceToTarget;   
    }
    
    public double getDistanceFromStart() {
        return distanceFromStart;
    }
    public double getEstimatedDistanceToTarget() {
        return estimatedDistanceToTarget;
    }
    public double getEstimatedTotalDistance() {
        return estimatedTotalDistance;
    }
    public INavMeshAtom getAtom() {
        return atom;
    }
    public AStarNode getFrom() {
        return from;
    } 
    public List<AStarNode> getFollowers() {
        return followers;
    }
}
