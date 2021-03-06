
	 	/**
         IMPORTANT !!!

         DO NOT EDIT THIS FILE. IT IS GENERATED FROM approriate xml file in xmlresources/gbcommands BY
         THE JavaClassesGenerator.xslt. MODIFY THAT FILE INSTEAD OF THIS ONE.
         
         Use Ant task process-gb-messages after that to generate .java files again.
         
         IMPORTANT END !!!
        */
 	package cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands;import java.util.*;import javax.vecmath.*;import cz.cuni.amis.pogamut.base.communication.messages.*;import cz.cuni.amis.pogamut.base.communication.worldview.*;import cz.cuni.amis.pogamut.base.communication.worldview.event.*;import cz.cuni.amis.pogamut.base.communication.worldview.object.*;import cz.cuni.amis.pogamut.multi.communication.worldview.object.*;import cz.cuni.amis.pogamut.base.communication.translator.event.*;import cz.cuni.amis.pogamut.multi.communication.translator.event.*;import cz.cuni.amis.pogamut.base3d.worldview.object.*;import cz.cuni.amis.pogamut.base3d.worldview.object.event.*;import cz.cuni.amis.pogamut.ut2004.communication.messages.*;import cz.cuni.amis.pogamut.ut2004.communication.worldview.objects.*;import cz.cuni.amis.pogamut.ut2004multi.communication.worldview.objects.*;import cz.cuni.amis.pogamut.ut2004.communication.translator.itemdescriptor.*;import cz.cuni.amis.pogamut.ut2004.communication.messages.ItemType.Category;import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;import cz.cuni.amis.utils.exception.*;import cz.cuni.amis.pogamut.base.communication.translator.event.IWorldObjectUpdateResult.Result;import cz.cuni.amis.utils.SafeEquals;import cz.cuni.amis.pogamut.base.agent.*;import cz.cuni.amis.pogamut.multi.agent.*;import cz.cuni.amis.pogamut.multi.communication.worldview.property.*;import cz.cuni.amis.pogamut.ut2004multi.communication.worldview.property.*;import cz.cuni.amis.utils.token.*;import cz.cuni.amis.utils.*;
 		/**
 		 * Representation of the GameBots2004 command GIVE.
 		 *
 		 * 
		Bot gives an item to other bot (regardless of the automatic pickup settings). Note that other bot
		must be within reach - less than 200 ut units away (approx. 2 meters). Bot receives message GIVERES
		with the result of this command - if it was successfull or not.
	
         */
 	public class GiveItem 
		extends CommandMessage
	        {
	        	
		        
    	/** Example how the message looks like - used during parser tests. */
    	public static final String PROTOTYPE =
    		" {Id unreal_id}  {Type text} ";
    
		/**
		 * Creates new instance of command GiveItem.
		 * 
		Bot gives an item to other bot (regardless of the automatic pickup settings). Note that other bot
		must be within reach - less than 200 ut units away (approx. 2 meters). Bot receives message GIVERES
		with the result of this command - if it was successfull or not.
	
		 * Corresponding GameBots message for this command is
		 * GIVE.
		 *
		 * 
		 *    @param Id 
			Id of the bot we want to give item to.
		
		 *    @param Type 
			Class of the item, e.g. "GBEmohawk.ItemBall" (without quotes) we want to give to the bot.
		
		 */
		public GiveItem(
			UnrealId Id,  String Type
		) {
			
				this.Id = Id;
            
				this.Type = Type;
            
		}

		
			/**
			 * Creates new instance of command GiveItem.
			 * 
		Bot gives an item to other bot (regardless of the automatic pickup settings). Note that other bot
		must be within reach - less than 200 ut units away (approx. 2 meters). Bot receives message GIVERES
		with the result of this command - if it was successfull or not.
	
			 * Corresponding GameBots message for this command is
			 * GIVE.
			 * <p></p>
			 * WARNING: this is empty-command constructor, you have to use setters to fill it up with data that should be sent to GameBots2004!
		     */
		    public GiveItem() {
		    }
			
		
		/**
		 * Cloning constructor.
		 *
		 * @param original
		 */
		public GiveItem(GiveItem original) {
		   
		        this.Id = original.Id;
		   
		        this.Type = original.Type;
		   
		}
    
	        /**
	        
			Id of the bot we want to give item to.
		 
	        */
	        protected
	         UnrealId Id =
	       	
	        		null
	        	;
	
	        
	        
 		/**
         * 
			Id of the bot we want to give item to.
		 
         */
        public UnrealId getId()
 	
	        {
	            return
	        	 Id;
	        }
	        
	        
	        
 		
 		/**
         * 
			Id of the bot we want to give item to.
		 
         */
        public GiveItem 
        setId(UnrealId Id)
 	
			{
				this.Id = Id;
				return this;
			}
		
	        /**
	        
			Class of the item, e.g. "GBEmohawk.ItemBall" (without quotes) we want to give to the bot.
		 
	        */
	        protected
	         String Type =
	       	
	        		null
	        	;
	
	        
	        
 		/**
         * 
			Class of the item, e.g. "GBEmohawk.ItemBall" (without quotes) we want to give to the bot.
		 
         */
        public String getType()
 	
	        {
	            return
	        	 Type;
	        }
	        
	        
	        
 		
 		/**
         * 
			Class of the item, e.g. "GBEmohawk.ItemBall" (without quotes) we want to give to the bot.
		 
         */
        public GiveItem 
        setType(String Type)
 	
			{
				this.Type = Type;
				return this;
			}
		
 	    public String toString() {
            return toMessage();
        }
 	
 		public String toHtmlString() {
			return super.toString() + "[<br/>" +
            	
            	"<b>Id</b> = " +
            	String.valueOf(getId()
 	) +
            	" <br/> " +
            	
            	"<b>Type</b> = " +
            	String.valueOf(getType()
 	) +
            	" <br/> " +
            	 
            	"<br/>]"
            ;
		}
 	
		public String toMessage() {
     		StringBuffer buf = new StringBuffer();
     		buf.append("GIVE");
     		
						if (Id != null) {
							buf.append(" {Id " + Id.getStringId() + "}");
						}
					
						if (Type != null) {
							buf.append(" {Type " + Type + "}");
						}
					
   			return buf.toString();
   		}
 	
 		// --- Extra Java from XML BEGIN (extra/code/java)
        	
		// --- Extra Java from XML END (extra/code/java)
 	
	        }
    	