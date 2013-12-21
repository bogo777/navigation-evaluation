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

import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.base3d.worldview.object.Rotation;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensomotoric.BSPRayInfoContainer;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.DrawStayingDebugLines;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.AutoTraceRay;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.AutoTraceRayMessage;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Self;
import cz.cuni.amis.pogamut.ut2004.server.impl.UT2004Server;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;
import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;
import math.geom2d.Point2D;
import math.geom2d.line.StraightLine2D;
import math.geom3d.Point3D;
import math.geom3d.Vector3D;
import math.geom3d.line.StraightLine3D;
import math.geom3d.plane.Plane3D;

/**
 * Class containing complete data with the geometry of the environment
 * It is useful for steering
 * It is part of NavMesh class
 * @author Jakub Tomek
 */
public class LevelGeometry implements Serializable {
    
    public ArrayList<double[]> verts = new ArrayList<double[]>();
    public ArrayList<int[]>  triangles = new ArrayList<int[]>();
    private LevelGeometryBSPNode bspTree;
    private LevelGeometryBSPNode biggestLeafInTree;
    public int leafCount = 0;
    
    // boundries of the space
    public double minX = Double.MAX_VALUE, maxX = Double.MIN_VALUE, minY = Double.MAX_VALUE, maxY = Double.MIN_VALUE, minZ = Double.MAX_VALUE, maxZ = Double.MIN_VALUE;    
    
    public Random random = new Random();
    
    /**
     * Constructor creates the object from a file defined by map name and path which is in constants
     * @param mapName 
     */
    public LevelGeometry(String mapName) throws FileNotFoundException, IOException, Exception {
        
        // vertices are indexed from 1. Put something to 0
        verts.add(new double[0]);
        
        double scale;
        double[] centre;
        String fileName, line;
        BufferedReader br;
        
        // read scale
        fileName = NavMeshConstants.pureLevelGeometryReadDir + "\\" + mapName + ".scale";
        br = new BufferedReader(new FileReader(fileName));
        line = br.readLine();
        scale = Double.parseDouble(line);
        
        // read centre
        fileName = NavMeshConstants.pureLevelGeometryReadDir + "\\" + mapName + ".centre";
        br = new BufferedReader(new FileReader(fileName));
        line = br.readLine();
        String[] sc = line.split("[ \\t]");
        centre = new double[3];
        for(int i=0; i<3; i++) {
            centre[i] = Double.parseDouble(sc[i]);
        }
        
        // read all vertices and triangles from file
        fileName = NavMeshConstants.pureLevelGeometryReadDir + "\\" + mapName + ".obj";
        br = new BufferedReader(new FileReader(fileName));     
        while((line = br.readLine()) != null) {
            String[] words = line.split("[ \\t]");
            if(words[0].equals("v")) {
                double[] v = new double[3];
                v[0] = Double.parseDouble(words[1])*scale + centre[0];
                v[1] = Double.parseDouble(words[3])*scale + centre[1];
                v[2] = Double.parseDouble(words[2])*scale + centre[2];
                verts.add(v);
                
                // check the boundries
                if(v[0] < minX) minX = v[0];
                if(v[0] > maxX) maxX = v[0];
                if(v[1] < minY) minY = v[1];
                if(v[1] > maxY) maxY = v[1];
                if(v[2] < minZ) minZ = v[2];
                if(v[2] > maxZ) maxZ = v[2];                
                
            }
            if(words[0].equals("f")) {
                int[] t = new int[3];
                t[0] = Integer.parseInt(words[1]);
                t[1] = Integer.parseInt(words[2]);
                t[2] = Integer.parseInt(words[3]);
                triangles.add(t);
            }
        }
        
        // create a BSP tree structure
        resetBSPTree();
    }
    
    public void draw(UT2004Server server, Location color) {
        for(int tId = 0; tId < triangles.size(); tId++) {
            DrawStayingDebugLines d = new DrawStayingDebugLines();
            String lines = this.triangleToDebugString(tId);
            d.setVectors(lines);
            d.setColor(new Location(255, 255, 255));
            d.setClearAll(tId==0);                
            server.getAct().act(d); 
        }
    }

    public void drawOneTriangle(UT2004Server server, int tId, Location color) {
        DrawStayingDebugLines d = new DrawStayingDebugLines();
        String lines = this.triangleToDebugString(tId);
        d.setVectors(lines);
        d.setColor(color);
        d.setClearAll(false);                
        server.getAct().act(d);                           
    }

    private String triangleToDebugString(int tId) {
        StringBuilder result = new StringBuilder("");      
        // projdeme vsechny polygony a vykreslime caru vzdy z vrcholu n do n+1
        int[] t = (int[]) triangles.get(tId);
        for(int j = 0; j<t.length; j++) {
            if(result.length()>0) result.append(";");
            // ziskame vrcholy v1 a v2 mezi kterymi vykreslime caru
            double[] v1,v2;
            v1 = (double[]) verts.get(t[j]);
            if(j==t.length-1) v2 = (double[]) verts.get(t[0]);
            else v2 = (double[]) verts.get(t[j+1]);
            // pridani cary
            result.append(v1[0]+","+v1[1]+","+v1[2]+";"+v2[0]+","+v2[1]+","+v2[2]);
        }     
        return result.toString(); 
    }

    /**
     * Gets a new BSPTree for this geometry
     */
    private void resetBSPTree() throws Exception {
        System.out.println("Creating BSP tree...");
        bspTree = new LevelGeometryBSPNode(this, null, minX, maxX, minY, maxY, minZ, maxZ);
        for(int i = 0; i<triangles.size(); i++) bspTree.triangles.add(i);
        biggestLeafInTree = null;
        bspTree.build();
        System.out.println("BSP tree is done building.");
        System.out.println("Biggest leaf has " + biggestLeafInTree.triangles.size() + " triangles.");
        System.out.println("BSP tree has " +leafCount+ " leaves");
        bspTree.cleanInnerNodes();
        
    }

    /**
     * Sets the biggest leag in tree
     * @param node 
     */
    void setBiggestLeafInTree(LevelGeometryBSPNode node) {
        biggestLeafInTree = node;
    }

    int getNumberOfTrianglesInBiggestLeaf() {
        if(biggestLeafInTree==null) return 0;
        else return biggestLeafInTree.triangles.size();
    }

    /**
     * Returns the node of BSP leaf which contains this location 
     * Walk througt BSP tree compating the coordinates
     * @param from
     * @return 
     */
    private LevelGeometryBSPNode getBSPLeaf(Location location) {
        double x = location.x;
        double y = location.y;
        double z = location.z;
        
        LevelGeometryBSPNode node = this.bspTree;
        //check if we are in
        if(x>node.maxX || x<node.minX) return null;
        if(y>node.maxY || y<node.minY) return null;
        if(z>node.maxZ || z<node.minZ) return null;
        
        // we want a leaf!
        while(node.left!=null) {
            // in which half are we?
            switch(node.sepDim) {
                case 0:
                    if(x<node.sepVal) node = node.left;
                    else node = node.right;
                    break;
                case 1:
                    if(y<node.sepVal) node = node.left;
                    else node = node.right;                    
                    break;
                case 2:
                    if(z<node.sepVal) node = node.left;
                    else node = node.right;                    
                    break;
                default: throw new UnsupportedOperationException("This should not happen!");
            }
        }
        return node;
    }
    
    /**
     * The most important method on this class.
     * It computes the information about collision of the given ray and this geometry
     * @param self
     * @param rayInfo
     * @return 
     */
    public AutoTraceRay getAutoTraceRayMessage(Self self, BSPRayInfoContainer rayInfo) {

        // lets update the direction vector according to agen't rotation  
        Vector3d direction = rayInfo.direction;
        direction.normalize();
        
        // angle up/down
        double upDownAngle = Math.asin(direction.getZ()/1);
        
        // we transform the ray vector to polar coordinates 
        // -32768 - 32767 total is 65536
        double UTFullAngle = 65536;
        double UTHalfAngle = UTFullAngle/2;
        double UTQuarterAngle = UTHalfAngle/2;        
        // yaw:  
        double rayYaw;
        double x = rayInfo.direction.x;
        double y = rayInfo.direction.y;
        
        if(x==0 && y==0) {
            direction = new Vector3d(0,0,1);
        } else {
            rayYaw = NavMeshConstants.transform2DVectorToRotation(new Vector2d(x,y));
            // now we have the input yaw
            // let's add adent's rotation to get the final rotation
            double Yaw = rayYaw + self.getRotation().getYaw();
            Vector2d vector2d = NavMeshConstants.transformRotationTo2DVector(Yaw);
            direction.x = vector2d.x;
            direction.y = vector2d.y;
            // now we have the direction on x,y.
            // we must add back the z direction
            direction.normalize();
            direction.z = Math.tan(upDownAngle);           
        }
        
        
        direction.normalize();
        // last thing with direction: stretch it to the proper length
        direction.x *= rayInfo.length;
        direction.y *= rayInfo.length;
        direction.z *= rayInfo.length;   
        // The direction is now Done. The agent's rotation is added


        
        // now get the from and to locations
        
        Location from = self.getLocation(); 
        Location to = new Location(from.x + direction.x, from.y + direction.y, from.z + direction.z); 

        // call the recursive function
        return getAutoTraceRayMessage(from, to, rayInfo);
    }
    
    /**
     * Recursive function for getting ray collision
     * @param from
     * @param to
     * @param rayInfo
     * @return 
     */
    private AutoTraceRay getAutoTraceRayMessage(Location from, Location to, BSPRayInfoContainer rayInfo) {
        // the actual 3D line
        StraightLine3D ray =  new StraightLine3D(from.asPoint3D(), to.asPoint3D());              
        

        
        // information abou ray's collision with triangle
        boolean collisionFound = false;
        double collisionDistance = Double.MAX_VALUE;
        Location hitLocation = null;
        Vector3d normalVector = null;
        
        // now start examining hitting the walls
        // let's get the BSP leaf
        LevelGeometryBSPNode leaf = this.getBSPLeaf(from);
        
        // now let's examine the ray's collisions with triangles
        for(Integer tId : leaf.triangles) {
            int[] triangle = this.triangles.get(tId);
            double[] v1 = this.verts.get(triangle[0]);
            double[] v2 = this.verts.get(triangle[1]);
            double[] v3 = this.verts.get(triangle[2]);
            Point3D p1 = new Point3D(v1[0],v1[1],v1[2]);
            Vector3D vector1 = new Vector3D(v2[0]-v1[0], v2[1]-v1[1], v2[2]-v1[2]);
            Vector3D vector2 = new Vector3D(v3[0]-v1[0], v3[1]-v1[1], v3[2]-v1[2]);            
            Plane3D plane = new Plane3D(p1,vector1,vector2);
            Point3D intersection = plane.getLineIntersection(ray);
            // is intersection inside triangle?
            boolean collision = this.isPointInsideTriangle(intersection, tId);
            
            // we remember the closest collision
            if(collision) {
                double distance = intersection.getDistance(from.asPoint3D());
                if(distance < collisionDistance) {
                    collision = true;
                    collisionDistance = distance;
                    hitLocation = new Location(intersection.getX(),intersection.getY(),intersection.getZ());
                    Vector3D normalVector3D = plane.getNormalVector();
                    normalVector = new Vector3d(normalVector3D.getX(),normalVector3D.getY(),normalVector3D.getZ());
                }
            }
        }
        
        // if we found a collision, we collect and return the information about it
        if(collisionFound) {
            return new AutoTraceRayMessage(
                    rayInfo.unrealId,
                    from,
                    to,
                    false,
                    rayInfo.floorCorrection,
                    true,
                    normalVector,
                    hitLocation,
                    false,
                    null
                    );
        } 
        else {
            // we did not find a collision. Does the ray continue to different node?
            boolean out = false;
            
            if(to.x > leaf.maxX) {
                out = true;
            }
            if(to.x < leaf.minX) {
                out = true;
            }         
            if(to.y > leaf.maxY) {
                out = true;
            }
            if(to.y < leaf.minY) {
                out = true;
            }
            if(to.z > leaf.maxZ) {
                out = true;
            }
            if(to.z < leaf.minZ) {
                out = true;
            }
            
            // if we are out, we must continue to the next node
            if(out) {
                // we must find the ray's intersections with the nodee's borders. 
                // we will take 3 intersection with planes bordering the node on x,y,z and take the clocest one
                
                collisionDistance = Double.MAX_VALUE;
                Point3D collisionPoint = null; 
                double distance;
                
                // 1. x axis
                if(to.x > from.x) {
                    Point3D p1 = new Point3D(leaf.maxX,0,0);
                    Vector3D vector1 = new Vector3D(0, 1, 0);
                    Vector3D vector2 = new Vector3D(0, 0, 1);
                    Plane3D plane = new Plane3D(p1,vector1,vector2);
                    Point3D intersection = plane.getLineIntersection(ray);
                    distance = intersection.getDistance(ray.getFirstPoint());
                    // is it the closes one sofar?
                    if(distance < collisionDistance) {
                        collisionPoint = intersection;
                        collisionDistance = distance;
                    }
                } else {
                    Point3D p1 = new Point3D(leaf.minX,0,0);
                    Vector3D vector1 = new Vector3D(0, 1, 0);
                    Vector3D vector2 = new Vector3D(0, 0, 1);
                    Plane3D plane = new Plane3D(p1,vector1,vector2);
                    Point3D intersection = plane.getLineIntersection(ray);
                    distance = intersection.getDistance(ray.getFirstPoint());
                    // is it the closes one sofar?
                    if(distance < collisionDistance) {
                        collisionPoint = intersection;
                        collisionDistance = distance;
                    }
                }
                
                // 2. y axis
                if(to.y > from.y) {
                    Point3D p1 = new Point3D(0, leaf.maxY,0);
                    Vector3D vector1 = new Vector3D(1, 0, 0);
                    Vector3D vector2 = new Vector3D(0, 0, 1);
                    Plane3D plane = new Plane3D(p1,vector1,vector2);
                    Point3D intersection = plane.getLineIntersection(ray);
                    distance = intersection.getDistance(ray.getFirstPoint());
                    // is it the closes one sofar?
                    if(distance < collisionDistance) {
                        collisionPoint = intersection;
                        collisionDistance = distance;
                    }
                } else {
                    Point3D p1 = new Point3D(0,leaf.minY,0);
                    Vector3D vector1 = new Vector3D(1, 0, 0);
                    Vector3D vector2 = new Vector3D(0, 0, 1);
                    Plane3D plane = new Plane3D(p1,vector1,vector2);
                    Point3D intersection = plane.getLineIntersection(ray);
                    distance = intersection.getDistance(ray.getFirstPoint());
                    // is it the closes one sofar?
                    if(distance < collisionDistance) {
                        collisionPoint = intersection;
                        collisionDistance = distance;
                    }
                }
                
                // 3. z axis
                if(to.z > from.z) {
                    Point3D p1 = new Point3D(0,0,leaf.maxZ);
                    Vector3D vector1 = new Vector3D(1, 0, 0);
                    Vector3D vector2 = new Vector3D(0, 1, 0);
                    Plane3D plane = new Plane3D(p1,vector1,vector2);
                    Point3D intersection = plane.getLineIntersection(ray);
                    distance = intersection.getDistance(ray.getFirstPoint());
                    // is it the closes one sofar?
                    if(distance < collisionDistance) {
                        collisionPoint = intersection;
                        collisionDistance = distance;
                    }
                } else {
                    Point3D p1 = new Point3D(0,0,leaf.minZ);
                    Vector3D vector1 = new Vector3D(1, 0, 0);
                    Vector3D vector2 = new Vector3D(0, 1, 0);
                    Plane3D plane = new Plane3D(p1,vector1,vector2);
                    Point3D intersection = plane.getLineIntersection(ray);
                    distance = intersection.getDistance(ray.getFirstPoint());
                    // is it the closes one sofar?
                    if(distance < collisionDistance) {
                        collisionPoint = intersection;
                        collisionDistance = distance;
                    }
                }
                
                // now we have the collision point at the border of the node.
                // we will move it a little further, to push it into the node
                Vector3d direction = (Vector3d) rayInfo.direction.clone();
                direction.normalize();
                Location newPoint = new Location(collisionPoint.getX()+direction.x,collisionPoint.getX()+direction.y,collisionPoint.getX()+direction.z);
                
                // get the answer recursively...
                AutoTraceRay falseAnswer = getAutoTraceRayMessage(newPoint, to, rayInfo);
                // put back the original from point and return result
                return new AutoTraceRayMessage(
                        rayInfo.unrealId,
                        from,
                        to,
                        false,
                        rayInfo.floorCorrection,
                        falseAnswer.isResult(),
                        falseAnswer.getHitNormal(),
                        falseAnswer.getHitLocation(),
                        false,
                        null
                        ); 
            
            }
            else {
                // otherwise there is no intersection att all
                return new AutoTraceRayMessage(
                        rayInfo.unrealId,
                        from,
                        to,
                        false,
                        rayInfo.floorCorrection,
                        false,
                        null,
                        null,
                        false,
                        null
                        ); 
            
            }
        } 
    }

    /***
     * returns true is point is  inside 3D triangle, assuming it is on the same 2D plane in 3D
     * @param intersection
     * @param tId
     * @return 
     */
    private boolean isPointInsideTriangle(Point3D point3D, Integer tId) {
       boolean result = true;
       int[] triangle = this.triangles.get(tId);
            
       // let's see if the point is inside in 2D projection
       Point2D point2D = new Point2D(point3D.getX(), point3D.getY());
       double rightSide = 0.0;
       double[] v1, v2;
       for(int i = 0; i < triangle.length; i++) {
           v1 = this.verts.get(triangle[i]);
           if(i < triangle.length -1) v2 = this.verts.get(triangle[i+1]);
           else  v2 = this.verts.get(triangle[0]);
           Point2D p1 = new Point2D(v1[0], v1[1]);
           Point2D p2 = new Point2D(v2[0], v2[1]);
           StraightLine2D line = new StraightLine2D(p1, p2);
           double dist = line.getSignedDistance(point2D);
           
           if(rightSide == 0.0) {
               rightSide = Math.signum(dist);
           }
           else {
               if(rightSide * dist < 0) {
                   result = false;
                   break;
               }
           }
       }
       return result;      
    }
    
    
}
