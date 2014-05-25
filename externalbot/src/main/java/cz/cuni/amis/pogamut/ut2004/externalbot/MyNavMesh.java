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

/**
 *
 * @author Jakub Tomek
 */
import cz.cuni.amis.pogamut.base.agent.navigation.IPathFuture;
import cz.cuni.amis.pogamut.base.agent.navigation.impl.PrecomputedPathFuture;
import cz.cuni.amis.pogamut.base3d.worldview.object.ILocated;
import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.navmesh.BSPNode;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.navmesh.INavMeshAtom;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.navmesh.LevelGeometry;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.navmesh.NavMesh;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.navmesh.NavMeshCore;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.navmesh.NavMeshPolygon;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.navmesh.OffMeshEdge;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.navmesh.OffMeshPoint;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.DrawStayingDebugLines;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPointNeighbourLink;
import cz.cuni.amis.pogamut.ut2004.factory.guice.remoteagent.UT2004ServerFactory;
import cz.cuni.amis.pogamut.ut2004.factory.guice.remoteagent.UT2004ServerModule;
import cz.cuni.amis.pogamut.ut2004.server.impl.UT2004Server;
import cz.cuni.amis.pogamut.ut2004.utils.LinkFlag;
import cz.cuni.amis.pogamut.ut2004.utils.UT2004ServerRunner;
import java.io.*;
import java.util.*;
import javax.vecmath.Vector2d;
import math.geom2d.Point2D;
import math.geom2d.line.Line2D;
import math.geom2d.line.StraightLine2D;
import math.geom3d.Point3D;
import math.geom3d.Vector3D;
import math.geom3d.plane.Plane3D;

/**
 * Main class of Navigation Mesh module
 *
 * @author Jakub Tomek
 */
public class MyNavMesh extends NavMesh {

    private ArrayList<double[]> verts = new ArrayList<double[]>();
    private ArrayList<int[]> polys = new ArrayList<int[]>();
    private ArrayList<ArrayList<Integer>> vertsToPolys;
    private ArrayList<Boolean> safeVertex;
    private BSPNode bspTree;
    private BSPNode biggestLeafInTree;
    private ArrayList<OffMeshPoint> offMeshPoints;
    private ArrayList<ArrayList<OffMeshPoint>> polysToOffMeshPoints;
    private LevelGeometry levelGeometry;

    private UT2004Server server;

    public Random random;

    public MyNavMesh(boolean loadLevelGeometry) throws IOException {
        this(loadLevelGeometry, 0);
    }

    /**
     * Reads Navmesh from file - creates data structure
     */
    public MyNavMesh(boolean loadLevelGeometry, int port) throws FileNotFoundException, IOException {

        random = new Random();

        // 1. create internal server and read navmesh file
        UT2004ServerModule serverModule = new UT2004ServerModule();
        UT2004ServerFactory serverFactory = new UT2004ServerFactory(serverModule);
        UT2004ServerRunner serverRunner = null;
        if (port != 0) {
            System.out.println("Using passed port to initialize ServerRunner. Port: " + port);
            serverRunner = new UT2004ServerRunner(serverFactory, "UT2004Server", "localhost", port);
        } else {
            serverRunner = new UT2004ServerRunner(serverFactory);
        }

        this.server = (UT2004Server) serverRunner.startAgent();
        String mapName = server.getMapName();

        // 2. try to read all the data from already proccesed navmesh
        boolean dataRestored = false;
        System.out.println("Looking for previously saved data core...");
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(MyNavMeshConstants.processedMeshDir + "\\" + mapName + ".processed"));
            NavMeshCore core = (NavMeshCore) in.readObject();
            this.biggestLeafInTree = core.biggestLeafInTree;
            this.bspTree = core.bspTree;
            this.polys = core.polys;
            this.verts = core.verts;
            this.vertsToPolys = core.vertsToPolys;
            this.offMeshPoints = core.offMeshPoints;
            this.polysToOffMeshPoints = core.polysToOffMeshPoints;
            this.safeVertex = core.safeVertex;
            System.out.println("Data core has been restored. Great!");
            dataRestored = true;
        } catch (Exception e) {
            System.out.println("Previously saved data core could not be restored. New NavMesh will be processed. Exception message: " + e.getMessage());
        }

        if (!dataRestored) {
            // 3. read all vertices and polygons from file
            String fileName = MyNavMeshConstants.pureMeshReadDir + "\\" + mapName + ".navmesh";
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            String line;

            while ((line = br.readLine()) != null) {
                String[] toks = line.split("[ \\t]");

                if (toks[0].equals("v")) {
                    double[] v = new double[3];
                    v[0] = Double.parseDouble(toks[1]);
                    v[1] = Double.parseDouble(toks[2]);
                    v[2] = Double.parseDouble(toks[3]);
                    verts.add(v);
                }
                if (toks[0].equals("p")) {
                    int[] p = new int[toks.length - 1];
                    for (int i = 0; i < toks.length - 1; i++) {
                        p[i] = Integer.parseInt(toks[i + 1]);
                    }
                    polys.add(p);
                }
            }

            // 4. when vertices and polygons are done creating, we create an array mapping vertices to polygons
            resetVertsToPolys();

            // 5. mark safe and unsafe vertices
            resetSafeVerts();

            // 6. create a BSP tree structure
            resetBSPTree();

            // 7. get rid of unreachable polygons
            eliminateUnreachablePolygons();

            // 8. create off-mesh connections
            resetOffMeshConnections();

            // 9. save data core for next time
            System.out.println("Writing navmesh core to a file...");
            try {
                NavMeshCore core = new NavMeshCore();
                core.biggestLeafInTree = this.biggestLeafInTree;
                core.bspTree = this.bspTree;
                core.polys = this.polys;
                core.verts = this.verts;
                core.vertsToPolys = this.vertsToPolys;
                core.offMeshPoints = this.offMeshPoints;
                core.polysToOffMeshPoints = this.polysToOffMeshPoints;
                core.safeVertex = this.safeVertex;
                ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(MyNavMeshConstants.processedMeshDir + "\\" + mapName + ".processed"));
                out.writeObject(core);
                System.out.println("Writing navmesh to a file complete.");
            } catch (Exception e) {
                System.out.println("Exception during writing navmesh to a file: " + e.getMessage());
            }
        }

        // 10. get level geometry
        if (loadLevelGeometry) {
            // try to read it from processed file
            dataRestored = false;
            System.out.println("Looking for previously saved data level geom...");
            try {
                ObjectInputStream in = new ObjectInputStream(new FileInputStream(MyNavMeshConstants.processedLevelGeometryDir + "\\" + mapName + ".processed"));
                levelGeometry = (LevelGeometry) in.readObject();
                System.out.println("Level geom has been restored. Great!");
                dataRestored = true;
            } catch (Exception e) {
                System.out.println("Previously saved level geom could not be restored. Exception message: " + e.getMessage());
                e.printStackTrace();
            }

            if (!dataRestored) {
                System.out.println("Reading and building level geometry...");
                try {
                    levelGeometry = new LevelGeometry(mapName);
                    System.out.println("Level geometry read and built ok.");
                    // save levelgeom for next time
                    try {
                        System.out.println("Writing level geometry to a file...");
                        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(MyNavMeshConstants.processedLevelGeometryDir + "\\" + mapName + ".processed"));
                        out.writeObject(levelGeometry);
                        System.out.println("Level geometry written ok.");
                    } catch (Exception e) {
                        System.out.println("Exception during writing level geom to a file: " + e.getMessage());
                    }
                } catch (Exception e) {
                    System.out.println("Unable to get level geometry from file. Exception message: " + e.getMessage());
                    levelGeometry = null;
                }
            }
        }

    }

    /**
     * Overload for constructor
     */
    public MyNavMesh() throws FileNotFoundException, IOException {
        this(true);
    }

    /**
     * Returns all lines in one long string.
     */
    public String toDebugString() {
        StringBuilder result = new StringBuilder("");
        // projdeme vsechny polygony a vykreslime caru vzdy z vrcholu n do n+1
        for (int i = 0; i < polys.size(); i++) {
            int[] p = (int[]) polys.get(i);
            for (int j = 0; j < p.length; j++) {
                if (result.length() > 0) {
                    result.append(";");
                }
                // ziskame vrcholy v1 a v2 mezi kterymi vykreslime caru
                double[] v1, v2;
                v1 = (double[]) verts.get(p[j]);
                if (j == p.length - 1) {
                    v2 = (double[]) verts.get(p[0]);
                } else {
                    v2 = (double[]) verts.get(p[j + 1]);
                }
                // pridani cary
                result.append(v1[0] + "," + v1[1] + "," + v1[2] + ";" + v2[0] + "," + v2[1] + "," + v2[2]);
            }
        }
        return result.toString();
    }

    /**
     * Restricted alternative to toDebugString() - returns only one polygon as
     * string
     */
    public String polygonToDebugString(int polygonNumber) {
        StringBuilder result = new StringBuilder("");
        // projdeme vsechny polygony a vykreslime caru vzdy z vrcholu n do n+1
        int[] p = (int[]) polys.get(polygonNumber);
        for (int j = 0; j < p.length; j++) {
            if (result.length() > 0) {
                result.append(";");
            }
            // ziskame vrcholy v1 a v2 mezi kterymi vykreslime caru
            double[] v1, v2;
            v1 = (double[]) verts.get(p[j]);
            if (j == p.length - 1) {
                v2 = (double[]) verts.get(p[0]);
            } else {
                v2 = (double[]) verts.get(p[j + 1]);
            }
            // pridani cary
            result.append(v1[0] + "," + v1[1] + "," + v1[2] + ";" + v2[0] + "," + v2[1] + "," + v2[2]);
        }
        return result.toString();
    }

    /**
     * computes debug string of one off-mesh edge to be drawn.
     *
     * @param oe
     * @return
     */
    private String offMeshEdgeToDebugString(OffMeshEdge oe) {
        StringBuilder result = new StringBuilder("");
        Location l1 = oe.getFrom().getLocation();
        Location l2 = oe.getTo().getLocation();
        result.append(l1.x + "," + l1.y + "," + l1.z + ";" + l2.x + "," + l2.y + "," + l2.z);

        // add arrow at the end
        double[] vector = new double[3];
        vector[0] = l1.x - l2.x;
        vector[1] = l1.y - l2.y;
        vector[2] = l1.z - l2.z;
        // normalize the vector to small lenght
        double length = Math.sqrt(vector[0] * vector[0] + vector[1] * vector[1] + vector[2] * vector[2]);
        vector[0] *= 1 / length * 20;
        vector[1] *= 1 / length * 20;
        vector[2] *= 1 / length * 20;
        Point3D cross = new Point3D(l2.x + vector[0], l2.y + vector[1], l2.z + vector[2]);
        // to find edges of the arrow, we take 2D normal vector. And half size
        double[] vector2 = new double[2];
        vector2[0] = vector[1] / 2;
        vector2[1] = -vector[0] / 2;
        Point3D arrowPoint1 = new Point3D(cross.getX() + vector2[0], cross.getY() + vector2[1], cross.getZ());
        Point3D arrowPoint2 = new Point3D(cross.getX() - vector2[0], cross.getY() - vector2[1], cross.getZ());
        // we have all the points, now just connect them 
        result.append(";");
        result.append(arrowPoint1.getX() + "," + arrowPoint1.getY() + "," + arrowPoint1.getZ() + ";" + l2.x + "," + l2.y + "," + l2.z);
        result.append(";");
        result.append(arrowPoint2.getX() + "," + arrowPoint2.getY() + "," + arrowPoint2.getZ() + ";" + l2.x + "," + l2.y + "," + l2.z);
        result.append(";");
        result.append(arrowPoint1.getX() + "," + arrowPoint1.getY() + "," + arrowPoint1.getZ() + ";" + arrowPoint2.getX() + "," + arrowPoint2.getY() + "," + arrowPoint2.getZ());

        return result.toString();
    }

    /**
     * Cretaes a string of vector to be drawn from given path
     *
     * @param path
     * @return
     */
    private String pathToDebugString(IPathFuture<ILocated> path) {
        StringBuilder result = new StringBuilder("");
        // projdeme vsechny body a vykreslime caru vzdy z vrcholu n do n+1
        ILocated p0 = null;
        List<ILocated> pathList = path.get();
        for (ILocated p1 : pathList) {
            if (result.length() > 0) {
                result.append(";");
            }
            if (p0 != null) {
                result.append(Math.round(p0.getLocation().x) + "," + Math.round(p0.getLocation().y) + "," + Math.round(p0.getLocation().z) + ";" + Math.round(p1.getLocation().x) + "," + Math.round(p1.getLocation().y) + "," + Math.round(p1.getLocation().z));
            }
            p0 = p1;
        }
        return result.toString();
    }

    /**
     * Draws navmesh in game
     */
    public void draw() {
        // draw polygons
        for (int i = 0; i < this.polyCount(); i++) {
            DrawStayingDebugLines d = new DrawStayingDebugLines();
            String lines = this.polygonToDebugString(i);
            d.setVectors(lines);
            d.setColor(new Location(255, 255, 255));
            d.setClearAll(i == 0);
            server.getAct().act(d);
        }

        // draw off-mesh connections
        for (OffMeshPoint op : offMeshPoints) {
            // draw all outgoing edges
            for (OffMeshEdge oe : op.getOutgoingEdges()) {
                DrawStayingDebugLines d = new DrawStayingDebugLines();
                String lines = offMeshEdgeToDebugString(oe);
                d.setVectors(lines);
                d.setColor(MyNavMeshConstants.getColorForOffMeshConnection(oe, server));
                d.setClearAll(false);
                server.getAct().act(d);
            }
        }
    }

    /**
     * Draws given lines
     *
     * @param lines
     */
    public void draw(String lines, Location color) {
        DrawStayingDebugLines d = new DrawStayingDebugLines();
        d.setVectors(lines);
        d.setColor(color);
        d.setClearAll(false);
        server.getAct().act(d);
    }

    /**
     * Undraws all currently drawn lines
     */
    public void unDraw() {
        DrawStayingDebugLines d = new DrawStayingDebugLines();
        d.setClearAll(true);
        server.getAct().act(d);
    }

    public void drawLevelGeometry(Location color) {
        levelGeometry.draw(server, color);
    }

    /**
     * Draws one polygon with the color set in color
     */
    public void drawOnePolygon(int i, Location color) {
        DrawStayingDebugLines d = new DrawStayingDebugLines();
        String lines = this.polygonToDebugString(i);
        d.setVectors(lines);
        d.setColor(color);
        d.setClearAll(false);
        server.getAct().act(d);
    }

    /**
     * Draws one polygon with default color (yellow)
     */
    public void drawOnePolygon(int i) {
        drawOnePolygon(i, new Location(255, 255, 0));
    }

    /**
     * Draws one atom (polygon or point)
     *
     * @param atom
     * @param location
     */
    private void drawAtom(INavMeshAtom atom, Location location) {
        if (atom.getClass() == NavMeshPolygon1.class) {
            NavMeshPolygon1 p = (NavMeshPolygon1) atom;
            drawOnePolygon(p.getPolygonId(), location);
        }
    }

    /**
     * Draws entire list of polygons
     *
     * @param polygonPath
     */
    public void drawPolygonPath(List<INavMeshAtom> polygonPath, Location location) {
        for (INavMeshAtom atom : polygonPath) {
            drawAtom(atom, location);
        }
    }

    /**
     * Draws the given path
     *
     * @param path
     */
    public void drawPath(IPathFuture<ILocated> path, Location color) {

        // the commented code sometimes doesnt work for soem reason. maybe there is a corrupted point along the path?
        //String lines = pathToDebugString(path);
        //System.out.println("path to be drawn: " + lines);
        //draw(lines,color);
        List<ILocated> pathList = path.get();
        for (int i = 0; i < pathList.size() - 1; i++) {
            StringBuilder lines = new StringBuilder();
            lines.append(pathList.get(i).getLocation().x + ",");
            lines.append(pathList.get(i).getLocation().y + ",");
            lines.append(pathList.get(i).getLocation().z + ";");
            lines.append(pathList.get(i + 1).getLocation().x + ",");
            lines.append(pathList.get(i + 1).getLocation().y + ",");
            lines.append(pathList.get(i + 1).getLocation().z + "");
            draw(lines.toString(), color);
        }
    }

    /**
     * Debug method: Draws only the polygons in the biggeswt leaf so that we can
     * see whe they could not have been split any further
     */
    public void drawOnlyBiggestLeaf() {
        for (int i = 0; i < this.polyCount(); i++) {
            if (!biggestLeafInTree.polys.contains(i)) {
                continue;
            }

            DrawStayingDebugLines d = new DrawStayingDebugLines();
            String lines = this.polygonToDebugString(i);
            //System.out.println("polygon["+i+"] lines: " + lines);
            d.setVectors(lines);
            d.setColor(new Location(255, 255, 0));
            d.setClearAll(false);
            server.getAct().act(d);
        }
    }

    /**
     * Returns the number of polygons in navmesh
     */
    public int polyCount() {
        return polys.size();
    }

    /**
     * Returns the number of vertices in navmesh
     */
    public int vertCount() {
        return verts.size();
    }

    /**
     * gets a polygon by its order
     */
    public int[] getPolygon(int i) {
        int[] p = ((int[]) polys.get(i)).clone();
        return p;
    }

    /**
     * gets a vertex by its order
     */
    public double[] getVertex(int i) {
        double[] v = ((double[]) verts.get(i)).clone();
        return v;
    }

    /**
     * Builds the resetVertsToPolys mapping array
     */
    private void resetVertsToPolys() {
        System.out.println("Setting vertsToPolys mapping array...");
        vertsToPolys = new ArrayList();
        for (int i = 0; i < verts.size(); i++) {
            vertsToPolys.add(new ArrayList());
        }
        for (int i = 0; i < polys.size(); i++) {
            int[] p = (int[]) polys.get(i);
            for (int j = 0; j < p.length; j++) {
                ArrayList p2 = (ArrayList) vertsToPolys.get(p[j]);
                if (!p2.contains(i)) {
                    p2.add(i);
                }
            }
        }
    }

    /**
     * Resets the array of boolean values saying whether a vertex is at the edge
     * of navmesh
     */
    private void resetSafeVerts() {
        System.out.println("Setting safe vertices...");
        safeVertex = new ArrayList<Boolean>();
        int safeCount = 0;

        // check vertices one by one
        for (int v1 = 0; v1 < verts.size(); v1++) {
            System.out.println("Looking at vertex " + v1 + "...");

            // angles of polygons around must give 2 pi in sum
            double sum = 0;

            ArrayList<Integer> polys = vertsToPolys.get(v1);

            for (Integer pId : polys) {
                //System.out.println("Looking at polygon "+pId+"...");
                //this.drawOnePolygon(pId);
                int[] polygon = getPolygon(pId);
                // we must fint the three vertices creating angle around vertex i
                int v0 = -1, v2 = -1;
                for (int i = 0; i < polygon.length; i++) {
                    if (polygon[i] == v1) {
                        v0 = (i == 0 ? polygon[polygon.length - 1] : polygon[i - 1]);
                        v2 = (i == polygon.length - 1 ? polygon[0] : polygon[i + 1]);
                        break;
                    }
                }
                // three vertices found, now get the angle
                double[] vv0 = getVertex(v0);
                double[] vv1 = getVertex(v1);
                double[] vv2 = getVertex(v2);

                double a = Math.sqrt((vv1[0] - vv0[0]) * (vv1[0] - vv0[0]) + (vv1[1] - vv0[1]) * (vv1[1] - vv0[1]) + (vv1[2] - vv0[2]) * (vv1[2] - vv0[2]));
                double b = Math.sqrt((vv2[0] - vv1[0]) * (vv2[0] - vv1[0]) + (vv2[1] - vv1[1]) * (vv2[1] - vv1[1]) + (vv2[2] - vv1[2]) * (vv2[2] - vv1[2]));
                double c = Math.sqrt((vv2[0] - vv0[0]) * (vv2[0] - vv0[0]) + (vv2[1] - vv0[1]) * (vv2[1] - vv0[1]) + (vv2[2] - vv0[2]) * (vv2[2] - vv0[2]));

                double gama = Math.acos((a * a + b * b - c * c) / (2 * a * b));
                System.out.println("angle gama is " + gama);
                //this.drawOnePolygon(pId, new Location(255,255,255));
                sum += gama;
            }
            System.out.println("sum angle is " + sum);
            // we give it some tolerance for roundng errors
            if (sum >= 2 * 3.14) {
                safeVertex.add(v1, true);
                safeCount++;
            } else {
                safeVertex.add(v1, false);
            }
        }
        System.out.println("There are " + safeCount + " safe and " + (verts.size() + safeCount) + " unsafe vertices.");
    }

    /**
     * gets a list of polygons containing this vertex
     */
    public ArrayList getPolygonsByVertex(int i) {
        return (ArrayList) ((ArrayList) this.vertsToPolys.get(i)).clone();
    }

    /**
     * gets an array of polygon ids by an polygon id
     */
    public ArrayList getNeighbourIdsToPolygon(int i) {

        ArrayList neighbours = new ArrayList();
        int[] p = this.getPolygon(i);

        for (int j = 0; j < p.length; j++) {
            ArrayList<Integer> p2 = getPolygonsByVertex(p[j]);
            for (int k = 0; k < p2.size(); k++) {
                int candidateId = p2.get(k);
                //this polygon shares one vertex with the input polygon, but that is not enough. neighbour must share two.
                // p[j] is one vertex
                int secondVertex = p[((j == p.length - 1) ? 0 : j + 1)];
                int[] candidatePolygon = this.getPolygon(candidateId);
                for (int l = 0; l < candidatePolygon.length; l++) {
                    if (candidatePolygon[l] == secondVertex) {
                        // its him! ok! shares 2 tertices
                        if (!neighbours.contains(candidateId) && candidateId != i) {
                            neighbours.add(candidateId);
                        }
                        break;
                    }
                }

            }
        }
        return neighbours;
    }

    /**
     * Returns a List of offmeshpoints that are located on target polygon
     *
     * @param pId
     * @return
     */
    public List<OffMeshPoint> getOffMeshPoinsOnPolygon(int pId) {
        return polysToOffMeshPoints.get(pId);
    }

    /**
     * Gets a new BSPTree for this mesh
     */
    private void resetBSPTree() {
        System.out.println("Creating BSP tree...");
        bspTree = new BSPNode(this, null);
        for (int i = 0; i < polys.size(); i++) {
            bspTree.polys.add(i);
        }
        biggestLeafInTree = null;
        bspTree.build();
        System.out.println("BSP tree is done building.");
        System.out.println("Biggest leaf has " + biggestLeafInTree.polys.size() + " polygons.");
    }

    /**
     * Debug method: helps to describe BSP tree by telling the number of
     * polygons in the biggest leaf (this should not bee too big. 5 is a good
     * number.)
     */
    public int getNumberOfPolygonsInBiggestLeaf() {
        if (biggestLeafInTree != null) {
            return biggestLeafInTree.polys.size();
        } else {
            return -1;
        }
    }

    /**
     * Debug method: sets the biggest leaf so it can be easily found.
     */
    public void setBiggestLeafInTree(BSPNode node) {
        biggestLeafInTree = node;
    }

    /**
     *
     * @param point3D
     * @return id of polygon on which is this point
     */
    public int getPolygonIdByPoint3D(Point3D point3D) {
        //System.out.println("trying to fing polygon for location [" + point3D.getX() + ", " + point3D.getY() + ", " + point3D.getZ() + "]");
        // 2D projection of point
        Point2D point2D = new Point2D(point3D.getX(), point3D.getY());
        // walk through BSP tree
        BSPNode node = bspTree;
        while (!node.isLeaf()) {
            //System.out.println("Searching an inner node. There are " +node.polys.size()+ " polygons and their numbers are: " + node.polys.toString());            
            StraightLine2D sepLine = node.sepLine;
            double dist = sepLine.getSignedDistance(point2D);
            if (dist < 0) {
                //System.out.println("go to left child");
                node = node.left;
            }
            if (dist > 0) {
                //System.out.println("go to right child");
                node = node.right;
            }
            if (dist == 0) {
                //System.out.println("Wow, the location is exactly on the border. Let's move a little");
                point2D = new Point2D(point3D.getX() + random.nextDouble() - 0.5, point3D.getY() + random.nextDouble() - 0.5);
            }
        }
        // now we are in leaf so, we should see the list of possible polygons
        //System.out.println("The leaf is found. There are " +node.polys.size()+ " polygons and their numbers are: " + node.polys.toString());

        // now we must choose which polygon is really the one we are staying at, if any
        ArrayList candidatePolygons = new ArrayList();

        // are we staying inside in 2D projection? if not, reject this polygon
        for (int i = 0; i < node.polys.size(); i++) {
            Integer pId = (Integer) node.polys.get(i);

            if (polygonContainsPoint(pId, point2D)) {
                //System.out.println("Polygon " +pId+ " contains this location.");
                candidatePolygons.add(pId);
            } else {
                //System.out.println("Polygon " +pId+ " does not contain this location.");
            }
        }

        // now we have the candidtate polygons. the agent is inside each of them in a 2D projection
        // now we select the one that is closest on z coordinate and return it   
        //System.out.println("candidatePolygons: " + candidatePolygons.toString());
        if (candidatePolygons.isEmpty()) {
            return -1;
        }

        // spocitame vzdalenost od vrcholu 0 na z souradnici. nejmensi vzdalenost = vitez
        double minDist = MyNavMeshConstants.maxDistanceBotPolygon;
        int retPId = -2;
        for (int i = 0; i < candidatePolygons.size(); i++) {
            Integer pId = (Integer) candidatePolygons.get(i);
            int[] polygon = getPolygon(pId);

            // we take first three points, create plane, calculate distance
            double[] v1 = getVertex(polygon[0]);
            double[] v2 = getVertex(polygon[1]);
            double[] v3 = getVertex(polygon[2]);
            Point3D p1 = new Point3D(v1[0], v1[1], v1[2]);
            Vector3D vector1 = new Vector3D(v2[0] - v1[0], v2[1] - v1[1], v2[2] - v1[2]);
            Vector3D vector2 = new Vector3D(v3[0] - v1[0], v3[1] - v1[1], v3[2] - v1[2]);
            Plane3D plane = new Plane3D(p1, vector1, vector2);
            double dist = plane.getDistance(point3D);
            if (dist < minDist) {
                retPId = pId;
                minDist = dist;
            }

            // watch out for rounding errors!
            // if those three points are almost in line rounding error could become huge
            // better try more points
            if (polygon.length > 3) {
                v1 = getVertex(polygon[0]);
                v2 = getVertex(polygon[1]);
                v3 = getVertex(polygon[3]);
                p1 = new Point3D(v1[0], v1[1], v1[2]);
                vector1 = new Vector3D(v2[0] - v1[0], v2[1] - v1[1], v2[2] - v1[2]);
                vector2 = new Vector3D(v3[0] - v1[0], v3[1] - v1[1], v3[2] - v1[2]);
                plane = new Plane3D(p1, vector1, vector2);
                dist = plane.getDistance(point3D);
                if (dist < minDist) {
                    retPId = pId;
                    minDist = dist;
                }
            }
            // or even more
            if (polygon.length > 4) {
                v1 = getVertex(polygon[0]);
                v2 = getVertex(polygon[2]);
                v3 = getVertex(polygon[4]);
                p1 = new Point3D(v1[0], v1[1], v1[2]);
                vector1 = new Vector3D(v2[0] - v1[0], v2[1] - v1[1], v2[2] - v1[2]);
                vector2 = new Vector3D(v3[0] - v1[0], v3[1] - v1[1], v3[2] - v1[2]);
                plane = new Plane3D(p1, vector1, vector2);
                dist = plane.getDistance(point3D);
                if (dist < minDist) {
                    retPId = pId;
                    minDist = dist;
                }
            }
        }
        System.out.println("distance of a point from polygon " + retPId + " is " + minDist);
        return retPId;
    }

    /**
     * Gets the id of a polygon that contains this location
     *
     * @param location
     * @return id of polygon or value < 0 if the is no such polygon
     */
    public int getPolygonIdByLocation(Location location) {
        return getPolygonIdByPoint3D(new Point3D(location.x, location.y, location.z));
    }

    /**
     * Decides whether the input point is inside of the polygon of navmesh
     * identified by its id.
     *
     * @param pId
     * @param point2D
     * @return
     */
    private boolean polygonContainsPoint(Integer pId, Point2D point2D) {
        boolean result = true;
        double rightSide = 0.0;

        int[] polygon = getPolygon(pId);
        double[] v1, v2;
        for (int i = 0; i < polygon.length; i++) {
            v1 = getVertex(polygon[i]);
            if (i < polygon.length - 1) {
                v2 = getVertex(polygon[i + 1]);
            } else {
                v2 = getVertex(polygon[0]);
            }
            Point2D p1 = new Point2D(v1[0], v1[1]);
            Point2D p2 = new Point2D(v2[0], v2[1]);
            StraightLine2D line = new StraightLine2D(p1, p2);
            double dist = line.getSignedDistance(point2D);

            if (rightSide == 0.0) {
                rightSide = Math.signum(dist);
            } else {
                if (rightSide * dist < 0) {
                    result = false;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Some polygons cannot be reached we find them with the hhelp of navigation
     * graph Definition: 1. Any polygon with navigation point is reachable 2.
     * Any polygon sharing edge with a reachbale polygon is also reachable.
     */
    private void eliminateUnreachablePolygons() {
        System.out.println("eliminateUnreachablePolygons() starts...");

        // 1. get list of all navpoints
        Map<UnrealId, NavPoint> navPoints = (Map<UnrealId, NavPoint>) (Map) server.getWorldView().getAll(NavPoint.class);

        // which polygons are reachbale and which are not?
        boolean[] reachable = new boolean[polys.size()];
        // 2. walk through all navpoints and mark all reachable polygons
        System.out.println("marking reachable polygons...");
        for (NavPoint navPoint : navPoints.values()) {
            Point3D point3D = navPoint.getLocation().asPoint3D();
            int pId = getPolygonIdByPoint3D(point3D);
            if (pId < 0) {
                continue;
            }
            markAsReachableRecursive(pId, reachable);
        }
        // debugging control: how many polygons are unreachable?
        int reachableCount = 0;
        int polyDelCount = 0;
        int vertDelCount = 0;
        for (int i = 0; i < polys.size(); i++) {
            if (reachable[i]) {
                reachableCount++;
            }
        }
        System.out.println("Marking complete. There are " + reachableCount + " reachable polygons and " + (polys.size() - reachableCount) + " unreachable polygons.");
        System.out.println("Deleting unreachable polygons...");
        for (int i = polys.size() - 1; i >= 0; i--) {
            if (!reachable[i]) {
                polys.remove(i);
                polyDelCount++;
            }
        }
        System.out.println("Reseting vertsToPolys...");
        resetVertsToPolys();
        System.out.println("Deleting unused vertices...");
        for (int i = vertsToPolys.size() - 1; i >= 0; i--) {
            ArrayList polygons = (ArrayList) vertsToPolys.get(i);
            if (polygons.isEmpty()) {
                verts.remove(i);
                vertDelCount++;
                // after removing a vertex (and moving all following vertices by -1) we must recalculate the vertices in polygons
                for (int j = 0; j < polys.size(); j++) {
                    int[] polygon = (int[]) polys.get(j);
                    for (int k = 0; k < polygon.length; k++) {
                        if (polygon[k] > i) {
                            polygon[k]--;
                        }
                    }
                }
            }
        }
        System.out.println("Deleting done. " + polyDelCount + " Polygons and " + vertDelCount + " vertices were deleted.");

        // we have changed the polygon a and vertex numbers, therefore we must reset BSPTree and vertsToPolys mapping array
        resetVertsToPolys();
        resetSafeVerts();
        resetBSPTree();
    }

    /**
     * Helping function used only in method eliminateUnreachablePolygons
     *
     * @param pId
     * @param reachable
     */
    private void markAsReachableRecursive(int pId, boolean[] reachable) {
        if (reachable[pId]) {
            return;
        }
        reachable[pId] = true;
        ArrayList neighbours = getNeighbourIdsToPolygon(pId);
        for (int i = 0; i < neighbours.size(); i++) {
            markAsReachableRecursive((Integer) neighbours.get(i), reachable);
        }
    }

    /**
     * Creates off-mesh connections between polygons that does not share an
     * edge, but there is a connection from one to the other in navigation
     * graph. The endpoints are not necessarily polygons. Thay also may be
     * off-mesh navpoints. This method also creates list of off-mesh points and
     * also creates mappings between polygons and these points
     */
    private void resetOffMeshConnections() {
        System.out.println("Creating off-mesh connections...");

        Map<UnrealId, OffMeshPoint> offPoints = new HashMap<UnrealId, OffMeshPoint>();

        // 1. get list of all navpoints
        Map<UnrealId, NavPoint> navPoints = (Map<UnrealId, NavPoint>) (Map) server.getWorldView().getAll(NavPoint.class);

        // offPoint definition: it has more than 0 offMeshEdges (outgoing or incoming)
        // 1. we act like if every navpoint was an offpoint and calculate all outgoing & incoming offedges
        // 2. any point with any (outgoing or incoming) out edges will be added to list of navmesh
        // for each navpoint create offpoint
        for (Map.Entry<UnrealId, NavPoint> entry : navPoints.entrySet()) {
            NavPoint np = entry.getValue();
            UnrealId uId = entry.getKey();
            int pId = this.getPolygonIdByPoint3D(np.getLocation().asPoint3D());
            OffMeshPoint op = new OffMeshPoint(np, pId);
            offPoints.put(uId, op);
        }

        // now again - for each navpoint check his outgoing edges
        // if we find an offedge, we add it to both start and target
        for (Map.Entry<UnrealId, NavPoint> entry : navPoints.entrySet()) {
            NavPoint np = entry.getValue();
            UnrealId uId = entry.getKey();

            // check all outgoing edges
            for (Map.Entry<UnrealId, NavPointNeighbourLink> entry2 : np.getOutgoingEdges().entrySet()) {
                NavPointNeighbourLink link = entry2.getValue();
                UnrealId uId2 = entry2.getKey();

                NavPoint target = link.getToNavPoint();

                System.out.println("Checking edge from navpoint " + uId + " to navpoint " + target.getId() + "...");

                // Flags. Important thing. Why?
                // Some edges will be considered as off-mesh immidietely, without checking mesh (lift)
                // Some edges will be ignored immidietely (ladder, swimming, etc.)
                // maybe put this code block into separate method/class/NavMeshConsts?
                boolean forceIgnore = false;
                boolean forceAdd = false;
                boolean addThisEdge = false;
                // point flags
                if (np.isLiftCenter()) {
                    forceAdd = true;
                }
                if (target.isLiftCenter()) {
                    forceAdd = true;
                }
                // edge flags
                int linkFlags = link.getFlags();
                if ((linkFlags & LinkFlag.DOOR.get()) > 0) {
                }
                if ((linkFlags & LinkFlag.FLY.get()) > 0) {
                    forceIgnore = true;
                }
                if ((linkFlags & LinkFlag.FORCED.get()) > 0) {
                }
                if ((linkFlags & LinkFlag.JUMP.get()) > 0) {
                }
                if ((linkFlags & LinkFlag.LADDER.get()) > 0) {
                    forceIgnore = true;
                }
                if ((linkFlags & LinkFlag.PLAYERONLY.get()) > 0) {
                }
                if ((linkFlags & LinkFlag.PROSCRIBED.get()) > 0) {
                    forceIgnore = true;
                }
                if ((linkFlags & LinkFlag.SPECIAL.get()) > 0) {
                }
                if ((linkFlags & LinkFlag.SWIM.get()) > 0) {
                    forceIgnore = true;
                }
                if ((linkFlags & LinkFlag.WALK.get()) > 0) {
                }

                if (!forceAdd && !forceIgnore) {

                    // 2D projection of link
                    Line2D linkAsLine2D = new Line2D(link.getFromNavPoint().getLocation().x, link.getFromNavPoint().getLocation().y, link.getToNavPoint().getLocation().x, link.getToNavPoint().getLocation().y);

                    // how to decide if edge is offmesh?
                    // 1. start on the polygon of starting navpoinpoint (no polygon = offmesh)
                    // 2. while the current polygon (no polygon = offmesh) is not polygon of target repeat:
                    // 3. go to the neighbour polygon that is behind the edge that is crossed by our line                 
                    int currentPolygonId = getPolygonIdByPoint3D(link.getFromNavPoint().getLocation().asPoint3D());
                    int targetPolygonId = this.getPolygonIdByPoint3D(link.getToNavPoint().getLocation().asPoint3D());
                    int tabooPolygon = -1; // we are not searching backwards
                    while (currentPolygonId >= 0 && currentPolygonId != targetPolygonId) {
                        int newPolygon = -1;

                        // try all neighbours (except last one)
                        List<Integer> neighbours = this.getNeighbourIdsToPolygon(currentPolygonId);
                        for (Integer neighbour : neighbours) {
                            if (neighbour.intValue() == tabooPolygon) {
                                continue;
                            }

                            // find the shared edge
                            Line2D sharedEdge = null;
                            int[] polygon1 = getPolygon(currentPolygonId);
                            int[] polygon2 = getPolygon(neighbour);
                            for (int i = 0; i < polygon1.length; i++) {
                                int v1 = polygon1[i];
                                int v2 = polygon1[((i == polygon1.length - 1) ? 0 : i + 1)];
                                // polygon2 must contain both vertices
                                boolean containsV1 = false, containsV2 = false;
                                for (int j = 0; j < polygon2.length; j++) {
                                    if (polygon2[j] == v1) {
                                        containsV1 = true;
                                    }
                                    if (polygon2[j] == v2) {
                                        containsV2 = true;
                                    }
                                }
                                if (containsV1 && containsV2) {
                                    double[] vertex1 = this.getVertex(v1);
                                    double[] vertex2 = this.getVertex(v2);
                                    sharedEdge = new Line2D(vertex1[0], vertex1[1], vertex2[0], vertex2[1]);
                                }
                            }
                            // now we should have the shared edge or there is an error
                            if (sharedEdge == null) {
                                System.out.println("Error! shared edge between polygon " + currentPolygonId + " and " + neighbour + " was not found!");
                            }

                            // does our examined edge cross the shared edge?
                            if (linkAsLine2D.getIntersection(sharedEdge) != null) {
                                System.out.println("Crossed a line into polygon " + neighbour);
                                tabooPolygon = currentPolygonId;
                                newPolygon = neighbour;
                                break;
                            }
                        }
                        currentPolygonId = newPolygon;
                    }
                    // so now we either reached the target over the polygons, or we are off the mesh
                    // which one is it?
                    if (currentPolygonId > 0) {
                        // path is inside navmesh
                        addThisEdge = false;
                    } else {
                        // path is off mesh
                        addThisEdge = true;
                    }
                } // end of checking path
                // else: we were forced to add/reject this edge
                else {
                    // ignoring has higher priority
                    if (forceAdd) {
                        addThisEdge = true;
                    }
                    if (forceIgnore) {
                        addThisEdge = false;
                    }
                }

                // will we add this edge?
                if (addThisEdge) {
                    System.out.println("This edge is off-mesh!");
                    OffMeshPoint op1 = offPoints.get(uId);
                    OffMeshPoint op2 = offPoints.get(target.getId());
                    OffMeshEdge oe = new OffMeshEdge(op1, op2, link);
                    op1.getOutgoingEdges().add(oe);
                    op2.getIncomingEdges().add(oe);
                } else {
                    System.out.println("This edge is not off-mesh.");
                }
            }
        }

        // all edges from all navpoints are checked. now lets see how many off-mesh points do we have
        offMeshPoints = new ArrayList<OffMeshPoint>();
        int offCount = 0;
        for (OffMeshPoint op : offPoints.values()) {
            if (op.getOutgoingEdges().isEmpty() && op.getIncomingEdges().isEmpty()) {
                // nothing
            } else {
                offMeshPoints.add(op);
                offCount++;
            }
        }
        System.out.println("We found " + offCount + " offMeshPoints in total of " + offPoints.size() + " NavPoints.");

        // create mapping from polygons to offmesh points
        polysToOffMeshPoints = new ArrayList<ArrayList<OffMeshPoint>>();
        for (int i = 0; i < polys.size(); i++) {
            polysToOffMeshPoints.add(new ArrayList<OffMeshPoint>());
        }
        for (OffMeshPoint op : offMeshPoints) {
            int pId = op.getPId();
            if (pId >= 0) {
                polysToOffMeshPoints.get(pId).add(op);
            }
        }

        System.out.println("Off-mesh connections done.");
    }

    /**
     * Gets a List of polygons on which the path should go.
     *
     * @param from
     * @param to
     * @return
     */
    public List<INavMeshAtom> getPolygonPath(INavMeshAtom fromAtom, INavMeshAtom toAtom) {
        // List of atoms from which we will always pick the one with shortest distance and expand ir
        List<AStarNode1> pickable = new ArrayList<AStarNode1>();
        // List of atoms, that are no longer pickable, because they have no more neighbours
        List<AStarNode1> expanded = new ArrayList<AStarNode1>();
        pickable.add(new AStarNode1(null, fromAtom, this, fromAtom, toAtom));

        // Let's search for toAtom!
        AStarNode1 targetNode = null;
        while (targetNode == null) {

            // 1. if pickable is empty, there is no way
            if (pickable.isEmpty()) {
                return null;
            }

            // 2. find the most perspective node in pickable
            // that means that it has the shortest estimated total path length;
            AStarNode1 best = pickable.get(0);
            for (AStarNode1 node : pickable) {
                if (node.getEstimatedTotalDistance() < best.getEstimatedTotalDistance()) {
                    best = node;
                }
            }

            // 3. we expand the best node
            List<INavMeshAtom> neighbours = best.getAtom().getNeighbours(this);
            for (INavMeshAtom atom : neighbours) {
                boolean add = true;
                // if this atom is already in our expanded tree, we reject it?
                // TODO some optimalization for teleports
                for (AStarNode1 expNode : expanded) {
                    if (expNode.getAtom().equals(atom)) {
                        add = false;
                    }
                }
                // we add new neighbour
                if (add) {
                    AStarNode1 newNode = new AStarNode1(best, atom, this, fromAtom, toAtom);
                    best.getFollowers().add(newNode);
                    pickable.add(newNode);
                    // target reach test
                    if (atom.equals(toAtom)) {
                        targetNode = newNode;
                    }
                }
            }
            // put expadned node into expanded
            pickable.remove(best);
            expanded.add(best);
        }

        // now we just return the path of atoms from start to end. We must build it from the end
        List<INavMeshAtom> path = new ArrayList<INavMeshAtom>();
        AStarNode1 node = targetNode;
        while (node != null) {
            path.add(node.getAtom());
            node = node.getFrom();
        }
        Collections.reverse(path);
        return path;
    }

    /**
     * Calls the method with the same name but polygons as arguments and returns
     * result
     *
     * @param from
     * @param to
     * @return
     */
    public List<INavMeshAtom> getPolygonPath(Location from, Location to) {
        INavMeshAtom fromAtom = getNearestAtom(from);
        INavMeshAtom toAtom = getNearestAtom(to);
        return getPolygonPath(fromAtom, toAtom);
    }

    /**
     * Counts a simple path from polygonPath. Only for testing use. rules for
     * adding points: for each new atom in path:
     *
     * 1. point -> point : just add the new point! :-) 2. point -> polygon : is
     * that point inside that polygon? a) yes : add nothing b) no : this should
     * not happen. offmesh points are connected to points inside. Add the
     * center... 3. polygon -> polygon : add point that is in middle of shared
     * line 4. polygon -> point : is that point inside that polygon? a) yes :
     * add the new point b) no : this should not happen. offmesh points are
     * connected to points inside. Add the new point anyway
     *
     * @param from
     * @param to
     * @param polygonPath
     * @return
     */
    private List<ILocated> getPolygonCentrePath(ILocated from, ILocated to, List<INavMeshAtom> polygonPath) {
        List path = new ArrayList<ILocated>();
        path.add(from);
        INavMeshAtom lastAtom = null;
        for (INavMeshAtom atom : polygonPath) {
            // * -> point
            if (atom.getClass() == OffMeshPoint.class) {
                NavPoint np = (NavPoint) server.getWorldView().get(((OffMeshPoint) atom).getNavPointId());
                path.add(np);
            } else {
                // point -> polygon
                NavMeshPolygon1 polygon = (NavMeshPolygon1) atom;

                if (lastAtom == null || lastAtom.getClass() == OffMeshPoint.class) {
                    OffMeshPoint op = (OffMeshPoint) lastAtom;

                    // was lastAtom inside this polygon?
                    boolean inside;
                    if (lastAtom == null) {
                        inside = polygonContainsPoint(polygon.getPolygonId(), new Point2D(from.getLocation().x, from.getLocation().y));
                    } else {
                        inside = false;
                        List<OffMeshPoint> offPs = polysToOffMeshPoints.get(polygon.getPolygonId());
                        for (OffMeshPoint op2 : offPs) {
                            if (op2.equals(op)) {
                                inside = true;
                            }
                        }
                    }

                    if (inside) {
                        // nothing
                    } else {
                        // add center
                        path.add(getLocation(atom));
                    }
                } // polygon -> polygon
                else {
                    // we must find the two shared points
                    NavMeshPolygon1 polygon2 = (NavMeshPolygon1) lastAtom;
                    int[] p1 = this.getPolygon(polygon.getPolygonId());
                    int[] p2 = this.getPolygon(polygon2.getPolygonId());
                    int v1 = -1;
                    int v2 = -1;
                    outer:
                    for (int i = 0; i < p1.length; i++) {
                        for (int j = 0; j < p2.length; j++) {
                            if (p1[i] == p2[j]) {
                                if (v1 == -1) {
                                    v1 = p1[i];
                                } else {
                                    if (p1[i] != v1) {
                                        v2 = p1[i];
                                    }
                                    break outer;
                                }
                            }
                        }
                    }
                    double[] vv1 = getVertex(v1);
                    double[] vv2 = getVertex(v2);
                    path.add(new Location((vv1[0] + vv2[0]) / 2, (vv1[1] + vv2[1]) / 2, (vv1[2] + vv2[2]) / 2 + MyNavMeshConstants.liftPolygonLocation));
                }
            }
            lastAtom = atom;
        }
        path.add(to);
        return path;
    }

    /**
     * Computes shortest funneled path between given points from and to
     *
     * @param from
     * @param to
     * @param polygonPath
     * @return
     */
    private List<ILocated> getFunneledPath(ILocated from, ILocated to, List<INavMeshAtom> polygonPath) {
        List<ILocated> path = new ArrayList<ILocated>();
        path.add(from);
        INavMeshAtom lastAtom = null;
        INavMeshAtom atom = null;
        int index = -1;
        while (index < polygonPath.size() - 1) {
            index++;
            if (index > 0) {
                lastAtom = polygonPath.get(index - 1);
            } else {
                lastAtom = null;
            }
            atom = polygonPath.get(index);

            // * -> point
            if (atom.getClass() == OffMeshPoint.class) {
                NavPoint np = (NavPoint) server.getWorldView().get(((OffMeshPoint) atom).getNavPointId());
                path.add(np);
            } // * -> polygon
            else {

                NavMeshPolygon1 polygon = (NavMeshPolygon1) atom;
                // point -> polygon
                if (lastAtom == null || lastAtom.getClass() == OffMeshPoint.class) {
                    OffMeshPoint op = (OffMeshPoint) lastAtom;

                    // was lastAtom inside this polygon?
                    boolean inside;
                    if (lastAtom == null) {
                        inside = polygonContainsPoint(polygon.getPolygonId(), new Point2D(from.getLocation().x, from.getLocation().y));
                    } else {
                        inside = false;
                        List<OffMeshPoint> offPs = polysToOffMeshPoints.get(polygon.getPolygonId());
                        for (OffMeshPoint op2 : offPs) {
                            if (op2.equals(op)) {
                                inside = true;
                            }
                        }
                    }

                    if (inside) {
                        // nothing
                    } else {
                        // add center
                        path.add(getLocation(atom));
                    }
                } // polygon -> polygon
                else {
                    // here comes the funneling

                    // point from which we are starting = last point in path
                    ILocated start = path.get(path.size() - 1);

                    // we must find the 'gateway'
                    NavMeshPolygon1 polygon2 = (NavMeshPolygon1) lastAtom;
                    int[] p1 = this.getPolygon(polygon.getPolygonId());
                    int[] p2 = this.getPolygon(polygon2.getPolygonId());
                    int v1 = -1;
                    int v2 = -1;
                    outer:
                    for (int i = 0; i < p1.length; i++) {
                        for (int j = 0; j < p2.length; j++) {
                            if (p1[i] == p2[j]) {
                                if (v1 == -1) {
                                    v1 = p1[i];
                                } else {
                                    if (p1[i] != v1) {
                                        v2 = p1[i];
                                    }
                                    break outer;
                                }
                            }
                        }
                    }
                    double[] vv1 = getVertex(v1);
                    double[] vv2 = getVertex(v2);
                    Line2D gateway = new Line2D(vv1[0], vv1[1], vv2[0], vv2[1]);
                    // gateway found

                    if ((start.getLocation().x == vv2[0] && start.getLocation().y == vv2[1])
                            || (start.getLocation().x == vv1[0] && start.getLocation().y == vv1[1])) {
                        System.out.println("We are already in the next polygon. No comparation, let's just continue.");
                        continue;
                    }

                    // !!! recgonize left and right correctly !!!
                    double dist = gateway.getSignedDistance(start.getLocation().x, start.getLocation().y);
                    // create left and right mantinel

                    Line2D leftMantinel = new Line2D(start.getLocation().x, start.getLocation().y, vv2[0], vv2[1]);
                    Line2D rightMantinel = new Line2D(start.getLocation().x, start.getLocation().y, vv1[0], vv1[1]);
                    if (dist < 0) {
                        Line2D swap = leftMantinel;
                        leftMantinel = rightMantinel;
                        rightMantinel = swap;
                        vv1 = getVertex(v2);
                        vv2 = getVertex(v1);
                        gateway = new Line2D(vv1[0], vv1[1], vv2[0], vv2[1]);
                    }
                    // now left and right mantinel are set correctly                                        

                    int leftMantinelIndex = index;
                    double leftMantinelZ = vv2[2];
                    Location leftMantinelTarget;
                    if (safeVertex.get(v2)) {
                        leftMantinelTarget = new Location(vv2[0], vv2[1], vv2[2] + MyNavMeshConstants.liftPolygonLocation);
                    } else {
                        if (gateway.getLength() <= 2 * MyNavMeshConstants.agentRadius) {
                            leftMantinelTarget = new Location((vv2[0] + vv1[0]) / 2, (vv2[1] + vv1[1]) / 2, (vv2[2] + vv1[2]) / 2 + MyNavMeshConstants.liftPolygonLocation);
                        } else {
                            leftMantinelTarget = new Location(vv2[0] + (vv1[0] - vv2[0]) / gateway.getLength() * MyNavMeshConstants.agentRadius,
                                    vv2[1] + (vv1[1] - vv2[1]) / gateway.getLength() * MyNavMeshConstants.agentRadius,
                                    vv2[2] + (vv1[2] - vv2[2]) / gateway.getLength() * MyNavMeshConstants.agentRadius + MyNavMeshConstants.liftPolygonLocation);
                        }
                    }

                    int rightMantinelIndex = index;
                    double rightMantinelZ = vv1[2];
                    Location rightMantinelTarget;
                    if (safeVertex.get(v1)) {
                        rightMantinelTarget = new Location(vv1[0], vv1[1], vv1[2] + MyNavMeshConstants.liftPolygonLocation);
                    } else {
                        if (gateway.getLength() <= 2 * MyNavMeshConstants.agentRadius) {
                            rightMantinelTarget = new Location((vv2[0] + vv1[0]) / 2, (vv2[1] + vv1[1]) / 2, (vv2[2] + vv1[2]) / 2 + MyNavMeshConstants.liftPolygonLocation);
                        } else {
                            rightMantinelTarget = new Location(vv1[0] + (vv2[0] - vv1[0]) / gateway.getLength() * MyNavMeshConstants.agentRadius,
                                    vv1[1] + (vv2[1] - vv1[1]) / gateway.getLength() * MyNavMeshConstants.agentRadius,
                                    vv1[2] + (vv2[2] - vv1[2]) / gateway.getLength() * MyNavMeshConstants.agentRadius + MyNavMeshConstants.liftPolygonLocation);
                        }
                    }

                    // now we will go further over the polygons until the mantinels cross or until we find target point inside funnel
                    boolean targetAdded = false;
                    boolean outOfMantinels = false;
                    boolean endOfPolygonPathReached = false;
                    while (!targetAdded && !outOfMantinels) {
                        index++;
                        lastAtom = polygonPath.get(index - 1);
                        if (index < polygonPath.size()) {
                            atom = polygonPath.get(index);
                        } // if we are at the end, we dont need an atom
                        else {
                            endOfPolygonPathReached = true;
                        }
                        // last atom surely was polygon because we are in polygon->polygon branch
                        polygon2 = (NavMeshPolygon1) lastAtom;

                        // new atom is point - potential end of algorithm
                        // also go this way if we reached the last polygon
                        // every command has an alternative for this special option
                        if (endOfPolygonPathReached || atom.getClass() == OffMeshPoint.class) {
                            NavPoint np = null;
                            if (!endOfPolygonPathReached) {
                                np = (NavPoint) server.getWorldView().get(((OffMeshPoint) atom).getNavPointId());
                            }

                            ILocated target;
                            if (endOfPolygonPathReached) {
                                target = to;
                            } else {
                                target = np;
                            }

                            // is np inside funnel?
                            // compare with left mantinel:
                            Line2D virtualGateway1 = new Line2D(target.getLocation().x, target.getLocation().y, leftMantinel.p2.x, leftMantinel.p2.y);
                            dist = virtualGateway1.getSignedDistance(start.getLocation().x, start.getLocation().y);
                            if (dist < 0) {
                                // point is out from left mantinel. we must go to corner of left mantinel and continue from there
                                //path.add(new Location(leftMantinel.p2.x, leftMantinel.p2.y, leftMantinelZ+MyNavMeshConstants.liftPolygonLocation));
                                path.add(leftMantinelTarget);
                                // we will now 'restart' funneling algorithm - continue from this point and its polygon 
                                outOfMantinels = true;
                                index = leftMantinelIndex;
                            } else {
                                // point is inside left mantinel - Ok
                                // check the right mantinel
                                Line2D virtualGateway2 = new Line2D(rightMantinel.p2.x, rightMantinel.p2.y, target.getLocation().x, target.getLocation().y);
                                dist = virtualGateway2.getSignedDistance(start.getLocation().x, start.getLocation().y);
                                if (dist < 0) {
                                    // point is out from right mantinel. we must go to corner of right mantinel and continue from there
                                    //path.add(new Location(rightMantinel.p2.x, rightMantinel.p2.y, rightMantinelZ+MyNavMeshConstants.liftPolygonLocation));
                                    path.add(rightMantinelTarget);
                                    // we will now 'restart' funneling algorithm - continue from this point and its polygon 
                                    outOfMantinels = true;
                                    index = leftMantinelIndex;
                                } else {
                                    // point is inside the maninels. that is great - we successfully finnished this funnelling
                                    if (!endOfPolygonPathReached) {
                                        path.add(np);
                                    }
                                    targetAdded = true;
                                }
                            }
                        } // new atom is polygon again
                        else {
                            polygon = (NavMeshPolygon1) atom;
                            Point2D middleOfOldGateway = new Point2D((gateway.p1.x + gateway.p2.x) / 2, (gateway.p1.y + gateway.p2.y) / 2);
                            // find new gateway
                            p1 = this.getPolygon(polygon.getPolygonId());
                            p2 = this.getPolygon(polygon2.getPolygonId());
                            v1 = -1;
                            v2 = -1;
                            outer:
                            for (int i = 0; i < p1.length; i++) {
                                for (int j = 0; j < p2.length; j++) {
                                    if (p1[i] == p2[j]) {
                                        if (v1 == -1) {
                                            v1 = p1[i];
                                        } else {
                                            if (p1[i] != v1) {
                                                v2 = p1[i];
                                            }
                                            break outer;
                                        }
                                    }
                                }
                            }
                            vv1 = getVertex(v1);
                            vv2 = getVertex(v2);
                            gateway = new Line2D(vv1[0], vv1[1], vv2[0], vv2[1]);
                            // decide which endpoint of gateway is on the left side and which is on the right side
                            // if the gateway is directed correctly, than the middle of old gateway should be on the left side of it
                            dist = gateway.getSignedDistance(middleOfOldGateway);
                            if (dist < 0) {
                                vv1 = getVertex(v2);
                                vv2 = getVertex(v1);
                                gateway = new Line2D(vv1[0], vv1[1], vv2[0], vv2[1]);
                            }
                            // gateway found

                            // try to update mantinels
                            // left mantinel
                            dist = leftMantinel.getSignedDistance(gateway.p2);
                            // if the point is inside, it should be right from left mantinel
                            if (dist < 0) {
                                // ok, it is right from left mantinel
                                // now check if the new mantinel would cross right mantinel
                                dist = rightMantinel.getSignedDistance(gateway.p2);
                                // if the point is inside, it should be on the left
                                if (dist > 0) {
                                    // ok, left point is inside funnel. we can narrow the funnel
                                    leftMantinel = new Line2D(leftMantinel.p1, gateway.p2);
                                    leftMantinelIndex = index;
                                    leftMantinelZ = vv2[2];
                                    if (safeVertex.get(v2)) {
                                        leftMantinelTarget = new Location(vv2[0], vv2[1], vv2[2] + MyNavMeshConstants.liftPolygonLocation);
                                    } else {
                                        if (gateway.getLength() <= 2 * MyNavMeshConstants.agentRadius) {
                                            leftMantinelTarget = new Location((vv2[0] + vv1[0]) / 2, (vv2[1] + vv1[1]) / 2, (vv2[2] + vv1[2]) / 2 + MyNavMeshConstants.liftPolygonLocation);
                                        } else {
                                            leftMantinelTarget = new Location(vv2[0] + (vv1[0] - vv2[0]) / gateway.getLength() * MyNavMeshConstants.agentRadius,
                                                    vv2[1] + (vv1[1] - vv2[1]) / gateway.getLength() * MyNavMeshConstants.agentRadius,
                                                    vv2[2] + (vv1[2] - vv2[2]) / gateway.getLength() * MyNavMeshConstants.agentRadius + MyNavMeshConstants.liftPolygonLocation);
                                        }
                                    }
                                } else {
                                    // there is a cross! left mantinel would cross the right one!
                                    // we will restart funneling and continue from corner of right mantinel
                                    //path.add(new Location(rightMantinel.p2.x, rightMantinel.p2.y, rightMantinelZ+MyNavMeshConstants.liftPolygonLocation));
                                    path.add(rightMantinelTarget);
                                    outOfMantinels = true;
                                    index = rightMantinelIndex;
                                }
                            } else {
                                // point is left from left mantinel.
                                // we cannot do anything.
                                // the mantinel stays where it is
                            }

                            // now the right mantinel
                            dist = rightMantinel.getSignedDistance(gateway.p1);
                            // if the point is inside, it should be left from right mantinel
                            if (dist > 0) {
                                // ok, it is left from right mantinel

                                // btw this is impossible if the left mantinel is already crossing the right.
                                // now check if the new mantinel would cross left mantinel
                                dist = leftMantinel.getSignedDistance(gateway.p1);
                                // if the point is inside, it should be on the right
                                if (dist < 0) {
                                    // ok, right point is inside funnel. we can narrow the funnel
                                    rightMantinel = new Line2D(rightMantinel.p1, gateway.p1);
                                    rightMantinelIndex = index;
                                    rightMantinelZ = vv1[2];
                                    if (safeVertex.get(v1)) {
                                        rightMantinelTarget = new Location(vv1[0], vv1[1], vv1[2] + MyNavMeshConstants.liftPolygonLocation);
                                    } else {
                                        if (gateway.getLength() <= 2 * MyNavMeshConstants.agentRadius) {
                                            rightMantinelTarget = new Location((vv2[0] + vv1[0]) / 2, (vv2[1] + vv1[1]) / 2, (vv2[2] + vv1[2]) / 2 + MyNavMeshConstants.liftPolygonLocation);
                                        } else {
                                            rightMantinelTarget = new Location(vv1[0] + (vv2[0] - vv1[0]) / gateway.getLength() * MyNavMeshConstants.agentRadius,
                                                    vv1[1] + (vv2[1] - vv1[1]) / gateway.getLength() * MyNavMeshConstants.agentRadius,
                                                    vv1[2] + (vv2[2] - vv1[2]) / gateway.getLength() * MyNavMeshConstants.agentRadius + MyNavMeshConstants.liftPolygonLocation);
                                        }
                                    }
                                } else {
                                    // there is a cross! right mantinel would cross the left one!
                                    // we will restart funneling and continue from corner of left mantinel
                                    //path.add(new Location(leftMantinel.p2.x, leftMantinel.p2.y, leftMantinelZ+MyNavMeshConstants.liftPolygonLocation));
                                    path.add(leftMantinelTarget);
                                    outOfMantinels = true;
                                    index = leftMantinelIndex;
                                }
                            } else {
                                // point is right from right mantinel.
                                // we cannot do anything.
                                // the mantinel stays where it is
                            }
                        }
                    }
                }
            }
        }
        path.add(to);
        return path;
    }

    /**
     * Computes and returns a path between two points anywhere on the map. If no
     * such path is found, returns null;
     *
     * @param from
     * @param to
     * @return
     */
    public List<ILocated> getPath(ILocated from, ILocated to) {

        // first we found a list of polygons and off-mesh connections on the path
        // using A* algorithm
        List<INavMeshAtom> polygonPath = getPolygonPath(from.getLocation(), to.getLocation());
        if (polygonPath == null) {
            return null;
        }

        //this.drawPolygonPath(polygonPath, new Location(255,255,0));
        List<ILocated> path;

        // now we transform path made of polygons to path made of Locations        
        // path = getPolygonCentrePath(from, to, polygonPath);      
        path = getFunneledPath(from, to, polygonPath);

        return path;
    }

    /**
     * Computes and returns a path between two points anywhere on the map. If no
     * such path is found, returns path of zero length;
     *
     * @param from
     * @param to
     * @return
     */
    public IPathFuture<ILocated> computePath(ILocated from, ILocated to) {
        return new PrecomputedPathFuture<ILocated>(from, to, getPath(from, to));
    }

    /**
     * Returns the nearest NavMeshAtom to given location
     *
     * @param location
     * @return
     */
    private INavMeshAtom getNearestAtom(Location location) {

        // if this point is on a polygon we return this polygon
        int pId = getPolygonIdByLocation(location);
        if (pId > 0) {
            return new NavMeshPolygon1(pId);
        } else {
            // we return nearest offmeshPoint
            // TODO: be smarter! count in polygons too!
            double minDist = Double.MAX_VALUE;
            INavMeshAtom nearest = null;
            for (OffMeshPoint op : offMeshPoints) {
                double dist = location.getDistance(op.getLocation());
                if (dist < minDist) {
                    nearest = op;
                    minDist = dist;
                }
            }

            // if there are no offmeshpoints, return nearest polygon
            // this is slow and it should not happen often
            if (nearest == null) {
                for (int i = 0; i < polys.size(); i++) {
                    NavMeshPolygon1 p = new NavMeshPolygon1(i);
                    Location pl = getLocation(p);
                    double dist = location.getDistance(pl);
                    if (dist < minDist) {
                        nearest = p;
                        minDist = dist;
                    }
                }
            }

            return nearest;
        }
    }

    /**
     * Returns distance between two atoms (euclidean distance) If the atom is a
     * polygon, this method takes its middle
     *
     * @param atom1
     * @param atom2
     * @return
     */
    double getDistance(INavMeshAtom atom1, INavMeshAtom atom2) {
        if (atom1.equals(atom2)) {
            return 0.0;
        }
        Location l1, l2;
        l1 = getLocation(atom1);
        l2 = getLocation(atom2);
        return l1.getDistance(l2);
    }

    /**
     * Empty method it is nver really called for interface atom. always for
     * polygon or point
     *
     * @param atom1
     * @return
     */
    private Location getLocation(INavMeshAtom atom1) {
        if (atom1.getClass() == OffMeshPoint.class) {
            return getLocation((OffMeshPoint) atom1);
        }
        if (atom1.getClass() == NavMeshPolygon1.class) {
            return getLocation((NavMeshPolygon1) atom1);
        }
        if (atom1.getClass() == NavMeshPolygon.class) {
            return getLocation((NavMeshPolygon) atom1);
        }
        throw new UnsupportedOperationException("Not implemented. Not now. Not ever.");
    }

    /**
     * Returns location of the contained navpoint
     *
     * @param op
     * @return
     */
    private Location getLocation(OffMeshPoint op) {
        NavPoint np = (NavPoint) server.getWorldView().get(op.getNavPointId());
        return np.getLocation();
    }

    /**
     * Returns the middle point of the polygon
     *
     * @param p
     * @return
     */
    private Location getLocation(NavMeshPolygon1 p) {
        int[] polygon = this.getPolygon(p.getPolygonId());
        double sumX = 0.0;
        double sumY = 0.0;
        double sumZ = 0.0;
        for (int i = 0; i < polygon.length; i++) {
            double[] v = getVertex(polygon[i]);
            sumX += v[0];
            sumY += v[1];
            sumZ += v[2];
        }
        return new Location(sumX / polygon.length, sumY / polygon.length, (sumZ / polygon.length) + MyNavMeshConstants.liftPolygonLocation);
    }
    
    private Location getLocation(NavMeshPolygon p) {
        int[] polygon = this.getPolygon(p.getPolygonId());
        double sumX = 0.0;
        double sumY = 0.0;
        double sumZ = 0.0;
        for (int i = 0; i < polygon.length; i++) {
            double[] v = getVertex(polygon[i]);
            sumX += v[0];
            sumY += v[1];
            sumZ += v[2];
        }
        return new Location(sumX / polygon.length, sumY / polygon.length, (sumZ / polygon.length) + MyNavMeshConstants.liftPolygonLocation);
    }

    /**
     * Returns levelGeometry object
     *
     * @return
     */
    public LevelGeometry getLevelGeometry() {
        return this.levelGeometry;
    }

    /**
     * A simple implementation of NavMesh's 2D raycasting. Returns distance from
     * the edge of navmesh in a direction from a location if the entrire ray is
     * inside navmesh of there is no navmesh it returns 0;
     *
     * @param location
     * @param vector
     * @return
     */
    public double getDistanceFromEdge(Location location, Vector2d vector, double rayLength) {
        if (rayLength <= 0) {
            return 0;
        }

        // get the end location (in 2D)
        vector.normalize();
        vector.x *= rayLength;
        vector.y *= rayLength;
        Location end = new Location(location.x + vector.x, location.y + vector.y);

        // get a 2D projection of ray
        Line2D ray = new Line2D(location.x, location.y, end.x, end.y);
        // get the current polygon
        int pId = this.getPolygonIdByLocation(location);
        if (pId < 0) {
            return 0;
        }

        // how to find end of navmesh?
        // 1. start on the polygon of starting location
        // 2. find where the line crosses its border
        // 3. while there is another polyogn behind, repeat
        // 4. return the last cross (its distance from location)
        int currentPolygonId = pId;
        int lastPolygonId = -1;
        int nextPolygonId = -1;

        // find the first cross
        Point2D cross = null;
        int v1 = -1, v2 = -1;
        int[] polygon = getPolygon(currentPolygonId);
        for (int i = 0; i < polygon.length; i++) {
            v1 = polygon[i];
            v2 = polygon[((i == polygon.length - 1) ? 0 : i + 1)];
            double[] vertex1 = getVertex(v1);
            double[] vertex2 = getVertex(v2);
            Line2D edge = new Line2D(vertex1[0], vertex1[1], vertex2[0], vertex2[1]);
            cross = ray.getIntersection(edge);
            if (cross != null) {
                if (cross.x <= Math.max(edge.p1.x, edge.p2.x) && cross.x >= Math.min(edge.p1.x, edge.p2.x)
                        && cross.x <= Math.max(ray.p1.x, ray.p2.x) && cross.x >= Math.min(ray.p1.x, ray.p2.x)) {
                    // int's a cross!
                    break;
                } else {
                    // its not a cross
                    cross = null;
                }
            }
        }
        // now we have the cross.
        // if it too far from location, we return 0;
        if (cross == null || ray.p1.getDistance(cross) >= rayLength) {
            return 0;
        }

        // is there another polygon behind?
        nextPolygonId = getNeighbourPolygon(currentPolygonId, v1, v2);
        if (nextPolygonId == -1) {
            // there is no polygon. we return distance of cross
            return ray.p1.getDistance(cross);
        } else {
            // if there is another polygon, we return recursively distance from egde in that direction
            // move a little so it is in the neighbour polygon
            vector = ((Vector2d) vector.clone());
            vector.normalize();
            Location crossLocation = new Location(cross.x + vector.x, cross.y + vector.y, location.z);
            return getDistanceFromEdge(crossLocation, vector, rayLength - ray.p1.getDistance(cross));

        }
    }

    /**
     * Returns distance of the location from the navmesh's edge in the given
     * direction. If location is not not on navmesh, 0 is returned
     *
     * @param location
     * @param vector
     * @return
     */
    public double getDistanceFromEdge(Location location, Vector2d vector) {
        return getDistanceFromEdge(location, vector, Double.MAX_VALUE);
    }

    /**
     * Finds neighbour behind given vertexes. Returns polygon id or -1 there is
     * none
     *
     * @param currentPolygonId
     * @param v1
     * @param v2
     * @return
     */
    private int getNeighbourPolygon(int currentPolygonId, int v1, int v2) {
        // try all neighbours (except last one)
        List<Integer> neighbours = this.getNeighbourIdsToPolygon(currentPolygonId);
        for (Integer neighbour : neighbours) {
            // find the shared edge
            int[] polygon2 = getPolygon(neighbour);
            // polygon2 must contain both vertices
            boolean containsV1 = false, containsV2 = false;
            for (int j = 0; j < polygon2.length; j++) {
                if (polygon2[j] == v1) {
                    containsV1 = true;
                }
                if (polygon2[j] == v2) {
                    containsV2 = true;
                }
            }

            if (containsV1 && containsV2) {
                return neighbour;
            }
        }
        // no good neighbour was found
        return -1;
    }

}
