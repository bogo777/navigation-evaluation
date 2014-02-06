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
package cz.cuni.amis.pogamut.ut2004.navigation.evaluator.bot;

import cz.cuni.amis.pogamut.ut2004.bot.params.UT2004BotParameters;

/**
 *
 * @author Bogo
 */
public class BotNavigationParameters extends UT2004BotParameters {
    
    private String pathPlanner;
    
    private String navigation;
    
    private boolean onlyRelevantPaths;
    
    private int limit;
    
    private String resultPath;

    public String getResultPath() {
        return resultPath;
    }

    public BotNavigationParameters setResultPath(String resultPath) {
        this.resultPath = resultPath;
        return this;
    }

    public boolean isOnlyRelevantPaths() {
        return onlyRelevantPaths;
    }

    public BotNavigationParameters setOnlyRelevantPaths(boolean onlyRelevantPaths) {
        this.onlyRelevantPaths = onlyRelevantPaths;
        return this;
    }

    public int getLimit() {
        return limit;
    }

    public BotNavigationParameters setLimit(int limit) {
        this.limit = limit;
        return this;
    }

    public String getPathPlanner() {
        return pathPlanner;
    }

    /**
     * Sets path planner for given bot. Supported values: fwMap, navMesh
     * 
     * @param pathPlanner
     * @return 
     * 
     */
    public BotNavigationParameters setPathPlanner(String pathPlanner) {
        this.pathPlanner = pathPlanner;
        return this;
    }

    public String getNavigation() {
        return navigation;
    }

    /**
     * Sets navigation for given bot. Supported values: 
     * 
     * @param navigation
     * @return 
     */
    public BotNavigationParameters setNavigation(String navigation) {
        this.navigation = navigation;
        return this;
    }
    
}
