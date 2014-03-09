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
package cz.cuni.amis.pogamut.ut2004.navigation.evaluator;

import java.io.File;

/**
 *
 * @author Bogo
 */
public class FileNames {

    public static final String DATA_FILE = "data.csv";
    public static final String DATA_AGG_FILE = "data.aggregate.csv";
    public static final String PATH_CONTAINER_FILE = "pathcontainer.csv";
    public static final String LOG_FILE = "log.log";

    public static String joinPath(String dir, String file) {
        if (dir.endsWith("/") || dir.endsWith("\\")) {
            return dir + file;
        } else {
            return String.format("%s/%s", dir, file);
        }
    }

    public static File getFile(String dir, String file) {
        return new File(joinPath(dir, file));
    }

    public static File getFile(File dir, String file) {
        return new File(joinPath(dir.getAbsolutePath(), file));
    }

}
