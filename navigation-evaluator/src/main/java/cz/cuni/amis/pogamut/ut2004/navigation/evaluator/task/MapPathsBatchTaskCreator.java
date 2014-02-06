package cz.cuni.amis.pogamut.ut2004.navigation.evaluator.task;

import cz.cuni.amis.pogamut.ut2004.navigation.evaluator.task.MapPathsEvaluationTask.PathType;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Bogo
 */
public class MapPathsBatchTaskCreator {
    
    private int batchNumber;
    private List<String> maps;
    private String navigation;
    private String pathPlanner;
    private String resultPath;
    private List<PathType> pathTypes;
    
    public MapPathsBatchTaskCreator(int batchNumber, List<String> maps, String navigation, String pathPlanner, String resultPath, List<PathType> pathTypes) {
        this.batchNumber = batchNumber;
        this.maps = maps;
        this.navigation = navigation;
        this.pathPlanner = pathPlanner;
        this.resultPath = resultPath;
        this.pathTypes = pathTypes;
    }
    
    public List<MapPathsEvaluationTask> createBatch() {
        List<MapPathsEvaluationTask> tasks = new LinkedList<MapPathsEvaluationTask>();
        
        for (String map : maps) {
            for (PathType type : pathTypes) {
                tasks.add(new MapPathsEvaluationTask(map, navigation, pathPlanner, resultPath, type, batchNumber));
            }
        }
        
        return tasks;
    }
}
