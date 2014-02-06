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
import cz.cuni.amis.pogamut.ut2004.navigation.evaluator.data.EvaluationRepeatTask;
import cz.cuni.amis.pogamut.ut2004.navigation.evaluator.data.IEvaluationTask;
import cz.cuni.amis.pogamut.ut2004.navigation.evaluator.data.RecordType;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Navigation parameters for creating custom navigation in
 * {@link NavigationFactory}.
 *
 * @author Bogo
 */
public class BotNavigationParameters extends UT2004BotParameters {

    private IEvaluationTask task;

    public BotNavigationParameters(IEvaluationTask task) {
        this.task = task;
    }

    /**
     * Get path to unique and existing directory for saving of evaluation
     * results.
     *
     * TODO: Move somewhere?!
     *
     * @return
     *
     */
    public String getResultPath() {
        String basePath = String.format("%s/%s_%s/%s", task.getResultPath(), task.getNavigation(), task.getPathPlanner(), task.getMapName());
        SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyy_HHmmss");
        String fullPath = String.format("%s/%s/", basePath, dateFormat.format(new Date()));
        File resultFile = new File(fullPath);
        resultFile.mkdirs();

        return fullPath;
    }

//    public BotNavigationParameters setResultPath(String resultPath) {
//        this.task.setResultPath(resultPath);
//        return this;
//    }
    public boolean isOnlyRelevantPaths() {
        return task.isOnlyRelevantPaths();
    }

//    public BotNavigationParameters setOnlyRelevantPaths(boolean onlyRelevantPaths) {
//        this.task.setOnlyRelevantPaths(onlyRelevantPaths);
//        return this;
//    }
    public int getLimit() {
        return task.getLimit();
    }

//    public BotNavigationParameters setLimit(int limit) {
//        this.task.setLimit(limit);
//        return this;
//    }
    public String getPathPlanner() {
        return task.getPathPlanner();
    }

    /**
     * Sets path planner for given bot. Supported values: fwMap, navMesh
     *
     * @param pathPlanner
     * @return
     *
     */
//    public BotNavigationParameters setPathPlanner(String pathPlanner) {
//        this.task.setPathPlanner(pathPlanner);
//        return this;
//    }
    public String getNavigation() {
        return task.getNavigation();
    }

    /**
     * Sets navigation for given bot. Supported values:
     *
     * @param navigation
     * @return
     */
//    public BotNavigationParameters setNavigation(String navigation) {
//        this.task.setNavigation(navigation);
//        return this;
//    }
    public boolean getResultUnique() {
        return task.isResultUnique();
    }

//    public BotNavigationParameters setResultUnique(boolean resultUnique) {
//        this.task.setResultUnique(resultUnique);
//        return this;
//    }
    public boolean isRepeatTask() {
        return task instanceof EvaluationRepeatTask;
    }

    public String getRepeatFile() {
        if (!isRepeatTask()) {
            return null;
        } else {
            return ((EvaluationRepeatTask) task).getRepeatFile();
        }

    }

    public int getLimitForCompare() {
        return task.getLimit() < 0 ? Integer.MAX_VALUE : task.getLimit();
    }

    public String getRecordPath() {
        return String.format("%s_%s_record", getNavigation(), getPathPlanner());
    }

    public RecordType getRecordType() {
        return task.getRecordType();
    }

    boolean isPathRecord() {
        return task.getRecordType() == RecordType.PATH || task.getRecordType() == RecordType.PATH_FAILED;
    }

    boolean isFullRecord() {
        return task.getRecordType() == RecordType.FULL || task.getRecordType() == RecordType.FULL_FAILED;
    }

    boolean keepOnlyFailedRecords() {
        return task.getRecordType() == RecordType.PATH_FAILED || task.getRecordType() == RecordType.FULL_FAILED;
    }
}
