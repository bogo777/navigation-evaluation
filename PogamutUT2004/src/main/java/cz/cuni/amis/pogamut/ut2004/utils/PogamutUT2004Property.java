package cz.cuni.amis.pogamut.ut2004.utils;

import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.visibility.model.VisibilityMatrix;

public enum PogamutUT2004Property {
	/**
	 * Where the bot should connect to (hostname of the server running
	 * GameBots2004)
	 */
	POGAMUT_UT2004_BOT_HOST("pogamut.ut2004.bot.host"),

	/**
	 * Where the bot should connect to (bot port of the GameBots2004).
	 */
	POGAMUT_UT2004_BOT_PORT("pogamut.ut2004.bot.port"),

	/**
	 * Where the server should connect to (hostname of the server running
	 * GameBots2004)
	 */
	POGAMUT_UT2004_SERVER_HOST("pogamut.ut2004.server.host"),

	/**
	 * Where the server should connect to (server port of the GameBots2004)
	 */
	POGAMUT_UT2004_SERVER_PORT("pogamut.ut2004.server.port"),

	/**
	 * Where the observer should connect to (hostname of the server running
	 * GameBots2004)
	 */
	POGAMUT_UT2004_OBSERVER_HOST("pogamut.ut2004.observer.host"),

	/**
	 * Where the observer should connect to (observer port of the GameBots2004)
	 */
	POGAMUT_UT2004_OBSERVER_PORT("pogamut.ut2004.observer.port"),

	/** Path to the Unreal home dir. */
	POGAMUT_UNREAL_HOME("pogamut.ut2004.home"),

	/** Should tests use external UCC instance or they will run internal one? */
	POGAMUT_UNREAL_TEST_EXT_SERVER("pogamut.test.useExternalUCC"), 
	
	/**
	 * Whether UT2004PathExecutor is using SetRoute command (causes RED LINE to appear in UT2004 GUI when enablind "display bot routes").
	 */
	POGAMUT_UT2004_PATH_EXECUTOR_SEND_SET_ROUTE("pogamut.ut2004.path_executor.send_set_route"),
	
	/**
	 * Directory where to search for {@link VisibilityMatrix}es for respective maps.
	 */
	POGAMUT_UT2004_VISIBILITY_DIRECTORY("pogamut.ut2004.visibility.dir");

	private String key;

	private PogamutUT2004Property(String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}

	public String toString() {
		return key;
	}
}
