package cz.cuni.amis.pogamut.ut2004.navigation.evaluator.task;

import cz.cuni.amis.pogamut.ut2004.navigation.evaluator.data.RecordType;

/**
 *
 * @author Bogo
 */
public class NavigationEvaluationRepeatTask extends NavigationEvaluationTask {

    private String repeatFile;

    public NavigationEvaluationRepeatTask(String repeatFile, String navigation, String pathPlanner, String resultPath, RecordType recordType) {
        super(navigation, pathPlanner, null, true, -1, resultPath, recordType);
        this.repeatFile = repeatFile;
        int end = repeatFile.length();
        for(int i = 0; i < 2; i++) {
            end = repeatFile.lastIndexOf('/', end - 1);
        }
        int start = repeatFile.lastIndexOf('/', end - 1);
        setMapName(repeatFile.substring(start + 1, end));
    }

    public String getRepeatFile() {
        return repeatFile;
    }
}
