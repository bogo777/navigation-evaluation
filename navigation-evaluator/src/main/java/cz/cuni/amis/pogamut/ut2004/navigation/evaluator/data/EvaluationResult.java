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

import cz.cuni.amis.pogamut.base.communication.command.IAct;
import cz.cuni.amis.pogamut.ut2004.navigation.evaluator.bot.Path;
import cz.cuni.amis.pogamut.base.utils.logging.LogCategory;
import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Record;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.StopRecord;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;
import cz.cuni.amis.pogamut.ut2004.navigation.evaluator.ServerRunner;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;

/**
 * Result of evaluation of corresponding task.
 *
 * @author Bogo
 */
public class EvaluationResult {

    private int totalPaths;
    private String mapName;
    private LogCategory log;
    private HashSet<PathResult> pathResults;
    private int completedCount = 0;
    private int failedCount = 0;
    private int notBuiltCount = 0;
    private int processedCount = 0;
    
    private int failedToStartCount = 0;
    private int failedInNavigateCount = 0;
    private int failedToStartInNavigateCount = 0;
    
    private String resultPath;

    public EvaluationResult(int total, String map, LogCategory log, String resultPath) {
        totalPaths = total;
        mapName = map;
        this.log = log;
        this.resultPath = resultPath;
        pathResults = new HashSet<PathResult>(totalPaths);
    }

    /**
     * Adds result for single path.
     *
     * @param path Added path.
     * @param type Result of evaluation.
     * @param duration Duration of navigation.
     */
    public void addResult(Path path, PathResult.ResultType type, long duration) {
        updateCounters(type);
        pathResults.add(new PathResult(path, type, duration));
    }
    
    /**
     * Adds result for single path.
     *
     * @param path Added path.
     * @param type Result of evaluation.
     * @param duration Duration of navigation.
     * @param location Current bot location. (Failed only)
     * @param nearestNavPoint Nearest NavPoint. (Failed only)
     */
    public void addResult(Path path, PathResult.ResultType type, long duration, Location location, NavPoint nearestNavPoint) {
        updateCounters(type);
        pathResults.add(new PathResult(path, type, duration, location, nearestNavPoint));
    }
    
    private void updateCounters(PathResult.ResultType type) {
        ++processedCount;
        switch (type) {
            case Completed:
                ++completedCount;
                break;
            case NotBuilt:
                ++notBuiltCount;
                break;
            case Failed:
                ++failedCount;
                break;
            case FailedToStart:
                ++failedToStartCount;
                break;
            case FailedInNavigate:
                ++failedInNavigateCount;
                break;
            case FailedToStartInNavigate:
                ++failedToStartInNavigateCount;
                break;
            default:
                throw new AssertionError(type.name());
        }
    }

    /**
     * Exports aggregate statistics about evaluation. TODO: Create unique files
     * on request?
     */
    public void exportAggregate() {
        FileWriter fstream = null;
        try {
            File resultFile = getResultFile("aggregate.csv");
            fstream = new FileWriter(resultFile);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write("Map;Total;Processes;Completed;Failed;NotBuilt;FailedToStart;FailedInNavigate;FailedToStartInNavigate");
            out.newLine();
            out.write(String.format("%s;%d;%d;%d;%d;%d;%d;%d;%d", mapName, totalPaths, processedCount, completedCount, failedCount, notBuiltCount, failedToStartCount, failedInNavigateCount, failedToStartInNavigateCount));
            out.newLine();
            out.close();
        } catch (IOException ex) {
            log.warning(ex.getMessage());
        } finally {
            try {
                if (fstream != null) {
                    fstream.close();
                }
            } catch (IOException ex) {
                log.warning(ex.getMessage());
            }
        }

    }

    /**
     * Export complete statistics about evaluation.
     */
    public void export() {
        FileWriter fstream = null;
        try {
            File resultFile = getResultFile("csv");
            resultFile.getParentFile().mkdirs();
            fstream = new FileWriter(resultFile);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write("ID;From;To;Type;Duration;Length;Jumps;Lifts;Location;NavPoint");
            out.newLine();
            for (PathResult result : pathResults) {
                out.write(result.export());
                out.newLine();
            }
            out.close();
        } catch (IOException ex) {
            log.warning(ex.getMessage());
        } finally {
            try {
                if (fstream != null) {
                    fstream.close();
                }
            } catch (IOException ex) {
                log.warning(ex.getMessage());
            }
        }
    }

    private File getResultFile(String fileNameSuffix) {
        String fileName = "data." + fileNameSuffix;
        String fullFilePath = resultPath + fileName;
        File resultFile = new File(fullFilePath);
        return resultFile;
    }

    /**
     * Starts recording.
     * 
     * @param act 
     */
    public void startRecording(IAct act) {
        act.act(new Record(getRecordName()));
    }

    /**
     * Starts recording of path. Saves record to corresponding file.
     * 
     * @param act
     * @param path 
     */
    public void startRecording(IAct act, Path path) {
        act.act(new Record(getRecordName(path)));
    }

    /**
     * Stops recording of path. Possibly deletes the record.
     * 
     * @param act
     * @param path
     * @param delete 
     */
    public void stopRecording(IAct act, Path path, boolean delete) {
        act.act(new StopRecord());
        try {
            //give server some time to stop recording and close file...
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            //TODO: log?
        }
        //Move record file to result directory
        String recordFileName = getRecordName(path) + ".demo4";
        File recordFile = new File(ServerRunner.recordsPath, recordFileName);
        if (delete) {
            recordFile.delete();
        } else {
            recordFile.renameTo(new File(resultPath, recordFileName));
        }
    }

    /**
     * Stops recording. Possibly deletes the record.
     * 
     * @param act
     * @param delete 
     */
    public void stopRecording(IAct act, boolean delete) {
        act.act(new StopRecord());
        try {
            //give server some time to stop recording and close file...
            Thread.sleep(500);
        } catch (InterruptedException ex) {
            //TODO: log?
        }
        //Move record file to result directory
        String recordFileName = getRecordName() + ".demo4";
        File recordFile = new File(ServerRunner.recordsPath, recordFileName);
        if (delete) {
            recordFile.delete();
        } else {
            recordFile.renameTo(new File(resultPath, recordFileName));
        }
    }

    private String getRecordName() {
        int last = resultPath.length();
        for (int i = 0; i < 4; i++) {
            last = resultPath.lastIndexOf('/', last - 1);
        }
        String relevantPath = resultPath.substring(last + 1);
        String name = relevantPath.replace("/", "_");
        return name + "record";
    }

    public String getLogFile() {
        return resultPath + "log.log";
    }

    private String getRecordName(Path path) {
        return getRecordName() + path.getId().replace('.', '_');
    }

    /**
     * If the evaluation contains failed paths.
     * 
     * @return 
     */
    public boolean hasFailedResult() {
        return failedCount > 0;
    }

    /**
     * Total number of paths for evaluation.
     * 
     * @return 
     */
    public int getTotalPaths() {
        return totalPaths;
    }

    /**
     * Number of processed paths.
     * 
     * @return 
     */
    public int getProcessedCount() {
        return processedCount;
    } 

    public void setLog(LogCategory log) {
        this.log = log;
    }
    
    
}
