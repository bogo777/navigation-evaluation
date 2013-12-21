package cz.cuni.amis.pogamut.ut2004.agent.module.logic;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.inject.Inject;

import cz.cuni.amis.pogamut.base.agent.module.IAgentLogic;
import cz.cuni.amis.pogamut.base.communication.command.ICommandListener;
import cz.cuni.amis.pogamut.base.communication.worldview.object.IWorldObjectEvent;
import cz.cuni.amis.pogamut.base.communication.worldview.react.EventReact;
import cz.cuni.amis.pogamut.base.communication.worldview.react.ObjectEventReact;
import cz.cuni.amis.pogamut.base.component.controller.ComponentDependencies;
import cz.cuni.amis.pogamut.base.component.controller.ComponentDependencyType;
import cz.cuni.amis.pogamut.base3d.ILockableVisionWorldView;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004Bot;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Respawn;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.ConfigChange;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.EndMessage;

public class SyncUT2004BotLogic<BOT extends UT2004Bot<? extends ILockableVisionWorldView, ?, ?>> extends UT2004BotLogic<BOT> {

	private ObjectEventReact<ConfigChange, ?> configChangeReaction;
	
	private EventReact<EndMessage>        endReactionAfterRespawn;
	private int						      shouldExecuteLogicLatch = 0;
	
	private ICommandListener<Respawn> respawnListener = new ICommandListener<Respawn>() {		

		@Override
		public void notify(Respawn event) {			
			synchronized(respawnListener) {
				endReactionAfterRespawn.enable();
				shouldExecuteLogicLatch = 2;
			}
		}
		
	};
	
	@Inject
	public SyncUT2004BotLogic(BOT agent, IAgentLogic logic) {
		this(agent, logic, null, new ComponentDependencies(ComponentDependencyType.STARTS_AFTER).add(agent.getWorldView()));
	}
	
	public SyncUT2004BotLogic(BOT agent, IAgentLogic logic, Logger log) {
		this(agent, logic, log, new ComponentDependencies(ComponentDependencyType.STARTS_AFTER).add(agent.getWorldView()));
	}
	
	public SyncUT2004BotLogic(BOT agent, IAgentLogic logic, Logger log, ComponentDependencies dependencies) {
		super(agent, logic, log, dependencies);
		endReactionAfterRespawn = new EventReact<EndMessage>(EndMessage.class, agent.getWorldView()) {
			@Override
			protected void react(EndMessage event) {
				synchronized(respawnListener) {
					if (shouldExecuteLogicLatch > 0) {
						--shouldExecuteLogicLatch;
					}
				}
			}
		};
		agent.getAct().addCommandListener(Respawn.class, respawnListener);
		
		configChangeReaction = new ObjectEventReact<ConfigChange, IWorldObjectEvent<ConfigChange>>(ConfigChange.class, agent.getWorldView()) {
			@Override
			protected void react(IWorldObjectEvent<ConfigChange> event) {
				setLogicFrequency(1 / (Math.max(0.05, event.getObject().getVisionTime() - 0.049)));
			}
		};
	}
	
	@Override
	protected void beforeLogic(String threadName) {
		super.beforeLogic(threadName);
		if (log.isLoggable(Level.FINEST)) log.finest(threadName + ": Locking world view.");
		agent.getWorldView().lock();
		if (log.isLoggable(Level.FINER)) log.finer(threadName + ": World view locked.");
	}
	
	@Override
	protected void afterLogic(String threadName) {
		super.afterLogic(threadName);
		if (log.isLoggable(Level.FINEST)) log.finest(threadName + ": Unlocking world view.");
		agent.getWorldView().unlock();
		if (log.isLoggable(Level.FINER)) log.finer(threadName + ": World view unlocked.");
	}
	
	@Override
	protected void afterLogicException(String threadName, Throwable e) {
		super.afterLogicException(threadName, e);
		if (agent.getWorldView().isLocked()) {
			if (log.isLoggable(Level.FINEST)) log.finest("Unlocking world view.");
			agent.getWorldView().unlock();
			if (log.isLoggable(Level.FINER)) log.finer("World view unlocked.");
		}
	}
	
	@Override
	protected boolean shouldExecuteLogic() {
		synchronized(respawnListener) {
			if (shouldExecuteLogicLatch != 0) {
				if (log.isLoggable(Level.INFO)) log.info("Respawn command sensed - waiting for the bot respawn to execute logic with correct world view state.");
				return false;
			} else {
				return true;
			}
		}			
	}
	
}
