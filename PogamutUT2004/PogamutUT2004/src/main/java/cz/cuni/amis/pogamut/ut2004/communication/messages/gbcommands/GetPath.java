
	 	/**
         IMPORTANT !!!

         DO NOT EDIT THIS FILE. IT IS GENERATED FROM approriate xml file in xmlresources/gbcommands BY
         THE JavaClassesGenerator.xslt. MODIFY THAT FILE INSTEAD OF THIS ONE.
         
         Use Ant task process-gb-messages after that to generate .java files again.
         
         IMPORTANT END !!!
        */
 	package cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands;import java.util.*;import javax.vecmath.*;import cz.cuni.amis.pogamut.base.communication.messages.*;import cz.cuni.amis.pogamut.base.communication.worldview.*;import cz.cuni.amis.pogamut.base.communication.worldview.event.*;import cz.cuni.amis.pogamut.base.communication.worldview.object.*;import cz.cuni.amis.pogamut.multi.communication.worldview.object.*;import cz.cuni.amis.pogamut.base.communication.translator.event.*;import cz.cuni.amis.pogamut.multi.communication.translator.event.*;import cz.cuni.amis.pogamut.base3d.worldview.object.*;import cz.cuni.amis.pogamut.base3d.worldview.object.event.*;import cz.cuni.amis.pogamut.ut2004.communication.messages.*;import cz.cuni.amis.pogamut.ut2004.communication.worldview.objects.*;import cz.cuni.amis.pogamut.ut2004multi.communication.worldview.objects.*;import cz.cuni.amis.pogamut.ut2004.communication.translator.itemdescriptor.*;import cz.cuni.amis.pogamut.ut2004.communication.messages.ItemType.Category;import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;import cz.cuni.amis.utils.exception.*;import cz.cuni.amis.pogamut.base.communication.translator.event.IWorldObjectUpdateResult.Result;import cz.cuni.amis.utils.SafeEquals;import cz.cuni.amis.pogamut.base.agent.*;import cz.cuni.amis.pogamut.multi.agent.*;import cz.cuni.amis.pogamut.multi.communication.worldview.property.*;import cz.cuni.amis.pogamut.ut2004multi.communication.worldview.property.*;import cz.cuni.amis.utils.token.*;import cz.cuni.amis.utils.*;
 		/**
 		 * Representation of the GameBots2004 command GETPATH.
 		 *
 		 * 
		Get a path to a specified location or a navigation point. An ordered list of path
		nodes will be returned to you by IPTH messages.
	
         */
 	public class GetPath 
		extends CommandMessage
	        {
	        	
		        
    	/** Example how the message looks like - used during parser tests. */
    	public static final String PROTOTYPE =
    		" {Id text}  {Target unreal_id}  {Location 0,0,0} ";
    
		/**
		 * Creates new instance of command GetPath.
		 * 
		Get a path to a specified location or a navigation point. An ordered list of path
		nodes will be returned to you by IPTH messages.
	
		 * Corresponding GameBots message for this command is
		 * GETPATH.
		 *
		 * 
		 *    @param Id 
			Message Id made up by you and echoed in response so you can
			match up response with query.
		
		 *    @param Target UnrealId of navigation point you want to find path to. Will be parsed BEFORE Location attribute. If supported, Location attribute won't be parsed. If the point does not exists, blank path will be returned.
		 *    @param Location Location you want to go to.
		 */
		public GetPath(
			String Id,  UnrealId Target,  Location Location
		) {
			
				this.Id = Id;
            
				this.Target = Target;
            
				this.Location = Location;
            
		}

		
			/**
			 * Creates new instance of command GetPath.
			 * 
		Get a path to a specified location or a navigation point. An ordered list of path
		nodes will be returned to you by IPTH messages.
	
			 * Corresponding GameBots message for this command is
			 * GETPATH.
			 * <p></p>
			 * WARNING: this is empty-command constructor, you have to use setters to fill it up with data that should be sent to GameBots2004!
		     */
		    public GetPath() {
		    }
			
		
		/**
		 * Cloning constructor.
		 *
		 * @param original
		 */
		public GetPath(GetPath original) {
		   
		        this.Id = original.Id;
		   
		        this.Target = original.Target;
		   
		        this.Location = original.Location;
		   
		}
    
	        /**
	        
			Message Id made up by you and echoed in response so you can
			match up response with query.
		 
	        */
	        protected
	         String Id =
	       	
	        		null
	        	;
	
	        
	        
 		/**
         * 
			Message Id made up by you and echoed in response so you can
			match up response with query.
		 
         */
        public String getId()
 	
	        {
	            return
	        	 Id;
	        }
	        
	        
	        
 		
 		/**
         * 
			Message Id made up by you and echoed in response so you can
			match up response with query.
		 
         */
        public GetPath 
        setId(String Id)
 	
			{
				this.Id = Id;
				return this;
			}
		
	        /**
	        UnrealId of navigation point you want to find path to. Will be parsed BEFORE Location attribute. If supported, Location attribute won't be parsed. If the point does not exists, blank path will be returned. 
	        */
	        protected
	         UnrealId Target =
	       	
	        		null
	        	;
	
	        
	        
 		/**
         * UnrealId of navigation point you want to find path to. Will be parsed BEFORE Location attribute. If supported, Location attribute won't be parsed. If the point does not exists, blank path will be returned. 
         */
        public UnrealId getTarget()
 	
	        {
	            return
	        	 Target;
	        }
	        
	        
	        
 		
 		/**
         * UnrealId of navigation point you want to find path to. Will be parsed BEFORE Location attribute. If supported, Location attribute won't be parsed. If the point does not exists, blank path will be returned. 
         */
        public GetPath 
        setTarget(UnrealId Target)
 	
			{
				this.Target = Target;
				return this;
			}
		
	        /**
	        Location you want to go to. 
	        */
	        protected
	         Location Location =
	       	
	        		null
	        	;
	
	        
	        
 		/**
         * Location you want to go to. 
         */
        public Location getLocation()
 	
	        {
	            return
	        	 Location;
	        }
	        
	        
	        
 		
 		/**
         * Location you want to go to. 
         */
        public GetPath 
        setLocation(Location Location)
 	
			{
				this.Location = Location;
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
            	
            	"<b>Target</b> = " +
            	String.valueOf(getTarget()
 	) +
            	" <br/> " +
            	
            	"<b>Location</b> = " +
            	String.valueOf(getLocation()
 	) +
            	" <br/> " +
            	 
            	"<br/>]"
            ;
		}
 	
		public String toMessage() {
     		StringBuffer buf = new StringBuffer();
     		buf.append("GETPATH");
     		
						if (Id != null) {
							buf.append(" {Id " + Id + "}");
						}
					
						if (Target != null) {
							buf.append(" {Target " + Target.getStringId() + "}");
						}
					
					    if (Location != null) {
					        buf.append(" {Location " +
					            Location.getX() + "," +
					            Location.getY() + "," +
					            Location.getZ() + "}");
					    }
					
   			return buf.toString();
   		}
 	
 		// --- Extra Java from XML BEGIN (extra/code/java)
        	
		// --- Extra Java from XML END (extra/code/java)
 	
	        }
    	