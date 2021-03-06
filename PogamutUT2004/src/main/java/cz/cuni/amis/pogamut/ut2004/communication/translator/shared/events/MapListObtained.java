package cz.cuni.amis.pogamut.ut2004.communication.translator.shared.events;


import java.util.List;

import cz.cuni.amis.pogamut.base.communication.translator.event.WorldEventIdentityWrapper;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.MapList;


/**
 * TODO implement the FSM logic for this event
 * @author ik
 */

public class MapListObtained  extends TranslatorEvent {

	private List<MapList> maps;

	public MapListObtained(List<MapList> list, long simTime) {
		super(simTime);
		this.maps = list;
	}

	public List<MapList> getMaps() {
		return maps;
	}

	@Override
	public String toString() {
		return "MapListObtained[maps.size() = " + maps.size() + "]";
	}

}
