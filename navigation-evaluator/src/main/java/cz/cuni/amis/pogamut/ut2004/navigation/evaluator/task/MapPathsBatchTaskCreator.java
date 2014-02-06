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
package cz.cuni.amis.pogamut.ut2004.navigation.evaluator.task;

import cz.cuni.amis.pogamut.ut2004.navigation.evaluator.task.MapPathsEvaluationTask.PathType;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Helper class for creating batches of {@link MapPathsEvaluationTask}s.
 *
 * @author Bogo
 */
public class MapPathsBatchTaskCreator {
    
    private int batchNumber;
    private List<String> maps;
    private String navigation;
    private String pathPlanner;
    private String resultPath;
    private List<PathType> pathTypes;
    
    /**
     * Default constructor.
     * 
     * @param batchNumber
     * @param maps
     * @param navigation
     * @param pathPlanner
     * @param resultPath
     * @param pathTypes 
     */
    public MapPathsBatchTaskCreator(int batchNumber, List<String> maps, String navigation, String pathPlanner, String resultPath, List<PathType> pathTypes) {
        this.batchNumber = batchNumber;
        this.maps = maps;
        this.navigation = navigation;
        this.pathPlanner = pathPlanner;
        this.resultPath = resultPath;
        this.pathTypes = pathTypes;
    }
    
    /**
     * Create batch of tasks.
     * 
     * @return 
     */
    public List<MapPathsEvaluationTask> createBatch() {
        List<MapPathsEvaluationTask> tasks = new LinkedList<MapPathsEvaluationTask>();
        
        for (String map : maps) {
            for (PathType type : pathTypes) {
                tasks.add(new MapPathsEvaluationTask(map, navigation, pathPlanner, resultPath, type, batchNumber));
            }
        }
        
        return tasks;
    }
    
    public static List<String> getAllDMMaps() {
        return Arrays.asList(DM_MAPS);
    }
    
    public static List<String> getAllCTFMaps() {
        return Arrays.asList(CTF_MAPS);
    }
    
    public static List<String> getAllMaps() {
        LinkedList<String> all = new LinkedList<String>(getAllDMMaps());
        all.addAll(getAllCTFMaps());
        return all;
    }
    
    public static final String[] CTF_MAPS = new String[] {
		"CTF-1on1-Joust",
		"CTF-AbsoluteZero",
		"CTF-Avaris",
		"CTF-BridgeOfFate",
		"CTF-Chrome",
		"CTF-Citadel",
		"CTF-Colossus",
		"CTF-December",
		"CTF-DE-ElecFields",
		"CTF-DoubleDammage",
		"CTF-Face3",
		"CTF-FaceClassic",
		"CTF-Geothermal",
		"CTF-Grassyknoll",
		"CTF-Grendelkeep",
		"CTF-January",
		"CTF-Lostfaith",
		"CTF-Magma",
		"CTF-Maul",
		"CTF-MoonDragon",
		"CTF-Orbital2",
		"CTF-Smote",
		"CTF-TwinTombs"
	};
	
	public static String[] DM_MAPS = new String[] {
		"DM-1on1-Albatross",
		"DM-1on1-Crash",
		"DM-1on1-Desolation",
		"DM-1on1-Idoma",
		"DM-1on1-Irondust",
		"DM-1on1-Mixer",
		"DM-1on1-Roughinery",
		"DM-1on1-Serpen-tine",
		"DM-1on1-Spirit",
		"DM-1on1-Squader",
		"DM-1on1-Trite",
		"DM-Antalus",
		"DM-Asbestos",
		"DM-Compressed",
		"DM-Corrugation",
		"DM-Curse4",
		"DM-Deck17",
		"DM-DE-Grendelkeep",
		"DM-DE-Ironic",
		"DM-DE-Osiris2",
		"DM-DesertIsle",
		"DM-Flux2",
		"DM-Gael",
		"DM-Gestalt",
		"DM-Goliath",
		"DM-HyperBlast2",
		"DM-Icetomb",
		"DM-Inferno",
		"DM-Injector",
		"DM-Insidious",
		"DM-IronDeity",
		"DM-Junkyard",
		"DM-Leviathan",
		"DM-Metallurgy",
		"DM-Morpheus3",
		"DM-Oceanic",
		"DM-Phobos2",
		"DM-Plunge",
		"DM-Rankin",
		"DM-Rrajigar",
		"DM-Rustatorium",
		"DM-Sulphur",
		"DM-TokaraForest",
		"DM-TrainingDay"
	};
}
