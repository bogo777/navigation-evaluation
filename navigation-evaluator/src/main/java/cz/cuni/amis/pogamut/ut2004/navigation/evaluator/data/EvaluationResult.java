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

import cz.cuni.amis.pogamut.ut2004.navigation.evaluator.bot.Path;
import cz.cuni.amis.pogamut.base.utils.logging.LogCategory;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
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
    private int proccessedCount = 0;
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
        ++proccessedCount;
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
        }
        pathResults.add(new PathResult(path, type, duration));
    }

    /**
     * Exports aggregate statistics about evaluation. TODO: Create unique files
     * on request?
     */
    public void exportAggregate(boolean uniqueFile) {
        FileWriter fstream = null;
        try {
            String fileName = mapName + ".aggregate.csv";
            if(uniqueFile) {
                File file = new File(resultPath + fileName);
                while(file.exists()) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyy_HHmmss");
                    fileName = String.format("%s_%s.aggregate.csv", mapName, dateFormat.format(new Date()));
                    file = new File(fileName);
                }
            }
            String fullFilePath = resultPath + fileName;
            fstream = new FileWriter(fullFilePath);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write("Map;Total;Processes;Completed;Failed;NotBuilt");
            out.newLine();
            out.write(String.format("%s;%d;%d;%d;%d;%d", mapName, totalPaths, proccessedCount, completedCount, failedCount, notBuiltCount));
            out.newLine();
            out.close();
        } catch (IOException ex) {
            log.warning(ex.getMessage());
        } finally {
            try {
                fstream.close();
            } catch (IOException ex) {
                log.warning(ex.getMessage());
            }
        }

    }
    
    public void exportAggregate() {
        exportAggregate(false);
    }

    /**
     * Export complete statistics about evaluation. TODO: Create unique files on
     * request?
     */
    public void export(boolean uniqueFile) {
        FileWriter fstream = null;
        try {
            String fileName = mapName + ".csv";
            if(uniqueFile) {
                File file = new File(resultPath + fileName);
                while(file.exists()) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyy_HHmmss");
                    fileName = String.format("%s_%s.csv", mapName, dateFormat.format(new Date()));
                    file = new File(fileName);
                }
            }
            String fullFilePath = resultPath + fileName;
            fstream = new FileWriter(fullFilePath);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write("ID;From;To;Type;Duration");
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
    
    public void export() {
        export(false);
    }
}
