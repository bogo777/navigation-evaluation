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

import java.util.ArrayList;

/**
 * Serializable container for key data of Navmesh
 * This the part of Navmesh that can be stored in a file
 * @author Jakub
 */
public class NavMeshCore implements java.io.Serializable {
    public ArrayList verts;
    public ArrayList polys;
    public ArrayList vertsToPolys;
    public BSPNode bspTree;
    public BSPNode biggestLeafInTree;  
    public ArrayList offMeshPoints;
    public ArrayList polysToOffMeshPoints;    
    public ArrayList safeVertex;
}
