package cz.cuni.amis.pogamut.ut2004.navigation.evaluator.data;

/**
 *
 * @author Bogo
 */
public class EvaluationRepeatTask extends EvaluationTask {

    private String repeatFile;

    public EvaluationRepeatTask(String repeatFile, String navigation, String pathPlanner, String resultPath, RecordType recordType) {
        super(navigation, pathPlanner, null, true, -1, resultPath, true, recordType);
        this.repeatFile = repeatFile;
        int start = repeatFile.lastIndexOf('/');
        int end = repeatFile.indexOf('_', start);
        if (end == -1) {
            end = repeatFile.indexOf('.', start);
        }
        setMapName(repeatFile.substring(start + 1, end));
    }

    public String getRepeatFile() {
        return repeatFile;
    }
}
