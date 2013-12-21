
	 	/**
         IMPORTANT !!!

         DO NOT EDIT THIS FILE. IT IS GENERATED FROM approriate xml file in xmlresources/gbcommands BY
         THE JavaClassesGenerator.xslt. MODIFY THAT FILE INSTEAD OF THIS ONE.
         
         Use Ant task process-gb-messages after that to generate .java files again.
         
         IMPORTANT END !!!
        */
 	package cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands;import java.util.*;import javax.vecmath.*;import cz.cuni.amis.pogamut.base.communication.messages.*;import cz.cuni.amis.pogamut.base.communication.worldview.*;import cz.cuni.amis.pogamut.base.communication.worldview.event.*;import cz.cuni.amis.pogamut.base.communication.worldview.object.*;import cz.cuni.amis.pogamut.multi.communication.worldview.object.*;import cz.cuni.amis.pogamut.base.communication.translator.event.*;import cz.cuni.amis.pogamut.multi.communication.translator.event.*;import cz.cuni.amis.pogamut.base3d.worldview.object.*;import cz.cuni.amis.pogamut.base3d.worldview.object.event.*;import cz.cuni.amis.pogamut.ut2004.communication.messages.*;import cz.cuni.amis.pogamut.ut2004.communication.worldview.objects.*;import cz.cuni.amis.pogamut.ut2004multi.communication.worldview.objects.*;import cz.cuni.amis.pogamut.ut2004.communication.translator.itemdescriptor.*;import cz.cuni.amis.pogamut.ut2004.communication.messages.ItemType.Category;import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;import cz.cuni.amis.utils.exception.*;import cz.cuni.amis.pogamut.base.communication.translator.event.IWorldObjectUpdateResult.Result;import cz.cuni.amis.utils.SafeEquals;import cz.cuni.amis.pogamut.base.agent.*;import cz.cuni.amis.pogamut.multi.agent.*;import cz.cuni.amis.pogamut.multi.communication.worldview.property.*;import cz.cuni.amis.pogamut.ut2004multi.communication.worldview.property.*;import cz.cuni.amis.utils.token.*;import cz.cuni.amis.utils.*;
 		/**
 		 * Representation of the GameBots2004 command STARTPLRS.
 		 *
 		 * 
		Will start to export IPLR messages regularly (like synchronous
		batch). Can be used for continuous visualization of players
		moving around the map. There are three categories (see below).
		The default values for all category is true, that means that
		without attributes all the categories will be exported.
	
         */
 	public class StartPlayers 
		extends CommandMessage
	        {
	        	
		        
    	/** Example how the message looks like - used during parser tests. */
    	public static final String PROTOTYPE =
    		" {Humans False}  {GBBots False}  {UnrealBots False} ";
    
		/**
		 * Creates new instance of command StartPlayers.
		 * 
		Will start to export IPLR messages regularly (like synchronous
		batch). Can be used for continuous visualization of players
		moving around the map. There are three categories (see below).
		The default values for all category is true, that means that
		without attributes all the categories will be exported.
	
		 * Corresponding GameBots message for this command is
		 * STARTPLRS.
		 *
		 * 
		 *    @param Humans 
			All human players will be exported.
		
		 *    @param GBBots 
			All GameBots bots will be exported.
		
		 *    @param UnrealBots All UnrealBots will be exported.
		 */
		public StartPlayers(
			Boolean Humans,  Boolean GBBots,  Boolean UnrealBots
		) {
			
				this.Humans = Humans;
            
				this.GBBots = GBBots;
            
				this.UnrealBots = UnrealBots;
            
		}

		
			/**
			 * Creates new instance of command StartPlayers.
			 * 
		Will start to export IPLR messages regularly (like synchronous
		batch). Can be used for continuous visualization of players
		moving around the map. There are three categories (see below).
		The default values for all category is true, that means that
		without attributes all the categories will be exported.
	
			 * Corresponding GameBots message for this command is
			 * STARTPLRS.
			 * <p></p>
			 * WARNING: this is empty-command constructor, you have to use setters to fill it up with data that should be sent to GameBots2004!
		     */
		    public StartPlayers() {
		    }
			
		
		/**
		 * Cloning constructor.
		 *
		 * @param original
		 */
		public StartPlayers(StartPlayers original) {
		   
		        this.Humans = original.Humans;
		   
		        this.GBBots = original.GBBots;
		   
		        this.UnrealBots = original.UnrealBots;
		   
		}
    
	        /**
	        
			All human players will be exported.
		 
	        */
	        protected
	         Boolean Humans =
	       	
	        		null
	        	;
	
	        
	        
 		/**
         * 
			All human players will be exported.
		 
         */
        public Boolean isHumans()
 	
	        {
	            return
	        	 Humans;
	        }
	        
	        
	        
 		
 		/**
         * 
			All human players will be exported.
		 
         */
        public StartPlayers 
        setHumans(Boolean Humans)
 	
			{
				this.Humans = Humans;
				return this;
			}
		
	        /**
	        
			All GameBots bots will be exported.
		 
	        */
	        protected
	         Boolean GBBots =
	       	
	        		null
	        	;
	
	        
	        
 		/**
         * 
			All GameBots bots will be exported.
		 
         */
        public Boolean isGBBots()
 	
	        {
	            return
	        	 GBBots;
	        }
	        
	        
	        
 		
 		/**
         * 
			All GameBots bots will be exported.
		 
         */
        public StartPlayers 
        setGBBots(Boolean GBBots)
 	
			{
				this.GBBots = GBBots;
				return this;
			}
		
	        /**
	        All UnrealBots will be exported. 
	        */
	        protected
	         Boolean UnrealBots =
	       	
	        		null
	        	;
	
	        
	        
 		/**
         * All UnrealBots will be exported. 
         */
        public Boolean isUnrealBots()
 	
	        {
	            return
	        	 UnrealBots;
	        }
	        
	        
	        
 		
 		/**
         * All UnrealBots will be exported. 
         */
        public StartPlayers 
        setUnrealBots(Boolean UnrealBots)
 	
			{
				this.UnrealBots = UnrealBots;
				return this;
			}
		
 	    public String toString() {
            return toMessage();
        }
 	
 		public String toHtmlString() {
			return super.toString() + "[<br/>" +
            	
            	"<b>Humans</b> = " +
            	String.valueOf(isHumans()
 	) +
            	" <br/> " +
            	
            	"<b>GBBots</b> = " +
            	String.valueOf(isGBBots()
 	) +
            	" <br/> " +
            	
            	"<b>UnrealBots</b> = " +
            	String.valueOf(isUnrealBots()
 	) +
            	" <br/> " +
            	 
            	"<br/>]"
            ;
		}
 	
		public String toMessage() {
     		StringBuffer buf = new StringBuffer();
     		buf.append("STARTPLRS");
     		
						if (Humans != null) {
							buf.append(" {Humans " + Humans + "}");
						}
					
						if (GBBots != null) {
							buf.append(" {GBBots " + GBBots + "}");
						}
					
						if (UnrealBots != null) {
							buf.append(" {UnrealBots " + UnrealBots + "}");
						}
					
   			return buf.toString();
   		}
 	
 		// --- Extra Java from XML BEGIN (extra/code/java)
        	
		// --- Extra Java from XML END (extra/code/java)
 	
	        }
    	