
	 	/**
         IMPORTANT !!!

         DO NOT EDIT THIS FILE. IT IS GENERATED FROM approriate xml file in xmlresources/gbcommands BY
         THE JavaClassesGenerator.xslt. MODIFY THAT FILE INSTEAD OF THIS ONE.
         
         Use Ant task process-gb-messages after that to generate .java files again.
         
         IMPORTANT END !!!
        */
 	package cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands;import java.util.*;import javax.vecmath.*;import cz.cuni.amis.pogamut.base.communication.messages.*;import cz.cuni.amis.pogamut.base.communication.worldview.*;import cz.cuni.amis.pogamut.base.communication.worldview.event.*;import cz.cuni.amis.pogamut.base.communication.worldview.object.*;import cz.cuni.amis.pogamut.multi.communication.worldview.object.*;import cz.cuni.amis.pogamut.base.communication.translator.event.*;import cz.cuni.amis.pogamut.multi.communication.translator.event.*;import cz.cuni.amis.pogamut.base3d.worldview.object.*;import cz.cuni.amis.pogamut.base3d.worldview.object.event.*;import cz.cuni.amis.pogamut.ut2004.communication.messages.*;import cz.cuni.amis.pogamut.ut2004.communication.worldview.objects.*;import cz.cuni.amis.pogamut.ut2004multi.communication.worldview.objects.*;import cz.cuni.amis.pogamut.ut2004.communication.translator.itemdescriptor.*;import cz.cuni.amis.pogamut.ut2004.communication.messages.ItemType.Category;import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;import cz.cuni.amis.utils.exception.*;import cz.cuni.amis.pogamut.base.communication.translator.event.IWorldObjectUpdateResult.Result;import cz.cuni.amis.utils.SafeEquals;import cz.cuni.amis.pogamut.base.agent.*;import cz.cuni.amis.pogamut.multi.agent.*;import cz.cuni.amis.pogamut.multi.communication.worldview.property.*;import cz.cuni.amis.pogamut.ut2004multi.communication.worldview.property.*;import cz.cuni.amis.utils.token.*;import cz.cuni.amis.utils.*;
 		/**
 		 * Representation of the GameBots2004 command GIVEINV.
 		 *
 		 * 
		Gives inventory from one bot to another. Bot can give just owned
		items. (in his inventory chain). He can't give weapon he is
		wielding right now. This command is not fully tested yet and has
		issues.
	
         */
 	public class GiveInventory 
		extends CommandMessage
	        {
	        	
		        
    	/** Example how the message looks like - used during parser tests. */
    	public static final String PROTOTYPE =
    		" {Target unreal_id}  {ItemId text} ";
    
		/**
		 * Creates new instance of command GiveInventory.
		 * 
		Gives inventory from one bot to another. Bot can give just owned
		items. (in his inventory chain). He can't give weapon he is
		wielding right now. This command is not fully tested yet and has
		issues.
	
		 * Corresponding GameBots message for this command is
		 * GIVEINV.
		 *
		 * 
		 *    @param Target Id of the receiving bot.
		 *    @param ItemId 
			Id of the item in the inventory chain of the giver.
		
		 */
		public GiveInventory(
			UnrealId Target,  String ItemId
		) {
			
				this.Target = Target;
            
				this.ItemId = ItemId;
            
		}

		
			/**
			 * Creates new instance of command GiveInventory.
			 * 
		Gives inventory from one bot to another. Bot can give just owned
		items. (in his inventory chain). He can't give weapon he is
		wielding right now. This command is not fully tested yet and has
		issues.
	
			 * Corresponding GameBots message for this command is
			 * GIVEINV.
			 * <p></p>
			 * WARNING: this is empty-command constructor, you have to use setters to fill it up with data that should be sent to GameBots2004!
		     */
		    public GiveInventory() {
		    }
			
		
		/**
		 * Cloning constructor.
		 *
		 * @param original
		 */
		public GiveInventory(GiveInventory original) {
		   
		        this.Target = original.Target;
		   
		        this.ItemId = original.ItemId;
		   
		}
    
	        /**
	        Id of the receiving bot. 
	        */
	        protected
	         UnrealId Target =
	       	
	        		null
	        	;
	
	        
	        
 		/**
         * Id of the receiving bot. 
         */
        public UnrealId getTarget()
 	
	        {
	            return
	        	 Target;
	        }
	        
	        
	        
 		
 		/**
         * Id of the receiving bot. 
         */
        public GiveInventory 
        setTarget(UnrealId Target)
 	
			{
				this.Target = Target;
				return this;
			}
		
	        /**
	        
			Id of the item in the inventory chain of the giver.
		 
	        */
	        protected
	         String ItemId =
	       	
	        		null
	        	;
	
	        
	        
 		/**
         * 
			Id of the item in the inventory chain of the giver.
		 
         */
        public String getItemId()
 	
	        {
	            return
	        	 ItemId;
	        }
	        
	        
	        
 		
 		/**
         * 
			Id of the item in the inventory chain of the giver.
		 
         */
        public GiveInventory 
        setItemId(String ItemId)
 	
			{
				this.ItemId = ItemId;
				return this;
			}
		
 	    public String toString() {
            return toMessage();
        }
 	
 		public String toHtmlString() {
			return super.toString() + "[<br/>" +
            	
            	"<b>Target</b> = " +
            	String.valueOf(getTarget()
 	) +
            	" <br/> " +
            	
            	"<b>ItemId</b> = " +
            	String.valueOf(getItemId()
 	) +
            	" <br/> " +
            	 
            	"<br/>]"
            ;
		}
 	
		public String toMessage() {
     		StringBuffer buf = new StringBuffer();
     		buf.append("GIVEINV");
     		
						if (Target != null) {
							buf.append(" {Target " + Target.getStringId() + "}");
						}
					
						if (ItemId != null) {
							buf.append(" {ItemId " + ItemId + "}");
						}
					
   			return buf.toString();
   		}
 	
 		// --- Extra Java from XML BEGIN (extra/code/java)
        	
		// --- Extra Java from XML END (extra/code/java)
 	
	        }
    	