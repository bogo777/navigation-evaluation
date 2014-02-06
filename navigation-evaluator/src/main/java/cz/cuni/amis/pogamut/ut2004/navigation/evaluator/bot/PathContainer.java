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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Container for path evaluation. Provides paths which should be evaluated.
 *
 * @author Bogo
 */
public class PathContainer {

    IVisionWorldView world;
    private HashMap<WorldObjectId, Set<WorldObjectId>> paths;

    /**
     * Create container for given worldview.
     * @param world 
     * 
     */
    public PathContainer(IVisionWorldView world) {
        this.world = world;
        paths = new HashMap<WorldObjectId, Set<WorldObjectId>>();
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
        WorldObjectId start = MyCollections.getRandom(paths.keySet());
        Set<WorldObjectId> ends = paths.get(start);
        while (ends == null) {
            start = MyCollections.getRandom(paths.keySet());
            ends = paths.get(start);
        }
        if (ends == null) {
            //No more paths exists!
            return null;
        }
        WorldObjectId end = MyCollections.getRandom(ends);
        ends.remove(end);
        if (ends.isEmpty()) {
            paths.remove(start);
        }
        return new Path((NavPoint) world.get(start), (NavPoint) world.get(end));
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
        paths.clear();
        Map<WorldObjectId, NavPoint> navPoints = world.getAll(NavPoint.class);
        Set<WorldObjectId> navPointsIds = new HashSet<WorldObjectId>(navPoints.keySet());
        for (WorldObjectId navPointId : navPoints.keySet()) {
            HashSet<WorldObjectId> ends = new HashSet<WorldObjectId>(navPointsIds);
            ends.remove(navPointId);
            paths.put(navPointId, ends);
        }
    }

    /**
     * Build set of all relevant paths for given map. Relevant paths are paths
     * between {@link NavPoint}s which are either inventory spots or player
     * starts.
     *
     * @param limit Max number of paths built. If limit < 0 build all paths.
     */
    public void buildRelevant(int limit) {
        paths.clear();
        Map<WorldObjectId, NavPoint> navPoints = world.getAll(NavPoint.class);
        HashSet<WorldObjectId> relevantNavPoints = new HashSet<WorldObjectId>();
        for (NavPoint navPoint : navPoints.values()) {
            if (navPoint.isInvSpot() || navPoint.isPlayerStart()) {
                relevantNavPoints.add(navPoint.getId());
            }
        }
        int pathCount = relevantNavPoints.size() * (relevantNavPoints.size() - 1);
        boolean buildIncrementaly = limit < pathCount / 5;

        if (limit < 0 || !buildIncrementaly) {
            HashSet<WorldObjectId> relevantEnds = new HashSet<WorldObjectId>(relevantNavPoints);
            for (WorldObjectId navPointId : relevantNavPoints) {
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
                    WorldObjectId start = MyCollections.getRandom(relevantNavPoints);
                    WorldObjectId end = MyCollections.getRandom(relevantNavPoints);
                    if (start.equals(end)) {
                        //Path which ends where it starts is not valid!
                        continue;
                    }
                    Set<WorldObjectId> pathsFromStart = paths.get(start);
                    if (pathsFromStart == null) {
                        pathsFromStart = new HashSet<WorldObjectId>();
                        paths.put(start, pathsFromStart);
                    }
                    if (pathsFromStart.add(end)) {
                        ++pathCount;
                    }
                }
            }
        }

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
        return paths.isEmpty();
    }

    /**
     * Returns size of the container.
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
            return size;
        }
    }
}
