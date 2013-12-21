
	 	/**
         IMPORTANT !!!

         DO NOT EDIT THIS FILE. IT IS GENERATED FROM approriate xml file in xmlresources/gbcommands BY
         THE JavaClassesGenerator.xslt. MODIFY THAT FILE INSTEAD OF THIS ONE.
         
         Use Ant task process-gb-messages after that to generate .java files again.
         
         IMPORTANT END !!!
        */
 	package cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands;import java.util.*;import javax.vecmath.*;import cz.cuni.amis.pogamut.base.communication.messages.*;import cz.cuni.amis.pogamut.base.communication.worldview.*;import cz.cuni.amis.pogamut.base.communication.worldview.event.*;import cz.cuni.amis.pogamut.base.communication.worldview.object.*;import cz.cuni.amis.pogamut.multi.communication.worldview.object.*;import cz.cuni.amis.pogamut.base.communication.translator.event.*;import cz.cuni.amis.pogamut.multi.communication.translator.event.*;import cz.cuni.amis.pogamut.base3d.worldview.object.*;import cz.cuni.amis.pogamut.base3d.worldview.object.event.*;import cz.cuni.amis.pogamut.ut2004.communication.messages.*;import cz.cuni.amis.pogamut.ut2004.communication.worldview.objects.*;import cz.cuni.amis.pogamut.ut2004multi.communication.worldview.objects.*;import cz.cuni.amis.pogamut.ut2004.communication.translator.itemdescriptor.*;import cz.cuni.amis.pogamut.ut2004.communication.messages.ItemType.Category;import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;import cz.cuni.amis.utils.exception.*;import cz.cuni.amis.pogamut.base.communication.translator.event.IWorldObjectUpdateResult.Result;import cz.cuni.amis.utils.SafeEquals;import cz.cuni.amis.pogamut.base.agent.*;import cz.cuni.amis.pogamut.multi.agent.*;import cz.cuni.amis.pogamut.multi.communication.worldview.property.*;import cz.cuni.amis.pogamut.ut2004multi.communication.worldview.property.*;import cz.cuni.amis.utils.token.*;import cz.cuni.amis.utils.*;
 		/**
 		 * Representation of the GameBots2004 command CONF.
 		 *
 		 * 
		Configures various attributes of the observer. You can include several of the parameters.
		The state of options you don't specify will remain unchanged.
    
         */
 	public class ConfigurationObserver 
		extends CommandMessage
	        {
	        	
		        
    	/** Example how the message looks like - used during parser tests. */
    	public static final String PROTOTYPE =
    		" {UpdateTime 0}  {Update 0}  {Game False}  {Self False}  {See False}  {Special False}  {All False}  {Async False} ";
    
		/**
		 * Creates new instance of command ConfigurationObserver.
		 * 
		Configures various attributes of the observer. You can include several of the parameters.
		The state of options you don't specify will remain unchanged.
    
		 * Corresponding GameBots message for this command is
		 * CONF.
		 *
		 * 
		 *    @param UpdateTime 
			The frequency (in seconds) you will receive updates from the observer.
		
		 *    @param Update 
			The frequency (in seconds) you will receive updates from the observer. Same as UpdateTime.
		
		 *    @param Game 
			Whether to send game-related messages with every update.
			Affects the following messages: NFO, PLR.
		
		 *    @param Self 
			Whether to send messages about the observed player with every update.
			Affects the following messages: SLF, MYINV.
		
		 *    @param See 
			Whether to send messages about what the observed player sees with every update.
			Affects the following messages: PLR, INV, NAV, MOV, PRJ, VEH.
		
		 *    @param Special 
			Whether to send messages about special objects on the map with every update.
			Affects the following messages: FLG, BOM, DOM.
		
		 *    @param All 
			Toggles sending all messages with every update.
            You can still receive the messages by asking for them using the commands GAME, SELF, SEE, SPECIAL and ALL.
		
		 *    @param Async 
			Whether to send asynchronous messages, such as HIT or AIN.
            Note that if you turn this off you will not be able to receive these messages at all.
		
		 */
		public ConfigurationObserver(
			Double UpdateTime,  Double Update,  Boolean Game,  Boolean Self,  Boolean See,  Boolean Special,  Boolean All,  Boolean Async
		) {
			
				this.UpdateTime = UpdateTime;
            
				this.Update = Update;
            
				this.Game = Game;
            
				this.Self = Self;
            
				this.See = See;
            
				this.Special = Special;
            
				this.All = All;
            
				this.Async = Async;
            
		}

		
			/**
			 * Creates new instance of command ConfigurationObserver.
			 * 
		Configures various attributes of the observer. You can include several of the parameters.
		The state of options you don't specify will remain unchanged.
    
			 * Corresponding GameBots message for this command is
			 * CONF.
			 * <p></p>
			 * WARNING: this is empty-command constructor, you have to use setters to fill it up with data that should be sent to GameBots2004!
		     */
		    public ConfigurationObserver() {
		    }
			
		
		/**
		 * Cloning constructor.
		 *
		 * @param original
		 */
		public ConfigurationObserver(ConfigurationObserver original) {
		   
		        this.UpdateTime = original.UpdateTime;
		   
		        this.Update = original.Update;
		   
		        this.Game = original.Game;
		   
		        this.Self = original.Self;
		   
		        this.See = original.See;
		   
		        this.Special = original.Special;
		   
		        this.All = original.All;
		   
		        this.Async = original.Async;
		   
		}
    
	        /**
	        
			The frequency (in seconds) you will receive updates from the observer.
		 
	        */
	        protected
	         Double UpdateTime =
	       	
	        		null
	        	;
	
	        
	        
 		/**
         * 
			The frequency (in seconds) you will receive updates from the observer.
		 
         */
        public Double getUpdateTime()
 	
	        {
	            return
	        	 UpdateTime;
	        }
	        
	        
	        
 		
 		/**
         * 
			The frequency (in seconds) you will receive updates from the observer.
		 
         */
        public ConfigurationObserver 
        setUpdateTime(Double UpdateTime)
 	
			{
				this.UpdateTime = UpdateTime;
				return this;
			}
		
	        /**
	        
			The frequency (in seconds) you will receive updates from the observer. Same as UpdateTime.
		 
	        */
	        protected
	         Double Update =
	       	
	        		null
	        	;
	
	        
	        
 		/**
         * 
			The frequency (in seconds) you will receive updates from the observer. Same as UpdateTime.
		 
         */
        public Double getUpdate()
 	
	        {
	            return
	        	 Update;
	        }
	        
	        
	        
 		
 		/**
         * 
			The frequency (in seconds) you will receive updates from the observer. Same as UpdateTime.
		 
         */
        public ConfigurationObserver 
        setUpdate(Double Update)
 	
			{
				this.Update = Update;
				return this;
			}
		
	        /**
	        
			Whether to send game-related messages with every update.
			Affects the following messages: NFO, PLR.
		 
	        */
	        protected
	         Boolean Game =
	       	
	        		null
	        	;
	
	        
	        
 		/**
         * 
			Whether to send game-related messages with every update.
			Affects the following messages: NFO, PLR.
		 
         */
        public Boolean isGame()
 	
	        {
	            return
	        	 Game;
	        }
	        
	        
	        
 		
 		/**
         * 
			Whether to send game-related messages with every update.
			Affects the following messages: NFO, PLR.
		 
         */
        public ConfigurationObserver 
        setGame(Boolean Game)
 	
			{
				this.Game = Game;
				return this;
			}
		
	        /**
	        
			Whether to send messages about the observed player with every update.
			Affects the following messages: SLF, MYINV.
		 
	        */
	        protected
	         Boolean Self =
	       	
	        		null
	        	;
	
	        
	        
 		/**
         * 
			Whether to send messages about the observed player with every update.
			Affects the following messages: SLF, MYINV.
		 
         */
        public Boolean isSelf()
 	
	        {
	            return
	        	 Self;
	        }
	        
	        
	        
 		
 		/**
         * 
			Whether to send messages about the observed player with every update.
			Affects the following messages: SLF, MYINV.
		 
         */
        public ConfigurationObserver 
        setSelf(Boolean Self)
 	
			{
				this.Self = Self;
				return this;
			}
		
	        /**
	        
			Whether to send messages about what the observed player sees with every update.
			Affects the following messages: PLR, INV, NAV, MOV, PRJ, VEH.
		 
	        */
	        protected
	         Boolean See =
	       	
	        		null
	        	;
	
	        
	        
 		/**
         * 
			Whether to send messages about what the observed player sees with every update.
			Affects the following messages: PLR, INV, NAV, MOV, PRJ, VEH.
		 
         */
        public Boolean isSee()
 	
	        {
	            return
	        	 See;
	        }
	        
	        
	        
 		
 		/**
         * 
			Whether to send messages about what the observed player sees with every update.
			Affects the following messages: PLR, INV, NAV, MOV, PRJ, VEH.
		 
         */
        public ConfigurationObserver 
        setSee(Boolean See)
 	
			{
				this.See = See;
				return this;
			}
		
	        /**
	        
			Whether to send messages about special objects on the map with every update.
			Affects the following messages: FLG, BOM, DOM.
		 
	        */
	        protected
	         Boolean Special =
	       	
	        		null
	        	;
	
	        
	        
 		/**
         * 
			Whether to send messages about special objects on the map with every update.
			Affects the following messages: FLG, BOM, DOM.
		 
         */
        public Boolean isSpecial()
 	
	        {
	            return
	        	 Special;
	        }
	        
	        
	        
 		
 		/**
         * 
			Whether to send messages about special objects on the map with every update.
			Affects the following messages: FLG, BOM, DOM.
		 
         */
        public ConfigurationObserver 
        setSpecial(Boolean Special)
 	
			{
				this.Special = Special;
				return this;
			}
		
	        /**
	        
			Toggles sending all messages with every update.
            You can still receive the messages by asking for them using the commands GAME, SELF, SEE, SPECIAL and ALL.
		 
	        */
	        protected
	         Boolean All =
	       	
	        		null
	        	;
	
	        
	        
 		/**
         * 
			Toggles sending all messages with every update.
            You can still receive the messages by asking for them using the commands GAME, SELF, SEE, SPECIAL and ALL.
		 
         */
        public Boolean isAll()
 	
	        {
	            return
	        	 All;
	        }
	        
	        
	        
 		
 		/**
         * 
			Toggles sending all messages with every update.
            You can still receive the messages by asking for them using the commands GAME, SELF, SEE, SPECIAL and ALL.
		 
         */
        public ConfigurationObserver 
        setAll(Boolean All)
 	
			{
				this.All = All;
				return this;
			}
		
	        /**
	        
			Whether to send asynchronous messages, such as HIT or AIN.
            Note that if you turn this off you will not be able to receive these messages at all.
		 
	        */
	        protected
	         Boolean Async =
	       	
	        		null
	        	;
	
	        
	        
 		/**
         * 
			Whether to send asynchronous messages, such as HIT or AIN.
            Note that if you turn this off you will not be able to receive these messages at all.
		 
         */
        public Boolean isAsync()
 	
	        {
	            return
	        	 Async;
	        }
	        
	        
	        
 		
 		/**
         * 
			Whether to send asynchronous messages, such as HIT or AIN.
            Note that if you turn this off you will not be able to receive these messages at all.
		 
         */
        public ConfigurationObserver 
        setAsync(Boolean Async)
 	
			{
				this.Async = Async;
				return this;
			}
		
 	    public String toString() {
            return toMessage();
        }
 	
 		public String toHtmlString() {
			return super.toString() + "[<br/>" +
            	
            	"<b>UpdateTime</b> = " +
            	String.valueOf(getUpdateTime()
 	) +
            	" <br/> " +
            	
            	"<b>Update</b> = " +
            	String.valueOf(getUpdate()
 	) +
            	" <br/> " +
            	
            	"<b>Game</b> = " +
            	String.valueOf(isGame()
 	) +
            	" <br/> " +
            	
            	"<b>Self</b> = " +
            	String.valueOf(isSelf()
 	) +
            	" <br/> " +
            	
            	"<b>See</b> = " +
            	String.valueOf(isSee()
 	) +
            	" <br/> " +
            	
            	"<b>Special</b> = " +
            	String.valueOf(isSpecial()
 	) +
            	" <br/> " +
            	
            	"<b>All</b> = " +
            	String.valueOf(isAll()
 	) +
            	" <br/> " +
            	
            	"<b>Async</b> = " +
            	String.valueOf(isAsync()
 	) +
            	" <br/> " +
            	 
            	"<br/>]"
            ;
		}
 	
		public String toMessage() {
     		StringBuffer buf = new StringBuffer();
     		buf.append("CONF");
     		
						if (UpdateTime != null) {
							buf.append(" {UpdateTime " + UpdateTime + "}");
						}
					
						if (Update != null) {
							buf.append(" {Update " + Update + "}");
						}
					
						if (Game != null) {
							buf.append(" {Game " + Game + "}");
						}
					
						if (Self != null) {
							buf.append(" {Self " + Self + "}");
						}
					
						if (See != null) {
							buf.append(" {See " + See + "}");
						}
					
						if (Special != null) {
							buf.append(" {Special " + Special + "}");
						}
					
						if (All != null) {
							buf.append(" {All " + All + "}");
						}
					
						if (Async != null) {
							buf.append(" {Async " + Async + "}");
						}
					
   			return buf.toString();
   		}
 	
 		// --- Extra Java from XML BEGIN (extra/code/java)
        	
		// --- Extra Java from XML END (extra/code/java)
 	
	        }
    	