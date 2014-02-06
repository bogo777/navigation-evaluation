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
package cz.cuni.amis.pogamut.ut2004.navigation.evaluator.data;

import cz.cuni.amis.pogamut.ut2004.navigation.evaluator.bot.BotNavigationParameters;
import java.util.List;
import org.apache.commons.beanutils.ConvertUtils;

/**
 *
 * @author Bogo
 */
public class EvaluationTask {
    
    private String navigation;
    
    private String pathPlanner;
    
    private String mapName;
    
    private boolean onlyRelevantPaths;
    
    private String resultPath;
    
    //Max number of paths to explore
    private int limit;
    
    public EvaluationTask(String navigation, String pathPlanner, String mapName, boolean onlyRelevantPaths, int limit, String resultPath) {
        this.navigation = navigation;
        this.pathPlanner = pathPlanner;
        this.mapName = mapName;
        this.onlyRelevantPaths = onlyRelevantPaths;
        this.limit = limit;
        this.resultPath = resultPath;
    }
    
    public EvaluationTask(String navigation, String pathPlanner, String mapName, boolean onlyRelevantPaths, String resultPath) {
        this(navigation, pathPlanner, mapName, onlyRelevantPaths, 30, resultPath);
    }
    
    public EvaluationTask() {
        this("navigation", "fwMap", "DM-TrainingDay", true, 10, "C:\\Temp\\Pogamut\\stats\\");
    }
    
    public static EvaluationTask buildFromArgs(String[] args) {
        //TODO: Parse args and build task from them
        if(args.length == 6) {
            return new EvaluationTask(args[0], args[1], args[2], Boolean.parseBoolean(args[3]), Integer.parseInt(args[4]), args[5]);
        } 
        return new EvaluationTask();
    }
    
    
    public BotNavigationParameters getBotNavigationParams() {
        return new BotNavigationParameters().setNavigation(navigation).setPathPlanner(pathPlanner).setOnlyRelevantPaths(onlyRelevantPaths).setLimit(limit).setResultPath(resultPath);
    }

    public String getMapName() {
        return mapName;
    }

    public boolean isOnlyRelevantPaths() {
        return onlyRelevantPaths;
    }

    public void toArgs(List<String> command) {
        command.add(navigation);
        command.add(pathPlanner);
        command.add(mapName);
        command.add(Boolean.toString(onlyRelevantPaths));
        command.add(Integer.toString(limit));
        command.add(resultPath);
    }
    
    
}
