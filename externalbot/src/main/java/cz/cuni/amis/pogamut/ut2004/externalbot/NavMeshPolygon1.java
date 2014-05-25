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
package cz.cuni.amis.pogamut.ut2004.externalbot;

import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.navmesh.INavMeshAtom;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.navmesh.NavMesh;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.navmesh.OffMeshPoint;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of INavMeshAtom for polygons
 * @author Jakub
 */
public class NavMeshPolygon1 implements INavMeshAtom {
   private int pId;
   
   NavMeshPolygon1(int pId) {
       this.pId = pId;
   }
   
   public int getPolygonId() {
       return pId;
   }

    @Override
    public List<INavMeshAtom> getNeighbours(NavMesh mesh) {
       List<INavMeshAtom> neighbours = new ArrayList<INavMeshAtom>(); 
       
       // add all nearby polygons
       List<Integer> pn = mesh.getNeighbourIdsToPolygon(pId);
       for(Integer i : pn) {
           neighbours.add(new NavMeshPolygon1(i));
       }
       
       // add all offmesh points on this polgon
       List<OffMeshPoint> ops = mesh.getOffMeshPoinsOnPolygon(pId);
       for(OffMeshPoint op : ops) {
           neighbours.add(op);
       }
       
       return neighbours; 
    }

    /**
     * Compares ids of polygons and returns true if they are the same
     * returns false if p is point
     * @param p
     * @return 
     */
    public boolean equals(INavMeshAtom atom) {
        if(atom.getClass() == NavMeshPolygon1.class) {
            NavMeshPolygon1 p = (NavMeshPolygon1) atom;
            return (p.getPolygonId()==pId);
        }
        else return false;
    }
}
