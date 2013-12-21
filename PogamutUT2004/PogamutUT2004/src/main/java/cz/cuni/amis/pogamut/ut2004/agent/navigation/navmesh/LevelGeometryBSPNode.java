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

import java.io.Serializable;
import java.util.ArrayList;
import math.geom2d.line.StraightLine2D;

/**
 * Node for building a BSP tree structure on the level geometry
 * @author Jakub
 */
public class LevelGeometryBSPNode implements Serializable {
    
    public LevelGeometry geom;
    
    // list of triangle ids (actual polygons are stored in navmesh)
    public ArrayList<Integer> triangles = new ArrayList<Integer>();
    public LevelGeometryBSPNode parent;
    
    // parameters of the space part
    public double minX, maxX, minY, maxY, minZ, maxZ;
    // separator dimension and value
    public int sepDim;
    public double sepVal;
    
    // node children
    public LevelGeometryBSPNode left;
    public LevelGeometryBSPNode right;    
    
    /**
     * Constructor sets the parameters of space
     * @param geom
     * @param parent
     * @param minX
     * @param maxX
     * @param minY
     * @param maxY
     * @param minZ
     * @param maxZ 
     */
    public LevelGeometryBSPNode(LevelGeometry geom, LevelGeometryBSPNode parent, double minX, double maxX, double minY, double maxY, double minZ, double maxZ) {
        this.geom = geom;
        this.parent = parent;
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
        this.minZ = minZ;
        this.maxZ = maxZ;        
    }

    /**
     * Main recursive function of BSP. builds the tree.
     */
    void build() throws Exception {
        // test end of recursion
        if (!shouldSplit()) {
            // set biggest leaf?
            if(triangles.size() > geom.getNumberOfTrianglesInBiggestLeaf()) {
                System.out.println("Setting biggest leaf sofar. Number of polygons in this node is " + triangles.size());
                geom.setBiggestLeafInTree(this);
            }
            geom.leafCount++;
            return;
        }
        
        System.out.println("build(): splitting " + triangles.size() + " triangles");
        
        // now we must split this block
        // which side? the longest one!
        double maxLength = 0.0;
        sepDim = -1;
        if(maxX - minX > maxLength) { maxLength = maxX - minX; sepDim = 0; sepVal = (maxX + minX)/2; };
        if(maxY - minY > maxLength) { maxLength = maxY - minY; sepDim = 1; sepVal = (maxY + minY)/2; };
        if(maxZ - minZ > maxLength) { maxLength = maxZ - minZ; sepDim = 2; sepVal = (maxZ + minZ)/2; };
        System.out.println("The longest side is this long: "+maxLength+". The dimension is: "+sepDim);
        
        // create new nodes
        //left son
        this.left = new LevelGeometryBSPNode(geom, this, minX, (sepDim==0 ? sepVal : maxX),
                                                         minY, (sepDim==1 ? sepVal : maxY),
                                                         minZ, (sepDim==2 ? sepVal : maxZ));
        this.right = new LevelGeometryBSPNode(geom, this, (sepDim==0 ? sepVal : minX), maxX,
                                                          (sepDim==1 ? sepVal : minY), maxY,
                                                          (sepDim==2 ? sepVal : minZ), maxZ);
        
        // walk through triangles and put them into the the nodes
        for(int i=0; i<triangles.size(); i++) {
            int tId = triangles.get(i);  
            int[] triangle = geom.triangles.get(tId);
            
            // condition for triangle to be in left node: at least one vertex is under sepVal
            // condition for triangle to be in right node: at least one vertex is over sepVal
            boolean isInLeft = false, isInRight = false;            
            
            for(int j = 0; j<3; j++) {
                int vId = triangle[j];
                double[] v = geom.verts.get(vId);        
                if(v[sepDim] < sepVal) isInLeft = true;
                if(v[sepDim] > sepVal) isInRight = true; 
            }
            
            if(isInLeft) this.left.triangles.add(tId);
            if(isInRight) this.right.triangles.add(tId);  
            
            if(!isInLeft && !isInRight) {
                throw new Exception("this triangle is on neither side. That's impossible!");
            }
        }
        
        System.out.println("build(): Left has " + left.triangles.size() + " triangles.");
        System.out.println("build(): Right has " + right.triangles.size() + " triangles.");
        // now triangles are in their nodes.
        // we will check the split factor before buildin them
        double sl = left.triangles.size();
        double sr = right.triangles.size();
        double s = this.triangles.size();
        double crossFactor = (sl+sr-s) / s;
        
        if(crossFactor > NavMeshConstants.maxAllowedCrossFactor) {
            System.out.println("The cross factor is "+crossFactor+". This node will not be splitted. Let it be a leaf.");
            this.left = null;
            this.right = null;
            // set biggest leaf?
            if(triangles.size() > geom.getNumberOfTrianglesInBiggestLeaf()) {
                System.out.println("Setting biggest leaf sofar. Number of polygons in this node is " + triangles.size());
                geom.setBiggestLeafInTree(this);
            }
            geom.leafCount++;
            return;
        }
        else {
          System.out.println("Node splitted with cross factor " + crossFactor);
          left.build();
          right.build();
        }
        
    }
    

    /**
     * Decides whether this node should split.
     * @return 
     */
    private boolean shouldSplit() {
        return (triangles.size() > NavMeshConstants.stopSplittingNumberOfTriangles
                && (maxX - minX) > NavMeshConstants.stopSplittingSizeOfOneBlock);
    }
   
    /**
     * returns the higher nomber;
     * @param d1
     * @param d2
     * @return 
     */
    private double max(double d1, double d2) {
        return d1 > d2 ? d1 : d2;
    }

    /**
     * Recursively walks through the tree and deletes lists of triangles in inner nodes.
     * This saves memory.
     */
    void cleanInnerNodes() {
        if(left == null && right==null) return;
        this.triangles = null;
        if(left!=null) left.cleanInnerNodes();
        if(right!=null) right.cleanInnerNodes();
    }
    
}
