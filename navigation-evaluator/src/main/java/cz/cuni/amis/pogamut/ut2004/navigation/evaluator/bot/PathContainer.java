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
package cz.cuni.amis.pogamut.ut2004.navigation.evaluator.bot;

import cz.cuni.amis.pogamut.base.communication.worldview.object.WorldObjectId;
import cz.cuni.amis.pogamut.base3d.worldview.IVisionWorldView;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;
import cz.cuni.amis.utils.collections.MyCollections;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Container for path evaluation. Provides paths which should be evaluated.
 *
 * @author Bogo
 */
public class PathContainer {

    private int tabooRetryCount = 1;
    IVisionWorldView world;
    private HashMap<WorldObjectId, Set<WorldObjectId>> paths;
    private HashMap<Path, Integer> tabooPaths;

    /**
     * Create container for given worldview.
     *
     * @param world
     *
     */
    public PathContainer(IVisionWorldView world) {
        this.world = world;
        paths = new HashMap<WorldObjectId, Set<WorldObjectId>>();
        tabooPaths = new HashMap<Path, Integer>();
    }

    /**
     * Get any path from container. Returns null if container is empty.
     *
     * @return Path to navigate
     */
    public Path getPath() {
        if (isEmpty()) {
            return null;
        }
        if (paths.isEmpty()) {
            return MyCollections.getRandom(tabooPaths.keySet());
        }
        WorldObjectId start = MyCollections.getRandom(paths.keySet());
        Set<WorldObjectId> ends = paths.get(start);
        while (ends == null) {
            start = MyCollections.getRandom(paths.keySet());
            ends = paths.get(start);
        }
        if (ends != null) {
            WorldObjectId end = MyCollections.getRandom(ends);
            ends.remove(end);
            if (ends.isEmpty()) {
                paths.remove(start);
            }
            return new Path((NavPoint) world.get(start), (NavPoint) world.get(end));
        }
        return null;
    }

    /**
     * Get path with start at given {@link NavPoint}. TODO: Variant with start
     * parameter as source for finding path with nearest possible start.
     *
     * @param start Where the path should start
     * @return Path to navigate
     *
     */
    public Path getPath(NavPoint start) {
        Set<WorldObjectId> ends = paths.get(start.getId());
        if (ends == null) {
            //No more paths exists!
            return null;
        }
        WorldObjectId end = MyCollections.getRandom(ends);
        ends.remove(end);
        if (ends.isEmpty()) {
            paths.remove(start.getId());
        }
        return new Path(start, (NavPoint) world.get(end));
    }

    /**
     * Builds set of paths between all {@link NavPoint}s of given map.
     */
    public void build() {
        build(-1);
    }

    /**
     * Build set of all relevant paths for given map. Relevant paths are paths
     * between {@link NavPoint}s which are either inventory spots or player
     * starts.
     *
     * @param limit Max number of paths built. If limit < 0 build all paths.
     */
    public void buildRelevant(int limit) {
        Map<WorldObjectId, NavPoint> navPoints = world.getAll(NavPoint.class);
        HashSet<WorldObjectId> relevantNavPoints = new HashSet<WorldObjectId>();
        for (NavPoint navPoint : navPoints.values()) {
            if (navPoint.isInvSpot() || navPoint.isPlayerStart()) {
                relevantNavPoints.add(navPoint.getId());
            }
        }
        buildPaths(relevantNavPoints, limit);
    }

    /**
     * Build set of all relevant paths for given map. Relevant paths are paths
     * between {@link NavPoint}s which are either inventory spots or player
     * starts.
     */
    public void buildRelevant() {
        buildRelevant(-1);
    }

    /**
     * Checks whether container is empty.
     *
     * @return Container is empty
     */
    protected boolean isEmpty() {
        return paths.isEmpty() && tabooPaths.isEmpty();
    }

    /**
     * Returns size of the container.
     *
     * @return Number of paths in the container.
     */
    protected int size() {
        if (isEmpty()) {
            return 0;
        } else {
            int size = 0;
            for (Set<WorldObjectId> set : paths.values()) {
                size += set.size();
            }
            return size + tabooPaths.size();
        }
    }

    void build(int limit) {
        Map<WorldObjectId, NavPoint> navPoints = world.getAll(NavPoint.class);
        Set<WorldObjectId> navPointsIds = new HashSet<WorldObjectId>(navPoints.keySet());
        buildPaths(navPointsIds, limit);
    }

    private void buildPaths(Set<WorldObjectId> navPoints, int limit) {
        paths.clear();
        int pathCount = navPoints.size() * (navPoints.size() - 1);
        boolean buildIncrementaly = limit < pathCount / 5;

        if (limit < 0 || !buildIncrementaly) {
            HashSet<WorldObjectId> relevantEnds = new HashSet<WorldObjectId>(navPoints);
            for (WorldObjectId navPointId : navPoints) {
                HashSet<WorldObjectId> ends = new HashSet<WorldObjectId>(relevantEnds);
                ends.remove(navPointId);
                paths.put(navPointId, ends);
            }
        }
        if (limit > 0) {
            if (!buildIncrementaly) {
                while (pathCount > limit) {
                    getPath();
                    --pathCount;
                }
            } else {
                pathCount = 0;
                while (pathCount < limit) {
                    WorldObjectId start = MyCollections.getRandom(navPoints);
                    WorldObjectId end = MyCollections.getRandom(navPoints);
                    if (start.equals(end)) {
                        //Path which ends where it starts is not valid!
                        continue;
                    }
                    pathCount += addPath(start, end);
                }
            }
        }
    }

    void buildFromFile(String repeatFilePath) {
        paths.clear();
        BufferedReader reader = null;
        try {
            File repeatFile = new File(repeatFilePath);
            reader = new BufferedReader(new FileReader(repeatFile));
             //Skip first line - contains column descriptors
            reader.readLine();
            String line = reader.readLine();
            while (line != null) {
                String[] splitLine = line.split(";");
                WorldObjectId startId = WorldObjectId.get(splitLine[1]);
                WorldObjectId endId = WorldObjectId.get(splitLine[2]);
                addPath(startId, endId);
                line = reader.readLine();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PathContainer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PathContainer.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(PathContainer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private int addPath(WorldObjectId start, WorldObjectId end) {
        Set<WorldObjectId> pathsFromStart = paths.get(start);
        if (pathsFromStart == null) {
            pathsFromStart = new HashSet<WorldObjectId>();
            paths.put(start, pathsFromStart);
        }
        if (pathsFromStart.add(end)) {
            return 1;
        }
        return 0;
    }

    public boolean addTabooPath(Path path) {
        Integer retries = tabooPaths.get(path);
        if (retries == null) {
            tabooPaths.put(path, tabooRetryCount);
        } else if (retries <= 1) {
            tabooPaths.remove(path);
            return false;
        } else {
            tabooPaths.put(path, retries - 1);
        }
        return true;
    }
}
