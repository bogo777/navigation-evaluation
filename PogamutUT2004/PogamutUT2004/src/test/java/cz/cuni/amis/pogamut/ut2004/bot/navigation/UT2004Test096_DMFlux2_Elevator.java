package cz.cuni.amis.pogamut.ut2004.bot.navigation;

import cz.cuni.amis.pogamut.ut2004.bot.UT2004BotTest;
import org.junit.Test;

/**
 *
 * @author Peta Michalik
 */
public class UT2004Test096_DMFlux2_Elevator extends UT2004BotTest {

	@Override
	protected String getMapName() {
		return "DM-Flux2";
	}

	@Override
	protected String getGameType() {
		return "BotDeathMatch";
	}

        @Test
	public void test96_elevator_1_time() {
		startTest(
			// use NavigationTestBot for the test
			NavigationTestBot.class,
			// timeout: 1 minute
			1,
			// test movement between        start: DM-Flux2.InventorySpot55, end: DM-Flux2.InventorySpot56 number of repetitions   both ways
			new NavigationTestBotParameters("DM-Flux2.InventorySpot55",      "DM-Flux2.InventorySpot56",    1,                        false)
		);
	}

        @Test
	public void test96_elevator_20_time() {


		startTest(
			// use NavigationTestBot for the test
			NavigationTestBot.class,
			// timeout: 4 minutes
			4,
			// test movement between        start: DM-Flux2.InventorySpot55, end: DM-Flux2.InventorySpot56 number of repetitions   both ways
			new NavigationTestBotParameters("DM-Flux2.InventorySpot55",      "DM-Flux2.InventorySpot56",    20,                        false)
		);
	}

}