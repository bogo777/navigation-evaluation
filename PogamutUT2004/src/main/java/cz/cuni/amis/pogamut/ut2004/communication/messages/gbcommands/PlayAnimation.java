
	 	/**
         IMPORTANT !!!

         DO NOT EDIT THIS FILE. IT IS GENERATED FROM approriate xml file in xmlresources/gbcommands BY
         THE JavaClassesGenerator.xslt. MODIFY THAT FILE INSTEAD OF THIS ONE.
         
         Use Ant task process-gb-messages after that to generate .java files again.
         
         IMPORTANT END !!!
        */
 	package cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands;import java.util.*;import javax.vecmath.*;import cz.cuni.amis.pogamut.base.communication.messages.*;import cz.cuni.amis.pogamut.base.communication.worldview.*;import cz.cuni.amis.pogamut.base.communication.worldview.event.*;import cz.cuni.amis.pogamut.base.communication.worldview.object.*;import cz.cuni.amis.pogamut.multi.communication.worldview.object.*;import cz.cuni.amis.pogamut.base.communication.translator.event.*;import cz.cuni.amis.pogamut.multi.communication.translator.event.*;import cz.cuni.amis.pogamut.base3d.worldview.object.*;import cz.cuni.amis.pogamut.base3d.worldview.object.event.*;import cz.cuni.amis.pogamut.ut2004.communication.messages.*;import cz.cuni.amis.pogamut.ut2004.communication.worldview.objects.*;import cz.cuni.amis.pogamut.ut2004multi.communication.worldview.objects.*;import cz.cuni.amis.pogamut.ut2004.communication.translator.itemdescriptor.*;import cz.cuni.amis.pogamut.ut2004.communication.messages.ItemType.Category;import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;import cz.cuni.amis.utils.exception.*;import cz.cuni.amis.pogamut.base.communication.translator.event.IWorldObjectUpdateResult.Result;import cz.cuni.amis.utils.SafeEquals;import cz.cuni.amis.pogamut.base.agent.*;import cz.cuni.amis.pogamut.multi.agent.*;import cz.cuni.amis.pogamut.multi.communication.worldview.property.*;import cz.cuni.amis.pogamut.ut2004multi.communication.worldview.property.*;import cz.cuni.amis.utils.token.*;import cz.cuni.amis.utils.*;
 		/**
 		 * Representation of the GameBots2004 command ACT.
 		 *
 		 * 
		Will trigger some of the native animations.
	
         */
 	public class PlayAnimation 
		extends CommandMessage
	        {
	        	
		        
    	/** Example how the message looks like - used during parser tests. */
    	public static final String PROTOTYPE =
    		" {Name text}  {Loop False} ";
    
		/**
		 * Creates new instance of command PlayAnimation.
		 * 
		Will trigger some of the native animations.
	
		 * Corresponding GameBots message for this command is
		 * ACT.
		 *
		 * 
		 *    @param Name Name of the animation. Possible animations: gesture_beckon, gesture_cheer, gesture_halt, gesture_point, Gesture_Taunt01, PThrust, AssSmack, ThroatCut, Specific_1, Gesture_Taunt02, Idle_Character02, Idle_Character03, Gesture_Taunt03.
		 *    @param Loop When true the animation will be played in loop. Supported un GameBotsUE2.
		 */
		public PlayAnimation(
			String Name,  Boolean Loop
		) {
			
				this.Name = Name;
            
				this.Loop = Loop;
            
		}

		
			/**
			 * Creates new instance of command PlayAnimation.
			 * 
		Will trigger some of the native animations.
	
			 * Corresponding GameBots message for this command is
			 * ACT.
			 * <p></p>
			 * WARNING: this is empty-command constructor, you have to use setters to fill it up with data that should be sent to GameBots2004!
		     */
		    public PlayAnimation() {
		    }
			
		
		/**
		 * Cloning constructor.
		 *
		 * @param original
		 */
		public PlayAnimation(PlayAnimation original) {
		   
		        this.Name = original.Name;
		   
		        this.Loop = original.Loop;
		   
		}
    
	        /**
	        Name of the animation. Possible animations: gesture_beckon, gesture_cheer, gesture_halt, gesture_point, Gesture_Taunt01, PThrust, AssSmack, ThroatCut, Specific_1, Gesture_Taunt02, Idle_Character02, Idle_Character03, Gesture_Taunt03. 
	        */
	        protected
	         String Name =
	       	
	        		null
	        	;
	
	        
	        
 		/**
         * Name of the animation. Possible animations: gesture_beckon, gesture_cheer, gesture_halt, gesture_point, Gesture_Taunt01, PThrust, AssSmack, ThroatCut, Specific_1, Gesture_Taunt02, Idle_Character02, Idle_Character03, Gesture_Taunt03. 
         */
        public String getName()
 	
	        {
	            return
	        	 Name;
	        }
	        
	        
	        
 		
 		/**
         * Name of the animation. Possible animations: gesture_beckon, gesture_cheer, gesture_halt, gesture_point, Gesture_Taunt01, PThrust, AssSmack, ThroatCut, Specific_1, Gesture_Taunt02, Idle_Character02, Idle_Character03, Gesture_Taunt03. 
         */
        public PlayAnimation 
        setName(String Name)
 	
			{
				this.Name = Name;
				return this;
			}
		
	        /**
	        When true the animation will be played in loop. Supported un GameBotsUE2. 
	        */
	        protected
	         Boolean Loop =
	       	
	        		null
	        	;
	
	        
	        
 		/**
         * When true the animation will be played in loop. Supported un GameBotsUE2. 
         */
        public Boolean isLoop()
 	
	        {
	            return
	        	 Loop;
	        }
	        
	        
	        
 		
 		/**
         * When true the animation will be played in loop. Supported un GameBotsUE2. 
         */
        public PlayAnimation 
        setLoop(Boolean Loop)
 	
			{
				this.Loop = Loop;
				return this;
			}
		
 	    public String toString() {
            return toMessage();
        }
 	
 		public String toHtmlString() {
			return super.toString() + "[<br/>" +
            	
            	"<b>Name</b> = " +
            	String.valueOf(getName()
 	) +
            	" <br/> " +
            	
            	"<b>Loop</b> = " +
            	String.valueOf(isLoop()
 	) +
            	" <br/> " +
            	 
            	"<br/>]"
            ;
		}
 	
		public String toMessage() {
     		StringBuffer buf = new StringBuffer();
     		buf.append("ACT");
     		
						if (Name != null) {
							buf.append(" {Name " + Name + "}");
						}
					
						if (Loop != null) {
							buf.append(" {Loop " + Loop + "}");
						}
					
   			return buf.toString();
   		}
 	
 		// --- Extra Java from XML BEGIN (extra/code/java)
        	
		// --- Extra Java from XML END (extra/code/java)
 	
	        }
    	