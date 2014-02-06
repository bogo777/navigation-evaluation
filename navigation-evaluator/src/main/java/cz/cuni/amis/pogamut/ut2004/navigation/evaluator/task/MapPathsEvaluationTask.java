package cz.cuni.amis.pogamut.ut2004.navigation.evaluator.task;

import cz.cuni.amis.pogamut.ut2004.navigation.evaluator.bot.MapPathsBot;
import cz.cuni.amis.pogamut.ut2004.navigation.evaluator.bot.MapPathsBotParameters;
import java.io.File;
import java.util.List;

/**
 *
 * @author Bogo
 */
public class MapPathsEvaluationTask extends EvaluationTask<MapPathsBotParameters, MapPathsBot> implements IEvaluationTask<MapPathsBotParameters, MapPathsBot> {

    private String navigation;
    private String pathPlanner;
    private String mapName;
    private String resultPath;
    private PathType pathType;
    private int batchNumber = 0;

    public MapPathsEvaluationTask(String mapName, String navigation, String pathPlanner, String resultPath, PathType pathType) {
        super(MapPathsBotParameters.class, MapPathsBot.class);
        this.navigation = navigation;
        this.mapName = mapName;
        this.pathPlanner = pathPlanner;
        this.resultPath = resultPath;
        this.pathType = pathType;
    }

    public MapPathsEvaluationTask(String mapName, String navigation, String pathPlanner, String resultPath, PathType pathType, int batchNumber) {
        this(mapName, navigation, pathPlanner, resultPath, pathType);
        this.batchNumber = batchNumber;
    }

    public String getMapName() {
        return mapName;
    }

    public MapPathsBotParameters getBotParams() {
        return new MapPathsBotParameters(this);
    }

    public String getNavigation() {
        return navigation;
    }

    public String getPathPlanner() {
        return pathPlanner;
    }

    public String getResultPath() {
        String fullPath = String.format("%s/%s/", resultPath, getMapName());
        File resultFile = new File(fullPath);
        resultFile.mkdirs();

        return fullPath;
    }

    public PathType getPathType() {
        return pathType;
    }

    public int getBatchNumber() {
        return batchNumber;
    }

    public boolean isBatchTask() {
        return batchNumber > 0;
    }

    /**
     * Creates task from command line arguments.
     *
     * @param args Command line arguments.
     * @return Task built from command line arguments.
     */
    public static MapPathsEvaluationTask buildFromArgs(String[] args) {
        //TODO: Check validity of args?
        if (args.length == 5) {
//            String[] pathTypeStrings = args[4].split(";");
//            List<PathType> types = new LinkedList<PathType>();
//            for (String type : pathTypeStrings) {
//                types.add(PathType.valueOf(type));
//            }
            return new MapPathsEvaluationTask(args[0], args[1], args[2], args[3], PathType.valueOf(args[4]));
        }
        return null;
    }

    /**
     * Push task as command line arguments.
     *
     * @param command Arguments list to fill.
     */
    public void toArgs(List<String> command) {
        command.add(mapName);
        command.add(navigation);
        command.add(pathPlanner);
        command.add(resultPath);

//        String typeString = "";
//        for (PathType type : pathTypes) {
//            typeString += type.name() + ";";
//        }
//        if (!typeString.isEmpty()) {
//            typeString = typeString.substring(typeString.length() - 1);
//        }

        command.add(pathType.name());
    }

    public String getLogPath() {
        return getResultPath() + "log.log";
    }

    public String getFileName() {
        return String.format("MapPaths_%s_%s_%s_%d", navigation, mapName, pathType.name(), batchNumber);
    }

    public enum PathType {

        ALL, NO_JUMPS, NO_LIFTS, NO_JUMP_NO_LIFTS
    }
}
