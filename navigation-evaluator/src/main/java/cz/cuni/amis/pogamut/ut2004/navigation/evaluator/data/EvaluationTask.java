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

/**
 * Evaluation task. Consists of map to evaluate and parameters of navigation.
 *
 * @author Bogo
 */
public class EvaluationTask implements IEvaluationTask {
    
    private String navigation;
    private String pathPlanner;
    private String mapName;
    private boolean onlyRelevantPaths;
    private String resultPath;
    private boolean resultUnique;
    private RecordType recordType;
    //Max number of paths to explore
    private int limit;
    
    public EvaluationTask(String navigation, String pathPlanner, String mapName, boolean onlyRelevantPaths, int limit, String resultPath, boolean resultUnique, RecordType recordType) {
        this.navigation = navigation;
        this.pathPlanner = pathPlanner;
        this.mapName = mapName;
        this.onlyRelevantPaths = onlyRelevantPaths;
        this.limit = limit;
        this.resultPath = resultPath;
        this.resultUnique = resultUnique;
        this.recordType = recordType;
    }
    
    public EvaluationTask(String navigation, String pathPlanner, String mapName, boolean onlyRelevantPaths, String resultPath) {
        this(navigation, pathPlanner, mapName, onlyRelevantPaths, 30, resultPath, false, RecordType.FULL);
    }

    //TODO: Remove
    public EvaluationTask() {
        this("navigation", "fwMap", "DM-TrainingDay", true, 10, "C:/Temp/Pogamut/stats/", false, RecordType.FULL);
    }

    /**
     * Creates task from command line arguments.
     *
     * @param args Command line arguments.
     * @return Task built from command line arguments.
     */
    public static EvaluationTask buildFromArgs(String[] args) {
        //TODO: Check validity of args?
        if (args.length == 8) {
            return new EvaluationTask(args[0], args[1], args[2], Boolean.parseBoolean(args[3]), Integer.parseInt(args[4]), args[5], Boolean.parseBoolean(args[6]), RecordType.valueOf(args[7]));
        }
        return new EvaluationTask();
    }

    /**
     * Creates {@link BotNavigationParameters} from this task.
     *
     * @return {@link BotNavigationParameters}'s representation of this task.
     */
    public BotNavigationParameters getBotNavigationParams() {
        return new BotNavigationParameters(this);
    }

    /**
     * Map for evaluation.
     *
     * @return
     */
    public String getMapName() {
        return mapName;
    }
    
    public void setMapName(String mapName) {
        this.mapName = mapName;
    }

    /**
     * Whether to evaluate only relevant paths.
     *
     * @return
     */
    public boolean isOnlyRelevantPaths() {
        return onlyRelevantPaths;
    }
    
    public String getNavigation() {
        return navigation;
    }
    
    public String getPathPlanner() {
        return pathPlanner;
    }
    
    public String getResultPath() {
        return resultPath;
    }
    
    public boolean isResultUnique() {
        return resultUnique;
    }
    
    public int getLimit() {
        return limit;
    }
    
    public void setNavigation(String navigation) {
        this.navigation = navigation;
    }
    
    public void setPathPlanner(String pathPlanner) {
        this.pathPlanner = pathPlanner;
    }
    
    public void setOnlyRelevantPaths(boolean onlyRelevantPaths) {
        this.onlyRelevantPaths = onlyRelevantPaths;
    }
    
    public void setResultPath(String resultPath) {
        this.resultPath = resultPath;
    }
    
    public void setResultUnique(boolean resultUnique) {
        this.resultUnique = resultUnique;
    }
    
    public void setLimit(int limit) {
        this.limit = limit;
    }
    
    public RecordType getRecordType() {
        return recordType;
    }

    /**
     * Push task as command line arguments.
     *
     * @param command Arguments list to fill.
     */
    public void toArgs(List<String> command) {
        command.add(navigation);
        command.add(pathPlanner);
        command.add(mapName);
        command.add(Boolean.toString(onlyRelevantPaths));
        command.add(Integer.toString(limit));
        command.add(resultPath);
        command.add(Boolean.toString(resultUnique));
        command.add(recordType.name());
    }
}
