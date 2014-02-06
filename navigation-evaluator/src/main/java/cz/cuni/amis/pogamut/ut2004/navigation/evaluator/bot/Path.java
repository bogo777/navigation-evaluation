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
package cz.cuni.amis.pogamut.ut2004.navigation.evaluator.bot;

import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;

/**
 * Represents path for evaluation. Contains start and end point.
 *
 * @author Bogo
 */
public class Path {
    
    private NavPoint start;
    private NavPoint end;
    
    /**
     * Creates path with given start and end point.
     * @param start Start point of path.
     * @param end End point of path.
     * 
     */
    public Path(NavPoint start, NavPoint end) {
        this.start = start;
        this.end = end;
    }
    
    public NavPoint getStart() {
        return start;
    }
    
    public NavPoint getEnd() {
        return end;
    }

    /**
     * Get ID of the path. ID is in format [ID of start]-[ID of end].
     * @return ID of this path.
     * 
     */
    public String getId() {
        return String.format("%s-%s", start.getId().toString(), end.getId().toString());
    }
    
}
