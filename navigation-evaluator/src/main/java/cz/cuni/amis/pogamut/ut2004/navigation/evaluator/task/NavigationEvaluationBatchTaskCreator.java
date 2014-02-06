package cz.cuni.amis.pogamut.ut2004.navigation.evaluator.task;

import cz.cuni.amis.pogamut.ut2004.navigation.evaluator.data.RecordType;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Bogo
 */
public class NavigationEvaluationBatchTaskCreator {
    
    public static List<NavigationEvaluationTask> createBatch(String navigation, String pathPlanner, List<String> mapNames, boolean onlyRelevantPaths, int limit, String resultPath, RecordType recordType) {
        List<NavigationEvaluationTask> tasks = new LinkedList<NavigationEvaluationTask>();        
        
        for (String map : mapNames) {
            tasks.add(new NavigationEvaluationTask(navigation, pathPlanner, map, onlyRelevantPaths, limit, resultPath, recordType));
        }
        
        return tasks;
    }
}
