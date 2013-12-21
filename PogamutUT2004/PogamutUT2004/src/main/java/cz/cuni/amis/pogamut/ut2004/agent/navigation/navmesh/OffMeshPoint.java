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
import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPointNeighbourLink;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import math.geom3d.Point3D;

/**
 * CLass representing a navpoint which is a part of an off mesh connection in navMesh
 * Contains mainly the real navpoint and then some more information , like polygon and connections to others
 * @author Jakub Tomek
 */
public class OffMeshPoint implements ILocated, INavMeshAtom, java.io.Serializable {
    
    private UnrealId navpointId = null;
    private int pId = -1;
    private ArrayList<OffMeshEdge> outgoingEdges = new ArrayList<OffMeshEdge>();
    private ArrayList<OffMeshEdge> incomingEdges = new ArrayList<OffMeshEdge>();
    private Location location;
   
    public OffMeshPoint(NavPoint navpoint, int pId) {
        this.navpointId = navpoint.getId();
        this.pId = pId;
        this.location = navpoint.getLocation();
    }

    public UnrealId getNavPointId() {
        return navpointId;
    }

    public int getPId() {
        return pId;
    } 

    public ArrayList<OffMeshEdge> getOutgoingEdges() {
        return outgoingEdges;
    } 

    public ArrayList<OffMeshEdge> getIncomingEdges() {
        return incomingEdges;
    }

    @Override
    public Location getLocation() {
        return location;
    }

     /**
     * Gets a list of all neighbousrs of this atom in navmesh. That includes both polygons and offmesh points.
     * @return 
     */
    @Override
    public List<INavMeshAtom> getNeighbours(NavMesh mesh) {      
        List<INavMeshAtom> neighbours = new ArrayList<INavMeshAtom>();
        
        if(pId > 0) neighbours.add(new NavMeshPolygon(pId));
        
        for(OffMeshEdge oe : outgoingEdges) {
            neighbours.add(oe.getTo());
        }
        
        return neighbours;
    }
    
    /**
     * Compares atoms if they are the same (same class, same polygon/point)
     * @param atom
     * @return 
     */
    @Override
    public boolean equals(INavMeshAtom atom) {
        if(atom.getClass() == OffMeshPoint.class) {
            OffMeshPoint op = (OffMeshPoint) atom;
            return (op.navpointId.getStringId().equals(this.navpointId.getStringId()));
        }
        else return false;
    }
    
}
