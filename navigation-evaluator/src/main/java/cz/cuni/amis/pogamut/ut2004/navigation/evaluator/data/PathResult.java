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

/**
 * Result of evaluation on given path.
 *
 * @author Bogo
 */
public class PathResult {
    
    private Path path;
    private ResultType type;
    private long duration;
    
    public PathResult(Path path, ResultType type) {
        this(path, type, 0);
    }
    
    public PathResult(Path path, ResultType type, long duration) {
        this.path = path;
        this.type = type;
        this.duration = duration;
    }
    
    /**
     * Result types of evaluation.
     */
    public enum ResultType {
        NotBuilt, Failed, Completed
    }
    
    /**
     * Create CSV entry about this result.
     * @return 
     */
    public String export() {
        return String.format("%s;%s;%s;%s;%d", path.getId(), path.getStart().getId().getStringId(), path.getEnd().getId().getStringId(), type, duration);
    }
    
}
