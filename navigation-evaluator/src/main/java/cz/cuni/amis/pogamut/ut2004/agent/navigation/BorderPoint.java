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

package cz.cuni.amis.pogamut.ut2004.agent.navigation;

import cz.cuni.amis.pogamut.base3d.worldview.object.Location;

/**
 *
 * @author Bogo
 */
public class BorderPoint {
    
    private Location point;
    private Location direction;

    public BorderPoint(Location point, Location direction) {
        this.point = point;
        this.direction = direction;
    }

    public Location getPoint() {
        return point;
    }

    public Location getDirection() {
        return direction;
    }

    public void setPoint(Location point) {
        this.point = point;
    }

    public void setDirection(Location direction) {
        this.direction = direction;
    }
    
    
    
}
