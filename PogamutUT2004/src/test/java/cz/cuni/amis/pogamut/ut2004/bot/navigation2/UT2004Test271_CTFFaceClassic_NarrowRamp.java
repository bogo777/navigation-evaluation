package cz.cuni.amis.pogamut.ut2004.bot.navigation2;

import cz.cuni.amis.pogamut.ut2004.bot.UT2004BotTest;
import org.junit.Test;

/**
 *
 * @author Peta Michalik
 */
public class UT2004Test271_CTFFaceClassic_NarrowRamp extends UT2004BotTest {

	@Override
	protected String getMapName() {
		return "CTF-FaceClassic";
	}

	@Override
	protected String getGameType() {
		return "BotDeathMatch";
	}

        @Test
	public void test271_narrow_1_time() {
		startTest(
			// use NavigationTestBot for the test
			Navigation2TestBot.class,
			// timeout: 1 minute
			1,
			// test movement between        start: CTF-FaceClassic.InventorySpot114, end: CTF-FaceClassic.InventorySpot113 number of repetitions   both ways
			new Navigation2TestBotParameters("CTF-FaceClassic.InventorySpot114",      "CTF-FaceClassic.InventorySpot113",    1,                        true)
		);
	}

        /*
        * TODO: Test fails
        */
        @Test
	public void test271_narrow_20_time() {


		startTest(
			// use NavigationTestBot for the test
			Navigation2TestBot.class,
			// timeout: 10 minutes
			10,
			// test movement between        start: CTF-FaceClassic.InventorySpot114, end: CTF-FaceClassic.InventorySpot113 number of repetitions   both ways
			new Navigation2TestBotParameters("CTF-FaceClassic.InventorySpot114",      "CTF-FaceClassic.InventorySpot113",    20,                        true)
		);
	}

}
